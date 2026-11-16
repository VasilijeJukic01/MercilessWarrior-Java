package platformer.ui.overlays;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryBonus;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.state.types.GameState;
import platformer.ui.buttons.*;
import platformer.ui.components.ItemComparisonTooltip;
import platformer.ui.dnd.DragAndDropManager;
import platformer.ui.dnd.DragSourceType;
import platformer.ui.overlays.controller.InventoryViewController;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * Renders the player's Inventory overlay with runic plaques, interactive Quick-Use potion slots, equipment slots, stat summary, and item inspector.
 */
public class InventoryOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    public static final int INV_QUICK_SLOT_X = scale(595);
    public static final int INV_QUICK_SLOT_Y = scale(252);
    public static final int INV_QUICK_SLOT_SIZE = scale(28);
    public static final int INV_QUICK_SLOT_SPACING = scale(6);

    private final InventoryViewController controller;
    private final ItemComparisonTooltip comparisonTooltip;
    private final Map<String, Integer> equipmentSlots;

    private Rectangle2D overlay;
    private final MediumButton[] mediumButtons;
    private MediumButton unequipBtn;
    private final SmallButton[] smallButtons;

    private Rectangle2D backpackPanel, equipPanel, statsPanel, inspectorPanel;
    private BufferedImage coinIcon;
    private BufferedImage[] playerAnim;
    private Rectangle2D.Double selectedSlot;

    private BufferedImage[] equipmentPlaceholders;
    private final int[] slotIconMap = new int[6];

    private int playerAnimTick, playerAnimIndex;
    private final int playerAnimSpeed = 20;

    public InventoryOverlay(GameState gameState) {
        this.controller = new InventoryViewController(gameState, this);
        this.mediumButtons = new MediumButton[3];
        this.smallButtons = new SmallButton[2];
        this.comparisonTooltip = new ItemComparisonTooltip((int) (170 * SCALE), (int) (125 * SCALE));
        this.equipmentSlots = new HashMap<>();
        init();
    }

    private void init() {
        loadImages();
        initEquipmentSlots();
        loadButtons();
        initSelectedSlot();
    }

    private void loadImages() {
        this.overlay = new Rectangle2D.Double(INV_OVERLAY_X, INV_OVERLAY_Y, INV_OVERLAY_WID, INV_OVERLAY_HEI);
        this.backpackPanel = new Rectangle2D.Double(BACKPACK_X, BACKPACK_Y, BACKPACK_WID, BACKPACK_HEI);
        this.equipPanel = new Rectangle2D.Double(EQUIPMENT_X, EQUIPMENT_Y, EQUIPMENT_WID, EQUIPMENT_HEI);
        this.statsPanel = new Rectangle2D.Double(scale(570), EQUIPMENT_Y, scale(175), scale(135));
        this.inspectorPanel = new Rectangle2D.Double(EQUIPMENT_X, scale(286), scale(365), scale(92));

        this.equipmentPlaceholders = new BufferedImage[6];
        BufferedImage placeholderSheet = ImageUtils.importImage(EQUIPMENT_PLACEHOLDERS_PATH, -1, -1);
        int iconWidth = placeholderSheet.getWidth() / 6;
        int iconHeight = placeholderSheet.getHeight();
        for (int i = 0; i < 6; i++) {
            equipmentPlaceholders[i] = placeholderSheet.getSubimage(i * iconWidth, 0, iconWidth, iconHeight);
        }

        BufferedImage[] idleAnim = SpriteManager.getInstance().getPlayerAnimations(true)[Anim.IDLE.ordinal()];
        this.playerAnim = new BufferedImage[idleAnim.length];
        for (int i = 0; i < idleAnim.length; i++) {
            this.playerAnim[i] = ImageUtils.resizeImage(idleAnim[i], INV_PLAYER_WID, INV_PLAYER_HEI);
        }

        this.coinIcon = SpriteManager.getInstance().getCoinAnimations()[1][0];
    }

    private void initEquipmentSlots() {
        equipmentSlots.put("Helmet", 0);
        equipmentSlots.put("Trousers", 1);
        equipmentSlots.put("Armor", 2);
        equipmentSlots.put("Ring", 3);
        equipmentSlots.put("Charm", 3);
        equipmentSlots.put("Bracelets", 4);
        equipmentSlots.put("Boots", 5);

        slotIconMap[0] = 0;
        slotIconMap[1] = 4;
        slotIconMap[2] = 1;
        slotIconMap[3] = 3;
        slotIconMap[4] = 2;
        slotIconMap[5] = 5;
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(PREV_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(NEXT_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
        mediumButtons[0] = new MediumButton(USE_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.USE);
        mediumButtons[1] = new MediumButton(EQUIP_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.EQUIP);
        mediumButtons[2] = new MediumButton(DROP_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.DROP);
        this.unequipBtn = new MediumButton(UNEQUIP_BTN_X, UNEQUIP_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.UNEQUIP);
    }

    private void initSelectedSlot() {
        int xPos = (controller.getBackpackSlotNumber() % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
        int yPos = (controller.getBackpackSlotNumber() / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    public void updateSelectedSlot(boolean isInBackpack) {
        if (isInBackpack) {
            this.selectedSlot.x = (controller.getBackpackSlotNumber() % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
            this.selectedSlot.y = (controller.getBackpackSlotNumber() / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y;
        }
        else {
            this.selectedSlot.x = (controller.getEquipmentSlotNumber() % EQUIPMENT_SLOT_MAX_ROW) * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X;
            this.selectedSlot.y = (controller.getEquipmentSlotNumber() / EQUIPMENT_SLOT_MAX_ROW) * SLOT_SPACING + EQUIPMENT_SLOT_Y;
        }
    }

    @Override
    public void update() {
        controller.update();
        updatePlayerAnimation();
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
        unequipBtn.update();
    }

    private void updatePlayerAnimation() {
        playerAnimTick++;
        if (playerAnimTick >= playerAnimSpeed) {
            playerAnimTick = 0;
            playerAnimIndex = (playerAnimIndex + 1) % playerAnim.length;
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 165));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        Graphics2D g2d = (Graphics2D) g;

        renderOverlay(g2d);
        renderBackpackPanel(g2d);
        renderEquipmentPanel(g2d);
        renderStatsAndQuickUsePanel(g2d);
        renderInspectorPanel(g2d);
        renderBackpackSlots(g);
        renderEquipmentSlots(g);
        renderQuickUseSlots(g2d);

        UIButtonRenderer.drawSelectedSlotFrame(g, (int) selectedSlot.x, (int) selectedSlot.y, (int) selectedSlot.width);
        renderButtons(g);
        renderTooltip(g);
        controller.getDndManager().render(g);
    }

    private void renderOverlay(Graphics2D g2d) {
        UIButtonRenderer.drawRunicPanel(g2d, overlay);
        UIButtonRenderer.drawRunicHeader(g2d, "INVENTORY", INV_OVERLAY_X + INV_OVERLAY_WID / 2, INV_TEXT_Y);
    }

    private void renderBackpackPanel(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) backpackPanel.getX(), (int) backpackPanel.getY(), (int) backpackPanel.getWidth(), (int) backpackPanel.getHeight());

        g2d.setFont(UIButtonRenderer.getFont(10.5f * SCALE));
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("BACKPACK", (int) backpackPanel.getX() + scale(4), (int) backpackPanel.getY() - scale(5));

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(INV_TEXT_LABEL);
        int itemsPerPage = INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL;
        int maxPages = Math.max(1, (int) Math.ceil((double) BACKPACK_CAPACITY / itemsPerPage));
        String pageText = "Page " + (controller.getBackpackSlot() + 1) + " / " + maxPages;
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (int) (backpackPanel.getX() + backpackPanel.getWidth() - fm.stringWidth(pageText) - scale(4));
        g2d.drawString(pageText, textX, (int) backpackPanel.getY() - scale(5));
    }

    private void renderBackpackSlots(Graphics g) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        int pageOffset = controller.getBackpackSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL);
        DragAndDropManager dnd = controller.getDndManager();

        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xPos = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int yPos = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                int absSlot = (j * INVENTORY_SLOT_MAX_ROW + i) + pageOffset;

                InventoryItem item = (absSlot < inventory.getBackpack().size()) ? inventory.getBackpack().get(absSlot) : null;
                boolean beingDragged = dnd.isDragging() && !dnd.isSplitDrag() && dnd.getSource() == DragSourceType.BACKPACK && dnd.getSourceIndex() == absSlot;

                Color rarityFill = (!beingDragged && item != null && item.getData() != null) ? item.getData().rarity.getColor() : null;
                Color rarityBorder = (!beingDragged && item != null && item.getData() != null) ? item.getData().rarity.getTextColor() : null;
                boolean hovered = controller.getHoveredBackpackSlot() == (j * INVENTORY_SLOT_MAX_ROW + i);

                UIButtonRenderer.drawRunicSlot(g, xPos, yPos, SLOT_SIZE, rarityFill, rarityBorder, hovered);

                if (item != null && !beingDragged) {
                    g.drawImage(item.getModel(), xPos + ITEM_OFFSET_X, yPos + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE, null);
                    if (item.getData() != null && item.getData().stackable) {
                        UIButtonRenderer.drawStackCount(g, item.getAmount(), xPos, yPos, SLOT_SIZE);
                    }
                }
            }
        }
    }

    private void renderEquipmentPanel(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) equipPanel.getX(), (int) equipPanel.getY(), (int) equipPanel.getWidth(), (int) equipPanel.getHeight());
        g2d.setFont(UIButtonRenderer.getFont(10.5f * SCALE));
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("EQUIPMENT", (int) equipPanel.getX() + scale(4), (int) equipPanel.getY() - scale(5));

        g2d.drawImage(playerAnim[playerAnimIndex], INV_PLAYER_X, INV_PLAYER_Y, INV_PLAYER_WID, INV_PLAYER_HEI, null);
    }

    private void renderEquipmentSlots(Graphics g) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        DragAndDropManager dnd = controller.getDndManager();
        Graphics2D g2d = (Graphics2D) g.create();

        try {
            for (int j = 0; j < EQUIPMENT_SLOT_MAX_COL; j++) {
                for (int i = 0; i < EQUIPMENT_SLOT_MAX_ROW; i++) {
                    int xPos = i * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X;
                    int yPos = j * SLOT_SPACING + EQUIPMENT_SLOT_Y;
                    int slotIndex = j * EQUIPMENT_SLOT_MAX_ROW + i;

                    InventoryItem item = inventory.getEquipped()[slotIndex];
                    boolean beingDragged = dnd.isDragging() && dnd.getSource() == DragSourceType.EQUIPMENT && dnd.getSourceIndex() == slotIndex;
                    Color rarityFill = (!beingDragged && item != null && item.getData() != null) ? item.getData().rarity.getColor() : null;
                    Color rarityBorder = (!beingDragged && item != null && item.getData() != null) ? item.getData().rarity.getTextColor() : null;
                    UIButtonRenderer.drawRunicSlot(g2d, xPos, yPos, SLOT_SIZE, rarityFill, rarityBorder, false);

                    if (item == null || beingDragged) {
                        BufferedImage placeholderIcon = equipmentPlaceholders[slotIconMap[slotIndex]];
                        if (placeholderIcon != null) {
                            int iconX = xPos + (int) ((SLOT_SIZE - ITEM_SIZE) / 1.2);
                            int iconY = yPos + (int) ((SLOT_SIZE - ITEM_SIZE) / 1.2);
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
                            g2d.drawImage(placeholderIcon, iconX, iconY, (int) (ITEM_SIZE / 1.3), (int) (ITEM_SIZE / 1.3), null);
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                        }
                    }
                    else g2d.drawImage(item.getModel(), xPos + ITEM_OFFSET_X, yPos + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE, null);
                }
            }
        } finally {
            g2d.dispose();
        }
    }

    private void renderStatsAndQuickUsePanel(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) statsPanel.getX(), (int) statsPanel.getY(), (int) statsPanel.getWidth(), (int) statsPanel.getHeight());

        int x = (int) statsPanel.getX() + scale(10);
        int rightX = (int) (statsPanel.getX() + statsPanel.getWidth() - scale(10));
        int y = (int) statsPanel.getY() + scale(16);

        // Coins header
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g2d.setColor(INV_TEXT_LABEL);
        g2d.drawString("Coins:", x, y);

        String coinVal = String.valueOf(controller.getGameState().getPlayer().getCoins());
        g2d.setColor(INV_TEXT_VALUE);
        int coinW = g2d.getFontMetrics().stringWidth(coinVal);
        int iconSize = (int) (COIN_WID / 1.3);
        g2d.drawString(coinVal, rightX - coinW - iconSize - scale(3), y);
        g2d.drawImage(coinIcon, rightX - iconSize, y - g2d.getFontMetrics().getAscent() + 1, iconSize, iconSize, null);

        y += scale(6);
        g2d.setColor(new Color(55, 215, 210, 70));
        g2d.drawLine(x, y, rightX, y);
        y += scale(13);

        int spacing = scale(14);
        renderStatRow(g2d, "Health", InventoryBonus.getInstance().getHealth(), x, rightX, y);
        renderStatRow(g2d, "Defense", InventoryBonus.getInstance().getDefense(), x, rightX, y + spacing);
        renderStatRow(g2d, "Attack", InventoryBonus.getInstance().getAttack(), x, rightX, y + spacing * 2);
        renderStatRow(g2d, "Stamina", InventoryBonus.getInstance().getStamina(), x, rightX, y + spacing * 3);
        renderStatRow(g2d, "Critical", InventoryBonus.getInstance().getCritical(), x, rightX, y + spacing * 4);
        renderStatRow(g2d, "Spell Power", InventoryBonus.getInstance().getSpell(), x, rightX, y + spacing * 5);
        renderStatRow(g2d, "Cooldown", InventoryBonus.getInstance().getCooldown(), x, rightX, y + spacing * 6);
    }

    private void renderStatRow(Graphics2D g2d, String label, double bonusValue, int leftX, int rightX, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
        g2d.setColor(INV_TEXT_LABEL);
        g2d.drawString(label, leftX, y);

        String valStr = String.format("+%.1f%%", bonusValue * 100);
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(bonusValue > 0 ? INV_TEXT_BONUS : new Color(130, 145, 148));
        int w = g2d.getFontMetrics().stringWidth(valStr);
        g2d.drawString(valStr, rightX - w, y);
    }

    private void renderQuickUseSlots(Graphics2D g2d) {
        String[] quickSlots = controller.getGameState().getPlayer().getInventory().getQuickUseSlots();
        Inventory inv = controller.getGameState().getPlayer().getInventory();

        int barWidth = 4 * INV_QUICK_SLOT_SIZE + 3 * INV_QUICK_SLOT_SPACING;
        int pad = scale(5);
        UIButtonRenderer.drawRunicSubPanel(g2d, INV_QUICK_SLOT_X - pad, INV_QUICK_SLOT_Y - pad, barWidth + pad * 2, INV_QUICK_SLOT_SIZE + pad * 2);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        g2d.setColor(new Color(180, 210, 212));
        g2d.drawString("QUICK POTIONS (1-4)", INV_QUICK_SLOT_X - pad + scale(2), INV_QUICK_SLOT_Y - pad - scale(3));

        int iconSize = scale(19);
        for (int i = 0; i < 4; i++) {
            int sx = INV_QUICK_SLOT_X + i * (INV_QUICK_SLOT_SIZE + INV_QUICK_SLOT_SPACING);
            int sy = INV_QUICK_SLOT_Y;
            String itemId = quickSlots[i];
            Optional<InventoryItem> itemOpt = (itemId == null) ? Optional.empty() : inv.getBackpack().stream().filter(Objects::nonNull).filter(it -> it.getItemId().equals(itemId)).findFirst();

            Color rFill = itemOpt.map(it -> it.getData().rarity.getColor()).orElse(null);
            Color rBorder = itemOpt.map(it -> it.getData().rarity.getTextColor()).orElse(null);
            UIButtonRenderer.drawRunicSlot(g2d, sx, sy, INV_QUICK_SLOT_SIZE, rFill, rBorder, false);
            if (itemOpt.isPresent()) {
                InventoryItem item = itemOpt.get();
                int ix = sx + (INV_QUICK_SLOT_SIZE - iconSize) / 2;
                int iy = sy + (INV_QUICK_SLOT_SIZE - iconSize) / 2;
                g2d.drawImage(item.getModel(), ix, iy, iconSize, iconSize, null);
                UIButtonRenderer.drawStackCount(g2d, item.getAmount(), sx, sy, INV_QUICK_SLOT_SIZE);
            }

            // Keybind badge
            int badgeS = scale(9);
            g2d.setColor(new Color(14, 24, 28, 240));
            g2d.fillRoundRect(sx - 1, sy - 1, badgeS, badgeS, scale(3), scale(3));
            g2d.setColor(new Color(55, 215, 210, 160));
            g2d.drawRoundRect(sx - 1, sy - 1, badgeS, badgeS, scale(3), scale(3));
            g2d.setFont(UIButtonRenderer.getFont(6.5f * SCALE));
            String kStr = String.valueOf(i + 1);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.setColor(new Color(220, 245, 245));
            g2d.drawString(kStr, sx - 1 + (badgeS - fm.stringWidth(kStr)) / 2, sy - 1 + (badgeS + (int) (fm.getAscent() * 0.72f)) / 2);
        }
    }

    private void renderInspectorPanel(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) inspectorPanel.getX(), (int) inspectorPanel.getY(), (int) inspectorPanel.getWidth(), (int) inspectorPanel.getHeight());

        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        InventoryItem item = null;
        if (controller.isInBackpack()) {
            int absSlot = controller.getBackpackSlotNumber() + (controller.getBackpackSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
            if (absSlot < inventory.getBackpack().size()) item = inventory.getBackpack().get(absSlot);
        }
        else {
            if (controller.getEquipmentSlotNumber() < inventory.getEquipped().length) {
                item = inventory.getEquipped()[controller.getEquipmentSlotNumber()];
                if (item != null) unequipBtn.render(g2d);
            }
        }

        if (item == null || item.getData() == null) {
            g2d.setFont(new Font("Arial", Font.ITALIC, FONT_LIGHT));
            g2d.setColor(new Color(130, 150, 155));
            String emptyMsg = "Select an item to inspect its details. (Right-Click to quick Use / Equip)";
            FontMetrics fm = g2d.getFontMetrics();
            int tx = (int) (inspectorPanel.getX() + (inspectorPanel.getWidth() - fm.stringWidth(emptyMsg)) / 2);
            int ty = (int) (inspectorPanel.getY() + inspectorPanel.getHeight() / 2);
            g2d.drawString(emptyMsg, tx, ty);
            return;
        }

        ItemData data = item.getData();
        int pad = scale(8);
        int previewX = (int) inspectorPanel.getX() + pad;
        int previewY = (int) inspectorPanel.getY() + pad;
        int previewSize = scale(36);

        UIButtonRenderer.drawRunicSlot(g2d, previewX, previewY, previewSize, data.rarity.getColor(), data.rarity.getTextColor(), false);
        g2d.drawImage(item.getModel(), previewX + scale(4), previewY + scale(4), previewSize - scale(8), previewSize - scale(8), null);

        int textStartX = previewX + previewSize + scale(10);
        int titleY = previewY + scale(12);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g2d.setColor(data.rarity.getTextColor());
        g2d.drawString(data.name, textStartX, titleY);

        int nameW = g2d.getFontMetrics().stringWidth(data.name);
        String badgeLabel = (data.equip != null && data.equip.canEquip) ? data.rarity.name() + " • " + data.equip.slot.toUpperCase() : data.rarity.name();
        UIButtonRenderer.drawBadge(g2d, badgeLabel, textStartX + nameW + scale(8), titleY, data.rarity.getColor(), data.rarity.getTextColor());

        String valText = String.valueOf(data.sellValue);
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        int valW = g2d.getFontMetrics().stringWidth(valText);
        int iconS = (int) (COIN_WID / 1.4);
        int rightEdge = (int) (inspectorPanel.getX() + inspectorPanel.getWidth() - scale(10));
        g2d.setColor(INV_TEXT_VALUE);
        g2d.drawString(valText, rightEdge - valW - iconS - scale(3), titleY);
        g2d.drawImage(coinIcon, rightEdge - iconS, titleY - g2d.getFontMetrics().getAscent() + 1, iconS, iconS, null);

        // Desc
        g2d.setColor(INV_TEXT_DESC);
        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
        int descMaxW = rightEdge - textStartX;
        List<String> lines = wrapText(data.description, descMaxW, g2d.getFontMetrics());
        int descY = titleY + scale(14);
        for (int i = 0; i < Math.min(lines.size(), 3); i++) {
            g2d.drawString(lines.get(i), textStartX, descY);
            descY += g2d.getFontMetrics().getHeight();
        }

        g2d.setFont(new Font("Arial", Font.ITALIC, FONT_SMALL));
        g2d.setColor(new Color(120, 175, 180));
        String hint = item.getItemId().contains("POTION")
                ? "Tip: Press 1-4 or drag to Quick Potion bar  |  Right-Click: Drink"
                : (data.equip != null && data.equip.canEquip ? "Tip: Right-Click to Equip / Unequip  |  Shift+Drag: Split Stack" : "Tip: Shift+Drag to Split Stack");
        g2d.drawString(hint, previewX, (int) (inspectorPanel.getY() + inspectorPanel.getHeight() - scale(5)));
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(smallButtons).forEach(button -> button.render(g));
        Arrays.stream(mediumButtons).forEach(button -> button.render(g));
    }

    private void renderTooltip(Graphics g) {
        InventoryItem hoveredItem = controller.getHoveredItem();
        if (hoveredItem != null && controller.isInBackpack() && hoveredItem.getData() != null && hoveredItem.getData().equip != null && hoveredItem.getData().equip.canEquip) {
            Inventory inventory = controller.getGameState().getPlayer().getInventory();
            String slotType = hoveredItem.getData().equip.slot;
            for (Map.Entry<String, Integer> entry : equipmentSlots.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(slotType)) {
                    InventoryItem equippedItem = inventory.getEquipped()[entry.getValue()];
                    Point p = controller.getMousePosition();
                    if (equippedItem != null && controller.isMouseInBackpack(p) && !controller.getDndManager().isDragging()) {
                        if (p != null) comparisonTooltip.render(g, hoveredItem, equippedItem, p.x + 15, p.y + 15, overlay.getBounds());
                    }
                    break;
                }
            }
        }
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
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

    public void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> button.getButtonHitBox().contains(e.getPoint()))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    public void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));
        Arrays.stream(buttons)
                .filter(button -> button.getButtonHitBox().contains(e.getPoint()))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        controller.mouseDragged(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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

    // Getters
    public InventoryViewController getController() {
        return controller;
    }

    public Rectangle2D getOverlay() {
        return overlay;
    }

    public MediumButton[] getMediumButtons() {
        return mediumButtons;
    }

    public MediumButton getUnequipBtn() {
        return unequipBtn;
    }

    public SmallButton[] getSmallButtons() {
        return smallButtons;
    }
}