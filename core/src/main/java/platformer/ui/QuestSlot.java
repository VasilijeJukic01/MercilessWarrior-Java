package platformer.ui;

import lombok.Getter;
import lombok.Setter;
import platformer.model.inventory.ItemData;
import platformer.model.inventory.ItemDatabase;
import platformer.model.quests.Quest;
import platformer.model.quests.QuestType;

import java.awt.*;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * The QuestSlot class represents a slot in the quest log where a player's quests are stored.
 */
@Getter
@Setter
public class QuestSlot {

    private Quest quest;
    private int xPos, yPos;
    private boolean selected;

    public QuestSlot(Quest quest, int xPos, int yPos) {
        this.quest = quest;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void render(Graphics g) {
        g.setColor(quest.getType() == QuestType.PROGRESSIVE ? QUEST_SLOT_COLOR : QUEST_SLOT_REPEATABLE_COLOR);
        if (quest.isCompleted()) g.setColor(QUEST_SLOT_COMPLETE);
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
            g.drawString(quest.getName(), xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 19));
            g.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
            if (quest.getObjectives() != null && !quest.getObjectives().isEmpty()) {
                var obj = quest.getObjectives().get(quest.getActiveObjectiveIndex());
                int percent = (int) ((obj.getCurrentAmount() * 100.0) / obj.getRequiredAmount());
                String progress = String.format("Progress: (%d%%)", percent);
                g.drawString(progress, xPos + (int)(SCALE * 117), yPos + (int)(SCALE * 37));
            }
            if (quest.getItemRewards() != null) {
                quest.getItemRewards()
                        .forEach((itemId, amount) -> {
                            ItemData data = ItemDatabase.getInstance().getItemData(itemId);
                            String name = (data != null) ? data.name : "Unknown Item";
                            g.drawString("Reward: " + amount + "x " + name, xPos + (int) (SCALE * 10), yPos + (int) (SCALE * 37));
                        });
            }
        }
    }

    public void checkSelected(int x, int y) {
        this.selected = isPointInSlot(x, y);
    }

    public boolean isCompleted() {
        return quest != null && quest.isCompleted();
    }

    private boolean isPointInSlot(int x, int y) {
        return (x >= xPos && x <= xPos + QUEST_SLOT_WID && y >= yPos && y <= yPos + QUEST_SLOT_HEI);
    }
}
