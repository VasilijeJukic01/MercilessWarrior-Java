package platformer.ui.components;

import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * A UI component that displays a runic tooltip comparing the hovered backpack item with the currently equipped item.
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

        UIButtonRenderer.drawRunicSubPanel(g, x, y, width, height);

        ItemData equippedData = equippedItem.getData();
        if (equippedData == null) return;

        int padX = x + scale(10);
        int rightX = x + width - scale(10);
        int yPos = y + scale(16);

        g.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        g.setColor(new Color(150, 185, 190));
        g.drawString("CURRENTLY EQUIPPED", padX, yPos);
        yPos += scale(13);

        g.setColor(equippedData.rarity.getTextColor());
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(equippedData.name, padX, yPos);
        yPos += scale(6);

        g.setColor(new Color(55, 215, 210, 75));
        g.drawLine(padX, yPos, rightX, yPos);
        yPos += scale(14);

        if (equippedData.equip != null && equippedData.equip.bonuses != null) {
            renderStatComparison(g, selectedItem.getData(), equippedData, yPos, padX, rightX);
        }
    }

    private void renderStatComparison(Graphics g, ItemData selectedData, ItemData equippedData, int startY, int leftX, int rightX) {
        Map<String, Double> selectedBonuses = selectedData.equip.bonuses;
        Map<String, Double> equippedBonuses = equippedData.equip.bonuses;

        int yPos = startY;
        Set<String> allKeys = new HashSet<>(selectedBonuses.keySet());
        allKeys.addAll(equippedBonuses.keySet());

        for (String key : allKeys) {
            double selectedValue = selectedBonuses.getOrDefault(key, 0.0);
            double equippedValue = equippedBonuses.getOrDefault(key, 0.0);
            double diff = selectedValue - equippedValue;

            g.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
            g.setColor(INV_TEXT_LABEL);
            g.drawString(capitalize(key) + ":", leftX, yPos);

            String valueText = String.format("%.0f%% ", selectedValue);
            String diffText = String.format("(%s%.0f%%)", diff >= 0 ? "+" : "", diff);

            g.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
            FontMetrics fm = g.getFontMetrics();
            int diffW = fm.stringWidth(diffText);
            int valW = fm.stringWidth(valueText);

            g.setColor(Color.WHITE);
            g.drawString(valueText, rightX - diffW - valW, yPos);

            if (diff > 0) g.setColor(new Color(100, 255, 120));
            else if (diff < 0) g.setColor(new Color(255, 95, 95));
            else g.setColor(INV_TEXT_LABEL);

            g.drawString(diffText, rightX - diffW, yPos);
            yPos += scale(14);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}