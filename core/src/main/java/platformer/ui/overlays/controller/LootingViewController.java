package platformer.ui.overlays.controller;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.Loot;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.overlays.LootingOverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static platformer.constants.Constants.ITEM_TEXT_COLOR;
import static platformer.constants.UI.SLOT_SIZE;
import static platformer.ui.overlays.LootingOverlay.*;

/**
 * Handles user interaction, selection clamping, and item transfer for the Looting overlay.
 */
public class LootingViewController {

    private final GameContext context;
    private final LootingOverlay overlay;

    private Interactable lastSource = null;
    private int selectedSlot = 0;
    private int hoveredSlot = -1;
    private Point mousePosition = null;

    public LootingViewController(GameContext context, LootingOverlay overlay) {
        this.context = context;
        this.overlay = overlay;
    }

    public void update() {
        Interactable currentSource = context.getObjectManager().getIntersection();
        if (currentSource != lastSource) {
            lastSource = currentSource;
            selectedSlot = 0;
            hoveredSlot = -1;
        }
        clampSelection();
    }

    public Interactable getActiveSource() {
        return context.getObjectManager().getIntersection();
    }

    public List<InventoryItem> getItemsFromSource() {
        Interactable source = getActiveSource();
        if (source instanceof Loot loot) return loot.getItems();
        else if (source instanceof Container container) return container.getItems();
        return null;
    }

    public InventoryItem getSelectedItem() {
        List<InventoryItem> items = getItemsFromSource();
        if (items == null || selectedSlot < 0 || selectedSlot >= items.size()) return null;
        return items.get(selectedSlot);
    }

    public InventoryItem getHoveredItem() {
        List<InventoryItem> items = getItemsFromSource();
        if (items == null || hoveredSlot < 0 || hoveredSlot >= items.size()) return null;
        return items.get(hoveredSlot);
    }

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            int clickedSlot = getSlotAt(e.getPoint());
            if (clickedSlot != -1) {
                selectedSlot = clickedSlot;
                takeSelectedItem();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int clickedSlot = getSlotAt(e.getPoint());
            if (clickedSlot != -1) {
                selectedSlot = clickedSlot;
                takeSelectedItem();
            }
            return;
        }

        setMousePressed(e, overlay.getButtons());
        int clickedSlot = getSlotAt(e.getPoint());
        if (clickedSlot != -1) {
            selectedSlot = clickedSlot;
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) return;
        for (AbstractButton button : overlay.getButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case TAKE -> takeSelectedItem();
                    case TAKE_ALL -> takeAllItems();
                    case CLOSE -> closeOverlay();
                    default -> {}
                }
                break;
            }
        }
        Arrays.stream(overlay.getButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        this.mousePosition = e.getPoint();
        setMouseMoved(e, overlay.getButtons());
        this.hoveredSlot = getSlotAt(e.getPoint());
    }

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
        int maxSlots = GRID_COLS * GRID_ROWS - 1;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_X, KeyEvent.VK_ENTER -> takeSelectedItem();
            case KeyEvent.VK_SPACE -> takeAllItems();
            case KeyEvent.VK_UP -> selectedSlot = Math.max(0, selectedSlot - GRID_COLS);
            case KeyEvent.VK_DOWN -> selectedSlot = Math.min(maxSlots, selectedSlot + GRID_COLS);
            case KeyEvent.VK_LEFT -> selectedSlot = Math.max(0, selectedSlot - 1);
            case KeyEvent.VK_RIGHT -> selectedSlot = Math.min(maxSlots, selectedSlot + 1);
        }
    }

    public void takeSelectedItem() {
        List<InventoryItem> items = getItemsFromSource();
        if (items == null || selectedSlot < 0 || selectedSlot >= items.size()) return;

        Player player = context.getPlayer();
        Inventory inventory = player.getInventory();
        InventoryItem item = items.remove(selectedSlot);
        inventory.addItemToBackpack(item);

        Audio.getInstance().getAudioPlayer().playSound(Sound.COIN_PICK);
        spawnPickupFeedback(item, player);
        clampSelection();
        checkAndCloseOverlayIfEmpty();
    }

    public void takeAllItems() {
        List<InventoryItem> items = getItemsFromSource();
        if (items == null || items.isEmpty()) return;

        Player player = context.getPlayer();
        Inventory inventory = player.getInventory();
        List<InventoryItem> snapshot = new ArrayList<>(items);
        items.clear();

        for (InventoryItem item : snapshot) {
            inventory.addItemToBackpack(item);
            spawnPickupFeedback(item, player);
        }

        Audio.getInstance().getAudioPlayer().playSound(Sound.COIN_PICK);
        checkAndCloseOverlayIfEmpty();
    }

    private void spawnPickupFeedback(InventoryItem item, Player player) {
        if (item == null || context.getEffectManager() == null) return;
        ItemData data = item.getData();
        String label = (data != null) ? data.name : item.getItemId();
        Color color = (data != null && data.rarity != null) ? data.rarity.getTextColor() : ITEM_TEXT_COLOR;
        context.getEffectManager().spawnItemPickupText("+" + item.getAmount() + " " + label, player, color);
    }

    private void checkAndCloseOverlayIfEmpty() {
        List<InventoryItem> items = getItemsFromSource();
        if (items != null && items.isEmpty()) {
            context.getObjectManager().setIntersection(null);
            closeOverlay();
        }
    }

    private void closeOverlay() {
        EventBus.getInstance().publish(new OverlayChangeEvent(null));
    }

    private void clampSelection() {
        List<InventoryItem> items = getItemsFromSource();
        if (items == null || items.isEmpty()) {
            selectedSlot = 0;
            return;
        }
        int maxIdx = Math.min(items.size(), GRID_COLS * GRID_ROWS) - 1;
        if (selectedSlot > maxIdx) {
            selectedSlot = Math.max(0, maxIdx);
        }
    }

    private int getSlotAt(Point p) {
        if (p == null) return -1;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int sx = GRID_START_X + c * GRID_SLOT_STEP;
                int sy = GRID_START_Y + r * GRID_SLOT_STEP;
                Rectangle bounds = new Rectangle(sx, sy, SLOT_SIZE, SLOT_SIZE);
                if (bounds.contains(p)) {
                    return r * GRID_COLS + c;
                }
            }
        }
        return -1;
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

    public List<InventoryItem> getVisibleItems() {
        List<InventoryItem> items = getItemsFromSource();
        return items != null ? items : Collections.emptyList();
    }

    public void reset() {
        selectedSlot = 0;
        hoveredSlot = -1;
        lastSource = null;
        mousePosition = null;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getHoveredSlot() {
        return hoveredSlot;
    }

    public Point getMousePosition() {
        return mousePosition;
    }
}