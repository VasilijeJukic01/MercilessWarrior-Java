package platformer.launcher.view.styler;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        List<String> tips = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(resourcePath))));

            StringBuilder jsonContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("//")) jsonContent.append(line);
            }

            String content = jsonContent.toString().trim();

            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1);
                String[] entries = content.split(",");

                for (String entry : entries) {
                    String cleanEntry = entry.trim();
                    if (cleanEntry.startsWith("\"") && cleanEntry.endsWith("\"")) {
                        cleanEntry = cleanEntry.substring(1, cleanEntry.length() - 1);
                        tips.add(cleanEntry);
                    }
                }
            }

        } catch (IOException e) {
            tips.add("Blocking enemy attacks at the right moment can grant power!");
        }
        return tips;
    }

    @Override
    public void applyStyleClass(Node node, String styleClass) {
        node.getStyleClass().add(styleClass);
    }

    @Override
    public void applyStyleClass(String styleClass, Node... nodes) {
        for (Node node : nodes) {
            node.getStyleClass().add(styleClass);
        }
    }
}
