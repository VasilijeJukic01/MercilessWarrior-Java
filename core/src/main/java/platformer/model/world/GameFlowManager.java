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
import platformer.model.levels.metadata.RoomExitMetadata;
import platformer.state.types.GameState;
import platformer.state.types.PlayingState;
import platformer.ui.transition.TransitionDirection;

import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.TILES_SIZE;

/**
 * Manages high-level game flow, such as graph-based level transitions and player death.
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
     * Resolves the destination level and spawn point via the world map graph using the trigger's tile coordinates.
     */
    private void checkLevelExit() {
        Player player = gameState.getPlayer();
        if (player.checkAction(PlayerAction.DASH)) return;

        LevelManager levelManager = context.getLevelManager();
        List<Trigger> triggers = new ArrayList<>(levelManager.getCurrentLevel().getTriggers());
        for (Trigger trigger : triggers) {
            LvlTriggerType type = trigger.type();
            if (type != LvlTriggerType.LOAD_LEFT_LEVEL && type != LvlTriggerType.LOAD_RIGHT_LEVEL && type != LvlTriggerType.LOAD_UP_LEVEL && type != LvlTriggerType.LOAD_DOWN_LEVEL) {
                continue;
            }

            if (player.getHitBox().intersects(trigger.bounds())) {
                int tileX = (int) (trigger.bounds().x / TILES_SIZE);
                int tileY = (int) (trigger.bounds().y / TILES_SIZE);
                RoomExitMetadata exit = levelManager.resolveExit(type, tileX, tileY);
                if (exit != null && exit.getTargetLevel() != null) {
                    LvlTriggerType targetSpawn = exit.getTargetSpawn() != null ? exit.getTargetSpawn() : getDefaultSpawnForTrigger(type);
                    goToLevel(exit.getTargetLevel(), type, targetSpawn);
                    return;
                }
            }
        }
    }

    private LvlTriggerType getDefaultSpawnForTrigger(LvlTriggerType triggerType) {
        return switch (triggerType) {
            case LOAD_LEFT_LEVEL -> LvlTriggerType.SPAWN_A;
            case LOAD_RIGHT_LEVEL -> LvlTriggerType.SPAWN_B;
            case LOAD_UP_LEVEL -> LvlTriggerType.SPAWN_C;
            case LOAD_DOWN_LEVEL -> LvlTriggerType.SPAWN_D;
            default -> LvlTriggerType.SPAWN_A;
        };
    }

    private TransitionDirection getTransitionDirection(LvlTriggerType triggerType) {
        return switch (triggerType) {
            case LOAD_RIGHT_LEVEL -> TransitionDirection.FROM_LEFT;
            case LOAD_LEFT_LEVEL -> TransitionDirection.FROM_RIGHT;
            case LOAD_UP_LEVEL -> TransitionDirection.FROM_BOTTOM;
            case LOAD_DOWN_LEVEL -> TransitionDirection.FROM_TOP;
            default -> TransitionDirection.FROM_LEFT;
        };
    }

    /**
     * Orchestrates the process of changing levels.
     * This involves loading the next level's data and resetting the world's component.
     *
     * @param targetLevelId The ID of the level to load.
     * @param exitType      The exit trigger type that initiated the level transition.
     * @param spawn   The spawn trigger in the new level.
     */
    private void goToLevel(String targetLevelId, LvlTriggerType exitType, LvlTriggerType spawn) {
        TransitionDirection direction = getTransitionDirection(exitType);

        Runnable levelLoadCallback = () -> {
            gameState.getPlayer().removeAction(PlayerAction.LEVEL_TRANSITION);
            gameState.getPlayer().resetDirections();
            gameState.getStateController().resetKeys();
            gameState.flushAWTEventQueue();
            gameState.getPlayer().activateMinimap(false);
            context.getLevelManager().loadLevelById(targetLevelId);
            gameState.getWorld().levelLoadReset(spawn);
            context.getMinimapManager().changeLevel();
            gameState.getPlayer().activateMinimap(true);
            gameState.getCamera().updateLevelBounds(context.getLevelManager().getCurrentLevel());
            gameState.getCamera().snapToPlayer(gameState.getPlayer());
            gameState.getOverlayManager().reset();
            context.getQuestManager().reset();
            Logger.getInstance().notify("Loaded level: " + targetLevelId + " at " + spawn, Message.NOTIFICATION);
        };

        gameState.getPlayer().addAction(PlayerAction.LEVEL_TRANSITION);
        gameState.getTransitionManager().startTransition(direction, levelLoadCallback);
    }

    private void configureSpawnPoint() {
        String currentLevelId = context.getLevelManager().getCurrentLevelId();
        int currentSpawnId = -1;
        for (Spawn s : Spawn.values()) {
            if (s.getLevelId().equals(currentLevelId)) {
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
