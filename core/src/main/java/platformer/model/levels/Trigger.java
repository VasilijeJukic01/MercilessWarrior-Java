package platformer.model.levels;

import java.awt.geom.Rectangle2D;

/**
 * A lightweight spatial volume used for level logic (exits, AI boundaries, kill zones).
 */
public record Trigger(Rectangle2D.Double bounds, LvlTriggerType type) {}