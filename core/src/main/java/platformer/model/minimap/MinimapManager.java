package platformer.model.minimap;

import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

import static platformer.constants.FilePaths.MINIMAP;

/**
 * MinimapManager handles minimap operations such as finding player spawn, level positions and updating player position.
 */
public class MinimapManager {

    private BufferedImage minimapImage;

    private BufferedImage minimap;
    private Point playerLocation;
    private Map<String, Point> levelPositions;

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
        findLevelPositions();
    }

    private void findLevelPositions() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                String levelName = "level" + i + j;
                BufferedImage levelImage = Utils.getInstance().importImage("/images/levels/"+levelName+".png", -1, -1);
                if (levelImage == null) continue;
                BufferedImage img = levelImage.getSubimage(0, 0, levelImage.getWidth() / 2, levelImage.getHeight());
                Point position = findImageOnMinimap(img);
                if (position != null) levelPositions.put(levelName, position);
            }
        }
    }

    /**
     * Finds the position of a level image on the minimap.
     *
     * @param levelImage The level image to find on the minimap.
     * @return The position of the level image on the minimap.
     */
    private Point findImageOnMinimap(BufferedImage levelImage) {
        int levelWidth = levelImage.getWidth();
        int levelHeight = levelImage.getHeight();
        int totalPixels = levelWidth * levelHeight;
        int requiredMatches = (int) (totalPixels * 0.9);

        for (int y = 0; y <= minimapImage.getHeight() - levelHeight; y++) {
            for (int x = 0; x <= minimapImage.getWidth() - levelWidth; x++) {
                int matchCount = 0;
                boolean earlyExit = false;

                for (int j = 0; j < levelHeight && !earlyExit; j++) {
                    for (int i = 0; i < levelWidth; i++) {
                        if (minimapImage.getRGB(x + i, y + j) == levelImage.getRGB(i, j)) {
                            matchCount++;
                        }
                        if (totalPixels - (j * levelWidth + i + 1) + matchCount < requiredMatches) {
                            earlyExit = true;
                            break;
                        }
                    }
                }

                if (matchCount >= requiredMatches) {
                    System.out.println("DEBUG MINIMAP LEVEL: Match percent: " + (matchCount * 100 / totalPixels) + "%");
                    return new Point(x, y);
                }
            }
        }
        return null;
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

    // TODO: Maybe transfer this to overlay?
    private void updateMinimap() {
        Graphics2D g2d = minimap.createGraphics();
        g2d.drawImage(minimapImage, 0, 0, null);
        if (playerLocation != null && flashVisible) {
            g2d.setColor(Color.RED);
            g2d.fillOval(playerLocation.x, playerLocation.y - 1, 1, 2);
        }
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
}