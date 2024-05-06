package platformer.launcher.controller.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import platformer.launcher.core.KeyboardConfigurator;
import platformer.launcher.view.alert.AlertHelper;

import java.util.Map;

public class ApplyControlsController implements EventHandler<ActionEvent> {

    private final Map<String, TextField> commandFields;

    public ApplyControlsController(TextField ... textFields) {
        this.commandFields = CommandFieldsInitializer.initCommandFields(textFields);
    }

    @Override
    public void handle(ActionEvent event) {
        if (areAllFieldsFilled()) {
            commandFields.forEach((command, field) -> {
                String keyText = field.getText();
                KeyboardConfigurator.getInstance().setKeyForCommand(command, KeyCode.getKeyCode(keyText));
            });
        }
        else {
            AlertHelper.showError("All fields must be filled before applying.");
        }
    }

    private boolean areAllFieldsFilled() {
        return commandFields.values().stream().noneMatch(field -> field.getText().isEmpty());
    }

}
