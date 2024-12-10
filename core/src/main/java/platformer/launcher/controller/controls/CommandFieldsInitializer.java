package platformer.launcher.controller.controls;

import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommandFieldsInitializer {

    public static Map<String, TextField> initCommandFields(TextField ... fields) {
        Map<String, TextField> commandFields = new LinkedHashMap<>();
        commandFields.put("Move Left",  fields[0]);
        commandFields.put("Move Right", fields[1]);
        commandFields.put("Jump",       fields[2]);
        commandFields.put("Dash",       fields[3]);
        commandFields.put("Attack",     fields[4]);
        commandFields.put("Flames",     fields[5]);
        commandFields.put("Fireball",   fields[6]);
        commandFields.put("Shield",     fields[7]);
        commandFields.put("Interact",   fields[8]);
        commandFields.put("Quest",      fields[9]);
        commandFields.put("Transform",  fields[10]);
        commandFields.put("Inventory",  fields[11]);
        commandFields.put("Accept",     fields[12]);
        commandFields.put("Decline",    fields[13]);
        commandFields.put("Minimap",    fields[14]);
        commandFields.put("Pause",      fields[15]);

        return commandFields;
    }

}
