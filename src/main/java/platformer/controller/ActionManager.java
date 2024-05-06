package platformer.controller;

import java.util.HashMap;
import java.util.Map;

public class ActionManager<T> {

    private final Map<T, Runnable> actions = new HashMap<>();

    public void addAction(T key, Runnable action) {
        actions.put(key, action);
    }

    public void execute(T key) {
        Runnable action = actions.get(key);
        if (action != null) {
            action.run();
        }
    }

}
