package platformer.physics;

import java.awt.geom.Rectangle2D;

/**
 * A marker interface for any object that can be considered a source of damage to an entity.
 * Implementing this interface allows an object to be passed into methods, enabling the entity to react appropriately
 * (e.g., determine knockback direction) without needing to know the specific type of the damage source.
 */
public interface DamageSource {

    /**
     * Gets the bounding box of the damage source.
     * This is used to determine the direction of knockback and other positional effects.
     *
     * @return The Rectangle2D bounds of the object causing the damage.
     */
    Rectangle2D.Double getHitBox();
}
