package platformer.launcher.view;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import platformer.launcher.controller.LoadingController;
import platformer.launcher.view.styler.FXStyler;
import platformer.launcher.view.styler.Styler;
import platformer.utils.loading.LoadingProgressTracker;

import java.util.List;
import java.util.Random;

import static platformer.launcher.constants.LauncherConstants.*;

public class LoadingView extends BaseView {

    private final BorderPane root = new BorderPane();
    private final VBox contentBox = new VBox(15);
    private final Label loadingLabel = new Label("LOADING MERCILESS WARRIOR");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("Initializing");
    private final Label tipLabel = new Label("");
    private final ImageView logoView = new ImageView();

    private final List<String> loadingTips;
    private final Random random = new Random();
    private final Styler styler = new FXStyler();

    private final LoadingController controller;

    private int currentTipIndex = 0;

    public LoadingView(String playerName, String password, boolean enableCheats, boolean fullScreen) {
        this.loadingTips = styler.loadTipsFromResource(LOADING_TIPS_PATH);

        if (!loadingTips.isEmpty()) tipLabel.setText(loadingTips.get(0));

        this.controller = new LoadingController(this, playerName, password, enableCheats, fullScreen);

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        super.loadImages();
        init();
    }

    private void init() {
        super.initScene(root, LOADING_WID, LOADING_HEI, "Loading");
        initComponents();
        styler.applyStylesheet(getScene(), STYLES_PATH);
        controller.startLoadingProcess();
        rotateTips();
    }

    private void initComponents() {
        logoView.setImage(getLogo());
        styler.configureLogoView(logoView, 400);

        styler.styleLoadingComponents(logoView, loadingLabel, progressBar, statusLabel, tipLabel);

        VBox bottomBox = new VBox(tipLabel);
        styler.applyStyleClass(bottomBox, "bottom-box");
        styler.applyStyleClass(contentBox, "content-box");
        contentBox.getChildren().addAll(logoView, loadingLabel, progressBar, statusLabel);
        styler.setupPulsingEffect(loadingLabel);

        root.setCenter(contentBox);
        root.setBottom(bottomBox);
        root.setBackground(getLauncherBackground());

        progressBar.progressProperty().bind(LoadingProgressTracker.getInstance().progressProperty());
        statusLabel.textProperty().bind(LoadingProgressTracker.getInstance().statusProperty());
    }

    private void rotateTips() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            FadeTransition fadeOut = styler.createFadeTransition(tipLabel, 500, 1.0, 0.0);
            fadeOut.setOnFinished(e -> {
                if (!loadingTips.isEmpty()) {
                    int newIndex;
                    do {
                        newIndex = random.nextInt(loadingTips.size());
                    } while (newIndex == currentTipIndex && loadingTips.size() > 1);

                    currentTipIndex = newIndex;
                    tipLabel.setText(loadingTips.get(currentTipIndex));
                }
                FadeTransition fadeIn = styler.createFadeTransition(tipLabel, 500, 0.0, 1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Public method to close the view, called by the controller when loading completes
     */
    public void closeView() {
        this.close();
    }
}
