package platformer.model.quests;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single objective within a quest.
 * Objectives are used by the quest system to track player progress and determine when a quest can be completed.
 */
@Getter
@Setter
public class QuestObjective {

    private QuestObjectiveType type;
    private String target;
    private String levelId;
    private String mapLevelId;
    private Integer targetX;
    private Integer targetY;
    private String description;
    private int requiredAmount;
    private int currentAmount;
    private boolean hideCounter = false;

}