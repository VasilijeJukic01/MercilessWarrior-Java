package platformer.ui.overlays.hud;

import platformer.model.entities.player.Player;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.SLOT_INVENTORY;

/**
 * This panel is responsible for rendering the quick-use slots in the game's HUD.
 * It displays the items assigned to quick-use slots.
 */
public class QuickUsePanel {

    private final Player player;
    private final BufferedImage slotImage;

    private final int SLOT_HUD_SIZE = (int)(32 * SCALE);
    private final int ITEM_HUD_SIZE = (int)(16 * SCALE);
    private final float PANEL_ALPHA = 0.75f;

    public QuickUsePanel(Player player) {
        this.player = player;
        this.slotImage = ImageUtils.importImage(SLOT_INVENTORY, -1, -1);
    }

    public void render(Graphics g) {
        String[] quickUseItems = player.getInventory().getQuickUseSlots();
        int numberOfSlots = quickUseItems.length;
        if (numberOfSlots == 0) return;

        Graphics2D g2d = (Graphics2D) g;
        Composite originalComposite = g2d.getComposite();
        try {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PANEL_ALPHA));
            int slotSize = SLOT_HUD_SIZE;
            int slotSpacing = (int)(6 * SCALE);
            int panelPadding = (int)(8 * SCALE);
            int barWidth = (numberOfSlots * slotSize) + ((numberOfSlots - 1) * slotSpacing);
            int barStartX = (int)(20 * SCALE);
            int barY = GAME_HEIGHT - slotSize - (int)(20 * SCALE);
            renderBackgroundPanel(g, barStartX, barY, barWidth, slotSize, panelPadding);
            for (int i = 0; i < numberOfSlots; i++) {
                int currentSlotX = barStartX + (i * (slotSize + slotSpacing));
                g.drawImage(slotImage, currentSlotX, barY, slotSize, slotSize, null);
                renderItemInSlot(g, quickUseItems[i], currentSlotX, barY);
                renderKeybind(g, i + 1, currentSlotX, barY);
            }
        }
        finally {
            g2d.setComposite(originalComposite);
        }
    }

    private void renderBackgroundPanel(Graphics g, int barStartX, int barY, int barWidth, int slotSize, int padding) {
        Graphics2D g2d = (Graphics2D) g;
        int panelX = barStartX - padding;
        int panelY = barY - padding;
        int panelWidth = barWidth + (padding * 2);
        int panelHeight = slotSize + (padding * 2);

        g2d.setColor(new Color(10, 20, 30));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
    }

    private void renderItemInSlot(Graphics g, String itemId, int x, int y) {
        if (itemId == null) return;
        Inventory inventory = player.getInventory();
        Optional<InventoryItem> itemOptional = inventory.getBackpack().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst();

        if (itemOptional.isEmpty()) return;
        InventoryItem item = itemOptional.get();
        ItemData itemData = item.getData();
        if (itemData == null) return;

        int itemX = x + (SLOT_HUD_SIZE - ITEM_HUD_SIZE) / 2;
        int itemY = y + (SLOT_HUD_SIZE - ITEM_HUD_SIZE) / 2;

        g.setColor(itemData.rarity.getColor());
        g.fillRoundRect(x + (int)(2*SCALE), y + (int)(2*SCALE), SLOT_HUD_SIZE - (int)(4*SCALE), SLOT_HUD_SIZE - (int)(4*SCALE), 5, 5);
        g.drawImage(item.getModel(), itemX, itemY, ITEM_HUD_SIZE, ITEM_HUD_SIZE, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        String amount = String.valueOf(item.getAmount());
        int amountX = x + SLOT_HUD_SIZE - g.getFontMetrics().stringWidth(amount) - (int)(5 * SCALE);
        int amountY = y + SLOT_HUD_SIZE - (int)(5 * SCALE);
        g.drawString(amount, amountX, amountY);
    }

    private void renderKeybind(Graphics g, int key, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        String keyText = String.valueOf(key);
        Font font = new Font("Arial", Font.BOLD, FONT_SMALL);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (int)(5 * SCALE);
        int textY = y + fm.getAscent() + (int)(3 * SCALE);

        g2d.setColor(Color.BLACK);
        g2d.drawString(keyText, textX + 1, textY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(keyText, textX, textY);
    }
}