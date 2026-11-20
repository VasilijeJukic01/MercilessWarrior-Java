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

    private double zoomFactor = 2.2;
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

    /**
     * Computes the uniform pixels-per-tile scale so the map fits cleanly at MIN_ZOOM with zero distortion.
     */
    public double getPixelsPerTile() {
        BufferedImage map = minimapManager.getMinimap();
        if (map == null || map.getWidth() == 0 || map.getHeight() == 0) return 1.0;
        double fitScale = Math.min((double) MAP_OVERLAY_WID / map.getWidth(), (double) MAP_OVERLAY_HEI / map.getHeight());
        return fitScale * zoomFactor;
    }

    public int getRenderedMapWidth() {
        BufferedImage map = minimapManager.getMinimap();
        return map != null ? (int) Math.round(map.getWidth() * getPixelsPerTile()) : MAP_OVERLAY_WID;
    }

    public int getRenderedMapHeight() {
        BufferedImage map = minimapManager.getMinimap();
        return map != null ? (int) Math.round(map.getHeight() * getPixelsPerTile()) : MAP_OVERLAY_HEI;
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        if (drag && canPan()) {
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
        if (!isWithinOverlay(e.getPoint())) return;

        if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
            MinimapIcon foundIcon = findIconAtScreenPos(e.getX(), e.getY());
            Point targetCoords = (foundIcon != null) ? foundIcon.position() : screenToMinimap(e.getX(), e.getY());
            Point walkableCoords = findNearestWalkablePoint(minimapManager.getMinimap(), targetCoords, 4);
            if (walkableCoords != null) {
                boolean isQuestTarget = minimapManager.getActiveQuestMinimapTargets().stream()
                        .anyMatch(qt -> qt.distance(targetCoords) <= 5.0);
                if (minimapManager.isExplored(targetCoords) || isQuestTarget) {
                    minimapManager.handlePinRequest(walkableCoords, foundIcon);
                }
            }
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1 && canPan()) {
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

    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS) {
            adjustZoom(0.25);
        }
        else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            adjustZoom(-0.25);
        }
        else if (e.getKeyCode() == KeyEvent.VK_L) {
            showLegend = !showLegend;
        }
    }

    private void adjustZoom(double delta) {
        double oldScale = getPixelsPerTile();
        double centerMapX = (MAP_OVERLAY_WID / 2.0 - offsetX) / oldScale;
        double centerMapY = (MAP_OVERLAY_HEI / 2.0 - offsetY) / oldScale;

        zoomFactor = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomFactor + delta));

        double newScale = getPixelsPerTile();
        offsetX = (int) Math.round(MAP_OVERLAY_WID / 2.0 - centerMapX * newScale);
        offsetY = (int) Math.round(MAP_OVERLAY_HEI / 2.0 - centerMapY * newScale);
        clampOffsets();
    }

    private boolean canPan() {
        return getRenderedMapWidth() > MAP_OVERLAY_WID || getRenderedMapHeight() > MAP_OVERLAY_HEI;
    }

    private void centerOverlay() {
        BufferedImage map = minimapManager.getMinimap();
        Point2D.Double playerLoc = minimapManager.getPlayerLocation();
        if (map != null && playerLoc != null) {
            double scale = getPixelsPerTile();
            int playerScreenX = MAP_OVERLAY_X + (int) Math.round(playerLoc.x * scale);
            int playerScreenY = MAP_OVERLAY_Y + (int) Math.round(playerLoc.y * scale);

            offsetX = (MAP_OVERLAY_X + MAP_OVERLAY_WID / 2) - playerScreenX;
            offsetY = (MAP_OVERLAY_Y + MAP_OVERLAY_HEI / 2) - playerScreenY;
            clampOffsets();
        }
    }

    private void clampOffsets() {
        int width = getRenderedMapWidth(), height = getRenderedMapHeight();
        if (width <= MAP_OVERLAY_WID) offsetX = (MAP_OVERLAY_WID - width) / 2;
        else offsetX = Math.max(MAP_OVERLAY_WID - width, Math.min(offsetX, 0));
        if (height <= MAP_OVERLAY_HEI) offsetY = (MAP_OVERLAY_HEI - height) / 2;
        else offsetY = Math.max(MAP_OVERLAY_HEI - height, Math.min(offsetY, 0));
    }

    private Point screenToMinimap(int screenX, int screenY) {
        int mapX = MAP_OVERLAY_X + offsetX;
        int mapY = MAP_OVERLAY_Y + offsetY;
        double scale = getPixelsPerTile();

        int minimapX = (int) ((screenX - mapX) / scale);
        int minimapY = (int) ((screenY - mapY) / scale);
        return new Point(minimapX, minimapY);
    }

    private Point findNearestWalkablePoint(BufferedImage image, Point origin, int maxRadius) {
        if (isValid(image, origin)) return origin;
        Point bestPoint = null;
        double minDist = Double.MAX_VALUE;
        for (int r = 1; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    Point candidate = new Point(origin.x + dx, origin.y + dy);
                    if (isValid(image, candidate)) {
                        double dist = origin.distanceSq(candidate);
                        if (dist < minDist) {
                            minDist = dist;
                            bestPoint = candidate;
                        }
                    }
                }
            }
            if (bestPoint != null) return bestPoint;
        }
        return null;
    }

    private MinimapIcon findIconAtScreenPos(int screenX, int screenY) {
        int mapX = MAP_OVERLAY_X + offsetX;
        int mapY = MAP_OVERLAY_Y + offsetY;
        double scale = getPixelsPerTile();

        for (MinimapIcon icon : minimapManager.getIcons()) {
            if (!minimapManager.isIconVisible(icon)) continue;
            int iconSize = Math.max((int) (10 * SCALE), (int) Math.round(3.0 * scale));
            int iconX = mapX + (int) Math.round((icon.position().x + 0.5) * scale) - iconSize / 2;
            int iconY = mapY + (int) Math.round((icon.position().y + 0.5) * scale) - iconSize / 2;

            Rectangle iconBounds = new Rectangle(iconX, iconY, iconSize, iconSize);
            if (iconBounds.contains(screenX, screenY)) return icon;
        }
        return null;
    }

    private boolean isValid(BufferedImage image, Point point) {
        if (point.x >= 0 && point.y >= 0 && point.x < image.getWidth() && point.y < image.getHeight()) {
            int argb = image.getRGB(point.x, point.y);
            int alpha = (argb >>> 24) & 0xFF;
            int rgb = argb & 0xFFFFFF;
            return (alpha == 254) || (rgb == (MINIMAP_WALKABLE.getRGB() & 0xFFFFFF));
        }
        return false;
    }

    private boolean isWithinOverlay(Point p) {
        return p.x >= MAP_OVERLAY_X && p.x <= MAP_OVERLAY_X + MAP_OVERLAY_WID && p.y >= MAP_OVERLAY_Y && p.y <= MAP_OVERLAY_Y + MAP_OVERLAY_HEI;
    }

    public void reset() {
        this.centered = false;
    }

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