package platformer.observer;

/**
 * Defines the contract for the 'Subscriber' in the Observer design pattern.
 * <p>
 * A Subscriber registers with a {@link Publisher} to receive notifications about state changes or events.
 * When an event occurs, the Publisher calls the {@code update} method on all of its registered subscribers.
 *
 * @see Publisher
 */
public interface Subscriber {

    /**
     * This method is called by a {@link Publisher} to notify the Subscriber of an event.
     *
     * @param <T> The type of the data being passed in the event.
     * @param o   A list containing the event data sent by the Publisher. Subscribers must know the expected structure of this data to correctly process the event.
     */
    <T> void update(T ... o);

}
