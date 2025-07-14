package platformer.ui;

import lombok.Getter;
import lombok.Setter;
import platformer.model.quests.Quest;
import platformer.model.quests.QuestObjective;
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
        this.xPos = QUEST_SLOT_X;
        Graphics2D g2d = (Graphics2D) g;
        renderSlotRectangle(g2d);
        renderSelectionBorder(g2d);
        renderQuestInfo(g);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(xPos, yPos, QUEST_SLOT_WID, QUEST_SLOT_HEI, 10, 10);
    }

    private void renderSlotRectangle(Graphics2D g2d) {
        GradientPaint gp;
        if (quest.isCompleted())
            gp = new GradientPaint(xPos, yPos, QUEST_SLOT_COMPLETED_BG_START, xPos, yPos + QUEST_SLOT_HEI, QUEST_SLOT_COMPLETED_BG_END);
        else if (quest.getType() == QuestType.PROGRESSIVE)
            gp = new GradientPaint(xPos, yPos, QUEST_SLOT_PROGRESSIVE_BG_START, xPos, yPos + QUEST_SLOT_HEI, QUEST_SLOT_PROGRESSIVE_BG_END);
        else
            gp = new GradientPaint(xPos, yPos, QUEST_SLOT_REPEATABLE_BG_START, xPos, yPos + QUEST_SLOT_HEI, QUEST_SLOT_REPEATABLE_BG_END);
        g2d.setPaint(gp);
        g2d.fillRoundRect(xPos, yPos, QUEST_SLOT_WID, QUEST_SLOT_HEI, 10, 10);
    }

    private void renderSelectionBorder(Graphics2D g2d) {
        if (selected) {
            g2d.setColor(QUEST_SELECTED_GLOW_COLOR);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(xPos - 1, yPos - 1, QUEST_SLOT_WID + 2, QUEST_SLOT_HEI + 2, 12, 12);
        }
    }

    private void renderQuestInfo(Graphics g) {
        if (quest == null) {
            g.setColor(Color.WHITE);
            g.drawString("Empty", xPos + (int)(QUEST_SLOT_WID / 2.3), yPos + (int)(QUEST_SLOT_HEI / 2.1));
        }
        else {
            g.setFont(new Font("Arial", Font.BOLD, FONT_DIALOGUE));
            g.setColor(Color.WHITE);
            String questTitle = quest.getName();
            if (quest.getType() == QuestType.REPEATABLE) questTitle += " [R]";
            g.drawString(questTitle, xPos + (int)(10 * SCALE), yPos + (int)(19 * SCALE));
            g.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
            if (quest.getObjectives() != null && !quest.getObjectives().isEmpty()) {
                if (quest.isCompleted()) {
                    g.setColor(QUEST_REWARD_XP_COLOR);
                    g.drawString("Completed", xPos + (int)(10 * SCALE), yPos + (int)(33 * SCALE));
                }
                else {
                    g.setColor(Color.LIGHT_GRAY);
                    QuestObjective obj = quest.getObjectives().get(quest.getActiveObjectiveIndex());
                    int percent = (int) ((obj.getCurrentAmount() * 100.0) / obj.getRequiredAmount());
                    String progress = String.format("Progress: (%d%%)", percent);
                    g.drawString(progress, xPos + (int)(10 * SCALE), yPos + (int)(33 * SCALE));
                }
            }
        }
    }

    public boolean isCompleted() {
        return quest != null && quest.isCompleted();
    }

    public boolean isPointInSlot(int x, int y) {
        return (x >= xPos && x <= xPos + QUEST_SLOT_WID && y >= yPos && y <= yPos + QUEST_SLOT_HEI);
    }
}
