package platformer.event.handlers;

import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.EnemyDefeatedEvent;
import platformer.event.events.effects.HitStopEvent;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.event.events.lancer.LancerAuraEvent;
import platformer.event.events.lancer.LancerDashSlashEvent;
import platformer.event.events.lancer.LancerTeleportEvent;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.boss.Lancer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Listens for Lancer-specific and other game events from the EventBus.
 */
public class LancerEventHandler {

    private static final Color ELECTRIC_CYAN = new Color(110, 235, 255);
    private static final Color SPARK_GOLD = new Color(255, 200, 95);

    private final GameContext context;
    private final ScreenEffectsManager screenEffectsManager;
    private final EffectManager effectManager;
    private final Random rand = new Random();

    public LancerEventHandler(GameContext context) {
        this.context = context;
        this.effectManager = context.getEffectManager();
        this.screenEffectsManager = context.getScreenEffectsManager();
        EventBus.getInstance().register(EnemyDefeatedEvent.class, this::onEnemyDefeated);
    }

    public void onLancerTeleport(LancerTeleportEvent event) {
        context.getTimeCycleManager().setLockedToNight(true);
        double wx = event.location().getX(), wy = event.location().getY();
        effectManager.spawnDustParticles(wx, wy, 30, DustType.SW_TELEPORT, 0, null);
        effectManager.spawnDustParticles(wx, wy, 12, DustType.SW_AURA_CRACKLE, 0, null);

        if (event.isTeleportIn()) {
            int sx = (int) wx - context.getCamera().getXOffset();
            int sy = (int) wy - context.getCamera().getYOffset();
            screenEffectsManager.triggerShockwave(sx, sy, scale(95), 24, new Color(170, 100, 255));
            screenEffectsManager.triggerShake(12, 4.5);
        }
        effectManager.setStormIntensity(1.65);
    }

    public void onLancerAura(LancerAuraEvent event) {
        Lancer lancer = event.lancer();
        switch (event.effectType()) {
            case AURA_TOGGLE -> handleAuraToggle(lancer, event.shouldBeActive());
            case BLADE_SWING -> handleBladeSwing(lancer);
            case RAPID_THRUST -> handleRapidThrust(lancer);
            case ORB_CAST -> handleOrbCast(lancer);
            case THUNDER_SLAM_IMPACT -> handleThunderSlamImpact(lancer);
        }
    }

    private void handleAuraToggle(Lancer lancer, boolean shouldBeActive) {
        if (shouldBeActive) {
            context.getTimeCycleManager().setLockedToNight(true);
            effectManager.spawnAura(lancer, 40);
            int sx = (int) lancer.getHitBox().getCenterX() - context.getCamera().getXOffset();
            int sy = (int) lancer.getHitBox().getCenterY() - context.getCamera().getYOffset();
            screenEffectsManager.triggerShockwave(sx, sy, scale(220), 38, ELECTRIC_CYAN);
            screenEffectsManager.triggerLightningStrobe(ELECTRIC_CYAN, 110, false);
            effectManager.setStormIntensity(2.6);
        }
        else {
            effectManager.clearAura(lancer);
            effectManager.setStormIntensity(1.65);
        }
    }

    private void handleBladeSwing(Lancer lancer) {
        Rectangle2D.Double hb = lancer.getHitBox();
        int dirSign = lancer.getFlipSign();
        double tipX = hb.getCenterX() + dirSign * scale(36);
        double tipY = hb.getCenterY();

        for (int i = 0; i < 14; i++) {
            double sx = tipX + (rand.nextDouble() - 0.5) * scale(28);
            double sy = tipY + (rand.nextDouble() - 0.5) * hb.height * 0.85;
            effectManager.spawnDustParticles(sx, sy, 1, DustType.IMPACT_SPARK, dirSign, null);
            effectManager.spawnDustParticles(sx, sy, 1, DustType.SW_AURA_CRACKLE, dirSign, null);
            if (i % 2 == 0) {
                effectManager.spawnDustParticles(sx, sy, 1, DustType.SW_DASH_SLASH, dirSign, null);
            }
        }
        effectManager.spawnDustParticles(hb.getCenterX(), hb.getMaxY(), 8, DustType.IMPACT, dirSign, lancer);
        int screenX = (int) tipX - context.getCamera().getXOffset();
        int screenY = (int) tipY - context.getCamera().getYOffset();
        screenEffectsManager.triggerShockwave(screenX, screenY, scale(75), 18, SPARK_GOLD);
    }

    private void handleRapidThrust(Lancer lancer) {
        Rectangle2D.Double hb = lancer.getHitBox();
        int dirSign = lancer.getFlipSign();
        double tipX = hb.getCenterX() + dirSign * scale(40);
        double tipY = hb.getCenterY() + (rand.nextDouble() - 0.5) * scale(22);

        effectManager.spawnDustParticles(tipX, tipY, 6, DustType.IMPACT_SPARK, dirSign, null);
        effectManager.spawnDustParticles(tipX, tipY, 6, DustType.SW_AURA_CRACKLE, dirSign, null);
        effectManager.spawnDustParticles(tipX, tipY, 3, DustType.SW_DASH_SLASH, dirSign, null);
        effectManager.spawnDustParticles(hb.getCenterX(), hb.getMaxY(), 4, DustType.RUNNING, dirSign, lancer);
    }

