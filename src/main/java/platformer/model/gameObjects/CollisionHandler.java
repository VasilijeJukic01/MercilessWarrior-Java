package platformer.model.gameObjects;

import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.gameObjects.objects.Blacksmith;
import platformer.model.gameObjects.objects.Brick;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.Loot;
import platformer.model.levels.LevelManager;
import platformer.utils.Utils;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Stream;

public class CollisionHandler {

    private final LevelManager levelManager;
    private final ObjectManager objectManager;

    public CollisionHandler(LevelManager levelManager, ObjectManager objectManager) {
        this.levelManager = levelManager;
        this.objectManager = objectManager;
    }

    public <T extends GameObject> boolean isTouchingObject(Class<T> objectClass, Entity entity) {
        if (entity == null) return false;
        return getObjects(objectClass).stream()
                .anyMatch(o -> o.isAlive() && o.getAnimIndex() < 1 && o.getHitBox().intersects(entity.getHitBox()));
    }

    public <T extends GameObject> boolean isGlitchedInObject(Class<T> objectClass, Entity entity) {
        if (entity == null) return false;
        return getObjects(objectClass).stream()
                .anyMatch(o -> o.isAlive() && o.getAnimIndex() < 1 && checkTouch(o, entity.getHitBox(), "Y", 2));
    }

    public double getXObjectBound(Rectangle2D.Double hitBox, double dx) {
        return Stream.concat(
                        getObjects(Container.class).stream(),
                        getObjects(Brick.class).stream()
                )
                .filter(o -> o.isAlive() && (checkTouch(o, hitBox, "X", 0) || checkTouch(o, hitBox, "Y", 0)))
                .mapToDouble(o -> checkTouch(o, hitBox, "X", 0) ? hittingObjectBySide(o, hitBox, dx) : standingOnObject(hitBox, dx))
                .findFirst().orElse(hitBox.x);
    }

    private boolean checkTouch(GameObject o, Rectangle2D.Double hitBox, String axis, double offset) {
        int x = (int)hitBox.x, y = (int)hitBox.y;
        int width = (int)hitBox.width, height = (int)hitBox.height;

        if (o.isAlive() && o.getAnimIndex() < 1) {
            if (axis.equals("X")) {
                if (o.getHitBox().contains(x, y)) return true;
                if (o.getHitBox().contains(x+width, y)) return true;
                if (o.getHitBox().contains(x, y+height-5)) return true;
                return o.getHitBox().contains(x+width, y+height-5);
            }
            else if (axis.equals("Y")) {
                if (o.getHitBox().contains(x+offset, y+height + 3)) return true;
                return o.getHitBox().contains(x+width-offset, y+height + 3);
            }
        }

        return false;
    }

    private <T extends GameObject> GameObject getCollidingObject(Direction direction, Rectangle2D.Double hitBox, Class<T> objectClass) {
        for (T o : getObjects(objectClass)) {
            if (o.isAlive() && checkTouch(o, hitBox, "X", 0)) {
                if (o.getHitBox().x < hitBox.x && direction == Direction.LEFT) return o;
                else if(o.getHitBox().x > hitBox.x && direction == Direction.RIGHT) return o;
            }
        }
        return null;
    }

    private double hittingObjectBySide(GameObject o, Rectangle2D.Double hitBox, double dx) {
        if (o.getHitBox().x < hitBox.x && dx > 0) {
            if (canMove(hitBox.x+1, hitBox.y, hitBox.width, hitBox.height)) return hitBox.x+1;
        }
        else if(o.getHitBox().x > hitBox.x && dx < 0) {
            if (canMove(hitBox.x-1, hitBox.y, hitBox.width, hitBox.height)) return hitBox.x-1;
        }
        return hitBox.x;
    }

    private double standingOnObject(Rectangle2D.Double hitBox, double dx) {
        GameObject leftContainer = getCollidingObject(Direction.LEFT, hitBox, Container.class);
        GameObject rightContainer = getCollidingObject(Direction.RIGHT, hitBox, Container.class);

        GameObject leftBrick = getCollidingObject(Direction.LEFT, hitBox, Brick.class);
        GameObject rightBrick = getCollidingObject(Direction.RIGHT, hitBox, Brick.class);

        GameObject left = leftContainer != null ? leftContainer : leftBrick;
        GameObject right = rightContainer != null ? rightContainer : rightBrick;

        if (dx < 0 && left == null) {
            if (canMove(hitBox.x-1, hitBox.y, hitBox.width, hitBox.height)) return hitBox.x-1;
        }
        else if(dx > 0 && right == null) {
            if (canMove(hitBox.x+1, hitBox.y, hitBox.width, hitBox.height)) return hitBox.x+1;
        }
        else if (dx < 0 && left.isAlive()) {
            double x = left.getHitBox().x+left.getHitBox().width;
            if (canMove(x, hitBox.y, hitBox.width, hitBox.height)) return x;
        }
        else if (dx > 0 && right.isAlive()) {
            double x = right.getHitBox().x-hitBox.width;
            if (canMove(x, hitBox.y, hitBox.width, hitBox.height)) return x;
        }
        return hitBox.x;
    }

    // In Air Check
    private <T extends GameObject> boolean isObjectInAir(T object, Class<T> objectClass) {
        for (T obj : getObjects(objectClass)) {
            if (obj.isAlive() && obj instanceof Container && obj != object) {
                if (obj.getHitBox().intersects(object.getHitBox())) return false;
            }
        }
        double xPos = object.getHitBox().x;
        double yPos = object.getHitBox().y + 1;
        return canMove(xPos, yPos, object.getHitBox().width, object.getHitBox().height);
    }

    private <T extends GameObject> void landObject(T gameObject, Class<T> objectClass) {
        boolean isSafe = true;
        while(isSafe) {
            isSafe = isObjectInAir(gameObject, objectClass);
            if (isSafe) {
                double xPos = gameObject.getHitBox().x;
                double yPos = gameObject.getHitBox().y + 1;
                if (canMove(xPos, yPos, gameObject.getHitBox().width, gameObject.getHitBox().height)) {
                    gameObject.getHitBox().y += 1;
                }
            }
            else {
                gameObject.getHitBox().y += 2;
                gameObject.setOnGround(true);
            }
        }
    }

    private <T extends GameObject> void updateObjectInAir(Class<T> objectClass) {
        for (T object : getObjects(objectClass)) {
            if (isObjectInAir(object, objectClass)) object.setOnGround(false);
            if (!object.isOnGround) landObject(object, objectClass);
        }
    }

    public void updateObjectInAir() {
        updateObjectInAir(Container.class);
        updateObjectInAir(Blacksmith.class);
        updateObjectInAir(Loot.class);
    }

    // Helper
    private boolean canMove(double x, double y, double w, double h) {
        return Utils.getInstance().canMoveHere(x, y, w, h, levelManager.getCurrentLevel().getLvlData());
    }

    private <T> List<T> getObjects(Class<T> objectType) {
        return objectManager.getObjects(objectType);
    }

}
