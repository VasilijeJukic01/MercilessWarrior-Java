package platformer.model.entities.player;

import platformer.core.GameContext;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.Direction;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.interior.InteriorManager;
import platformer.model.perks.PerksBonus;
import platformer.physics.CollisionDetector;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

/**
 * Handles all player kinematics, jumping, coyote time, jump buffering, wall sliding, wall kicking, dash movement, and post-dash momentum conservation.
 */
public class PlayerMovementHandler {

    private static final int COYOTE_TIME_TICKS = 18;
    private static final int WALL_COYOTE_TICKS = 16;
    private static final int JUMP_BUFFER_TICKS = 22;
    private static final int WALL_JUMP_LOCK_TICKS = 22;
    private static final int MAX_JUMPS = 2;
    private static final double POST_DASH_MOMENTUM_DECAY = 0.88;

    private final Player player;
    private final ObjectManager objectManager;
    private final InteriorManager interiorManager;
    private final EffectManager effectManager;

    // Physics
    private final double downwardGravity = 0.03 * SCALE;
    private final double upwardGravity = 0.022 * SCALE;
    private final double jumpCutGravityMultiplier = 3.0;
    private final double apexGravityMultiplier = 0.55;
    private final double apexSpeedThreshold = 0.32 * SCALE;
    private final double maxFallSpeed = 3.6 * SCALE;

    private final double wallSlideAcceleration = 0.012 * SCALE;
    private final double maxWallSlideSpeed = 0.45 * SCALE;
    private final double fastWallSlideSpeed = 1.35 * SCALE;
    private final double wallJumpSpeedX = 1.45 * SCALE;

    private final double jumpSpeed = -2.25 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    // Status
    private int currentJumps = 0;
    private int coyoteTimer = 0;
    private int wallCoyoteTimer = 0;
    private int lastWallSide = 0;
    private int jumpBufferTimer = 0;
    private int wallJumpLockTimer = 0;
    private int wallJumpDir = 0;
    private int dashTick = 0;
    private double dashMomentumX = 0.0;

    public PlayerMovementHandler(Player player, GameContext context) {
        this.player = player;
        this.objectManager = context.getObjectManager();
        this.interiorManager = context.getInteriorManager();
        this.effectManager = context.getEffectManager();
    }

    public void queueJumpBuffer() {
        this.jumpBufferTimer = JUMP_BUFFER_TICKS;
    }

    public void updatePosition(int[][] levelData, PlayerCombatHandler combatHandler, PlayerMinimapHandler minimapHandler) {
        if (checkTransitionUpdate()) return;

        Rectangle2D.Double hitBox = player.getHitBox();
        player.removeAction(PlayerAction.MOVE);
        checkOnObject();
        boolean onObject = player.checkAction(PlayerAction.ON_OBJECT);
        boolean onFloor = isEntityOnFloor(hitBox, levelData) || onObject;

        if (!player.isInAir() && !onFloor) {
            player.setInAir(true);
            coyoteTimer = COYOTE_TIME_TICKS;
            if (currentJumps == 0) currentJumps = 1;
        }
        else if (!player.isInAir() && onFloor) {
            coyoteTimer = COYOTE_TIME_TICKS;
            wallCoyoteTimer = 0;
            currentJumps = 0;
            wallJumpLockTimer = 0;
            player.removeAction(PlayerAction.DOUBLE_JUMP);
        }
        else if (player.isInAir() && coyoteTimer > 0) {
            coyoteTimer--;
        }

        if (wallCoyoteTimer > 0) wallCoyoteTimer--;
        if (jumpBufferTimer > 0) jumpBufferTimer--;
        if (wallJumpLockTimer > 0) wallJumpLockTimer--;

        boolean canTransform = player.checkAction(PlayerAction.CAN_TRANSFORM);
        boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
        if (player.getSpellState() != 0 || canBlock || canTransform) return;
        updateWallPosition(levelData);
        if (jumpBufferTimer > 0) doJump(levelData, combatHandler);
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);
        if (!player.isInAir() || onWall) player.getActionHandler().setDashCount(0);
        boolean left = player.checkAction(PlayerAction.LEFT);
        boolean right = player.checkAction(PlayerAction.RIGHT);
        boolean dash = player.checkAction(PlayerAction.DASH);
        boolean blocking = player.checkAction(PlayerAction.BLOCK);

