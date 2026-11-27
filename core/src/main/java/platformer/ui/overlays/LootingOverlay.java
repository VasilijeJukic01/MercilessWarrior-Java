package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.core.GameContext;
import platformer.core.config.BalanceManager;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.objects.Container;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryBonus;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.components.ItemComparisonTooltip;
import platformer.ui.overlays.controller.LootingViewController;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Renders the Runic Looting overlay for crates, barrels, and fallen enemy loot bags.
 * Displays a 2x6 runic slot grid, an item inspector with set bonus previews, and equipment comparison tooltips.
 */
public class LootingOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    public static final int PANEL_W = scale(384);
    public static final int PANEL_H = scale(336);
    public static final int PANEL_X = (GAME_WIDTH - PANEL_W) / 2;
    public static final int PANEL_Y = (GAME_HEIGHT - PANEL_H) / 2;

    public static final int SUB_X = PANEL_X + scale(18);
    public static final int SUB_W = PANEL_W - scale(36);

    public static final int GRID_PANEL_Y = PANEL_Y + scale(64);
    public static final int GRID_PANEL_H = scale(104);
    public static final int GRID_COLS = 6;
    public static final int GRID_ROWS = 2;
    public static final int GRID_SLOT_GAP = scale(8);
    public static final int GRID_SLOT_STEP = SLOT_SIZE + GRID_SLOT_GAP;
    public static final int GRID_TOTAL_W = (GRID_COLS - 1) * GRID_SLOT_STEP + SLOT_SIZE;
    public static final int GRID_TOTAL_H = (GRID_ROWS - 1) * GRID_SLOT_STEP + SLOT_SIZE;
    public static final int GRID_START_X = SUB_X + (SUB_W - GRID_TOTAL_W) / 2;
    public static final int GRID_START_Y = GRID_PANEL_Y + (GRID_PANEL_H - GRID_TOTAL_H) / 2;

    public static final int INSPECTOR_Y = GRID_PANEL_Y + GRID_PANEL_H + scale(10);
    public static final int INSPECTOR_H = scale(90);

    private final GameContext context;
    private final LootingViewController controller;
    private final ItemComparisonTooltip comparisonTooltip;
    private final Map<String, Integer> equipmentSlots = new HashMap<>();

    private final Rectangle2D.Double overlayBounds;
    private final MediumButton[] buttons = new MediumButton[3];
    private BufferedImage coinIcon;

    private final Font boldFont = new Font("Arial", Font.BOLD, FONT_MEDIUM);
    private final Font plainFont = new Font("Arial", Font.PLAIN, FONT_LIGHT);
    private final Font smallBoldFont = new Font("Arial", Font.BOLD, FONT_SMALL);
    private final Font hintFont = new Font("Arial", Font.ITALIC, FONT_SMALL);

    public LootingOverlay(GameContext context) {
        this.context = context;
        this.overlayBounds = new Rectangle2D.Double(PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        this.comparisonTooltip = new ItemComparisonTooltip(scale(170), scale(125));
        this.controller = new LootingViewController(context, this);
        initEquipmentSlots();
        loadImages();
        loadButtons();
    }

    private void initEquipmentSlots() {
        equipmentSlots.put("Helmet", 0);
        equipmentSlots.put("Trousers", 1);
        equipmentSlots.put("Armor", 2);
        equipmentSlots.put("Ring", 3);
        equipmentSlots.put("Charm", 3);
        equipmentSlots.put("Bracelets", 4);
        equipmentSlots.put("Boots", 5);
    }

    private void loadImages() {
        this.coinIcon = SpriteManager.getInstance().getCoinAnimations()[1][0];
    }

    private void loadButtons() {
        int btnW = scale(88);
        int btnH = scale(28);
        int gap = scale(16);
        int totalW = btnW * 3 + gap * 2;
        int startX = PANEL_X + (PANEL_W - totalW) / 2;
        int btnY = PANEL_Y + PANEL_H - btnH - scale(12);

        buttons[0] = new MediumButton(startX, btnY, btnW, btnH, ButtonType.TAKE);
        buttons[1] = new MediumButton(startX + btnW + gap, btnY, btnW, btnH, ButtonType.TAKE_ALL);
        buttons[2] = new MediumButton(startX + (btnW + gap) * 2, btnY, btnW, btnH, ButtonType.CLOSE);
    }

    @Override
    public void update() {
        controller.update();
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        if (controller.getItemsFromSource() == null) {
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
            return;
        }

        g.setColor(new Color(0, 0, 0, 165));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        UIButtonRenderer.drawRunicPanel(g2d, overlayBounds);
        UIButtonRenderer.drawRunicHeader(g2d, resolveHeaderTitle(), PANEL_X + PANEL_W / 2, PANEL_Y + scale(10));

        renderGridSection(g2d);
        renderInspectorSection(g2d);
        renderHotkeyHint(g2d);
        Arrays.stream(buttons).forEach(b -> b.render(g2d));
        renderComparisonTooltip(g2d);

        g2d.dispose();
    }

    private String resolveHeaderTitle() {
        Interactable source = controller.getActiveSource();
        if (source instanceof Container container) {
            return container.getObjType() == ObjType.BARREL ? "STORAGE BARREL" : "WOODEN CRATE";
        }
        return "SPOILS OF BATTLE";
    }

    private void renderGridSection(Graphics2D g2d) {
        List<InventoryItem> items = controller.getVisibleItems();

        g2d.setFont(UIButtonRenderer.getFont(9.5f * SCALE));
        g2d.setColor(new Color(205, 226, 228));
        g2d.drawString("DISCOVERED ITEMS", SUB_X + scale(4), GRID_PANEL_Y - scale(4));

        g2d.setFont(smallBoldFont);
        String countText = items.size() + (items.size() == 1 ? " Item" : " Items");
        int countW = g2d.getFontMetrics().stringWidth(countText);
        g2d.setColor(items.isEmpty() ? new Color(135, 155, 160) : new Color(90, 235, 225));
        g2d.drawString(countText, SUB_X + SUB_W - countW - scale(4), GRID_PANEL_Y - scale(4));

        UIButtonRenderer.drawRunicSubPanel(g2d, SUB_X, GRID_PANEL_Y, SUB_W, GRID_PANEL_H);

        int selectedSlot = controller.getSelectedSlot();
        int hoveredSlot = controller.getHoveredSlot();
        int selX = -1, selY = -1;

        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int idx = r * GRID_COLS + c;
                int sx = GRID_START_X + c * GRID_SLOT_STEP;
                int sy = GRID_START_Y + r * GRID_SLOT_STEP;

                InventoryItem item = idx < items.size() ? items.get(idx) : null;
                ItemData data = (item != null) ? item.getData() : null;
                Color rFill = (data != null && data.rarity != null) ? data.rarity.getColor() : null;
                Color rBorder = (data != null && data.rarity != null) ? data.rarity.getTextColor() : null;

                UIButtonRenderer.drawRunicSlot(g2d, sx, sy, SLOT_SIZE, rFill, rBorder, idx == hoveredSlot);

                if (item != null && data != null) {
                    BufferedImage model = item.getModel();
                    if (model != null) {
                        g2d.drawImage(model, sx + ITEM_OFFSET_X, sy + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE, null);
                    }
                    if (data.stackable || item.getAmount() > 1) {
                        UIButtonRenderer.drawStackCount(g2d, item.getAmount(), sx, sy, SLOT_SIZE);
                    }
                }

                if (idx == selectedSlot) {
                    selX = sx;
                    selY = sy;
                }
            }
        }

        if (selX != -1) {
            UIButtonRenderer.drawSelectedSlotFrame(g2d, selX, selY, SLOT_SIZE);
        }
    }

    private void renderInspectorSection(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, SUB_X, INSPECTOR_Y, SUB_W, INSPECTOR_H);

        List<InventoryItem> items = controller.getVisibleItems();
        InventoryItem item = controller.getHoveredItem();
        if (item == null) {
            item = controller.getSelectedItem();
        }

        if (item == null || item.getData() == null) {
            g2d.setFont(hintFont);
            g2d.setColor(new Color(135, 155, 160));
            String msg = items.isEmpty() ? "This container is empty." : "Select an item to inspect, or press [SPACE] to Take All.";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, SUB_X + (SUB_W - fm.stringWidth(msg)) / 2, INSPECTOR_Y + INSPECTOR_H / 2 + scale(3));
            return;
        }

        ItemData data = item.getData();
        int pad = scale(10);
        int previewX = SUB_X + pad;
        int previewY = INSPECTOR_Y + pad;
        int previewS = scale(38);

        UIButtonRenderer.drawRunicSlot(g2d, previewX, previewY, previewS, data.rarity.getColor(), data.rarity.getTextColor(), false);
        BufferedImage model = item.getModel();
        if (model != null) {
            g2d.drawImage(model, previewX + scale(4), previewY + scale(4), previewS - scale(8), previewS - scale(8), null);
        }
        if (data.stackable || item.getAmount() > 1) {
            UIButtonRenderer.drawStackCount(g2d, item.getAmount(), previewX, previewY, previewS);
        }

        int textX = previewX + previewS + scale(10);
        int rightX = SUB_X + SUB_W - pad;
        int titleY = previewY + scale(12);

        g2d.setFont(boldFont);
        g2d.setColor(data.rarity.getTextColor());
        g2d.drawString(data.name, textX, titleY);

        int nameW = g2d.getFontMetrics().stringWidth(data.name);
        String badgeText;
        Color badgeBg = data.rarity.getColor();
        Color badgeFg = data.rarity.getTextColor();
        if (data.questItem) {
            badgeText = "QUEST ITEM";
            badgeBg = new Color(35, 165, 175);
            badgeFg = new Color(115, 248, 245);
        }
        else if (data.equip != null && data.equip.canEquip) {
            badgeText = data.rarity.name() + " | " + data.equip.slot.toUpperCase();
        }
        else {
            badgeText = data.rarity.name();
        }
        UIButtonRenderer.drawBadge(g2d, badgeText, textX + nameW + scale(7), titleY, badgeBg, badgeFg);

        if (!data.questItem && data.sellValue > 0) {
            String valText = String.valueOf(data.sellValue);
            g2d.setFont(boldFont);
            int valW = g2d.getFontMetrics().stringWidth(valText);
            int iconS = (int) (COIN_WID / 1.35);
            g2d.setColor(INV_TEXT_VALUE);
            g2d.drawString(valText, rightX - valW - iconS - scale(3), titleY);
            if (coinIcon != null) {
                g2d.drawImage(coinIcon, rightX - iconS, titleY - g2d.getFontMetrics().getAscent() + 1, iconS, iconS, null);
            }
        }

        int lineY = titleY + scale(6);
        g2d.setColor(new Color(55, 215, 210, 60));
        g2d.drawLine(textX, lineY, rightX, lineY);

        g2d.setFont(plainFont);
        g2d.setColor(INV_TEXT_DESC);
        List<String> lines = wrapText(data.description, rightX - textX, g2d.getFontMetrics());
        int descY = lineY + scale(13);
        int maxDescLines = (data.equip != null && data.equip.setName != null && !data.equip.setName.isEmpty()) ? 2 : 3;
        for (int i = 0; i < Math.min(lines.size(), maxDescLines); i++) {
            g2d.drawString(lines.get(i), textX, descY);
            descY += g2d.getFontMetrics().getHeight();
        }

        if (data.equip != null && data.equip.setName != null && !data.equip.setName.isEmpty()) {
            String setName = data.equip.setName;
            InventoryBonus equipBonus = context.getPlayer().getStats().getEquipment();
            int count = equipBonus.getEquippedSetCount(setName);
            Map<String, Double> setBonuses = BalanceManager.getInstance().getSetBonus(setName);
            if (setBonuses != null && !setBonuses.isEmpty()) {
                StringBuilder sb = new StringBuilder(setName).append(" SET (").append(count).append("/5): ");
                setBonuses.forEach((stat, val) -> sb.append("+").append(val.intValue()).append("% ").append(stat.toUpperCase()).append("  "));
                g2d.setFont(smallBoldFont);
                g2d.setColor(count >= 5 ? new Color(255, 215, 75) : new Color(135, 165, 170));
                g2d.drawString(sb.toString().trim(), textX, INSPECTOR_Y + INSPECTOR_H - scale(7));
            }
        }
    }

    private void renderHotkeyHint(Graphics2D g2d) {
        g2d.setFont(hintFont);
        g2d.setColor(new Color(135, 180, 185));
        String hint = "[X / Right-Click] Take   |   [SPACE] Take All   |   [ESC] Close";
        FontMetrics fm = g2d.getFontMetrics();
        int hx = PANEL_X + (PANEL_W - fm.stringWidth(hint)) / 2;
        int hy = INSPECTOR_Y + INSPECTOR_H + scale(13);
        g2d.drawString(hint, hx, hy);
    }

    private void renderComparisonTooltip(Graphics2D g2d) {
        InventoryItem hoveredItem = controller.getHoveredItem();
        if (hoveredItem == null || hoveredItem.getData() == null || hoveredItem.getData().equip == null || !hoveredItem.getData().equip.canEquip) {
            return;
        }
        Point mousePos = controller.getMousePosition();
        if (mousePos == null) return;

        Inventory inventory = context.getPlayer().getInventory();
        String slotType = hoveredItem.getData().equip.slot;
        for (Map.Entry<String, Integer> entry : equipmentSlots.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(slotType)) {
                InventoryItem equippedItem = inventory.getEquipped()[entry.getValue()];
                if (equippedItem != null) {
                    comparisonTooltip.render(g2d, hoveredItem, equippedItem, mousePos.x + scale(12), mousePos.y + scale(12), overlayBounds.getBounds());
                }
                break;
            }
        }
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                if (fm.stringWidth(currentLine + " " + word) <= maxWidth) {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                }
                else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
            if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        }
        return lines;
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        controller.mouseClicked(e);
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
    public void reset() {
        controller.reset();
    }

    public MediumButton[] getButtons() {
        return buttons;
    }
}