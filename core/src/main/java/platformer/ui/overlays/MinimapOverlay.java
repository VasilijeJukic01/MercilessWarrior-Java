package platformer.ui.overlays;

import platformer.model.minimap.MinimapIconType;
import platformer.state.GameState;
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

    private final GameState gameState;
    private final MinimapRenderer minimapRenderer;
    private final MinimapViewController controller;
    private final Rectangle overlay;

    public MinimapOverlay(GameState gameState) {
        this.gameState = gameState;
        this.minimapRenderer = new MinimapRenderer();
        this.controller = new MinimapViewController(gameState);
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
    public void update() {
        controller.update();
    }

    @Override
    public void render(Graphics g) {
        BufferedImage map = gameState.getMinimapManager().getMinimap();
        renderOverlay(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setClip(overlay);

        int width = (int) (MAP_OVERLAY_WID * controller.getZoomFactor());
        int height = (int) (MAP_OVERLAY_HEI * controller.getZoomFactor());
        int x = MAP_OVERLAY_X + controller.getOffsetX();
        int y = MAP_OVERLAY_Y + controller.getOffsetY();

        g2d.drawImage(map, x, y, width, height, null);
        minimapRenderer.renderIcons(g2d, x, y, width, height, map, gameState.getMinimapManager());
        g2d.setClip(null);

        if (controller.isShowLegend()) renderLegend(g);
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
        controller.reset();
    }
}