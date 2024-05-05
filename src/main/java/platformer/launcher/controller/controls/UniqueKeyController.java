package platformer.launcher.controller.controls;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class UniqueKeyController {

    private final Map<KeyCode, TextField> keyFields = new LinkedHashMap<>();

    public UniqueKeyController(TextField ... textFields) {
        Arrays.stream(textFields).forEach(field ->
                keyFields.put(KeyCode.getKeyCode(field.getText()), field));
    }

    public void handleKeyInput(KeyCode key, TextField currentField) {
        if (keyFields.containsKey(key)) {
            TextField existingField = keyFields.get(key);
            existingField.clear();
            existingField.setStyle(null);
        }

        currentField.setText(key.getName());
        keyFields.put(key, currentField);
    }
}