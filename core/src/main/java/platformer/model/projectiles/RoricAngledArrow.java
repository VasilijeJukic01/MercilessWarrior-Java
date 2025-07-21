package platformer.model.projectiles;

import platformer.debug.DebugSettings;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

public class RoricAngledArrow extends Projectile {

    private final Direction creatorDirection;

    private final double angle;
    private Polygon polygonHitbox;

    private final boolean spawnsTrapOnImpact;

    public RoricAngledArrow(int xPos, int yPos, double angle, boolean spawnsTrap, Direction creatorDirection) {
        super(PRType.RORIC_ANGLED_ARROW, null);
        this.angle = angle;
        this.spawnsTrapOnImpact = spawnsTrap;
        this.creatorDirection = creatorDirection;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int[] xPoints = { 0, ARROW_WID, ARROW_WID, 0 };
        int[] yPoints = { 0, 0, ARROW_HEI, ARROW_HEI };
        Polygon initialPolygon = new Polygon(xPoints, yPoints, 4);

        AffineTransform at = new AffineTransform();
        at.translate(xPos, yPos);
        at.rotate(angle, ARROW_WID / 2.0, ARROW_HEI / 2.0);

        Shape rotatedShape = at.createTransformedShape(initialPolygon);
        PathIterator pi = rotatedShape.getPathIterator(null);
        List<Point2D> points = new ArrayList<>();
        double[] coords = new double[6];
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                points.add(new Point2D.Double(coords[0], coords[1]));
            }
            pi.next();
        }

        this.polygonHitbox = new Polygon();
        for (Point2D p : points) {
            this.polygonHitbox.addPoint((int) p.getX(), (int) p.getY());
        }
    }

    @Override
    public void updatePosition(Player player, ObjectManager objectManager, int[][] levelData) {
        if (!alive) return;
        int dx = (int)(Math.cos(angle) * ANGLED_ARROW_SPEED);
        int dy = (int)(Math.sin(angle) * ANGLED_ARROW_SPEED);
        polygonHitbox.translate(dx, dy);

        Rectangle2D bounds = polygonHitbox.getBounds2D();
        Rectangle2D.Double checkHitbox = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        if (isEntityOnFloor(checkHitbox, levelData)) {
            this.alive = false;
            if (spawnsTrapOnImpact)
                objectManager.spawnRoricTrap((int) polygonHitbox.getBounds2D().getCenterX(), (int) (polygonHitbox.getBounds2D().getMaxY()), creatorDirection);
        }
    }

    @Override
    public void updatePosition(Player player) {
//        int dx = (int)(Math.cos(angle) * ANGLED_ARROW_SPEED);
//        int dy = (int)(Math.sin(angle) * ANGLED_ARROW_SPEED);
//        polygonHitbox.translate(dx, dy);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, Object animations) {
        if (!(animations instanceof BufferedImage)) return;
        Graphics2D g2d = (Graphics2D) g.create();

        int drawX = polygonHitbox.xpoints[0] - xLevelOffset;
        int drawY = polygonHitbox.ypoints[0] - yLevelOffset;
        AffineTransform at = new AffineTransform();
        at.translate(drawX, drawY);
        at.rotate(angle);
        g2d.setTransform(at);
        g2d.drawImage((BufferedImage) animations, 0, -ARROW_HEI / 2, ARROW_WID, ARROW_HEI, null);
        g2d.dispose();
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    @Override
    public void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        Polygon translatedPolygon = new Polygon(polygonHitbox.xpoints, polygonHitbox.ypoints, polygonHitbox.npoints);
        translatedPolygon.translate(-xLevelOffset, -yLevelOffset);
        g.drawPolygon(translatedPolygon);
    }

    @Override
    public Shape getShapeBounds() {
        return polygonHitbox;
    }
}