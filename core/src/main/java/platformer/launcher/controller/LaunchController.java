package platformer.launcher.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import platformer.AppCore;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.launcher.core.KeyboardConfigurator;
import platformer.launcher.view.LauncherView;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static platformer.constants.FilePaths.KEYBOARD_CONFIG_PATH;
import static platformer.launcher.Config.SCALING_FACTOR;

public class LaunchController implements EventHandler<ActionEvent> {

    private final LauncherView launcherView;

    private String scale = "1";

    private final TextField tfName;
    private final PasswordField passwordField;
    private final RadioButton rbYes;
    private final ComboBox<String> cbResolution;
    private final CheckBox cbFullScreen;

    public LaunchController(LauncherView launcherView, TextField tfName, PasswordField passwordField, RadioButton rbYes, ComboBox<String> cbResolution, CheckBox cbFullScreen) {
        this.launcherView = launcherView;
        this.tfName = tfName;
        this.passwordField = passwordField;
        this.rbYes = rbYes;
        this.cbResolution = cbResolution;
        this.cbFullScreen = cbFullScreen;
    }

    private void writeMapToFile(Map<String, KeyCode> map) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(KEYBOARD_CONFIG_PATH))) {
            for (Map.Entry<String, KeyCode> entry : map.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            Logger.getInstance().notify("Saving keyboard configuration failed!", Message.ERROR);
        }
    }

    @Override
    public void handle(ActionEvent event) {
        String[] args = new String[4];

        Map<String, KeyCode> commandKeyMap = KeyboardConfigurator.getInstance().getCommandKeyMap();
        writeMapToFile(commandKeyMap);

        switch (cbResolution.getSelectionModel().getSelectedIndex()) {
            case 0:
                scale = "1";
                break;
            case 1:
                scale = "1.5";
                break;
            case 2:
                scale = "2";
                break;
            default: break;
        }
        SCALING_FACTOR = Float.parseFloat(scale);
        args[0] = rbYes.isSelected() ? "Yes" : "No";
        args[1] = tfName.getText();
        args[2] = passwordField.getText();
        args[3] = cbFullScreen.isSelected() ? "Yes" : "No";
        launcherView.close();
        AppCore.main(args);
    }
}
