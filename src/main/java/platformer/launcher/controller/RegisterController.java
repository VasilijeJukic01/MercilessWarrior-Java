package platformer.launcher.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import platformer.launcher.view.RegisterView;
import platformer.launcher.view.alert.AlertHelper;
import platformer.core.LauncherPrompt;
import platformer.database.bridge.Database;

public class RegisterController implements EventHandler<ActionEvent> {

    private final TextField tfName;
    private final PasswordField tfPassword;
    private final PasswordField tfConfirmPassword;
    private final RegisterView registerView;

    public RegisterController(TextField tfName, PasswordField tfPassword, PasswordField tfConfirmPassword, RegisterView registerView) {
        this.tfName = tfName;
        this.tfPassword = tfPassword;
        this.tfConfirmPassword = tfConfirmPassword;
        this.registerView = registerView;
    }

    @Override
    public void handle(ActionEvent event) {
        if (tfName.getText().isEmpty() || tfPassword.getText().isEmpty() || tfConfirmPassword.getText().isEmpty()) {
            AlertHelper.showError("All fields must be filled in!");
            return;
        }
        if (!tfPassword.getText().equals(tfConfirmPassword.getText())) {
            AlertHelper.showError("Passwords do not match!");
            return;
        }

        Database database = new Database(new LauncherPrompt(tfName.getText(), tfPassword.getText(), false));
        int status = database.createAccount(tfName.getText(), tfPassword.getText());

        switch (status) {
            case 0:
                AlertHelper.showInfo("Registration successful!");
                registerView.close();
                break;
            case 1:
                AlertHelper.showError("There was an error registering the user.");
                break;
            case 2:
                AlertHelper.showError("User already exists with that name!");
                break;
            default:
                AlertHelper.showError("Error: Unknown error occurred.");
                break;
        }
    }

}
