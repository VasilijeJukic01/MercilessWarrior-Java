package platformer.ui.overlays;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapIconType;
import platformer.state.GameState;
import platformer.ui.overlays.render.MinimapRenderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * MinimapOverlay class is an overlay that is displayed when the player selects the minimap option.
 * It allows the player to view a smaller version of the game map, zoom in and out, and drag the map around.
 */
public class MinimapOverlay implements Overlay<MouseEvent, KeyEvent, Graphics>, MouseListener {

    private final GameState gameState;
    private final MinimapRenderer minimapRenderer;

    private final Rectangle2D overlay;
    private double zoomFactor = 1.8;
    private boolean showLegend = true;

    private boolean drag = false;
    private int lastMouseX, lastMouseY;
    private int offsetX = 0, offsetY = 0;

    private boolean centered = false;

    public MinimapOverlay(GameState gameState) {
        this.gameState = gameState;
        this.minimapRenderer = new MinimapRenderer();
        this.overlay = new Rectangle2D.Double(MAP_OVERLAY_X, MAP_OVERLAY_Y, MAP_OVERLAY_WID, MAP_OVERLAY_HEI);
    }

    private void centerOverlay() {
        BufferedImage map = gameState.getMinimapManager().getMinimap();
        Point2D.Double playerLoc = gameState.getMinimapManager().getPlayerLocation();
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

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            if (overlay.contains(e.getPoint())) {
                MinimapIcon foundIcon = findIconAtScreenPos(e.getX(), e.getY());
                Point minimapCoords = screenToMinimap(e.getX(), e.getY());

                if (isValid(gameState.getMinimapManager().getMinimap(), minimapCoords)) {
                    gameState.getMinimapManager().handlePinRequest(minimapCoords, foundIcon);
                }
            }
        }
    }

    @Override
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

    @Override
    public void mousePressed(MouseEvent e) {
        if (overlay.contains(e.getPoint()) && zoomFactor > MIN_ZOOM) {
            drag = true;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        drag = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (overlay.contains(e.getPoint())) {
            MinimapIcon foundIcon = findIconAtScreenPos(e.getX(), e.getY());
            gameState.getMinimapManager().setHoveredIcon(foundIcon);
        }
        else gameState.getMinimapManager().setHoveredIcon(null);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
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

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g) {
        if (!centered && gameState.getMinimapManager().getPlayerLocation() != null) {
            centerOverlay();
            centered = true;
        }
        BufferedImage map = gameState.getMinimapManager().getMinimap();
        renderOverlay(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setClip(overlay);

        int width = (int) (MAP_OVERLAY_WID * zoomFactor);
        int height = (int) (MAP_OVERLAY_HEI * zoomFactor);
        int x = MAP_OVERLAY_X + offsetX;
        int y = MAP_OVERLAY_Y + offsetY;

        g2d.drawImage(map, x, y, width, height, null);
        minimapRenderer.renderIcons(g2d, x, y, width, height, map, gameState.getMinimapManager());
        g2d.setClip(null);

        if (showLegend) renderLegend(g);
    }

    private void renderOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(OVERLAY_COLOR);
        g2d.fill(overlay);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(overlay);
    }

    private void renderLegend(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(LEGEND_X, LEGEND_Y, LEGEND_WID, LEGEND_HEI, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g.setColor(Color.WHITE);

        BufferedImage[] icons = gameState.getMinimapManager().getMinimapIcons();
        int itemX = LEGEND_X + LEGEND_PADDING;
        int itemY = LEGEND_Y + (LEGEND_HEI / 2) - (LEGEND_ICON_SIZE / 2);

        for (MinimapIconType type : MinimapIconType.values()) {
            if (itemX + LEGEND_ITEM_SPACING > LEGEND_X + LEGEND_WID) break;
            BufferedImage iconImg = icons[type.ordinal()];
            g.drawImage(iconImg, itemX, itemY, LEGEND_ICON_SIZE, LEGEND_ICON_SIZE, null);
            g.drawString(getLabelForIconType(type), itemX + LEGEND_TEXT_OFFSET, itemY + LEGEND_ICON_SIZE / 2 + FONT_LIGHT / 2);
            itemX += LEGEND_ITEM_SPACING;
        }
    }

    /**
     * Clamps the offsets to ensure the minimap does not go out of bounds.
     */
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

        double scaledPixelWidth = (double) width / gameState.getMinimapManager().getMinimap().getWidth();
        double scaledPixelHeight = (double) height / gameState.getMinimapManager().getMinimap().getHeight();

        int minimapX = (int) ((screenX - mapX) / scaledPixelWidth);
        int minimapY = (int) ((screenY - mapY) / scaledPixelHeight);

        return new Point(minimapX, minimapY);
    }

    private MinimapIcon findIconAtScreenPos(int screenX, int screenY) {
        BufferedImage map = gameState.getMinimapManager().getMinimap();
        int mapX = MAP_OVERLAY_X + offsetX;
        int mapY = MAP_OVERLAY_Y + offsetY;
        int width = (int) (MAP_OVERLAY_WID * zoomFactor);
        int height = (int) (MAP_OVERLAY_HEI * zoomFactor);

        double scaledPixelWidth = (double) width / map.getWidth();
        double scaledPixelHeight = (double) height / map.getHeight();

        for (MinimapIcon icon : gameState.getMinimapManager().getIcons()) {
            int iconWidth = (int) (2.5 * scaledPixelWidth);
            int iconHeight = (int) (2.5 * scaledPixelHeight);
            int iconX = mapX + (int) (icon.position().x * scaledPixelWidth) - iconWidth / 2;
            int iconY = mapY + (int) (icon.position().y * scaledPixelHeight) - iconHeight / 2;

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

    private String getLabelForIconType(MinimapIconType type) {
        return switch (type) {
            case PLAYER -> "Player";
            case SAVE -> "Totem";
            case SHOP -> "Shop";
            case BLACKSMITH -> "Blacksmith";
            case CRAFTING -> "Crafting Table";
            case BOSS -> "Danger Zone";
            case FLAG -> "Pinned Location";
        };
    }

    @Override
    public void reset() {
        this.centered = false;
    }
}