    private void handleOrbCast(Lancer lancer) {
        int dirSign = lancer.getFlipSign();
        double handX = lancer.getHitBox().getCenterX() + dirSign * scale(20);
        double handY = lancer.getHitBox().getCenterY() - scale(2);

        effectManager.spawnDustParticles(handX, handY, 14, DustType.SW_AURA_CRACKLE, dirSign, null);
        effectManager.spawnDustParticles(handX, handY, 8, DustType.IMPACT_SPARK, dirSign, null);

        int sx = (int) handX - context.getCamera().getXOffset();
        int sy = (int) handY - context.getCamera().getYOffset();
        screenEffectsManager.triggerShockwave(sx, sy, scale(70), 20, ELECTRIC_CYAN);
    }

    private void handleThunderSlamImpact(Lancer lancer) {
        Rectangle2D.Double hb = lancer.getHitBox();
        double groundX = hb.getCenterX();
        double groundY = hb.getMaxY();

        effectManager.spawnDustParticles(groundX, groundY, 22, DustType.IMPACT, 0, lancer);
        effectManager.spawnDustParticles(groundX, groundY, 24, DustType.IMPACT_SPARK, 0, null);
        effectManager.spawnDustParticles(groundX, groundY - scale(10), 25, DustType.SW_AURA_CRACKLE, 0, null);

        int sx = (int) groundX - context.getCamera().getXOffset();
        int sy = (int) groundY - context.getCamera().getYOffset();
        screenEffectsManager.triggerShockwave(sx, sy, scale(250), 38, ELECTRIC_CYAN);
    }

    public void onScreenShake(ScreenShakeEvent event) {
        screenEffectsManager.triggerShake(event.duration(), event.intensity());
        if (event.intensity() >= 11.0) {
            context.getTimeCycleManager().setLockedToNight(true);
            context.getLightManager().setCurrentAmbientAlpha(0);
            screenEffectsManager.triggerLightningStrobe(ELECTRIC_CYAN, 165, true);
        }
        else if (event.intensity() >= 5.5) {
            screenEffectsManager.triggerLightningStrobe(ELECTRIC_CYAN, 75, false);
        }
    }

    public void onLancerDashSlash(LancerDashSlashEvent event) {
        screenEffectsManager.triggerShake(18, 9.5);
        screenEffectsManager.triggerLightningStrobe(ELECTRIC_CYAN, 105, false);

        int endScreenX = (int) event.endPos().x - context.getCamera().getXOffset();
        int endScreenY = (int) (event.endPos().y + event.hitboxHeight() / 2.0) - context.getCamera().getYOffset();
        screenEffectsManager.triggerShockwave(endScreenX, endScreenY, scale(140), 28, ELECTRIC_CYAN);

        for (int i = 0; i < 38; i++) {
            double t = rand.nextDouble();
            double spawnX = event.startPos().x + t * (event.endPos().x - event.startPos().x);
            double spawnY = event.startPos().y + (rand.nextDouble() * event.hitboxHeight());
            effectManager.spawnDustParticles(spawnX, spawnY, 1, DustType.SW_DASH_SLASH, 0, null);
            if (i % 2 == 0) {
                effectManager.spawnDustParticles(spawnX, spawnY, 1, DustType.SW_AURA_CRACKLE, 0, null);
            }
            if (i % 3 == 0) {
                effectManager.spawnDustParticles(spawnX, spawnY, 1, DustType.IMPACT_SPARK, 0, null);
            }
        }
    }

    private void onEnemyDefeated(EnemyDefeatedEvent event) {
        if (event.defeatedEnemy().getEnemyType() == EnemyType.LANCER) {
            double cx = event.defeatedEnemy().getHitBox().getCenterX();
            double cy = event.defeatedEnemy().getHitBox().getCenterY();
            int sx = (int) cx - context.getCamera().getXOffset();
            int sy = (int) cy - context.getCamera().getYOffset();

            EventBus.getInstance().publish(new HitStopEvent(16));
            screenEffectsManager.triggerShake(55, 16.0);
            screenEffectsManager.triggerShockwave(sx, sy, scale(380), 55, ELECTRIC_CYAN);
            screenEffectsManager.triggerLightningStrobe(Color.WHITE, 230, true);
            context.getLightManager().setCurrentAmbientAlpha(0);
            context.getTimeCycleManager().setLockedToNight(false);
            context.getProjectileManager().reset();

            effectManager.spawnDustParticles(cx, cy, 45, DustType.SW_TELEPORT, 0, null);
            effectManager.spawnDustParticles(cx, cy, 35, DustType.SW_AURA_CRACKLE, 0, null);
            effectManager.spawnDustParticles(cx, cy, 25, DustType.IMPACT_SPARK, 0, null);
            effectManager.setStormIntensity(1.0);
        }
    }
}