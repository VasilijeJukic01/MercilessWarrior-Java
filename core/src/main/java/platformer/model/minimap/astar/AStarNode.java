package platformer.model.minimap.astar;

import lombok.Getter;

import java.awt.*;

@Getter
public class AStarNode  {

    private final Point point;
    // Cost from start to current node
    private final double g;
    // Total estimated cost (g + heuristic)
    private final double f;

    public AStarNode(Point point, double g, double f) {
        this.point = point;
        this.g = g;
        this.f = f;
    }

}
