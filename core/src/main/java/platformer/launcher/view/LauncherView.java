package platformer.launcher.view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import platformer.launcher.controller.LaunchController;
import platformer.launcher.view.component.DefaultHBox;
import platformer.launcher.view.component.DefaultVBox;
import platformer.launcher.view.styler.FXStyler;
import platformer.launcher.view.styler.Styler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static platformer.launcher.constants.LauncherConstants.*;

@SuppressWarnings({"FieldCanBeLocal"})
public class LauncherView extends BaseView {

    // Components
    private final DefaultVBox root = new DefaultVBox(Pos.CENTER);

    private final Label lbLogo = new Label("Merciless Warrior Launcher");
    private final Label lbSpacing1 = new Label();
    private final Label lbName = new Label("Name:      ");
    private final Label lbPassword = new Label("Password: ");

    private final TextField tfName = new TextField();
    private final PasswordField tfPassword = new PasswordField();
    private final Label lnEnableCheats = new Label("Enable cheats: ");
    private final Label lbYes = new Label("Yes");
    private final RadioButton rbEnableCheatsYes = new RadioButton();
    private final Label lbNo = new Label("No");
    private final RadioButton rbEnableCheatsNo = new RadioButton();
    private final Label lbResolution = new Label("Resolution: ");
    private final ComboBox<String> cbResolution = new ComboBox<>();
    private final Label lbFullScreen = new Label("Full Screen: ");
    private final CheckBox cbFullScreen = new CheckBox();

    private final Label lbSpacing2 = new Label();
    private final Button btnLaunch = new Button("Launch");
    private final Button btnRegister = new Button("Register");
    private final Button btnControls = new Button("Controls");
    private final Button btnExit = new Button("Exit");

    private final ToggleGroup tgCheats = new ToggleGroup();

    public LauncherView() {
        initModality(Modality.APPLICATION_MODAL);
        super.loadImages();
        init();
    }

    private void init() {
        super.initScene(root, SCENE_WID, SCENE_HEI, "MW Launcher");
        initComponents();
        initRoot();
        initButtons();
        initStyles(super.getScene());
    }

    private void initStyles(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/launcher.css")).toExternalForm());

        Styler styler = new FXStyler();
        List<Label> labels = Arrays.asList(lbLogo, lbName, lbPassword, lbYes, lbNo, lnEnableCheats, lbResolution, lbFullScreen);
        labels.forEach(styler::setBoldStyle);
    }

    private void initComponents() {
        ImageView imgLogo = new ImageView(super.getLogo());
        imgLogo.setFitWidth(LOGO_WID);
        imgLogo.setFitHeight(LOGO_HEI);

        root.getChildren().add(imgLogo);
        tgCheats.getToggles().addAll(rbEnableCheatsYes, rbEnableCheatsNo);
        cbResolution.setItems(FXCollections.observableArrayList("832x448", "1248x672", "1664x896"));
        cbResolution.getSelectionModel().select(2);
        rbEnableCheatsNo.setSelected(true);
    }

    private void initRoot() {
        root.getChildren().addAll(lbSpacing1, new DefaultHBox(Pos.CENTER, lbName, tfName),
                new DefaultHBox(Pos.CENTER, lbPassword, tfPassword),
                new DefaultHBox(Pos.CENTER, lnEnableCheats, lbYes, rbEnableCheatsYes, lbNo, rbEnableCheatsNo));
        root.getChildren().addAll(new DefaultHBox(Pos.CENTER, lbResolution, cbResolution),
                new DefaultHBox(Pos.CENTER, lbFullScreen, cbFullScreen),
                lbSpacing2,
                new DefaultHBox(Pos.CENTER, btnLaunch, btnRegister, btnControls), btnExit);
    }

    private void initButtons() {
        btnLaunch.setOnAction(new LaunchController(this, tfName, tfPassword, rbEnableCheatsYes, cbResolution, cbFullScreen));
        btnRegister.setOnAction(e -> new RegisterView(this).show());
        btnControls.setOnAction(e -> new ControlsView().show());
        btnExit.setOnAction(e -> System.exit(0));
    }

}
