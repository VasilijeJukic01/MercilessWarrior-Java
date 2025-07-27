package platformer.animation.graphics;

import platformer.model.entities.Direction;

import java.awt.*;

import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.SCALE;

public class WaveAnim implements GraphicsAnimation<Point> {

    private Direction direction;
    private double x, y;                   // Current point
    private double xOffset, yOffset;       // Starting point
    private double dx;
    private int amplitudeY;
    private int periodY;
    private double time;

    public WaveAnim(int xOffset, int yOffset, double dx, int amplitudeY, int periodY, double time) {
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
        if (x > GAME_WIDTH) {
            x = xOffset;
        }
        time += 0.5 * SCALE;
        return new Point((int) x, (int) y);
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDx() {
        return dx;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
