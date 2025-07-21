package platformer.model.effects.particles;

import platformer.model.effects.particles.behaviors.*;
import platformer.model.entities.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating {@link DustParticle} instances with appropriate behaviors.
 * <p>
 * This class uses the Factory and Strategy patterns.
 * It encapsulates the logic for selecting the correct {@link ParticleBehavior} for a given {@link DustType}.
 * It also caches stateless behavior objects to improve performance by reducing object instantiation.
 */
public class ParticleFactory {

    private static final Map<DustType, ParticleBehavior> behaviorCache = new HashMap<>();

    static {
        behaviorCache.put(DustType.IMPACT, new ImpactBehavior());
        behaviorCache.put(DustType.RUNNING, new RunningBehavior());
        behaviorCache.put(DustType.DASH, new DashBehavior());
        behaviorCache.put(DustType.WALL_SLIDE, new WallSlideBehavior());
        behaviorCache.put(DustType.WALL_JUMP, new WallJumpBehavior());
        behaviorCache.put(DustType.IMPACT_SPARK, new ImpactSparkBehavior());
        behaviorCache.put(DustType.CRITICAL_HIT, new CriticalHitBehavior());
        behaviorCache.put(DustType.PLAYER_HIT, new PlayerHitBehavior());
        behaviorCache.put(DustType.SW_TELEPORT, new TeleportBehavior());
        behaviorCache.put(DustType.SW_CHANNELING_AURA, new ChannelingAuraBehavior());
        behaviorCache.put(DustType.SW_AURA_PULSE, new AuraPulseBehavior());
        behaviorCache.put(DustType.SW_AURA_CRACKLE, new AuraCrackleBehavior());
        behaviorCache.put(DustType.SW_DASH_SLASH, new DashSlashBehavior());
        behaviorCache.put(DustType.JUMP_PAD, new JumpPadBehavior());
        behaviorCache.put(DustType.THUNDERBOLT_AURA, new MythicAuraBehavior());
        behaviorCache.put(DustType.HERB_CUT, new HerbCutBehavior());
    }

    /**
     * Creates and initializes a DustParticle.
     *
     * @param x The initial x-coordinate.
     * @param y The initial y-coordinate.
     * @param size The size of the particle.
     * @param type The {@link DustType} which determines the particle's behavior.
     * @param playerFlipSign The direction the player is facing (-1 for left, 1 for right).
     * @param target An optional target entity for behaviors that track an entity (like auras).
     * @return A fully initialized DustParticle instance.
     */
    public static DustParticle createParticle(int x, int y, int size, DustType type, int playerFlipSign, Entity target) {
        ParticleBehavior behavior = behaviorCache.get(type);
        // Fallback
        if (behavior == null) behavior = new ImpactBehavior();
        return new DustParticle(x, y, size, type, behavior, target, playerFlipSign);
    }
}