package platformer.state;

import platformer.core.Game;
import platformer.state.types.*;

import java.util.EnumMap;
import java.util.Map;

/**
 * This class is managing the different states of the game.
 * It holds references to all the game states and a reference to the current state.
 */
public class StateManager {

    private State currentState;
    private final Map<StateType, State> states = new EnumMap<>(StateType.class);

    public StateManager(Game game) {
        states.put(StateType.MENU, new MenuState(game));
        states.put(StateType.PLAYING, new GameState(game));
        states.put(StateType.OPTIONS, new OptionsState(game));
        states.put(StateType.CONTROLS, new ControlsState(game));
        states.put(StateType.LEADERBOARD, new LeaderboardState(game));
        states.put(StateType.CHOOSE_GAME, new ChoseGameState(game));
        states.put(StateType.CREDITS, new CreditsState(game));
        states.put(StateType.QUIT, new QuitState(game));

        // Initial state
        this.currentState = states.get(StateType.MENU);
        this.currentState.enter();
    }

    /**
     * Changes the current game state.
     * It calls exit() on the old state and enter() on the new state.
     *
     * @param type The type of the state to switch to.
     */
    public void setState(StateType type) {
        if (currentState != null) currentState.exit();
        currentState = states.get(type);
        if (currentState != null) {
            if (type == StateType.PLAYING) {
                ((GameState)currentState).reloadSave();
            }
            currentState.enter();
        }
    }

    public State getCurrentState() {
        return currentState;
    }
}
