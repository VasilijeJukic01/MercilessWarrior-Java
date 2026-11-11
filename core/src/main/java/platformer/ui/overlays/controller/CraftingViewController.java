package platformer.ui.overlays.controller;

import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.inventory.craft.Recipe;
import platformer.model.inventory.craft.RecipeManager;
import platformer.model.inventory.item.InventoryItem;
import platformer.state.types.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.overlays.CraftingOverlay;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Handles the logic for the Alchemy & Material Crafting Table overlay.
 */
public class CraftingViewController {

    private final GameState gameState;
    private final CraftingOverlay craftingOverlay;

    private int craftingSlot = 0;
    private int slotNumber = 0;

    public CraftingViewController(GameState gameState, CraftingOverlay craftingOverlay) {
        this.gameState = gameState;
        this.craftingOverlay = craftingOverlay;
    }

    // Event Handlers
    public void mousePressed(MouseEvent e) {
        setMousePressed(e, craftingOverlay.getSmallButtons());
        setMousePressed(e, craftingOverlay.getMediumButtons());
        if (changeSlot(e) && SwingUtilities.isRightMouseButton(e)) {
            craftItem();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) return;
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(craftingOverlay.getSmallButtons()).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(craftingOverlay.getMediumButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, craftingOverlay.getSmallButtons());
        setMouseMoved(e, craftingOverlay.getMediumButtons());
    }

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> moveUp();
            case KeyEvent.VK_DOWN -> moveDown();
            case KeyEvent.VK_LEFT -> moveLeft();
            case KeyEvent.VK_RIGHT -> moveRight();
            case KeyEvent.VK_C, KeyEvent.VK_ENTER -> craftItem();
        }
    }

    private void releaseSmallButtons(MouseEvent e) {
        for (AbstractButton button : craftingOverlay.getSmallButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (button.getButtonType() == ButtonType.PREV) prevCraftingSlot();
                else if (button.getButtonType() == ButtonType.NEXT) nextCraftingSlot();
                break;
            }
        }
    }

    private void releaseMediumButtons(MouseEvent e) {
        for (AbstractButton button : craftingOverlay.getMediumButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (button.getButtonType() == ButtonType.CRAFT) {
                    craftItem();
                } else if (button.getButtonType() == ButtonType.CLOSE) {
                    EventBus.getInstance().publish(new OverlayChangeEvent(null));
                }
            }
        }
    }

    private void craftItem() {
        List<Recipe> recipes = RecipeManager.getInstance().getRecipes();
        int absSlot = slotNumber + (craftingSlot * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
        if (absSlot >= recipes.size()) return;
        Recipe recipe = recipes.get(absSlot);
        if (recipe == null) return;
        InventoryItem itemToCraft = new InventoryItem(recipe.getOutput(), recipe.getAmount());
        gameState.getPlayer().getInventory().craftItem(itemToCraft, recipe.getIngredients());
    }

    private boolean changeSlot(MouseEvent e) {
        for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
            for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
                int xStart = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int xEnd = xStart + SLOT_SIZE;
                int yStart = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                int yEnd = yStart + SLOT_SIZE;

                if (e.getX() >= xStart && e.getX() <= xEnd && e.getY() >= yStart && e.getY() <= yEnd) {
                    slotNumber = i + (j * INVENTORY_SLOT_MAX_ROW);
                    craftingOverlay.updateSelectedSlot();
                    return true;
                }
            }
        }
        return false;
    }

    private void moveUp() {
        slotNumber = Math.max(0, slotNumber - INVENTORY_SLOT_MAX_ROW);
        craftingOverlay.updateSelectedSlot();
    }

    private void moveDown() {
        int maxSlot = INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL - 1;
        slotNumber = Math.min(maxSlot, slotNumber + INVENTORY_SLOT_MAX_ROW);
        craftingOverlay.updateSelectedSlot();
    }

    private void moveLeft() {
        slotNumber = Math.max(0, slotNumber - 1);
        craftingOverlay.updateSelectedSlot();
    }

    private void moveRight() {
        int maxSlot = INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL - 1;
        slotNumber = Math.min(maxSlot, slotNumber + 1);
        craftingOverlay.updateSelectedSlot();
    }

    private void prevCraftingSlot() {
        this.craftingSlot = Math.max(craftingSlot - 1, 0);
    }

    private void nextCraftingSlot() {
        int maxPages = Math.max(1, (int) Math.ceil((double) RecipeManager.getInstance().getRecipes().size() / (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL)));
        this.craftingSlot = Math.min(craftingSlot + 1, maxPages - 1);
    }

    // Helpers
    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).filter(button -> isMouseInButton(e, button)).findFirst().ifPresent(button -> button.setMousePressed(true));
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));
        Arrays.stream(buttons).filter(button -> isMouseInButton(e, button)).findFirst().ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getPoint());
    }

    public int getCraftingSlot() {
        return craftingSlot;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public GameState getGameState() {
        return gameState;
    }
}