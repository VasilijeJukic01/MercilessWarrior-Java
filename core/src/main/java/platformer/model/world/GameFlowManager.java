package platformer.model.world;

import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.PreSaveEvent;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.levels.*;
import platformer.state.types.GameState;
import platformer.state.types.PlayingState;
import platformer.ui.transition.TransitionDirection;

import java.util.List;

/**
 * Manages high-level game flow, such as level transitions and player death.
 */
public class GameFlowManager {

    private final GameContext context;
    private final GameState gameState;

    public GameFlowManager(GameContext context) {
        this.context = context;
        this.gameState = context.getGameState();
        EventBus.getInstance().register(PreSaveEvent.class, e -> {
            configureSpawnPoint();
            saveMinimapData();
        });
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
     * Scans the current level's spatial triggers to see if the player has entered a level transition zone.
     * If an intersection with a LOAD trigger is detected, it orchestrates the screen-wipe transition and loads the adjacent map.
     */
    private void checkLevelExit() {
        Player player = gameState.getPlayer();
        if (player.checkAction(PlayerAction.DASH)) return;

        List<Trigger> triggers = context.getLevelManager().getCurrentLevel().getTriggers();
        for (Trigger trigger : triggers) {
            if (player.getHitBox().intersects(trigger.bounds())) {
                switch (trigger.type()) {
                    case LOAD_LEFT_LEVEL:
                        goToLevel(0, -1, LvlTriggerType.SPAWN_A, "Left level loaded.");
                        return;
                    case LOAD_RIGHT_LEVEL:
                        goToLevel(0, 1, LvlTriggerType.SPAWN_B, "Right level loaded.");
                        return;
                    case LOAD_UP_LEVEL:
                        goToLevel(-1, 0, LvlTriggerType.SPAWN_C, "Upper level loaded.");
                        return;
                    case LOAD_DOWN_LEVEL:
                        goToLevel(1, 0, LvlTriggerType.SPAWN_D, "Bottom level loaded.");
                        return;
                }
            }
        }
    }

    /**
     * Orchestrates the process of changing levels.
     * This involves loading the next level's data and resetting the world's component.
     *
     * @param dI      The change in the level grid's row index.
     * @param dJ      The change in the level grid's column index.
     * @param spawn   The spawn trigger in the new level.
     * @param message A message to log upon successful level transition.
     */
    private void goToLevel(int dI, int dJ, LvlTriggerType spawn, String message) {
        TransitionDirection direction = TransitionDirection.FROM_LEFT;
        if (dI == 0 && dJ == 1) direction = TransitionDirection.FROM_LEFT;
        else if (dI == 0 && dJ == -1) direction = TransitionDirection.FROM_RIGHT;
        else if (dI == -1 && dJ == 0) direction = TransitionDirection.FROM_BOTTOM;
        else if (dI == 1 && dJ == 0) direction = TransitionDirection.FROM_TOP;

        Runnable levelLoadCallback = () -> {
            gameState.getPlayer().resetDirections();
            gameState.getStateController().resetKeys();
            gameState.flushAWTEventQueue();
            gameState.getPlayer().activateMinimap(false);
            context.getLevelManager().loadNextLevel(dI, dJ);
            gameState.getWorld().levelLoadReset(spawn);
            context.getMinimapManager().changeLevel();
            gameState.getPlayer().activateMinimap(true);
            gameState.getCamera().updateLevelBounds(context.getLevelManager().getCurrentLevel());
            gameState.getOverlayManager().reset();
            context.getQuestManager().reset();
            Logger.getInstance().notify(message, Message.NOTIFICATION);
        };

        gameState.getTransitionManager().startTransition(direction, levelLoadCallback);
    }

    private void configureSpawnPoint() {
        LevelManager levelManager = context.getLevelManager();
        int currentSpawnId = -1;
        for (Spawn s : Spawn.values()) {
            if (s.getLevelI() == levelManager.getLevelIndexI() && s.getLevelJ() == levelManager.getLevelIndexJ()) {
                currentSpawnId = s.getId();
                break;
            }
        }
        Framework.getInstance().getAccount().setSpawn(currentSpawnId);
    }

    private void saveMinimapData() {
        String data = context.getMinimapManager().getExplorationDataForSave();
        Framework.getInstance().getAccount().setExplorationData(data);
    }
}
