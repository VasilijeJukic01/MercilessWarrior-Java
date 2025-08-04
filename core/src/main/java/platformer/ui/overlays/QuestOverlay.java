package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.database.ItemDatabase;
import platformer.model.quests.Quest;
import platformer.model.quests.QuestObjective;
import platformer.state.types.GameState;
import platformer.ui.components.slots.QuestSlot;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.controller.QuestViewController;
import platformer.utils.ImageUtils;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * QuestOverlay class is an overlay that is displayed when the player opens the quest log.
 * It allows the player to interact with their quests.
 */
public class QuestOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;
    private final QuestViewController controller;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;

    private Rectangle2D overlay, questListPanel;
    private BufferedImage questsText, coinIcon, expIcon;

    private final Font titleFont = new Font("Arial", Font.BOLD, FONT_BIG);
    private final Font headerFont = new Font("Arial", Font.BOLD, FONT_DIALOGUE);
    private final Font bodyFont = new Font("Arial", Font.PLAIN, FONT_MEDIUM);
    private final Font rewardFont = new Font("Arial", Font.PLAIN, FONT_LIGHT);

    public QuestOverlay(GameState gameState) {
        this.gameState = gameState;
        this.controller = new QuestViewController(gameState, this);
        this.smallButtons = new SmallButton[2];
        this.mediumButtons = new MediumButton[1];
        init();
    }

    // Init
    private void init() {
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.questsText = ImageUtils.importImage(QUESTS_TXT, QUEST_TXT_WID, QUEST_TXT_HEI);
        this.overlay = new Rectangle2D.Double(INV_OVERLAY_X, INV_OVERLAY_Y, INV_OVERLAY_WID, INV_OVERLAY_HEI);
        this.questListPanel = new Rectangle2D.Double(BACKPACK_X, BACKPACK_Y, BACKPACK_WID, BACKPACK_HEI);
        this.coinIcon = SpriteManager.getInstance().loadFromSprite(QUEST_COIN_PATH, 1, 0, QUEST_ICON_SIZE, QUEST_ICON_SIZE, 0, COIN_W, COIN_H)[0];
        this.expIcon = SpriteManager.getInstance().loadFromSprite(QUEST_EXP_PATH, 1, 0, QUEST_ICON_SIZE, QUEST_ICON_SIZE, 0, COIN_W, COIN_H)[0];
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(QUEST_BTN_PREV_X, QUEST_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(QUEST_BTN_NEXT_X, QUEST_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
        mediumButtons[0] = new MediumButton(QUEST_BTN_X, QUEST_BTN_Y + (int)(2 * SCALE) , TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CLOSE);
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
        controller.update();
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        renderOverlay(g);
        renderQuestListPanel((Graphics2D) g);
        renderQuestSlots(g);
        renderQuestDetails(g);
        renderButtons(g);
    }

    private void renderOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(OVERLAY_COLOR);
        g2d.fill(overlay);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(overlay);
        g.drawImage(questsText, QUEST_TEXT_X, QUEST_TEXT_Y, questsText.getWidth(), questsText.getHeight(), null);
    }

    private void renderQuestListPanel(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(questListPanel);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(questListPanel);
        g2d.drawString("Page: " + (controller.getCurrentPage() + 1), (int) (questListPanel.getX() + questListPanel.getWidth() - 40 * SCALE), (int) questListPanel.getY() - 10);
    }

    private void renderQuestSlots(Graphics g) {
        List<QuestSlot> slots = gameState.getQuestManager().getSlots();
        int start = controller.getCurrentPage() * QUEST_SLOT_CAP;
        int end = Math.min(start + QUEST_SLOT_CAP, slots.size());

        for (int i = start; i < end; i++) {
            QuestSlot slot = slots.get(i);
            slot.setYPos(QUEST_SLOT_Y + (i-start) * QUEST_SLOT_SPACING);
            slot.setSelected(i == controller.getSelectedQuest());
            slot.render(g);
        }
    }

    private void renderQuestDetails(Graphics g) {
        List<QuestSlot> slots = gameState.getQuestManager().getSlots();
        if (slots.isEmpty() || controller.getSelectedQuest() >= slots.size()) return;

        Quest quest = slots.get(controller.getSelectedQuest()).getQuest();
        int y = QUEST_DESC_Y;

        y = renderQuestTitle(g, quest, y);
        y = renderQuestDescription(g, quest, y);
        y = renderQuestObjectives(g, quest, y);
        renderQuestRewards(g, quest, y);
    }

    private int renderQuestTitle(Graphics g, Quest quest, int yPos) {
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        g.drawString(quest.getName(), QUEST_DESC_X, yPos);

        String levelReqText = "Lvl. " + quest.getLevelRequirement();
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(quest.getName());
        g.setFont(rewardFont);
        g.setColor(Color.ORANGE);
        g.drawString(levelReqText, QUEST_DESC_X + titleWidth + (int)(10 * SCALE), yPos);

        yPos += (int)(5 * SCALE);
        g.setColor(QUEST_SEPARATOR_COLOR);
        g.fillRect(QUEST_DESC_X - (int)(5*SCALE), yPos, (int)(280*SCALE), 2);
        return yPos + (int) (20 * SCALE);
    }

    private int renderQuestDescription(Graphics g, Quest quest, int yPos) {
        g.setFont(bodyFont);
        g.setColor(INV_TEXT_DESC);
        for (String line : wrapText(quest.getDescription(), (int) (280 * SCALE), g.getFontMetrics())) {
            g.drawString(line, QUEST_DESC_X, yPos);
            yPos += g.getFontMetrics().getHeight();
        }
        return yPos;
    }

    private int renderQuestObjectives(Graphics g, Quest quest, int yPos) {
        yPos += (int) (25 * SCALE);
        g.setColor(QUEST_HEADER_COLOR);
        g.setFont(headerFont);
        g.drawString("Objectives", QUEST_DESC_X, yPos);
        yPos += (int) (8 * SCALE);
        g.setColor(QUEST_SEPARATOR_COLOR);
        g.fillRect(QUEST_DESC_X, yPos, (int)(270*SCALE), 1);
        yPos += (int) (20 * SCALE);

        g.setFont(bodyFont);
        for (int i = 0; i < quest.getObjectives().size(); i++) {
            QuestObjective objective = quest.getObjectives().get(i);
            boolean isComplete = objective.getCurrentAmount() >= objective.getRequiredAmount();
            boolean isActive = (i == quest.getActiveObjectiveIndex());
            if (isComplete) g.setColor(INV_TEXT_BONUS);
            else if (isActive) g.setColor(Color.WHITE);
            else g.setColor(Color.GRAY);
            String objectiveText = String.format("- %s (%d/%d)", objective.getDescription(), objective.getCurrentAmount(), objective.getRequiredAmount());
            g.drawString(objectiveText, QUEST_DESC_X, yPos);
            yPos += g.getFontMetrics().getHeight();
        }
        return yPos;
    }

    private void renderQuestRewards(Graphics g, Quest quest, int yPos) {
        yPos += (int) (25 * SCALE);
        g.setColor(QUEST_HEADER_COLOR);
        g.setFont(headerFont);
        g.drawString("Rewards", QUEST_DESC_X, yPos);
        yPos += (int) (8 * SCALE);
        g.setColor(QUEST_SEPARATOR_COLOR);
        g.fillRect(QUEST_DESC_X, yPos, (int)(270*SCALE), 1);
        yPos += (int) (20 * SCALE);

        g.setFont(rewardFont);
        int rewardSpacing = (int)(12 * SCALE);

        if (quest.getCoinReward() > 0) {
            yPos = renderRewardLine(g, coinIcon, quest.getCoinReward() + " Coins", INV_TEXT_VALUE, yPos);
            yPos += rewardSpacing;
        }

        if (quest.getExpReward() > 0) {
            yPos = renderRewardLine(g, expIcon, quest.getExpReward() + " XP", QUEST_REWARD_XP_COLOR, yPos);
            yPos += rewardSpacing;
        }

        if (quest.getItemRewards() != null) {
            for (Map.Entry<String, Integer> entry : quest.getItemRewards().entrySet()) {
                ItemData item = ItemDatabase.getInstance().getItemData(entry.getKey());
                if (item != null) {
                    BufferedImage itemIcon = ImageUtils.importImage(item.imagePath, -1, -1);
                    yPos = renderRewardLine(g, itemIcon, item.name + " x" + entry.getValue(), item.rarity.getTextColor(), yPos);
                    yPos += rewardSpacing;
                }
            }
        }
    }

    private int renderRewardLine(Graphics g, BufferedImage icon, String text, Color textColor, int yPos) {
        int iconTextPadding = (int)(8 * SCALE);
        int iconY = yPos - g.getFontMetrics().getAscent() + (g.getFontMetrics().getHeight() - ITEM_SIZE) / 2;
        if (icon != null) g.drawImage(icon, QUEST_DESC_X, iconY, ITEM_SIZE, ITEM_SIZE, null);
        g.setColor(textColor);
        g.drawString(text, QUEST_DESC_X + ITEM_SIZE + iconTextPadding, yPos);
        return yPos + g.getFontMetrics().getHeight();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;
        String[] words = text.split(" ");
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
        return lines;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

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

    private void renderButtons(Graphics g) {
        Arrays.stream(smallButtons).forEach(button -> button.render(g));
        Arrays.stream(mediumButtons).forEach(button -> button.render(g));
    }

    @Override
    public void reset() {

    }

    public SmallButton[] getSmallButtons() {
        return smallButtons;
    }

    public MediumButton[] getMediumButtons() {
        return mediumButtons;
    }
}