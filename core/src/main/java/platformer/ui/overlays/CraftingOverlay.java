package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.craft.Recipe;
import platformer.model.inventory.craft.RecipeManager;
import platformer.model.inventory.database.ItemDatabase;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.state.types.GameState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.SmallButton;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.overlays.controller.CraftingViewController;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static platformer.constants.AnimConstants.COIN_H;
import static platformer.constants.AnimConstants.COIN_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.COIN_SHEET;
import static platformer.constants.UI.*;

/**
 * Renders the Alchemy & Material Crafting Table overlay with runic plaques, craftable-ready indicators, and a detailed Recipe Inspector.
 */
public class CraftingOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final CraftingViewController controller;

    private Rectangle2D overlay, craftingPanel, inspectorPanel;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;

    private BufferedImage coinIcon;
    private Rectangle2D.Double selectedSlot;

    private final Map<String, InventoryItem> renderCache = new HashMap<>();
    private final Font boldFont = new Font("Arial", Font.BOLD, FONT_MEDIUM);
    private final Font plainFont = new Font("Arial", Font.PLAIN, FONT_LIGHT);
    private Recipe lastSelectedRecipe = null;
    private List<String> linesCache = new ArrayList<>();

    public CraftingOverlay(GameState gameState) {
        this.controller = new CraftingViewController(gameState, this);
        this.mediumButtons = new MediumButton[2];
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
        this.craftingPanel = new Rectangle2D.Double(BACKPACK_X, BACKPACK_Y, BACKPACK_WID, BACKPACK_HEI);
        this.inspectorPanel = new Rectangle2D.Double(scale(380), BACKPACK_Y, scale(365), BACKPACK_HEI);
        this.coinIcon = SpriteManager.getInstance().loadFromSprite(COIN_SHEET, 1, 1, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H)[0];
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(PREV_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(NEXT_BTN_X, INV_SMALL_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);

        mediumButtons[0] = new MediumButton(USE_BTN_X + scale(32), INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CRAFT);
        mediumButtons[1] = new MediumButton(DROP_BTN_X - scale(16), INV_MEDIUM_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CLOSE);
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

    // Core
    @Override
    public void update() {
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 165));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        Graphics2D g2d = (Graphics2D) g;
        renderOverlay(g2d);
        renderSubPanels(g2d);
        renderCraftingSlots(g2d);
        UIButtonRenderer.drawSelectedSlotFrame(g2d, (int) selectedSlot.x, (int) selectedSlot.y, (int) selectedSlot.width);
        renderButtons(g);
    }

    // Render
    private void renderOverlay(Graphics2D g2d) {
        UIButtonRenderer.drawRunicPanel(g2d, overlay);
        UIButtonRenderer.drawRunicHeader(g2d, "ALCHEMY & CRAFTING", INV_OVERLAY_X + INV_OVERLAY_WID / 2, INV_TEXT_Y);
    }

    private void renderSubPanels(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) craftingPanel.getX(), (int) craftingPanel.getY(), (int) craftingPanel.getWidth(), (int) craftingPanel.getHeight());
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) inspectorPanel.getX(), (int) inspectorPanel.getY(), (int) inspectorPanel.getWidth(), (int) inspectorPanel.getHeight());

        g2d.setFont(UIButtonRenderer.getFont(10.5f * SCALE));
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("RECIPES", (int) craftingPanel.getX() + scale(4), (int) craftingPanel.getY() - scale(5));
        g2d.drawString("FORMULA DETAILS", (int) inspectorPanel.getX() + scale(4), (int) inspectorPanel.getY() - scale(5));

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(INV_TEXT_LABEL);
        int pageNumber = controller.getCraftingSlot() + 1;
        String pageStr = "Page " + pageNumber;
        int pw = g2d.getFontMetrics().stringWidth(pageStr);
        g2d.drawString(pageStr, (int) (craftingPanel.getX() + craftingPanel.getWidth() - pw - scale(4)), (int) craftingPanel.getY() - scale(5));
    }

    private Map<String, Integer> getPlayerMaterialCounts() {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        Map<String, Integer> counts = new HashMap<>();
        for (InventoryItem item : inventory.getBackpack()) {
            if (item != null) counts.merge(item.getItemId(), item.getAmount(), Integer::sum);
        }
        return counts;
    }

    private boolean canCraftRecipe(Recipe recipe, Map<String, Integer> playerMaterials) {
        if (recipe == null || recipe.getIngredients() == null) return false;
        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            if (playerMaterials.getOrDefault(entry.getKey(), 0) < entry.getValue()) return false;
        }
        return true;
    }

    private void renderCraftingSlots(Graphics2D g2d) {
        List<Recipe> recipes = RecipeManager.getInstance().getRecipes();
        Map<String, Integer> playerMaterials = getPlayerMaterialCounts();
        int pageOffset = controller.getCraftingSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL);

        for (int j = 0; j < INVENTORY_SLOT_MAX_COL; j++) {
            for (int i = 0; i < INVENTORY_SLOT_MAX_ROW; i++) {
                int xPos = i * SLOT_SPACING + BACKPACK_SLOT_X;
                int yPos = j * SLOT_SPACING + BACKPACK_SLOT_Y;
                int relSlot = j * INVENTORY_SLOT_MAX_ROW + i;
                int absSlot = relSlot + pageOffset;

                Recipe recipe = (absSlot < recipes.size()) ? recipes.get(absSlot) : null;
                InventoryItem item = null;
                if (recipe != null) {
                    item = renderCache.computeIfAbsent(recipe.getOutput() + ":" + recipe.getAmount(), k -> new InventoryItem(recipe.getOutput(), recipe.getAmount()));
                }

                Color rFill = (item != null && item.getData() != null) ? item.getData().rarity.getColor() : null;
                Color rBorder = (item != null && item.getData() != null) ? item.getData().rarity.getTextColor() : null;
                UIButtonRenderer.drawRunicSlot(g2d, xPos, yPos, SLOT_SIZE, rFill, rBorder, false);

                if (recipe != null && item != null) {
                    boolean craftable = canCraftRecipe(recipe, playerMaterials);
                    Composite old = g2d.getComposite();
                    if (!craftable) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));
                    }
                    g2d.drawImage(item.getModel(), xPos + ITEM_OFFSET_X, yPos + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE, null);
                    g2d.setComposite(old);

                    if (recipe.getAmount() > 1) {
                        UIButtonRenderer.drawStackCount(g2d, recipe.getAmount(), xPos, yPos, SLOT_SIZE);
                    }

                    // Dot indicator
                    if (craftable) {
                        int dotS = scale(5);
                        g2d.setColor(new Color(80, 250, 120));
                        g2d.fillOval(xPos + scale(3), yPos + scale(3), dotS, dotS);
                    }
                }
            }
        }
        renderRecipeInspector(g2d, recipes, playerMaterials);
    }

    private void renderRecipeInspector(Graphics2D g2d, List<Recipe> recipes, Map<String, Integer> playerMaterials) {
        int absSlot = controller.getSlotNumber() + (controller.getCraftingSlot() * (INVENTORY_SLOT_MAX_ROW * INVENTORY_SLOT_MAX_COL));
        int leftX = (int) inspectorPanel.getX() + scale(12);
        int rightX = (int) (inspectorPanel.getX() + inspectorPanel.getWidth() - scale(12));
        int topY = (int) inspectorPanel.getY() + scale(12);

        if (absSlot >= recipes.size() || recipes.get(absSlot) == null) {
            g2d.setFont(new Font("Arial", Font.ITALIC, FONT_LIGHT));
            g2d.setColor(new Color(135, 155, 160));
            String msg = "Select a recipe to inspect its formula.";
            int mw = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, (int) (inspectorPanel.getX() + (inspectorPanel.getWidth() - mw) / 2), (int) (inspectorPanel.getY() + inspectorPanel.getHeight() / 2));
            return;
        }

        Recipe recipe = recipes.get(absSlot);
        ItemData itemData = ItemDatabase.getInstance().getItemData(recipe.getOutput());
        if (itemData == null) return;

        InventoryItem previewItem = renderCache.computeIfAbsent(recipe.getOutput() + ":" + recipe.getAmount(), k -> new InventoryItem(recipe.getOutput(), recipe.getAmount()));

        int previewS = scale(38);
        UIButtonRenderer.drawRunicSlot(g2d, leftX, topY, previewS, itemData.rarity.getColor(), itemData.rarity.getTextColor(), false);
        g2d.drawImage(previewItem.getModel(), leftX + scale(4), topY + scale(4), previewS - scale(8), previewS - scale(8), null);
        if (recipe.getAmount() > 1) {
            UIButtonRenderer.drawStackCount(g2d, recipe.getAmount(), leftX, topY, previewS);
        }

        int textX = leftX + previewS + scale(10);
        int titleY = topY + scale(13);

        g2d.setFont(boldFont);
        g2d.setColor(itemData.rarity.getTextColor());
        String title = itemData.name + (recipe.getAmount() > 1 ? " (x" + recipe.getAmount() + ")" : "");
        g2d.drawString(title, textX, titleY);

        boolean craftable = canCraftRecipe(recipe, playerMaterials);
        String statusText = craftable ? "READY TO CRAFT" : "MISSING INGREDIENTS";
        Color statusCol = craftable ? new Color(95, 245, 130) : new Color(255, 105, 105);
        UIButtonRenderer.drawBadge(g2d, statusText, textX, titleY + scale(14), statusCol, statusCol);

        String valText = String.valueOf(itemData.sellValue);
        int valW = g2d.getFontMetrics().stringWidth(valText);
        int iconS = (int) (COIN_WID / 1.3);
        g2d.setColor(INV_TEXT_VALUE);
        g2d.drawString(valText, rightX - valW - iconS - scale(3), titleY);
        g2d.drawImage(coinIcon, rightX - iconS, titleY - g2d.getFontMetrics().getAscent() + 1, iconS, iconS, null);

        // Desc
        g2d.setColor(INV_TEXT_DESC);
        g2d.setFont(plainFont);
        if (recipe != lastSelectedRecipe) {
            lastSelectedRecipe = recipe;
            linesCache = wrapText(itemData.description, rightX - leftX, g2d.getFontMetrics());
        }

        int y = topY + previewS + scale(14);
        for (int i = 0; i < Math.min(linesCache.size(), 2); i++) {
            g2d.drawString(linesCache.get(i), leftX, y);
            y += g2d.getFontMetrics().getHeight();
        }

        y += scale(6);
        g2d.setColor(new Color(55, 215, 210, 70));
        g2d.drawLine(leftX, y, rightX, y);
        y += scale(14);

        g2d.setFont(boldFont);
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("REQUIRED INGREDIENTS:", leftX, y);
        y += scale(8);

        int rowH = scale(24);
        int iconBoxS = scale(20);
        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            String reqId = entry.getKey();
            int reqAmt = entry.getValue();
            int haveAmt = playerMaterials.getOrDefault(reqId, 0);

            ItemData reqData = ItemDatabase.getInstance().getItemData(reqId);
            if (reqData == null) continue;
            InventoryItem ingItem = renderCache.computeIfAbsent(reqId + ":1", k -> new InventoryItem(reqId, 1));

            UIButtonRenderer.drawRunicSlot(g2d, leftX, y, iconBoxS, reqData.rarity.getColor(), reqData.rarity.getTextColor(), false);
            g2d.drawImage(ingItem.getModel(), leftX + scale(2), y + scale(2), iconBoxS - scale(4), iconBoxS - scale(4), null);

            int rowTextY = y + scale(14);
            g2d.setFont(boldFont);
            g2d.setColor(reqData.rarity.getTextColor());
            g2d.drawString(reqData.name, leftX + iconBoxS + scale(8), rowTextY);

            String countStr = haveAmt + " / " + reqAmt;
            g2d.setColor(haveAmt >= reqAmt ? new Color(110, 255, 130) : new Color(255, 95, 95));
            int cw = g2d.getFontMetrics().stringWidth(countStr);
            g2d.drawString(countStr, rightX - cw, rowTextY);

            y += rowH;
        }

        g2d.setFont(new Font("Arial", Font.ITALIC, FONT_SMALL));
        g2d.setColor(new Color(120, 175, 180));
        g2d.drawString("Right-Click or C to Craft", leftX, (int) (inspectorPanel.getY() + inspectorPanel.getHeight() - scale(6)));
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(smallButtons).forEach(button -> button.render(g));
        Arrays.stream(mediumButtons).forEach(button -> button.render(g));
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;
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
    public void reset() {
        renderCache.clear();
        lastSelectedRecipe = null;
        linesCache.clear();
    }

    public SmallButton[] getSmallButtons() {
        return smallButtons;
    }

    public MediumButton[] getMediumButtons() {
        return mediumButtons;
    }
}