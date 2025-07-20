package platformer.model.entities.enemies.boss.roric;

/**
 * Defines the possible states for the Roric boss AI.
 * This is used to manage its behavior in a state machine pattern.
 */
public enum RoricState {
    IDLE,
    JUMPING,
    AERIAL_ATTACK,
    REPOSITIONING,
    BEAM_ATTACK,
    ARROW_RAIN,
    ARROW_ATTACK,
    PHANTOM_BARRAGE,
    SKYFALL_BARRAGE,
    CELESTIAL_RAIN
}
