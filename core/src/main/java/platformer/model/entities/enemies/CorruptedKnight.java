package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.projectiles.ProjectileFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

/**
 * Sir Dejanovic's void-corrupted Oath-Brothers.
 * Features a distinct moveset from regular Knights:
 * 1. Void Shadow-Step: Teleports behind the player when closing distance or retaliating from a hit.
 * 2. Void Crescent Wave (ATTACK_2): Conjures purple void fire and hurls a ranged crescent slash projectile.
 * 3. Relentless Void Cleave (ATTACK_1): Fast lunging greatsword strike with void afterimages.
 */
public class CorruptedKnight extends Enemy {

    private static final double VOID_WAVE_MIN_RANGE = KNIGHT_ATT_RANGE * 1.15;
    private static final double VOID_WAVE_MAX_RANGE = SIGHT_RANGE * 0.90;
    private static final double SHADOW_STEP_SPEED = 1.35 * SCALE;

    private int attackOffset;
    private int shadowStepCooldown = 0;
    private boolean isShadowStepping = false;

    private final double gravity = 0.020 * SCALE;
    private final double collisionFallSpeed = 0.6 * SCALE;

    public CorruptedKnight(int xPos, int yPos) {
        super(xPos, yPos, KNIGHT_WIDTH, KNIGHT_HEIGHT, EnemyType.CORRUPTED_KNIGHT, 19);
        initHitBox(KNIGHT_HB_WID, KNIGHT_HB_HEI);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, KNIGHT_AB_WID, KNIGHT_AB_HEI);
        this.attackOffset = (int) (20 * SCALE);
    }

    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();

        if (currentHealth <= 0) {
            checkDeath();
            return true;
        }

        isShadowStepping = false;
        setEnemyAction(Anim.HIT);
        if (pushOffset == 0) pushOffsetDirection = Direction.UP;
        enemySpeed = ENEMY_SPEED_SLOW;
        return true;
    }

    @Override
    public void spellHit(double damage) {
        if (entityState == Anim.DEATH || !alive) return;
        currentHealth -= damage;
        if (currentHealth <= 0) {
            checkDeath();
        }
        else if (!isAttacking()) {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
        enemySpeed = 0;
    }

    private void checkDeath() {
        isShadowStepping = false;
        Audio.getInstance().getAudioPlayer().playSound(Sound.SKELETON_DEATH_1);
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE -> idleAction();
            case RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> voidCleaveAction(levelData, target);
            case ATTACK_2 -> voidCrescentWaveAction(target);
            case HIT -> hitAction(levelData, target);
            default -> {}
        }
    }

    private void idleAction() {
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            setEnemyAction(Anim.WALK);
        }
    }

    private void moveAction(int[][] levelData, Entity entity) {
        if (canSeeEntity(levelData, entity)) {
            directToEntity(entity);
            enemySpeed = KNIGHT_SPEED_FAST * 0.90;
            double dist = Math.abs(entity.getHitBox().getCenterX() - hitBox.getCenterX());

            if (shadowStepCooldown == 0 && cooldown[Cooldown.ATTACK.ordinal()] == 0
                    && dist > KNIGHT_ATT_RANGE * 1.6 && dist < TILES_SIZE * 4.5 && rand.nextDouble() < 0.005) {
                if (performShadowStepBehind(levelData, entity)) {
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
            }

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (dist <= KNIGHT_ATT_RANGE) {
                    isShadowStepping = true;
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                else if (dist >= VOID_WAVE_MIN_RANGE && dist <= VOID_WAVE_MAX_RANGE && rand.nextDouble() < 0.008) {
                    setEnemyAction(Anim.ATTACK_2);
                    return;
                }
            }
        }

        double enemyXSpeed = (direction == Direction.LEFT) ? -enemySpeed : enemySpeed;
        if (canMoveHere(hitBox.x + enemyXSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            if (isFloor(hitBox, enemyXSpeed, levelData, direction)) {
                hitBox.x += enemyXSpeed;
                return;
            }
        }
        changeDirection();
    }

    private void voidCleaveAction(int[][] levelData, Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
        }
        else if ((animIndex == 1 || animIndex == 3) && checkPlayerParry(target)) {
            isShadowStepping = false;
            attackCheck = true;
            return;
        }
        else if (animIndex == 3 || animIndex == 4) {
            double dx = (direction == Direction.LEFT) ? -SHADOW_STEP_SPEED : SHADOW_STEP_SPEED;
            if (animTick % 2 == 0 && canMoveHere(hitBox.x + dx, hitBox.y, hitBox.width, hitBox.height, levelData)
                    && isFloor(hitBox, dx, levelData, direction)) {
                hitBox.x += dx;
            }
            if (animIndex == 4 && !attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
        else if (animIndex == 7) {
            isShadowStepping = false;
        }
    }

    /**
     * Row 4 (ATTACK_2): Conjures purple void fire in hand (frames 1..3) and hurls a Void Crescent projectile on frame 4,
     * while also slashing any target standing directly in front of the blade.
     */
    private void voidCrescentWaveAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            faceEntity(target);
        }
        else if ((animIndex == 1 || animIndex == 3) && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        else if (animIndex == 4 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createVoidSlash(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
            if (attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
            }
            attackCheck = true;
        }
    }

    private void hitAction(int[][] levelData, Entity target) {
        pushBack(pushDirection, levelData, 1.2, enemySpeed);
        if (animIndex == 3 && shadowStepCooldown == 0 && cooldown[Cooldown.ATTACK.ordinal()] == 0 && rand.nextDouble() < 0.12) {
            if (performShadowStepBehind(levelData, target)) {
                setEnemyAction(Anim.ATTACK_1);
            }
        }
    }

    private boolean performShadowStepBehind(int[][] levelData, Entity target) {
        double targetCenterX = target.getHitBox().getCenterX();
        double side = (hitBox.getCenterX() < targetCenterX) ? 1.0 : -1.0;
        double destX = targetCenterX + (side * 1.4 * TILES_SIZE) - (hitBox.width / 2.0);
        Direction moveDir = (destX > hitBox.x) ? Direction.RIGHT : Direction.LEFT;

        if (canMoveHere(destX, hitBox.y, hitBox.width, hitBox.height, levelData)
                && isFloor(new Rectangle2D.Double(destX, hitBox.y, hitBox.width, hitBox.height), 0, levelData, moveDir)) {
            hitBox.x = destX;
            faceEntity(target);
            isShadowStepping = true;
            shadowStepCooldown = 800;
            Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
            return true;
        }
        return false;
    }

    private boolean checkPlayerParry(Entity target) {
        if (target instanceof Player player) {
            boolean block = player.checkAction(PlayerAction.BLOCK);
            boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
            if (block && !canBlock) {
                boolean facingEnemy = (player.getHitBox().x < hitBox.x && player.getFlipSign() == 1)
                        || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1);
                if (facingEnemy) {
                    player.addAction(PlayerAction.CAN_BLOCK);
                    Audio.getInstance().getAudioPlayer().playBlockSound("Player");
                    return true;
                }
            }
        }
        return false;
    }

    private void faceEntity(Entity entity) {
        if (entity.getHitBox().getCenterX() > hitBox.getCenterX()) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (shadowStepCooldown > 0) shadowStepCooldown--;
        if (handleStunTick(levelData, gravity, collisionFallSpeed)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity entity) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, entity);
    }

    private void updateAttackBox() {
        this.attackBox.x = hitBox.x - attackOffset;
        this.attackBox.y = hitBox.y;
    }

    public boolean isShadowStepping() {
        return isShadowStepping || entityState == Anim.ATTACK_1;
    }

    @Override
    protected void reset() {
        super.reset();
        shadowStepCooldown = 0;
        isShadowStepping = false;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }
}