package platformer.launcher.controller.controls;

import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides the canonical list of configurable control commands and binds them to UI fields.
 */
public class CommandFieldsInitializer {

    public static final String[] COMMANDS = {
            "Move Left", "Move Right", "Jump", "Dash", "Attack", "Flames", "Fireball", "Shield",
            "Ultimate", "Interact", "Quest", "Transform", "Inventory", "Perks", "Accept", "Decline",
            "Minimap", "Pause", "Look Down", "QuickUse1", "QuickUse2", "QuickUse3", "QuickUse4"
    };

    public static Map<String, TextField> initCommandFields(TextField... fields) {
        Map<String, TextField> commandFields = new LinkedHashMap<>();
        for (int i = 0; i < COMMANDS.length && i < fields.length; i++) {
            commandFields.put(COMMANDS[i], fields[i]);
        }
        return commandFields;
    }
}