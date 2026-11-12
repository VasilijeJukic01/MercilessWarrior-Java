package platformer.model.levels.metadata;

import lombok.Getter;
import lombok.Setter;
import platformer.model.minimap.MinimapIconType;
import platformer.model.quests.QuestManager;
import platformer.model.world.WorldMemoryManager;

/**
 * Defines declarative visibility rules for a minimap icon in a level's metadata JSON.
 */
@Getter
@Setter
public class MinimapIconMetadata {

    private MinimapIconType type;
    private String requiredFlag;
    private String excludeFlag;
    private String excludeQuestActive;
    private boolean hideInCombat;

    public boolean isConditionMet(WorldMemoryManager memory, QuestManager quests, boolean bossFightActive) {
        if (hideInCombat && bossFightActive) return false;
        if (requiredFlag != null && !memory.hasFlag(requiredFlag)) return false;
        if (excludeFlag != null && memory.hasFlag(excludeFlag)) return false;
        if (excludeQuestActive != null && quests.isQuestActive(excludeQuestActive)) return false;
        return true;
    }
}