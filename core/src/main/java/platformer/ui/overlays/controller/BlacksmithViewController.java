package platformer.ui.overlays.controller;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.entities.player.Player;
import platformer.model.inventory.craft.BlacksmithRecipeManager;
import platformer.model.inventory.craft.BlacksmithRecipeManager.SmithingRecipe;
import platformer.model.inventory.craft.BlacksmithRecipeManager.SwordUpgradeTier;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestObjectiveType;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.overlays.BlacksmithOverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.UI.SLOT_SIZE;
import static platformer.ui.overlays.BlacksmithOverlay.*;

/**
 * Handles user interaction for the Blacksmith Forge (Weapon Tempering & Equipment Smithing).
 */
public class BlacksmithViewController {

    private final GameContext context;
    private final BlacksmithOverlay blacksmithOverlay;

    private boolean anvilSelected = true;
    private int selectedRecipeIndex = 0;

    public BlacksmithViewController(GameContext context, BlacksmithOverlay blacksmithOverlay) {
        this.context = context;
        this.blacksmithOverlay = blacksmithOverlay;
    }

    public void mousePressed(MouseEvent e) {
        setMousePressed(e, blacksmithOverlay.getButtons());
        if (checkSelection(e) && SwingUtilities.isRightMouseButton(e)) {
            forgeSelected();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) return;
        for (AbstractButton button : blacksmithOverlay.getButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case CRAFT, BUY -> forgeSelected();
                    case LEAVE, CLOSE -> EventBus.getInstance().publish(new OverlayChangeEvent(null));
                    default -> {}
                }
                break;
            }
        }
        Arrays.stream(blacksmithOverlay.getButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, blacksmithOverlay.getButtons());
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_C) {
            forgeSelected();
        }
    }

    public void keyReleased(KeyEvent e) {
        int maxRecipes = BlacksmithRecipeManager.getInstance().getSmithingRecipes().size();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> {
                if (!anvilSelected) {
                    if (selectedRecipeIndex < FORGE_GRID_COLS) anvilSelected = true;
                    else selectedRecipeIndex -= FORGE_GRID_COLS;
                }
            }
            case KeyEvent.VK_DOWN -> {
                if (anvilSelected) anvilSelected = false;
                else if (selectedRecipeIndex + FORGE_GRID_COLS < maxRecipes) {
                    selectedRecipeIndex += FORGE_GRID_COLS;
                }
            }
            case KeyEvent.VK_LEFT -> {
                if (!anvilSelected) selectedRecipeIndex = Math.max(0, selectedRecipeIndex - 1);
            }
            case KeyEvent.VK_RIGHT -> {
                if (!anvilSelected) selectedRecipeIndex = Math.min(maxRecipes - 1, selectedRecipeIndex + 1);
            }
        }
    }

    private boolean checkSelection(MouseEvent e) {
        Rectangle anvilBounds = new Rectangle(ANVIL_CARD_X, ANVIL_CARD_Y, ANVIL_CARD_W, ANVIL_CARD_H);
        if (anvilBounds.contains(e.getPoint())) {
            anvilSelected = true;
            return true;
        }

        List<SmithingRecipe> recipes = BlacksmithRecipeManager.getInstance().getSmithingRecipes();
        for (int r = 0; r < FORGE_GRID_ROWS; r++) {
            for (int c = 0; c < FORGE_GRID_COLS; c++) {
                int idx = r * FORGE_GRID_COLS + c;
                if (idx >= recipes.size()) continue;
                int xPos = FORGE_GRID_X + c * FORGE_SLOT_SPACING;
                int yPos = FORGE_GRID_Y + r * FORGE_SLOT_SPACING;
                Rectangle slotBounds = new Rectangle(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
                if (slotBounds.contains(e.getPoint())) {
                    anvilSelected = false;
                    selectedRecipeIndex = idx;
                    return true;
                }
            }
        }
        return false;
    }

    private void forgeSelected() {
        Player player = context.getPlayer();
        if (anvilSelected) temperSword(player);
        else smithItem(player);
    }

    private void temperSword(Player player) {
        BlacksmithRecipeManager brm = BlacksmithRecipeManager.getInstance();
        int currentTier = brm.getCurrentSwordTier(context.getWorldMemoryManager());
        List<SwordUpgradeTier> tiers = brm.getSwordUpgrades();
        if (currentTier >= tiers.size()) return;

        SwordUpgradeTier nextTier = tiers.get(currentTier);
        if (player.getInventory().hasAndConsumeResources(nextTier.getIngredients(), nextTier.getCoinCost(), player)) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.CRAFTING);
            context.getWorldMemoryManager().addFlag("SWORD_TIER_" + nextTier.getTier());
            context.getQuestManager().updateQuestProgress(QuestObjectiveType.UPGRADE, ObjectiveTarget.SWORD_UPGRADE);
        }
    }

    private void smithItem(Player player) {
        List<SmithingRecipe> recipes = BlacksmithRecipeManager.getInstance().getSmithingRecipes();
        if (selectedRecipeIndex < 0 || selectedRecipeIndex >= recipes.size()) return;

        SmithingRecipe recipe = recipes.get(selectedRecipeIndex);
        if (player.getInventory().hasAndConsumeResources(recipe.getIngredients(), recipe.getCoinCost(), player)) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.CRAFTING);
            player.getInventory().addItemToBackpack(new InventoryItem(recipe.getOutput(), recipe.getAmount()));
        }
    }

    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getPoint());
    }

    public boolean isAnvilSelected() {
        return anvilSelected;
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipeIndex;
    }
}