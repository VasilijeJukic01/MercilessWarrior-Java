package platformer.observer;

/**
 * Defines a contract for classes that manage continuous, frame-by-frame event logic.
 * <p>
 * The EventHandler interface is designed for logic that must be evaluated in every game update loop.
 * This allows {@link platformer.state.GameState} to manage various event-driven systems without needing
 * to know their internal implementation details; it simply calls {@code continuousUpdate()} on all registered handlers.
 *
 * @see Subscriber
 * @see platformer.state.GameState
 */
public interface EventHandler {

    /**
     * Called continuously every frame to handle time-based or ongoing events.
     */
    void continuousUpdate();
}