        boolean hasImpulse = combatHandler.hasActiveImpulse() || Math.abs(dashMomentumX) >= 0.05;
        if (((!left && !right) || (left && right)) && !player.isInAir() && !dash && !hasImpulse && !player.checkAction(PlayerAction.ATTACK) && wallJumpLockTimer == 0) {
            double slip = CollisionDetector.getLedgeSlip(hitBox, levelData);
            if (slip == 0) return;
        }

        double dx = 0;
        if (wallJumpLockTimer > 0 && !dash) {
            dx = wallJumpDir * wallJumpSpeedX;
        }
        else {
            boolean slowedByLava = player.checkAction(PlayerAction.LAVA) && !PerksBonus.getInstance().isLavaWalk();
            double baseSpeed = slowedByLava ? LAVA_PLAYER_SPEED : PLAYER_SPEED;
            if (player.isInAir() && Math.abs(player.getAirSpeed()) < apexSpeedThreshold) baseSpeed *= 1.12;
            if (blocking && !player.isInAir()) baseSpeed *= 0.35;
            else if (player.checkAction(PlayerAction.ATTACK) && !player.isInAir()) baseSpeed *= 0.65;

            if (left) dx -= baseSpeed;
            if (right) dx += baseSpeed;
            if (!left && !right && !player.isInAir()) {
                dx += CollisionDetector.getLedgeSlip(hitBox, levelData);
            }
            boolean wallPush = player.checkAction(PlayerAction.WALL_PUSH);
            if (right && player.isInAir() && wallPush && !dash) dx += PLAYER_BOOST;
            if (left && player.isInAir() && wallPush && !dash) dx -= PLAYER_BOOST;
        }

        dx += combatHandler.consumeCombatDisplacementX(levelData);

        if (dash) {
            wallJumpLockTimer = 0;
            dashMomentumX = 0;
            combatHandler.clearImpulses();
            if (((!left && !right) || (left && right)) && player.getFlipSign() == -1) dx = -PLAYER_SPEED;
            else if (((!left && !right) || (left && right)) && player.getFlipSign() == 1) dx = PLAYER_SPEED;
            dx *= DASH_SPEED;
        }
        else if (Math.abs(dashMomentumX) >= 0.05) {
            if ((dashMomentumX > 0 && left) || (dashMomentumX < 0 && right)) {
                dashMomentumX *= 0.65;
            }
            else {
                dx += dashMomentumX;
                dashMomentumX *= POST_DASH_MOMENTUM_DECAY;
            }
            if (Math.abs(dashMomentumX) < 0.05) {
                dashMomentumX = 0;
            }
        }

