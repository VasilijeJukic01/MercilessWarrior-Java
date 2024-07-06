package platformer.launcher;

import platformer.launcher.core.LauncherCore;

/**
 * This class is used to launch the game.
 */
public class Launcher {

    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        String libPath = currentDir + "/../lib";
        System.setProperty("java.library.path", libPath);

        LauncherCore.launch(LauncherCore.class, args);
    }

}
