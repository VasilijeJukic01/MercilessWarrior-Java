package platformer.event.events.roric;

import platformer.event.Event;
import platformer.model.entities.enemies.boss.Roric;

/**
 * A generic event for triggering various visual effects related to Roric.
 *
 * @param roric      The Roric instance associated with the event.
 * @param effectType The type of effect being triggered.
 */
public record RoricEffectEvent(Roric roric, RoricEffectType effectType) implements Event {
    public enum RoricEffectType {
        JUMP,
        LAND,
        REPOSITIONING,
        BEAM_CHARGE_START,
        BEAM_CHARGE_END,
        CELESTIAL_RAIN_START,
        CELESTIAL_RAIN_END
    }
}