        if (player.isInAir() && !dash) inAirUpdate(levelData);
        updateX(dx, levelData, combatHandler);
        minimapHandler.update();
        if (left || right || player.isInAir() || dash || Math.abs(dashMomentumX) >= 0.05) {
            player.addAction(PlayerAction.MOVE);
        }
    }

    private boolean checkTransitionUpdate() {
        if (player.checkAction(PlayerAction.LEVEL_TRANSITION)) {
            if (player.isInAir()) {
                player.setAirSpeed(player.getAirSpeed() + downwardGravity);
                player.getHitBox().y += player.getAirSpeed();
            }
            player.getHitBox().x += getHorizontalSpeed();
            player.addAction(PlayerAction.MOVE);
            return true;
        }
        return false;
    }

    public void inAirUpdate(int[][] levelData) {
        Rectangle2D.Double hitBox = player.getHitBox();
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);
        double airSpeed = player.getAirSpeed();

        if (airSpeed < 0) {
            if (!player.checkAction(PlayerAction.JUMP)) {
                airSpeed += upwardGravity * jumpCutGravityMultiplier;
            }
            else if (Math.abs(airSpeed) < apexSpeedThreshold) {
                airSpeed += upwardGravity * apexGravityMultiplier;
            }
            else airSpeed += upwardGravity;
        }
        else {
            if (onWall) {
                double targetSlideCap = player.checkAction(PlayerAction.LOOK_DOWN) ? fastWallSlideSpeed : maxWallSlideSpeed;
                airSpeed = Math.min(targetSlideCap, airSpeed + wallSlideAcceleration);
            }
            else if (Math.abs(airSpeed) < apexSpeedThreshold) {
                airSpeed += downwardGravity * apexGravityMultiplier;
            }
            else airSpeed = Math.min(maxFallSpeed, airSpeed + downwardGravity);
        }

        if (canMoveHere(hitBox.x, hitBox.y + airSpeed + 1, hitBox.width, hitBox.height, levelData)) {
            hitBox.y += airSpeed;
            player.setAirSpeed(airSpeed);
        }
        else {
            hitBox.y = getYPosOnTheCeil(hitBox, airSpeed);
            if (airSpeed > 0) {
                player.removeActions(PlayerAction.WALL_PUSH, PlayerAction.DOUBLE_JUMP, PlayerAction.ON_WALL);
                player.setInAir(false);
                player.setAirSpeed(0);
                currentJumps = 0;
                wallJumpLockTimer = 0;
                wallCoyoteTimer = 0;
                coyoteTimer = COYOTE_TIME_TICKS;

                boolean isHit = player.checkAction(PlayerAction.HIT);
                if (!isHit) {
                    effectManager.spawnDustParticles(hitBox.x + hitBox.width / 2, hitBox.y + hitBox.height, LAND_DUST_BURST, DustType.IMPACT, player.getFlipSign(), player);
                }
            }
            else player.setAirSpeed((onWall) ? wallSlideAcceleration : collisionFallSpeed);
        }
    }

    private void updateWallPosition(int[][] levelData) {
        if (interiorManager != null && interiorManager.isInterior()) return;
        if (wallJumpLockTimer > 0) {
            player.removeAction(PlayerAction.ON_WALL);
            return;
        }

        Rectangle2D.Double hitBox = player.getHitBox();
        boolean left = player.checkAction(PlayerAction.LEFT);
        boolean right = player.checkAction(PlayerAction.RIGHT);
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);

        if (onWall) player.removeAction(PlayerAction.ATTACK);

        boolean leftSideCheck = isOnWall(hitBox, levelData, Direction.LEFT);
        boolean rightSideCheck = isOnWall(hitBox, levelData, Direction.RIGHT);

        if (player.isInAir() && !isEntityOnFloor(hitBox, levelData) && (leftSideCheck || rightSideCheck)) {
            lastWallSide = leftSideCheck ? -1 : 1;
            wallCoyoteTimer = WALL_COYOTE_TICKS;
        }

        if (!onWall && ((left && leftSideCheck) || (right && rightSideCheck)) && !isEntityOnFloor(hitBox, levelData) && player.getAirSpeed() >= 0) {
            int wallSide = leftSideCheck ? -1 : 1;
            player.setFacingDirection(wallSide);
            player.addActions(PlayerAction.ON_WALL, PlayerAction.WALL_PUSH);
            player.removeAction(PlayerAction.DOUBLE_JUMP);
            currentJumps = 1;
            lastWallSide = wallSide;
            wallCoyoteTimer = WALL_COYOTE_TICKS;
            dashMomentumX = 0;
            player.setAirSpeed(Math.min(player.getAirSpeed(), 0.15 * SCALE));
        }
        if (onWall) {
            wallCoyoteTimer = WALL_COYOTE_TICKS;
            if (!leftSideCheck && !rightSideCheck || (!left && leftSideCheck) || (!right && rightSideCheck)) {
                currentJumps = 1;
                player.removeAction(PlayerAction.ON_WALL);
            }
        }
    }

    private void updateX(double dx, int[][] levelData, PlayerCombatHandler combatHandler) {
        Rectangle2D.Double hitBox = player.getHitBox();
        boolean dash = player.checkAction(PlayerAction.DASH);
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);
        boolean onObject = player.checkAction(PlayerAction.ON_OBJECT);

        double actualDx = objectManager.checkSolidObjectCollision(hitBox, dx);
        if (dash && actualDx != dx) {
            player.removeAction(PlayerAction.DASH);
            player.removeAction(PlayerAction.DASH_HIT);
            dashTick = 0;
            dashMomentumX = 0;
        }

        if (!onObject && !onWall && canMoveHere(hitBox.x + actualDx, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x += actualDx;
        }
        else if (dash && canMoveHere(hitBox.x + actualDx, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x += actualDx;
        }
        else {
            if (onObject) hitBox.x = objectManager.getXObjectBound(hitBox, actualDx);
            else if (!onWall) hitBox.x = getXPosOnTheWall(hitBox, actualDx);

            if (dash) {
                player.removeAction(PlayerAction.DASH);
                player.removeAction(PlayerAction.DASH_HIT);
                dashTick = 0;
            }
            dashMomentumX = 0;
            combatHandler.clearAttackLunge();
        }
    }

    private void checkOnObject() {
        boolean onObject = player.checkAction(PlayerAction.ON_OBJECT);

        if (objectManager.isPlayerTouchingObject(player) && !onObject) {
            player.setInAir(!objectManager.isPlayerGlitchedInObject(player));
            player.removeActions(PlayerAction.WALL_PUSH, PlayerAction.DOUBLE_JUMP);
            player.setAirSpeed(0);
            currentJumps = 0;
            wallCoyoteTimer = 0;
            coyoteTimer = COYOTE_TIME_TICKS;
            player.addAction(PlayerAction.ON_OBJECT);
        }
        else if (onObject && !objectManager.isPlayerTouchingObject(player)) {
            player.removeAction(PlayerAction.ON_OBJECT);
        }

        if (onObject) player.removeAction(PlayerAction.WALL_PUSH);
    }

    public void doJump(int[][] levelData, PlayerCombatHandler combatHandler) {
        Rectangle2D.Double hitBox = player.getHitBox();
        boolean left = player.checkAction(PlayerAction.LEFT);
        boolean right = player.checkAction(PlayerAction.RIGHT);
        boolean doubleJump = player.checkAction(PlayerAction.DOUBLE_JUMP);
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);

        boolean wallLeftNow = player.isInAir() && isOnWall(hitBox, levelData, Direction.LEFT);
        boolean wallRightNow = player.isInAir() && isOnWall(hitBox, levelData, Direction.RIGHT);
        boolean canWallJump = onWall || wallLeftNow || wallRightNow || (player.isInAir() && wallCoyoteTimer > 0);

        if (!canWallJump && !player.getActionHandler().canJump(levelData, left, right)) return;

        combatHandler.cancelForMobility();

        if (player.checkAction(PlayerAction.DASH)) {
            player.removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT);
            dashTick = 0;
            dashMomentumX = player.getFlipSign() * PLAYER_SPEED * (DASH_SPEED - 1.0) * 0.72;
        }

        if (canWallJump) {
            int wallSide;
            if (wallLeftNow) wallSide = -1;
            else if (wallRightNow) wallSide = 1;
            else if (lastWallSide != 0) wallSide = lastWallSide;
            else wallSide = isOnWall(hitBox, levelData, Direction.LEFT) ? -1 : 1;

            int kickDir = -wallSide;

            double dustX = (wallSide == -1) ? hitBox.x : hitBox.x + hitBox.width;
            double dustY = hitBox.y + hitBox.height / 2.0;
            effectManager.spawnDustParticles(dustX, dustY, 12, DustType.WALL_JUMP, wallSide, player);

            player.setFacingDirection(kickDir);
            this.wallJumpDir = kickDir;
            this.wallJumpLockTimer = WALL_JUMP_LOCK_TICKS;
            this.wallCoyoteTimer = 0;
            combatHandler.clearImpulses();

            player.removeActions(PlayerAction.ON_WALL, PlayerAction.DOUBLE_JUMP);
            player.addAction(PlayerAction.WALL_PUSH);
            currentJumps = 1;
            coyoteTimer = 0;
            jumpBufferTimer = 0;
            player.setInAir(true);
            player.setAirSpeed(jumpSpeed * 1.02);
            player.resetAnimTickAndIndex();
            return;
        }

        if (!player.isInAir() || coyoteTimer > 0) {
            currentJumps = 1;
            coyoteTimer = 0;
            jumpBufferTimer = 0;
            player.setInAir(true);
            player.setAirSpeed(jumpSpeed);
            player.resetAnimTickAndIndex();
            effectManager.spawnDustParticles(hitBox.x + hitBox.width / 2, hitBox.y + hitBox.height, JUMP_DUST_BURST, DustType.IMPACT, player.getFlipSign(), player);
            return;
        }

        if (currentJumps < MAX_JUMPS && !doubleJump) {
            currentJumps = MAX_JUMPS;
            player.addAction(PlayerAction.DOUBLE_JUMP);
            wallJumpLockTimer = 0;
            jumpBufferTimer = 0;
            player.setInAir(true);
            player.setAirSpeed(jumpSpeed * 0.94);
            player.resetAnimTickAndIndex();
            double feetX = hitBox.getCenterX();
            double feetY = hitBox.y + hitBox.height;
            effectManager.spawnDustParticles(feetX, feetY, 1, DustType.DOUBLE_JUMP, 0, player);
            effectManager.spawnDustParticles(feetX, feetY, 8, DustType.DOUBLE_JUMP, player.getFlipSign(), player);
        }
    }

    public void updateDashTimer() {
        if (player.checkAction(PlayerAction.DASH)) {
            dashTick++;
            if (dashTick >= 40) {
                dashTick = 0;
                player.removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT);
                dashMomentumX = player.getFlipSign() * PLAYER_SPEED * (DASH_SPEED - 1.0) * 0.60;
            }
        }
    }

    public void handlePushback(Direction pushDirection, int[][] levelData) {
        Rectangle2D.Double hitBox = player.getHitBox();
        double pushForce = 1.2;
        double basePushSpeed = (pushDirection == Direction.RIGHT) ? PLAYER_SPEED : -PLAYER_SPEED;
        double totalPushDistance = basePushSpeed * pushForce;

        double allowedObjectDx = objectManager.checkSolidObjectCollision(hitBox, totalPushDistance);
        if (canMoveHere(hitBox.x + allowedObjectDx, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x += allowedObjectDx;
        }
    }

    public void launch(double launchSpeed) {
        Rectangle2D.Double hitBox = player.getHitBox();
        effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.y + hitBox.height, 25, DustType.JUMP_PAD, 0, player);
        player.setInAir(true);
        currentJumps = 1;
        wallJumpLockTimer = 0;
        wallCoyoteTimer = 0;
        player.removeAction(PlayerAction.DOUBLE_JUMP);
        player.setAirSpeed(launchSpeed);
    }

    public double getHorizontalSpeed() {
        boolean slowedByLava = player.checkAction(PlayerAction.LAVA) && !PerksBonus.getInstance().isLavaWalk();
        double currentSpeed = slowedByLava ? LAVA_PLAYER_SPEED : PLAYER_SPEED;
        if (player.checkAction(PlayerAction.DASH)) currentSpeed *= DASH_SPEED;
        if (player.checkAction(PlayerAction.LEFT) && !player.checkAction(PlayerAction.RIGHT)) return -currentSpeed + dashMomentumX;
        else if (player.checkAction(PlayerAction.RIGHT) && !player.checkAction(PlayerAction.LEFT)) return currentSpeed + dashMomentumX;
        return dashMomentumX;
    }

    public void clearMomentum() {
        this.dashMomentumX = 0.0;
    }

    public boolean isWallJumpLocked() {
        return wallJumpLockTimer > 0;
    }

    public void reset() {
        currentJumps = 0;
        coyoteTimer = 0;
        wallCoyoteTimer = 0;
        lastWallSide = 0;
        jumpBufferTimer = 0;
        wallJumpLockTimer = 0;
        dashTick = 0;
        dashMomentumX = 0.0;
    }
}