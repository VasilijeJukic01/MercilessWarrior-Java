package platformer.model.entities.follower;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.follower.behavior.AnitaBehavior;
import platformer.model.entities.follower.behavior.FollowerBehavior;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.gameObjects.npc.NpcType;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.canMoveHere;

/**
 * Represents an intelligent NPC follower that tracks the player.
 * <p>
 * This entity uses a <b>Hybrid AI System</b>:
 * <ol>
 *     <li><b>Standard Pathfinding:</b> Simple horizontal tracking when the path is clear.</li>
 *     <li><b>Predictive Physics:</b> Uses quadratic projectile motion equations to calculate precise jump velocities required to cross gaps or climb ledges.</li>
 *     <li><b>Mimicry (Breadcrumbing):</b> If physics calculations fail (e.g., impossible jumps), it falls back to recording the player's successful
 *     actions and replaying them with enhanced physics capabilities.</li>
 * </ol>
 * This entity acts as the Body, handling physics and execution. The decision-making (Targeting, Combat) is delegated to a {@link FollowerBehavior} instance.
 */
public class Follower extends Entity implements Interactable {

    private final NpcType type;
    private final EffectManager effectManager;
    private final ObjectManager objectManager;

    // Components
    private final FollowerBehavior behavior;
    private final JumpHistoryTracker jumpTracker;
    private final TrajectoryCalculator trajectoryCalculator;
    private final FollowerCombatManager combatManager;

    private int[][] currentLevelData;
    private List<Enemy> enemyContext;

    // Settings
    private final int animSpeed = 25;
    private final double walkSpeed = 0.6 * SCALE;
    private final int teleportDist = TILES_SIZE * 20;
    private int animTick, animIndex;

    // Physics
    private final double gravity = 0.04 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;
    private final double jumpSpeed = -2.25 * SCALE;

    /**
     * If true, standard movement logic is suspended, and the NPC follows a calculated ballistic arc.
     */
    private boolean isJumpOverride = false;

    /**
     * The calculated horizontal velocity required to complete the current smart jump.
     */
    private double overrideSpeedX = 0;
    private int jumpDecisionTick = 0;

    private boolean isMoving = false;

    // Stuck Detection
    private double lastX = 0;
    private int stuckTick = 0;
    private final int STUCK_THRESHOLD = 200;

    // AI State
    private boolean isAttacking = false;
    private double targetX;
    private boolean attackRequested;

