package platformer.ui.overlays;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.Loot;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemData;
import platformer.state.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.SLOT_INVENTORY;
import static platformer.constants.UI.*;

/**
 * Class that represents the overlay that is displayed when the player is looting.
 */
public class LootingOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;
    private final MediumButton[] buttons;

    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private int slotNumber;

    public LootingOverlay(GameState gameState) {
        this.gameState = gameState;
        this.buttons = new MediumButton[3];
        init();
    }

    // Init
    private void init() {
        loadImages();
        loadButtons();
        initSelectedSlot();
    }

    private void loadImages() {
        this.slotImage = Utils.getInstance().importImage(SLOT_INVENTORY, SLOT_SIZE, SLOT_SIZE);
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(TAKE_BTN_X, LOOT_BTN_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.TAKE);
        buttons[1] = new MediumButton(TAKE_ALL_BTN_X, LOOT_BTN_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.TAKE_ALL);
        buttons[2] = new MediumButton(CLOSE_BTN_X, LOOT_BTN_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.CLOSE);
    }

    private void initSelectedSlot() {
        int xPos = (slotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + LOOT_SLOT_X;
        int yPos = (slotNumber / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + LOOT_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    private List<InventoryItem> getItemsFromSource() {
        GameObject source = gameState.getObjectManager().getIntersection();
        if (source instanceof Loot) return ((Loot) source).getItems();
        else if (source instanceof Container) return ((Container) source).getItems();
        return null;
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().renderOverlay(g);
        renderSlots(g);
        renderItems(g);
        renderButtons(g);
        g.setColor(Color.RED);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
    }

    // Render
    private void renderSlots(Graphics g) {
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xPos = i * SLOT_SPACING + LOOT_SLOT_X;
                int yPos = j * SLOT_SPACING + LOOT_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
            }
        }
    }

    private void renderItems(Graphics g) {
        List<InventoryItem> itemsToRender = getItemsFromSource();
        if (itemsToRender == null) {
            gameState.setOverlay(null);
            return;
        }
        int slot = 0;
        for (InventoryItem item : itemsToRender) {
            if (item.getAmount() > 0) {
                renderItem(g, item, slot);
                slot++;
            }
        }
    }

    private void renderItem(Graphics g, InventoryItem item, int slot) {
        ItemData itemData = item.getData();
        if (itemData == null) return;
        int xPos = (slot % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + LOOT_SLOT_X + ITEM_OFFSET_X;
        int yPos = (slot / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + LOOT_SLOT_Y + ITEM_OFFSET_Y;
        g.setColor(itemData.rarity.getColor());
        g.fillRect(xPos-(int)(ITEM_OFFSET_X/1.1), yPos-(int)(ITEM_OFFSET_Y/1.1), (int)(SLOT_SIZE/1.06), (int)(SLOT_SIZE/1.06));
        g.drawImage(item.getModel(), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        int countX = xPos + ITEM_COUNT_OFFSET_X, countY =  yPos + ITEM_COUNT_OFFSET_Y;
        g.drawString(String.valueOf(item.getAmount()), countX, countY);
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(buttons).forEach(button -> button.render(g));
    }

    // Selection
    private void setSelectedSlot() {
        this.selectedSlot.x = (slotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + LOOT_SLOT_X;
        int offset = slotNumber / INVENTORY_SLOT_MAX_COL;
        this.selectedSlot.y = offset * SLOT_SPACING + LOOT_SLOT_Y;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xStart = i * SLOT_SPACING + LOOT_SLOT_X;
                int xEnd = i * SLOT_SPACING + LOOT_SLOT_X + SLOT_SIZE;
                int yStart = j * SLOT_SPACING + LOOT_SLOT_Y;
                int yEnd = j * SLOT_SPACING + LOOT_SLOT_Y + SLOT_SIZE;
                if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                    slotNumber = i + (j* INVENTORY_SLOT_MAX_COL);
                    setSelectedSlot();
                    break;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));

        changeSlot(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (MediumButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case TAKE:
                        takeCurrentItem();
                        break;
                    case TAKE_ALL:
                        takeAllItems();
                        break;
                    case CLOSE:
                        gameState.setOverlay(null);
                        break;
                    default: break;
                }
                break;
            }
        }
        Arrays.stream(buttons).forEach(AbstractButton::resetMouseSet);
    }

    private void takeCurrentItem() {
        List<InventoryItem> items = getItemsFromSource();
        if (items == null || slotNumber >= items.size()) return;
        Inventory inventory = gameState.getPlayer().getInventory();
        InventoryItem item = items.get(slotNumber);
        inventory.addItemToBackpack(item);
        items.remove(item);
        checkAndCloseOverlayIfEmpty();
    }

    private void takeAllItems() {
        List<InventoryItem> items = getItemsFromSource();
        if (items == null) return;
        Inventory inventory = gameState.getPlayer().getInventory();
        for (InventoryItem item : new ArrayList<>(items)) {
            inventory.addItemToBackpack(item);
        }
        items.clear();
        checkAndCloseOverlayIfEmpty();
    }

    private void checkAndCloseOverlayIfEmpty() {
        List<InventoryItem> items = getItemsFromSource();
        if (items != null && items.isEmpty()) {
            gameState.getObjectManager().setIntersection(null);
            gameState.setOverlay(null);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, MediumButton mediumButton) {
        return mediumButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            slotNumber -= INVENTORY_SLOT_MAX_ROW;
            if (slotNumber < 0) slotNumber = 0;
            setSelectedSlot();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            slotNumber += INVENTORY_SLOT_MAX_ROW;
            if (slotNumber >= INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL) slotNumber = INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL - 1;
            setSelectedSlot();
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            slotNumber--;
            if (slotNumber < 0) slotNumber = 0;
            setSelectedSlot();
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            slotNumber++;
            if (slotNumber >= INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL) slotNumber = INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL - 1;
            setSelectedSlot();
        }
        else if (e.getKeyCode() == KeyEvent.VK_X) {
            takeCurrentItem();
        }
    }

    @Override
    public void reset() {

    }

}
