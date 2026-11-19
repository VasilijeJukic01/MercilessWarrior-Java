package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

/**
 * Elite armored enemy featuring hyper-armor poise during attack windups, a heavy greatsword cleave, and a mid-range teal arcane surge that can chain as a 2-hit combo.
 */
public class Knight extends Enemy {

    private static final double ARCANE_SLASH_RANGE = KNIGHT_ATT_RANGE * 1.45;
    private static final double CLEAVE_LUNGE_SPEED = 1.05 * SCALE;
    private static final double ARCANE_SURGE_SPEED = 1.45 * SCALE;

    private int attackOffset;
    private boolean hasHyperArmor = true;
    private boolean chainIntoArcaneSlash = false;

    // Physics
    private final double gravity = 0.020 * SCALE;
    private final double collisionFallSpeed = 0.6 * SCALE;

    public Knight(int xPos, int yPos) {
        super(xPos, yPos, KNIGHT_WIDTH, KNIGHT_HEIGHT, EnemyType.KNIGHT, 25);
        initHitBox(KNIGHT_HB_WID, KNIGHT_HB_HEI);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, KNIGHT_AB_WID, KNIGHT_AB_HEI);
        this.attackOffset = (int) (20 * SCALE);
    }

    // Attack
    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
            return true;
        }
        if (isAttacking() && hasHyperArmor && freezeTick == 0) {
            hasHyperArmor = false;
            Audio.getInstance().getAudioPlayer().playBlockSound("Enemy");
            return true;
        }
        chainIntoArcaneSlash = false;
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
        chainIntoArcaneSlash = false;
        Audio.getInstance().getAudioPlayer().playSound(Sound.SKELETON_DEATH_1);
        setEnemyAction(Anim.DEATH);
    }

    // Core
    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE -> idleAction();
            case RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> cleaveAttackAction(levelData, target);
            case ATTACK_2 -> arcaneSlashAction(levelData, target);
            case HIT -> pushBack(pushDirection, levelData, 1.1, enemySpeed);
            default -> {}
        }
    }

    // Behavior
    private void idleAction() {
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            hasHyperArmor = true;
            setEnemyAction(Anim.WALK);
        }
    }

    private void moveAction(int[][] levelData, Entity entity) {
        if (canSeeEntity(levelData, entity)) {
            directToEntity(entity);
            double dist = Math.abs(entity.getHitBox().getCenterX() - hitBox.getCenterX());
            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (dist <= KNIGHT_ATT_RANGE) {
                    hasHyperArmor = true;
                    chainIntoArcaneSlash = rand.nextDouble() < 0.25;
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                else if (dist <= ARCANE_SLASH_RANGE && rand.nextDouble() < 0.012) {
                    hasHyperArmor = true;
                    chainIntoArcaneSlash = false;
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

    private void cleaveAttackAction(int[][] levelData, Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
        }
        else if ((animIndex == 1 || animIndex == 3) && checkPlayerParry(target)) {
            chainIntoArcaneSlash = false;
            attackCheck = true;
        }
        else if (animIndex == 4) {
            performForwardStep(levelData, CLEAVE_LUNGE_SPEED);
            if (!attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
        else if (animIndex == 7 && chainIntoArcaneSlash) {
            chainIntoArcaneSlash = false;
            faceEntity(target);
            setEnemyAction(Anim.ATTACK_2);
        }
    }

    private void arcaneSlashAction(int[][] levelData, Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
        }
        else if ((animIndex == 2 || animIndex == 3) && checkPlayerParry(target)) {
            attackCheck = true;
        }
        else if (animIndex == 4 || animIndex == 5) {
            performForwardStep(levelData, ARCANE_SURGE_SPEED);
            if (!attackCheck) {
                double damage = getEnemyType().getDamage() * 1.15;
                if (attackBox.intersects(target.getHitBox())) {
                    if (target instanceof Player player && !player.checkAction(PlayerAction.CAN_BLOCK)) {
                        player.changeHealth(-damage, this);
                    }
                }
                attackCheck = true;
            }
        }
    }

    private boolean checkPlayerParry(Entity target) {
        if (target instanceof Player player) {
            boolean block = player.checkAction(PlayerAction.BLOCK);
            boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
            if (block && !canBlock) {
                boolean facingKnight = (player.getHitBox().x < hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1);
                if (facingKnight) {
                    hasHyperArmor = false;
                    player.addAction(PlayerAction.CAN_BLOCK);
                    Audio.getInstance().getAudioPlayer().playBlockSound("Player");
                    return true;
                }
            }
        }
        return false;
    }

    private void performForwardStep(int[][] levelData, double stepSpeed) {
        if (animTick % 2 != 0) return;
        double dx = (direction == Direction.LEFT) ? -stepSpeed : stepSpeed;
        if (canMoveHere(hitBox.x + dx, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, dx, levelData, direction)) {
            hitBox.x += dx;
        }
    }

    private void faceEntity(Entity entity) {
        if (entity.getHitBox().getCenterX() > hitBox.getCenterX()) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (handleStunTick(levelData, gravity, collisionFallSpeed)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) updatePushOffset();
    }

    public void updateMove(int[][] levelData, Entity entity) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, entity);
    }

    private void updateAttackBox() {
        if (entityState == Anim.ATTACK_2) {
            attackBox.width = (int) (KNIGHT_AB_WID * 1.25);
            if (direction == Direction.RIGHT) {
                attackBox.x = hitBox.x - (10 * SCALE);
            }
            else attackBox.x = hitBox.x + hitBox.width - attackBox.width + (10 * SCALE);
        }
        else {
            attackBox.width = KNIGHT_AB_WID;
            attackBox.x = hitBox.x - attackOffset;
        }
        attackBox.y = hitBox.y;
    }

    @Override
    protected void reset() {
        super.reset();
        hasHyperArmor = true;
        chainIntoArcaneSlash = false;
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