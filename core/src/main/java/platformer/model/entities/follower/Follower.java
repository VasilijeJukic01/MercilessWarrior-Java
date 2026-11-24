package platformer.model.entities.follower;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.renderer.AfterimageTrail;
import platformer.model.entities.follower.behavior.AnitaBehavior;
import platformer.model.entities.follower.behavior.DejanovicBehavior;
import platformer.model.entities.follower.behavior.FollowerBehavior;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.gameObjects.npc.NpcType;
import platformer.model.levels.Trigger;
import platformer.physics.CollisionDetector;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.canMoveHere;

/**
 * Represents an intelligent NPC follower that tracks and fights alongside the player.
 * <p>
 * Powered by a 2D Platform-Graph A* Pathfinder ({@link TrajectoryCalculator}), predictive ballistic jumps, mid-air dash/wall-boost parkour, and breadcrumb mimicry.
 */
public class Follower extends Entity implements Interactable {

    public record FollowerProfile(int drawWidth, int drawHeight, int offsetX, int offsetY) { }

    private final NpcType type;
    private final EffectManager effectManager;
    private final ObjectManager objectManager;

    private FollowerBehavior behavior;
    private final JumpHistoryTracker jumpTracker;
    private final TrajectoryCalculator trajectoryCalculator;
    private final FollowerCombatManager combatManager;
    private final AfterimageTrail afterimageTrail = new AfterimageTrail(4, 3);

    private int[][] currentLevelData;
    private List<Enemy> enemyContext;

    // Settings
    private final int animSpeed = 25;
    private final double walkSpeed = 0.62 * SCALE;
    private final int teleportDist = TILES_SIZE * 20;
    private int animTick, animIndex;
    private boolean active = false;

    // Physics
    private final double gravity = 0.04 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;
    private final double jumpSpeed = -2.25 * SCALE;

    // Audio
    private final FollowerAudioHandler audioHandler;

    // Navigation & Ballistic Jump State
    private TrajectoryCalculator.NavPlan currentPlan = null;
    private int planTimer = 0;
    private int navCommitTicks = 0;
    private int navCommitDir = 0;
    private boolean wasInAir = true;

    private boolean isJumpOverride = false;

    /**
     * The calculated horizontal velocity required to complete the current smart jump.
     */
    private double overrideSpeedX = 0;
    private int jumpDecisionTick = 0;
    private boolean canWallBoostInAir = false;

    // Dash State (Uses existing RUN/JUMP animations + AfterimageTrail)
    private boolean isDashing = false;
    private int dashTicks = 0;
    private double dashSpeedX = 0;
    private boolean pendingAirDash = false;

    private boolean isMoving = false;
    private int guardCooldown = 0;

    // Stuck Detection
    private double lastX = 0;
    private int stuckTick = 0;
    private final int STUCK_THRESHOLD = 200;

    // AI State
    private boolean isAttacking = false;
    private double targetX;
    private boolean attackRequested;

    // Profiles
    private static final Map<NpcType, FollowerProfile> PROFILES = Map.of(
            NpcType.ANITA, new FollowerProfile(FLW_WIDTH, FLW_HEIGHT, FOLLOWER_X_OFFSET, FOLLOWER_Y_OFFSET),
            NpcType.SIR_DEJANOVIC, new FollowerProfile(KNIGHT_WIDTH, KNIGHT_HEIGHT, KNIGHT_X_OFFSET, KNIGHT_Y_OFFSET - (int) (3 * SCALE))
    );

