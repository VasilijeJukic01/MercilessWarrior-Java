package platformer;

import platformer.core.Game;
import platformer.model.Tiles;

public class AppCore {

    public static void main(String[] args) {
        changeResolution(Float.parseFloat(args[0]));
        Game game = new Game();
        game.start();
    }

    public static void changeResolution(float value) {
        Tiles.SCALE.setValue(value);
        Tiles.TILES_SIZE.setValue(Tiles.TILES_DEFAULT_SIZE.getValue()*Tiles.SCALE.getValue());
        Tiles.GAME_WIDTH.setValue(Tiles.TILES_SIZE.getValue()*Tiles.TILES_WIDTH.getValue());
        Tiles.GAME_HEIGHT.setValue(Tiles.TILES_SIZE.getValue()*Tiles.TILES_HEIGHT.getValue());
    }

}
