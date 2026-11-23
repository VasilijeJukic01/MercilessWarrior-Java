package platformer.model.effects.decorations;

/**
 * Reusable, biome-agnostic ambient visual behaviors that can be attached to any decoration via decoration_effects.json.
 * Multiple behaviors can be stacked on a single decoration ID.
 */
public enum DecorationEffectType {
    SPARKLE,
    SPORE_DRIFT,
    FIREFLY_ORBIT,
    WATER_DRIP,
    SOUL_WISP,
    POLLEN_FLOAT,
    CHIMNEY_SMOKE,
    HEARTH_SPARK,
    CAVE_DUST,
    LEAF_SHED
}