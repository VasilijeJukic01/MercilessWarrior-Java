package platformer.state;

import platformer.core.Game;

public class StateManager {

    private State currentState;
    private final MenuState menuState;
    private final GameState gameState;
    private final OptionsState optionsState;
    private final ControlsState controlsState;
    private final QuitState quitState;

    public StateManager(Game game) {
        this.menuState = new MenuState(game);
        this.gameState = new GameState(game);
        this.optionsState = new OptionsState(game);
        this.quitState = new QuitState(game);
        this.controlsState = new ControlsState(game);
        this.currentState = menuState;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setMenuState() {
        this.currentState = menuState;
    }

    public void setPlayingState() {
        this.currentState = gameState;
    }

    public void setOptionsState() {
        this.currentState = optionsState;
    }

    public void setControlsState() {
        this.currentState = controlsState;
    }

    public void setQuitState() {
        this.currentState = quitState;
    }
}
