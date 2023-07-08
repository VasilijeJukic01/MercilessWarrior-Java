package launcher.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import launcher.view.LauncherView;
import platformer.AppCore;

public class LaunchController implements EventHandler<ActionEvent> {

    private String scale = "1";

    private final TextField tfName;
    private final RadioButton rbYes;
    private final ComboBox<String> cbResolution;

    public LaunchController(TextField tfName, RadioButton rbYes, ComboBox<String> cbResolution) {
        this.tfName = tfName;
        this.rbYes = rbYes;
        this.cbResolution = cbResolution;
    }

    @Override
    public void handle(ActionEvent event) {
        String[] args = new String[3];
        switch (cbResolution.getSelectionModel().getSelectedIndex()) {
            case 0:
                scale = "1";
                break;
            case 1:
                scale = "2";
                break;
            default: break;
        }
        args[0] = scale;
        args[1] = rbYes.isSelected() ? "Yes" : "No";
        args[2] = tfName.getText();
        LauncherView.getInstance().close();
        AppCore.main(args);
    }
}
