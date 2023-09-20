package platformer.ui.overlays;

import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryItem;
import platformer.state.GameState;
import platformer.ui.buttons.MediumButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;
import static platformer.constants.UI.SLOT_SIZE;

public class InventoryOverlay implements Overlay {

    private final GameState gameState;

    private Rectangle2D overlay;
    private BufferedImage inventoryText;
    private final MediumButton[] buttons;

    private Rectangle2D backpackPanel, equipPanel;
    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private int slotNumber;
    private int SLOT_MAX_ROW, SLOT_MAX_COL;

    public InventoryOverlay(GameState gameState) {
        this.gameState = gameState;
        this.buttons = new MediumButton[3];
        init();
    }

    // Init
    private void init() {
        this.SLOT_MAX_COL = INVENTORY_SLOT_MAX_COL;
        this.SLOT_MAX_ROW = INVENTORY_SLOT_MAX_ROW;
        loadImages();
        loadButtons();
        initSelectedSlot();
    }

    private void loadImages() {
        this.overlay = new Rectangle2D.Double(INV_OVERLAY_X, INV_OVERLAY_Y, INV_OVERLAY_WID, INV_OVERLAY_HEI);
        this.backpackPanel = new Rectangle2D.Double(INV_SPACE_X, INV_SPACE_Y, INV_SPACE_WID, INV_SPACE_HEI);
        this.equipPanel = new Rectangle2D.Double(INV_EQUIP_X, INV_EQUIP_Y, INV_EQUIP_WID, INV_EQUIP_HEI);
        this.inventoryText = Utils.getInstance().importImage(INVENTORY_TXT, INV_TEXT_WID, INV_TEXT_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_INVENTORY, SLOT_SIZE, SLOT_SIZE);
    }

    private void loadButtons() {
        // TODO: Buttons
    }

    private void initSelectedSlot() {
        int xPos = (slotNumber % SLOT_MAX_ROW) * SLOT_SPACING + INV_SLOT_X;
        int yPos = (slotNumber / SLOT_MAX_ROW) * SLOT_SPACING + INV_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    // Selection
    private void setSelectedSlot() {
        this.selectedSlot.x = (slotNumber % SLOT_MAX_ROW) * SLOT_SPACING + INV_SLOT_X;
        this.selectedSlot.y = (slotNumber / SLOT_MAX_ROW) * SLOT_SPACING + INV_SLOT_Y;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SLOT_MAX_COL; j++) {
                if (x >= i*SLOT_SPACING+INV_SLOT_X && x <= i*SLOT_SPACING+INV_SLOT_X+SLOT_SIZE && y >= j*SLOT_SPACING+INV_SLOT_Y && y <= j*SLOT_SPACING+INV_SLOT_Y+SLOT_SIZE) {
                    slotNumber = i + (j * SLOT_MAX_ROW);
                    System.out.println(slotNumber);
                    setSelectedSlot();
                    break;
                }
            }
        }
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        renderOverlay(g2d);
        renderItemsSpace(g2d);
        renderItemSlots(g);
        renderEquipSpace(g2d);
        renderEquipSlots(g);
        g.setColor(Color.RED);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
    }

    // Render
    private void renderOverlay(Graphics2D g2d) {
        g2d.setColor(OVERLAY_COLOR);
        g2d.fill(overlay);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(overlay);
        g2d.drawImage(inventoryText, INV_TEXT_X, INV_TEXT_Y, inventoryText.getWidth(), inventoryText.getHeight(), null);
    }

    private void renderItemsSpace(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(backpackPanel);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(backpackPanel);
    }

    private void renderItemSlots(Graphics g) {
        Inventory inventory = gameState.getPlayer().getInventory();
        for (int i = 0; i < SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SLOT_MAX_COL; j++) {
                int xPos = i * SLOT_SPACING + INV_SLOT_X;
                int yPos = j * SLOT_SPACING + INV_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
                if (j * SLOT_MAX_ROW + i < inventory.getBackpack().size())
                    renderItem(inventory, i, j, g);
            }
        }
    }

    private void renderItem(Inventory inventory, int i, int j, Graphics g) {
        int slot = j * SLOT_MAX_ROW + i;
        InventoryItem item = inventory.getBackpack().get(slot);
        int xPos = (slot % SLOT_MAX_ROW) * SLOT_SPACING + INV_SLOT_X + ITEM_OFFSET_X;
        int yPos = (slot / SLOT_MAX_ROW) * SLOT_SPACING + INV_SLOT_Y + ITEM_OFFSET_Y;
        g.drawImage(item.getModel(), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        int countX = xPos + ITEM_COUNT_OFFSET_X, countY =  yPos + ITEM_COUNT_OFFSET_Y;
        g.drawString(String.valueOf(item.getAmount()), countX, countY);
    }

    private void renderEquipSpace(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(equipPanel);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(equipPanel);
    }

    private void renderEquipSlots(Graphics g) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                int xPos = i * INV_EQUIP_SLOT_SPACING + INV_EQUIP_SLOT_X;
                int yPos = j * SLOT_SPACING + INV_EQUIP_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        changeSlot(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void update() {

    }

    @Override
    public void reset() {

    }

}
