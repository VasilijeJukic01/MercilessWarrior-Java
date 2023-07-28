package platformer.animation.graphics;

import platformer.model.entities.Direction;

import java.awt.*;

public class CircularAnim implements GraphicsAnimation {

    private Direction direction;
    private double angle; // Radians
    private double angularSpeed;
    private double radius;
    private int p, q; // Center

    public CircularAnim(double angle, double angularSpeed, double radius, int p, int q) {
        this.angle = angle;
        this.angularSpeed = angularSpeed;
        this.radius = radius;
        this.p = p;
        this.q = q;
    }

    @Override
    public Point calculatePoint() {
        double theta = (direction == Direction.LEFT) ? angle : -angle;
        int x1 = (int)(p+radius*Math.cos(theta));
        int y1 = (int)(q+radius*Math.sin(theta));
        angle += angularSpeed;
        return new Point(x1, y1);
    }

    @Override
    public void movementRender(Graphics g, boolean viewMovement) {
        Point curve = calculatePoint();

        if (viewMovement) {
            g.setColor(Color.GREEN);
            g.drawLine(p, q, curve.x, curve.y);
            int diameter = (int)(2 * radius);
            g.setColor(Color.BLUE);
            g.drawOval((int)(p - radius), (int)(q - radius), diameter, diameter);
        }
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setAngularSpeed(double angularSpeed) {
        this.angularSpeed = angularSpeed;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setCenter(Point p) {
        this.p = p.x;
        this.q = p.y;
    }
}
