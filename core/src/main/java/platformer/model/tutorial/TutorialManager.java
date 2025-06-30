package platformer.model.tutorial;

import platformer.state.GameState;
import platformer.state.PlayingState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages tutorials in the game.
 * It handles tutorial activation and deactivation.
 */
public class TutorialManager {

    private final GameState gameState;

    private static final Map<TutorialType, Boolean> tutorials = new HashMap<>();
    private int currentTutorial;

    static {
        Arrays.stream(TutorialType.values()).forEach(t -> tutorials.put(t, false));
    }

    public TutorialManager(GameState gameState) {
        this.gameState = gameState;
    }

    public void activateBlockTutorial() {
        if (!tutorials.get(TutorialType.BLOCK_ENEMY)) triggerTutorial(TutorialType.BLOCK_ENEMY);
    }

    private void triggerTutorial(TutorialType tutorialType) {
        tutorials.put(tutorialType, true);
        currentTutorial = tutorialType.ordinal();
        gameState.setOverlay(PlayingState.TUTORIAL);
    }

    public int getCurrentTutorial() {
        return currentTutorial;
    }
}
