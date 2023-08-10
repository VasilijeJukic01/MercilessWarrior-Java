package platformer;

import platformer.core.Game;

public class AppCore {

    public static void main(String[] args) {
        Game game = new Game(args[0], args[1]);
        game.start();
    }

}
