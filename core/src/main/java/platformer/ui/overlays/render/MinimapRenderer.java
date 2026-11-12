package platformer.ui.overlays.render;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapIconType;
import platformer.model.minimap.MinimapManager;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static platformer.constants.Constants.*;

/**
 * Responsible for rendering all minimap-related visuals (icons, player position, pins, and paths) onto the minimap overlay.
 */
public class MinimapRenderer {

    private final Map<MinimapIconType, BufferedImage> defaultTintedCache = new EnumMap<>(MinimapIconType.class);
    private final Map<MinimapIconType, BufferedImage> hoverCache = new EnumMap<>(MinimapIconType.class);
    private final Map<MinimapIconType, BufferedImage> pinnedCache = new EnumMap<>(MinimapIconType.class);

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
        double baseScale = Math.min(scaledPixelWid, scaledPixelHei);

        MinimapIcon pinnedIcon = minimapManager.getPinnedIcon();
        Graphics2D gIcons = (Graphics2D) g2d.create();
        gIcons.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gIcons.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int iconSize = Math.max((int) (7 * SCALE), (int) Math.round(2.1 * baseScale));
        int badgeSize = (int) Math.round(iconSize * 1.45);

        renderMinimapIcons(gIcons, minimapManager, mapX, mapY, scaledPixelWid, scaledPixelHei, badgeSize, iconSize, pinnedIcon);
        renderPath(gIcons, mapX, mapY, mapWid, mapHei, minimap, minimapManager);
        if (minimapManager.isFlashVisible()) renderPlayer(gIcons, mapX, mapY, scaledPixelWid, scaledPixelHei, baseScale, minimapManager);
        renderQuestMarkers(gIcons, mapX, mapY, scaledPixelWid, scaledPixelHei, minimapManager);
        if (pinnedIcon == null && minimapManager.getPinnedLocation() != null) {
            renderFlagPin(gIcons, mapX, mapY, scaledPixelWid, scaledPixelHei, baseScale, minimapManager);
        }

