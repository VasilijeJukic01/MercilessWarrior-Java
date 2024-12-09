package platformer.launcher.core;

import javafx.scene.input.KeyCode;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeyboardConfigurator {

    private static volatile KeyboardConfigurator instance = null;

    private final Map<String, KeyCode> commandKeyMap;
    private final Map<String, KeyCode> originalCommandKeyMap;

    private KeyboardConfigurator() {
        commandKeyMap = new LinkedHashMap<>();
        originalCommandKeyMap = new LinkedHashMap<>();
        initDefaultKeys();
    }

    public static KeyboardConfigurator getInstance() {
        if (instance == null) {
            synchronized (KeyboardConfigurator.class) {
                if (instance == null) {
                    instance = new KeyboardConfigurator();
                }
            }
        }
        return instance;
    }

    private void initDefaultKeys() {
        commandKeyMap.put("Move Left",  KeyCode.LEFT);
        commandKeyMap.put("Move Right", KeyCode.RIGHT);
        commandKeyMap.put("Jump",       KeyCode.UP);
        commandKeyMap.put("Dash",       KeyCode.V);
        commandKeyMap.put("Attack",     KeyCode.X);
        commandKeyMap.put("Flames",     KeyCode.C);
        commandKeyMap.put("Fireball",   KeyCode.Z);
        commandKeyMap.put("Shield",     KeyCode.S);
        commandKeyMap.put("Interact",   KeyCode.F);
        commandKeyMap.put("Quest",      KeyCode.O);
        commandKeyMap.put("Transform",  KeyCode.Q);
        commandKeyMap.put("Inventory",  KeyCode.I);
        commandKeyMap.put("Accept",     KeyCode.Y);
        commandKeyMap.put("Decline",    KeyCode.N);
        commandKeyMap.put("Minimap",    KeyCode.M);
        commandKeyMap.put("Pause",      KeyCode.ESCAPE);

        originalCommandKeyMap.putAll(commandKeyMap);
    }

    public void setKeyForCommand(String command, KeyCode key) {
        commandKeyMap.put(command, key);
    }

    public KeyCode getKeyForCommand(String command) {
        return commandKeyMap.get(command);
    }

    public void resetToDefault() {
        commandKeyMap.clear();
        commandKeyMap.putAll(originalCommandKeyMap);
    }

    public Map<String, KeyCode> getCommandKeyMap() {
        return commandKeyMap;
    }
}
