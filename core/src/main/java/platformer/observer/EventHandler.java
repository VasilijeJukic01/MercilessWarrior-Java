package platformer.observer;

import platformer.model.entities.effects.EffectManager;
import platformer.model.entities.effects.particles.DustType;
import platformer.model.entities.enemies.boss.SpearWoman;

import java.awt.*;

public class EventHandler {

    private final EffectManager effectManager;

    public EventHandler(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public <T> void handleEvent(T... o) {
        if (o == null || o.length < 2 || !(o[0] instanceof String)) return;
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
        }
    }

    private void handleTeleportEvent(Object arg) {
        if (arg instanceof Point location) {
            effectManager.spawnDustParticles(location.getX(), location.getY(), 30, DustType.SW_TELEPORT, 0, null);
        }
    }

    private void handleSpawnAuraEvent(Object arg) {
        if (arg instanceof SpearWoman boss) {
            effectManager.spawnAura(boss, 40);
        }
    }

    private void handleClearAuraEvent(Object arg) {
        if (arg instanceof SpearWoman boss) {
            effectManager.clearAura(boss);
        }
    }
}