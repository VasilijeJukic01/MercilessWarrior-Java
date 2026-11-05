package platformer.ui.overlays.render;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapIconType;
import platformer.model.minimap.MinimapManager;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platformer.constants.Constants.*;

/**
 * Responsible for rendering all minimap-related visuals (icons, player position, pins, and paths), onto the minimap overlay.
 * <p>
 * Handles icon tinting for hover and pin states, as well as animation effects for pins and the player.
 */
public class MinimapRenderer {

    private final Map<MinimapIconType, BufferedImage> hoverCache = new HashMap<>();
    private final Map<MinimapIconType, BufferedImage> pinnedCache = new HashMap<>();

    /**
     * Renders all minimap icons, the player, the path, and the pin (if present) onto the minimap.
     * Handles tinting for hovered and pinned icons, and animates the pin and player icon.
     *
     * @param g2d           The graphics context to draw on.
     * @param mapX          The X coordinate of the minimap on the overlay.
     * @param mapY          The Y coordinate of the minimap on the overlay.
     * @param mapWid        The width of the minimap rendering area.
     * @param mapHei        The height of the minimap rendering area.
     * @param minimap       The minimap image.
     * @param minimapManager The minimap manager providing state and data.
     */
    public void renderIcons(Graphics2D g2d, int mapX, int mapY, int mapWid, int mapHei, BufferedImage minimap, MinimapManager minimapManager) {
        double scaledPixelWid = (double) mapWid / minimap.getWidth();
        double scaledPixelHei = (double) mapHei / minimap.getHeight();
        List<MinimapIcon> icons = minimapManager.getIcons();
        BufferedImage[] minimapIcons = minimapManager.getMinimapIcons();

        MinimapIcon hoveredIcon = minimapManager.getHoveredIcon();
        MinimapIcon pinnedIcon = minimapManager.getPinnedIcon();

        for (MinimapIcon icon : icons) {
            BufferedImage originalImage = minimapIcons[icon.type().ordinal()];
            int wid = (int) (1.8 * scaledPixelWid);
            int hei = (int) (1.8 * scaledPixelHei);
            int xPos = mapX + (int) (icon.position().x * scaledPixelWid) - wid / 3;
            int yPos = mapY + (int) (icon.position().y * scaledPixelHei) - (int) (hei / 1.8);

            if (icon.equals(pinnedIcon)) {
                BufferedImage tintedImage = pinnedCache.computeIfAbsent(icon.type(), k -> createTintedIcon(originalImage, MINIMAP_PINNED));

                float pulseAlpha = (float) (0.8 + 0.2 * Math.sin(System.currentTimeMillis() / 250.0));
                Composite originalComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
                g2d.drawImage(tintedImage, xPos, yPos, wid, hei, null);
                g2d.setComposite(originalComposite);

            }
            else if (icon.equals(hoveredIcon)) {
                BufferedImage tintedImage = hoverCache.computeIfAbsent(icon.type(), k -> createTintedIcon(originalImage, MINIMAP_HOVER));
                g2d.drawImage(tintedImage, xPos, yPos, wid, hei, null);
            }
            else g2d.drawImage(originalImage, xPos, yPos, wid, hei, null);
        }

        if (minimapManager.isFlashVisible()) renderPlayer(g2d, mapX, mapY, mapWid, mapHei, minimap, minimapManager);
        renderPath(g2d, mapX, mapY, mapWid, mapHei, minimap, minimapManager);
        renderQuestMarkers(g2d, mapX, mapY, scaledPixelWid, scaledPixelHei, minimapManager);

        if (pinnedIcon == null && minimapManager.getPinnedLocation() != null) {
            renderFlagPin(g2d, mapX, mapY, scaledPixelWid, scaledPixelHei, minimapManager);
        }
    }

    private void renderPlayer(Graphics2D g2d, int mapX, int mapY, int mapWidth, int mapHeight, BufferedImage minimap, MinimapManager minimapManager) {
        Point2D.Double playerLocation = minimapManager.getPlayerLocation();

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

    private void renderFlagPin(Graphics2D g2d, int mapX, int mapY, double scaleW, double scaleH, MinimapManager minimapManager) {
        Point pinnedLocation = minimapManager.getPinnedLocation();
        if (pinnedLocation == null) return;

        BufferedImage pinIcon = minimapManager.getMinimapIcons()[MinimapIconType.FLAG.ordinal()];
        int pinIconWidth = (int) (2.0 * scaleW);
        int pinIconHeight = (int) (2.0 * scaleH);
        int pinX = mapX + (int) (pinnedLocation.x * scaleW) - pinIconWidth / 2 + (int)(scaleW / 2.1);
        int pinY = mapY + (int) (pinnedLocation.y * scaleH) - pinIconHeight / 2;

        float pulse = (float) (0.8 + 0.2 * Math.sin(System.currentTimeMillis() / 250.0));
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
        g2d.drawImage(pinIcon, pinX, pinY, pinIconWidth, pinIconHeight, null);
        g2d.setComposite(originalComposite);
    }

    /**
     * Creates a colorized version of a white icon using the specified tint color.
     *
     * @param source The original white icon.
     * @param tint   The color to apply.
     * @return A new, correctly tinted BufferedImage.
     */
    private BufferedImage createTintedIcon(BufferedImage source, Color tint) {
        BufferedImage tintedImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tintedImage.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        g2d.setComposite(AlphaComposite.SrcAtop);
        g2d.setColor(tint);
        g2d.fillRect(0, 0, source.getWidth(), source.getHeight());
        g2d.dispose();
        return tintedImage;
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

    private void renderQuestMarkers(Graphics2D g2d, int mapX, int mapY, double scaleW, double scaleH, MinimapManager minimapManager) {
        List<Point> questTargets = minimapManager.getActiveQuestMinimapTargets();
        if (questTargets.isEmpty()) return;

        float pulse = (float) (0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 200.0));
        int markerSize = (int) Math.max(8 * SCALE, 2.2 * scaleW);

        for (Point pt : questTargets) {
            int cx = mapX + (int) (pt.x * scaleW);
            int cy = mapY + (int) (pt.y * scaleH);

            Graphics2D gMarker = (Graphics2D) g2d.create();
            gMarker.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Ring
            int ringSize = (int) (markerSize * (1.1 + 0.3 * pulse));
            gMarker.setColor(new Color(255, 215, 0, (int) (180 * pulse)));
            gMarker.setStroke(new BasicStroke(2.0f));
            gMarker.drawOval(cx - ringSize / 2, cy - ringSize / 2, ringSize, ringSize);

            gMarker.setColor(new Color(255, 190, 0, 235));
            gMarker.fillOval(cx - markerSize / 2, cy - markerSize / 2, markerSize, markerSize);
            gMarker.setColor(new Color(40, 20, 0));
            gMarker.setFont(new Font("Arial", Font.BOLD, Math.max(10, (int) (markerSize * 0.75))));
            FontMetrics fm = gMarker.getFontMetrics();
            int tx = cx - fm.stringWidth("!") / 2;
            int ty = cy + (fm.getAscent() - fm.getDescent()) / 2;
            gMarker.drawString("!", tx, ty);

            gMarker.dispose();
        }
    }
}