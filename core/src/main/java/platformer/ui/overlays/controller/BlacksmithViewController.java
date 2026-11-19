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
import platformer.model.inventory.database.ItemDatabase;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestObjectiveType;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.overlays.BlacksmithOverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static platformer.constants.UI.SLOT_SIZE;
import static platformer.ui.overlays.BlacksmithOverlay.*;

/**
 * Handles user interaction for the Blacksmith Forge (Weapon Tempering & Equipment Smithing).
 */
public class BlacksmithViewController {

    public enum FilterCategory {
        ALL("ALL"),
        ARMOR("ARMOR"),
        JEWELRY("RINGS"),
        SMELT("SMELT");

        private final String label;

        FilterCategory(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum SortMode {
        DEFAULT("SET"),
        CRAFTABLE("READY"),
        RARITY("RARITY");

        private final String label;

        SortMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final GameContext context;
    private final BlacksmithOverlay blacksmithOverlay;

    private FilterCategory activeFilter = FilterCategory.ALL;
    private SortMode activeSort = SortMode.DEFAULT;

    private boolean anvilSelected = true;
    private int selectedRecipeIndex = 0;
    private int pageIndex = 0;

    public BlacksmithViewController(GameContext context, BlacksmithOverlay blacksmithOverlay) {
        this.context = context;
        this.blacksmithOverlay = blacksmithOverlay;
    }

    public List<SmithingRecipe> getFilteredRecipes() {
        List<SmithingRecipe> all = BlacksmithRecipeManager.getInstance().getSmithingRecipes();
        List<SmithingRecipe> filtered = new ArrayList<>();

        for (SmithingRecipe r : all) {
            if (r.getRequiredFlag() != null && !r.getRequiredFlag().isEmpty() && !context.getWorldMemoryManager().hasFlag(r.getRequiredFlag())) {
                continue;
            }
            ItemData data = ItemDatabase.getInstance().getItemData(r.getOutput());
            if (data == null) continue;
            boolean isEquip = data.equip != null && data.equip.canEquip;
            String slot = isEquip && data.equip.slot != null ? data.equip.slot : "";
            boolean isJewelry = "Ring".equalsIgnoreCase(slot) || "Charm".equalsIgnoreCase(slot);
            boolean match = switch (activeFilter) {
                case ALL -> true;
                case ARMOR -> isEquip && !isJewelry;
                case JEWELRY -> isEquip && isJewelry;
                case SMELT -> !isEquip;
            };
            if (match) filtered.add(r);
        }

        if (activeSort == SortMode.CRAFTABLE) {
            Map<String, Integer> mats = blacksmithOverlay.getPlayerMaterialCounts();
            filtered.sort((a, b) -> {
                boolean canA = blacksmithOverlay.canAffordIngredients(a.getIngredients(), a.getCoinCost(), mats);
                boolean canB = blacksmithOverlay.canAffordIngredients(b.getIngredients(), b.getCoinCost(), mats);
                return Boolean.compare(canB, canA);
            });
        }
        else if (activeSort == SortMode.RARITY) {
            filtered.sort(Comparator.comparingInt((SmithingRecipe r) -> {
                ItemData d = ItemDatabase.getInstance().getItemData(r.getOutput());
                return (d != null && d.rarity != null) ? d.rarity.ordinal() : 0;
            }).thenComparingInt(SmithingRecipe::getCoinCost));
        }
        return filtered;
    }

    public void mousePressed(MouseEvent e) {
        setMousePressed(e, blacksmithOverlay.getButtons());
        setMousePressed(e, blacksmithOverlay.getSmallButtons());
        if (checkFilterAndSortClicks(e)) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.BTN_CLICK);
            return;
        }
        if (checkSelection(e) && SwingUtilities.isRightMouseButton(e)) {
            forgeSelected();
        }
    }