    public Follower(int x, int y, NpcType type, EffectManager effectManager, ObjectManager objectManager) {
        super(x, y, FLW_WIDTH, FLW_HEIGHT, 100);
        this.type = type;
        this.effectManager = effectManager;
        this.objectManager = objectManager;

        initHitBox(FOLLOWER_HB_WID, FOLLOWER_HB_HEI);
        initAttackBox();
        this.inAir = true;
        this.flipSign = 1;
        this.flipCoefficient = 0;

        this.behavior = (type == NpcType.ANITA) ? new AnitaBehavior() : new PassiveBehavior();
        this.jumpTracker = new JumpHistoryTracker();
        this.trajectoryCalculator = new TrajectoryCalculator(objectManager, gravity, jumpSpeed, walkSpeed);
        this.combatManager = new FollowerCombatManager(this);
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos, FOLLOWER_HB_WID + (20 * SCALE), FOLLOWER_HB_HEI);
    }

    public void update(int[][] levelData, Player player, List<Enemy> enemies) {
        this.currentLevelData = levelData;
        this.enemyContext = enemies;

        combatManager.update(levelData, player);
        if (combatManager.isPanicking()) {
            updatePhysics(levelData);
            return;
        }

        if (entityState == Anim.HIT) {
            pushBack(pushDirection, levelData, 0.15, 1.5);
            updatePhysics(levelData);
            return;
        }

        if (combatManager.isKnockedDown()) {
            updatePhysics(levelData);
            return;
        }

        behavior.update(this, player, levelData, enemies);
        jumpTracker.updateTracking(player);
        updateMovement(levelData, player, targetX);
        updatePhysics(levelData);
    }

    /**
     * Orchestrates movement, hazard detection, and jump decision-making.
     *
     * @param levelData The collision map.
     * @param player    The target player (used for distance/teleport checks).
     * @param targetX   The specific X coordinate we want to move towards (Enemy or Player).
     */
    private void updateMovement(int[][] levelData, Player player, double targetX) {
        if (handleAttackIntent()) return;

        // Reset Jump Override if we landed
        if (!inAir) isJumpOverride = false;

        // Execute Jump Override (Ballistic Arc)
        // If locked into a smart jump, ignore AI inputs and follow the parabola
        if (inAir && isJumpOverride) {
            if (canMoveHere(hitBox.x + overrideSpeedX, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += overrideSpeedX;
            }
            entityState = (airSpeed < 0) ? Anim.JUMP : Anim.FALL;
            return;
        }

        double dx = targetX - hitBox.x;

        // Determine Intent (Hysteresis Thresholding)
        int activeThreshold = isMoving ? (int)(20 * SCALE) : (int)(40 * SCALE);
        boolean wantsToMove = Math.abs(dx) > activeThreshold;

        // Stuck & Teleport Check
        updateStuckState(wantsToMove);
        if (shouldTeleport(player, distTo(player))) {
            teleportToPlayer(player);
            return;
        }

        // Initial Movement Setup
        double xSpeed = determineHorizontalSpeed(wantsToMove, dx);

        // Hazard Awareness & Adaptive Jump
        if (jumpDecisionTick > 0) jumpDecisionTick--;

        if (isMoving && !inAir) {
            if (isHazardAhead(xSpeed, levelData, player)) {
                // STOP! Dont run into the hazard
                xSpeed = 0;
                if (jumpDecisionTick <= 0) {
                    attemptSmartJump(levelData, player);
                    jumpDecisionTick = 15;
                }
                else {
                    // Waiting for cooldown -> stay put
                    isMoving = false;
                }
            }
        }
        // Standard Physics Execution
        executeStandardMovement(xSpeed, levelData);
        determineNextAnimation();
    }

    private boolean handleAttackIntent() {
        // Handle Attack
        if (attackRequested) {
            isAttacking = true;
            attackRequested = false;
            resetAnimState();
        }
        if (isAttacking) {
            isMoving = false;
            return true;
        }
        return false;
    }

    private void updateStuckState(boolean wantsToMove) {
        // If we want to move but position hasn't changed, increase stuck timer
        if (wantsToMove && Math.abs(hitBox.x - lastX) < 0.5) stuckTick++;
        else {
            stuckTick = 0;
            lastX = hitBox.x;
        }
    }

    private boolean shouldTeleport(Player player, double distToPlayer) {
        return (distToPlayer > teleportDist && !player.isInAir()) || distToPlayer > teleportDist * 3 || stuckTick >= STUCK_THRESHOLD;
    }

    private double determineHorizontalSpeed(boolean wantsToMove, double dx) {
        if (!wantsToMove) {
            isMoving = false;
            return 0;
        }
        isMoving = true;
        if (dx > 0) {
            setDirection(Direction.RIGHT);
            return walkSpeed;
        }
        else {
            setDirection(Direction.LEFT);
            return -walkSpeed;
        }
    }

    private boolean isHazardAhead(double xSpeed, int[][] levelData, Player player) {
        double lookAheadX = hitBox.x + (xSpeed * 20);
        Rectangle2D.Double futureBody = new Rectangle2D.Double(lookAheadX, hitBox.y, hitBox.width, hitBox.height);

        // A. Check for Traps in front
        if (objectManager.isDangerous(futureBody)) return true;

        // B. Check for Cliffs (Altitude Aware)
        double feetX = (xSpeed > 0) ? lookAheadX + hitBox.width : lookAheadX;
        double feetY = hitBox.y + hitBox.height + 5;

        if (!CollisionDetector.isSolid(feetX, feetY, levelData)) {
            // If Player is higher or level, treat any drop as a hazard to force a jump check
            boolean playerIsHigher = player.getHitBox().y < (hitBox.y - TILES_SIZE);
            if (playerIsHigher) return true;
            return !trajectoryCalculator.isLandingSafe(feetX, feetY, levelData);
        }
        return false;
    }

    private void attemptSmartJump(int[][] levelData, Player player) {
        // Strategy 1 - Math
        // If a standard jump can clear the gap safely based on gravity/speed
        double calculatedJumpSpeed = trajectoryCalculator.calculateAdaptiveJump(hitBox, flipSign, levelData);
        if (calculatedJumpSpeed != 0) {
            executeJump(calculatedJumpSpeed, jumpSpeed);
            return;
        }

        // Strategy 2 - Breadcrumb
        // If math failed, check if the player performed a jump nearby recently
        JumpSnapshot crumb = jumpTracker.getNearestJumpSnapshot(player, hitBox);
        if (crumb != null) {
            // Snap to player's launch point for perfect arc alignment
            hitBox.x = crumb.x();
            hitBox.y = crumb.y();
            this.flipSign = crumb.direction();

            double mimicSpeed = crumb.speedX() * crumb.direction();

            // Apply Ability Boosts
            // If player used Dash, give NPC super-speed to clear the gap
            if (crumb.usedDash()) mimicSpeed *= 1.7;
            else mimicSpeed *= 1.1;

            // Apply Vertical Boost if player did a High Jump
            double vertSpeed = crumb.isHighJump() ? jumpSpeed * 1.35 : jumpSpeed;

            executeJump(mimicSpeed, vertSpeed);
        } else {
            // All strategies failed. Stand still and wait for teleport/player
            isMoving = false;
        }
    }

    private void executeJump(double hSpeed, double vSpeed) {
        isJumpOverride = true;
        overrideSpeedX = hSpeed;
        inAir = true;
        airSpeed = vSpeed;
    }

    private void executeStandardMovement(double xSpeed, int[][] levelData) {
        if (isMoving && !isJumpOverride && xSpeed != 0) {
            if (canMoveHere(hitBox.x + xSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += xSpeed;
            }
            else if (!inAir && isStandardJumpSafe()) {
                inAir = true;
                airSpeed = jumpSpeed;
            }
        }
    }

    private void determineNextAnimation() {
        Anim nextAnim;
        if (inAir) {
            if (airSpeed < 0) nextAnim = Anim.JUMP;
            else if (airSpeed > 0.2) nextAnim = Anim.FALL;
            else nextAnim = isMoving ? Anim.RUN : Anim.IDLE;
        }
        else nextAnim = isMoving ? Anim.RUN : Anim.IDLE;

        if (entityState != Anim.HIT && entityState != Anim.DEATH && nextAnim != entityState) {
            entityState = nextAnim;
            resetAnimState();
        }
    }

    private void updatePhysics(int[][] levelData) {
        updateAnimation();
        if (!CollisionDetector.isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;

            // Handle Attack Frame Events
            if (isAttacking && animIndex == 2 && behavior != null) {
                updateAttackBox();
                behavior.onAttackFrame(this, enemyContext);
            }

            BufferedImage[][] anims = SpriteManager.getInstance().getFollowerAnimations(type);
            int maxFrames = (anims != null && entityState.ordinal() < anims.length) ? anims[entityState.ordinal()].length : 1;

            if (animIndex >= maxFrames) {
                if (combatManager.isKnockedDown()) {
                    // Stay on last frame of death
                    animIndex = maxFrames - 1;
                } else {
                    animIndex = 0;
                    // If Hit animation finishes -> go back to Idle
                    if (entityState == Anim.HIT || isAttacking) {
                        isAttacking = false;
                        entityState = Anim.IDLE;
                    }
                }
            }
        }
    }

    /**
     * Checks the area directly above the NPC to ensure a standard jump won't hit a ceiling hazard.
     */
    private boolean isStandardJumpSafe() {
        double jumpHeight = 3 * TILES_SIZE;
        Rectangle2D.Double ceilingCheck = new Rectangle2D.Double(hitBox.x, hitBox.y - jumpHeight, hitBox.width, jumpHeight);
        return !objectManager.isDangerous(ceilingCheck);
    }

    public void hit(double damage, Entity attacker) {
        combatManager.handleHit(damage, attacker, currentLevelData);
    }

    private void teleportToPlayer(Player player) {
        if (effectManager != null)
            effectManager.spawnDustParticles(hitBox.x + hitBox.width/2, hitBox.y + hitBox.height/2, 20, DustType.SW_TELEPORT, 0, null);

        hitBox.x = player.getHitBox().x;
        hitBox.y = player.getHitBox().y - player.getHitBox().getHeight() + this.hitBox.height;
        inAir = true;
        airSpeed = 0;
        stuckTick = 0;

        if (effectManager != null)
            effectManager.spawnDustParticles(hitBox.x + hitBox.width/2, hitBox.y + hitBox.height/2, 20, DustType.SW_TELEPORT, 0, null);
    }

    private void updateAttackBox() {
        if (flipSign == 1) attackBox.x = hitBox.x + hitBox.width;
        else attackBox.x = hitBox.x - attackBox.width;
        attackBox.y = hitBox.y;
    }

    private double distTo(Player player) {
        return Math.sqrt(Math.pow(player.getHitBox().x - hitBox.x, 2) + Math.pow(player.getHitBox().y - hitBox.y, 2));
    }

    // Wrapper
    public void executePushBack(Direction direction, int[][] levelData, double speed, double knockbackSpeed) {
        super.pushBack(direction, levelData, speed, knockbackSpeed);
    }

    // Getters & Setters
    public void changeHealth(double value) {
        currentHealth += value;
    }

    public void healFully() {
        currentHealth = maxHealth;
    }

    public void setEntityState(Anim anim) {
        this.entityState = anim;
    }

    public void resetAnimState() {
        animIndex = 0;
        animTick = 0;
    }

    public void cancelActions() {
        isAttacking = false;
        attackRequested = false;
        isMoving = false;
        isJumpOverride = false;
    }

    public void cancelAttack() {
        isAttacking = false;
        attackRequested = false;
    }

    public void setPushOffsetDirection(Direction d) {
        pushOffsetDirection = d;
    }

    public void setPushOffset(double offset) {
        pushOffset = offset;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public void setAirSpeed(double speed) {
        this.airSpeed = speed;
    }

    public double getWalkSpeed() {
        return walkSpeed;
    }

    public void setMoveTarget(double x) {
        this.targetX = x;
    }

    public void setEnemyActionNoReset(Anim anim) {
        this.entityState = anim;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public Direction getPushDirection() {
        return super.pushDirection;
    }

    public void requestAttack() {
        this.attackRequested = true;
    }

    public boolean isBusy() {
        return isJumpOverride || isAttacking;
    }

    public void setDirection(Direction direction) {
        if (direction == Direction.RIGHT) {
            this.flipCoefficient = 0;
            this.flipSign = 1;
        }
        else if (direction == Direction.LEFT) {
            this.flipCoefficient = width;
            this.flipSign = -1;
        }
    }

    public boolean isKnockedDown() {
        return combatManager.isKnockedDown();
    }

    public void revive() {
        combatManager.revive();
    }

    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        BufferedImage[][] anims = SpriteManager.getInstance().getFollowerAnimations(type);
        if (anims == null) return;

        double drawX = hitBox.x;
        double drawY = hitBox.y;

        if (entityState == Anim.DEATH) {
            // Undoing the physical hitbox shift for the visual
            if (flipSign == 1) drawX += FollowerCombatManager.DEATH_HITBOX_EXPANSION;
            drawY -= (FOLLOWER_HB_HEI / 2.0);
            drawX += (-6 * SCALE) * flipSign;
        }

        int x = (int)drawX - FOLLOWER_X_OFFSET - xLvlOffset + flipCoefficient;
        int y = (int)drawY - FOLLOWER_Y_OFFSET - yLvlOffset;

        int stateIdx = entityState.ordinal();
        if (stateIdx < anims.length && anims[stateIdx] != null) {
            int idx = (animIndex < anims[stateIdx].length) ? animIndex : 0;
            g.drawImage(anims[stateIdx][idx], x, y, width * flipSign, height, null);
        }

        hitBoxRenderer(g, xLvlOffset, yLvlOffset, Color.CYAN);
        attackBoxRenderer(g, xLvlOffset, yLvlOffset);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }

    @Override
    public void onEnter(Player player) {

    }

    @Override
    public void onIntersect(Player player) {

    }

    @Override
    public void onExit(Player player) {

    }

    @Override
    public String getInteractionPrompt() {
        if (combatManager.isKnockedDown()) return "Revive";
        return null;
    }

    // Fallback behavior
    private static class PassiveBehavior implements FollowerBehavior {
        @Override
        public void update(Follower host, Player player, int[][] levelData, List<Enemy> enemies) {
            host.setMoveTarget(player.getHitBox().x);
        }
        @Override
        public void onAttackFrame(Follower host, List<Enemy> enemies) {}
    }
}