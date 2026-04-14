package platformer.model.entities.follower;

import platformer.model.gameObjects.ObjectManager;
import platformer.physics.CollisionDetector;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.TILES_SIZE;

/**
 * Handles Predictive Physics for the follower AI.
 * Uses quadratic projectile motion equations to calculate jump velocities required to cross gaps or climb ledges.
 */
public class TrajectoryCalculator {

    private final ObjectManager objectManager;
    private final double gravity;
    private final double jumpSpeed;
    private final double walkSpeed;

    private static final int MAX_JUMP_DIST_TILES = 7;
    private static final int MIN_JUMP_DIST_TILES = 2;

    public TrajectoryCalculator(ObjectManager objectManager, double gravity, double jumpSpeed, double walkSpeed) {
        this.objectManager = objectManager;
        this.gravity = gravity;
        this.jumpSpeed = jumpSpeed;
        this.walkSpeed = walkSpeed;
    }

    /**
     * Scans ahead for safe landing spots and calculates the exact X-velocity needed to reach them using kinematic equations.
     *
     * @param levelData Level collision data.
     * @return The required X speed, or 0 if no valid path is found.
     */
    public double calculateAdaptiveJump(Rectangle2D.Double hitBox, int flipSign, int[][] levelData) {
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
                        if (isTrajectoryClear(startX, startY, requiredSpeed, t, hitBox, levelData)) {
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
    private boolean isTrajectoryClear(double x, double y, double vx, double time, Rectangle2D.Double hitBox, int[][] levelData) {
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
            if (CollisionDetector.isSolid(leadingEdgeX, simY + hitBox.height / 2, levelData)) return false;

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
    public boolean isLandingSafe(double x, double startY, int[][] levelData) {
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
}