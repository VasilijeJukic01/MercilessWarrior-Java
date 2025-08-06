package platformer.model.world;

import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
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

    private void checkPlayerDeath() {
        Player player = gameState.getPlayer();
        if (player.checkAction(PlayerAction.DYING)) {
            gameState.setOverlay(PlayingState.DYING);
        }
        if (player.checkAction(PlayerAction.GAME_OVER)) {
            gameState.setOverlay(PlayingState.GAME_OVER);
        }
    }

    private void checkLevelExit() {
        Player player = gameState.getPlayer();
        if (player.checkAction(PlayerAction.DASH)) return;

        int exitStatus = isEntityOnExit(context.getLevelManager().getCurrentLevel(), player.getHitBox());
        if (exitStatus == RIGHT_EXIT) goToLevel(0, 1, "RIGHT", "Right level loaded.");
        else if (exitStatus == LEFT_EXIT) goToLevel(0, -1, "LEFT", "Left level loaded.");
        else if (exitStatus == UPPER_EXIT) goToLevel(-1, 0, "UPPER", "Upper level loaded.");
        else if (exitStatus == BOTTOM_EXIT) goToLevel(1, 0, "BOTTOM", "Bottom level loaded.");
    }

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
