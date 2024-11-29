package platformer.ui.overlays.hud;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapIconType;
import platformer.model.minimap.MinimapManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static platformer.constants.Constants.MINIMAP_ZOOM;
import static platformer.constants.Constants.SCALE;

public class MinimapPanel {

    private final MinimapManager minimapManager;
    private final int x, y, width, height;
    private int offsetX = 0, offsetY = 0;

    public MinimapPanel(MinimapManager minimapManager, int x, int y, int width, int height) {
        this.minimapManager = minimapManager;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(Graphics g) {
        centerOnPlayer();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(x, y, width, height);
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(x, y, width, height);

        BufferedImage minimap = minimapManager.getMinimap();
        g2d.setClip(x, y, width, height);

        int mapWidth = (int) (minimap.getWidth() * MINIMAP_ZOOM);
        int mapHeight = (int) (minimap.getHeight() * MINIMAP_ZOOM);
        int mapX = x + offsetX;
        int mapY = y + offsetY;

        g2d.drawImage(minimap, mapX, mapY, mapWidth, mapHeight, null);
        renderIcons(g2d, mapX, mapY, mapWidth, mapHeight, minimap);
        renderPath(g2d, mapX, mapY, mapWidth, mapHeight, minimap);
        g2d.setClip(null);
    }

    private void renderIcons(Graphics2D g2d, int mapX, int mapY, int mapWidth, int mapHeight, BufferedImage minimap) {
        double scaledPixelWidth = (double) mapWidth / minimap.getWidth();
        double scaledPixelHeight = (double) mapHeight / minimap.getHeight();

        List<MinimapIcon> icons = minimapManager.getIcons();
        BufferedImage[] minimapIcons = minimapManager.getMinimapIcons();
        for (MinimapIcon icon : icons) {
            BufferedImage img = minimapIcons[icon.getType().ordinal()];
            int iconWidth = (int) (1.8 * scaledPixelWidth);
            int iconHeight = (int) (1.8 * scaledPixelHeight);
            int iconX = mapX + (int) (icon.getPosition().x * scaledPixelWidth) - iconWidth / 3;
            int iconY = mapY + (int) (icon.getPosition().y * scaledPixelHeight) - (int)(iconHeight / 1.8);
            g2d.drawImage(img, iconX, iconY, iconWidth, iconHeight, null);
        }

        if (minimapManager.isFlashVisible()) {
            Point playerLocation = minimapManager.getPlayerLocation();
            if (playerLocation != null) {
                BufferedImage playerIcon = minimapIcons[MinimapIconType.PLAYER.ordinal()];
                int playerIconWidth = (int) (1.8 * scaledPixelWidth);
                int playerIconHeight = (int) (1.8 * scaledPixelHeight);
                int playerIconX = mapX + (int) (playerLocation.x * scaledPixelWidth) - playerIconWidth / 3;
                int playerIconY = mapY + (int) (playerLocation.y * scaledPixelHeight) - playerIconHeight / 2;
                g2d.drawImage(playerIcon, playerIconX, playerIconY, playerIconWidth, playerIconHeight, null);
            }
        }
    }

    private void renderPath(Graphics2D g2d, int mapX, int mapY, int mapWidth, int mapHeight, BufferedImage minimap) {
        double scaledPixelWidth = (double) mapWidth / minimap.getWidth();
        double scaledPixelHeight = (double) mapHeight / minimap.getHeight();

        List<Point> pathPoints = minimapManager.getPathPoints();
        g2d.setColor(Color.YELLOW);
        for (Point point : pathPoints) {
            int dotX = mapX + (int) (point.x * scaledPixelWidth);
            int dotY = mapY + (int) (point.y * scaledPixelHeight);
            int dotSize = (int) (2 * SCALE);
            g2d.fillOval(dotX - dotSize / 2, dotY - dotSize / 2, dotSize, dotSize);
        }
    }

    private void centerOnPlayer() {
        Point playerLocation = minimapManager.getPlayerLocation();
        if (playerLocation != null) {
            int mapWidth = (int) (minimapManager.getMinimap().getWidth() * MINIMAP_ZOOM);
            int mapHeight = (int) (minimapManager.getMinimap().getHeight() * MINIMAP_ZOOM);

            offsetX = (width / 2) - (int) (playerLocation.x * MINIMAP_ZOOM);
            offsetY = (height / 2) - (int) (playerLocation.y * MINIMAP_ZOOM);

            offsetX = Math.max(width - mapWidth, Math.min(0, offsetX));
            offsetY = Math.max(height - mapHeight, Math.min(0, offsetY));
        }
    }
}