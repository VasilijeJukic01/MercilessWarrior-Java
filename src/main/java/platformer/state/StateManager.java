package platformer.state;

import platformer.core.Game;

public class StateManager {

    private State currentState;
    private final MenuState menuState;
    private final PlayingState playingState;
    private final OptionsState optionsState;
    private final QuitState quitState;

    public StateManager(Game game) {
        this.menuState = new MenuState(game);
        this.playingState = new PlayingState(game);
        this.optionsState = new OptionsState(game);
        this.quitState = new QuitState(game);
        this.currentState = menuState;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setMenuState() {
        this.currentState = menuState;
    }

    public void setPlayingState() {
        this.currentState = playingState;
    }

    public void setOptionsState() {
        this.currentState = optionsState;
    }

    public void setQuitState() {
        this.currentState = quitState;
    }
}
