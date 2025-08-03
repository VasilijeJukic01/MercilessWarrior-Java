package platformer.event;

@FunctionalInterface
public interface EventListener<T extends Event> {

    /**
     * Handles the published event.
     *
     * @param event The event object containing relevant data.
     */
    void onEvent(T event);

}