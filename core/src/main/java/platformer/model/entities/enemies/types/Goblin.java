package platformer.model.entities.enemies.types;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.ProjectileFactory;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Polymorphic Goblin enemy supporting multiple archetypes via {@link EnemyType}:
 * - GOBLIN: Fast melee axe skirmisher.
 * - GOBLIN_SOLDIER: Shield-blocking frontline brawler.
 * - GOBLIN_ASSASSIN: Quick dagger striker that lobs arcing explosive bombs at mid-range.
 * - GOBLIN_SPEARMAN: Mid/long-range javelin thrower that keeps distance.
 * - GOBLIN_MAGE: Skull-staff shaman with a close-range purple pulse and long-range arcane orbs.
 */
public class Goblin extends Enemy {

    private final EnemyType variant;

    public Goblin(int xPos, int yPos, EnemyType variant) {
        super(xPos, yPos, variant);
        this.variant = variant;
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
    }

    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        if (enableBlock && variant == EnemyType.GOBLIN_SOLDIER && rand.nextDouble() < 0.30) {
            setEnemyAction(Anim.BLOCK);
            Audio.getInstance().getAudioPlayer().playBlockSound("Enemy");
            return true;
        }
        currentHealth -= damage;
        releaseCombatToken();
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            setEnemyAction(Anim.HIT);
            if (pushOffset == 0) pushOffsetDirection = Direction.UP;
        }
        enemySpeed = definition.getScaledWalkSpeed();
        return true;
    }

    @Override
    public void spellHit(double damage) {
        if (entityState == Anim.DEATH || !alive) return;
        currentHealth -= damage;
        enemySpeed = 0;
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
    }

    private void checkDeath() {
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK, BLOCK -> moveAction(levelData, target);
            case ATTACK_1 -> primaryAttackAction(levelData, target);
            case ATTACK_2 -> bombThrowAction(target);
            case SPELL_1 -> mageSpellAction(target);
            case HIT -> pushBack(pushDirection, levelData, 1.5, enemySpeed);
            default -> {}
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            if (isTerrified()) {
                fleeFromEntity(levelData, target);
                return;
            }
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            double attackRange = definition.getAttackRange();

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                switch (variant) {
                    case GOBLIN, GOBLIN_SOLDIER -> {
                        if (dist <= attackRange && requestMeleeToken()) {
                            faceEntity(target);
                            setEnemyAction(Anim.ATTACK_1);
                            return;
                        }
                    }
                    case GOBLIN_ASSASSIN -> {
                        if (dist <= attackRange && requestMeleeToken()) {
                            faceEntity(target);
                            setEnemyAction(Anim.ATTACK_1);
                            return;
                        }
                        else if (dist > attackRange * 1.4 && dist <= definition.getSightRange() * 0.90 && rand.nextDouble() < 0.025 && requestRangedToken()) {
                            faceEntity(target);
                            setEnemyAction(Anim.ATTACK_2);
                            return;
                        }
                    }
                    case GOBLIN_SPEARMAN -> {
                        if (dist <= definition.getSightRange() * 0.95 && requestRangedToken()) {
                            faceEntity(target);
                            setEnemyAction(Anim.ATTACK_1);
                            return;
                        }
                    }
                    case GOBLIN_MAGE -> {
                        if (dist <= attackRange && requestMeleeToken()) {
                            faceEntity(target);
                            setEnemyAction(Anim.ATTACK_1);
                            return;
                        }
                        else if (dist > attackRange * 1.25 && dist <= definition.getSightRange() * 0.95 && requestRangedToken()) {
                            faceEntity(target);
                            setEnemyAction(Anim.SPELL_1);
                            return;
                        }
                    }
                    default -> {}
                }
            }

            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }
        patrolStep(levelData);
    }

    private void primaryAttackAction(int[][] levelData, Entity target) {
        if (animIndex == 0) {
            faceEntity(target);
        }

        if (variant == EnemyType.GOBLIN_SPEARMAN) {
            if (animIndex == 3 && !attackCheck) {
                faceEntity(target);
                ProjectileFactory.createGoblinSpear(this);
                Audio.getInstance().getAudioPlayer().playSound(Sound.ARROW);
                attackCheck = true;
            }
            return;
        }

        if (variant == EnemyType.GOBLIN_MAGE) {
            if (animIndex >= 1 && animIndex <= 5 && !attackCheck && checkPlayerParry(target)) {
                attackCheck = true;
            }
            else if ((animIndex == 6 || animIndex == 7) && !attackCheck) {
                checkEntityHit(attackBox, target);
            }
            return;
        }

        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
        }
        else if (animIndex == 3) {
            if (variant == EnemyType.GOBLIN_ASSASSIN && animTick % 2 == 0) {
                stepForwardClamped(levelData, target, 1.1 * SCALE);
            }
            if (!attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
    }

    private void bombThrowAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            faceEntity(target);
        }
        else if (animIndex == 5 && !attackCheck) {
            faceEntity(target);
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            ProjectileFactory.createGoblinBomb(this, dist);
            Audio.getInstance().getAudioPlayer().playSound(Sound.ARROW);
            attackCheck = true;
        }
    }

    private void mageSpellAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            faceEntity(target);
        }
        else if (animIndex == 9 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createGoblinMageBall(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
            attackCheck = true;
        }
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (handleStunTick(levelData)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateForwardAttackBox(definition.getAttackBoxWidth());
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }
}