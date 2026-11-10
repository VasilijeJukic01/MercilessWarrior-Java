package platformer.event.events.lancer;

import platformer.event.Event;
import platformer.model.entities.enemies.boss.Lancer;

/**
 * Published to control the Lancer's special aura effect.
 *
 * @param lancer The Lancer instance.
 * @param shouldBeActive True to spawn the aura, false to clear it.
 * @param effectType The type of effect to trigger.
 */
public record LancerAuraEvent(Lancer lancer, boolean shouldBeActive, EffectType effectType) implements Event {

    public enum EffectType {
        AURA_TOGGLE,
        BLADE_SWING,
        RAPID_THRUST,
        ORB_CAST,
        THUNDER_SLAM_IMPACT
    }

    public LancerAuraEvent(Lancer lancer, boolean shouldBeActive) {
        this(lancer, shouldBeActive, EffectType.AURA_TOGGLE);
    }

    public LancerAuraEvent(Lancer lancer, EffectType effectType) {
        this(lancer, false, effectType);
    }
}