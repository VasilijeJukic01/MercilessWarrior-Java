package platformer.model.levels.metadata;

import lombok.Getter;
import lombok.Setter;
import platformer.model.entities.Direction;
import platformer.model.gameObjects.npc.NpcType;
import platformer.model.quests.QuestManager;
import platformer.model.world.WorldMemoryManager;

/**
 * Defines conditional spawn or visibility rules for an NPC in a level's metadata.
 */
@Getter
@Setter
public class NpcMetadata {

    private NpcType type;
    private Integer x;
    private Integer y;
    private Direction direction;
    private String requiredFlag;
    private String excludeFlag;
    private String excludeQuestActive;

    /**
     * Evaluates whether this NPC should be present based on current world flags and quest state.
     */
    public boolean isConditionMet(WorldMemoryManager memory, QuestManager quests) {
        if (requiredFlag != null && !memory.hasFlag(requiredFlag)) return false;
        if (excludeFlag != null && memory.hasFlag(excludeFlag)) return false;
        if (excludeQuestActive != null && quests.isQuestActive(excludeQuestActive)) return false;
        return true;
    }
}