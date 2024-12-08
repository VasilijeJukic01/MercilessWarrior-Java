package platformer.state;

import platformer.core.Game;

/**
 * This class is managing the different states of the game.
 * It holds references to all the game states and a reference to the current state.
 */
public class StateManager {

    private State currentState;
    private final MenuState menuState;
    private final GameState gameState;
    private final OptionsState optionsState;
    private final ControlsState controlsState;
    private final LeaderboardState leaderboardState;
    private final ChoseGameState choseGameState;
    private final CreditsState creditsState;
    private final QuitState quitState;

    public StateManager(Game game) {
        this.menuState = new MenuState(game);
        this.gameState = new GameState(game);
        this.optionsState = new OptionsState(game);
        this.quitState = new QuitState(game);
        this.controlsState = new ControlsState(game);
        this.leaderboardState = new LeaderboardState(game);
        this.choseGameState = new ChoseGameState(game);
        this.creditsState = new CreditsState(game);
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
        this.gameState.reloadSave();
    }

    public void setOptionsState() {
        this.currentState = optionsState;
    }

    public void setControlsState() {
        this.currentState = controlsState;
    }

    public void setLeaderboardState() {
        this.currentState = leaderboardState;
    }

    public void setChoseGameState() {
        this.currentState = choseGameState;
    }

    public void setCreditsState() {
        this.currentState = creditsState;
    }

    public void setQuitState() {
        this.currentState = quitState;
    }

}
