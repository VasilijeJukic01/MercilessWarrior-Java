package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.AbstractButton;

import java.awt.event.MouseEvent;

public abstract class AbstractState {

    protected final Game game;

    public AbstractState(Game game) {
        this.game = game;
    }

    public boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    public Game getGame() {
        return game;
    }
}
