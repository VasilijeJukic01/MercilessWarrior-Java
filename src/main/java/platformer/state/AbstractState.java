package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.MenuButton;

import java.awt.event.MouseEvent;

public abstract class AbstractState {

    protected final Game game;

    public AbstractState(Game game) {
        this.game = game;
    }

    public boolean isMouseInButton(MouseEvent e, MenuButton menuButton) {
        return menuButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    public Game getGame() {
        return game;
    }
}
