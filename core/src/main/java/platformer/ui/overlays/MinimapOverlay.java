package platformer.ui.overlays;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapIconType;
import platformer.state.GameState;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * MinimapOverlay class is an overlay that is displayed when the player selects the minimap option.
 * It allows the player to view a smaller version of the game map, zoom in and out, and drag the map around.
 */
public class MinimapOverlay implements Overlay<MouseEvent, KeyEvent, Graphics>, MouseListener {

    private final GameState gameState;

    private final Rectangle2D overlay;
    private double zoomFactor = 1.0;

    private boolean drag = false;
    private int lastMouseX, lastMouseY;
    private int offsetX = 0, offsetY = 0;

    public MinimapOverlay(GameState gameState) {
        this.gameState = gameState;
        this.overlay = new Rectangle2D.Double(MAP_OVERLAY_X, MAP_OVERLAY_Y, MAP_OVERLAY_WID, MAP_OVERLAY_HEI);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            if (overlay.contains(e.getPoint())) {
                Point minimapCoords = screenToMinimap(e.getX(), e.getY());
                gameState.getMinimapManager().pinLocation(minimapCoords, Color.BLUE);
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
    public void mouseMoved(MouseEvent mouseEvent) {

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
        clampOffsets();
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g) {
        BufferedImage map = gameState.getMinimapManager().getMinimap();
        renderOverlay(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setClip(overlay);

        int width = (int) ((MAP_OVERLAY_WID - (int)(40 * SCALE)) * zoomFactor);
        int height = (int) ((MAP_OVERLAY_HEI - (int)(40 * SCALE)) * zoomFactor);
        int x = MAP_OVERLAY_X + (int)(20 * SCALE) + offsetX;
        int y = MAP_OVERLAY_Y + (int)(20 * SCALE) + offsetY;

        g2d.drawImage(map, x, y, width, height, null);
        renderIcons(g2d, x, y, width, height, map);
        renderPath(g2d, x, y, width, height, map);
        g2d.setClip(null);
    }

    private void renderOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(OVERLAY_COLOR);
        g2d.fill(overlay);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(overlay);
    }

    private void renderIcons(Graphics2D g2d, int x, int y, int width, int height, BufferedImage map) {
        double scaledPixelWidth = (double) width / map.getWidth();
        double scaledPixelHeight = (double) height / map.getHeight();
        List<MinimapIcon> icons = gameState.getMinimapManager().getIcons();
        BufferedImage[] minimapIcons = gameState.getMinimapManager().getMinimapIcons();

        for (MinimapIcon icon : icons) {
            BufferedImage img = minimapIcons[icon.getType().ordinal()];
            int iconWidth = (int) (1.8 * scaledPixelWidth);
            int iconHeight = (int) (1.8 * scaledPixelHeight);
            int iconX = x + (int) (icon.getPosition().x * scaledPixelWidth) - iconWidth / 3;
            int iconY = y + (int) (icon.getPosition().y * scaledPixelHeight) - (int)(iconHeight / 1.8);
            g2d.drawImage(img, iconX, iconY, iconWidth, iconHeight, null);
        }

        if (gameState.getMinimapManager().isFlashVisible()) renderPlayer(g2d, x, y, width, height, map);
    }

    private void renderPlayer(Graphics2D g2d, int x, int y, int width, int height, BufferedImage map) {
        Point playerLocation = gameState.getMinimapManager().getPlayerLocation();

        if (playerLocation != null) {
            double scaledPixelWidth = (double) width / map.getWidth();
            double scaledPixelHeight = (double) height / map.getHeight();
            BufferedImage playerIcon = gameState.getMinimapManager().getMinimapIcons()[MinimapIconType.PLAYER.ordinal()];
            int playerIconWidth = (int) (1.8 * scaledPixelWidth);
            int playerIconHeight = (int) (1.8 * scaledPixelHeight);
            int playerIconX = x + (int) (playerLocation.x * scaledPixelWidth) - playerIconWidth / 3;
            int playerIconY = y + (int) (playerLocation.y * scaledPixelHeight) - playerIconHeight / 2;
            g2d.drawImage(playerIcon, playerIconX, playerIconY, playerIconWidth, playerIconHeight, null);
        }
    }

    private void renderPath(Graphics2D g2d, int x, int y, int width, int height, BufferedImage map) {
        double scaledPixelWidth = (double) width / map.getWidth();
        double scaledPixelHeight = (double) height / map.getHeight();

        List<Point> pathPoints = gameState.getMinimapManager().getPathPoints();
        g2d.setColor(Color.YELLOW);
        for (Point point : pathPoints) {
            int dotX = x + (int) (point.x * scaledPixelWidth);
            int dotY = y + (int) (point.y * scaledPixelHeight);
            int dotSize = (int) (2 * SCALE);
            g2d.fillOval(dotX + (int)(scaledPixelWidth / 2.1), dotY + (int)(scaledPixelHeight / 2.1), dotSize, dotSize);
        }
    }

    /**
     * Clamps the offsets to ensure the minimap does not go out of bounds.
     */
    private void clampOffsets() {
        int maxOffsetX = (int) ((MAP_OVERLAY_WID - (int)(40 * SCALE)) * (zoomFactor - 1));
        int maxOffsetY = (int) ((MAP_OVERLAY_HEI - (int)(40 * SCALE)) * (zoomFactor - 1));
        offsetX = Math.max(-maxOffsetX, Math.min(offsetX, 0));
        offsetY = Math.max(-maxOffsetY, Math.min(offsetY, 0));
    }

    private Point screenToMinimap(int screenX, int screenY) {
        int mapX = MAP_OVERLAY_X + (int)(20 * SCALE) + offsetX;
        int mapY = MAP_OVERLAY_Y + (int)(20 * SCALE) + offsetY;
        int width = (int) ((MAP_OVERLAY_WID - (int)(40 * SCALE)) * zoomFactor);
        int height = (int) ((MAP_OVERLAY_HEI - (int)(40 * SCALE)) * zoomFactor);

        double scaledPixelWidth = (double) width / gameState.getMinimapManager().getMinimap().getWidth();
        double scaledPixelHeight = (double) height / gameState.getMinimapManager().getMinimap().getHeight();

        int minimapX = (int) ((screenX - mapX) / scaledPixelWidth);
        int minimapY = (int) ((screenY - mapY) / scaledPixelHeight);

        return new Point(minimapX, minimapY);
    }

    @Override
    public void reset() {

    }
}