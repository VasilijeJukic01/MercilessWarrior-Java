package platformer.ui.overlays.controller;

import platformer.model.inventory.Inventory;
import platformer.model.inventory.item.InventoryItem;
import platformer.state.types.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.overlays.InventoryOverlay;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Handles the logic for the inventory overlay in the game.
 * It manages user interactions to navigate through the player's inventory and equipment slots.
 */
public class InventoryViewController {

    private final GameState gameState;
    private final InventoryOverlay inventoryOverlay;

    private int backpackSlot = 0;
    private int backpackSlotNumber = 0;
    private int equipmentSlotNumber = 0;
    private boolean isInBackpack = true;

    private InventoryItem hoveredItem = null;
    private Point mousePosition;

    public InventoryViewController(GameState gameState, InventoryOverlay inventoryOverlay) {
        this.gameState = gameState;
        this.inventoryOverlay = inventoryOverlay;
    }

    // Event Handlers
    public void mousePressed(MouseEvent e) {
        inventoryOverlay.setMousePressed(e, inventoryOverlay.getSmallButtons());
        inventoryOverlay.setMousePressed(e, inventoryOverlay.getMediumButtons());
        inventoryOverlay.setMousePressed(e, new AbstractButton[]{inventoryOverlay.getUnequipBtn()});
        changeSlot(e);
    }

    public void mouseReleased(MouseEvent e) {
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        releaseUnequipButton(e);
        Arrays.stream(inventoryOverlay.getSmallButtons()).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(inventoryOverlay.getMediumButtons()).forEach(AbstractButton::resetMouseSet);
        inventoryOverlay.getUnequipBtn().resetMouseSet();
    }

