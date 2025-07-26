package platformer.observer.events;

import platformer.core.GameContext;
import platformer.model.levels.Level;
import platformer.observer.EventHandler;
import platformer.observer.Subscriber;
import platformer.state.GameState;

public class GameFlowEventHandler implements EventHandler, Subscriber {

    private final GameState gameState;

    public GameFlowEventHandler(GameContext context) {
        this.gameState = context.getGameState();
    }

    @Override
    public <T> void update(T... o) {
        if (o.length < 2 || !(o[0] instanceof String eventType) || !(o[1] instanceof String target)) return;

        if ("START_FIGHT".equals(eventType) && "RORIC".equals(target)) startRoricFight();
    }

    private void startRoricFight() {
        gameState.getObjectManager().getIntersection().setAlive(false);
        gameState.getLevelManager().switchToArena();
        Level arena = gameState.getLevelManager().getCurrentLevel();

        gameState.getPlayer().loadLvlData(arena.getLvlData());
        gameState.getEnemyManager().loadEnemies(arena);
        gameState.getObjectManager().loadObjects(arena);
        gameState.getSpellManager().initBossSpells();
        gameState.getMinimapManager().changeLevel();
        gameState.getPlayer().activateMinimap(false);
        gameState.getCamera().updateLevelBounds(arena);

        gameState.getPlayer().setSpawn(arena.getPlayerSpawn("LEFT"));
        gameState.getPlayer().resetDirections();
    }

    @Override
    public void continuousUpdate() {

    }

    @Override
    public void reset() {

    }
}
