package platformer.observer;

import platformer.model.entities.effects.EffectManager;
import platformer.model.entities.effects.particles.DustType;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.state.GameState;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;

public class EventHandler {

    private GameState gameState;
    private final EffectManager effectManager;

    public EventHandler(GameState gameState, EffectManager effectManager) {
        this.gameState = gameState;
        this.effectManager = effectManager;
    }

    public <T> void handleEvent(T... o) {
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
                handleShakeScreenEvent(o[0]);
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

    private void handleShakeScreenEvent(Object arg) {
        if ("SHAKE_SCREEN".equals(arg)) {
            gameState.triggerScreenShake(30, 15.0);
        }
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
}