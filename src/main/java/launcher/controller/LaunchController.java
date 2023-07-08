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
    private final RadioButton rbNo;
    private final ComboBox<String> cbResolution;

    public LaunchController(TextField tfName, RadioButton rbYes, RadioButton rbNo, ComboBox<String> cbResolution) {
        this.tfName = tfName;
        this.rbYes = rbYes;
        this.rbNo = rbNo;
        this.cbResolution = cbResolution;
    }

    @Override
    public void handle(ActionEvent event) {
        switch (cbResolution.getSelectionModel().getSelectedIndex()) {
            case 0:
                scale = "1";
                break;
            case 1:
                scale = "2";
                break;
            default: break;
        }
        LauncherView.getInstance().close();
        String[] args = new String[1];
        args[0] = scale;
        AppCore.main(args);
    }
}
