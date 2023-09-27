package platformer.ui.overlays;

import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryBonus;
import platformer.model.inventory.InventoryItem;
import platformer.state.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.SmallButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

public class InventoryOverlay implements Overlay {

    private final GameState gameState;

    private Rectangle2D overlay;
    private BufferedImage inventoryText;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;

    private Rectangle2D backpackPanel, equipPanel;
    private BufferedImage playerImage, slotImage;
    private Rectangle2D.Double selectedSlot;
    private int backpackSlot;
    private int backpackSlotNumber, equipmentSlotNumber;
    private boolean isInBackpack = true;

    public InventoryOverlay(GameState gameState) {
        this.gameState = gameState;
        this.mediumButtons = new MediumButton[3];
        this.smallButtons = new SmallButton[2];
        init();
    }

    // Init
    private void init() {
        loadImages();
        loadButtons();
        initSelectedSlot();
    }

    private void loadImages() {
        this.overlay = new Rectangle2D.Double(INV_OVERLAY_X, INV_OVERLAY_Y, INV_OVERLAY_WID, INV_OVERLAY_HEI);
        this.backpackPanel = new Rectangle2D.Double(BACKPACK_X, BACKPACK_Y, BACKPACK_WID, BACKPACK_HEI);
        this.equipPanel = new Rectangle2D.Double(EQUIPMENT_X, EQUIPMENT_Y, EQUIPMENT_WID, EQUIPMENT_HEI);
        this.inventoryText = Utils.getInstance().importImage(INVENTORY_TXT, INV_TEXT_WID, INV_TEXT_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_INVENTORY, SLOT_SIZE, SLOT_SIZE);
        this.playerImage = Utils.getInstance().importImage(PLAYER_ICON, -1, -1);
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(PREV_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(NEXT_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);

        mediumButtons[0] = new MediumButton(USE_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.USE);
        mediumButtons[1] = new MediumButton(EQUIP_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.EQUIP);
        mediumButtons[2] = new MediumButton(DROP_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.DROP);
    }

    private void initSelectedSlot() {
        int xPos = (backpackSlotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
        int yPos = (backpackSlotNumber / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    // Selection
    private void setSelectedSlotBackpack() {
        this.selectedSlot.x = (backpackSlotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
        this.selectedSlot.y = (backpackSlotNumber / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y;
    }

    private void setSelectedSlotEquipment() {
        this.selectedSlot.x = (equipmentSlotNumber % EQUIPMENT_SLOT_MAX_ROW) * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X;
        this.selectedSlot.y = (equipmentSlotNumber / EQUIPMENT_SLOT_MAX_ROW) * SLOT_SPACING + EQUIPMENT_SLOT_Y;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        checkBackpackSelection(x, y);
        checkEquipmentSelection(x, y);
    }

    private void checkBackpackSelection(int x, int y) {
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xStart = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int xEnd = i * SLOT_SPACING + BACKPACK_SLOT_X + SLOT_SIZE;
                int yStart = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                int yEnd = j * SLOT_SPACING + BACKPACK_SLOT_Y + SLOT_SIZE;

                if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                    backpackSlotNumber = i + (j * INVENTORY_SLOT_MAX_ROW);
                    setSelectedSlotBackpack();
                    isInBackpack = true;
                    break;
                }
            }
        }
    }

    private void checkEquipmentSelection(int x, int y) {
        for (int i = 0; i < EQUIPMENT_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < EQUIPMENT_SLOT_MAX_COL; j++) {
                int xStart = i * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X;
                int xEnd = i * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X + SLOT_SIZE;
                int yStart = j * SLOT_SPACING + EQUIPMENT_SLOT_Y;
                int yEnd = j * SLOT_SPACING + EQUIPMENT_SLOT_Y + SLOT_SIZE;

                if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                    equipmentSlotNumber = i + (j * EQUIPMENT_SLOT_MAX_ROW);
                    setSelectedSlotEquipment();
                    isInBackpack = false;
                    break;
                }
            }
        }
    }

    // Core
    @Override
    public void render(Graphics g) {
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
    }

    // Render
    private void renderButtons(Graphics g) {
        Arrays.stream(smallButtons).forEach(button -> button.render(g));
        Arrays.stream(mediumButtons).forEach(button -> button.render(g));
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
        Inventory inventory = gameState.getPlayer().getInventory();
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xPos = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int yPos = j * SLOT_SPACING + BACKPACK_SLOT_Y;

                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
                int slotNumber = (j * INVENTORY_SLOT_MAX_ROW + i) + backpackSlot * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL);
                if (slotNumber < inventory.getBackpack().size())
                    renderBackpackItem(g, inventory, slotNumber);
            }
        }
        renderItemInfoBackpack(g);
    }

    private void renderBackpackItem(Graphics g, Inventory inventory, int slotNumber) {
        InventoryItem item = inventory.getBackpack().get(slotNumber);
        int xPos = (slotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X + ITEM_OFFSET_X;
        int yPos = (slotNumber / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y + ITEM_OFFSET_Y;
        g.setColor(item.getItemType().getRarity().getColor());
        g.fillRect(xPos-(int)(ITEM_OFFSET_X/1.1), yPos-(int)(ITEM_OFFSET_Y/1.1), (int)(SLOT_SIZE/1.06), (int)(SLOT_SIZE/1.06));
        g.drawImage(item.getModel(), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        int countX = xPos + ITEM_COUNT_OFFSET_X, countY =  yPos + ITEM_COUNT_OFFSET_Y;
        g.drawString(String.valueOf(item.getAmount()), countX, countY);
    }

    private void renderEquipmentPanel(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(equipPanel);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(equipPanel);
        g2d.drawImage(playerImage, INV_PLAYER_X, INV_PLAYER_Y, INV_PLAYER_WID, INV_PLAYER_HEI, null);
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
        Inventory inventory = gameState.getPlayer().getInventory();
        if (inventory.getEquipped()[j * EQUIPMENT_SLOT_MAX_ROW + i] != null) {
            InventoryItem item = inventory.getEquipped()[j * EQUIPMENT_SLOT_MAX_ROW + i];
            int slot = (j * EQUIPMENT_SLOT_MAX_ROW + i);
            int xPos = (slot % EQUIPMENT_SLOT_MAX_ROW) * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X + ITEM_OFFSET_X;
            int yPos = (slot / EQUIPMENT_SLOT_MAX_ROW) * SLOT_SPACING + EQUIPMENT_SLOT_Y + ITEM_OFFSET_Y;
            g.setColor(item.getItemType().getRarity().getColor());
            g.fillRect(xPos-(int)(ITEM_OFFSET_X/1.1), yPos-(int)(ITEM_OFFSET_Y/1.1), (int)(SLOT_SIZE/1.06), (int)(SLOT_SIZE/1.06));
            g.drawImage(item.getModel(), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
        }
    }

    private void renderItemInfoBackpack(Graphics g) {
        Inventory inventory = gameState.getPlayer().getInventory();
        if (backpackSlotNumber >= inventory.getBackpack().size()) return;
        InventoryItem item = inventory.getBackpack().get(backpackSlotNumber);
        if (item != null && isInBackpack) renderItemDescription(g, item);
    }

    private void renderItemInfoEquipment(Graphics g) {
        Inventory inventory = gameState.getPlayer().getInventory();
        if (equipmentSlotNumber >= inventory.getEquipped().length) return;
        InventoryItem item = inventory.getEquipped()[equipmentSlotNumber];
        if (item != null && !isInBackpack) renderItemDescription(g, item);
    }

    private void renderItemDescription(Graphics g, InventoryItem item) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(item.getItemType().getName(), INV_ITEM_NAME_X, INV_ITEM_NAME_Y);
        g.drawString("Value: " + item.getItemType().getSellValue(), INV_ITEM_VALUE_X, INV_ITEM_VALUE_Y);
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        String[] lines = item.getItemType().getDescription().split("\n");
        int lineHeight = g.getFontMetrics().getHeight();
        int y = INV_ITEM_DESC_Y;
        for (String line : lines) {
            g.drawString(line, INV_ITEM_DESC_X, y);
            y += lineHeight;
        }
    }

    private void renderBonusInfo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        g.drawString("Active bonuses: ", INV_BONUS_X, INV_BONUS_Y);
        g.drawString("Health Bonus: +"+InventoryBonus.getInstance().getHealth()+"%", INV_BONUS_X, INV_BONUS_Y + INV_BONUS_SPACING);
        g.drawString("Defense Bonus: +"+InventoryBonus.getInstance().getDefense()+"%", INV_BONUS_X, INV_BONUS_Y + 2 * INV_BONUS_SPACING);
        g.drawString("Attack Bonus: +"+InventoryBonus.getInstance().getAttack()+"%", INV_BONUS_X, INV_BONUS_Y + 3 * INV_BONUS_SPACING);
        g.drawString("Stamina Bonus: +"+InventoryBonus.getInstance().getStamina()+"%", INV_BONUS_X, INV_BONUS_Y + 4 * INV_BONUS_SPACING);
        g.drawString("Critical Bonus: +"+InventoryBonus.getInstance().getCritical()+"%", INV_BONUS_X, INV_BONUS_Y + 5 * INV_BONUS_SPACING);
        g.drawString("Spell Bonus: +"+InventoryBonus.getInstance().getSpell()+"%", INV_BONUS_X, INV_BONUS_Y + 6 * INV_BONUS_SPACING);
        g.drawString("Cooldown Bonus: +"+InventoryBonus.getInstance().getCooldown()+"%", INV_BONUS_X, INV_BONUS_Y +  7 * INV_BONUS_SPACING);
    }

    // Actions
    private void prevBackpackSlot() {
        this.backpackSlot = Math.max(backpackSlot-1, 0);
    }

    private void nextBackpackSlot() {
        this.backpackSlot = Math.min(backpackSlot+1, 5);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        setMousePressed(e, smallButtons);
        setMousePressed(e, mediumButtons);
        changeSlot(e);
    }

    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(smallButtons).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(mediumButtons).forEach(AbstractButton::resetMouseSet);
    }

    private void releaseSmallButtons(MouseEvent e) {
        for (SmallButton button : smallButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case PREV:
                        prevBackpackSlot();
                        break;
                    case NEXT:
                        nextBackpackSlot();
                        break;
                    default: break;
                }
                break;
            }
        }
    }

    private void releaseMediumButtons(MouseEvent e) {
        for (MediumButton button : mediumButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                Inventory inventory = gameState.getPlayer().getInventory();
                switch (button.getButtonType()) {
                    case USE:
                        inventory.useItem(backpackSlotNumber); break;
                    case EQUIP:
                        inventory.equipItem(backpackSlotNumber); break;
                    case DROP:
                        inventory.dropItem(backpackSlotNumber); break;
                    default: break;
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, smallButtons);
        setMouseMoved(e, mediumButtons);
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    @Override
    public void update() {
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void reset() {

    }

}
