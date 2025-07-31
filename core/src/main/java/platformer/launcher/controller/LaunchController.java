package platformer.launcher.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.launcher.core.KeyboardConfigurator;
import platformer.launcher.view.LauncherView;
import platformer.launcher.view.LoadingView;
import platformer.utils.loading.PathManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

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
        String configPath = PathManager.getConfigPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configPath))) {
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
        Map<String, KeyCode> commandKeyMap = KeyboardConfigurator.getInstance().getCommandKeyMap();
        writeMapToFile(commandKeyMap);

        switch (cbResolution.getSelectionModel().getSelectedIndex()) {
            case 0: scale = "1"; break;
            case 1: scale = "1.5"; break;
            case 2: scale = "2"; break;
            default: break;
        }
        System.setProperty("game.scale", scale);

        // Get player config
        String playerName = tfName.getText();
        String password = passwordField.getText();

        // Close the launcher
        launcherView.close();

        LoadingView loadingView = new LoadingView(
            playerName,
            password,
            rbYes.isSelected(),
            cbFullScreen.isSelected()
        );
        loadingView.show();
    }
}
