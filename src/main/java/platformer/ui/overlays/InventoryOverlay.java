package platformer.ui.overlays;

import platformer.model.inventory.Inventory;
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
    private int slotNumber;
    private int SLOT_MAX_ROW, SLOT_MAX_COL;

    public InventoryOverlay(GameState gameState) {
        this.gameState = gameState;
        this.mediumButtons = new MediumButton[3];
        this.smallButtons = new SmallButton[2];
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
        this.playerImage = Utils.getInstance().importImage(PLAYER_ICON, -1, -1);
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(PREV_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(NEXT_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);

        mediumButtons[0] = new MediumButton(USE_BTN_X, INV_MEDIUM_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.USE);
        mediumButtons[1] = new MediumButton(EQUIP_BTN_X, INV_MEDIUM_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.EQUIP);
        mediumButtons[2] = new MediumButton(DROP_BTN_X, INV_MEDIUM_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.DROP);
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
                int backpack = (j * SLOT_MAX_ROW + i) + backpackSlot * (SLOT_MAX_ROW * SLOT_MAX_COL);
                if (backpack < inventory.getBackpack().size())
                    renderItem(inventory, i, j, g);
            }
        }
        renderItemInfo(g);
    }

    private void renderItem(Inventory inventory, int i, int j, Graphics g) {
        int slot = (j * SLOT_MAX_ROW + i) + backpackSlot * (SLOT_MAX_ROW * SLOT_MAX_COL);
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
        g2d.drawImage(playerImage, INV_PLAYER_X, INV_PLAYER_Y, INV_PLAYER_WID, INV_PLAYER_HEI, null);
    }

    private void renderEquipSlots(Graphics g) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                int xPos = i * INV_EQUIP_SLOT_SPACING + INV_EQUIP_SLOT_X;
                int yPos = j * SLOT_SPACING + INV_EQUIP_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
                Inventory inventory = gameState.getPlayer().getInventory();
                if (inventory.getEquipped()[j * 2 + i] != null) {
                    InventoryItem item = inventory.getEquipped()[j * 2 + i];
                    int slot = (i * 2 + j);
                    int x = (slot % 2) * INV_EQUIP_SLOT_SPACING + INV_EQUIP_SLOT_X + ITEM_OFFSET_X;
                    int y = (slot / 2) * SLOT_SPACING + INV_EQUIP_SLOT_Y + ITEM_OFFSET_Y;
                    g.drawImage(item.getModel(), x, y, ITEM_SIZE, ITEM_SIZE, null);
                }

            }
        }
    }

    private void renderItemInfo(Graphics g) {
        Inventory inventory = gameState.getPlayer().getInventory();
        if (slotNumber >= inventory.getBackpack().size()) return;
        InventoryItem item = inventory.getBackpack().get(slotNumber);
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
        g.drawString("Health Bonus: +0%", INV_BONUS_X, INV_BONUS_Y + INV_BONUS_SPACING);
        g.drawString("Defense Bonus: +0%", INV_BONUS_X, INV_BONUS_Y + 2 * INV_BONUS_SPACING);
        g.drawString("Attack Bonus: +0%", INV_BONUS_X, INV_BONUS_Y + 3 * INV_BONUS_SPACING);
        g.drawString("Stamina Bonus: +0%", INV_BONUS_X, INV_BONUS_Y + 4 * INV_BONUS_SPACING);
        g.drawString("Critical Bonus: +0%", INV_BONUS_X, INV_BONUS_Y + 5 * INV_BONUS_SPACING);
        g.drawString("Spell Bonus: +0%", INV_BONUS_X, INV_BONUS_Y + 6 * INV_BONUS_SPACING);
        g.drawString("Cooldown Bonus: +0%", INV_BONUS_X, INV_BONUS_Y +  7 * INV_BONUS_SPACING);
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
        Arrays.stream(smallButtons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));

        Arrays.stream(mediumButtons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));

        changeSlot(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
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
        for (MediumButton button : mediumButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                Inventory inventory = gameState.getPlayer().getInventory();
                switch (button.getButtonType()) {
                    case USE:
                        inventory.useItem(slotNumber); break;
                    case EQUIP:
                        inventory.equipItem(slotNumber); break;
                    case DROP:
                        inventory.dropItem(slotNumber); break;
                    default: break;
                }
            }
        }
        Arrays.stream(smallButtons).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(mediumButtons).forEach(AbstractButton::resetMouseSet);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Arrays.stream(smallButtons).forEach(button -> button.setMouseOver(false));
        Arrays.stream(mediumButtons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(smallButtons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(mediumButton -> mediumButton.setMouseOver(true));

        Arrays.stream(mediumButtons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(mediumButton -> mediumButton.setMouseOver(true));
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
