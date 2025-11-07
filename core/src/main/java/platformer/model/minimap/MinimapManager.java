package platformer.model.minimap;

import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.Level;
import platformer.model.levels.LevelManager;
import platformer.model.minimap.astar.AStarPathfinding;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.util.*;
import java.util.List;
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

    private GameContext context;

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

    private float[][] explorationMap;
    private BufferedImage fogImage;
    private ConvolveOp convolution;

    public MinimapManager() {
        init();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    private void init() {
        this.minimapImage = ImageUtils.importImage(MINIMAP, -1, -1);
        this.explorationMap = new float[minimapImage.getWidth()][minimapImage.getHeight()];
        this.fogImage = new BufferedImage(minimapImage.getWidth(), minimapImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        initializeBlurFilter();
        this.minimap = new BufferedImage(minimapImage.getWidth(), minimapImage.getHeight(), minimapImage.getType());
        this.levelPositions = new LinkedHashMap<>();
        loadMinimapIcons();
        findLevelPositions();
        colorizeMinimap();
    }

    private void initializeBlurFilter() {
        float[] kernel = new float[BLUR_KERNEL_SIZE * BLUR_KERNEL_SIZE];
        Arrays.fill(kernel, 1.0f / (BLUR_KERNEL_SIZE * BLUR_KERNEL_SIZE));
        Kernel blurKernel = new Kernel(BLUR_KERNEL_SIZE, BLUR_KERNEL_SIZE, kernel);
        this.convolution = new ConvolveOp(blurKernel, ConvolveOp.EDGE_NO_OP, null);
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
        updateFogImage();
    }

    /**
     * Updates the player's position on the minimap based on their world coordinates.
     * This ensures the player's icon is accurately placed on the minimap, reflecting their position within the level.
     *
     * @param playerWorldX The player's X coordinate in world pixels.
     * @param playerWorldY The player's Y coordinate in world pixels.
     */
    public void updatePlayerPosition(double playerWorldX, double playerWorldY) {
        LevelManager levelManager = context.getLevelManager();
        Level currentLevel = levelManager.getCurrentLevel();
        int levelWidthInPixels = currentLevel.getLevelTilesWidth() * TILES_SIZE;
        int levelHeightInPixels = currentLevel.getLevelTilesHeight() * TILES_SIZE;

        String levelName = "level" + levelManager.getLevelIndexI() + levelManager.getLevelIndexJ();
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

        revealMapArea((int) playerLocation.x, (int) playerLocation.y);
        updatePinnedLocation();
    }

    private void revealMapArea(int centerX, int centerY) {
        int startX = Math.max(0, centerX - MAP_FOG_RADIUS);
        int endX = Math.min(explorationMap.length - 1, centerX + MAP_FOG_RADIUS);
        int startY = Math.max(0, centerY - MAP_FOG_RADIUS);
        int endY = Math.min(explorationMap[0].length - 1, centerY + MAP_FOG_RADIUS);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (Point2D.distance(centerX, centerY, x, y) <= MAP_FOG_RADIUS)
                    explorationMap[x][y] = 1.0f;
            }
        }
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

    public void updateFogImage() {
        int width = minimapImage.getWidth();
        int height = minimapImage.getHeight();
        int padding = BLUR_KERNEL_SIZE;

        BufferedImage hardFog = new BufferedImage(width + 2 * padding, height + 2 * padding, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = hardFog.getRaster();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = explorationMap[x][y] < 1.0f ? 255 : 0;
                raster.setSample(x + padding, y + padding, 0, value);
            }
        }

        // Edge Replication
        for (int x = 0; x < width; x++) {
            int topPixel = raster.getSample(x + padding, padding, 0);
            int bottomPixel = raster.getSample(x + padding, height + padding - 1, 0);
            for (int p = 0; p < padding; p++) {
                raster.setSample(x + padding, p, 0, topPixel);
                raster.setSample(x + padding, height + padding + p, 0, bottomPixel);
            }
        }
        for (int y = 0; y < height + 2 * padding; y++) {
            int leftPixel = raster.getSample(padding, y, 0);
            int rightPixel = raster.getSample(width + padding - 1, y, 0);
            for (int p = 0; p < padding; p++) {
                raster.setSample(p, y, 0, leftPixel);
                raster.setSample(width + padding + p, y, 0, rightPixel);
            }
        }

        BufferedImage blurredPaddedFog = convolution.filter(hardFog, null);
        BufferedImage blurredFog = blurredPaddedFog.getSubimage(padding, padding, width, height);

        int fogRgb = MAP_FOG_COLOR.getRGB() & 0x00FFFFFF;
        Raster blurredRaster = blurredFog.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = blurredRaster.getSample(x, y, 0);
                int finalColor = (alpha << 24) | fogRgb;
                fogImage.setRGB(x, y, finalColor);
            }
        }
    }

    public void loadExplorationData(String base64) {
        if (base64 == null || base64.isEmpty()) {
            this.explorationMap = new float[minimapImage.getWidth()][minimapImage.getHeight()];
            return;
        }
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            int width = minimapImage.getWidth();
            int height = minimapImage.getHeight();
            this.explorationMap = new float[width][height];
            for (int i = 0; i < data.length; i++) {
                int x = i % width;
                int y = i / width;
                this.explorationMap[x][y] = (data[i] & 0xFF) / 255.0f;
            }
        } catch (Exception e) {
            this.explorationMap = new float[minimapImage.getWidth()][minimapImage.getHeight()];
            Logger.getInstance().notify("Could not load minimap exploration data. Resetting fog.", Message.WARNING);
        }
    }

    public String getExplorationDataForSave() {
        int width = explorationMap.length;
        int height = explorationMap[0].length;
        byte[] data = new byte[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y * width + x] = (byte) (explorationMap[x][y] * 255);
            }
        }
        return Base64.getEncoder().encodeToString(data);
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
        playerLocation = null;
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

    public BufferedImage getFogImage() {
        return fogImage;
    }
}