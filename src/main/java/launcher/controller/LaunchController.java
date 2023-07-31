package launcher.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import launcher.view.LauncherView;
import platformer.AppCore;

import static launcher.Config.SCALING_FACTOR;

public class LaunchController implements EventHandler<ActionEvent> {

    private final LauncherView launcherView;

    private String scale = "1";

    private final TextField tfName;
    private final RadioButton rbYes;
    private final ComboBox<String> cbResolution;

    public LaunchController(LauncherView launcherView, TextField tfName, RadioButton rbYes, ComboBox<String> cbResolution) {
        this.launcherView = launcherView;
        this.tfName = tfName;
        this.rbYes = rbYes;
        this.cbResolution = cbResolution;
    }

    @Override
    public void handle(ActionEvent event) {
        String[] args = new String[2];
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
        launcherView.close();
        AppCore.main(args);
    }
}
