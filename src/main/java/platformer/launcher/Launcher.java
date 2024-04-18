package platformer.launcher;

import platformer.launcher.core.LauncherCore;

/**
 * This class is used to launch the game.
 */
public class Launcher {

    public static void main(String[] args) {
        LauncherCore.launch(LauncherCore.class, args);
    }

}
