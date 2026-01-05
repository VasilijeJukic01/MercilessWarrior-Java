package platformer.model.entities.follower;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.gameObjects.npc.NpcType;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.canMoveHere;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

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
 */
public class Follower extends Entity {

    private final NpcType type;
    private final EffectManager effectManager;
    private final ObjectManager objectManager;

    // Settings
    private final int animSpeed = 25;
    private final double walkSpeed = 0.7 * SCALE;
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

    private final int MAX_JUMP_DIST_TILES = 7;
    private final int MIN_JUMP_DIST_TILES = 2;
    private int jumpDecisionTick = 0;

    // Breadcrumb System
    private final List<JumpSnapshot> jumpHistory = new ArrayList<>();
    private JumpSnapshot tempJump = null;

    private boolean isMoving = false;

    // Stuck Detection
    private double lastX = 0;
    private int stuckTick = 0;
    private final int STUCK_THRESHOLD = 200;

    public Follower(int x, int y, NpcType type, EffectManager effectManager, ObjectManager objectManager) {
        super(x, y, FLW_WIDTH, FLW_HEIGHT, 100);
        this.type = type;
        this.effectManager = effectManager;
        this.objectManager = objectManager;

        initHitBox(FOLLOWER_HB_WID, FOLLOWER_HB_HEI);
        this.inAir = true;
        this.flipSign = 1;
        this.flipCoefficient = 0;
    }

    public void update(int[][] levelData, Player player) {
        trackPlayerJumps(player);
        updateMovement(levelData, player);
        updateAnimation();

        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
    }

    /**
     * Orchestrates movement, hazard detection, and jump decision-making.
     *
     * @param levelData The collision map.
     * @param player    The target player.
     */
    private void updateMovement(int[][] levelData, Player player) {
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

        double dx = player.getHitBox().x - hitBox.x;
        double dy = player.getHitBox().y - hitBox.y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        // Determine Intent (Hysteresis Thresholding)
        int stopThreshold = (int)(40 * SCALE);
        int startThreshold = (int)(60 * SCALE);
        int activeThreshold = isMoving ? stopThreshold : startThreshold;
        boolean wantsToMove = Math.abs(dx) > activeThreshold;

        // Stuck & Teleport Check
        // If we want to move but position hasn't changed, increase stuck timer
        if (wantsToMove && Math.abs(hitBox.x - lastX) < 0.5) stuckTick++;
        else {
            stuckTick = 0;
            lastX = hitBox.x;
        }

        if ((distance > teleportDist && !player.isInAir()) || distance > teleportDist * 3 || stuckTick >= STUCK_THRESHOLD) {
            teleportToPlayer(player);
            return;
        }

        // Initial Movement Setup
        double xSpeed = 0;
        if (wantsToMove) {
            isMoving = true;
            if (dx > 0) {
                xSpeed = walkSpeed;
                this.flipSign = 1;
                this.flipCoefficient = 0;
            }
            else {
                xSpeed = -walkSpeed;
                this.flipSign = -1;
                this.flipCoefficient = width;
            }
        }
        else isMoving = false;

        // Hazard Awareness & Adaptive Jump
        if (jumpDecisionTick > 0) jumpDecisionTick--;

        if (isMoving && !inAir) {
            double lookAheadX = hitBox.x + (xSpeed * 20);
            Rectangle2D.Double futureBody = new Rectangle2D.Double(lookAheadX, hitBox.y, hitBox.width, hitBox.height);

            boolean hazardDetected = false;

            // A. Check for Traps in front
            if (objectManager.isDangerous(futureBody)) hazardDetected = true;
            // B. Check for Cliffs (Altitude Aware)
            else {
                double feetX = (xSpeed > 0) ? lookAheadX + hitBox.width : lookAheadX;
                double feetY = hitBox.y + hitBox.height + 5;

                if (!CollisionDetector.isSolid(feetX, feetY, levelData)) {
                    // If Player is higher or level, treat any drop as a hazard to force a jump check
                    boolean playerIsHigher = player.getHitBox().y < (hitBox.y - TILES_SIZE);

                    if (playerIsHigher) hazardDetected = true;
                    else if (!isLandingSafe(feetX, feetY, levelData)) hazardDetected = true;
                }
            }

            if (hazardDetected) {
                // STOP! Dont run into the hazard.
                xSpeed = 0;

                // Throttled Brain: Only calculate heavy physics every ~0.075s
                if (jumpDecisionTick <= 0) {

                    // Strategy 1 - Math
                    // If a standard jump can clear the gap safely based on gravity/speed
                    double calculatedJumpSpeed = calculateAdaptiveJump(levelData);
                    if (calculatedJumpSpeed != 0) {
                        isJumpOverride = true;
                        overrideSpeedX = calculatedJumpSpeed;
                        jump();
                    }
                    // Strategy 2: Breadcrumb
                    // If math failed, check if the player performed a jump nearby recently
                    else {
                        JumpSnapshot crumb = getNearestJumpSnapshot(player);
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

                            this.overrideSpeedX = mimicSpeed;
                            isJumpOverride = true;
                            jump();

                            // Apply Vertical Boost if player did a High Jump
                            if (crumb.isHighJump()) {
                                this.airSpeed = jumpSpeed * 1.35;
                            }
                        }
                        else {
                            // All strategies failed. Stand still and wait for teleport/player
                            isMoving = false;
                        }
                    }
                    jumpDecisionTick = 15;
                }
                else {
                    // Waiting for cooldown, stay put
                    isMoving = false;
                }
            }
        }

