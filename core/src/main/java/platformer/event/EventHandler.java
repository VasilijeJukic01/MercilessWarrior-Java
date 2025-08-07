package platformer.event;

import platformer.state.types.GameState;

/**
 * Defines a contract for classes that manage continuous, frame-by-frame event logic.
 * <p>
 * The EventHandler interface is designed for logic that must be evaluated in every game update loop.
 * This allows {@link GameState} to manage various event-driven systems without needing
 * to know their internal implementation details; it simply calls {@code continuousUpdate()} on all registered handlers.
 *
 * @see GameState
 */
public interface EventHandler {

    /**
     * Called continuously every frame to handle time-based or ongoing events.
     */
    void continuousUpdate();

    /**
     * Called when the game state is paused.
     * Implementations can use this to stop any ongoing processes or animations.
     */
    default void pause() {}

    /**
     * Called when the game state is unpaused.
     * Implementations can use this to resume any paused processes or animations.
     */
    default void unpause() {}

    /**
     * Resets the handler's internal state to its default values.
     */
    void reset();
}