package platformer.observer.events;

import platformer.core.GameContext;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.event.events.lancer.LancerAuraEvent;
import platformer.event.events.lancer.LancerDashSlashEvent;
import platformer.event.events.lancer.LancerTeleportEvent;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.particles.DustType;

import java.util.Random;

/**
 * Listens for Lancer-specific and other game events from the EventBus.
 */
public class LancerEventHandler {

    private final ScreenEffectsManager screenEffectsManager;
    private final EffectManager effectManager;

    public LancerEventHandler(GameContext context) {
        this.effectManager = context.getEffectManager();
        this.screenEffectsManager = context.getScreenEffectsManager();
    }

    public void onLancerTeleport(LancerTeleportEvent event) {
        effectManager.spawnDustParticles(event.location().getX(), event.location().getY(), 30, DustType.SW_TELEPORT, 0, null);
    }

    public void onLancerAura(LancerAuraEvent event) {
        if (event.shouldBeActive()) {
            effectManager.spawnAura(event.lancer(), 40);
        }
        else effectManager.clearAura(event.lancer());
    }

    public void onScreenShake(ScreenShakeEvent event) {
        screenEffectsManager.triggerShake(event.duration(), event.intensity());
    }

    public void onLancerDashSlash(LancerDashSlashEvent event) {
        Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            double t = rand.nextDouble();
            double spawnX = event.startPos().x + t * (event.endPos().x - event.startPos().x);
            double spawnY = event.startPos().y + (rand.nextDouble() * event.hitboxHeight());
            effectManager.spawnDustParticles(spawnX, spawnY, 1, DustType.SW_DASH_SLASH, 0, null);
        }
    }
}
