package platformer.observer.events;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.Level;
import platformer.model.levels.Spawn;
import platformer.observer.EventHandler;
import platformer.observer.Subscriber;
import platformer.state.types.GameState;

import java.awt.*;

import static platformer.constants.Constants.TILES_SIZE;

public class GameFlowEventHandler implements EventHandler, Subscriber {

    private final GameState gameState;

    public GameFlowEventHandler(GameContext context) {
        this.gameState = context.getGameState();
    }

    @Override
    public <T> void update(T... o) {
        if (o.length < 2 || !(o[0] instanceof String eventType) || !(o[1] instanceof String target)) return;

        if ("START_FIGHT".equals(eventType) && "RORIC".equals(target)) startRoricFight();
        else if ("FIGHT_WON".equals(eventType) && "RORIC".equals(target)) returnFromArena();
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
