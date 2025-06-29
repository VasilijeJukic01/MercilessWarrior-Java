package platformer.model.quests;

/**
 * Represents a single objective within a quest.
 * Objectives are used by the quest system to track player progress and determine when a quest can be completed.
 */
public class QuestObjective {

    private QuestObjectiveType type;
    private ObjectiveTarget target;
    private int requiredAmount;
    private int currentAmount;

    public QuestObjectiveType getType() {
        return type;
    }

    public void setType(QuestObjectiveType type) {
        this.type = type;
    }

    public ObjectiveTarget getTarget() {
        return target;
    }

    public void setTarget(ObjectiveTarget target) {
        this.target = target;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public void setRequiredAmount(int requiredAmount) {
        this.requiredAmount = requiredAmount;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }
}
