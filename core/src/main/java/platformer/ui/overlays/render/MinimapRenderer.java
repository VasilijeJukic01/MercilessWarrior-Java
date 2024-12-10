package platformer.ui.overlays.render;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapIconType;
import platformer.model.minimap.MinimapManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static platformer.constants.Constants.SCALE;

public class MinimapRenderer {

    public void renderIcons(Graphics2D g2d, int mapX, int mapY, int mapWidth, int mapHeight, BufferedImage minimap, MinimapManager minimapManager) {
        double scaledPixelWidth = (double) mapWidth / minimap.getWidth();
        double scaledPixelHeight = (double) mapHeight / minimap.getHeight();
        List<MinimapIcon> icons = minimapManager.getIcons();
        BufferedImage[] minimapIcons = minimapManager.getMinimapIcons();

        for (MinimapIcon icon : icons) {
            BufferedImage img = minimapIcons[icon.type().ordinal()];
            int iconWidth = (int) (1.8 * scaledPixelWidth);
            int iconHeight = (int) (1.8 * scaledPixelHeight);
            int iconX = mapX + (int) (icon.position().x * scaledPixelWidth) - iconWidth / 3;
            int iconY = mapY + (int) (icon.position().y * scaledPixelHeight) - (int)(iconHeight / 1.8);
            g2d.drawImage(img, iconX, iconY, iconWidth, iconHeight, null);
        }

        if (minimapManager.isFlashVisible()) renderPlayer(g2d, mapX, mapY, mapWidth, mapHeight, minimap, minimapManager);
        renderPinnedLocation(g2d, mapX, mapY, mapWidth, mapHeight, minimap, minimapManager);
        renderPath(g2d, mapX, mapY, mapWidth, mapHeight, minimap, minimapManager);
    }

    private void renderPlayer(Graphics2D g2d, int mapX, int mapY, int mapWidth, int mapHeight, BufferedImage minimap, MinimapManager minimapManager) {
        Point playerLocation = minimapManager.getPlayerLocation();

        if (playerLocation != null) {
            double scaledPixelWidth = (double) mapWidth / minimap.getWidth();
            double scaledPixelHeight = (double) mapHeight / minimap.getHeight();
            BufferedImage playerIcon = minimapManager.getMinimapIcons()[MinimapIconType.PLAYER.ordinal()];
            int playerIconWidth = (int) (1.8 * scaledPixelWidth);
            int playerIconHeight = (int) (1.8 * scaledPixelHeight);
            int playerIconX = mapX + (int) (playerLocation.x * scaledPixelWidth) - playerIconWidth / 3;
            int playerIconY = mapY + (int) (playerLocation.y * scaledPixelHeight) - playerIconHeight / 2;

            g2d.drawImage(playerIcon, playerIconX, playerIconY, playerIconWidth, playerIconHeight, null);
        }
    }

    private void renderPinnedLocation(Graphics2D g2d, int x, int y, int width, int height, BufferedImage map, MinimapManager minimapManager) {
        Point pinnedLocation = minimapManager.getPinnedLocation();
        if (pinnedLocation != null) {
            double scaledPixelWidth = (double) width / map.getWidth();
            double scaledPixelHeight = (double) height / map.getHeight();
            BufferedImage pinIcon = minimapManager.getMinimapIcons()[MinimapIconType.FLAG.ordinal()];
            int pinIconWidth = (int) (1.8 * scaledPixelWidth);
            int pinIconHeight = (int) (1.8 * scaledPixelHeight);
            int pinX = x + (int) (pinnedLocation.x * scaledPixelWidth) + pinIconWidth / 6;
            int pinY = y + (int) (pinnedLocation.y * scaledPixelHeight) - pinIconHeight / 2;

            g2d.drawImage(pinIcon, pinX, pinY, pinIconWidth, pinIconHeight, null);
        }
    }

    private void renderPath(Graphics2D g2d, int mapX, int mapY, int mapWidth, int mapHeight, BufferedImage minimap, MinimapManager minimapManager) {
        double scaledPixelWidth = (double) mapWidth / minimap.getWidth();
        double scaledPixelHeight = (double) mapHeight / minimap.getHeight();

        List<Point> pathPoints = minimapManager.getPathPoints();
        g2d.setColor(Color.YELLOW);
        for (Point point : pathPoints) {
            int dotX = mapX + (int) (point.x * scaledPixelWidth);
            int dotY = mapY + (int) (point.y * scaledPixelHeight);
            int dotSize = (int) (2 * SCALE);
            g2d.fillOval(dotX + (int)(scaledPixelWidth / 2.1), dotY + (int)(scaledPixelHeight / 2.1), dotSize, dotSize);
        }
    }
}