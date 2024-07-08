package platformer;

import platformer.core.Framework;

/**
 * Main class of the game.
 */
public class AppCore {

    public static void main(String[] args) {
        Framework.getInstance().init(args[0], args[1], args[2]);
        Framework.getInstance().start();
    }

}
