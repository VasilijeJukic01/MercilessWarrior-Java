package platformer.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that manages actions that can be executed by a key.
 * @param <T> The type of the key that triggers the action.
 */
public class ActionManager<T> {

    private final Map<T, Runnable> actions = new HashMap<>();

    /**
     * Adds an action to the manager.
     * @param key The key that triggers the action.
     * @param action The action to be executed.
     */
    public void addAction(T key, Runnable action) {
        actions.put(key, action);
    }

    /**
     * Executes the action associated with the given key.
     * @param key The key that triggers the action.
     */
    public void execute(T key) {
        Runnable action = actions.get(key);
        if (action != null) {
            action.run();
        }
    }

}
