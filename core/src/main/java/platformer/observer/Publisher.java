package platformer.observer;

/**
 * Defines the contract for the 'Publisher' in the Observer design pattern.
 * <p>
 * A Publisher maintains a list of subscribers and provides a mechanism to notify them of any state changes or events.
 *
 * @see Subscriber
 */
public interface Publisher {

    /**
     * Registers a new Subscriber to receive notifications from this Publisher.
     *
     * @param s The Subscriber to add to the notification list.
     */
    void addSubscriber(Subscriber s);

    /**
     * Removes a Subscriber from the notification list. After being removed, the Subscriber will no longer receive updates from this Publisher.
     *
     * @param s The Subscriber to remove from the notification list.
     */
    void removeSubscriber(Subscriber s);

    /**
     * Broadcasts an event to all registered subscribers by calling their {@code update} method.
     *
     * @param <T> The type of the data being passed in the event.
     * @param o   A list containing the event data. The structure of this data is dependent on the specific implementation and event type.
     */
    <T> void notify(T ... o);

}
