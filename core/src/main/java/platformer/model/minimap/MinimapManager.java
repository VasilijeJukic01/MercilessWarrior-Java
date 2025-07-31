package platformer.model.minimap;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.Level;
import platformer.model.minimap.astar.AStarPathfinding;
import platformer.state.GameState;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static platformer.constants.AnimConstants.MINIMAP_FLASH_ANIM_SPEED;
import static platformer.constants.AnimConstants.MINIMAP_ICON_SIZE;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * MinimapManager handles minimap operations such as finding player spawn, level positions and updating player position.
 */
public class MinimapManager {

    private final GameState gameState;

    private BufferedImage minimapImage;
    private BufferedImage[] minimapIcons;

    private BufferedImage minimap;
    private Point2D.Double playerLocation;
    private Point pinnedLocation;
    private List<Point> pathPoints = new ArrayList<>();
    private Map<String, Point> levelPositions;
    private final List<MinimapIcon> icons = new ArrayList<>();

    private int flashTick = 0;
    private boolean flashVisible = true;

    private MinimapIcon hoveredIcon = null;
    private MinimapIcon pinnedIcon = null;

    public MinimapManager(GameState gameState) {
        this.gameState = gameState;
        init();
    }

    private void init() {
        this.minimapImage = ImageUtils.importImage(MINIMAP, -1, -1);
        this.minimap = new BufferedImage(minimapImage.getWidth(), minimapImage.getHeight(), minimapImage.getType());
        this.levelPositions = new LinkedHashMap<>();
        loadMinimapIcons();
        findLevelPositions();
        colorizeMinimap();
    }

    private void loadMinimapIcons() {
        this.minimapIcons = new BufferedImage[MINIMAP_ICONS_COUNT];
        BufferedImage image = ImageUtils.importImage(MINIMAP_ICONS, -1, -1);
        for (int i = 0; i < MINIMAP_ICONS_COUNT; i++) {
            minimapIcons[i] = image.getSubimage(i * MINIMAP_ICON_SIZE, 0, MINIMAP_ICON_SIZE, MINIMAP_ICON_SIZE);
        }
    }

    private void findLevelPositions() {
        for (int i = 0; i < MAX_LEVELS; i++) {
            for (int j = 0; j < MAX_LEVELS; j++) {
                String levelName = "level" + i + j;
                BufferedImage levelImage = ImageUtils.importImage(LEVEL_SPRITES.replace("level$", levelName), -1, -1);
                if (levelImage == null) continue;
                BufferedImage img = levelImage.getSubimage(0, 0, levelImage.getWidth() / 2, levelImage.getHeight());
                Point position = findImageOnMinimap(img, minimapImage);
                if (position != null) levelPositions.put(levelName, position);
            }
        }
    }

