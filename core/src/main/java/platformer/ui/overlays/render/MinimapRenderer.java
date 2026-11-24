package platformer.ui.overlays.render;

import platformer.model.minimap.MinimapIcon;
import platformer.model.minimap.MinimapIconType;
import platformer.model.minimap.MinimapManager;
import platformer.model.minimap.MinimapManager.QuestMinimapMarker;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Responsible for rendering all minimap-related visuals (icons, player position, quest markers, pins, and paths) onto the minimap overlay.
 */
public class MinimapRenderer {

    public static final Color[] QUEST_MARKER_COLORS = {
            new Color(255, 205, 45),
    };

    public record RenderedQuestMarker(QuestMinimapMarker marker, int index, Color color, int screenX, int screenY, int radius) {}

    private final Map<MinimapIconType, BufferedImage> defaultTintedCache = new EnumMap<>(MinimapIconType.class);
    private final Map<MinimapIconType, BufferedImage> hoverCache = new EnumMap<>(MinimapIconType.class);
    private final Map<MinimapIconType, BufferedImage> pinnedCache = new EnumMap<>(MinimapIconType.class);
    private final List<RenderedQuestMarker> lastRenderedQuestMarkers = new ArrayList<>();

    public void renderIcons(Graphics2D g2d, int mapX, int mapY, int mapWid, int mapHei, BufferedImage minimap, MinimapManager minimapManager) {
        renderIcons(g2d, mapX, mapY, mapWid, mapHei, minimap, minimapManager, -1, -1);
    }

    /**
     * Renders all minimap icons, the player, the path, quest markers, and the pin onto the minimap.
     */
    public void renderIcons(Graphics2D g2d, int mapX, int mapY, int mapWid, int mapHei, BufferedImage minimap, MinimapManager minimapManager, int mouseX, int mouseY) {
        double scaledPixelWid = (double) mapWid / minimap.getWidth();
        double scaledPixelHei = (double) mapHei / minimap.getHeight();
        double baseScale = Math.min(scaledPixelWid, scaledPixelHei);
        boolean isFullOverlay = mapWid >= MAP_OVERLAY_WID / 2;

        MinimapIcon pinnedIcon = minimapManager.getPinnedIcon();
        Graphics2D gIcons = (Graphics2D) g2d.create();
        gIcons.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gIcons.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int iconSize = Math.max((int) (7 * SCALE), (int) Math.round(2.1 * baseScale));
        int badgeSize = (int) Math.round(iconSize * 1.45);

        renderMinimapIcons(gIcons, minimapManager, mapX, mapY, scaledPixelWid, scaledPixelHei, badgeSize, iconSize, pinnedIcon);
        renderPath(gIcons, mapX, mapY, mapWid, mapHei, minimap, minimapManager);
        if (minimapManager.isFlashVisible()) renderPlayer(gIcons, mapX, mapY, scaledPixelWid, scaledPixelHei, baseScale, minimapManager);
        renderQuestMarkers(gIcons, mapX, mapY, scaledPixelWid, scaledPixelHei, minimapManager, isFullOverlay, mouseX, mouseY);
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
            if (!minimapManager.isIconVisible(icon)) {
                if (icon.equals(pinnedIcon)) minimapManager.unpin();
                continue;
            }
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
            g2d.setColor(ringColor);
            g2d.setStroke(new BasicStroke(icon.equals(hoveredIcon) || icon.equals(pinnedIcon) ? 2.0f : 1.2f));
            g2d.drawOval(cx - badgeSize / 2, cy - badgeSize / 2, badgeSize, badgeSize);

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

    private void renderQuestMarkers(Graphics2D g2d, int mapX, int mapY, double scaleW, double scaleH, MinimapManager minimapManager, boolean isFullOverlay, int mouseX, int mouseY) {
        if (isFullOverlay) {
            lastRenderedQuestMarkers.clear();
        }
        List<QuestMinimapMarker> markers = minimapManager.getActiveQuestMarkers();
        if (markers.isEmpty()) return;

        float pulse = (float) (0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 200.0));
        int markerSize = (int) Math.max(8 * SCALE, 2.2 * Math.min(scaleW, scaleH));
        RenderedQuestMarker hoveredMarker = null;

        for (int i = 0; i < markers.size(); i++) {
            QuestMinimapMarker qm = markers.get(i);
            Point pt = qm.position();
            Color markerColor = QUEST_MARKER_COLORS[i % QUEST_MARKER_COLORS.length];

            int duplicateOffset = 0;
            for (int j = 0; j < i; j++) {
                if (markers.get(j).position().distance(pt) <= 4.0) {
                    duplicateOffset += (markerSize + scale(3));
                }
            }

            int cx = mapX + (int) Math.round((pt.x + 0.5) * scaleW) + duplicateOffset;
            int cy = mapY + (int) Math.round((pt.y + 0.5) * scaleH);

            boolean isHovered = isFullOverlay && mouseX >= 0 && Point2D.distance(cx, cy, mouseX, mouseY) <= markerSize * 0.85;
            RenderedQuestMarker rendered = new RenderedQuestMarker(qm, i + 1, markerColor, cx, cy, markerSize / 2);
            if (isFullOverlay) {
                lastRenderedQuestMarkers.add(rendered);
                if (isHovered) hoveredMarker = rendered;
            }

            Graphics2D gMarker = (Graphics2D) g2d.create();
            gMarker.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int ringSize = (int) (markerSize * (isHovered ? 1.55 : (1.15 + 0.30 * pulse)));
            gMarker.setColor(new Color(markerColor.getRed(), markerColor.getGreen(), markerColor.getBlue(), (int) (190 * pulse)));
            gMarker.setStroke(new BasicStroke(isHovered ? 2.4f : 1.8f));
            gMarker.drawOval(cx - ringSize / 2, cy - ringSize / 2, ringSize, ringSize);

            gMarker.setColor(new Color(14, 22, 26, 235));
            gMarker.fillOval(cx - markerSize / 2, cy - markerSize / 2, markerSize, markerSize);

            gMarker.setColor(markerColor);
            gMarker.setStroke(new BasicStroke(1.6f));
            gMarker.drawOval(cx - markerSize / 2, cy - markerSize / 2, markerSize, markerSize);

            String badgeText = "!";
            gMarker.setFont(new Font("Arial", Font.BOLD, Math.max(10, (int) (markerSize * 0.72))));
            FontMetrics fm = gMarker.getFontMetrics();
            int tx = cx - fm.stringWidth(badgeText) / 2;
            int ty = cy + (fm.getAscent() - fm.getDescent()) / 2;
            gMarker.setColor(markerColor.brighter());
            gMarker.drawString(badgeText, tx, ty);

            gMarker.dispose();
        }

        if (hoveredMarker != null) {
            renderQuestTooltip(g2d, hoveredMarker);
        }
    }

