package platformer.observer.events;

import platformer.core.GameContext;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.observer.EventHandler;
import platformer.observer.Subscriber;
import platformer.state.GameState;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * An event handler that processes and creates effects for events published by the {@link Lancer} boss.
 * <p>
 * This class implements {@link Subscriber} to receive discrete event notifications from the Lancer boss,
 * such as teleporting or activating an aura. It then uses the {@link EffectManager} to create the appropriate
 * visual and auditory effects, decoupling the boss's core logic from its presentation.
 * <p>
 * This class is instantiated and managed by {@link GameState}.
 *
 * @see Lancer
 * @see GameState
 * @see Subscriber
 * @see EventHandler
 */
public class LancerEventHandler implements EventHandler, Subscriber {

    private final GameState gameState;
    private final EffectManager effectManager;

    public LancerEventHandler(GameContext context) {
        this.effectManager = context.getEffectManager();
        this.gameState = context.getGameState();
    }

    /**
     * Receives event notifications from the Lancer boss.
     * This method is triggered when the boss calls its {@code notify()} method.
     *
     * @param o An array of objects, typically starting with a String event type followed by event-specific data.
     * @param <T> The type of the event parameters.
     */
    @Override
    public <T> void update(T... o) {
        if (o == null || !(o[0] instanceof String)) return;
        String eventType = (String) o[0];

        switch (eventType) {
            case "TELEPORT_OUT":
            case "TELEPORT_IN":
                handleTeleportEvent(o[1]);
                break;
            case "SPAWN_AURA":
                handleSpawnAuraEvent(o[1]);
                break;
            case "CLEAR_AURA":
                handleClearAuraEvent(o[1]);
                break;
            case "SHAKE_SCREEN":
                handleShakeScreenEvent();
                break;
            case "DASH_SLASH":
                handleDashSlashEvent(o[1], o[2], o[3]);
                break;
        }
    }

    private void handleTeleportEvent(Object arg) {
        if (arg instanceof Point location) {
            effectManager.spawnDustParticles(location.getX(), location.getY(), 30, DustType.SW_TELEPORT, 0, null);
        }
    }

    private void handleSpawnAuraEvent(Object arg) {
        if (arg instanceof Lancer boss) {
            effectManager.spawnAura(boss, 40);
        }
    }

    private void handleClearAuraEvent(Object arg) {
        if (arg instanceof Lancer boss) {
            effectManager.clearAura(boss);
        }
    }

    private void handleShakeScreenEvent() {
        gameState.triggerScreenShake(30, 15.0);
    }

    private void handleDashSlashEvent(Object start, Object end, Object height) {
        if (start instanceof Point2D.Double startPos && end instanceof Point2D.Double endPos && height instanceof Double hitboxHeight) {
            Random rand = new Random();
            for (int i = 0; i < 30; i++) {
                double t = rand.nextDouble();
                double spawnX = startPos.x + t * (endPos.x - startPos.x);
                double spawnY = startPos.y + (rand.nextDouble() * hitboxHeight);
                effectManager.spawnDustParticles(spawnX, spawnY, 1, DustType.SW_DASH_SLASH, 0, null);
            }
        }
    }

    @Override
    public void continuousUpdate() {

    }

    @Override
    public void reset() {

    }
}
