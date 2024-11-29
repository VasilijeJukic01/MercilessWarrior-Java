package platformer.model.minimap;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.minimap.astar.AStarPathfinding;
import platformer.utils.Utils;

import java.awt.*;
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

    private BufferedImage minimapImage;
    private BufferedImage[] minimapIcons;

    private BufferedImage minimap;
    private Point playerLocation;
    private Point pinnedLocation;
    private List<Point> pathPoints = new ArrayList<>();
    private Map<String, Point> levelPositions;
    private final List<MinimapIcon> icons = new ArrayList<>();

    private int flashTick = 0;
    private boolean flashVisible = true;

    public MinimapManager() {
        init();
    }

    private void init() {
        this.minimapImage = Utils.getInstance().importImage(MINIMAP, -1, -1);
        this.minimap = new BufferedImage(minimapImage.getWidth(), minimapImage.getHeight(), minimapImage.getType());
        this.levelPositions = new LinkedHashMap<>();
        loadMinimapIcons();
        findLevelPositions();
        colorizeMinimap();
    }

    private void loadMinimapIcons() {
        this.minimapIcons = new BufferedImage[6];
        BufferedImage image = Utils.getInstance().importImage(MINIMAP_ICONS, -1, -1);
        for (int i = 0; i < 6; i++) {
            minimapIcons[i] = image.getSubimage(i * MINIMAP_ICON_SIZE, 0, MINIMAP_ICON_SIZE, MINIMAP_ICON_SIZE);
        }
    }

    private void findLevelPositions() {
        for (int i = 0; i < MAX_LEVELS; i++) {
            for (int j = 0; j < MAX_LEVELS; j++) {
                String levelName = "level" + i + j;
                BufferedImage levelImage = Utils.getInstance().importImage(LEVEL_SPRITES.replace("level$", levelName), -1, -1);
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
        int[][] minimapGray = Utils.getInstance().toGrayscale(minimapImage);
        int[][] levelGray = Utils.getInstance().toGrayscale(levelImage);

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
     * Updates the player's position on the minimap.
     *
     * @param dx The change in the player's x position.
     * @param dy The change in the player's y position.
     */
    public void updatePlayerPosition(int dx, int dy) {
        playerLocation.x += dx;
        playerLocation.y += dy;
        clampPlayerPosition();
        updatePinnedLocation();
    }

    /**
     * Clamps the player's position to ensure it stays within the bounds of the minimap.
     */
    private void clampPlayerPosition() {
        playerLocation.x = Math.max(0, Math.min(playerLocation.x, minimap.getWidth() - 1));
        playerLocation.y = Math.max(0, Math.min(playerLocation.y, minimap.getHeight() - 1));
    }

    private void updatePinnedLocation() {
        if (pinnedLocation == null) return;
        if (playerLocation.equals(pinnedLocation)) {
            resetPin(pinnedLocation);
            return;
        }
        minimapImage.setRGB(pinnedLocation.x, pinnedLocation.y, MINIMAP_WALKABLE.getRGB());
        findPath();
        minimapImage.setRGB(pinnedLocation.x, pinnedLocation.y, Color.BLUE.getRGB());
    }

    private void updateMinimap() {
        Graphics2D g2d = minimap.createGraphics();
        g2d.drawImage(minimapImage, 0, 0, null);
        g2d.dispose();
    }

    /**
     * Changes the player's position to a new level on the minimap.
     *
     * @param I The row index of the new level.
     * @param J The column index of the new level.
     */
    public void changeLevel(int I, int J) {
        Point newLocation = levelPositions.get("level" + I + J);
        if (newLocation != null)
            playerLocation = new Point(newLocation);
    }

    /**
     * Pins a location on the minimap.
     *
     * @param location The location to pin.
     * @param color The color to use for the pin.
     */
    public void pinLocation(Point location, Color color) {
        if (pinnedLocation != null) resetPin(pinnedLocation);
        else if (isValid(minimapImage, location)) {
            if (pinnedLocation != null) resetPin(pinnedLocation);
            pinnedLocation = location;
            findPath();
            minimapImage.setRGB(location.x, location.y, color.getRGB());
            Logger.getInstance().notify("Minimap location pinned at: (" + location.x + ", " + location.y + ")", Message.NOTIFICATION);
        }
    }

    private boolean isValid(BufferedImage image, Point point) {
        if (point.x >= 0 && point.y >= 0 && point.x < image.getWidth() && point.y < image.getHeight()) {
            int rgb = image.getRGB(point.x, point.y);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            return r == MINIMAP_WALKABLE.getRed() && g == MINIMAP_WALKABLE.getGreen() && b == MINIMAP_WALKABLE.getBlue();
        }
        return false;
    }

    private void resetPin(Point location) {
        minimapImage.setRGB(location.x, location.y, MINIMAP_WALKABLE.getRGB());
        pinnedLocation = null;
        pathPoints.clear();
    }

    public void findPath() {
        if (playerLocation == null || pinnedLocation == null) return;
        AStarPathfinding pathfinding = new AStarPathfinding();
        pathPoints = pathfinding.findPath(minimapImage, playerLocation, pinnedLocation);
    }

    public void reset() {
        if (pinnedLocation != null) resetPin(pinnedLocation);
    }

    // Getters & Setters
    public BufferedImage getMinimap() {
        updateMinimap();
        return minimap;
    }

    public Point getPlayerLocation() {
        return playerLocation;
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
}