    public void mouseMoved(MouseEvent e) {
        inventoryOverlay.setMouseMoved(e, inventoryOverlay.getSmallButtons());
        inventoryOverlay.setMouseMoved(e, inventoryOverlay.getMediumButtons());
        inventoryOverlay.setMouseMoved(e, new AbstractButton[]{inventoryOverlay.getUnequipBtn()});
        updateHoveredItem(e);
        this.mousePosition = e.getPoint();
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1 -> assignToQuickSlot(0);
            case KeyEvent.VK_2 -> assignToQuickSlot(1);
            case KeyEvent.VK_3 -> assignToQuickSlot(2);
            case KeyEvent.VK_4 -> assignToQuickSlot(3);
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> moveUp();
            case KeyEvent.VK_DOWN -> moveDown();
            case KeyEvent.VK_LEFT -> moveLeft();
            case KeyEvent.VK_RIGHT -> moveRight();
            case KeyEvent.VK_X -> useItem();
        }
    }

    // Actions
    private void updateHoveredItem(MouseEvent e) {
        hoveredItem = null;
        if (!isInBackpack) return; // Only show tooltip for backpack items

        Inventory inventory = gameState.getPlayer().getInventory();
        int slot = getSlotAt(e.getPoint());

        if (slot != -1) {
            int absoluteIndex = slot + (backpackSlot * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
            if (absoluteIndex < inventory.getBackpack().size()) {
                hoveredItem = inventory.getBackpack().get(absoluteIndex);
            }
        }
    }

    private int getSlotAt(Point p) {
        for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
            for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
                Rectangle slotBounds = new Rectangle(
                        i * SLOT_SPACING + BACKPACK_SLOT_X,
                        j * SLOT_SPACING + BACKPACK_SLOT_Y,
                        SLOT_SIZE, SLOT_SIZE);
                if (slotBounds.contains(p)) {
                    return i + (j * INVENTORY_SLOT_MAX_ROW);
                }
            }
        }
        return -1;
    }

    private void releaseSmallButtons(MouseEvent e) {
        for (AbstractButton button : inventoryOverlay.getSmallButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (button.getButtonType() == ButtonType.PREV) prevBackpackSlot();
                else if (button.getButtonType() == ButtonType.NEXT) nextBackpackSlot();
                break;
            }
        }
    }

    private void releaseMediumButtons(MouseEvent e) {
        for (AbstractButton button : inventoryOverlay.getMediumButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                Inventory inventory = gameState.getPlayer().getInventory();
                switch (button.getButtonType()) {
                    case USE -> inventory.useItem(getAbsoluteBackpackSlot(), gameState.getPlayer());
                    case EQUIP -> inventory.equipItem(getAbsoluteBackpackSlot());
                    case DROP -> inventory.dropItem(getAbsoluteBackpackSlot());
                }
            }
        }
    }

    private void releaseUnequipButton(MouseEvent e) {
        if (isMouseInButton(e, inventoryOverlay.getUnequipBtn()) && inventoryOverlay.getUnequipBtn().isMousePressed()) {
            Inventory inventory = gameState.getPlayer().getInventory();
            inventory.unequipItem(equipmentSlotNumber);
        }
    }

    private void changeSlot(MouseEvent e) {
        if (checkBackpackSelection(e)) return;
        checkEquipmentSelection(e);
    }

    private boolean checkBackpackSelection(MouseEvent e) {
        for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
            for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
                Rectangle slotBounds = new Rectangle(i * SLOT_SPACING + BACKPACK_SLOT_X, j * SLOT_SPACING + BACKPACK_SLOT_Y, SLOT_SIZE, SLOT_SIZE);
                if (slotBounds.contains(e.getPoint())) {
                    backpackSlotNumber = i + (j * INVENTORY_SLOT_MAX_ROW);
                    inventoryOverlay.updateSelectedSlot(true);
                    isInBackpack = true;
                    return true;
                }
            }
        }
        return false;
    }

    private void checkEquipmentSelection(MouseEvent e) {
        for (int j = 0; j < EQUIPMENT_SLOT_MAX_COL; j++) {
            for (int i = 0; i < EQUIPMENT_SLOT_MAX_ROW; i++) {
                Rectangle slotBounds = new Rectangle(i * EQUIPMENT_SLOT_SPACING + EQUIPMENT_SLOT_X, j * SLOT_SPACING + EQUIPMENT_SLOT_Y, SLOT_SIZE, SLOT_SIZE);
                if (slotBounds.contains(e.getPoint())) {
                    equipmentSlotNumber = i + (j * EQUIPMENT_SLOT_MAX_ROW);
                    inventoryOverlay.updateSelectedSlot(false);
                    isInBackpack = false;
                    break;
                }
            }
        }
    }

    private void moveUp() {
        if (isInBackpack) backpackSlotNumber = Math.max(0, backpackSlotNumber - INVENTORY_SLOT_MAX_ROW);
        else equipmentSlotNumber = Math.max(0, equipmentSlotNumber - EQUIPMENT_SLOT_MAX_ROW);
        inventoryOverlay.updateSelectedSlot(isInBackpack);
    }

    private void moveDown() {
        if (isInBackpack) backpackSlotNumber = Math.min(INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL - 1, backpackSlotNumber + INVENTORY_SLOT_MAX_ROW);
        else equipmentSlotNumber = Math.min(EQUIPMENT_SLOT_MAX_ROW * EQUIPMENT_SLOT_MAX_COL - 1, equipmentSlotNumber + EQUIPMENT_SLOT_MAX_ROW);
        inventoryOverlay.updateSelectedSlot(isInBackpack);
    }

    private void moveLeft() {
        if (isInBackpack) backpackSlotNumber = Math.max(0, backpackSlotNumber - 1);
        else equipmentSlotNumber = Math.max(0, equipmentSlotNumber - 1);
        inventoryOverlay.updateSelectedSlot(isInBackpack);
    }

    private void moveRight() {
        if (isInBackpack) backpackSlotNumber = Math.min(INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL - 1, backpackSlotNumber + 1);
        else equipmentSlotNumber = Math.min(EQUIPMENT_SLOT_MAX_ROW * EQUIPMENT_SLOT_MAX_COL - 1, equipmentSlotNumber + 1);
        inventoryOverlay.updateSelectedSlot(isInBackpack);
    }

    private void useItem() {
        gameState.getPlayer().getInventory().useItem(getAbsoluteBackpackSlot(), gameState.getPlayer());
    }

    private void assignToQuickSlot(int slotIndex) {
        if (!isInBackpack) return;
        int absoluteBackpackIndex = getAbsoluteBackpackSlot();
        gameState.getPlayer().getInventory().assignToQuickSlot(absoluteBackpackIndex, slotIndex);
    }

    private void prevBackpackSlot() {
        this.backpackSlot = Math.max(backpackSlot - 1, 0);
    }

    private void nextBackpackSlot() {
        this.backpackSlot = Math.min(backpackSlot + 1, INVENTORY_SLOT_CAP);
    }

    private int getAbsoluteBackpackSlot() {
        return backpackSlotNumber + (backpackSlot * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getPoint());
    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public int getBackpackSlot() {
        return backpackSlot;
    }

    public int getBackpackSlotNumber() {
        return backpackSlotNumber;
    }

    public int getEquipmentSlotNumber() {
        return equipmentSlotNumber;
    }

    public boolean isInBackpack() {
        return isInBackpack;
    }

    public InventoryItem getHoveredItem() {
        return hoveredItem;
    }

    public Point getMousePosition() {
        return mousePosition;
    }

    public void reset() {

    }
}