package platformer.event.events;

import platformer.event.Event;
import platformer.model.gameObjects.objects.Brick;

/**
 * An event published when a brick is destroyed in the game world.
 *
 * @param brick The Brick object that was destroyed.
 */
public record BrickDestroyedEvent(Brick brick) implements Event {}