package platformer.event.events;

import platformer.event.Event;
import platformer.model.gameObjects.objects.Container;

/**
 * An event published when a crate (or other container) is destroyed by the player.
 *
 * @param container The container instance that was destroyed.
 */
public record CrateDestroyedEvent(Container container) implements Event {}