package platformer.launcher.view.styler;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

import java.util.List;

public interface Styler {

    void setBoldStyle(Object o);

    /**
     * Applies the specified CSS file to a scene
     */
    void applyStylesheet(Scene scene, String cssPath);

    /**
     * Applies style classes to loading screen components
     */
    void styleLoadingComponents(ImageView logoView, Label loadingLabel, ProgressBar progressBar, Label statusLabel, Label tipLabel);

    /**
     * Configures the logo image view with standard dimensions
     */
    void configureLogoView(ImageView logoView, double width);

    /**
     * Creates a pulsing animation effect for a label
     */
    void setupPulsingEffect(Label label);

    /**
     * Creates a fade transition effect
     */
    FadeTransition createFadeTransition(Node node, double duration, double fromValue, double toValue);

    /**
     * Loads gameplay tips from a resource file
     */
    List<String> loadTipsFromResource(String resourcePath);

    /**
     * Applies a style class to a node
     */
    void applyStyleClass(Node node, String styleClass);

    /**
     * Applies a style class to multiple nodes
     */
    void applyStyleClass(String styleClass, Node... nodes);
}
