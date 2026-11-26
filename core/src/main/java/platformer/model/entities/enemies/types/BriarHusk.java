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
import platformer.model.entities.player.PlayerAction;
import platformer.model.projectiles.ProjectileFactory;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.canMoveHere;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Vine-mutated forest husk featuring a Vine-Hook Pull (ATTACK_1) that drags the player
 * into an immediate close-range Twin-Vine Overhead Smash (ATTACK_2) or Erupting Ground Briars (SPELL_1) combo.
 */
public class BriarHusk extends Enemy {

    private static final double VINE_HOOK_REACH = 1.75 * TILES_SIZE;
    private static final double GROUND_BRIAR_REACH = 1.45 * TILES_SIZE;
    private static final double PULL_SPEED = 2.6 * SCALE;

    private Entity hookedTarget = null;
    private int pullTicks = 0;
    private boolean comboQueued = false;

    public BriarHusk(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.BRIAR_HUSK);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
    }

    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        clearHook();
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
        clearHook();
        releaseCombatToken();
        enemySpeed = 0;
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
    }

    private void clearHook() {
        hookedTarget = null;
        pullTicks = 0;
        comboQueued = false;
    }

    private void checkDeath() {
        clearHook();
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        updateActivePull(levelData);

        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> vineHookPullAction(target);
            case ATTACK_2 -> clawSmashAction(target);
            case ATTACK_3 -> thornDartAction(target);
            case SPELL_1 -> groundBriarAction(target);
            case HIT -> pushBack(pushDirection, levelData, 1.3, enemySpeed);
            default -> {}
        }
    }

    /**
     * Reels the hooked target across the floor right up to the Briar Husk's claws.
     */
    private void updateActivePull(int[][] levelData) {
        if (pullTicks <= 0 || hookedTarget == null) return;
        pullTicks--;

        Rectangle2D.Double tBox = hookedTarget.getHitBox();
        double desiredStopX = (direction == Direction.RIGHT) ? (hitBox.getMaxX() + 4 * SCALE) : (hitBox.x - tBox.width - 4 * SCALE);
        double diff = desiredStopX - tBox.x;

        if (Math.abs(diff) <= PULL_SPEED) {
            if (canMoveHere(desiredStopX, tBox.y, tBox.width, tBox.height, levelData)) {
                tBox.x = desiredStopX;
            }
            pullTicks = 0;
            return;
        }

        double step = Math.signum(diff) * PULL_SPEED;
        if (canMoveHere(tBox.x + step, tBox.y, tBox.width, tBox.height, levelData)) {
            tBox.x += step;
        }
        else {
            pullTicks = 0;
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            double pointBlankRange = 0.85 * TILES_SIZE;

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                // Point-blank range: Twin-Vine Smash or Ground Briars
                if (dist <= pointBlankRange && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(rand.nextBoolean() ? Anim.ATTACK_2 : Anim.SPELL_1);
                    return;
                }
                // Vine-Hook range (0.85 to 1.75 tiles)
                if (dist > pointBlankRange && dist <= VINE_HOOK_REACH && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                // Long range: Thorn Dart Spit
                if (dist > VINE_HOOK_REACH * 1.15 && dist <= definition.getSightRange() * 0.85 && rand.nextDouble() < 0.018 && requestRangedToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.ATTACK_3);
                    return;
                }
            }

            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }
        patrolStep(levelData);
    }

    private void vineHookPullAction(Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            clearHook();
            attackCheck = true;
            return;
        }

        if (animIndex >= 2 && animIndex <= 6 && !attackCheck) {
            if (attackBox.intersects(target.getHitBox())) {
                if (target instanceof Player player) {
                    if (checkPlayerParry(player)) {
                        clearHook();
                        attackCheck = true;
                        return;
                    }
                    if (!player.checkAction(PlayerAction.CAN_BLOCK)) {
                        // Status damage so player is pulled in instead of knocked away!
                        player.applyStatusDamage(-6.0);
                        Audio.getInstance().getAudioPlayer().playHitSound();
                    }
                }
                this.hookedTarget = target;
                this.pullTicks = 18;
                this.comboQueued = true;
                attackCheck = true;
            }
            else if (animIndex == 6) {
                attackCheck = true;
            }
        }

        if (animIndex >= 8 && comboQueued) {
            comboQueued = false;
            faceEntity(target);
            setEnemyAction(rand.nextBoolean() ? Anim.ATTACK_2 : Anim.SPELL_1);
        }
    }

    private void clawSmashAction(Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        if ((animIndex == 2 || animIndex == 3) && !attackCheck) {
            checkEntityHit(attackBox, target);
        }
    }

    private void thornDartAction(Entity target) {
        if (animIndex == 0) {
            faceEntity(target);
        }
        if (animIndex == 3 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createThornDart(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.ARROW);
            attackCheck = true;
        }
    }

    private void groundBriarAction(Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        if (animIndex >= 3 && animIndex <= 5 && !attackCheck) {
            checkEntityHit(attackBox, target);
        }
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (handleStunTick(levelData)) {
            clearHook();
            return;
        }
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateDynamicAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }

    private void updateDynamicAttackBox() {
        double reach = definition.getAttackBoxWidth();
        if (entityState == Anim.ATTACK_1) {
            reach = VINE_HOOK_REACH;
        }
        else if (entityState == Anim.SPELL_1) {
            reach = GROUND_BRIAR_REACH;
        }
        updateForwardAttackBox(reach);
    }

    @Override
    protected void reset() {
        super.reset();
        clearHook();
    }
}