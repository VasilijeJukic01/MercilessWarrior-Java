package platformer.ui.overlays;

import platformer.model.gameObjects.objects.Table;
import platformer.model.inventory.*;
import platformer.state.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.SmallButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.CRAFTING_TXT;
import static platformer.constants.FilePaths.SLOT_INVENTORY;
import static platformer.constants.UI.*;

/**
 * CraftingOverlay is a class that is responsible for rendering the crafting overlay.
 * The crafting overlay is a screen that allows the player to interact with the crafting mechanics.
 */
public class CraftingOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;

    private Rectangle2D overlay;
    private BufferedImage craftingText;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;

    private Rectangle2D craftingPanel;
    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private int craftingSlot;
    private int slotNumber;

    private List<Table> tables;

    public CraftingOverlay(GameState gameState) {
        this.gameState = gameState;
        this.tables = gameState.getObjectManager().getObjects(Table.class);
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
        this.craftingText = Utils.getInstance().importImage(CRAFTING_TXT, INV_TEXT_WID, INV_TEXT_HEI);
        this.craftingPanel = new Rectangle2D.Double(BACKPACK_X, BACKPACK_Y, BACKPACK_WID, BACKPACK_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_INVENTORY, SLOT_SIZE, SLOT_SIZE);
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(PREV_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(NEXT_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);

        mediumButtons[0] = new MediumButton(EQUIP_BTN_X, INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CRAFT);
    }

    private void initSelectedSlot() {
        int xPos = (slotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
        int yPos = (slotNumber / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }


    // Selection
    private void setSelectedSlot() {
        this.selectedSlot.x = (slotNumber % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X;
        int offset = slotNumber / INVENTORY_SLOT_MAX_ROW;
        this.selectedSlot.y = offset * SLOT_SPACING + BACKPACK_SLOT_Y;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xStart = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int xEnd = i * SLOT_SPACING + BACKPACK_SLOT_X + SLOT_SIZE;
                int yStart = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                int yEnd = j * SLOT_SPACING + BACKPACK_SLOT_Y + SLOT_SIZE;

                if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                    slotNumber = i + (j * INVENTORY_SLOT_MAX_ROW);
                    setSelectedSlot();
                    break;
                }
            }
        }
    }

    @Override
    public void update() {
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
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
        int pageNumber = craftingSlot + 1;
        g2d.drawString("Page: " + pageNumber, (int) (craftingPanel.getX() + craftingPanel.getWidth() - 40 * SCALE), (int) craftingPanel.getY() - 10);
    }

    private void renderCraftingSlots(Graphics g) {
        List<Recipe> recipes = RecipeManager.getInstance().getRecipes();
        for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
                int xPos = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int yPos = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
                int currentSlot = (j * INVENTORY_SLOT_MAX_ROW + i) + craftingSlot * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL);
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
        BufferedImage itemImage = Utils.getInstance().importImage(itemData.imagePath, -1, -1);
        int slotPos = RecipeManager.getInstance().getRecipes().indexOf(recipe);

        int xPos = (slotPos % INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_X + ITEM_OFFSET_X;
        int yPos = (slotPos / INVENTORY_SLOT_MAX_ROW) * SLOT_SPACING + BACKPACK_SLOT_Y + ITEM_OFFSET_Y;
        g.setColor(itemData.rarity.getColor());
        g.fillRect(xPos - (int)(ITEM_OFFSET_X / 1.1), yPos - (int)(ITEM_OFFSET_Y / 1.1), (int)(SLOT_SIZE / 1.06), (int)(SLOT_SIZE / 1.06));
        g.drawImage(itemImage, xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
    }

    private void renderItemInfo(Graphics g, List<Recipe> recipes) {
        if (slotNumber >= recipes.size()) return;
        Recipe recipe = recipes.get(slotNumber);
        if (recipe != null) renderItemDescription(g, recipe);
    }

    private void renderItemDescription(Graphics g, Recipe recipe) {
        ItemData itemData = ItemDatabase.getInstance().getItemData(recipe.getOutput());
        if (itemData == null) return;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(itemData.name, CRAFT_VAL_ITEM_NAME_X, CRAFT_VAL_ITEM_NAME_Y);
        g.drawString("Value: " + itemData.sellValue, CRAFT_VAL_TEXT_X, CRAFT_VAL_TEXT_Y);
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
        Inventory inventory = gameState.getPlayer().getInventory();
        Map<String, Integer> playerItems = new HashMap<>();
        for (InventoryItem item : inventory.getBackpack()) {
            playerItems.put(item.getItemId(), item.getAmount());
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

            g.setColor(requiredData.rarity.getColor());
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

    // Actions
    private void prevCraftingSlot() {
        this.craftingSlot = Math.max(craftingSlot-1, 0);
    }

    private void nextCraftingSlot() {
        this.craftingSlot = Math.min(craftingSlot+1, CRAFT_SLOT_CAP);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        setMousePressed(e, smallButtons);
        setMousePressed(e, mediumButtons);
        changeSlot(e);
    }

    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(smallButtons).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(mediumButtons).forEach(AbstractButton::resetMouseSet);
    }

    private void releaseSmallButtons(MouseEvent e) {
        for (SmallButton button : smallButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case PREV:
                        prevCraftingSlot();
                        break;
                    case NEXT:
                        nextCraftingSlot();
                        break;
                    default: break;
                }
                break;
            }
        }
    }

    private void releaseMediumButtons(MouseEvent e) {
        for (MediumButton button : mediumButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (Objects.requireNonNull(button.getButtonType()) == ButtonType.CRAFT) {
                    releaseCraftButton();
                }
            }
        }
    }

    private void releaseCraftButton() {
        List<Recipe> recipes = RecipeManager.getInstance().getRecipes();
        if (slotNumber >= recipes.size()) return;
        Recipe recipe = recipes.get(slotNumber);
        if (recipe == null) return;
        InventoryItem itemToCraft = new InventoryItem(recipe.getOutput(), recipe.getAmount());
        gameState.getPlayer().getInventory().craftItem(itemToCraft, recipe.getIngredients());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, smallButtons);
        setMouseMoved(e, mediumButtons);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                moveUp();
                break;
            case KeyEvent.VK_DOWN:
                moveDown();
                break;
            case KeyEvent.VK_LEFT:
                moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                moveRight();
                break;
            case KeyEvent.VK_C:
                releaseCraftButton();
                break;
            default: break;
        }
    }

    private void moveUp() {
        int y = slotNumber / INVENTORY_SLOT_MAX_ROW;
        if (y > 0) slotNumber -= INVENTORY_SLOT_MAX_ROW;
        setSelectedSlot();
    }

    private void moveDown() {
        int y = slotNumber / INVENTORY_SLOT_MAX_ROW;
        if (y < INVENTORY_SLOT_MAX_COL - 1) slotNumber += INVENTORY_SLOT_MAX_ROW;
        setSelectedSlot();
    }

    private void moveLeft() {
        int x = slotNumber % INVENTORY_SLOT_MAX_ROW;
        if (x > 0) slotNumber--;
        setSelectedSlot();
    }

    private void moveRight() {
        int x = slotNumber % INVENTORY_SLOT_MAX_ROW;
        if (x < INVENTORY_SLOT_MAX_ROW - 1) slotNumber++;
        setSelectedSlot();
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void reset() {
        this.tables = gameState.getObjectManager().getObjects(Table.class);
    }

}
