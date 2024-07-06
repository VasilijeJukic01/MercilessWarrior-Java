package platformer.launcher.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import platformer.launcher.controller.RegisterController;
import platformer.launcher.view.styler.FXStyler;
import platformer.launcher.view.styler.Styler;

import java.util.Objects;

import static platformer.launcher.constants.LauncherConstants.*;

public class RegisterView extends Stage {

    // Components
    private final DefaultVBox root = new DefaultVBox(Pos.CENTER);

    private final Label lbName = new Label("Name: ");
    private final TextField tfName = new TextField();

    private final Label lbPassword = new Label("Password: ");
    private final PasswordField tfPassword = new PasswordField();

    private final Label lbConfirmPassword = new Label("Confirm Password: ");
    private final PasswordField tfConfirmPassword = new PasswordField();

    private final Button btnRegister = new Button("Register");
    private final Button btnCancel = new Button("Cancel");

    // Images
    private Image launcherIcon;
    private Image logo;

    public RegisterView(LauncherView launcherView) {
        initModality(Modality.APPLICATION_MODAL);
        loadImages();
        root.setBackground(launcherView.getLauncherBackground());
        init();
    }

    private void loadImages() {
        this.launcherIcon = new Image(Objects.requireNonNull(getClass().getResource("/images/icon.png")).toExternalForm());
        this.logo = new Image(Objects.requireNonNull(getClass().getResource("/images/menu/menuLogo.png")).toExternalForm());
    }

    private void init() {
        initScene();
        initRoot();
        initButtons();
        initStyles(super.getScene());
    }

    private void initStyles(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());

        Styler styler = new FXStyler();
        styler.setBoldStyle(lbName);
        styler.setBoldStyle(lbPassword);
        styler.setBoldStyle(lbConfirmPassword);
    }

    private void initRoot() {
        ImageView imgLogo = new ImageView(logo);
        imgLogo.setFitWidth(LOGO_WID);
        imgLogo.setFitHeight(LOGO_HEI);

        root.getChildren().add(imgLogo);

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(lbName, tfName, lbPassword, tfPassword, lbConfirmPassword, tfConfirmPassword, btnRegister, btnCancel);

    }

    private void initButtons() {
        btnRegister.setOnAction(new RegisterController(tfName, tfPassword, tfConfirmPassword, this));
        btnCancel.setOnAction(e -> super.close());
    }

    private void initScene() {
        Scene scene = new Scene(root, SCENE_WID, SCENE_HEI);
        super.setTitle("Register");
        super.getIcons().add(launcherIcon);
        super.setResizable(false);
        super.setScene(scene);
    }

}
