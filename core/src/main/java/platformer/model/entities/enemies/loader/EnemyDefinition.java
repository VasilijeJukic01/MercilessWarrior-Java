package platformer.model.entities.enemies.loader;

import lombok.Getter;
import platformer.audio.types.Sound;

import java.util.Collections;
import java.util.Map;

import static platformer.constants.Constants.*;

/**
 * Data-driven definition describing an enemy's sprite sheet, animation layout, dimensions, physics, and combat parameters.
 */
@Getter
public class EnemyDefinition {

    @Getter
    public static class AnimSpec {
        private int row;
        private int frames;
        private int offset = 0;
        private boolean reverse = false;
        private String aliasOf;
    }

    private String spriteSheet;
    private int spriteFrameW;
    private int spriteFrameH;
    private double drawW;
    private double drawH;
    private double xOffset;
    private double yOffset;
    private double hitboxW;
    private double hitboxH;
    private double attackBoxW;
    private double attackBoxH;

    private double gravity = 0.03;
    private double collisionFallSpeed = 0.5;
    private double walkSpeed = 0.2;
    private double runSpeed = 0.35;
    private double sightRangeTiles = 6.25;
    private double attackRangeTiles = 1.0;
    private double minSpacingTiles = 0.85;
    private double retreatSpacingTiles = 0.55;

    private int defaultAnimSpeed = 25;
    private int runAnimSpeed = 15;
    private int walkAnimSpeed = 23;
    private int attackAnimSpeed = 18;
    private int hitAnimSpeed = 14;

    private double attackCooldownMin = 14.0;
    private double attackCooldownMax = 22.0;
    private int attackObjectBreakFrame = -1;
    private int parryWindupStart = -1;
    private int parryWindupEnd = -1;

    private String fearFlag;
    private double fearChance = 0.0;

    private boolean undead = false;
    private boolean boss = false;
    private boolean multiFrameHitCheck = false;
    private int attackTokenCost = 1;
    private String questTarget;
    private String deathSound;
    private Map<String, AnimSpec> animations = Collections.emptyMap();

    public int getDrawWidth() {
        return scale(drawW);
    }

    public int getDrawHeight() {
        return scale(drawH);
    }

    public int getXOffset() {
        return scale(xOffset);
    }

    public int getYOffset() {
        return scale(yOffset);
    }

    public int getHitboxWidth() {
        return scale(hitboxW);
    }

    public int getHitboxHeight() {
        return scale(hitboxH);
    }

    public int getAttackBoxWidth() {
        return scale(attackBoxW);
    }

    public int getAttackBoxHeight() {
        return scale(attackBoxH);
    }

    public double getScaledGravity() {
        return gravity * SCALE;
    }

    public double getScaledCollisionFallSpeed() {
        return collisionFallSpeed * SCALE;
    }

    public double getScaledWalkSpeed() {
        return walkSpeed * SCALE;
    }

    public double getScaledRunSpeed() {
        return runSpeed * SCALE;
    }

    public double getSightRange() {
        return sightRangeTiles * TILES_SIZE;
    }

    public double getAttackRange() {
        return attackRangeTiles * TILES_SIZE;
    }

    public double getMinSpacing() {
        return minSpacingTiles * TILES_SIZE;
    }

    public double getRetreatSpacing() {
        return retreatSpacingTiles * TILES_SIZE;
    }

    public String getQuestTargetOrDefault(String fallback) {
        return (questTarget != null && !questTarget.isBlank()) ? questTarget : fallback;
    }

    public Sound getDeathSoundEnum() {
        if (deathSound == null || deathSound.isEmpty()) return null;
        try {
            return Sound.valueOf(deathSound);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}