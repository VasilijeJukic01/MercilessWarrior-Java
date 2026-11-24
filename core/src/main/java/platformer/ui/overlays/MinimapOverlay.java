package platformer.ui.overlays;

import platformer.core.GameContext;
import platformer.model.minimap.MinimapIconType;
import platformer.model.minimap.MinimapManager;
import platformer.model.minimap.MinimapManager.QuestMinimapMarker;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.overlays.controller.MinimapViewController;
import platformer.ui.overlays.render.MinimapRenderer;
import platformer.ui.overlays.render.MinimapRenderer.RenderedQuestMarker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * MinimapOverlay class is an overlay that is displayed when the player selects the minimap option.
 * It allows the player to view a smaller version of the game map, zoom in and out, and drag the map around.
 */
public class MinimapOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private record QuestTrackerRow(Rectangle2D.Double bounds, QuestMinimapMarker marker) {}

    private final MinimapManager minimapManager;
    private final MinimapRenderer minimapRenderer;
    private final MinimapViewController controller;
    private final Rectangle overlay;
    private final List<QuestTrackerRow> questTrackerRows = new ArrayList<>();

    private int mouseX = -1;
    private int mouseY = -1;

    public MinimapOverlay(GameContext context) {
        this.minimapManager = context.getMinimapManager();
        this.minimapRenderer = new MinimapRenderer();
        this.controller = new MinimapViewController(minimapManager);
        this.overlay = new Rectangle(MAP_OVERLAY_X, MAP_OVERLAY_Y, MAP_OVERLAY_WID, MAP_OVERLAY_HEI);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            for (QuestTrackerRow row : questTrackerRows) {
                if (row.bounds().contains(e.getPoint())) {
                    minimapManager.handlePinRequest(row.marker().position(), null);
                    return;
                }
            }
            for (RenderedQuestMarker rm : minimapRenderer.getLastRenderedQuestMarkers()) {
                if ( e.getPoint().distance(rm.screenX(), rm.screenY()) <= rm.radius() + scale(3)) {
                    minimapManager.handlePinRequest(rm.marker().position(), null);
                    return;
                }
            }
        }
        controller.mouseClicked(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        controller.mouseDragged(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        controller.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        controller.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        controller.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        controller.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        controller.keyReleased(e);
    }

    @Override
    public void update() {
        controller.update();
        minimapManager.update();
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        BufferedImage fogImage = minimapManager.getFogImage();
        BufferedImage map = minimapManager.getMinimap();
        renderOverlay(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setClip(overlay);
        g2d.setColor(MAP_FOG_COLOR);
        g2d.fill(overlay);

        int width = controller.getRenderedMapWidth();
        int height = controller.getRenderedMapHeight();
        int x = MAP_OVERLAY_X + controller.getOffsetX();
        int y = MAP_OVERLAY_Y + controller.getOffsetY();

        g2d.drawImage(minimapManager.getVisualMinimap(), x, y, width, height, null);
        if (fogImage != null) {
            g2d.drawImage(fogImage, x, y, width, height, null);
        }
        minimapRenderer.renderIcons(g2d, x, y, width, height, map, minimapManager, mouseX, mouseY);
        renderActiveQuestsCard(g2d);
        g2d.setClip(null);

        if (controller.isShowLegend()) renderLegend(g);
    }

    private void renderOverlay(Graphics g) {
        UIButtonRenderer.drawRunicPanel(g, overlay);
    }

    /**
     * Renders a compact card in the minimap listing all active map quests. Clicking any entry pins the A* path to that quest.
     */
    private void renderActiveQuestsCard(Graphics2D g2d) {
        questTrackerRows.clear();
        List<QuestMinimapMarker> markers = minimapManager.getActiveQuestMarkers();
        if (markers.isEmpty()) return;

        Graphics2D gCard = (Graphics2D) g2d.create();
        gCard.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font headerFont = UIButtonRenderer.getFont(8.5f * SCALE);
        Font itemFont = new Font("Arial", Font.BOLD, FONT_SMALL);
        gCard.setFont(itemFont);
        FontMetrics fmItem = gCard.getFontMetrics();

        int cardX = MAP_OVERLAY_X + scale(12);
        int cardY = MAP_OVERLAY_Y + scale(12);
        int rowH = scale(15);
        int headerH = scale(16);
        int maxTextW = scale(105);

        for (QuestMinimapMarker m : markers) {
            maxTextW = Math.max(maxTextW, fmItem.stringWidth(m.questName()) + scale(26));
        }

        int cardW = maxTextW + scale(14);
        int cardH = headerH + (markers.size() * rowH) + scale(6);
        int arc = scale(6);

        gCard.setColor(new Color(12, 18, 22, 215));
        gCard.fillRoundRect(cardX, cardY, cardW, cardH, arc, arc);
        gCard.setColor(new Color(85, 125, 130, 165));
        gCard.setStroke(new BasicStroke(1.2f));
        gCard.drawRoundRect(cardX, cardY, cardW, cardH, arc, arc);

        gCard.setFont(headerFont);
        gCard.setColor(new Color(56, 229, 223));
        gCard.drawString("ACTIVE QUESTS", cardX + scale(7), cardY + scale(11));

        gCard.setFont(itemFont);
        Point pinnedLoc = minimapManager.getPinnedLocation();

        for (int i = 0; i < markers.size(); i++) {
            QuestMinimapMarker qm = markers.get(i);
            Color col = MinimapRenderer.QUEST_MARKER_COLORS[i % MinimapRenderer.QUEST_MARKER_COLORS.length];
            int rowY = cardY + headerH + (i * rowH);
            Rectangle2D.Double rowBounds = new Rectangle2D.Double(cardX + scale(3), rowY, cardW - scale(6), rowH - scale(1));
            questTrackerRows.add(new QuestTrackerRow(rowBounds, qm));

            boolean isHovered = rowBounds.contains(mouseX, mouseY);
            boolean isPinned = pinnedLoc != null && pinnedLoc.equals(qm.position());

            if (isHovered || isPinned) {
                gCard.setColor(isPinned ? new Color(40, 95, 105, 180) : new Color(28, 48, 56, 175));
                gCard.fillRoundRect((int) rowBounds.x, (int) rowBounds.y, (int) rowBounds.width, (int) rowBounds.height, scale(3), scale(3));
            }

            int badgeSize = scale(10);
            int bx = cardX + scale(7);
            int by = rowY + (rowH - badgeSize) / 2;
            gCard.setColor(col);
            gCard.fillOval(bx, by, badgeSize, badgeSize);

            gCard.setColor(new Color(15, 20, 22));
            String num = "!";
            int ntx = bx + (badgeSize - fmItem.stringWidth(num)) / 2;
            int nty = by + (badgeSize + fmItem.getAscent() - fmItem.getDescent()) / 2;
            gCard.drawString(num, ntx, nty);

            gCard.setColor(isPinned ? new Color(110, 245, 255) : (isHovered ? Color.WHITE : new Color(220, 232, 235)));
            gCard.drawString(qm.questName(), bx + badgeSize + scale(6), by + badgeSize - scale(2));
        }

        gCard.dispose();
    }

    private void renderLegend(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setColor(new Color(16, 12, 18, 210));
        g2d.fillRoundRect(LEGEND_X, LEGEND_Y, LEGEND_WID, LEGEND_HEI, 15, 15);
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(LEGEND_X, LEGEND_Y, LEGEND_WID, LEGEND_HEI, 15, 15);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        FontMetrics fm = g2d.getFontMetrics();
        MinimapIconType[] types = MinimapIconType.values();
        int badgeSize = LEGEND_ICON_SIZE;
        int iconToTextGap = (int) (4 * SCALE);
        int itemGap = (int) (13 * SCALE);

        int totalContentWidth = 0;
        for (int i = 0; i < types.length; i++) {
            String label = getLabelForIconType(types[i]);
            totalContentWidth += badgeSize + iconToTextGap + fm.stringWidth(label);
            if (i < types.length - 1) totalContentWidth += itemGap;
        }

        int currentX = LEGEND_X + (LEGEND_WID - totalContentWidth) / 2;
        int centerY = LEGEND_Y + LEGEND_HEI / 2;
        int textY = centerY + (fm.getAscent() - fm.getDescent()) / 2;

        BufferedImage[] icons = minimapManager.getMinimapIcons();
        for (MinimapIconType type : types) {
            String label = getLabelForIconType(type);
            int badgeCenterX = currentX + badgeSize / 2;
            minimapRenderer.renderLegendBadge(g2d, type, icons[type.ordinal()], badgeCenterX, centerY, badgeSize);
            g2d.setColor(new Color(230, 230, 235));
            int textX = currentX + badgeSize + iconToTextGap;
            g2d.drawString(label, textX, textY);
            currentX = textX + fm.stringWidth(label) + itemGap;
        }
        g2d.dispose();
    }

    private String getLabelForIconType(MinimapIconType type) {
        return switch (type) {
            case TAVERN -> "Tavern";
            case HERB -> "Herb";
            case NPC -> "NPC";
            case SAVE -> "Totem";
            case SHOP -> "Shop";
            case BLACKSMITH -> "Blacksmith";
            case PLAYER -> "Player";
            case CRAFTING -> "Crafting";
            case BOSS -> "Boss";
            case FLAG -> "Pin";
        };
    }

    @Override
    public void reset() {
        controller.reset();
    }
}