        // Standard Physics Execution
        if (isMoving && !isJumpOverride && xSpeed != 0) {
            if (canMoveHere(hitBox.x + xSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += xSpeed;
            }
            else if (!inAir && isJumpSafe()) jump();
        }

        Anim nextAnim;
        if (inAir) {
            if (airSpeed < 0) nextAnim = Anim.JUMP;
            else if (airSpeed > 0.2) nextAnim = Anim.FALL;
            else nextAnim = isMoving ? Anim.RUN : Anim.IDLE;
        }
        else nextAnim = isMoving ? Anim.RUN : Anim.IDLE;

        if (nextAnim != entityState) {
            entityState = nextAnim;
            animIndex = 0;
            animTick = 0;
        }
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

    /**
     * Records the player's jumping actions to build a history of successful movements.
     * Records take-off location, speed, dash usage, and high-jump status.
     */
    private void trackPlayerJumps(Player player) {
        boolean playerInAir = player.isInAir();

        // A. Just Jumped
        if (playerInAir && tempJump == null && player.getAirSpeed() < 0) {
            double pSpeed = player.getHorizontalSpeed();
            if (Math.abs(pSpeed) > 0.1) {
                tempJump = new JumpSnapshot((int)player.getHitBox().x, (int)player.getHitBox().y, Math.abs(pSpeed), player.getFlipSign(), false, false);
            }
        }

        // B. In Air (update the current recording based on player input)
        if (playerInAir && tempJump != null) {
            if (player.checkAction(PlayerAction.DASH)) {
                tempJump = new JumpSnapshot(tempJump.x(), tempJump.y(), tempJump.speedX(), tempJump.direction(), true, tempJump.isHighJump());
            }

            // Detect High Jump (upward velocity persists longer than a tap jump)
            if (player.getAirSpeed() < -2.0) {
                tempJump = new JumpSnapshot(tempJump.x(), tempJump.y(), tempJump.speedX(), tempJump.direction(), tempJump.usedDash(), true);
            }
        }

        // C. Land (commit to history)
        if (!playerInAir && tempJump != null) {
            jumpHistory.add(tempJump);
            if (jumpHistory.size() > 5) jumpHistory.remove(0);
            tempJump = null;
        }
    }

    /**
     * Finds a recorded player jump that occurred near the NPC's current location and moving in the desired direction.
     */
    private JumpSnapshot getNearestJumpSnapshot(Player player) {
        for (int i = jumpHistory.size() - 1; i >= 0; i--) {
            JumpSnapshot s = jumpHistory.get(i);
            if (s.isRelevant(this.hitBox)) {
                // Ensure the jump goes in the direction we want to go
                int directionToPlayer = (player.getHitBox().x > hitBox.x) ? 1 : -1;
                if (s.direction() == directionToPlayer) return s;
            }
        }
        return null;
    }

    /**
     * Checks the area directly above the NPC to ensure a standard jump won't hit a ceiling hazard.
     */
    private boolean isJumpSafe() {
        double jumpHeight = 3 * TILES_SIZE;
        Rectangle2D.Double ceilingCheck = new Rectangle2D.Double(hitBox.x, hitBox.y - jumpHeight, hitBox.width, jumpHeight);
        return !objectManager.isDangerous(ceilingCheck);
    }

    private void jump() {
        if (inAir) return;
        inAir = true;
        airSpeed = jumpSpeed;
    }

    /**
     * Scans ahead for safe landing spots and calculates the exact X-velocity needed to reach them using kinematic equations.
     *
     * @param levelData Level collision data.
     * @return The required X speed, or 0 if no valid path is found.
     */
    private double calculateAdaptiveJump(int[][] levelData) {
        int direction = (flipSign == 1) ? 1 : -1;
        double startX = hitBox.x;
        double startY = hitBox.y;

        double bestSpeedX = 0;
        double bestScore = Double.MAX_VALUE;

        // Check varying distances ahead
        for (double i = MIN_JUMP_DIST_TILES; i <= MAX_JUMP_DIST_TILES; i += 1.0) {
            double dist = i * TILES_SIZE;
            // Aim for the center of the tile, not the edge
            double targetX = startX + (direction * (dist + (TILES_SIZE / 2.0)));

            // Manual Raycast
            double targetY = -1;
            int checkTileX = (int)(targetX / TILES_SIZE);
            int startTileY = (int)((startY - (3 * TILES_SIZE)) / TILES_SIZE);
            int maxDepthY = startTileY + 8;
            if (checkTileX < 0 || checkTileX >= levelData.length) continue;

            for (int y = startTileY; y < maxDepthY && y < levelData[0].length; y++) {
                if (y >= 0 && CollisionDetector.isTileSolid(checkTileX, y, levelData)) {
                    targetY = y * TILES_SIZE;
                    break;
                }
            }

            if (targetY == -1 || targetY > startY + 5 * TILES_SIZE) continue;

            Rectangle2D.Double landingBox = new Rectangle2D.Double(targetX - hitBox.width/2, targetY - hitBox.height, hitBox.width, hitBox.height);
            if (objectManager.isDangerous(landingBox)) continue;

            // Physics: dy = vy*t + 0.5*g*t^2
            double dy = targetY - startY;
            double a = 0.5 * gravity;
            double b = jumpSpeed;
            double c = -dy;

            double D = b*b - 4*a*c;

            if (D >= 0) {
                // Larger root = time when falling back down
                double t = (-b + Math.sqrt(D)) / (2 * a);
                if (t > 0) {
                    double requiredSpeed = (targetX - startX) / t;
                    // Physical limits check
                    if (Math.abs(requiredSpeed) <= walkSpeed * 4.2) {
                        if (isTrajectoryClear(startX, startY, requiredSpeed, t, levelData)) {
                            // Prefer Higher Ground (smaller Y value)
                            if (targetY < bestScore) {
                                bestScore = targetY;
                                bestSpeedX = requiredSpeed;
                            }
                        }
                    }
                }
            }
        }
        return bestSpeedX;
    }

    /**
     * Simulates a parabolic trajectory frame-by-frame to ensure the NPC won't clip a wall or a hazard (spike) mid-jump.
     */
    private boolean isTrajectoryClear(double x, double y, double vx, double time, int[][] levelData) {
        double simX = x, simY = y;
        double simVy = jumpSpeed;

        for (int i = 0; i < time; i++) {
            // Euler Integration
            simX += vx;
            simY += simVy;
            simVy += gravity;

            // Skip first few frames to allow leaving the ground without triggering wall collision
            if (i < 5) continue;

            // Wall Check
            double leadingEdgeX = (vx > 0) ? simX + hitBox.width : simX;
            if (CollisionDetector.isSolid(leadingEdgeX, simY + hitBox.height / 2, levelData)) {
                return false;
            }

            // Hazard Check (Paranoid)
            Rectangle2D.Double bodyBox = new Rectangle2D.Double(simX, simY, hitBox.width, hitBox.height);
            if (objectManager.isDangerous(bodyBox)) return false;

            // Check the feet explicitly with padding (catches cases where the visual sprite barely scrapes a spike)
            Rectangle2D.Double footBox = new Rectangle2D.Double(simX + 5, simY + hitBox.height - 5, hitBox.width - 10, 10);
            if (objectManager.isDangerous(footBox)) return false;
        }
        return true;
    }

    /**
     * Checks if the ground below a point is solid and safe. Used to prevent walking off cliffs into spikes or void.
     */
    private boolean isLandingSafe(double x, double startY, int[][] levelData) {
        int xTile = (int) (x / TILES_SIZE);
        int yTile = (int) (startY / TILES_SIZE);

        // Scan downwards for solid ground
        for (int y = yTile; y < levelData[0].length; y++) {
            if (CollisionDetector.isTileSolid(xTile, y, levelData)) {
                Rectangle2D.Double tileHitbox = new Rectangle2D.Double(xTile * TILES_SIZE, y * TILES_SIZE, TILES_SIZE, TILES_SIZE);
                return !objectManager.isDangerous(tileHitbox);
            }
        }
        return false;
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            BufferedImage[][] anims = SpriteManager.getInstance().getFollowerAnimations(type);
            int maxFrames = (anims != null && entityState.ordinal() < anims.length) ? anims[entityState.ordinal()].length : 1;
            if (animIndex >= maxFrames) {
                animIndex = 0;
            }
        }
    }

    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        BufferedImage[][] anims = SpriteManager.getInstance().getFollowerAnimations(type);
        if (anims == null) return;

        int x = (int)hitBox.x - FOLLOWER_X_OFFSET - xLvlOffset + flipCoefficient;
        int y = (int)hitBox.y - FOLLOWER_Y_OFFSET - yLvlOffset;

        int stateIdx = entityState.ordinal();
        if (stateIdx < anims.length && anims[stateIdx] != null) {
            int idx = (animIndex < anims[stateIdx].length) ? animIndex : 0;
            g.drawImage(anims[stateIdx][idx], x, y, width * flipSign, height, null);
        }

        renderHitBox(g, xLvlOffset, yLvlOffset, Color.CYAN);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {}
}