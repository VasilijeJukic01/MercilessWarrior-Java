package platformer.model.cutscene;

import platformer.core.GameContext;

/**
 * Command interface representing a single atomic action in a cutscene sequence.
 */
public interface CutsceneStep {

    /**
     * Invoked once when this step becomes the active step in the cutscene queue.
     */
    void start(GameContext context);

    /**
     * Invoked every game tick while this step is active.
     *
     * @return true when the step is complete and the queue should advance to the next step.
     */
    boolean update(GameContext context);
}