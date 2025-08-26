package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.model.inventory.*;
import platformer.model.inventory.craft.Recipe;
import platformer.model.inventory.craft.RecipeManager;
import platformer.model.inventory.database.ItemDatabase;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.state.types.GameState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.controller.CraftingViewController;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * CraftingOverlay is a class that is responsible for rendering the crafting overlay.
 * The crafting overlay is a screen that allows the player to interact with the crafting mechanics.
 */
public class CraftingOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final CraftingViewController controller;

    private Rectangle2D overlay;
    private BufferedImage craftingText;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;

    private Rectangle2D craftingPanel;
    private BufferedImage slotImage, coinIcon;
    private Rectangle2D.Double selectedSlot;

    public CraftingOverlay(GameState gameState) {
        this.controller = new CraftingViewController(gameState, this);
        this.mediumButtons = new MediumButton[1];
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
        this.craftingText = ImageUtils.importImage(CRAFTING_TXT, INV_TEXT_WID, INV_TEXT_HEI);
        this.craftingPanel = new Rectangle2D.Double(BACKPACK_X, BACKPACK_Y, BACKPACK_WID, BACKPACK_HEI);
        this.slotImage = ImageUtils.importImage(SLOT_INVENTORY, SLOT_SIZE, SLOT_SIZE);
        this.coinIcon = SpriteManager.getInstance().loadFromSprite(COIN_SHEET, 1, 1, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H)[0];
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(PREV_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(NEXT_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);

        mediumButtons[0] = new MediumButton(EQUIP_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CRAFT);
    }

    private void initSelectedSlot() {
        int xPos = (controller.getSlotNumber() % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
        int yPos = (controller.getSlotNumber() / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    public void updateSelectedSlot() {
        this.selectedSlot.x = (controller.getSlotNumber() % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
        this.selectedSlot.y = (controller.getSlotNumber() / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y;
    }

    @Override
    public void update() {
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        Graphics2D g2d = (Graphics2D) g;
        renderOverlay(g2d);
        renderCraftingPanel(g2d);
        renderCraftingSlots(g);
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
        g2d.drawImage(craftingText, INV_TEXT_X, INV_TEXT_Y, craftingText.getWidth(), craftingText.getHeight(), null);
    }

    private void renderCraftingPanel(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(craftingPanel);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(craftingPanel);
        int pageNumber = controller.getCraftingSlot() + 1;
        g2d.drawString("Page: " + pageNumber, (int) (craftingPanel.getX() + craftingPanel.getWidth() - 40 * SCALE), (int) craftingPanel.getY() - 10);
    }

    private void renderCraftingSlots(Graphics g) {
        List<Recipe> recipes = RecipeManager.getInstance().getRecipes();
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xPos = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int yPos = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
                int currentSlot = (j * INVENTORY_SLOT_MAX_ROW + i) + controller.getCraftingSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL);
                if (currentSlot < recipes.size()) {
                    renderItem(g, recipes.get(currentSlot));
                }
            }
        }
        renderItemInfo(g, recipes);
    }

    private void renderItem(Graphics g, Recipe recipe) {
        ItemData itemData = ItemDatabase.getInstance().getItemData(recipe.getOutput());
        if (itemData == null) return;
        BufferedImage itemImage = ImageUtils.importImage(itemData.imagePath, -1, -1);

        int absoluteSlotNumber = RecipeManager.getInstance().getRecipes().indexOf(recipe);
        int pageOffset = controller.getCraftingSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL);
        int relativeSlotNumber = absoluteSlotNumber - pageOffset;

        int xPos = (relativeSlotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X + ITEM_OFFSET_X;
        int yPos = (relativeSlotNumber / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y + ITEM_OFFSET_Y;

        g.setColor(itemData.rarity.getColor());
        g.fillRect(xPos - (int)(ITEM_OFFSET_X / 1.1), yPos - (int)(ITEM_OFFSET_Y / 1.1), (int)(SLOT_SIZE / 1.06), (int)(SLOT_SIZE / 1.06));
        g.drawImage(itemImage, xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
    }

    private void renderItemInfo(Graphics g, List<Recipe> recipes) {
        int absoluteSlot = controller.getSlotNumber() + (controller.getCraftingSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
        if (absoluteSlot >= recipes.size()) return;
        Recipe recipe = recipes.get(absoluteSlot);
        if (recipe != null) renderItemDescription(g, recipe);
    }

    private void renderItemDescription(Graphics g, Recipe recipe) {
        ItemData itemData = ItemDatabase.getInstance().getItemData(recipe.getOutput());
        if (itemData == null) return;
        g.setColor(itemData.rarity.getTextColor());
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(itemData.name, CRAFT_VAL_ITEM_NAME_X, CRAFT_VAL_ITEM_NAME_Y);
        g.setColor(INV_TEXT_LABEL);
        g.drawString("Value: ", CRAFT_VAL_TEXT_X, CRAFT_VAL_TEXT_Y);
        g.setColor(INV_TEXT_VALUE);
        String valueText = String.valueOf(itemData.sellValue);
        g.drawString(valueText, CRAFT_VAL_TEXT_X + g.getFontMetrics().stringWidth("Value: "), CRAFT_VAL_TEXT_Y);
        g.drawImage(coinIcon, CRAFT_VAL_TEXT_X + g.getFontMetrics().stringWidth("Value: " + valueText) + 2, CRAFT_VAL_TEXT_Y - g.getFontMetrics().getAscent() + 1, (int)(COIN_WID/1.5), (int)(COIN_HEI/1.5), null);

        g.setColor(INV_TEXT_DESC);
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));

        int descriptionMaxWidth = (int) (overlay.getX() + overlay.getWidth() - CRAFT_VAL_ITEM_DESC_X - (10 * SCALE));
        List<String> wrappedLines = wrapText(itemData.description, descriptionMaxWidth, g.getFontMetrics());

        int lineHeight = g.getFontMetrics().getHeight();
        int y = CRAFT_VAL_ITEM_DESC_Y;
        for (String line : wrappedLines) {
            g.drawString(line, CRAFT_VAL_ITEM_DESC_X, y);
            y += lineHeight;
        }
        renderResources(g, recipe.getIngredients(), y);
    }

    private void renderResources(Graphics g, Map<String, Integer> resources, int yPos) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        Map<String, Integer> playerItems = new HashMap<>();
        for (InventoryItem item : inventory.getBackpack()) {
            if (item != null) playerItems.merge(item.getItemId(), item.getAmount(), Integer::sum);
        }

        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        int lineHeight = g.getFontMetrics().getHeight();
        int y = yPos;

        g.setColor(Color.WHITE);
        y += lineHeight;
        g.drawString("Required Resources:", CRAFT_VAL_ITEM_DESC_X, y);
        y += lineHeight;

        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            String requiredId = entry.getKey();
            ItemData requiredData = ItemDatabase.getInstance().getItemData(requiredId);
            if (requiredData == null) continue;

            int requiredAmount = entry.getValue();
            int playerAmount = playerItems.getOrDefault(requiredId, 0);

            g.setColor(requiredData.rarity.getTextColor());
            String resourceText = " - " + requiredData.name + ": " + requiredAmount + "x";
            g.drawString(resourceText, CRAFT_VAL_ITEM_DESC_X, y);

            Color haveColor = (playerAmount >= requiredAmount) ? new Color(120, 255, 120) : new Color(255, 90, 90);
            g.setColor(haveColor);
            String haveText = "(" + playerAmount + ")";
            int resourceTextWidth = g.getFontMetrics().stringWidth(resourceText);
            g.drawString(haveText, CRAFT_VAL_ITEM_DESC_X + resourceTextWidth + (int)(5*SCALE), y);

            y += lineHeight;
        }
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (fm.stringWidth(currentLine + " " + word) <= maxWidth) {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                }
                else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
            if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        }
        return lines;
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent e) {
        controller.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        controller.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        controller.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        controller.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        controller.keyReleased(e);
    }

    @Override
    public void reset() { }

    // Getters for the controller
    public SmallButton[] getSmallButtons() {
        return smallButtons;
    }

    public MediumButton[] getMediumButtons() {
        return mediumButtons;
    }
}