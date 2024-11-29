package platformer.model.minimap.astar;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Provides an implementation of the A* algorithm for finding the shortest path between two points on a minimap.
 */
public class AStarPathfinding {

    private final int[] DX = {-1, 1, 0, 0, -1, -1, 1, 1};
    private final int[] DY = {0, 0, -1, 1, -1, 1, -1, 1};
    private final double[] COST = {1, 1, 1, 1, Math.sqrt(2), Math.sqrt(2), Math.sqrt(2), Math.sqrt(2)};
    private boolean[][] cachedWalkable;
    private BufferedImage cachedMinimap;

    /**
     * Finds the shortest path from the start point to the end point on the minimap.
     *
     * @param minimap Image of the minimap.
     * @param start   The starting point of the path.
     * @param end     The target point of the path.
     * @return A list of Points representing the path from start to end. Returns an empty list if no path is found.
     * @throws IllegalArgumentException If the start or end point is not walkable.
     */
    public List<Point> findPath(BufferedImage minimap, Point start, Point end) {
        boolean[][] walkable = preprocessWalkability(minimap);

        if (!isWalkableAndValid(walkable, start) || !isWalkableAndValid(walkable, end))
            throw new IllegalArgumentException("Start or end point is not walkable or valid.");

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(AStarNode::getF));
        Set<Point> closedSet = new HashSet<>();
        Map<Point, Double> gScores = new HashMap<>();
        Map<Point, Point> cameFrom = new HashMap<>();

        openSet.add(new AStarNode(start, 0, heuristic(start, end)));
        gScores.put(start, 0.0);

        while (!openSet.isEmpty()) {
            AStarNode currentNode = openSet.poll();
            Point currentPoint = currentNode.getPoint();

            if (currentPoint.equals(end)) return reconstructPath(cameFrom, end);
            if (!closedSet.add(currentPoint)) continue;

            for (int i = 0; i < 8; i++) {
                int nx = currentPoint.x + DX[i];
                int ny = currentPoint.y + DY[i];
                Point neighbor = new Point(nx, ny);

                if (!isWalkableAndValid(walkable, neighbor) || closedSet.contains(neighbor)) continue;

                double tentativeG = gScores.getOrDefault(currentPoint, Double.MAX_VALUE) + COST[i];
                if (tentativeG < gScores.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    gScores.put(neighbor, tentativeG);
                    double f = tentativeG + heuristic(neighbor, end);
                    openSet.add(new AStarNode(neighbor, tentativeG, f));
                    cameFrom.put(neighbor, currentPoint);
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * Precomputes a boolean array for walkable points on the minimap.
     *
     * Caches the result if the minimap has not changed since the last call.
     *
     * @param minimap Image of the minimap.
     * @return A 2D boolean array where true indicates a walkable point.
     */
    private boolean[][] preprocessWalkability(BufferedImage minimap) {
        if (minimap.equals(cachedMinimap) && cachedWalkable != null) return cachedWalkable;

        int width = minimap.getWidth();
        int height = minimap.getHeight();
        boolean[][] walkable = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = minimap.getRGB(x, y) & 0xFFFFFF;
                // RGB (78, 105, 80)
                walkable[x][y] = (rgb == 0x4E6950);
            }
        }

        cachedWalkable = walkable;
        cachedMinimap = minimap;
        return walkable;
    }

    /**
     * Checks if the given point is walkable and within bounds.
     *
     * @param walkable 2D boolean array indicating walkable points.
     * @param point    The point to check.
     * @return True if the point is walkable and within bounds, false otherwise.
     */
    private boolean isWalkableAndValid(boolean[][] walkable, Point point) {
        return point.x >= 0 && point.y >= 0
                && point.x < walkable.length && point.y < walkable[0].length
                && walkable[point.x][point.y];
    }

    /**
     * Heuristic function for estimating the distance between two points using Euclidean distance.
     *
     * @param a The first point.
     * @param b The second point.
     * @return The Euclidean distance between the two points.
     */
    private double heuristic(Point a, Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    /**
     * Reconstructs the path from the start to the end using the cameFrom map.
     *
     * @param cameFrom Map storing the path relationships.
     * @param end      The target point.
     * @return A list of Points representing the path.
     */
    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point end) {
        List<Point> path = new ArrayList<>();
        Point current = end;

        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }

        path.add(current);
        Collections.reverse(path);
        return path;
    }
}
