package platformer.model.levels.metadata;

import lombok.Getter;
import lombok.Setter;
import platformer.model.levels.LvlTriggerType;

/**
 * Defines a directional or coordinate-bounded exit transition from one room to another.
 */
@Getter
@Setter
public class RoomExitMetadata {

    private LvlTriggerType trigger;
    private String targetLevel;
    private LvlTriggerType targetSpawn;

    // Optional tile-coordinate bounds to disambiguate multiple exits of the same trigger type in one room
    private Integer minX, maxX;
    private Integer minY, maxY;

    /**
     * Checks whether a triggered tile coordinate (tileX, tileY) matches this exit rule.
     */
    public boolean matches(LvlTriggerType triggerType, int tileX, int tileY) {
        if (this.trigger != triggerType) return false;
        if (minX != null && tileX < minX) return false;
        if (maxX != null && tileX > maxX) return false;
        if (minY != null && tileY < minY) return false;
        if (maxY != null && tileY > maxY) return false;
        return true;
    }

    public boolean isBounded() {
        return minX != null || maxX != null || minY != null || maxY != null;
    }
}