    private void colorizeMinimap() {
        for (int y = 0; y < minimapImage.getHeight(); y++) {
            for (int x = 0; x < minimapImage.getWidth(); x++) {
                int rgb = minimapImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r == 254 && g == 254 && b >= 1 && b <= 5) {
                    icons.add(new MinimapIcon(new Point(x, y), MinimapIconType.values()[b]));
                    minimapImage.setRGB(x, y, MINIMAP_WALKABLE.getRGB());
                }
                else if (r == 254 && g == 254 && b == 254) minimapImage.setRGB(x, y, MINIMAP_WALKABLE.getRGB());
                else minimapImage.setRGB(x, y, MINIMAP_UNWALKABLE.getRGB());
            }
        }
    }

    /**
     * Finds the position of a level image on the minimap using the Sum of Absolute Differences (SAD) technique.
     * <p>
     * Time complexity: O(W_minimap × H_minimap × W_level × H_level).
     * <p>
     * @param levelImage The smaller image (level) to locate on the larger image (minimap).
     * @param minimapImage The larger image (minimap) to search within.
     * @return The top-left position of the level image on the minimap if found.
     */
    public Point findImageOnMinimap(BufferedImage levelImage, BufferedImage minimapImage) {
        int levelWidth = levelImage.getWidth(), levelHeight = levelImage.getHeight();
        int minimapWidth = minimapImage.getWidth(), minimapHeight = minimapImage.getHeight();

        // Images -> Grayscale
        int[][] minimapGray = ImageUtils.toGrayscale(minimapImage);
        int[][] levelGray = ImageUtils.toGrayscale(levelImage);

        // Thread-safe SAD Tracking
        AtomicInteger minSAD = new AtomicInteger(Integer.MAX_VALUE);
        AtomicReference<Point> bestMatch = new AtomicReference<>(null);

        // Parallelized Sliding Window
        IntStream.range(0, minimapHeight - levelHeight + 1).parallel().forEach(y -> {
            for (int x = 0; x <= minimapWidth - levelWidth; x++) {
                // Computing SAD for the current window
                int sad = computeSAD(minimapGray, levelGray, x, y, levelWidth, levelHeight, minSAD.get());
                if (sad < minSAD.get()) {
                    minSAD.set(sad);
                    bestMatch.set(new Point(x, y));
                }
            }
        });

        return bestMatch.get();
    }

    /**
     * Computes the Sum of Absolute Differences (SAD) for a specific window in the minimap.
     * SAD is a pixel-by-pixel comparison metric (measures the difference between two images by summing the absolute differences of their pixel values).
     *
     * @param minimapGray Grayscale representation of the minimap image.
     * @param levelGray Grayscale representation of the level image.
     * @param startX Top-left X coordinate of the current window in the minimap.
     * @param startY Top-left Y coordinate of the current window in the minimap.
     * @param levelWidth Width of the level image.
     * @param levelHeight Height of the level image.
     * @param currentMinSAD Current minimum SAD (for early stopping).
     * @return The SAD value for the current window.
     */
    private int computeSAD(int[][] minimapGray, int[][] levelGray, int startX, int startY, int levelWidth, int levelHeight, int currentMinSAD) {
        int sad = 0;
        for (int j = 0; j < levelHeight; j++) {
            for (int i = 0; i < levelWidth; i++) {
                int minimapPixel = minimapGray[startY + j][startX + i];
                int levelPixel = levelGray[j][i];
                // Computing the absolute difference
                int dx = Math.abs(minimapPixel - levelPixel);
                sad += dx;

                // Early stopping
                if (sad > currentMinSAD) return Integer.MAX_VALUE;
            }
        }
        return sad;
    }

    /**
     * Updates the flashing effect for the player's position.
     */
    public void update() {
        flashTick++;
        if (flashTick >= MINIMAP_FLASH_ANIM_SPEED) {
            flashTick = 0;
            flashVisible = !flashVisible;
        }
    }

    /**
     * Updates the player's position on the minimap based on their world coordinates.
     * This ensures the player's icon is accurately placed on the minimap, reflecting their position within the level.
     *
     * @param playerWorldX The player's X coordinate in world pixels.
     * @param playerWorldY The player's Y coordinate in world pixels.
     */
    public void updatePlayerPosition(double playerWorldX, double playerWorldY) {
        Level currentLevel = gameState.getLevelManager().getCurrentLevel();
        int levelWidthInPixels = currentLevel.getLevelTilesWidth() * TILES_SIZE;
        int levelHeightInPixels = currentLevel.getLevelTilesHeight() * TILES_SIZE;

        String levelName = "level" + gameState.getLevelManager().getLevelIndexI() + gameState.getLevelManager().getLevelIndexJ();
        Point levelPosOnMinimap = levelPositions.get(levelName);
        if (levelPosOnMinimap == null) return;

        int levelWidthOnMinimap = currentLevel.getLevelTilesWidth();
        int levelHeightOnMinimap = currentLevel.getLevelTilesHeight();

        double relativeX = playerWorldX / levelWidthInPixels;
        double relativeY = playerWorldY / levelHeightInPixels;

        double playerMapX = levelPosOnMinimap.x + (relativeX * levelWidthOnMinimap);
        double playerMapY = levelPosOnMinimap.y + (relativeY * levelHeightOnMinimap);

        if (playerLocation == null) playerLocation = new Point2D.Double(playerMapX, playerMapY);
        else playerLocation.setLocation(playerMapX, playerMapY);

        updatePinnedLocation();
    }

    private void updatePinnedLocation() {
        if (pinnedLocation == null) return;
        Point playerIntLocation = getPlayerIntegerLocation();
        if (playerIntLocation != null && playerIntLocation.equals(pinnedLocation)) {
            unpin();
            return;
        }
        findPath();
    }

    private void updateMinimap() {
        Graphics2D g2d = minimap.createGraphics();
        g2d.drawImage(minimapImage, 0, 0, null);
        g2d.dispose();
    }

    public void changeLevel() {
        this.playerLocation = null;
    }

    public void handlePinRequest(Point location, MinimapIcon icon) {
        if (icon != null) {
            if (icon.equals(pinnedIcon)) unpin();
            else {
                pinnedIcon = icon;
                pinnedLocation = icon.position();
                findPath();
                Logger.getInstance().notify("Minimap icon pinned: " + icon.type(), Message.NOTIFICATION);
            }
        }
        else {
            if (location.equals(pinnedLocation)) unpin();
            else {
                pinnedIcon = null;
                pinnedLocation = location;
                findPath();
                Logger.getInstance().notify("Minimap location pinned at: (" + location.x + ", " + location.y + ")", Message.NOTIFICATION);
            }
        }
    }

    public void unpin() {
        pinnedIcon = null;
        pinnedLocation = null;
        pathPoints.clear();
    }

    public void findPath() {
        Point playerIntLocation = getPlayerIntegerLocation();
        if (playerIntLocation == null || pinnedLocation == null) {
            pathPoints.clear();
            return;
        }
        AStarPathfinding pathfinding = new AStarPathfinding();
        pathPoints = pathfinding.findPath(minimapImage, playerIntLocation, pinnedLocation);
    }

    public void reset() {
        unpin();
    }

    // Getters & Setters
    public BufferedImage getMinimap() {
        updateMinimap();
        return minimap;
    }

    public Point2D.Double getPlayerLocation() {
        return playerLocation;
    }

    private Point getPlayerIntegerLocation() {
        if (playerLocation == null) return null;
        return new Point((int)Math.round(playerLocation.x), (int)Math.round(playerLocation.y));
    }

    public Point getPinnedLocation() {
        return pinnedLocation;
    }

    public List<MinimapIcon> getIcons() {
        return icons;
    }

    public BufferedImage[] getMinimapIcons() {
        return minimapIcons;
    }

    public boolean isFlashVisible() {
        return flashVisible;
    }

    public List<Point> getPathPoints() {
        return pathPoints;
    }

    public void setHoveredIcon(MinimapIcon icon) {
        this.hoveredIcon = icon;
    }

    public MinimapIcon getHoveredIcon() {
        return hoveredIcon;
    }

    public MinimapIcon getPinnedIcon() {
        return pinnedIcon;
    }
}