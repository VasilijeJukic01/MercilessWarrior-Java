package platformer.ui.overlays.controller;

import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.Recipe;
import platformer.model.inventory.RecipeManager;
import platformer.state.types.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.overlays.CraftingOverlay;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

//Javadoc this

/**
 * Handles the logic for the crafting overlay in the game.
 * It manages user interactions to navigate through the crafting recipes and perform crafting actions.
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
        changeSlot(e);
    }

    public void mouseReleased(MouseEvent e) {
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(craftingOverlay.getSmallButtons()).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(craftingOverlay.getMediumButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, craftingOverlay.getSmallButtons());
        setMouseMoved(e, craftingOverlay.getMediumButtons());
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> moveUp();
            case KeyEvent.VK_DOWN -> moveDown();
            case KeyEvent.VK_LEFT -> moveLeft();
            case KeyEvent.VK_RIGHT -> moveRight();
            case KeyEvent.VK_C -> craftItem();
        }
    }

    // Actions
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
                if (Objects.requireNonNull(button.getButtonType()) == ButtonType.CRAFT) {
                    craftItem();
                }
            }
        }
    }

    private void craftItem() {
        List<Recipe> recipes = RecipeManager.getInstance().getRecipes();
        if (slotNumber >= recipes.size()) return;
        Recipe recipe = recipes.get(slotNumber);
        if (recipe == null) return;
        InventoryItem itemToCraft = new InventoryItem(recipe.getOutput(), recipe.getAmount());
        gameState.getPlayer().getInventory().craftItem(itemToCraft, recipe.getIngredients());
    }

    private void changeSlot(MouseEvent e) {
        for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
            for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
                int xStart = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int xEnd = xStart + SLOT_SIZE;
                int yStart = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                int yEnd = yStart + SLOT_SIZE;

                if (e.getX() >= xStart && e.getX() <= xEnd && e.getY() >= yStart && e.getY() <= yEnd) {
                    slotNumber = i + (j * INVENTORY_SLOT_MAX_ROW);
                    craftingOverlay.updateSelectedSlot();
                    return;
                }
            }
        }
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
        int maxPages = (int)Math.ceil((double)RecipeManager.getInstance().getRecipes().size() / (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
        this.craftingSlot = Math.min(craftingSlot + 1, maxPages-1);
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

    // Getters
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