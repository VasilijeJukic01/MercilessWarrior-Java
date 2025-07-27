package platformer.ui.overlays;

import platformer.animation.Anim;
import platformer.animation.Animation;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryBonus;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemData;
import platformer.state.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.SmallButton;
import platformer.ui.coponents.ItemComparisonTooltip;
import platformer.ui.overlays.controller.InventoryViewController;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static platformer.constants.AnimConstants.COIN_H;
import static platformer.constants.AnimConstants.COIN_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * Class that represents the overlay that is displayed when the player opens the inventory.
 */
public class InventoryOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final InventoryViewController controller;
    private final ItemComparisonTooltip comparisonTooltip;
    private final Map<String, Integer> equipmentSlots;

    private Rectangle2D overlay;
    private BufferedImage inventoryText;
    private final MediumButton[] mediumButtons;
    private MediumButton unequipBtn;
    private final SmallButton[] smallButtons;

    private Rectangle2D backpackPanel, equipPanel;
    private BufferedImage slotImage, coinIcon;
    private BufferedImage[] playerAnim;
    private Rectangle2D.Double selectedSlot;

    private int playerAnimTick, playerAnimIndex;
    private final int playerAnimSpeed = 20;

    public InventoryOverlay(GameState gameState) {
        this.controller = new InventoryViewController(gameState, this);
        this.mediumButtons = new MediumButton[3];
        this.smallButtons = new SmallButton[2];
        this.comparisonTooltip = new ItemComparisonTooltip((int)(160*SCALE), (int)(120*SCALE));
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
        this.inventoryText = Utils.getInstance().importImage(INVENTORY_TXT, INV_TEXT_WID, INV_TEXT_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_INVENTORY, SLOT_SIZE, SLOT_SIZE);
        this.playerAnim = Animation.getInstance().loadPlayerAnimations(INV_PLAYER_WID, INV_PLAYER_HEI, PLAYER_TRANSFORM_SHEET)[Anim.IDLE.ordinal()];
        this.coinIcon = Animation.getInstance().loadFromSprite(COIN_SHEET, 1, 1, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H)[0];
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
        updatePlayerAnimation();
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
        unequipBtn.update();
    }

    private void updatePlayerAnimation() {
        playerAnimTick++;
        if (playerAnimTick >= playerAnimSpeed) {
            playerAnimTick = 0;
            playerAnimIndex++;
            if (playerAnimIndex >= playerAnim.length) {
                playerAnimIndex = 0;
            }
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        Graphics2D g2d = (Graphics2D) g;
        renderOverlay(g2d);
        renderBackpackPanel(g2d);
        renderBackpackSlots(g);
        renderEquipmentPanel(g2d);
        renderEquipmentSlots(g);
        renderBonusInfo(g);
        g.setColor(Color.RED);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
        renderButtons(g);
        renderTooltip(g);
    }

    private void renderOverlay(Graphics2D g2d) {
        g2d.setColor(OVERLAY_COLOR);
        g2d.fill(overlay);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(overlay);
        g2d.drawImage(inventoryText, INV_TEXT_X, INV_TEXT_Y, inventoryText.getWidth(), inventoryText.getHeight(), null);
    }

    private void renderBackpackPanel(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(backpackPanel);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(backpackPanel);
    }

    private void renderBackpackSlots(Graphics g) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        int pageOffset = controller.getBackpackSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL);
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xPos = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int yPos = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
                int slotNumber = (j * INVENTORY_SLOT_MAX_ROW + i) + pageOffset;
                if (slotNumber < inventory.getBackpack().size()) renderBackpackItem(g, inventory, slotNumber);
            }
        }
        renderItemInfoBackpack(g);
    }

    private void renderBackpackItem(Graphics g, Inventory inventory, int absoluteSlotNumber) {
        InventoryItem item = inventory.getBackpack().get(absoluteSlotNumber);
        ItemData itemData = item.getData();
        if (itemData == null) return;
        int relativeSlotNumber = absoluteSlotNumber - (controller.getBackpackSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
        int xPos = (relativeSlotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X + ITEM_OFFSET_X;
        int yPos = (relativeSlotNumber / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y + ITEM_OFFSET_Y;
        g.setColor(itemData.rarity.getColor());
        g.fillRect(xPos - (int)(ITEM_OFFSET_X / 1.1), yPos - (int)(ITEM_OFFSET_Y / 1.1), (int)(SLOT_SIZE / 1.06), (int)(SLOT_SIZE / 1.06));
        g.drawImage(item.getModel(), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
        g.setColor(INV_TEXT_DEFAULT);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(String.valueOf(item.getAmount()), xPos + ITEM_COUNT_OFFSET_X, yPos + ITEM_COUNT_OFFSET_Y);
    }

    private void renderEquipmentPanel(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(equipPanel);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(equipPanel);
        g2d.drawImage(playerAnim[playerAnimIndex], INV_PLAYER_X, INV_PLAYER_Y, INV_PLAYER_WID, INV_PLAYER_HEI, null);
    }

    private void renderEquipmentSlots(Graphics g) {
        for (int i = 0; i < EQUIPMENT_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < EQUIPMENT_SLOT_MAX_COL; j++) {
                int xPos = i * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X;
                int yPos = j * SLOT_SPACING + EQUIPMENT_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
                renderEquipmentItem(g, i, j);
            }
        }
        renderItemInfoEquipment(g);
    }

    private void renderEquipmentItem(Graphics g, int i, int j) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        int slotIndex = j * EQUIPMENT_SLOT_MAX_ROW + i;
        if (inventory.getEquipped()[slotIndex] != null) {
            InventoryItem item = inventory.getEquipped()[slotIndex];
            ItemData itemData = item.getData();
            if (itemData == null) return;
            int xPos = (slotIndex % EQUIPMENT_SLOT_MAX_ROW) * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X + ITEM_OFFSET_X;
            int yPos = (slotIndex / EQUIPMENT_SLOT_MAX_ROW) * SLOT_SPACING + EQUIPMENT_SLOT_Y + ITEM_OFFSET_Y;
            g.setColor(itemData.rarity.getColor());
            g.fillRect(xPos - (int)(ITEM_OFFSET_X / 1.1), yPos - (int)(ITEM_OFFSET_Y / 1.1), (int)(SLOT_SIZE / 1.06), (int)(SLOT_SIZE / 1.06));
            g.drawImage(item.getModel(), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
        }
    }

    private void renderItemInfoBackpack(Graphics g) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        int absoluteSlotNumber = controller.getBackpackSlotNumber() + (controller.getBackpackSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
        if (absoluteSlotNumber >= inventory.getBackpack().size()) return;
        InventoryItem item = inventory.getBackpack().get(absoluteSlotNumber);
        if (item != null && controller.isInBackpack()) renderItemDescription(g, item);
    }

    private void renderItemInfoEquipment(Graphics g) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        if (controller.getEquipmentSlotNumber() >= inventory.getEquipped().length) return;
        InventoryItem item = inventory.getEquipped()[controller.getEquipmentSlotNumber()];
        if (item != null && !controller.isInBackpack()) {
            renderItemDescription(g, item);
            unequipBtn.render(g);
        }
    }

    private void renderItemDescription(Graphics g, InventoryItem item) {
        ItemData itemData = item.getData();
        if (itemData == null) return;
        g.setColor(itemData.rarity.getTextColor());
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(itemData.name, INV_ITEM_NAME_X, INV_ITEM_NAME_Y);
        g.setColor(INV_TEXT_LABEL);
        g.drawString("Value: ", INV_ITEM_VALUE_X, INV_ITEM_VALUE_Y);
        g.setColor(INV_TEXT_VALUE);
        String valueText = String.valueOf(itemData.sellValue);
        g.drawString(valueText, INV_ITEM_VALUE_X + g.getFontMetrics().stringWidth("Value: "), INV_ITEM_VALUE_Y);
        g.drawImage(coinIcon, INV_ITEM_VALUE_X + g.getFontMetrics().stringWidth("Value: " + valueText) + 2, INV_ITEM_VALUE_Y - g.getFontMetrics().getAscent() + 1, (int)(COIN_WID/1.5), (int)(COIN_HEI/1.5), null);

        g.setColor(INV_TEXT_DESC);
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        int descriptionMaxWidth = (int) (overlay.getX() + overlay.getWidth() - INV_ITEM_DESC_X - (10 * SCALE));
        List<String> wrappedLines = wrapText(itemData.description, descriptionMaxWidth, g.getFontMetrics());
        int lineHeight = g.getFontMetrics().getHeight();
        int y = INV_ITEM_DESC_Y;
        for (String line : wrappedLines) {
            g.drawString(line, INV_ITEM_DESC_X, y);
            y += lineHeight;
        }
    }

    private void renderBonusInfo(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.setColor(INV_TEXT_COINS);
        String coinText = "Coins: ";
        g.drawString(coinText, INV_BONUS_X, INV_BONUS_Y);
        g.setColor(INV_TEXT_VALUE);
        String coinValue = String.valueOf(controller.getGameState().getPlayer().getCoins());
        int coinTextWidth = g.getFontMetrics().stringWidth(coinText);
        g.drawString(coinValue, INV_BONUS_X + coinTextWidth, INV_BONUS_Y);
        g.drawImage(coinIcon, INV_BONUS_X + coinTextWidth + g.getFontMetrics().stringWidth(coinValue) + 2, INV_BONUS_Y - g.getFontMetrics().getAscent() + 1, (int)(COIN_WID/1.5), (int)(COIN_HEI/1.5), null);

        g.setColor(INV_TEXT_HEADER);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("Active bonuses: ", INV_BONUS_X, INV_BONUS_Y + 2 * INV_BONUS_SPACING);

        renderBonusLine(g, "Health Bonus: ", InventoryBonus.getInstance().getHealth(), INV_BONUS_Y + 3 * INV_BONUS_SPACING);
        renderBonusLine(g, "Defense Bonus: ", InventoryBonus.getInstance().getDefense(), INV_BONUS_Y + 4 * INV_BONUS_SPACING);
        renderBonusLine(g, "Attack Bonus: ", InventoryBonus.getInstance().getAttack(), INV_BONUS_Y + 5 * INV_BONUS_SPACING);
        renderBonusLine(g, "Stamina Bonus: ", InventoryBonus.getInstance().getStamina(), INV_BONUS_Y + 6 * INV_BONUS_SPACING);
        renderBonusLine(g, "Critical Bonus: ", InventoryBonus.getInstance().getCritical(), INV_BONUS_Y + 7 * INV_BONUS_SPACING);
        renderBonusLine(g, "Spell Bonus: ", InventoryBonus.getInstance().getSpell(), INV_BONUS_Y + 8 * INV_BONUS_SPACING);
        renderBonusLine(g, "Cooldown Bonus: ", InventoryBonus.getInstance().getCooldown(), INV_BONUS_Y +  9 * INV_BONUS_SPACING);
    }

    private void renderBonusLine(Graphics g, String label, double bonusValue, int y) {
        g.setColor(INV_TEXT_LABEL);
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        g.drawString(label, INV_BONUS_X, y);
        g.setColor(INV_TEXT_BONUS);
        g.drawString(String.format("+%.1f%%", bonusValue * 100), INV_BONUS_X + g.getFontMetrics().stringWidth(label), y);
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(smallButtons).forEach(button -> button.render(g));
        Arrays.stream(mediumButtons).forEach(button -> button.render(g));
    }

    private void renderTooltip(Graphics g) {
        InventoryItem hoveredItem = controller.getHoveredItem();
        if (hoveredItem != null && controller.isInBackpack() && hoveredItem.getData().equip.canEquip) {
            Inventory inventory = controller.getGameState().getPlayer().getInventory();
            String slotType = hoveredItem.getData().equip.slot;
            for (Map.Entry<String, Integer> entry : equipmentSlots.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(slotType)) {
                    InventoryItem equippedItem = inventory.getEquipped()[entry.getValue()];
                    if (equippedItem != null) {
                        Point p = controller.getMousePosition();
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