    private void renderQuestTooltip(Graphics2D g2d, RenderedQuestMarker rm) {
        Font titleFont = UIButtonRenderer.getFont(9.5f * SCALE);
        Font objFont = new Font("Arial", Font.PLAIN, FONT_LIGHT);

        g2d.setFont(titleFont);
        FontMetrics fmTitle = g2d.getFontMetrics();
        String title = rm.marker().questName();

        g2d.setFont(objFont);
        FontMetrics fmObj = g2d.getFontMetrics();
        String objText = rm.marker().objectiveDescription() != null ? rm.marker().objectiveDescription() : "";

        int padX = scale(8);
        int boxW = Math.max(fmTitle.stringWidth(title), fmObj.stringWidth(objText)) + padX * 2;
        int boxH = scale(28);
        int margin = scale(8);

        int boxX = rm.screenX() - boxW / 2;
        int minX = MAP_OVERLAY_X + margin;
        int maxX = MAP_OVERLAY_X + MAP_OVERLAY_WID - boxW - margin;
        boxX = Math.max(minX, Math.min(boxX, maxX));

        int boxY = rm.screenY() - rm.radius() - boxH - scale(6);
        int minY = MAP_OVERLAY_Y + margin;
        if (boxY < minY) {
            boxY = rm.screenY() + rm.radius() + scale(6);
        }

        int arc = scale(5);

        g2d.setColor(new Color(12, 20, 24, 242));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, arc, arc);

        g2d.setColor(rm.color());
        g2d.setStroke(new BasicStroke(1.3f));
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, arc, arc);

        g2d.setFont(titleFont);
        g2d.setColor(rm.color().brighter());
        g2d.drawString(title, boxX + padX, boxY + scale(11));

        g2d.setFont(objFont);
        g2d.setColor(new Color(215, 230, 232));
        g2d.drawString(objText, boxX + padX, boxY + scale(22));
    }

    public List<RenderedQuestMarker> getLastRenderedQuestMarkers() {
        return lastRenderedQuestMarkers;
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