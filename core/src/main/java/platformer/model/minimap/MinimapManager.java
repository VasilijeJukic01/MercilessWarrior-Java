package platformer.model.minimap;

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

import static platformer.constants.FilePaths.MINIMAP;
import static platformer.constants.FilePaths.MINIMAP_ICONS;

/**
 * MinimapManager handles minimap operations such as finding player spawn, level positions and updating player position.
 */
public class MinimapManager {

    private BufferedImage minimapImage;
    private final BufferedImage[] minimapIcons = new BufferedImage[6];

    private BufferedImage minimap;
    private Point playerLocation;
    private Map<String, Point> levelPositions;
    private final List<MinimapIcon> icons = new ArrayList<>();

    private int flashTick = 0;
    private final int flashSpeed = 40;
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
        BufferedImage image = Utils.getInstance().importImage(MINIMAP_ICONS, -1, -1);
        for (int i = 0; i < 6; i++) {
            minimapIcons[i] = image.getSubimage(i * 16, 0, 16, 16);
        }
    }

    private void findLevelPositions() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                String levelName = "level" + i + j;
                BufferedImage levelImage = Utils.getInstance().importImage("/images/levels/"+levelName+".png", -1, -1);
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
                    minimapImage.setRGB(x, y, new Color(78, 105, 80).getRGB());
                }
                else if (r == 254 && g == 254 && b == 254)
                    minimapImage.setRGB(x, y, new Color(78, 105, 80).getRGB());
                else {
                    minimapImage.setRGB(x, y, new Color(41, 59, 41).getRGB());
                }
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
        if (flashTick >= flashSpeed) {
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
    }

    /**
     * Clamps the player's position to ensure it stays within the bounds of the minimap.
     */
    private void clampPlayerPosition() {
        playerLocation.x = Math.max(0, Math.min(playerLocation.x, minimap.getWidth() - 1));
        playerLocation.y = Math.max(0, Math.min(playerLocation.y, minimap.getHeight() - 1));
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
}