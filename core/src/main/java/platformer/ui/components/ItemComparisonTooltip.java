package platformer.ui.components;

import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemData;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.Constants.SCALE;
import static platformer.constants.UI.*;

/**
 * A UI component that displays a tooltip comparing the selected inventory item with the equipped item.
 * It shows the equipped item's name, its stats, and how they compare to the selected item.
 */
public class ItemComparisonTooltip {

    private final int width;
    private final int height;

    public ItemComparisonTooltip(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void render(Graphics g, InventoryItem selectedItem, InventoryItem equippedItem, int x, int y, Rectangle overlayBounds) {
        if (selectedItem == null || equippedItem == null) return;

        if (x + width > overlayBounds.x + overlayBounds.width)
            x = overlayBounds.x + overlayBounds.width - width;
        if (y + height > overlayBounds.y + overlayBounds.height)
            y = overlayBounds.y + overlayBounds.height - height;

        Rectangle bounds = new Rectangle(x, y, width, height);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(20, 20, 30, 245));
        g2d.fill(bounds);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(bounds);

        ItemData equippedData = equippedItem.getData();
        int yPos = bounds.y + (int)(15 * SCALE);

        g.setColor(equippedData.rarity.getTextColor());
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("Equipped: " + equippedData.name, bounds.x + (int)(10 * SCALE), yPos);
        yPos += (int)(10 * SCALE);

        g.setColor(new Color(100, 100, 120));
        g.drawLine(bounds.x + 5, yPos, bounds.x + width - 5, yPos);
        yPos += (int)(15 * SCALE);

        if (equippedData.equip != null && equippedData.equip.bonuses != null)
            renderStatComparison(g, selectedItem.getData(), equippedData, yPos, bounds);
    }

    private void renderStatComparison(Graphics g, ItemData selectedData, ItemData equippedData, int startY, Rectangle bounds) {
        Map<String, Double> selectedBonuses = selectedData.equip.bonuses;
        Map<String, Double> equippedBonuses = equippedData.equip.bonuses;

        FontMetrics fm = g.getFontMetrics();
        int yPos = startY;

        Set<String> allKeys = new HashSet<>(selectedBonuses.keySet());
        allKeys.addAll(equippedBonuses.keySet());

        for (String key : allKeys) {
            double selectedValue = selectedBonuses.getOrDefault(key, 0.0);
            double equippedValue = equippedBonuses.getOrDefault(key, 0.0);
            double diff = selectedValue - equippedValue;

            String label = capitalize(key) + ": ";
            String valueText = String.format("%.1f%%", selectedValue);
            String diffText = String.format("(%s%.1f%%)", diff >= 0 ? "+" : "", diff);

            g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
            g.setColor(INV_TEXT_LABEL);
            g.drawString(label, bounds.x + (int)(15 * SCALE), yPos);
            int labelWidth = fm.stringWidth(label);

            g.setColor(Color.WHITE);
            g.drawString(valueText, bounds.x + (int)(15 * SCALE) + labelWidth, yPos);
            int valueWidth = fm.stringWidth(valueText);

            if (diff > 0) g.setColor(new Color(100, 255, 100));
            else if (diff < 0) g.setColor(new Color(255, 100, 100));
            else g.setColor(INV_TEXT_LABEL);

            g.drawString(diffText, bounds.x + (int)(20 * SCALE) + labelWidth + valueWidth, yPos);
            yPos += (int)(15 * SCALE);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}