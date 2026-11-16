package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.core.GameContext;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.craft.BlacksmithRecipeManager;
import platformer.model.inventory.craft.BlacksmithRecipeManager.SmithingRecipe;
import platformer.model.inventory.craft.BlacksmithRecipeManager.SwordUpgradeTier;
import platformer.model.inventory.database.ItemDatabase;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.overlays.controller.BlacksmithViewController;
import platformer.utils.ImageUtils;

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
 * Renders the Blacksmith Forge overlay where the player can temper their sword and smelt/forge.
 */
public class BlacksmithOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    public static final int FORGE_GRID_COLS = 7;
    public static final int FORGE_GRID_ROWS = 2;
    public static final int FORGE_GRID_X = scale(104);
    public static final int FORGE_GRID_Y = scale(232);
    public static final int FORGE_SLOT_SPACING = scale(42);

    public static final int ANVIL_CARD_X = scale(104);
    public static final int ANVIL_CARD_Y = scale(122);
    public static final int ANVIL_CARD_W = scale(290);
    public static final int ANVIL_CARD_H = scale(68);

    private final GameContext context;
    private final BlacksmithViewController controller;

    private Rectangle2D.Double overlay, anvilPanel, smithingPanel, inspectorPanel;
    private final MediumButton[] buttons;
    private BufferedImage coinIcon, swordIcon;

    private final Map<String, InventoryItem> renderCache = new HashMap<>();
    private final Font boldFont = new Font("Arial", Font.BOLD, FONT_MEDIUM);
    private final Font plainFont = new Font("Arial", Font.PLAIN, FONT_LIGHT);

    public BlacksmithOverlay(GameContext context) {
        this.context = context;
        this.buttons = new MediumButton[2];
        initPanels();
        loadImages();
        loadButtons();
        this.controller = new BlacksmithViewController(context, this);
    }

    // Init
    private void initPanels() {
        this.overlay = new Rectangle2D.Double(PERKS_OVERLAY_X, PERKS_OVERLAY_Y, PERKS_OVERLAY_WID, PERKS_OVERLAY_HEI);
        this.anvilPanel = new Rectangle2D.Double(scale(92), scale(108), scale(314), scale(94));
        this.smithingPanel = new Rectangle2D.Double(scale(92), scale(216), scale(314), scale(150));
        this.inspectorPanel = new Rectangle2D.Double(scale(418), scale(108), scale(328), scale(258));
    }

    private void loadImages() {
        this.coinIcon = SpriteManager.getInstance().loadFromSprite(COIN_SHEET, 1, 1, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H)[0];
        this.swordIcon = ImageUtils.importImage("/images/objs/perks/Perk1.png", -1, -1);
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(BUY_PERK_BTN_X, BUY_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.CRAFT);
        buttons[1] = new MediumButton(LEAVE_PERK_BTN_X, LEAVE_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.LEAVE);
    }

    public Map<String, Integer> getPlayerMaterialCounts() {
        Inventory inventory = context.getPlayer().getInventory();
        Map<String, Integer> counts = new HashMap<>();
        for (InventoryItem item : inventory.getBackpack()) {
            if (item != null) counts.merge(item.getItemId(), item.getAmount(), Integer::sum);
        }
        return counts;
    }

    public boolean canAffordIngredients(Map<String, Integer> ingredients, int coinCost, Map<String, Integer> playerMats) {
        if (context.getPlayer().getCoins() < coinCost) return false;
        if (ingredients == null) return true;
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            if (playerMats.getOrDefault(entry.getKey(), 0) < entry.getValue()) return false;
        }
        return true;
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        Graphics2D g2d = (Graphics2D) g;
        Map<String, Integer> playerMats = getPlayerMaterialCounts();
        UIButtonRenderer.drawRunicPanel(g2d, overlay);
        UIButtonRenderer.drawRunicHeader(g2d, "BLACKSMITH FORGE", PERKS_OVERLAY_X + PERKS_OVERLAY_WID / 2, PERKS_TEXT_Y - scale(14));

        renderSubPanels(g2d);
        renderAnvilSection(g2d, playerMats);
        renderSmithingGrid(g2d, playerMats);
        renderForgeInspector(g2d, playerMats);
        renderButtons(g);
    }

    private void renderSubPanels(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) anvilPanel.x, (int) anvilPanel.y, (int) anvilPanel.width, (int) anvilPanel.height);
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) smithingPanel.x, (int) smithingPanel.y, (int) smithingPanel.width, (int) smithingPanel.height);
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) inspectorPanel.x, (int) inspectorPanel.y, (int) inspectorPanel.width, (int) inspectorPanel.height);

        g2d.setFont(UIButtonRenderer.getFont(10.0f * SCALE));
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("WEAPON TEMPERING", (int) anvilPanel.x + scale(6), (int) anvilPanel.y - scale(4));
        g2d.drawString("SMELTING & ARMOR SMITHING", (int) smithingPanel.x + scale(6), (int) smithingPanel.y - scale(4));
        g2d.drawString("FORGE BLUEPRINT", (int) inspectorPanel.x + scale(6), (int) inspectorPanel.y - scale(4));
    }

    private void renderAnvilSection(Graphics2D g2d, Map<String, Integer> playerMats) {
        BlacksmithRecipeManager brm = BlacksmithRecipeManager.getInstance();
        int currentTier = brm.getCurrentSwordTier(context.getWorldMemoryManager());
        List<SwordUpgradeTier> tiers = brm.getSwordUpgrades();
        SwordUpgradeTier nextTier = (currentTier < tiers.size()) ? tiers.get(currentTier) : null;

        boolean selected = controller.isAnvilSelected();
        Color cardBorder = selected ? new Color(90, 245, 240, 230) : new Color(85, 110, 115, 190);
        Color cardFill = nextTier == null ? new Color(235, 165, 30) : new Color(25, 115, 125);

        UIButtonRenderer.drawRunicSlot(g2d, ANVIL_CARD_X, ANVIL_CARD_Y, ANVIL_CARD_H, cardFill, cardBorder, false);
        if (swordIcon != null) {
            g2d.drawImage(swordIcon, ANVIL_CARD_X + scale(12), ANVIL_CARD_Y + scale(12), ANVIL_CARD_H - scale(24), ANVIL_CARD_H - scale(24), null);
        }
        if (selected) UIButtonRenderer.drawSelectedSlotFrame(g2d, ANVIL_CARD_X, ANVIL_CARD_Y, ANVIL_CARD_H);

        int textX = ANVIL_CARD_X + ANVIL_CARD_H + scale(12);
        int textY = ANVIL_CARD_Y + scale(18);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_DIALOGUE));
        g2d.setColor(new Color(255, 220, 110));
        String title = nextTier != null ? nextTier.getName() : "Runic Greatsword (MAX TIER)";
        g2d.drawString(title, textX, textY);

        int totalBonus = brm.getTotalSwordBonusDamage(context.getWorldMemoryManager());
        int currentBaseDmg = context.getPlayer().getAttackDmg();
        g2d.setFont(boldFont);
        g2d.setColor(INV_TEXT_DESC);
        String dmgLine = nextTier != null
                ? "Sword Damage: " + currentBaseDmg + "  ->  " + (currentBaseDmg + nextTier.getBonusDamage()) + " (+" + (totalBonus + nextTier.getBonusDamage()) + ")"
                : "Current Base Sword Damage: " + currentBaseDmg + " (+" + totalBonus + " Tempered)";
        g2d.drawString(dmgLine, textX, textY + scale(16));

        // Tier progress pips
        int pipX = textX;
        int pipY = textY + scale(26);
        int pipW = scale(26);
        int pipH = scale(7);
        for (int i = 0; i < tiers.size(); i++) {
            boolean unlocked = i < currentTier;
            g2d.setColor(unlocked ? new Color(255, 200, 55) : new Color(35, 50, 56));
            g2d.fillRoundRect(pipX + i * (pipW + scale(5)), pipY, pipW, pipH, scale(3), scale(3));
            g2d.setColor(unlocked ? new Color(255, 240, 150) : new Color(85, 105, 110));
            g2d.drawRoundRect(pipX + i * (pipW + scale(5)), pipY, pipW, pipH, scale(3), scale(3));
        }

        if (nextTier != null && canAffordIngredients(nextTier.getIngredients(), nextTier.getCoinCost(), playerMats)) {
            int dotS = scale(6);
            g2d.setColor(new Color(80, 250, 120));
            g2d.fillOval(ANVIL_CARD_X + scale(4), ANVIL_CARD_Y + scale(4), dotS, dotS);
        }
    }

    private void renderSmithingGrid(Graphics2D g2d, Map<String, Integer> playerMats) {
        List<SmithingRecipe> recipes = BlacksmithRecipeManager.getInstance().getSmithingRecipes();

        for (int r = 0; r < FORGE_GRID_ROWS; r++) {
            for (int c = 0; c < FORGE_GRID_COLS; c++) {
                int idx = r * FORGE_GRID_COLS + c;
                int xPos = FORGE_GRID_X + c * FORGE_SLOT_SPACING;
                int yPos = FORGE_GRID_Y + r * FORGE_SLOT_SPACING;

                SmithingRecipe recipe = idx < recipes.size() ? recipes.get(idx) : null;
                InventoryItem item = null;
                if (recipe != null) {
                    item = renderCache.computeIfAbsent(recipe.getOutput() + ":" + recipe.getAmount(), k -> new InventoryItem(recipe.getOutput(), recipe.getAmount()));
                }

                Color rFill = (item != null && item.getData() != null) ? item.getData().rarity.getColor() : null;
                Color rBorder = (item != null && item.getData() != null) ? item.getData().rarity.getTextColor() : null;
                UIButtonRenderer.drawRunicSlot(g2d, xPos, yPos, SLOT_SIZE, rFill, rBorder, false);

                if (recipe != null && item != null) {
                    boolean canForge = canAffordIngredients(recipe.getIngredients(), recipe.getCoinCost(), playerMats);
                    Composite old = g2d.getComposite();
                    if (!canForge) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));
                    }
                    g2d.drawImage(item.getModel(), xPos + ITEM_OFFSET_X, yPos + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE, null);
                    g2d.setComposite(old);

                    if (recipe.getAmount() > 1) UIButtonRenderer.drawStackCount(g2d, recipe.getAmount(), xPos, yPos, SLOT_SIZE);
                    if (canForge) {
                        int dotS = scale(5);
                        g2d.setColor(new Color(80, 250, 120));
                        g2d.fillOval(xPos + scale(3), yPos + scale(3), dotS, dotS);
                    }
                }
                if (!controller.isAnvilSelected() && controller.getSelectedRecipeIndex() == idx) {
                    UIButtonRenderer.drawSelectedSlotFrame(g2d, xPos, yPos, SLOT_SIZE);
                }
            }
        }
    }

    private void renderForgeInspector(Graphics2D g2d, Map<String, Integer> playerMats) {
        int leftX = (int) inspectorPanel.x + scale(12);
        int rightX = (int) (inspectorPanel.x + inspectorPanel.width - scale(12));
        int y = (int) inspectorPanel.y + scale(16);

        int playerCoins = context.getPlayer().getCoins();
        g2d.setFont(boldFont);
        g2d.setColor(INV_TEXT_LABEL);
        g2d.drawString("Your Gold:", leftX, y);

        String coinStr = String.valueOf(playerCoins);
        int coinW = g2d.getFontMetrics().stringWidth(coinStr);
        g2d.setColor(SHOP_TEXT_GOLD);
        g2d.drawString(coinStr, rightX - coinW - COIN_WID - scale(3), y);
        g2d.drawImage(coinIcon, rightX - COIN_WID, y - g2d.getFontMetrics().getAscent() + 1, COIN_WID, COIN_HEI, null);

        y += scale(6);
        g2d.setColor(new Color(55, 215, 210, 70));
        g2d.drawLine(leftX, y, rightX, y);
        y += scale(10);

        if (controller.isAnvilSelected()) renderSwordUpgradeDetails(g2d, leftX, rightX, y, playerCoins, playerMats);
        else renderSmithingRecipeDetails(g2d, leftX, rightX, y, playerCoins, playerMats);
    }

    private void renderSwordUpgradeDetails(Graphics2D g2d, int leftX, int rightX, int topY, int playerCoins, Map<String, Integer> playerMats) {
        BlacksmithRecipeManager brm = BlacksmithRecipeManager.getInstance();
        int currentTier = brm.getCurrentSwordTier(context.getWorldMemoryManager());
        List<SwordUpgradeTier> tiers = brm.getSwordUpgrades();

        if (currentTier >= tiers.size()) {
            g2d.setFont(new Font("Arial", Font.BOLD, FONT_DIALOGUE));
            g2d.setColor(new Color(255, 215, 80));
            g2d.drawString("Runic Blade Fully Tempered!", leftX, topY + scale(24));
            g2d.setFont(plainFont);
            g2d.setColor(INV_TEXT_DESC);
            g2d.drawString("Your weapon has reached the pinnacle of", leftX, topY + scale(44));
            g2d.drawString("the Blacksmith's art.", leftX, topY + scale(58));
            return;
        }

        SwordUpgradeTier next = tiers.get(currentTier);
        int previewS = scale(36);
        UIButtonRenderer.drawRunicSlot(g2d, leftX, topY, previewS, new Color(235, 165, 30), new Color(255, 215, 85), false);
        if (swordIcon != null) {
            g2d.drawImage(swordIcon, leftX + scale(6), topY + scale(6), previewS - scale(12), previewS - scale(12), null);
        }

        int textX = leftX + previewS + scale(10);
        g2d.setFont(boldFont);
        g2d.setColor(new Color(255, 220, 100));
        g2d.drawString(next.getName(), textX, topY + scale(13));

        boolean ready = canAffordIngredients(next.getIngredients(), next.getCoinCost(), playerMats);
        Color badgeCol = ready ? new Color(95, 245, 130) : new Color(255, 105, 105);
        UIButtonRenderer.drawBadge(g2d, ready ? "READY TO TEMPER" : "MISSING MATERIALS", textX, topY + scale(27), badgeCol, badgeCol);

        int y = topY + previewS + scale(12);
        g2d.setFont(plainFont);
        g2d.setColor(INV_TEXT_DESC);
        for (String line : wrapText(next.getDescription(), rightX - leftX, g2d.getFontMetrics())) {
            g2d.drawString(line, leftX, y);
            y += g2d.getFontMetrics().getHeight();
        }

        y += scale(6);
        renderRequirementsList(g2d, leftX, rightX, y, next.getCoinCost(), next.getIngredients(), playerCoins, playerMats);
    }

    private void renderSmithingRecipeDetails(Graphics2D g2d, int leftX, int rightX, int topY, int playerCoins, Map<String, Integer> playerMats) {
        List<SmithingRecipe> recipes = BlacksmithRecipeManager.getInstance().getSmithingRecipes();
        int idx = controller.getSelectedRecipeIndex();
        if (idx < 0 || idx >= recipes.size()) return;

        SmithingRecipe recipe = recipes.get(idx);
        ItemData data = ItemDatabase.getInstance().getItemData(recipe.getOutput());
        if (data == null) return;

        InventoryItem preview = renderCache.computeIfAbsent(recipe.getOutput() + ":" + recipe.getAmount(),
                k -> new InventoryItem(recipe.getOutput(), recipe.getAmount()));

        int previewS = scale(36);
        UIButtonRenderer.drawRunicSlot(g2d, leftX, topY, previewS, data.rarity.getColor(), data.rarity.getTextColor(), false);
        g2d.drawImage(preview.getModel(), leftX + scale(4), topY + scale(4), previewS - scale(8), previewS - scale(8), null);

        int textX = leftX + previewS + scale(10);
        g2d.setFont(boldFont);
        g2d.setColor(data.rarity.getTextColor());
        String title = data.name + (recipe.getAmount() > 1 ? " (x" + recipe.getAmount() + ")" : "");
        g2d.drawString(title, textX, topY + scale(13));

        boolean ready = canAffordIngredients(recipe.getIngredients(), recipe.getCoinCost(), playerMats);
        Color badgeCol = ready ? new Color(95, 245, 130) : new Color(255, 105, 105);
        UIButtonRenderer.drawBadge(g2d, ready ? "READY TO FORGE" : "MISSING MATERIALS", textX, topY + scale(27), badgeCol, badgeCol);

        int y = topY + previewS + scale(12);
        g2d.setFont(plainFont);
        g2d.setColor(INV_TEXT_DESC);
        List<String> lines = wrapText(data.description, rightX - leftX, g2d.getFontMetrics());
        for (int i = 0; i < Math.min(lines.size(), 2); i++) {
            g2d.drawString(lines.get(i), leftX, y);
            y += g2d.getFontMetrics().getHeight();
        }

        y += scale(6);
        renderRequirementsList(g2d, leftX, rightX, y, recipe.getCoinCost(), recipe.getIngredients(), playerCoins, playerMats);
    }

    private void renderRequirementsList(Graphics2D g2d, int leftX, int rightX, int startY, int coinCost, Map<String, Integer> ingredients, int playerCoins, Map<String, Integer> playerMats) {
        int y = startY;
        g2d.setFont(boldFont);
        g2d.setColor(INV_TEXT_LABEL);
        g2d.drawString("Smithing Fee:", leftX, y);

        String costStr = String.valueOf(coinCost);
        int cw = g2d.getFontMetrics().stringWidth(costStr);
        g2d.setColor(playerCoins >= coinCost ? SHOP_TEXT_GOLD : SHOP_TEXT_CANNOT_AFFORD);
        g2d.drawString(costStr, rightX - cw - COIN_WID - scale(3), y);
        g2d.drawImage(coinIcon, rightX - COIN_WID, y - g2d.getFontMetrics().getAscent() + 1, COIN_WID, COIN_HEI, null);

        y += scale(6);
        g2d.setColor(new Color(55, 215, 210, 70));
        g2d.drawLine(leftX, y, rightX, y);
        y += scale(8);

        int rowH = scale(22);
        int iconBoxS = scale(18);
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            String reqId = entry.getKey();
            int reqAmt = entry.getValue();
            int haveAmt = playerMats.getOrDefault(reqId, 0);

            ItemData reqData = ItemDatabase.getInstance().getItemData(reqId);
            if (reqData == null) continue;
            InventoryItem ingItem = renderCache.computeIfAbsent(reqId + ":1", k -> new InventoryItem(reqId, 1));

            UIButtonRenderer.drawRunicSlot(g2d, leftX, y, iconBoxS, reqData.rarity.getColor(), reqData.rarity.getTextColor(), false);
            g2d.drawImage(ingItem.getModel(), leftX + scale(2), y + scale(2), iconBoxS - scale(4), iconBoxS - scale(4), null);

            int rowTextY = y + scale(13);
            g2d.setFont(boldFont);
            g2d.setColor(reqData.rarity.getTextColor());
            g2d.drawString(reqData.name, leftX + iconBoxS + scale(8), rowTextY);

            String countStr = haveAmt + " / " + reqAmt;
            g2d.setColor(haveAmt >= reqAmt ? new Color(110, 255, 130) : new Color(255, 95, 95));
            int countW = g2d.getFontMetrics().stringWidth(countStr);
            g2d.drawString(countStr, rightX - countW, rowTextY);

            y += rowH;
        }

        g2d.setFont(new Font("Arial", Font.ITALIC, FONT_SMALL));
        g2d.setColor(new Color(120, 175, 180));
        g2d.drawString("Right-Click or Enter to Temper / Forge", leftX, (int) (inspectorPanel.y + inspectorPanel.height - scale(6)));
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(buttons).forEach(mediumButton -> mediumButton.render(g));
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
    }

    public MediumButton[] getButtons() {
        return buttons;
    }
}