package platformer.ui.overlays.hud;

import platformer.model.minimap.MinimapManager;
import platformer.ui.overlays.render.MinimapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.MINIMAP_ZOOM;

public class MinimapPanel {

    private final MinimapManager minimapManager;
    private final MinimapRenderer minimapRenderer;

    private final int x, y, width, height;
    private int offsetX = 0, offsetY = 0;

    public MinimapPanel(MinimapManager minimapManager, int x, int y, int width, int height) {
        this.minimapManager = minimapManager;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minimapRenderer = new MinimapRenderer();
    }

    public void render(Graphics g) {
        centerOnPlayer();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(x, y, width, height);
        renderMinimap(g2d);
        g2d.setClip(null);
    }

    private void renderMinimap(Graphics2D g2d) {
        BufferedImage minimap = minimapManager.getMinimap();
        g2d.setClip(x, y, width, height);

        int mapWidth = (int) (minimap.getWidth() * MINIMAP_ZOOM);
        int mapHeight = (int) (minimap.getHeight() * MINIMAP_ZOOM);
        int mapX = x + offsetX;
        int mapY = y + offsetY;

        g2d.drawImage(minimap, mapX, mapY, mapWidth, mapHeight, null);
        minimapRenderer.renderIcons(g2d, mapX, mapY, mapWidth, mapHeight, minimap, minimapManager);
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