    public Follower(int x, int y, NpcType type, EffectManager effectManager, ObjectManager objectManager) {
        super(x, y, PROFILES.getOrDefault(type, PROFILES.get(NpcType.ANITA)).drawWidth(), PROFILES.getOrDefault(type, PROFILES.get(NpcType.ANITA)).drawHeight(), 100);
        this.type = type;
        this.effectManager = effectManager;
        this.objectManager = objectManager;

        initHitBox(FOLLOWER_HB_WID, FOLLOWER_HB_HEI);
        initAttackBox();
        this.inAir = true;
        this.flipSign = 1;
        this.flipCoefficient = 0;
        configBehavior();

        this.jumpTracker = new JumpHistoryTracker();
        this.trajectoryCalculator = new TrajectoryCalculator(objectManager, gravity, jumpSpeed, walkSpeed);
        this.audioHandler = new FollowerAudioHandler(type);
        this.combatManager = new FollowerCombatManager(this);
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos, FOLLOWER_HB_WID + (20 * SCALE), FOLLOWER_HB_HEI);
    }

    private void configBehavior() {
        if (type == NpcType.ANITA) this.behavior = new AnitaBehavior();
        else if (type == NpcType.SIR_DEJANOVIC) this.behavior = new DejanovicBehavior();
        else this.behavior = new PassiveBehavior();
    }

    public void setLevelTriggers(List<Trigger> triggers) {
    }

    private void ensureClearOfFloor(int[][] levelData) {
        if (levelData == null) return;
        if (!canMoveHere(hitBox.x, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            int maxLift = TILES_SIZE * 2;
            for (int lift = 1; lift <= maxLift; lift++) {
                if (canMoveHere(hitBox.x, hitBox.y - lift, hitBox.width, hitBox.height, levelData)) {
                    hitBox.y -= lift;
                    break;
                }
            }
        }
    }

    public void update(int[][] levelData, Player player, List<Enemy> enemies) {
        this.currentLevelData = levelData;
        this.enemyContext = enemies;

        ensureClearOfFloor(levelData);

        if (planTimer > 0) planTimer--;
        if (navCommitTicks > 0) navCommitTicks--;
        if (guardCooldown > 0) guardCooldown--;

        if (entityState == Anim.DEATH) {
            afterimageTrail.clear();
            updatePhysics(levelData);
            return;
        }

        combatManager.update(levelData, player);
        if (combatManager.isPanicking()) {
            updatePhysics(levelData);
            return;
        }

        if (entityState == Anim.HIT) {
            isDashing = false;
            pushBack(pushDirection, levelData, 0.15, 1.5);
            updatePhysics(levelData);
            return;
        }

        if (combatManager.isKnockedDown()) {
            afterimageTrail.clear();
            updatePhysics(levelData);
            return;
        }

        if (!inAir) {
            if (wasInAir) {
                planTimer = 0;
                currentPlan = null;
            }
            isJumpOverride = false;
            canWallBoostInAir = true;
            pendingAirDash = false;
        }
        wasInAir = inAir;

        behavior.update(this, player, levelData, enemies);
        jumpTracker.updateTracking(player);
        updateMovement(levelData, player, targetX);
        updateAttackBox();
        updatePhysics(levelData);
        audioHandler.update();
    }

    /**
     * Orchestrates the follower's physical movement, hazard detection, and ledge navigation.
     * <p>
     * Handles the complex transition between standard walking, intelligent vertical dropping, and taking action when hazards are detected.
     * It uses hysteresis thresholding to prevent micro-stutters and includes specific logic to smoothly push the follower off ledges
     * when the target is directly below them.
     * </p>
     *
     * @param levelData The collision map of the current level.
     * @param player    The target player (used for distance/teleport checks and relative positioning).
     * @param targetX   The specific X coordinate the AI "Brain" wants to move towards (could be an Enemy or the Player).
     */
    private void updateMovement(int[][] levelData, Player player, double targetX) {
        if (handleAttackOrBlockState()) return;

        // Active Air-Dash Execution
        if (isDashing) {
            dashTicks--;
            if (canMoveHere(hitBox.x + dashSpeedX, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += dashSpeedX;
            }
            else {
                isDashing = false;
            }
            if (dashTicks <= 0) {
                isDashing = false;
            }
            entityState = Anim.RUN;
            return;
        }

        // Trigger Queued Mid-Air Dash near Jump Apex
        if (inAir && pendingAirDash && airSpeed >= -0.65 * SCALE) {
            pendingAirDash = false;
            startAirDash();
            return;
        }

        // Airborne Ballistic Jump & Wall-Boost Execution
        if (inAir && isJumpOverride) {
            if (canMoveHere(hitBox.x + overrideSpeedX, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += overrideSpeedX;
            }
            else {
                double stepSign = Math.signum(overrideSpeedX);
                if (stepSign != 0 && canMoveHere(hitBox.x + stepSign * walkSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                    hitBox.x += stepSign * walkSpeed;
                }
                else if (canWallBoostInAir && airSpeed >= -0.85 * SCALE) {
                    triggerWallBoost();
                }
            }
            entityState = (airSpeed < 0) ? Anim.JUMP : Anim.FALL;
            return;
        }

        // Committed Ledge Drop-Off (No left-right oscillation!)
        if (navCommitTicks > 0 && !inAir) {
            setDirection(navCommitDir == 1 ? Direction.RIGHT : Direction.LEFT);
            double stepX = navCommitDir * walkSpeed;
            if (canMoveHere(hitBox.x + stepX, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += stepX;
                isMoving = true;
            }
            else {
                navCommitTicks = 0;
                isMoving = false;
            }
            determineNextAnimation();
            return;
        }

        // Query 2D Platform-Graph A* Pathfinder when navigating to the player
        boolean followingPlayer = Math.abs(targetX - player.getHitBox().x) < 4.0;
        double effectiveTargetX = targetX;

        if (followingPlayer && !inAir) {
            if (planTimer <= 0 || currentPlan == null) {
                currentPlan = trajectoryCalculator.findNextNavStep(hitBox, player.getHitBox().x, player.getHitBox().y, levelData);
                planTimer = 14;
            }

            if (currentPlan != null && currentPlan.action() != TrajectoryCalculator.ActionType.WALK) {
                effectiveTargetX = currentPlan.takeoffX();
                double triggerRadius = (currentPlan.action() == TrajectoryCalculator.ActionType.BALLISTIC_JUMP) ? (TILES_SIZE * 0.45) : (walkSpeed * 2.4);
                if (Math.abs(hitBox.x - currentPlan.takeoffX()) <= triggerRadius) {
                    executeNavPlan(currentPlan, levelData);
                    currentPlan = null;
                    planTimer = 10;
                    return;
                }
            }
        }
        else if (!followingPlayer) {
            currentPlan = null;
        }

        double dx = effectiveTargetX - hitBox.x;
        double dy = player.getHitBox().y - hitBox.y;

        boolean hasSubGoal = (currentPlan != null && currentPlan.action() != TrajectoryCalculator.ActionType.WALK);
        int activeThreshold = hasSubGoal ? (int) (walkSpeed * 1.6) : (isMoving ? (int) (20 * SCALE) : (int) (40 * SCALE));
        boolean wantsToMove = Math.abs(dx) > activeThreshold;

        // Fallback ledge drop if directly above player
        if (!hasSubGoal && Math.abs(dx) <= 45 * SCALE && dy > TILES_SIZE * 1.5 && !inAir) {
            boolean canDropLeft = !CollisionDetector.isSolid(hitBox.x - TILES_SIZE, hitBox.y + hitBox.height + 5, levelData) && trajectoryCalculator.isLandingSafe(hitBox.x - TILES_SIZE, hitBox.y + hitBox.height + 5, levelData);
            boolean canDropRight = !CollisionDetector.isSolid(hitBox.x + hitBox.width + TILES_SIZE, hitBox.y + hitBox.height + 5, levelData) && trajectoryCalculator.isLandingSafe(hitBox.x + hitBox.width + TILES_SIZE, hitBox.y + hitBox.height + 5, levelData);

            if (canDropLeft || canDropRight) {
                navCommitDir = (canDropRight && !canDropLeft) ? 1 : (canDropLeft && !canDropRight) ? -1 : flipSign;
                navCommitTicks = 28;
                setDirection(navCommitDir == 1 ? Direction.RIGHT : Direction.LEFT);
                isMoving = true;
                executeStandardMovement(navCommitDir * walkSpeed, levelData, player);
                determineNextAnimation();
                return;
            }
            else wantsToMove = false;
        }

        updateStuckState(wantsToMove || Math.abs(dy) > TILES_SIZE * 2.0);
        if (shouldTeleport(player, distTo(player))) {
            teleportToPlayer(player, levelData);
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
                    jumpDecisionTick = 12;
                }
                else {
                    // Waiting for cooldown -> stay put
                    isMoving = false;
                }
            }
        }

        executeStandardMovement(xSpeed, levelData, player);
        determineNextAnimation();
    }

    private void executeNavPlan(TrajectoryCalculator.NavPlan plan, int[][] levelData) {
        setDirection(plan.moveDir() == 1 ? Direction.RIGHT : Direction.LEFT);

        switch (plan.action()) {
            case STEP_HOP -> {
                inAir = true;
                airSpeed = jumpSpeed;
                double stepX = plan.moveDir() * walkSpeed;
                if (canMoveHere(hitBox.x + stepX, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                    hitBox.x += stepX;
                }
            }
            case LEDGE_DROP -> {
                navCommitDir = plan.moveDir();
                navCommitTicks = 28;
            }
            case BALLISTIC_JUMP -> {
                this.pendingAirDash = plan.useAirDash();
                this.canWallBoostInAir = true;
                double vSpeed = plan.useWallBoost() ? (jumpSpeed * 1.25) : jumpSpeed;
                double vx = (Math.abs(plan.jumpVx()) < walkSpeed) ? (plan.moveDir() * walkSpeed * 1.6) : plan.jumpVx();
                executeJump(vx, vSpeed);
            }
            default -> {}
        }
    }

    private void startAirDash() {
        this.isDashing = true;
        this.dashTicks = 14;
        this.dashSpeedX = flipSign * walkSpeed * 3.6;
        this.airSpeed = 0;
        Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
        if (effectManager != null) {
            effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.getCenterY(), 10, DustType.DASH, flipSign, this);
        }
    }

    private void triggerWallBoost() {
        canWallBoostInAir = false;
        airSpeed = jumpSpeed * 1.18;
        if (effectManager != null) {
            double wallX = (flipSign == 1) ? hitBox.getMaxX() : hitBox.x;
            effectManager.spawnDustParticles(wallX, hitBox.getCenterY(), 10, DustType.WALL_JUMP, flipSign, this);
        }
    }

    private boolean handleAttackOrBlockState() {
        if (entityState == Anim.BLOCK) {
            isMoving = false;
            return true;
        }
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

    /**
     * Determines the horizontal speed and direction of the follower.
     * <p>
     * If the follower is extremely close to their target X-coordinate, this method locks their
     * current direction to prevent ledge dancing.
     *
     * @param wantsToMove Boolean indicating if the follower is outside the minimum resting distance.
     * @param dx          The delta X between the follower and their target.
     * @return            The horizontal velocity to apply this frame.
     */
    private double determineHorizontalSpeed(boolean wantsToMove, double dx) {
        if (!wantsToMove) {
            isMoving = false;
            return 0;
        }
        isMoving = true;
        if (Math.abs(dx) < walkSpeed * 3) {
            return (this.flipSign == 1) ? walkSpeed : -walkSpeed;
        }
        if (dx > 0) {
            setDirection(Direction.RIGHT);
            return walkSpeed;
        }
        else {
            setDirection(Direction.LEFT);
            return -walkSpeed;
        }
    }

    /**
     * Evaluates the terrain immediately in front of the follower to prevent walking into traps or bottomless pits.
     * <p>
     * This checks two conditions:
     * 1. Traps: Uses a look-ahead bounding box to check for spikes or lava.
     * 2. Cliffs: Checks if the floor drops away. If it does, it consults the {@link TrajectoryCalculator} to ensure there is safe ground before the fall.
     *
     * @param xSpeed    The current horizontal speed (determines look-ahead distance).
     * @param levelData The level collision map.
     * @param player    The player entity (used to determine if falling is required to reach the player).
     * @return          {@code true} if a hazard or unsafe drop is ahead, triggering a jump calculation; {@code false} otherwise.
     */
    private boolean isHazardAhead(double xSpeed, int[][] levelData, Player player) {
        double lookAheadX = hitBox.x + (xSpeed * 14);
        Rectangle2D.Double futureBody = new Rectangle2D.Double(lookAheadX, hitBox.y, hitBox.width, hitBox.height);

        // Check for Traps in front
        if (objectManager.isDangerous(futureBody)) return true;

        double nearFeetX = (xSpeed > 0) ? (hitBox.getMaxX() + 3 * SCALE) : (hitBox.x - 3 * SCALE);
        double farFeetX = (xSpeed > 0) ? (lookAheadX + hitBox.width) : lookAheadX;
        double feetY = hitBox.y + hitBox.height + 5;

        boolean nearDrop = !CollisionDetector.isSolid(nearFeetX, feetY, levelData);
        boolean farDrop = !CollisionDetector.isSolid(farFeetX, feetY, levelData);

        if (nearDrop || farDrop) {
            double dropCheckX = nearDrop ? nearFeetX : farFeetX;
            boolean playerIsHigher = player.getHitBox().y < (hitBox.y - TILES_SIZE * 0.75);
            if (playerIsHigher) return true;
            return !trajectoryCalculator.isLandingSafe(dropCheckX, feetY, levelData);
        }
        return false;
    }

    /**
     * Triggered when the follower is blocked by a hazard or a gap. Attempts to bypass the obstacle.
     * <p>
     * Strategy 1 (Kinematics): Asks the {@link TrajectoryCalculator} to find a safe landing spot and calculate the exact X-velocity needed to land there.
     * <br>
     * Strategy 2 (Breadcrumbs): If math fails, it queries the {@link JumpHistoryTracker} to find a recent jump of the player, and it will mimic it.
     *
     * @param levelData The level collision map.
     * @param player    The player entity (used to track breadcrumbs).
     */
    private void attemptSmartJump(int[][] levelData, Player player) {
        if (currentPlan != null && currentPlan.action() == TrajectoryCalculator.ActionType.BALLISTIC_JUMP) {
            executeNavPlan(currentPlan, levelData);
            currentPlan = null;
            return;
        }

        // Strategy 1 - Math
        currentPlan = trajectoryCalculator.findNextNavStep(hitBox, player.getHitBox().x, player.getHitBox().y, levelData);
        if (currentPlan != null && currentPlan.action() == TrajectoryCalculator.ActionType.BALLISTIC_JUMP && Math.abs(hitBox.x - currentPlan.takeoffX()) <= TILES_SIZE * 1.2) {
            executeNavPlan(currentPlan, levelData);
            currentPlan = null;
            return;
        }

        double calculatedJumpSpeed = trajectoryCalculator.calculateAdaptiveJump(hitBox, flipSign, levelData, player.getHitBox().y);
        if (calculatedJumpSpeed != 0) {
            this.pendingAirDash = Math.abs(calculatedJumpSpeed) > walkSpeed * 2.7;
            executeJump(calculatedJumpSpeed, jumpSpeed);
            return;
        }

        // Strategy 2 - Breadcrumb
        JumpSnapshot crumb = jumpTracker.getNearestJumpSnapshot(player, hitBox);
        if (crumb != null) {
            // Snap to player's launch point for perfect arc alignment
            hitBox.x = crumb.x();
            hitBox.y = crumb.y();
            this.flipSign = crumb.direction();

            double mimicSpeed = crumb.speedX() * crumb.direction();
            if (crumb.usedDash()) {
                mimicSpeed *= 1.4;
                this.pendingAirDash = true;
            }
            else mimicSpeed *= 1.1;

            double vertSpeed = crumb.isHighJump() ? jumpSpeed * 1.35 : jumpSpeed;
            executeJump(mimicSpeed, vertSpeed);
        }
        else {
            isMoving = false;
        }
    }

    private void executeJump(double hSpeed, double vSpeed) {
        isJumpOverride = true;
        overrideSpeedX = hSpeed;
        inAir = true;
        airSpeed = vSpeed;
    }

    public void launch(double launchSpeed) {
        if (effectManager != null) {
            effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.getMaxY(), 16, DustType.JUMP_PAD, 0, this);
        }
        this.inAir = true;
        this.isJumpOverride = false;
        this.isDashing = false;
        this.currentPlan = null;
        this.airSpeed = launchSpeed;
    }

    private void executeStandardMovement(double xSpeed, int[][] levelData, Player player) {
        if (isMoving && !isJumpOverride && xSpeed != 0) {
            double allowedDx = objectManager.checkSolidObjectCollision(hitBox, xSpeed);
            boolean blockedBySolidObject = Math.abs(allowedDx) < Math.abs(xSpeed) * 0.5;

            if (!blockedBySolidObject && canMoveHere(hitBox.x + allowedDx, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += allowedDx;
            }
            else if (!inAir && isStandardJumpSafe()) {
                inAir = true;
                airSpeed = jumpSpeed;
            }
            else if (inAir && canWallBoostInAir && player.getHitBox().y < hitBox.y - TILES_SIZE && airSpeed >= -0.65 * SCALE) {
                triggerWallBoost();
            }
        }
    }

    private void determineNextAnimation() {
        Anim nextAnim;
        if (isDashing) {
            nextAnim = Anim.RUN;
        }
        else if (inAir) {
            if (airSpeed < 0) nextAnim = Anim.JUMP;
            else if (airSpeed > 0.2) nextAnim = Anim.FALL;
            else nextAnim = isMoving ? Anim.RUN : Anim.IDLE;
        }
        else nextAnim = isMoving ? Anim.RUN : Anim.IDLE;

        if (entityState != Anim.HIT && entityState != Anim.DEATH && entityState != Anim.BLOCK && nextAnim != entityState) {
            entityState = nextAnim;
            resetAnimState();
        }
    }

    private void updatePhysics(int[][] levelData) {
        updateAnimation();
        if (!CollisionDetector.isEntityOnFloor(hitBox, levelData) && !objectManager.isFollowerTouchingObject(this)) {
            inAir = true;
        }
        if (inAir && !isDashing) {
            updateInAir(levelData, gravity, collisionFallSpeed);
        }
    }

    private void updateAnimation() {
        animTick++;
        int currentAnimSpeed = switch (entityState) {
            case SPELL_1 -> 11;
            case BLOCK -> 12;
            case ATTACK_1, ATTACK_2 -> 15;
            default -> isDashing ? 12 : animSpeed;
        };

        if (animTick >= currentAnimSpeed) {
            animTick = 0;
            animIndex++;

            // Handle Attack Frame Events
            if (isAttacking && behavior != null) {
                behavior.onAttackFrame(this, animIndex, enemyContext);
            }

            BufferedImage[][] anims = SpriteManager.getInstance().getFollowerAnimations(type);
            int maxFrames = (anims != null && entityState.ordinal() < anims.length && anims[entityState.ordinal()] != null) ? anims[entityState.ordinal()].length : 1;

            if (animIndex >= maxFrames) {
                if (entityState == Anim.DEATH) {
                    animIndex = maxFrames - 1;
                }
                else {
                    animIndex = 0;
                    if (entityState == Anim.HIT || entityState == Anim.BLOCK || isAttacking) {
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

    private void teleportToPlayer(Player player, int[][] levelData) {
        if (effectManager != null) {
            effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.getCenterY(), 20, DustType.SW_TELEPORT, 0, null);
        }

        hitBox.x = player.getHitBox().x;
        hitBox.y = player.getHitBox().y + player.getHitBox().height - this.hitBox.height;
        ensureClearOfFloor(levelData);

        inAir = true;
        airSpeed = 0;
        stuckTick = 0;
        isJumpOverride = false;
        isDashing = false;
        pendingAirDash = false;
        currentPlan = null;
        navCommitTicks = 0;
        afterimageTrail.clear();

        if (effectManager != null) {
            effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.getCenterY(), 20, DustType.SW_TELEPORT, 0, null);
        }
    }

    private void updateAttackBox() {
        attackBox.width = (entityState == Anim.SPELL_1) ? FOLLOWER_HB_WID + (38 * SCALE) : FOLLOWER_HB_WID + (22 * SCALE);
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
        isDashing = false;
        pendingAirDash = false;
        currentPlan = null;
        navCommitTicks = 0;
    }

    public void cancelAttack() {
        isAttacking = false;
        attackRequested = false;
    }

    public void activateGuard(int ticks) {
        if (guardCooldown > 0) return;
        if (!inAir && !isAttacking && !isDashing && entityState != Anim.HIT && entityState != Anim.DEATH && entityState != Anim.BLOCK) {
            this.entityState = Anim.BLOCK;
            this.guardCooldown = 110;
            resetAnimState();
        }
    }

    public boolean isBlocking() {
        return entityState == Anim.BLOCK;
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
        return isJumpOverride || isDashing || isAttacking || entityState == Anim.BLOCK;
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

    public void performAttackLunge(double stepDist, boolean spawnWindSlash) {
        double dx = stepDist * flipSign;
        if (currentLevelData != null && canMoveHere(hitBox.x + dx, hitBox.y, hitBox.width, hitBox.height, currentLevelData)) {
            hitBox.x += dx;
        }
        if (spawnWindSlash && effectManager != null) {
            double slashX = hitBox.getCenterX() + flipSign * (22 * SCALE);
            double slashY = hitBox.getCenterY();
            effectManager.spawnDustParticles(slashX, slashY, 3, DustType.ETHEREAL_DASH, flipSign, this);
            effectManager.spawnDustParticles(slashX, slashY, 4, DustType.SW_AURA_CRACKLE, flipSign, null);
        }
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

        FollowerProfile profile = PROFILES.getOrDefault(type, PROFILES.get(NpcType.ANITA));
        int drawW = profile.drawWidth(), drawH = profile.drawHeight();
        int fC = (flipSign == -1) ? drawW : 0;

        int worldDrawX = (int) drawX - profile.offsetX() + fC;
        int worldDrawY = (int) drawY - profile.offsetY() + 1;

        int stateIdx = entityState.ordinal();
        int frameIdx = (stateIdx < anims.length && anims[stateIdx] != null && animIndex < anims[stateIdx].length) ? animIndex : 0;

        if (isDashing) {
            afterimageTrail.record(worldDrawX, worldDrawY, flipSign, stateIdx, frameIdx);
            afterimageTrail.render(g, anims, drawW, drawH, xLvlOffset, yLvlOffset, 0.38f);
        }
        else afterimageTrail.clear();

        int x = worldDrawX - xLvlOffset;
        int y = worldDrawY - yLvlOffset;

        if (stateIdx < anims.length && anims[stateIdx] != null) {
            g.drawImage(anims[stateIdx][frameIdx], x, y, drawW * flipSign, drawH, null);
        }

        hitBoxRenderer(g, xLvlOffset, yLvlOffset, Color.CYAN);
        attackBoxRenderer(g, xLvlOffset, yLvlOffset);
    }

    @Override
    public void renderInteractionUI(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!combatManager.isKnockedDown()) return;
        int centerX = (int) (hitBox.getCenterX() - xLevelOffset);
        int promptY = (int) (hitBox.y - yLevelOffset - 8 * SCALE);
        UIButtonRenderer.drawInteractionPrompt(g, "REVIVE", centerX, promptY);
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
        this.active = true;
    }

    @Override
    public void onIntersect(Player player) {

    }

    @Override
    public void onExit(Player player) {
        this.active = false;
    }

    @Override
    public String getInteractionPrompt() {
        if (combatManager.isKnockedDown()) return "Revive";
        return null;
    }

    public FollowerAudioHandler getAudioHandler() {
        return audioHandler;
    }

    public double getAirSpeed() {
        return super.airSpeed;
    }

    private static class PassiveBehavior implements FollowerBehavior {
        @Override
        public void update(Follower host, Player player, int[][] levelData, List<Enemy> enemies) {
            host.setMoveTarget(player.getHitBox().x);
        }
        @Override
        public void onAttackFrame(Follower host, int animIndex, List<Enemy> enemies) {}
    }
}