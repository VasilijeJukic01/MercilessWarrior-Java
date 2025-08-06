package platformer.ui.overlays.controller;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Handles the interactions and logic for the minimap overlay.
 * It allows zooming, dragging, and pinning locations on the minimap.
 */
public class MinimapViewController {

    private final MinimapManager minimapManager;

    private double zoomFactor = 1.8;
    private boolean showLegend = true;

    private boolean drag = false;
    private int lastMouseX, lastMouseY;
    private int offsetX = 0, offsetY = 0;

    private boolean centered = false;

    public MinimapViewController(MinimapManager minimapManager) {
        this.minimapManager = minimapManager;
    }

    public void update() {
        if (!centered && minimapManager.getPlayerLocation() != null) {
            centerOverlay();
            centered = true;
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            if (isWithinOverlay(e.getPoint())) {
                MinimapIcon foundIcon = findIconAtScreenPos(e.getX(), e.getY());
                Point minimapCoords = screenToMinimap(e.getX(), e.getY());
                if (isValid(minimapManager.getMinimap(), minimapCoords))
                    minimapManager.handlePinRequest(minimapCoords, foundIcon);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (drag && zoomFactor > MIN_ZOOM) {
            int dx = e.getX() - lastMouseX;
            int dy = e.getY() - lastMouseY;
            offsetX += dx;
            offsetY += dy;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            clampOffsets();
        }
    }

    public void mousePressed(MouseEvent e) {
        if (isWithinOverlay(e.getPoint()) && zoomFactor > MIN_ZOOM) {
            drag = true;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    public void mouseReleased(MouseEvent e) {
        drag = false;
    }

    public void mouseMoved(MouseEvent e) {
        if (isWithinOverlay(e.getPoint())) {
            MinimapIcon foundIcon = findIconAtScreenPos(e.getX(), e.getY());
            minimapManager.setHoveredIcon(foundIcon);
        }
        else minimapManager.setHoveredIcon(null);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS) {
            zoomFactor = Math.min(zoomFactor + 0.1, MAX_ZOOM);
        }
        else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            zoomFactor = Math.max(zoomFactor - 0.1, MIN_ZOOM);
        }
        else if (e.getKeyCode() == KeyEvent.VK_L) {
            showLegend = !showLegend;
        }
        clampOffsets();
    }

    private void centerOverlay() {
        BufferedImage map = minimapManager.getMinimap();
        Point2D.Double playerLoc = minimapManager.getPlayerLocation();
        if (map != null && playerLoc != null) {
            int width = (int) (MAP_OVERLAY_WID * zoomFactor);
            int height = (int) (MAP_OVERLAY_HEI * zoomFactor);

            double scaledPixelWidth = (double) width / map.getWidth();
            double scaledPixelHeight = (double) height / map.getHeight();

            int playerScreenX = MAP_OVERLAY_X + (int) (playerLoc.x * scaledPixelWidth);
            int playerScreenY = MAP_OVERLAY_Y + (int) (playerLoc.y * scaledPixelHeight);

            offsetX = (MAP_OVERLAY_X + MAP_OVERLAY_WID / 2) - playerScreenX;
            offsetY = (MAP_OVERLAY_Y + MAP_OVERLAY_HEI / 2) - playerScreenY;
            clampOffsets();
        }
    }

    private void clampOffsets() {
        int width = (int) (MAP_OVERLAY_WID * zoomFactor);
        int height = (int) (MAP_OVERLAY_HEI * zoomFactor);

        int minOffsetX = Math.min(0, MAP_OVERLAY_WID - width);
        int minOffsetY = Math.min(0, MAP_OVERLAY_HEI - height);

        offsetX = Math.max(minOffsetX, Math.min(offsetX, 0));
        offsetY = Math.max(minOffsetY, Math.min(offsetY, 0));
    }

    private Point screenToMinimap(int screenX, int screenY) {
        int mapX = MAP_OVERLAY_X + offsetX;
        int mapY = MAP_OVERLAY_Y + offsetY;
        int width = (int) (MAP_OVERLAY_WID * zoomFactor);
        int height = (int) (MAP_OVERLAY_HEI * zoomFactor);

        double scaledPixelWidth = (double) width / minimapManager.getMinimap().getWidth();
        double scaledPixelHeight = (double) height / minimapManager.getMinimap().getHeight();

        int minimapX = (int) ((screenX - mapX) / scaledPixelWidth);
        int minimapY = (int) ((screenY - mapY) / scaledPixelHeight);

        return new Point(minimapX, minimapY);
    }

    private MinimapIcon findIconAtScreenPos(int screenX, int screenY) {
        BufferedImage map = minimapManager.getMinimap();
        int mapX = MAP_OVERLAY_X + offsetX;
        int mapY = MAP_OVERLAY_Y + offsetY;
        int width = (int) (MAP_OVERLAY_WID * zoomFactor);
        int height = (int) (MAP_OVERLAY_HEI * zoomFactor);

        double scaledPixelWidth = (double) width / map.getWidth();
        double scaledPixelHeight = (double) height / map.getHeight();

        for (MinimapIcon icon : minimapManager.getIcons()) {
            int iconWidth = (int) (2.5 * scaledPixelWidth);
            int iconHeight = (int) (2.5 * scaledPixelHeight);
            int iconX = mapX + (int) (icon.position().x * scaledPixelWidth) - iconWidth / 2;
            int iconY = mapY + (int) (icon.position().y * scaledPixelHeight) - (int) (iconHeight / 1.8);

            Rectangle iconBounds = new Rectangle(iconX, iconY, iconWidth, iconHeight);
            if (iconBounds.contains(screenX, screenY)) return icon;
        }
        return null;
    }

    private boolean isValid(BufferedImage image, Point point) {
        if (point.x >= 0 && point.y >= 0 && point.x < image.getWidth() && point.y < image.getHeight()) {
            int rgb = image.getRGB(point.x, point.y);
            return (rgb & 0xFFFFFF) == (MINIMAP_WALKABLE.getRGB() & 0xFFFFFF);
        }
        return false;
    }

    private boolean isWithinOverlay(Point p) {
        return p.x >= MAP_OVERLAY_X && p.x <= MAP_OVERLAY_X + MAP_OVERLAY_WID && p.y >= MAP_OVERLAY_Y && p.y <= MAP_OVERLAY_Y + MAP_OVERLAY_HEI;
    }

    public void reset() {
        this.centered = false;
    }

    // Getters
    public double getZoomFactor() {
        return zoomFactor;
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
}