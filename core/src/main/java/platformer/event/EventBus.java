package platformer.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An application-wide Event Bus for decoupled communication between components.
 * This implementation is synchronous, meaning events are handled immediately on the publisher's thread.
 */
public class EventBus {

    private static final EventBus instance = new EventBus();
    private final Map<Class<? extends Event>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return instance;
    }

    /**
     * Registers a listener for a specific type of event.
     *
     * @param eventClass The class of the event to listen for.
     * @param listener   The listener that will handle the event.
     * @param <T>        The type of the event.
     */
    public <T extends Event> void register(Class<T> eventClass, EventListener<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Publishes an event to all registered listeners for that event type.
     *
     * @param event The event object to publish.
     * @param <T>   The type of the event.
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        Class<T> eventClass = (Class<T>) event.getClass();
        List<EventListener<?>> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }
}