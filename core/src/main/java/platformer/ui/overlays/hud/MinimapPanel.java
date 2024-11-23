package platformer.ui.overlays.hud;

import platformer.model.minimap.MinimapManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MinimapPanel {

    private final MinimapManager minimapManager;
    private final int x, y, width, height;
    private double zoomFactor = 6.5;
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
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(x, y, width, height);

        BufferedImage minimap = minimapManager.getMinimap();
        g2d.setClip(x, y, width, height);

        int mapWidth = (int) (minimap.getWidth() * zoomFactor);
        int mapHeight = (int) (minimap.getHeight() * zoomFactor);
        int mapX = x + offsetX;
        int mapY = y + offsetY;

        g2d.drawImage(minimap, mapX, mapY, mapWidth, mapHeight, null);
        g2d.setClip(null);
    }

    private void centerOnPlayer() {
        Point playerLocation = minimapManager.getPlayerLocation();
        if (playerLocation != null) {
            int mapWidth = (int) (minimapManager.getMinimap().getWidth() * zoomFactor);
            int mapHeight = (int) (minimapManager.getMinimap().getHeight() * zoomFactor);

            offsetX = (width / 2) - (int) (playerLocation.x * zoomFactor);
            offsetY = (height / 2) - (int) (playerLocation.y * zoomFactor);

            offsetX = Math.max(width - mapWidth, Math.min(0, offsetX));
            offsetY = Math.max(height - mapHeight, Math.min(0, offsetY));
        }
    }
}