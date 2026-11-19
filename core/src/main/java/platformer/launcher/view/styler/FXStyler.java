package platformer.launcher.view.styler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import platformer.animation.AssetManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class FXStyler implements Styler{

    @Override
    public void setBoldStyle(Object o) {
        if (!(o instanceof Label)) return;
        ((Label)o).getStyleClass().add("black");
        ((Label)o).getStyleClass().add("bold");
    }

    @Override
    public void applyStylesheet(Scene scene, String cssPath) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
    }

    @Override
    public void styleLoadingComponents(ImageView logoView, Label loadingLabel, ProgressBar progressBar, Label statusLabel, Label tipLabel) {
        logoView.getStyleClass().add("game-logo");
        loadingLabel.getStyleClass().add("loading-label");
        progressBar.getStyleClass().add("loading-progress-bar");
        statusLabel.getStyleClass().add("status-label");
        tipLabel.getStyleClass().add("tip-label");
    }

    @Override
    public void configureLogoView(ImageView logoView, double width) {
        logoView.setFitWidth(width);
        logoView.setPreserveRatio(true);
    }

    @Override
    public void setupPulsingEffect(Label label) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), label);
        fadeTransition.setFromValue(0.7);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(Timeline.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
    }

    @Override
    public FadeTransition createFadeTransition(Node node, double duration, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    @Override
    public List<String> loadTipsFromResource(String resourcePath) {
        try (InputStream is = AssetManager.getInstance().getResourceAsStream(resourcePath);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> loaded = new Gson().fromJson(reader, listType);
            if (loaded != null && !loaded.isEmpty()) {
                return loaded;
            }
        } catch (Exception ignored) { }
        return List.of("Blocking enemy attacks at the right moment can grant power!");
    }

    @Override
    public void applyStyleClass(Node node, String styleClass) {
        node.getStyleClass().add(styleClass);
    }
}
