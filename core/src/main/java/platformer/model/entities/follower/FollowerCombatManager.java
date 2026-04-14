package platformer.model.entities.follower;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.physics.CollisionDetector;

import static platformer.constants.Constants.*;

/**
 * Manages the follower's combat state, including poise, i-frames, stun-locking prevention, and knockdown logic.
 */
public class FollowerCombatManager {

    private final Follower follower;

    // Poise = how much damage can take while attacking before flinching
    private int currentPoise;
    private final int MAX_POISE = 50;

    // Stunlock prevention
    private int consecutiveStuns = 0;
    private int panicTimer = 0;
    private int iFrameTick = 0;
    private final int IFRAME_THRESHOLD = 80;

    private boolean isKnockedDown = false;
    public static final double DEATH_HITBOX_EXPANSION = 35 * SCALE;

    public FollowerCombatManager(Follower follower) {
        this.follower = follower;
        this.currentPoise = MAX_POISE;
    }

    public void update(int[][] levelData, Player player) {
        if (iFrameTick > 0) iFrameTick--;

        if (currentPoise < MAX_POISE && iFrameTick == 0) {
            currentPoise++;
        }

        if (panicTimer > 0) {
            panicTimer--;
            performPanicRetreat(levelData, player);
        }
    }

    public void handleHit(double damage, Entity attacker, int[][] currentLevelData) {
        if (isKnockedDown || iFrameTick > 0) return;

        follower.changeHealth(-damage);
        Audio.getInstance().getAudioPlayer().playHitSound();
        iFrameTick = IFRAME_THRESHOLD;

        // If attacking, damage reduces poise instead of stun
        if (follower.isAttacking() && currentPoise > 0) {
            currentPoise -= damage;
            applyPushbackDirection(attacker);
            follower.executePushBack(follower.getPushDirection(), currentLevelData, 0.5, 0.5);
            return;
        }

        consecutiveStuns++;

        // If stunned 3 times in a row, trigger panic
        if (consecutiveStuns >= 3) {
            panicTimer = 150;
            consecutiveStuns = 0;
            currentPoise = MAX_POISE;
        }

        if (follower.getCurrentHealth() <= 0) {
            knockDown();
        } else {
            triggerHitState(attacker);
        }
    }

    private void triggerHitState(Entity attacker) {
        follower.setEntityState(Anim.HIT);
        follower.resetAnimState();
        currentPoise = 0;

        follower.cancelActions();
        applyPushbackDirection(attacker);
        follower.setPushOffsetDirection(Direction.DOWN);
        follower.setPushOffset(0);
    }

    private void applyPushbackDirection(Entity attacker) {
        if (attacker != null) {
            follower.setPushDirection(attacker.getHitBox().x < follower.getHitBox().x ? Direction.RIGHT : Direction.LEFT);
        }
    }

    private void knockDown() {
        follower.changeHealth(-follower.getCurrentHealth()); // Ensure 0
        isKnockedDown = true;
        follower.setEntityState(Anim.DEATH);
        follower.resetAnimState();
        follower.cancelActions();

        follower.getHitBox().y += (FOLLOWER_HB_HEI / 2.0);
        follower.getHitBox().height = FOLLOWER_HB_HEI / 2.0;

        if (follower.getFlipSign() == 1) {
            follower.getHitBox().x -= DEATH_HITBOX_EXPANSION;
            follower.getHitBox().width = FOLLOWER_HB_WID + DEATH_HITBOX_EXPANSION;
        } else {
            follower.getHitBox().width = FOLLOWER_HB_WID + DEATH_HITBOX_EXPANSION;
        }
    }

    public void revive() {
        if (!isKnockedDown) return;
        isKnockedDown = false;
        follower.healFully();
        follower.setEntityState(Anim.IDLE);
        follower.resetAnimState();
        follower.setInAir(true);
        follower.setAirSpeed(-1.0 * SCALE);

        if (follower.getFlipSign() == 1) {
            follower.getHitBox().x += DEATH_HITBOX_EXPANSION;
        }
        follower.getHitBox().width = FOLLOWER_HB_WID;
        follower.getHitBox().y -= (FOLLOWER_HB_HEI / 2.0);
        follower.getHitBox().height = FOLLOWER_HB_HEI;
    }

    private void performPanicRetreat(int[][] levelData, Player player) {
        follower.cancelAttack();
        // Run towards player to reset position
        double dx = player.getHitBox().x - follower.getHitBox().x;
        int direction = (dx > 0) ? 1 : -1;
        follower.setDirection(direction == 1 ? Direction.RIGHT : Direction.LEFT);

        // Move fast
        double panicSpeed = follower.getWalkSpeed() * 1.5;
        if (CollisionDetector.canMoveHere(follower.getHitBox().x + (panicSpeed * direction), follower.getHitBox().y, follower.getHitBox().width, follower.getHitBox().height, levelData)) {
            follower.getHitBox().x += panicSpeed * direction;
            follower.setEntityState(Anim.RUN);
        }
    }

    public boolean isKnockedDown() {
        return isKnockedDown;
    }

    public boolean isPanicking() {
        return panicTimer > 0;
    }
}