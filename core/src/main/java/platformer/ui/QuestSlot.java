package platformer.ui;

import platformer.model.quests.Quest;
import platformer.model.quests.QuestType;

import java.awt.*;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * The QuestSlot class represents a slot in the quest log where a player's quests are stored.
 */
public class QuestSlot {

    private Quest quest;
    private final int xPos, yPos;
    private boolean selected;

    public QuestSlot(Quest quest, int xPos, int yPos) {
        this.quest = quest;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void render(Graphics g) {
        g.setColor(quest.getType() == QuestType.PROGRESSIVE ? QUEST_SLOT_COLOR : QUEST_SLOT_REPEATABLE_COLOR);
        g.fillRect(xPos, yPos, QUEST_SLOT_WID, QUEST_SLOT_HEI);

        renderQuestInfo(g);

        if (selected) g.drawRect(xPos, yPos, QUEST_SLOT_WID, QUEST_SLOT_HEI);
    }

    private void renderQuestInfo(Graphics g) {
        g.setColor(Color.WHITE);
        if (quest == null) {
            g.drawString("Empty", xPos + (int)(QUEST_SLOT_WID / 2.3), yPos + (int)(QUEST_SLOT_HEI / 2.1));
        }
        else {
            g.setFont(new Font("Arial", Font.PLAIN, FONT_DIALOGUE));
            g.drawString(quest.getDescription(), xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 19));
            g.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
            g.drawString("Progress: " + (quest.getProgress() / (double) quest.getGoal()) * 100 + "%", xPos + (int)(SCALE * 115), yPos + (int)(SCALE * 37));
            quest.getItemRewards()
                    .forEach((item, amount) -> g.drawString("Reward: "+amount+"x "+item.getName(), xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 37)));
        }
    }

    public void checkSelected(int x, int y) {
        this.selected = isPointInSlot(x, y);
    }

    private boolean isPointInSlot(int x, int y) {
        return (x >= xPos && x <= xPos + QUEST_SLOT_WID && y >= yPos && y <= yPos + QUEST_SLOT_HEI);
    }

    // Getters and Setters
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }
}
