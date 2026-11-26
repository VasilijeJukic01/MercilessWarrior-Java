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

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.*;

/**
 * Crowned Elder Ashen Ghoul.
 * Features a 13-frame Phantom Flurry Rush (ATTACK_2, Row 3), a White-Hot Crescent Claw (ATTACK_1, Row 5),
 * and proactive Ash-Sink Teleportation (HIDE -> REVEAL -> instant ambush strike).
 */
public class AshenGhoul extends Enemy {

    private static final double FLURRY_GLIDE_SPEED = 1.45 * SCALE;
    private static final double REVEAL_OFFSET_DIST = 1.65 * TILES_SIZE;

    private int burrowCooldown = 0;
    private boolean ambushOnReveal = false;
    private boolean secondFlurryHitChecked = false;

    public AshenGhoul(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.ASHEN_GHOUL);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
    }

    @Override
    public boolean hit(double damage, boolean enableRevive, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        if (enableRevive && burrowCooldown == 0 && currentHealth < maxHealth * 0.80 && rand.nextDouble() < 0.35) {
            triggerAshBurrow(true);
            return false;
        }
        currentHealth -= damage;
        releaseCombatToken();
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) checkDeath();
        else setEnemyAction(Anim.HIT);
        return true;
    }

    @Override
    public void spellHit(double damage) {
        if (entityState == Anim.DEATH || !alive || entityState == Anim.HIDE) return;
        currentHealth -= damage;
        releaseCombatToken();
        enemySpeed = 0;
        if (currentHealth <= 0) checkDeath();
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
    }

    private void triggerAshBurrow(boolean queueAmbush) {
        burrowCooldown = 480;
        ambushOnReveal = queueAmbush;
        releaseCombatToken();
        setEnemyAction(Anim.HIDE);
        Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_HIDE);
    }

    private void checkDeath() {
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> crescentClawAction(levelData, target);
            case ATTACK_2 -> phantomFlurryAction(levelData, target);
            case REVEAL -> revealAction(levelData, target);
            case HIT -> pushBack(pushDirection, levelData, 1.15, enemySpeed);
            default -> {}
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            double attackRange = definition.getAttackRange();

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (burrowCooldown == 0 && dist > TILES_SIZE * 2.2 && dist <= TILES_SIZE * 5.0 && rand.nextDouble() < 0.018 && requestMeleeToken()) {
                    triggerAshBurrow(true);
                    return;
                }
                if (dist <= TILES_SIZE * 1.45 && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                if (dist > TILES_SIZE * 1.45 && dist <= attackRange && requestMeleeToken()) {
                    faceEntity(target);
                    secondFlurryHitChecked = false;
                    setEnemyAction(Anim.ATTACK_2);
                    return;
                }
            }

            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }
        patrolStep(levelData);
    }

    private void crescentClawAction(int[][] levelData, Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        if (animIndex >= 2 && animIndex <= 4) {
            stepForwardClamped(levelData, target, definition.getScaledRunSpeed() * 1.5);
            if ((animIndex == 3 || animIndex == 4) && !attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
    }

    /**
     * Executes Row 3 (13-frame Phantom Flurry Rush): glides forward and slashes on frame 6 and frame 10.
     */
    private void phantomFlurryAction(int[][] levelData, Entity target) {
        if (animIndex >= 4 && animIndex <= 6 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            secondFlurryHitChecked = true;
            return;
        }
        if (animIndex >= 5 && animIndex <= 11) {
            stepForwardClamped(levelData, target, FLURRY_GLIDE_SPEED);
        }
        if ((animIndex == 6 || animIndex == 7) && !attackCheck) {
            checkEntityHit(attackBox, target);
            attackCheck = true;
        }
        else if ((animIndex == 10 || animIndex == 11) && !secondFlurryHitChecked) {
            if ( !checkPlayerParry(target) ) {
                attackCheck = false;
                checkEntityHit(attackBox, target);
            }
            secondFlurryHitChecked = true;
        }
    }

    private void revealAction(int[][] levelData, Entity target) {
        if (animIndex == 0 && animTick <= 1) {
            double targetCenterX = target.getHitBox().getCenterX();
            double dirSign = (target.getFlipSign() == 1) ? -1.0 : 1.0;
            double[] offsets = {dirSign * REVEAL_OFFSET_DIST, -dirSign * REVEAL_OFFSET_DIST};

            for (double off : offsets) {
                double candX = targetCenterX + off - (hitBox.width / 2.0);
                if (canMoveHere(candX, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(new Rectangle2D.Double(candX, hitBox.y, hitBox.width, hitBox.height), 0, levelData, Direction.RIGHT)) {
                    hitBox.x = candX;
                    break;
                }
            }
            faceEntity(target);
        }
        else if (animIndex == 5 && animTick == 0) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_REVEAL);
        }
        else if (animIndex >= 16 && ambushOnReveal) {
            ambushOnReveal = false;
            faceEntity(target);
            setEnemyAction(Anim.ATTACK_1);
        }
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (burrowCooldown > 0) burrowCooldown--;
        if (handleStunTick(levelData)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateForwardAttackBox(definition.getAttackBoxWidth());
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }

    @Override
    protected void reset() {
        super.reset();
        burrowCooldown = 0;
        ambushOnReveal = false;
        secondFlurryHitChecked = false;
    }
}