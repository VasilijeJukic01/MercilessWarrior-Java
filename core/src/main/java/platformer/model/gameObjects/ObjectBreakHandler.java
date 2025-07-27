package platformer.model.gameObjects;

import platformer.audio.Audio;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.gameObjects.objects.*;
import platformer.model.projectiles.Projectile;
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestObjectiveType;
import platformer.model.spells.Flame;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Class that handles breaking of objects.
 */
public class ObjectBreakHandler {

    private final ObjectManager objectManager;
    private final LootHandler lootHandler;

    public ObjectBreakHandler(ObjectManager objectManager, LootHandler lootHandler) {
        this.objectManager = objectManager;
        this.lootHandler = lootHandler;
    }

    /**
     * Checks if an object should be broken by an attack or flame.
     *
     * @param attackBox The attack box.
     * @param flame The flame.
     */
    public void checkObjectBreak(Rectangle2D.Double attackBox, Flame flame) {
        for (Container container : getObjects(Container.class)) {
            if (!container.isAlive() || container.animate) continue;

            boolean isFlame = flame.getHitBox().intersects(container.getHitBox()) && flame.isActive();
            if (attackBox.intersects(container.getHitBox()) || isFlame) {
                breakContainer(container);
            }
        }

        for (RoricTrap trap : getObjects(RoricTrap.class)) {
            if (trap.isAlive() && !trap.isAnimate() && attackBox.intersects(trap.getHitBox())) {
                trap.hit(10);
            }
        }
    }

    /**
     * Checks if an object should be broken by an enemy attack.
     *
     * @param attackBox The attack box of the enemy.
     */
    public void checkObjectBreakByEnemy(Rectangle2D.Double attackBox) {
        for (Container container : getObjects(Container.class)) {
            if (!container.isAlive() || container.animate) continue;

            if (attackBox.intersects(container.getHitBox())) {
                breakContainerByEnemy(container);
            }
        }
    }

    /**
     * Breaks a container when the player is pushed into it.
     *
     * @param container The container that was pushed into.
     */
    public void breakContainerOnPush(Container container) {
        container.setAnimate(true);
        Audio.getInstance().getAudioPlayer().playCrateSound();
        Logger.getInstance().notify("Player was pushed into a container, breaking it.", Message.INFORMATION);
    }

    /**
     * Checks if a projectile should break an object.
     *
     * @param projectiles The list of projectiles.
     */
    public void checkProjectileBreak(List<Projectile> projectiles) {
        for (Container container : getObjects(Container.class)) {
            if (!container.isAlive() || container.animate) continue;

            for (Projectile projectile : projectiles) {
                if (projectile.isAlive()) {
                    if (projectile.getShapeBounds().intersects(container.getHitBox())) breakContainer(container);
                }
            }
        }

        for (Brick brick : getObjects(Brick.class)) {
            if (!brick.isAlive() || brick.animate) continue;

            for (Projectile projectile : projectiles) {
                if (projectile.isAlive()) {
                    if (projectile.getShapeBounds().intersects(brick.getHitBox())) breakBrick(brick);
                }
            }
        }
    }

    private void breakContainer(Container container) {
        container.setAnimate(true);
        Audio.getInstance().getAudioPlayer().playCrateSound();
        Logger.getInstance().notify("Player breaks container.", Message.NOTIFICATION);
        objectManager.notify(QuestObjectiveType.COLLECT, ObjectiveTarget.CRATE);
    }

    private void breakContainerByEnemy(Container container) {
        container.setAnimate(true);
        Audio.getInstance().getAudioPlayer().playCrateSound();
        Logger.getInstance().notify("Enemy breaks container.", Message.NOTIFICATION);
    }


    private void breakBrick(Brick brick) {
        brick.setAnimate(true);
        Logger.getInstance().notify("Player breaks brick.", Message.NOTIFICATION);
    }

    private <T> List<T> getObjects(Class<T> objectType) {
        return objectManager.getObjects(objectType);
    }

}
