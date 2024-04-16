package platformer.launcher.core;

import javafx.application.Application;
import javafx.stage.Stage;
import platformer.launcher.view.LauncherView;

public class LauncherCore extends Application {

    @Override
    public void start(Stage primaryStage) {
        LauncherView launcherView = new LauncherView();
        launcherView.show();
    }

}
