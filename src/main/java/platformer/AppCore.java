package platformer;

import platformer.core.Game;
import platformer.utils.Utils;

public class AppCore {

    public static void main(String[] args) {
        Utils.getInstance().changeResolution(Float.parseFloat(args[0]));
        Game game = new Game(args[1], args[2]);
        game.start();
    }

}
