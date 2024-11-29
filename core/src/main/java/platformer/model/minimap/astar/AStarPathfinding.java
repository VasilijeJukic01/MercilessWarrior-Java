package platformer.model.minimap.astar;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Provides an implementation of the A* algorithm for finding the shortest path between two points on a minimap.
 */
public class AStarPathfinding {

    private final int[] DX = {-1, 1, 0, 0};
    private final int[] DY = {0, 0, -1, 1};

    /**
     * Finds the shortest path from the start point to the end point on the minimap.
     *
     * @param minimap Image of the minimap. Walkable pixels are white (R=254, G=254, B=254).
     * @param start The starting point of the path.
     * @param end The target point of the path.
     * @return A list of Points representing the path from start to end. Returns an empty list if no path is found.
     * @throws IllegalArgumentException If the start or end point is not walkable.
     */
    public List<Point> findPath(BufferedImage minimap, Point start, Point end) {
        if (!isWalkable(minimap, start) || !isWalkable(minimap, end)) {
            throw new IllegalArgumentException("Start or end point is not walkable.");
        }

        // Priority queue (openSet) to hold nodes to explore, prioritized by their f-score.
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(AStarNode::getF));
        // Closed set to track nodes that have already been evaluated.
        Set<Point> closedSet = new HashSet<>();
        // Map to reconstruct the path from end to start.
        Map<Point, Point> cameFrom = new HashMap<>();

        openSet.add(new AStarNode(start, 0, heuristic(start, end)));

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();
            Point currentPoint = current.getPoint();

            if (currentPoint.equals(end)) return reconstructPath(cameFrom, end);

            closedSet.add(currentPoint);

            for (int i = 0; i < 4; i++) {
                Point neighbor = new Point(currentPoint.x + DX[i], currentPoint.y + DY[i]);
                if (!isValid(minimap, neighbor) || closedSet.contains(neighbor) || !isWalkable(minimap, neighbor)) continue;

                double tentativeG = current.getG() + 1;
                Optional<AStarNode> existingNode = openSet.stream()
                        .filter(n -> n.getPoint().equals(neighbor))
                        .findFirst();

                if (!existingNode.isPresent() || tentativeG < existingNode.get().getG()) {
                    cameFrom.put(neighbor, currentPoint);
                    existingNode.ifPresent(openSet::remove);
                    openSet.add(new AStarNode(neighbor, tentativeG, tentativeG + heuristic(neighbor, end)));
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * Validates whether a point lies within the bounds of the minimap.
     *
     * @param minimap BufferedImage representing the minimap.
     * @param point The point to validate.
     * @return True if the point is within bounds, false otherwise.
     */
    private boolean isValid(BufferedImage minimap, Point point) {
        return point.x >= 0 && point.y >= 0 && point.x < minimap.getWidth() && point.y < minimap.getHeight();
    }

    private boolean isWalkable(BufferedImage minimap, Point point) {
        int rgb = minimap.getRGB(point.x, point.y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return r == 78 && g == 105 && b == 80;
    }

    /**
     * Heuristic function for estimating the distance between two points using Manhattan distance.
     *
     * @param a The first point.
     * @param b The second point.
     * @return The Manhattan distance between the two points.
     */
    private double heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point end) {
        List<Point> path = new LinkedList<>();
        Point current = end;

        while (cameFrom.containsKey(current)) {
            path.add(0, current);
            current = cameFrom.get(current);
        }

        path.add(0, current);
        return path;
    }
}
