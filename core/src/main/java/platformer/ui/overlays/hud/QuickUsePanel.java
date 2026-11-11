package platformer.ui.overlays.hud;

import platformer.model.entities.player.Player;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;

import static platformer.constants.Constants.*;

/**
 * Renders the styled runic quick-use potion slots in the bottom-left corner of the HUD.
 */
public class QuickUsePanel {

    private final Player player;

    public static final int SLOT_HUD_SIZE = scale(26);
    public static final int SLOT_HUD_SPACING = scale(6);
    public static final int BAR_START_X = scale(16);
    public static final int BAR_START_Y = GAME_HEIGHT - SLOT_HUD_SIZE - scale(16);

    private static final int ITEM_HUD_SIZE = scale(18);
    private static final int PANEL_PADDING = scale(6);

    private static final Color PANEL_BG_TOP = new Color(14, 22, 26, 210);
    private static final Color PANEL_BG_BOTTOM = new Color(20, 32, 38, 225);
    private static final Color OUTER_BORDER = new Color(115, 138, 142, 200);
    private static final Color INNER_TRIM = new Color(45, 195, 190, 75);
    private static final Color SLOT_BG_TOP = new Color(10, 16, 19, 230);
    private static final Color SLOT_BG_BOTTOM = new Color(18, 28, 33, 230);
    private static final Color SLOT_BORDER = new Color(75, 95, 100, 180);

    public QuickUsePanel(Player player) {
        this.player = player;
    }

    public void render(Graphics g) {
        String[] quickUseItems = player.getInventory().getQuickUseSlots();
        int numberOfSlots = quickUseItems.length;
        if (numberOfSlots == 0) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int barWidth = (numberOfSlots * SLOT_HUD_SIZE) + ((numberOfSlots - 1) * SLOT_HUD_SPACING);
        renderRunicPlaque(g2d, BAR_START_X, BAR_START_Y, barWidth, SLOT_HUD_SIZE);
        for (int i = 0; i < numberOfSlots; i++) {
            int slotX = BAR_START_X + i * (SLOT_HUD_SIZE + SLOT_HUD_SPACING);
            renderSlotBase(g2d, slotX, BAR_START_Y);
            renderItemInSlot(g2d, quickUseItems[i], slotX, BAR_START_Y);
            renderKeybindBadge(g2d, i + 1, slotX, BAR_START_Y);
        }

        g2d.dispose();
    }

    private void renderRunicPlaque(Graphics2D g2d, int startX, int startY, int barWidth, int slotSize) {
        int x = startX - PANEL_PADDING;
        int y = startY - PANEL_PADDING;
        int w = barWidth + PANEL_PADDING * 2;
        int h = slotSize + PANEL_PADDING * 2;
        int arc = scale(6);

        GradientPaint bg = new GradientPaint(x, y, PANEL_BG_TOP, x, y + h, PANEL_BG_BOTTOM);
        g2d.setPaint(bg);
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        g2d.setColor(OUTER_BORDER);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, w, h, arc, arc);

        int inset = scale(2);
        g2d.setColor(INNER_TRIM);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(x + inset, y + inset, w - inset * 2, h - inset * 2, arc / 2, arc / 2);

