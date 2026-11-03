package platformer.model.levels.metadata;

import lombok.Getter;
import lombok.Setter;
import platformer.model.levels.LvlTriggerType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single room node in the world graph, including its minimap offset and exit links.
 */
@Getter
@Setter
public class RoomNodeMetadata {

    private Integer mapX, mapY;
    private List<RoomExitMetadata> exits = new ArrayList<>();

    /**
     * Finds the matching exit for the given trigger type and tile coordinates.
     * Prioritizes bounded rules (specific X/Y ranges) before falling back to unbounded rules.
     */
    public RoomExitMetadata findExit(LvlTriggerType triggerType, int tileX, int tileY) {
        for (RoomExitMetadata exit : exits) {
            if (exit.isBounded() && exit.matches(triggerType, tileX, tileY)) {
                return exit;
            }
        }
        for (RoomExitMetadata exit : exits) {
            if (!exit.isBounded() && exit.matches(triggerType, tileX, tileY)) {
                return exit;
            }
        }
        return null;
    }
}