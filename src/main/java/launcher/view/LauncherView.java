package launcher.view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import launcher.controller.LaunchController;

import java.io.File;

public class LauncherView extends Stage {

    public static LauncherView instance = null;

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

    private final ToggleGroup tgCheats = new ToggleGroup();

    private final Image BackgroundImg =  new Image(new File("src/main/resources/images/launcherBG.jpg").toURI().toString());
    private final BackgroundImage backgroundImage = new BackgroundImage(
            BackgroundImg,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(100,800, true, true,true,true)
    );
    Background background = new Background(backgroundImage);

    private LauncherView() {}

    private void init() {

        // Style
        lbLogo.setStyle("-fx-font-weight: bold;");
        lbName.setStyle("-fx-font-weight: bold;");
        lbYes.setStyle("-fx-font-weight: bold;");
        lbNo.setStyle("-fx-font-weight: bold;");
        lnEnableCheats.setStyle("-fx-font-weight: bold;");
        lbResolution.setStyle("-fx-font-weight: bold;");

        // Setup
        tgCheats.getToggles().addAll(rbEnableCheatsYes, rbEnableCheatsNo);
        cbResolution.setItems(FXCollections.observableArrayList("832x448", "1664x896"));
        cbResolution.getSelectionModel().select(1);
        rbEnableCheatsNo.setSelected(true);

        // Components init
        root.getChildren().addAll(lbLogo, lbSpacing1, new DefaultHBox(Pos.CENTER, lbName, tfName),
                new DefaultHBox(Pos.CENTER, lnEnableCheats, lbYes, rbEnableCheatsYes, lbNo, rbEnableCheatsNo));
        root.getChildren().addAll(new DefaultHBox(Pos.CENTER, lbResolution, cbResolution), lbSpacing2, btnLaunch);

        btnLaunch.setOnAction(new LaunchController(tfName, rbEnableCheatsYes, rbEnableCheatsNo, cbResolution));

        // Window
        super.setTitle("MW Launcher");
        super.getIcons().add(new Image("file:src/main/resources/images/icon.png"));
        root.setBackground(background);
        super.setResizable(false);
        super.setScene(new Scene(root, 300, 400));
    }

    public static LauncherView getInstance() {
        if (instance == null) {
            instance = new LauncherView();
            instance.init();
        }
        return instance;
    }
}
