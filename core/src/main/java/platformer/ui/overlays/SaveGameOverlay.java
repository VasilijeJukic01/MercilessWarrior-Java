package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.model.effects.TimeCycleManager;
import platformer.model.gameObjects.ObjType;
import platformer.model.levels.LevelManager.TotemNode;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.components.slots.GameSlot;
import platformer.ui.overlays.controller.SaveViewController;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Renders the redesigned two-panel overlay for Save Totems.
 * Left panel handles Rest & Save (with time-skip and full heal); right panel handles Fast Travel between discovered totems.
 */
public class SaveGameOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    public static final int TOTEM_OVERLAY_X = PERKS_OVERLAY_X;
    public static final int TOTEM_OVERLAY_Y = PERKS_OVERLAY_Y;
    public static final int TOTEM_OVERLAY_WID = PERKS_OVERLAY_WID;
    public static final int TOTEM_OVERLAY_HEI = PERKS_OVERLAY_HEI;

    public static final int LEFT_PANEL_X = scale(88);
    public static final int LEFT_PANEL_Y = scale(104);
    public static final int LEFT_PANEL_W = scale(320);
    public static final int LEFT_PANEL_H = scale(262);

    public static final int RIGHT_PANEL_X = scale(422);
    public static final int RIGHT_PANEL_Y = scale(104);
    public static final int RIGHT_PANEL_W = scale(326);
    public static final int RIGHT_PANEL_H = scale(262);

    public static final int SAVE_CARD_X = LEFT_PANEL_X + scale(12);
    public static final int SAVE_CARD_Y = LEFT_PANEL_Y + scale(42);
    public static final int SAVE_CARD_W = LEFT_PANEL_W - scale(24);
    public static final int SAVE_CARD_H = scale(44);
    public static final int SAVE_CARD_GAP = scale(52);

    public static final int TRAVEL_CARD_X = RIGHT_PANEL_X + scale(12);
    public static final int TRAVEL_CARD_Y = RIGHT_PANEL_Y + scale(42);
    public static final int TRAVEL_CARD_W = RIGHT_PANEL_W - scale(24);
    public static final int TRAVEL_CARD_H = scale(44);
    public static final int TRAVEL_CARD_GAP = scale(52);

    private final GameContext context;
    private final SaveViewController controller;

    private Rectangle2D.Double overlay, savePanel, travelPanel;
    private final MediumButton[] buttons = new MediumButton[3];
    private BufferedImage totemIcon;

    public SaveGameOverlay(GameContext context) {
        this.context = context;
        initPanels();
        loadImages();
        loadButtons();
        this.controller = new SaveViewController(context, this);
    }

    private void initPanels() {
        this.overlay = new Rectangle2D.Double(TOTEM_OVERLAY_X, TOTEM_OVERLAY_Y, TOTEM_OVERLAY_WID, TOTEM_OVERLAY_HEI);
        this.savePanel = new Rectangle2D.Double(LEFT_PANEL_X, LEFT_PANEL_Y, LEFT_PANEL_W, LEFT_PANEL_H);
        this.travelPanel = new Rectangle2D.Double(RIGHT_PANEL_X, RIGHT_PANEL_Y, RIGHT_PANEL_W, RIGHT_PANEL_H);
    }

    private void loadImages() {
        BufferedImage[] anims = SpriteManager.getInstance().getObjectAnimations(ObjType.SAVE_TOTEM);
        if (anims != null && anims.length > 0) {
            this.totemIcon = anims[0];
        }
    }

    private void loadButtons() {
        int btnY = scale(380);
        buttons[0] = new MediumButton( LEFT_PANEL_X + (LEFT_PANEL_W - MEDIUM_BTN_WID) / 2, btnY, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.SAVE);
        buttons[1] = new MediumButton(RIGHT_PANEL_X + scale(42), btnY, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.TRAVEL);
        buttons[2] = new MediumButton(RIGHT_PANEL_X + RIGHT_PANEL_W - MEDIUM_BTN_WID - scale(42), btnY, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.CLOSE);
    }

    @Override
    public void update() {
        controller.update();
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        Graphics2D g2d = (Graphics2D) g;
        UIButtonRenderer.drawRunicPanel(g2d, overlay);
        UIButtonRenderer.drawRunicHeader(g2d, "ANCIENT TOTEM", TOTEM_OVERLAY_X + TOTEM_OVERLAY_WID / 2, PERKS_TEXT_Y - scale(14));

        renderSubPanels(g2d);
        renderSaveSection(g2d);
        renderFastTravelSection(g2d);
        Arrays.stream(buttons).forEach(b -> b.render(g));
    }

    private void renderSubPanels(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) savePanel.x, (int) savePanel.y, (int) savePanel.width, (int) savePanel.height);
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) travelPanel.x, (int) travelPanel.y, (int) travelPanel.width, (int) travelPanel.height);

        g2d.setFont(UIButtonRenderer.getFont(10.0f * SCALE));
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("REST & SAVE PROGRESS", (int) savePanel.x + scale(6), (int) savePanel.y - scale(4));
        g2d.drawString("DISCOVERED TOTEMS (FAST TRAVEL)", (int) travelPanel.x + scale(6), (int) travelPanel.y - scale(4));
    }

    private void renderSaveSection(Graphics2D g2d) {
        int leftX = SAVE_CARD_X;
        int rightX = SAVE_CARD_X + SAVE_CARD_W;
        int topY = LEFT_PANEL_Y + scale(16);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(new Color(145, 225, 220));
        g2d.drawString("Rest: Full Heal • Time Skip • Respawn Enemies", leftX, topY);

        String timeLabel = getTimeOfDayLabel(context.getTimeCycleManager());
        Color timeCol = new Color(255, 215, 110);
        int badgeW = g2d.getFontMetrics().stringWidth(timeLabel) + scale(10);
        UIButtonRenderer.drawBadge(g2d, timeLabel, rightX - badgeW, topY, timeCol, timeCol);

        g2d.setColor(new Color(55, 215, 210, 65));
        g2d.drawLine(leftX, topY + scale(7), rightX, topY + scale(7));

        List<GameSlot> slots = Framework.getInstance().getSaveController().getGameSlots();
        for (int i = 0; i < slots.size(); i++) {
            int cardY = SAVE_CARD_Y + i * SAVE_CARD_GAP;
            slots.get(i).renderAt(g2d, SAVE_CARD_X, cardY, SAVE_CARD_W, SAVE_CARD_H);
        }
    }

    private void renderFastTravelSection(Graphics2D g2d) {
        int leftX = TRAVEL_CARD_X;
        int rightX = TRAVEL_CARD_X + TRAVEL_CARD_W;
        int topY = RIGHT_PANEL_Y + scale(16);

        List<TotemNode> unlockedNodes = context.getLevelManager().getUnlockedTotemNodes();
        String currentLevelId = context.getLevelManager().getCurrentLevelId();

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(INV_TEXT_LABEL);
        g2d.drawString("Attuned Waypoints: " + unlockedNodes.size(), leftX, topY);

        g2d.setColor(new Color(55, 215, 210, 65));
        g2d.drawLine(leftX, topY + scale(7), rightX, topY + scale(7));

        if (unlockedNodes.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.ITALIC, FONT_MEDIUM));
            g2d.setColor(new Color(135, 155, 160));
            String msg = "No Save Totems attuned yet.";
            int mw = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, RIGHT_PANEL_X + (RIGHT_PANEL_W - mw) / 2, RIGHT_PANEL_Y + RIGHT_PANEL_H / 2);
            return;
        }

        for (int i = 0; i < Math.min(GAME_SLOT_CAP, unlockedNodes.size()); i++) {
            TotemNode node = unlockedNodes.get(i);
            int cardX = TRAVEL_CARD_X;
            int cardY = TRAVEL_CARD_Y + i * TRAVEL_CARD_GAP;

            boolean isHere = node.levelId().equalsIgnoreCase(currentLevelId);
            boolean selected = controller.getSelectedTotemIndex() == i;
            int arc = scale(6);
            GradientPaint gp = isHere
                    ? new GradientPaint(cardX, cardY, new Color(16, 42, 52, 235), cardX, cardY + TRAVEL_CARD_H, new Color(24, 62, 74, 240))
                    : new GradientPaint(cardX, cardY, new Color(16, 28, 34, 235), cardX, cardY + TRAVEL_CARD_H, new Color(24, 42, 48, 240));
            g2d.setPaint(gp);
            g2d.fillRoundRect(cardX, cardY, TRAVEL_CARD_W, TRAVEL_CARD_H, arc, arc);

            if (selected) {
                float pulse = (float) (0.7 + 0.3 * Math.sin(System.currentTimeMillis() / 180.0));
                g2d.setColor(new Color(45, 240, 230, (int) (90 * pulse)));
                g2d.setStroke(new BasicStroke(scale(2.6)));
                g2d.drawRoundRect(cardX - 1, cardY - 1, TRAVEL_CARD_W + 2, TRAVEL_CARD_H + 2, arc + 2, arc + 2);

                g2d.setColor(new Color(160, 255, 250, 245));
                g2d.setStroke(new BasicStroke(scale(1.3)));
                g2d.drawRoundRect(cardX, cardY, TRAVEL_CARD_W, TRAVEL_CARD_H, arc, arc);
            }
            else {
                g2d.setColor(new Color(85, 118, 124, 200));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(cardX, cardY, TRAVEL_CARD_W, TRAVEL_CARD_H, arc, arc);
            }

            int iconBoxS = TRAVEL_CARD_H - scale(12);
            int iconX = cardX + scale(6);
            int iconY = cardY + scale(6);
            Color slotFill = isHere ? new Color(45, 195, 190) : new Color(35, 95, 110);
            Color slotBorder = isHere ? new Color(90, 250, 240) : new Color(85, 175, 190);
            UIButtonRenderer.drawRunicSlot(g2d, iconX, iconY, iconBoxS, slotFill, slotBorder, false);

            if (totemIcon != null) {
                g2d.drawImage(totemIcon, iconX + scale(4), iconY + scale(3), iconBoxS - scale(8), iconBoxS - scale(6), null);
            }

            int textX = iconX + iconBoxS + scale(10);
            g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            g2d.setColor(Color.WHITE);
            g2d.drawString(node.totemName(), textX, cardY + scale(17));

            String badgeText = isHere ? "CURRENT LOCATION" : "READY TO TRAVEL";
            Color badgeColor = isHere ? new Color(80, 245, 235) : new Color(110, 240, 135);
            UIButtonRenderer.drawBadge(g2d, badgeText, textX, cardY + scale(33), badgeColor, badgeColor);
        }

        g2d.setFont(new Font("Arial", Font.ITALIC, FONT_SMALL));
        g2d.setColor(new Color(120, 175, 180));
        g2d.drawString("Right-Click or Enter to Fast Travel", leftX, RIGHT_PANEL_Y + RIGHT_PANEL_H - scale(7));
    }

    private String getTimeOfDayLabel(TimeCycleManager tcm) {
        if (tcm == null) return "DAY";
        if (tcm.isDawn()) return "DAWN";
        if (tcm.isDay()) return "DAYTIME";
        if (tcm.isDusk()) return "DUSK";
        return "NIGHTTIME";
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

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
    public void reset() {
        controller.reset();
    }

    public MediumButton[] getButtons() {
        return buttons;
    }
}