        int cLen = scale(6);
        g2d.setColor(new Color(175, 198, 202, 210));
        g2d.setStroke(new BasicStroke(1.8f));
        g2d.drawLine(x, y, x + cLen, y);
        g2d.drawLine(x, y, x, y + cLen);
        g2d.drawLine(x + w, y, x + w - cLen, y);
        g2d.drawLine(x + w, y, x + w, y + cLen);
        g2d.drawLine(x, y + h, x + cLen, y + h);
        g2d.drawLine(x, y + h, x, y + h - cLen);
        g2d.drawLine(x + w, y + h, x + w - cLen, y + h);
        g2d.drawLine(x + w, y + h, x + w, y + h - cLen);
    }

    private void renderSlotBase(Graphics2D g2d, int x, int y) {
        int arc = scale(5);
        GradientPaint slotBg = new GradientPaint(x, y, SLOT_BG_TOP, x, y + SLOT_HUD_SIZE, SLOT_BG_BOTTOM);
        g2d.setPaint(slotBg);
        g2d.fillRoundRect(x, y, SLOT_HUD_SIZE, SLOT_HUD_SIZE, arc, arc);

        g2d.setColor(SLOT_BORDER);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(x, y, SLOT_HUD_SIZE, SLOT_HUD_SIZE, arc, arc);
    }

    private void renderItemInSlot(Graphics2D g2d, String itemId, int x, int y) {
        if (itemId == null) {
            renderEmptyPotionSilhouette(g2d, x, y);
            return;
        }
        Inventory inventory = player.getInventory();
        Optional<InventoryItem> itemOptional = inventory.getBackpack().stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst();

        if (itemOptional.isEmpty()) {
            renderEmptyPotionSilhouette(g2d, x, y);
            return;
        }

        InventoryItem item = itemOptional.get();
        ItemData itemData = item.getData();
        if (itemData == null) return;

        int inset = scale(2);
        int arc = scale(4);
        Color rarityCol = itemData.rarity.getColor();
        g2d.setColor(new Color(rarityCol.getRed(), rarityCol.getGreen(), rarityCol.getBlue(), 75));
        g2d.fillRoundRect(x + inset, y + inset, SLOT_HUD_SIZE - inset * 2, SLOT_HUD_SIZE - inset * 2, arc, arc);

        Color borderCol = itemData.rarity.getTextColor();
        g2d.setColor(new Color(borderCol.getRed(), borderCol.getGreen(), borderCol.getBlue(), 150));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(x + 1, y + 1, SLOT_HUD_SIZE - 2, SLOT_HUD_SIZE - 2, arc, arc);

        int itemX = x + (SLOT_HUD_SIZE - ITEM_HUD_SIZE) / 2;
        int itemY = y + (SLOT_HUD_SIZE - ITEM_HUD_SIZE) / 2;
        g2d.drawImage(item.getModel(), itemX, itemY, ITEM_HUD_SIZE, ITEM_HUD_SIZE, null);

        String amount = String.valueOf(item.getAmount());
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        FontMetrics fm = g2d.getFontMetrics();
        int textW = fm.stringWidth(amount);
        int tx = x + SLOT_HUD_SIZE - textW - scale(2.5);
        int ty = y + SLOT_HUD_SIZE - scale(2.5);

        g2d.setColor(new Color(8, 12, 14, 220));
        g2d.drawString(amount, tx + 1, ty + 1);
        g2d.setColor(new Color(235, 245, 245));
        g2d.drawString(amount, tx, ty);
    }

    private void renderEmptyPotionSilhouette(Graphics2D g2d, int x, int y) {
        int cx = x + SLOT_HUD_SIZE / 2;
        int cy = y + SLOT_HUD_SIZE / 2 + scale(1);
        int r = scale(4.5);

        g2d.setColor(new Color(90, 115, 120, 45));
        g2d.fillOval(cx - r, cy - r / 2, r * 2, r * 2);
        g2d.fillRect(cx - scale(2), cy - r - scale(2), scale(4), scale(3));
    }

    private void renderKeybindBadge(Graphics2D g2d, int key, int x, int y) {
        int badgeSize = scale(9);
        int bx = x - scale(1);
        int by = y - scale(1);

        g2d.setColor(new Color(14, 24, 28, 240));
        g2d.fillRoundRect(bx, by, badgeSize, badgeSize, scale(3), scale(3));
        g2d.setColor(new Color(55, 215, 210, 150));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(bx, by, badgeSize, badgeSize, scale(3), scale(3));

        String keyText = String.valueOf(key);
        g2d.setFont(UIButtonRenderer.getFont(6.5f * SCALE));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = bx + (badgeSize - fm.stringWidth(keyText)) / 2;
        int ty = by + (badgeSize + (int) (fm.getAscent() * 0.72f)) / 2;

        g2d.setColor(new Color(215, 240, 240));
        g2d.drawString(keyText, tx, ty);
    }
}