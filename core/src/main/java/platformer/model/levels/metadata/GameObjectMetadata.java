package platformer.model.levels.metadata;

import lombok.Getter;
import lombok.Setter;
import platformer.model.gameObjects.ObjType;
import platformer.model.quests.QuestManager;
import platformer.model.world.WorldMemoryManager;

/**
 * Defines conditional visibility rules for game objects in a level's metadata.
 */
@Getter
@Setter
public class GameObjectMetadata {

    private ObjType type;
    private Integer tileX;
    private Integer tileY;
    private String requiredFlag;
    private String excludeFlag;
    private String excludeQuestActive;

    public boolean isConditionMet(WorldMemoryManager memory, QuestManager quests) {
        if (requiredFlag != null && !memory.hasFlag(requiredFlag)) return false;
        if (excludeFlag != null && memory.hasFlag(excludeFlag)) return false;
        if (excludeQuestActive != null && quests.isQuestActive(excludeQuestActive)) return false;
        return true;
    }

    public boolean matchesObject(int objTileX, int objTileY) {
        if (tileX != null && tileX != objTileX) return false;
        if (tileY != null && tileY != objTileY) return false;
        return true;
    }
}