package platformer.ui.overlays;

import platformer.core.GameContext;
import platformer.model.minimap.MinimapIconType;
import platformer.model.minimap.MinimapManager;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.overlays.controller.MinimapViewController;
import platformer.ui.overlays.render.MinimapRenderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * MinimapOverlay class is an overlay that is displayed when the player selects the minimap option.
 * It allows the player to view a smaller version of the game map, zoom in and out, and drag the map around.
 */
public class MinimapOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final MinimapManager minimapManager;
    private final MinimapRenderer minimapRenderer;
    private final MinimapViewController controller;
    private final Rectangle overlay;

    public MinimapOverlay(GameContext context) {
        this.minimapManager = context.getMinimapManager();
        this.minimapRenderer = new MinimapRenderer();
        this.controller = new MinimapViewController(minimapManager);
        this.overlay = new Rectangle(MAP_OVERLAY_X, MAP_OVERLAY_Y, MAP_OVERLAY_WID, MAP_OVERLAY_HEI);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        controller.mouseClicked(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
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

        int width = (int) (MAP_OVERLAY_WID * controller.getZoomFactor());
        int height = (int) (MAP_OVERLAY_HEI * controller.getZoomFactor());
        int x = MAP_OVERLAY_X + controller.getOffsetX();
        int y = MAP_OVERLAY_Y + controller.getOffsetY();

        g2d.drawImage(minimapManager.getVisualMinimap(), x, y, width, height, null);
        if (fogImage != null) {
            g2d.drawImage(fogImage, x, y, width, height, null);
        }
        minimapRenderer.renderIcons(g2d, x, y, width, height, map, minimapManager);
        g2d.setClip(null);

        if (controller.isShowLegend()) renderLegend(g);
    }

    private void renderOverlay(Graphics g) {
        UIButtonRenderer.drawRunicPanel(g, overlay);
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