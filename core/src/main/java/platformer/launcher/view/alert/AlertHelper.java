package platformer.launcher.view.alert;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

import static platformer.launcher.constants.LauncherConstants.MAIN_ICON_PATH;

public class AlertHelper {

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, message);
    }

    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, message);
    }

    private static void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(AlertHelper.class.getResource(MAIN_ICON_PATH)).toExternalForm()));
        alert.showAndWait();
    }
}
