package platformer.launcher.controller.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import platformer.launcher.core.KeyboardConfigurator;

import java.util.Map;

public class ResetControlsController implements EventHandler<ActionEvent> {

    private final Map<String, TextField> commandFields;

    public ResetControlsController(TextField ... textFields) {
        this.commandFields = CommandFieldsInitializer.initCommandFields(textFields);
    }

    @Override
    public void handle(ActionEvent event) {
        KeyboardConfigurator.getInstance().resetToDefault();
        updateTextFields();
    }

    public void updateTextFields() {
        KeyboardConfigurator configurator = KeyboardConfigurator.getInstance();
        commandFields.forEach((command, field) -> field.setText(configurator.getKeyForCommand(command).getName()));
    }

}