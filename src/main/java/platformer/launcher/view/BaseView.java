package platformer.launcher.view;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

import static platformer.launcher.constants.LauncherConstants.BG_HEI;
import static platformer.launcher.constants.LauncherConstants.BG_WID;

public abstract class BaseView extends Stage {

    // Images
    protected Image launcherIcon;
    protected Image backgroundImg;
    protected BackgroundImage background;
    protected Background launcherBackground;
    private Image logo;

    protected void loadImages() {
        this.launcherIcon = new Image(Objects.requireNonNull(getClass().getResource("/images/icon.png")).toExternalForm());
        this.backgroundImg = new Image(Objects.requireNonNull(getClass().getResource("/images/launcherBG.jpg")).toExternalForm());
        this.background = new BackgroundImage(
                backgroundImg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BG_WID, BG_HEI, true, true,true,true)
        );
        this.logo = new Image(Objects.requireNonNull(getClass().getResource("/images/menu/menuLogo.png")).toExternalForm());
        this.launcherBackground = new Background(background);
    }

    protected void initScene(Pane root, int width, int height, String title) {
        Scene scene = new Scene(root, width, height);
        root.setBackground(launcherBackground);
        super.setTitle(title);
        super.getIcons().add(launcherIcon);
        super.setResizable(false);
        super.setScene(scene);
    }

    public Background getLauncherBackground() {
        return launcherBackground;
    }

    public Image getLogo() {
        return logo;
    }
}
