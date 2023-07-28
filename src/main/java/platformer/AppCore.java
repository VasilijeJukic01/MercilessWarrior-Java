package platformer;

import platformer.constants.Constants;
import platformer.core.Game;

public class AppCore {

    public static void main(String[] args) {
        Constants.setResolution(Float.parseFloat(args[0]));
        Game game = new Game(args[1], args[2]);
        game.start();
    }

}
