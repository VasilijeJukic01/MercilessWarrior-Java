package platformer.model.entities.follower;

import platformer.model.gameObjects.ObjectManager;
import platformer.physics.CollisionDetector;

import java.awt.geom.Rectangle2D;
import java.util.*;

import static platformer.constants.Constants.TILES_SIZE;

/**
 * 2D Platform-Graph A* Pathfinder and Ballistic Trajectory Calculator for Followers.
 * Solves multi-platform routes across steps, floating islands, gaps, overhangs, and vertical shafts.
 */
public class TrajectoryCalculator {

    public enum ActionType {
        WALK,
        STEP_HOP,
        LEDGE_DROP,
        BALLISTIC_JUMP
    }

    public record NavPlan(
            ActionType action,
            double takeoffX,
            double targetX,
            double targetY,
            double jumpVx,
            int moveDir,
            boolean useAirDash,
            boolean useWallBoost
    ) {}

    private static class AStarNode implements Comparable<AStarNode> {
        final int tx, ty;
        final double g, f;
        final AStarNode parent;
        final ActionType actionFromParent;
        final boolean dashJump;
        final boolean wallBoostJump;

        AStarNode(int tx, int ty, double g, double f, AStarNode parent, ActionType actionFromParent, boolean dashJump, boolean wallBoostJump) {
            this.tx = tx;
            this.ty = ty;
            this.g = g;
            this.f = f;
            this.parent = parent;
            this.actionFromParent = actionFromParent;
            this.dashJump = dashJump;
            this.wallBoostJump = wallBoostJump;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.f, other.f);
        }
    }

    private final ObjectManager objectManager;
    private final double gravity;
    private final double jumpSpeed;
    private final double walkSpeed;

    public TrajectoryCalculator(ObjectManager objectManager, double gravity, double jumpSpeed, double walkSpeed) {
        this.objectManager = objectManager;
        this.gravity = gravity;
        this.jumpSpeed = jumpSpeed;
        this.walkSpeed = walkSpeed;
    }

    /**
     * Finds the optimal next navigation action from the follower's current position to the goal (player or enemy).
     */
    public NavPlan findNextNavStep(Rectangle2D.Double hitBox, double goalWorldX, double goalWorldY, int[][] levelData) {
        if (levelData == null) return null;
        int w = levelData.length, h = levelData[0].length;

        int startTx = clamp((int) (hitBox.getCenterX() / TILES_SIZE), 0, w - 1);
        int startTy = findStandTileY(startTx, (int) ((hitBox.getMaxY() - 2) / TILES_SIZE), levelData);
        int goalTx = clamp((int) ((goalWorldX + hitBox.width * 0.5) / TILES_SIZE), 0, w - 1);
        int goalTy = findStandTileY(goalTx, (int) ((goalWorldY + hitBox.height - 2) / TILES_SIZE), levelData);

        if (startTy == -1 || goalTy == -1) return null;

        if (startTy == goalTy && isContinuousFlatWalk(startTx, goalTx, startTy, levelData)) {
            int dir = Integer.compare(goalTx, startTx);
            return new NavPlan(ActionType.WALK, goalWorldX, goalWorldX, goalWorldY, 0, dir == 0 ? 1 : dir, false, false);
        }

        PriorityQueue<AStarNode> open = new PriorityQueue<>();
        double[][] bestG = new double[w][h];
        for (int i = 0; i < w; i++) {
            Arrays.fill(bestG[i], Double.MAX_VALUE);
        }

        AStarNode startNode = new AStarNode(startTx, startTy, 0, heuristic(startTx, startTy, goalTx, goalTy), null, ActionType.WALK, false, false);
        open.add(startNode);
        bestG[startTx][startTy] = 0;

        AStarNode reachedGoal = null;
        AStarNode bestPartial = null;
        double bestPartialH = Double.MAX_VALUE;

        int iterations = 0;
        int maxIterations = 550;

        while (!open.isEmpty() && iterations < maxIterations) {
            AStarNode curr = open.poll();
            iterations++;
            if (curr.tx == goalTx && curr.ty == goalTy) {
                reachedGoal = curr;
                break;
            }

            double hDist = heuristic(curr.tx, curr.ty, goalTx, goalTy);
            if (curr.parent != null && hDist < bestPartialH) {
                bestPartialH = hDist;
                bestPartial = curr;
            }

            for (int dir = -1; dir <= 1; dir += 2) {
                int nx = curr.tx + dir;
                if (nx < 0 || nx >= w) continue;

                // Flat Walk
                if (isStandable(nx, curr.ty, levelData)) {
                    relax(curr, nx, curr.ty, 1.0, ActionType.WALK, false, false, goalTx, goalTy, bestG, open);
                }
                // Step-Up 1 or 2 Tiles
                for (int up = 1; up <= 2; up++) {
                    int ny = curr.ty - up;
                    if (ny >= 1 && isStandable(nx, ny, levelData) && isColumnAirClear(curr.tx, ny, curr.ty, levelData)) {
                        relax(curr, nx, ny, 1.3 + up * 0.4, ActionType.STEP_HOP, false, false, goalTx, goalTy, bestG, open);
                    }
                }
                // Ledge Drop-Down
                if (!CollisionDetector.isTileSolid(nx, curr.ty, levelData) && !CollisionDetector.isTileSolid(nx, curr.ty + 1, levelData)) {
                    for (int down = 1; down <= 7; down++) {
                        int ny = curr.ty + down;
                        if (ny >= h - 1) break;
                        if (isTileHazardous(nx, ny)) break;
                        if (CollisionDetector.isTileSolid(nx, ny, levelData)) break;
                        if (isStandable(nx, ny, levelData)) {
                            ActionType act = (down == 1) ? ActionType.WALK : ActionType.LEDGE_DROP;
                            relax(curr, nx, ny, 1.2 + down * 0.3, act, false, false, goalTx, goalTy, bestG, open);
                            break;
                        }
                    }
                }
                // Wall-Boost Climb
                for (int up = 3; up <= 4; up++) {
                    int ny = curr.ty - up;
                    if (ny >= 1 && isStandable(nx, ny, levelData) && isColumnAirClear(curr.tx, ny, curr.ty, levelData)) {
                        relax(curr, nx, ny, 2.2 + up * 0.5, ActionType.BALLISTIC_JUMP, false, true, goalTx, goalTy, bestG, open);
                    }
                }

                // Gap/Platform Jumps
                boolean atEdgeOrStart = !isStandable(nx, curr.ty, levelData) || (curr.tx == startTx && curr.ty == startTy);
                if (atEdgeOrStart) {
                    for (int dist = 2; dist <= 7; dist++) {
                        int jx = curr.tx + dir * dist;
                        if (jx < 0 || jx >= w) break;

                        for (int dy = -4; dy <= 3; dy++) {
                            if (dist >= 6 && dy < -1) continue;
                            if (dist >= 5 && dy < -2) continue;
                            if (dist >= 4 && dy < -3) continue;
                            int jy = curr.ty + dy;
                            if (jy < 1 || jy >= h - 1) continue;
                            if (isStandable(jx, jy, levelData) && isJumpArcClear(curr.tx, curr.ty, jx, jy, levelData)) {
                                boolean needsDash = (dist >= 5) || (dist >= 4 && dy <= -2);
                                boolean needsWallBoost = (dy <= -2);
                                double cost = dist * 1.3 + Math.abs(dy) * 0.8 + (needsDash ? 1.4 : 0.4);
                                relax(curr, jx, jy, cost, ActionType.BALLISTIC_JUMP, needsDash, needsWallBoost, goalTx, goalTy, bestG, open);
                            }
                        }
                    }
                }
            }
        }
        AStarNode chosen = (reachedGoal != null) ? reachedGoal : bestPartial;
        if (chosen == null || chosen.parent == null) return null;
        return buildFirstNavPlan(chosen, hitBox, goalWorldX, goalWorldY);
    }

    private void relax(AStarNode curr, int nx, int ny, double cost, ActionType action, boolean dash, boolean wallBoost, int goalTx, int goalTy, double[][] bestG, PriorityQueue<AStarNode> open) {
        double newG = curr.g + cost;
        if (newG < bestG[nx][ny]) {
            bestG[nx][ny] = newG;
            double f = newG + heuristic(nx, ny, goalTx, goalTy);
            open.add(new AStarNode(nx, ny, newG, f, curr, action, dash, wallBoost));
        }
    }

    private NavPlan buildFirstNavPlan(AStarNode destinationNode, Rectangle2D.Double hitBox, double finalGoalX, double finalGoalY) {
        List<AStarNode> path = new ArrayList<>();
        AStarNode cur = destinationNode;
        while (cur != null) {
            path.add(cur);
            cur = cur.parent;
        }
        Collections.reverse(path);
        if (path.size() < 2) return null;

        for (int i = 1; i < path.size(); i++) {
            AStarNode from = path.get(i - 1);
            AStarNode to = path.get(i);
            if (to.actionFromParent != ActionType.WALK) {
                int dir = Integer.compare(to.tx, from.tx);
                if (dir == 0) dir = 1;

                double takeoffX;
                if (to.actionFromParent == ActionType.LEDGE_DROP || to.actionFromParent == ActionType.STEP_HOP) {
                    takeoffX = (dir > 0) ? ((from.tx + 1) * TILES_SIZE - hitBox.width * 0.3) : (from.tx * TILES_SIZE - hitBox.width * 0.7);
                }
                else takeoffX = from.tx * TILES_SIZE + (TILES_SIZE - hitBox.width) * 0.5;

                double landX = to.tx * TILES_SIZE + (TILES_SIZE - hitBox.width) * 0.5;
                double landY = (to.ty + 1) * TILES_SIZE - hitBox.height - 1;
                double reqVx = computeBallisticVx(takeoffX, (from.ty + 1) * TILES_SIZE - hitBox.height - 1, landX, landY, dir, to.wallBoostJump);
                return new NavPlan(to.actionFromParent, takeoffX, landX, landY, reqVx, dir, to.dashJump, to.wallBoostJump);
            }
        }

        AStarNode last = path.get(path.size() - 1);
        int dir = Integer.compare(last.tx, path.get(0).tx);
        return new NavPlan(ActionType.WALK, finalGoalX, finalGoalX, finalGoalY, 0, dir == 0 ? 1 : dir, false, false);
    }

    private double computeBallisticVx(double startX, double startY, double landX, double landY, int dir, boolean wallBoost) {
        double dx = landX - startX;
        double dy = landY - startY;
        double effJumpSpeed = wallBoost ? (jumpSpeed * 1.28) : jumpSpeed;

        double a = 0.5 * gravity;
        double b = effJumpSpeed;
        double c = -dy;
        double D = b * b - 4 * a * c;

        if (D >= 0) {
            double t = (-b + Math.sqrt(D)) / (2 * a);
            if (t > 4) {
                double vx = dx / t;
                double maxSpeed = walkSpeed * 4.4;
                return Math.max(-maxSpeed, Math.min(maxSpeed, vx));
            }
        }
        return dir * walkSpeed * 2.2;
    }

    /**
     * Fallback local kinematic jump scan (includes 1-tile and 2-tile high ledges across 1-7 tile gaps).
     */
    public double calculateAdaptiveJump(Rectangle2D.Double hitBox, int flipSign, int[][] levelData, double playerY) {
        int direction = (flipSign == 1) ? 1 : -1;
        double startX = hitBox.x;
        double startY = hitBox.y;
        boolean allowLowerLanding = playerY > (startY + TILES_SIZE);

        double bestSpeedX = 0;
        double bestScore = Double.MAX_VALUE;

        for (double i = 1; i <= 7; i += 1.0) {
            double dist = i * TILES_SIZE;
            double targetX = startX + (direction * (dist + (TILES_SIZE / 2.0)));
            double targetY = -1;
            int checkTileX = (int) (targetX / TILES_SIZE);
            int startTileY = (int) ((startY - (4 * TILES_SIZE)) / TILES_SIZE);
            int maxDepthY = startTileY + 9;
            if (checkTileX < 0 || checkTileX >= levelData.length) continue;

            for (int y = startTileY; y < maxDepthY && y < levelData[0].length; y++) {
                if (y > 0 && CollisionDetector.isTileSolid(checkTileX, y, levelData) && !CollisionDetector.isTileSolid(checkTileX, y - 1, levelData)) {
                    targetY = y * TILES_SIZE;
                    break;
                }
            }

            if (targetY == -1 || targetY > startY + 5 * TILES_SIZE) continue;

            double landingTopY = targetY - hitBox.height;
            if (!allowLowerLanding && landingTopY > startY + TILES_SIZE * 0.5) continue;

            Rectangle2D.Double landingBox = new Rectangle2D.Double(targetX - hitBox.width / 2, landingTopY, hitBox.width, hitBox.height);
            if (objectManager.isDangerous(landingBox)) continue;

            double dy = targetY - startY;
            double effJump = (dy < -1.8 * TILES_SIZE) ? (jumpSpeed * 1.25) : jumpSpeed;
            double a = 0.5 * gravity;
            double b = effJump;
            double c = -dy;
            double D = b * b - 4 * a * c;

            if (D >= 0) {
                double t = (-b + Math.sqrt(D)) / (2 * a);
                if (t > 0) {
                    double requiredSpeed = (targetX - startX) / t;
                    if (Math.abs(requiredSpeed) <= walkSpeed * 4.5) {
                        if (isTrajectoryClear(startX, startY, requiredSpeed, effJump, t, hitBox, levelData)) {
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

    private boolean isTrajectoryClear(double x, double y, double vx, double initVy, double time, Rectangle2D.Double hitBox, int[][] levelData) {
        double simX = x, simY = y;
        double simVy = initVy;

        for (int i = 0; i < time; i++) {
            simX += vx;
            simY += simVy;
            simVy += gravity;

            if (i < 12) continue;
            double leadingEdgeX = (vx > 0) ? simX + hitBox.width : simX;
            if (CollisionDetector.isSolid(leadingEdgeX, simY + hitBox.height / 2, levelData)) return false;
            Rectangle2D.Double bodyBox = new Rectangle2D.Double(simX, simY, hitBox.width, hitBox.height);
            if (objectManager.isDangerous(bodyBox)) return false;
        }
        return true;
    }

    private boolean isJumpArcClear(int x0, int y0, int x1, int y1, int[][] levelData) {
        int topY = Math.max(1, Math.min(y0, y1));
        if (!isColumnAirClear(x0, topY, y0, levelData)) return false;

        int dir = (x1 > x0) ? 1 : -1;
        int steps = Math.abs(x1 - x0);
        for (int s = 1; s < steps; s++) {
            int cx = x0 + dir * s;
            double progress = (double) s / steps;
            int arcY = (int) Math.round(topY - Math.sin(progress * Math.PI) * 1.0);
            arcY = clamp(arcY, 1, levelData[0].length - 2);
            if (CollisionDetector.isTileSolid(cx, arcY, levelData) || CollisionDetector.isTileSolid(cx, arcY - 1, levelData)) {
                return false;
            }
            if (isTileHazardous(cx, arcY)) return false;
        }
        return true;
    }

    private boolean isColumnAirClear(int tx, int topTy, int bottomTy, int[][] levelData) {
        for (int y = Math.max(1, topTy); y <= bottomTy; y++) {
            if (!isAirClear(tx, y, levelData)) return false;
        }
        return true;
    }

    private boolean isAirClear(int tx, int ty, int[][] levelData) {
        if (tx < 0 || tx >= levelData.length || ty < 1 || ty >= levelData[0].length) return false;
        return !CollisionDetector.isTileSolid(tx, ty, levelData) && !CollisionDetector.isTileSolid(tx, ty - 1, levelData);
    }

    private boolean isContinuousFlatWalk(int tx0, int tx1, int ty, int[][] levelData) {
        int step = (tx1 >= tx0) ? 1 : -1;
        for (int x = tx0; x != tx1; x += step) {
            if (!isStandable(x, ty, levelData)) return false;
        }
        return isStandable(tx1, ty, levelData);
    }

    private int findStandTileY(int tx, int startTy, int[][] levelData) {
        int h = levelData[0].length;
        int clamped = clamp(startTy, 1, h - 2);
        for (int y = clamped; y < Math.min(h - 1, clamped + 6); y++) {
            if (isStandable(tx, y, levelData)) return y;
        }
        for (int y = clamped - 1; y >= Math.max(1, clamped - 4); y--) {
            if (isStandable(tx, y, levelData)) return y;
        }
        return -1;
    }

    private boolean isStandable(int tx, int ty, int[][] levelData) {
        if (tx < 0 || tx >= levelData.length || ty < 1 || ty >= levelData[0].length - 1) return false;
        if (CollisionDetector.isTileSolid(tx, ty, levelData) || CollisionDetector.isTileSolid(tx, ty - 1, levelData)) {
            return false;
        }
        if (!CollisionDetector.isTileSolid(tx, ty + 1, levelData)) {
            return false;
        }
        return !isTileHazardous(tx, ty);
    }

    private boolean isTileHazardous(int tx, int ty) {
        Rectangle2D.Double box = new Rectangle2D.Double(tx * TILES_SIZE + 4, ty * TILES_SIZE + 4, TILES_SIZE - 8, TILES_SIZE - 6);
        return objectManager.isDangerous(box);
    }

    public boolean isLandingSafe(double x, double startY, int[][] levelData) {
        int xTile = (int) (x / TILES_SIZE);
        int yTile = (int) (startY / TILES_SIZE);
        if (xTile < 0 || xTile >= levelData.length) return false;

        for (int y = Math.max(0, yTile); y < levelData[0].length; y++) {
            if (isTileHazardous(xTile, y)) return false;
            if (CollisionDetector.isTileSolid(xTile, y, levelData)) {
                return !isTileHazardous(xTile, Math.max(0, y - 1));
            }
        }
        return false;
    }

    private double heuristic(int x0, int y0, int x1, int y1) {
        return Math.abs(x1 - x0) + Math.abs(y1 - y0) * 1.35;
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}