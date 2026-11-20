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
import platformer.model.gameObjects.GameObject;

public class GameFlowEventHandler {

    private final GameContext context;

    public GameFlowEventHandler(GameContext context) {
        this.context = context;
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
        if (event.boss().getEnemyType() == EnemyType.RORIC) {
            returnFromArena();
        }
    }

    private void startRoricFight() {
        if (context.getObjectManager().getIntersection() instanceof GameObject obj) {
            obj.setAlive(false);
        }
        context.getWorldTransitionCoordinator().enterArena();
    }

    private void returnFromArena() {
        Logger.getInstance().notify("Roric defeated! Returning to the main world.", Message.INFORMATION);
        Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);

        int spawnId = Framework.getInstance().getAccount().getSpawn();
        context.getWorldTransitionCoordinator().exitArena(spawnId);
    }
}
