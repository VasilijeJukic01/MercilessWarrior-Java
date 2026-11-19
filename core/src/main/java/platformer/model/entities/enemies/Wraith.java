package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.renderer.AfterimageTrail;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.projectiles.ProjectileFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

/**
 * Spectral apparition featuring three distinct attacks and an ethereal Phase-Blink:
 * - ATTACK_1: Forward spectral chain-whip lash.
 * - ATTACK_2): 360-degree AoE Soul Vortex when the player gets close.
 * - SPELL_1 : Ranged homing Spectral Soul Bolt volley at long distance.
 * - Phase-Blink: Fades into mist and repositions behind or away from the player.
 */
public class Wraith extends Enemy {

    private static final double VORTEX_CLOSE_RANGE = WRAITH_ATT_RANGE * 0.80;
    private static final double WHIP_LASH_RANGE = WRAITH_ATT_RANGE * 1.40;
    private static final double SOUL_BOLT_MAX_RANGE = SIGHT_RANGE * 0.95;

    private int blinkCooldown = 0;
    private float phaseAlpha = 1.0f;
    private double hoverPhase = Math.random() * Math.PI * 2.0;
    private boolean secondBoltFired = false;

    private final double gravity = 0.03 * SCALE;
    private final double collisionFallSpeed = 0.2 * SCALE;

    // Effect
    private final AfterimageTrail afterimageTrail = new AfterimageTrail(3, 4);

    public Wraith(int xPos, int yPos) {
        super(xPos, yPos, WRAITH_WIDTH, WRAITH_HEIGHT, EnemyType.WRAITH, 20);
        initHitBox(WRAITH_HB_WID, WRAITH_HB_HEI);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, WRAITH_AB_WID, WRAITH_AB_HEI);
    }

    // Attack
    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            setEnemyAction(Anim.HIT);
            if (pushOffset == 0) pushOffsetDirection = Direction.UP;
        }
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
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
        enemySpeed = 0;
    }

    private void checkDeath() {
        phaseAlpha = 1.0f;
        Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_DEATH);
        setEnemyAction(Anim.DEATH);
    }

    // Core
    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE -> idleAction();
            case RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1, ATTACK_2 -> meleeAttackAction(target);
            case SPELL_1 -> spellBoltAction(target);
            case HIT -> hitAction(levelData, target);
            default -> {}
        }
    }

    // Behavior
    private void idleAction() {
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            setEnemyAction(Anim.WALK);
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            directToEntity(target);
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            if (blinkCooldown == 0 && dist > WHIP_LASH_RANGE && dist < SOUL_BOLT_MAX_RANGE && rand.nextDouble() < 0.015) {
                if (tryPhaseBlink(levelData, target, true)) {
                    setEnemyAction(Anim.ATTACK_2);
                    return;
                }
            }

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (dist <= VORTEX_CLOSE_RANGE) {
                    setEnemyAction(Anim.ATTACK_2);
                    return;
                }
                else if (dist <= WHIP_LASH_RANGE) {
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                else if (dist <= SOUL_BOLT_MAX_RANGE && rand.nextDouble() < 0.045) {
                    secondBoltFired = false;
                    setEnemyAction(Anim.SPELL_1);
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

    private void meleeAttackAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
        }
        else if ((animIndex == 2 || animIndex == 3) && checkPlayerParry(target)) {
            attackCheck = true;
        }
        else if ((animIndex == 4 || animIndex == 5) && !attackCheck) {
            checkEntityHit(attackBox, target);
        }
    }

    private void spellBoltAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            secondBoltFired = false;
            faceEntity(target);
        }
        else if (animIndex == 4 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createWraithBolt(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
            attackCheck = true;
        }
        else if (animIndex == 6 && !secondBoltFired && currentHealth <= maxHealth * 0.6) {
            ProjectileFactory.createWraithBolt(this);
            secondBoltFired = true;
        }
    }

    private boolean checkPlayerParry(Entity target) {
        if (target instanceof Player player) {
            boolean block = player.checkAction(PlayerAction.BLOCK);
            boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
            if (block && !canBlock) {
                boolean facingWraith = (player.getHitBox().x < hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1);
                if (facingWraith) {
                    player.addAction(PlayerAction.CAN_BLOCK);
                    Audio.getInstance().getAudioPlayer().playBlockSound("Player");
                    return true;
                }
            }
        }
        return false;
    }

    private void hitAction(int[][] levelData, Entity target) {
        pushBack(pushDirection, levelData, 1.8, enemySpeed);
        if (animIndex == 3 && blinkCooldown == 0 && rand.nextDouble() < 0.45) {
            boolean blinkBehind = rand.nextBoolean();
            if (tryPhaseBlink(levelData, target, blinkBehind)) {
                if (blinkBehind) {
                    setEnemyAction(Anim.ATTACK_1);
                }
                else {
                    secondBoltFired = false;
                    setEnemyAction(Anim.SPELL_1);
                }
            }
        }
    }

    /**
     * Teleports the Wraith either behind the target for a surprise ambush or away from the target to cast SPELL_1.
     */
    private boolean tryPhaseBlink(int[][] levelData, Entity target, boolean behindTarget) {
        double targetCenterX = target.getHitBox().getCenterX();
        double dirFromTarget = (hitBox.getCenterX() < targetCenterX) ? -1.0 : 1.0;
        double desiredOffset = behindTarget ? (-dirFromTarget * 1.8 * TILES_SIZE) : (dirFromTarget * 3.6 * TILES_SIZE);
        double newX = targetCenterX + desiredOffset - (hitBox.width / 2.0);
        Direction moveDir = (newX > hitBox.x) ? Direction.RIGHT : Direction.LEFT;

        if (canMoveHere(newX, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(new Rectangle2D.Double(newX, hitBox.y, hitBox.width, hitBox.height), 0, levelData, moveDir)) {
            hitBox.x = newX;
            faceEntity(target);
            phaseAlpha = 0.20f;
            blinkCooldown = 280;
            Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_REVEAL);
            return true;
        }
        return false;
    }

    private void faceEntity(Entity entity) {
        if (entity.getHitBox().getCenterX() > hitBox.getCenterX()) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        hoverPhase += 0.045;
        if (blinkCooldown > 0) blinkCooldown--;
        if (phaseAlpha < 1.0f) {
            phaseAlpha = Math.min(1.0f, phaseAlpha + 0.025f);
        }

        if (handleStunTick(levelData, gravity, collisionFallSpeed)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    public void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, target);
    }

    private void updateAttackBox() {
        if (entityState == Anim.ATTACK_1) {
            attackBox.width = (int) (WRAITH_AB_WID * 0.85);
            if (direction == Direction.RIGHT) {
                attackBox.x = hitBox.x;
            }
            else attackBox.x = hitBox.x + hitBox.width - attackBox.width;
        }
        else {
            attackBox.width = (int) (WRAITH_AB_WID * 1.15);
            attackBox.x = hitBox.getCenterX() - (attackBox.width / 2.0);
        }
        attackBox.y = hitBox.y - (4 * SCALE);
    }

    public int getHoverOffsetY() {
        if (entityState == Anim.DEATH) return 0;
        return (int) Math.round(Math.sin(hoverPhase) * (3.0 * SCALE));
    }

    public float getPhaseAlpha() {
        return phaseAlpha;
    }

    public AfterimageTrail getAfterimageTrail() {
        return afterimageTrail;
    }

    @Override
    protected void reset() {
        super.reset();
        blinkCooldown = 0;
        phaseAlpha = 1.0f;
        secondBoltFired = false;
        afterimageTrail.clear();
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