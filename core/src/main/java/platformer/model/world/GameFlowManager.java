package platformer.model.world;

import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.state.types.GameState;
import platformer.state.types.PlayingState;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isEntityOnExit;

/**
 * Manages high-level game flow, such as level transitions and player death.
 */
public class GameFlowManager {

    private final GameContext context;
    private final GameState gameState;

    public GameFlowManager(GameContext context) {
        this.context = context;
        this.gameState = context.getGameState();
    }

    public void update() {
        checkLevelExit();
        checkPlayerDeath();
    }

    /**
     * Monitors the player's actions for {@link PlayerAction#DYING} or {@link PlayerAction#GAME_OVER} states
     * and sets the appropriate overlay in the {@link GameState} to display the death or game over screen.
     */
    private void checkPlayerDeath() {
        Player player = gameState.getPlayer();
        if (player.checkAction(PlayerAction.DYING)) {
            EventBus.getInstance().publish(new OverlayChangeEvent(PlayingState.DYING));
        }
        if (player.checkAction(PlayerAction.GAME_OVER)) {
            EventBus.getInstance().publish(new OverlayChangeEvent(PlayingState.GAME_OVER));
        }
    }

    /**
     * Checks if the player's hitbox intersects with any of the level's exit tiles.
     * If an intersection is found, it calls {@link #goToLevel} to handle the transition.
     */
    private void checkLevelExit() {
        Player player = gameState.getPlayer();
        if (player.checkAction(PlayerAction.DASH)) return;

        int exitStatus = isEntityOnExit(context.getLevelManager().getCurrentLevel(), player.getHitBox());
        if (exitStatus == RIGHT_EXIT) goToLevel(0, 1, "RIGHT", "Right level loaded.");
        else if (exitStatus == LEFT_EXIT) goToLevel(0, -1, "LEFT", "Left level loaded.");
        else if (exitStatus == UPPER_EXIT) goToLevel(-1, 0, "UPPER", "Upper level loaded.");
        else if (exitStatus == BOTTOM_EXIT) goToLevel(1, 0, "BOTTOM", "Bottom level loaded.");
    }

    /**
     * Orchestrates the process of changing levels.
     * This involves loading the next level's data and resetting the world's component.
     *
     * @param dI      The change in the level grid's row index.
     * @param dJ      The change in the level grid's column index.
     * @param spawn   The spawn location name ("LEFT", "RIGHT", "UPPER", "BOTTOM") in the new level.
     * @param message A message to log upon successful level transition.
     */
    private void goToLevel(int dI, int dJ, String spawn, String message) {
        gameState.getPlayer().activateMinimap(false);
        context.getLevelManager().loadNextLevel(dI, dJ);
        gameState.getWorld().levelLoadReset(spawn);
        context.getMinimapManager().changeLevel();
        gameState.getPlayer().activateMinimap(true);
        gameState.getCamera().updateLevelBounds(context.getLevelManager().getCurrentLevel());
        gameState.getOverlayManager().reset();
        context.getQuestManager().reset();
        Logger.getInstance().notify(message, Message.NOTIFICATION);
    }
}