    private boolean checkFilterAndSortClicks(MouseEvent e) {
        Rectangle[] filterBounds = blacksmithOverlay.getFilterTabBounds();
        FilterCategory[] cats = FilterCategory.values();
        for (int i = 0; i < filterBounds.length; i++) {
            if (filterBounds[i].contains(e.getPoint())) {
                activeFilter = cats[i];
                pageIndex = 0;
                selectedRecipeIndex = 0;
                return true;
            }
        }

        if (blacksmithOverlay.getSortButtonBounds().contains(e.getPoint())) {
            SortMode[] modes = SortMode.values();
            activeSort = modes[(activeSort.ordinal() + 1) % modes.length];
            pageIndex = 0;
            selectedRecipeIndex = 0;
            return true;
        }
        return false;
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
        for (AbstractButton button : blacksmithOverlay.getSmallButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (button.getButtonType() == ButtonType.PREV) prevPage();
                else if (button.getButtonType() == ButtonType.NEXT) nextPage();
                break;
            }
        }
        Arrays.stream(blacksmithOverlay.getButtons()).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(blacksmithOverlay.getSmallButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, blacksmithOverlay.getButtons());
        setMouseMoved(e, blacksmithOverlay.getSmallButtons());
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_C) {
            forgeSelected();
        }
    }

    public void keyReleased(KeyEvent e) {
        List<SmithingRecipe> recipes = getFilteredRecipes();
        int maxRecipes = recipes.size();
        int itemsPerPage = FORGE_GRID_COLS * FORGE_GRID_ROWS;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> {
                if (!anvilSelected) {
                    if (selectedRecipeIndex % itemsPerPage < FORGE_GRID_COLS) anvilSelected = true;
                    else selectedRecipeIndex -= FORGE_GRID_COLS;
                }
            }
            case KeyEvent.VK_DOWN -> {
                if (anvilSelected && maxRecipes > 0) {
                    anvilSelected = false;
                    selectedRecipeIndex = pageIndex * itemsPerPage;
                }
                else if (!anvilSelected) {
                    int nextIdx = selectedRecipeIndex + FORGE_GRID_COLS;
                    int maxOnPage = Math.min((pageIndex + 1) * itemsPerPage, maxRecipes) - 1;
                    if (nextIdx <= maxOnPage) {
                        selectedRecipeIndex = nextIdx;
                    }
                }
            }
            case KeyEvent.VK_LEFT -> {
                if (!anvilSelected && maxRecipes > 0) {
                    if (selectedRecipeIndex > pageIndex * itemsPerPage) selectedRecipeIndex--;
                    else prevPage();
                }
            }
            case KeyEvent.VK_RIGHT -> {
                if (!anvilSelected && maxRecipes > 0) {
                    int maxOnPage = Math.min((pageIndex + 1) * itemsPerPage, maxRecipes) - 1;
                    if (selectedRecipeIndex < maxOnPage) selectedRecipeIndex++;
                    else nextPage();
                }
            }
        }
    }

    private boolean checkSelection(MouseEvent e) {
        Rectangle anvilBounds = new Rectangle(ANVIL_CARD_X, ANVIL_CARD_Y, ANVIL_CARD_W, ANVIL_CARD_H);
        if (anvilBounds.contains(e.getPoint())) {
            anvilSelected = true;
            return true;
        }

        List<SmithingRecipe> recipes = getFilteredRecipes();
        int itemsPerPage = FORGE_GRID_COLS * FORGE_GRID_ROWS;
        int pageOffset = pageIndex * itemsPerPage;
        for (int r = 0; r < FORGE_GRID_ROWS; r++) {
            for (int c = 0; c < FORGE_GRID_COLS; c++) {
                int slotIdx = r * FORGE_GRID_COLS + c;
                int absIdx = pageOffset + slotIdx;
                if (absIdx >= recipes.size()) continue;

                int xPos = FORGE_GRID_X + c * FORGE_SLOT_SPACING;
                int yPos = FORGE_GRID_Y + r * FORGE_SLOT_SPACING;
                Rectangle slotBounds = new Rectangle(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
                if (slotBounds.contains(e.getPoint())) {
                    anvilSelected = false;
                    selectedRecipeIndex = absIdx;
                    return true;
                }
            }
        }
        return false;
    }

    private void prevPage() {
        if (pageIndex > 0) {
            pageIndex--;
            if (!anvilSelected) {
                selectedRecipeIndex = pageIndex * (FORGE_GRID_COLS * FORGE_GRID_ROWS);
            }
        }
    }

    private void nextPage() {
        int itemsPerPage = FORGE_GRID_COLS * FORGE_GRID_ROWS;
        int maxPages = (int) Math.ceil((double) getFilteredRecipes().size() / itemsPerPage);
        if (pageIndex < maxPages - 1) {
            pageIndex++;
            if (!anvilSelected) {
                selectedRecipeIndex = pageIndex * itemsPerPage;
            }
        }
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
        List<SmithingRecipe> recipes = getFilteredRecipes();
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

    public int getPageIndex() {
        return pageIndex;
    }

    public FilterCategory getActiveFilter() {
        return activeFilter;
    }

    public SortMode getActiveSort() {
        return activeSort;
    }
}