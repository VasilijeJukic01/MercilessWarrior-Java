package launcher.view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import launcher.controller.LaunchController;
import launcher.view.styler.FXStyler;
import launcher.view.styler.Styler;

import java.util.Objects;

@SuppressWarnings({"FieldCanBeLocal"})
public class LauncherView extends Stage {

    // Components
    private final DefaultVBox root = new DefaultVBox(Pos.CENTER);

    private final Label lbLogo = new Label("Merciless Warrior Launcher");
    private final Label lbSpacing1 = new Label();
    private final Label lbName = new Label("Name: ");

    private final TextField tfName = new TextField();
    private final Label lnEnableCheats = new Label("Enable cheats: ");
    private final Label lbYes = new Label("Yes");
    private final RadioButton rbEnableCheatsYes = new RadioButton();
    private final Label lbNo = new Label("No");
    private final RadioButton rbEnableCheatsNo = new RadioButton();
    private final Label lbResolution = new Label("Resolution: ");
    private final ComboBox<String> cbResolution = new ComboBox<>();

    private final Label lbSpacing2 = new Label();
    private final Button btnLaunch = new Button("Launch");
    private final Button btnExit = new Button("Exit");

    private final ToggleGroup tgCheats = new ToggleGroup();

    // Size
    private final int BG_WID = 100, BG_HEI = 800;
    private final int LOGO_WID = 200, LOGO_HEI = 100;
    private final int SCENE_WID = 300, SCENE_HEI = 400;

    // Images
    private Image launcherIcon;
    private Image backgroundImg;
    private BackgroundImage background;
    private Background launcherBackground;
    private Image logo;

    public LauncherView() {
        loadImages();
        init();
    }

    private void loadImages() {
        this.launcherIcon = new Image(Objects.requireNonNull(getClass().getResource("/images/icon.png")).toExternalForm());
        this.backgroundImg = new Image(Objects.requireNonNull(getClass().getResource("/images/launcherBG.jpg")).toExternalForm());
        this.background = new BackgroundImage(
                backgroundImg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BG_WID,BG_HEI, true, true,true,true)
        );
        this.launcherBackground = new Background(background);
        this.logo = new Image(Objects.requireNonNull(getClass().getResource("/images/menu/menuLogo.png")).toExternalForm());
    }

    private void init() {
        initScene();
        initComponents();
        initRoot();
        initButtons();
        initStyles(super.getScene());
    }

    private void initStyles(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());

        Styler styler = new FXStyler();
        styler.setBoldStyle(lbLogo);
        styler.setBoldStyle(lbName);
        styler.setBoldStyle(lbYes);
        styler.setBoldStyle(lbNo);
        styler.setBoldStyle(lnEnableCheats);
        styler.setBoldStyle(lbResolution);
    }

    private void initComponents() {
        ImageView imgLogo = new ImageView(logo);
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
                new DefaultHBox(Pos.CENTER, lnEnableCheats, lbYes, rbEnableCheatsYes, lbNo, rbEnableCheatsNo));
        root.getChildren().addAll(new DefaultHBox(Pos.CENTER, lbResolution, cbResolution), lbSpacing2, btnLaunch, btnExit);
    }

    private void initButtons() {
        btnLaunch.setOnAction(new LaunchController(this, tfName, rbEnableCheatsYes, cbResolution));
        btnExit.setOnAction(e -> System.exit(0));
    }

    private void initScene() {
        Scene scene = new Scene(root, SCENE_WID, SCENE_HEI);
        this.root.setBackground(launcherBackground);
        super.setTitle("MW Launcher");
        super.getIcons().add(launcherIcon);
        super.setResizable(false);
        super.setScene(scene);
    }
}
