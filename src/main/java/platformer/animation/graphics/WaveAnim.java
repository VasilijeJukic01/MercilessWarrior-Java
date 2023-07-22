package platformer.animation.graphics;

import platformer.model.Tiles;
import platformer.model.entities.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WaveAnim implements GraphicsAnimation {

    private Direction direction;
    private int x, y;
    private int xOffset, yOffset;
    private int dx;
    private int amplitudeY;
    private int periodY;
    private double time;
    private final List<Point> points = new ArrayList<>();

    public WaveAnim(int xOffset, int yOffset, int dx, int amplitudeY, int periodY, double time) {
        this.xOffset = this.x = xOffset;
        this.yOffset = this.y = yOffset;
        this.dx = dx;
        this.amplitudeY = amplitudeY;
        this.periodY = periodY;
        this.time = time;
    }

    @Override
    public Point calculatePoint() {
        if (direction == Direction.LEFT) this.x += dx;
        else x -= dx;
        this.y = (int)(yOffset + amplitudeY * Math.sin(2 * Math.PI * time / periodY));
        if (x > Tiles.GAME_WIDTH.getValue()) {
            x = xOffset;
            points.clear();
        }
        time++;
        return new Point(x, y);
    }

    @Override
    public void movementRender(Graphics g, boolean viewMovement) {
        calculatePoint();
        g.drawRect(x, y, 20, 20);
        if (viewMovement) {
            if (!points.contains(new Point(x, y))) points.add(new Point(x, y));
            for (Point point : points) {
                g.drawRect(point.x, point.y, 3, 3);
            }
        }
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDx() {
        return dx;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
