package platformer.event.handlers;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.events.BossDefeatedEvent;
import platformer.event.events.FightInitiatedEvent;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.levels.Level;
import platformer.model.levels.Spawn;
import platformer.event.EventHandler;
import platformer.state.types.GameState;

import java.awt.*;

import static platformer.constants.Constants.TILES_SIZE;

public class GameFlowEventHandler implements EventHandler {

    private final GameState gameState;

    public GameFlowEventHandler(GameContext context) {
        this.gameState = context.getGameState();
    }

    /**
     * Listens for the start of a major fight and triggers the appropriate level transition.
     *
     * @param event The event containing the ID of the boss.
     */
    public void onFightInitiated(FightInitiatedEvent event) {
        if (event.bossId().equals("RORIC")) startRoricFight();
    }

    /**
     * Handles the event when a boss is defeated.
     *
     * @param event The event containing the defeated boss.
     */
    public void onBossDefeated(BossDefeatedEvent event) {
        if (event.boss().getEnemyType() == EnemyType.RORIC) returnFromArena();
    }

    private void startRoricFight() {
        gameState.getObjectManager().getIntersection().setAlive(false);
        gameState.getLevelManager().switchToArena();
        Level arena = gameState.getLevelManager().getCurrentLevel();
        levelTransition(arena, arena.getPlayerSpawn("LEFT"));
    }

    private void returnFromArena() {
        Logger.getInstance().notify("Roric defeated! Returning to the main world.", Message.INFORMATION);
        Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);

        gameState.getLevelManager().returnToMainMap();
        Level originalLevel = gameState.getLevelManager().getCurrentLevel();

        int spawnId = Framework.getInstance().getAccount().getSpawn();
        Point playerSpawn = null;
        for (Spawn spawn : Spawn.values()) {
            if (spawn.getId() == spawnId) {
                playerSpawn = new Point(spawn.getX() * TILES_SIZE, spawn.getY() * TILES_SIZE);
                break;
            }
        }
        // Fallback
        if (playerSpawn == null) playerSpawn = originalLevel.getPlayerSpawn("LEFT");

        levelTransition(originalLevel, playerSpawn);
        gameState.getPlayer().activateMinimap(true);
    }

    private void levelTransition(Level level, Point playerSpawn) {
        gameState.reset();
        gameState.getPlayer().loadLvlData(level.getLvlData());
        gameState.getEnemyManager().loadEnemies(level);
        gameState.getObjectManager().loadObjects(level);
        gameState.getSpellManager().initBossSpells();
        gameState.getMinimapManager().changeLevel();
        gameState.getCamera().updateLevelBounds(level);

        gameState.getPlayer().setSpawn(playerSpawn);
        gameState.getPlayer().resetDirections();
    }

    @Override
    public void continuousUpdate() {

    }

    @Override
    public void reset() {

    }
}