        gIcons.dispose();
    }

    private void renderMinimapIcons(Graphics2D g2d, MinimapManager minimapManager, int mapX, int mapY, double scaledPixelWid, double scaledPixelHei, int badgeSize, int iconSize, MinimapIcon pinnedIcon) {
        List<MinimapIcon> icons = minimapManager.getIcons();
        BufferedImage[] minimapIcons = minimapManager.getMinimapIcons();
        MinimapIcon hoveredIcon = minimapManager.getHoveredIcon();

        for (MinimapIcon icon : icons) {
            if (icon.equals(pinnedIcon)) minimapManager.unpin();
            if (!minimapManager.isIconVisible(icon)) continue;
            BufferedImage originalImage = minimapIcons[icon.type().ordinal()];
            if (originalImage == null) continue;

            int cx = mapX + (int) Math.round((icon.position().x + 0.5) * scaledPixelWid);
            int cy = mapY + (int) Math.round((icon.position().y + 0.5) * scaledPixelHei);
            Color themeColor = getThemeColor(icon.type()), ringColor;
            BufferedImage iconToDraw;

            if (icon.equals(pinnedIcon)) {
                iconToDraw = pinnedCache.computeIfAbsent(icon.type(), k -> createTintedIcon(originalImage, MINIMAP_PINNED));
                ringColor = MINIMAP_PINNED;
            }
            else if (icon.equals(hoveredIcon)) {
                iconToDraw = hoverCache.computeIfAbsent(icon.type(), k -> createTintedIcon(originalImage, MINIMAP_HOVER));
                ringColor = MINIMAP_HOVER;
            }
            else {
                iconToDraw = defaultTintedCache.computeIfAbsent(icon.type(), k -> createTintedIcon(originalImage, themeColor));
                ringColor = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 160);
            }

            g2d.setColor(new Color(16, 22, 20, 225));
            g2d.fillOval(cx - badgeSize / 2, cy - badgeSize / 2, badgeSize, badgeSize);
            // Outer ring
            g2d.setColor(ringColor);
            g2d.setStroke(new BasicStroke(icon.equals(hoveredIcon) || icon.equals(pinnedIcon) ? 2.0f : 1.2f));
            g2d.drawOval(cx - badgeSize / 2, cy - badgeSize / 2, badgeSize, badgeSize);
            // Icon sprite
            if (icon.equals(pinnedIcon)) {
                float pulseAlpha = (float) (0.8 + 0.2 * Math.sin(System.currentTimeMillis() / 250.0));
                Composite oldComp = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
                g2d.drawImage(iconToDraw, cx - iconSize / 2, cy - iconSize / 2, iconSize, iconSize, null);
                g2d.setComposite(oldComp);
            }
            else g2d.drawImage(iconToDraw, cx - iconSize / 2, cy - iconSize / 2, iconSize, iconSize, null);
        }
    }

    private Color getThemeColor(MinimapIconType type) {
        return switch (type) {
            case SAVE -> new Color(110, 225, 255);
            case SHOP -> new Color(255, 215, 90);
            case BLACKSMITH -> new Color(255, 140, 80);
            case CRAFTING -> new Color(215, 185, 135);
            case BOSS -> new Color(255, 95, 95);
            case TAVERN -> new Color(255, 190, 105);
            case NPC -> new Color(210, 165, 255);
            case HERB -> new Color(130, 235, 140);
            case PLAYER -> Color.WHITE;
            case FLAG -> MINIMAP_PINNED;
        };
    }

    private void renderPlayer(Graphics2D g2d, int mapX, int mapY, double scaleW, double scaleH, double baseScale, MinimapManager minimapManager) {
        Point2D.Double playerLocation = minimapManager.getPlayerLocation();
        if (playerLocation != null) {
            BufferedImage playerIcon = minimapManager.getMinimapIcons()[MinimapIconType.PLAYER.ordinal()];
            int size = Math.max((int) (8 * SCALE), (int) Math.round(2.3 * baseScale));
            int badgeSize = (int) Math.round(size * 1.35);
            int cx = mapX + (int) Math.round(playerLocation.x * scaleW);
            int cy = mapY + (int) Math.round(playerLocation.y * scaleH);

            g2d.setColor(new Color(20, 35, 30, 220));
            g2d.fillOval(cx - badgeSize / 2, cy - badgeSize / 2, badgeSize, badgeSize);
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(cx - badgeSize / 2, cy - badgeSize / 2, badgeSize, badgeSize);
            g2d.drawImage(playerIcon, cx - size / 2, cy - size / 2, size, size, null);
        }
    }

    private void renderFlagPin(Graphics2D g2d, int mapX, int mapY, double scaleW, double scaleH, double baseScale, MinimapManager minimapManager) {
        Point pinnedLocation = minimapManager.getPinnedLocation();
        if (pinnedLocation == null) return;

        BufferedImage pinIcon = minimapManager.getMinimapIcons()[MinimapIconType.FLAG.ordinal()];
        int size = Math.max((int) (8 * SCALE), (int) Math.round(2.2 * baseScale));
        int pinX = mapX + (int) Math.round((pinnedLocation.x + 0.5) * scaleW) - size / 2;
        int pinY = mapY + (int) Math.round((pinnedLocation.y + 0.5) * scaleH) - size / 2;

        float pulse = (float) (0.8 + 0.2 * Math.sin(System.currentTimeMillis() / 250.0));
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
        g2d.drawImage(pinIcon, pinX, pinY, size, size, null);
        g2d.setComposite(originalComposite);
    }

    public BufferedImage createTintedIcon(BufferedImage source, Color tint) {
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
            g2d.fillOval(dotX + (int) (scaledPixelWidth / 2.1), dotY + (int) (scaledPixelHeight / 2.1), dotSize, dotSize);
        }
    }

    private void renderQuestMarkers(Graphics2D g2d, int mapX, int mapY, double scaleW, double scaleH, MinimapManager minimapManager) {
        List<Point> questTargets = minimapManager.getActiveQuestMinimapTargets();
        if (questTargets.isEmpty()) return;

        float pulse = (float) (0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 200.0));
        int markerSize = (int) Math.max(8 * SCALE, 2.2 * Math.min(scaleW, scaleH));

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

    /**
     * Renders a single styled icon badge for the minimap legend.
     */
    public void renderLegendBadge(Graphics2D g2d, MinimapIconType type, BufferedImage originalImage, int cx, int cy, int badgeSize) {
        if (originalImage == null) return;
        int iconSize = (int) Math.round(badgeSize / 1.35);
        Color themeColor = getThemeColor(type);

        BufferedImage iconToDraw = (type == MinimapIconType.PLAYER || type == MinimapIconType.FLAG) ? originalImage : defaultTintedCache.computeIfAbsent(type, k -> createTintedIcon(originalImage, themeColor));

        g2d.setColor(new Color(16, 22, 20, 225));
        g2d.fillOval(cx - badgeSize / 2, cy - badgeSize / 2, badgeSize, badgeSize);

        g2d.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 180));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(cx - badgeSize / 2, cy - badgeSize / 2, badgeSize, badgeSize);

        g2d.drawImage(iconToDraw, cx - iconSize / 2, cy - iconSize / 2, iconSize, iconSize, null);
    }
}