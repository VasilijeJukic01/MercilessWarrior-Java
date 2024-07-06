package platformer.model.gameObjects;

import platformer.audio.Audio;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.gameObjects.objects.*;
import platformer.model.gameObjects.projectiles.Projectile;
import platformer.model.perks.PerksBonus;
import platformer.model.spells.Flame;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

/**
 * Class that handles breaking of objects.
 */
public class ObjectBreakHandler {

    private final ObjectManager objectManager;

    public ObjectBreakHandler(ObjectManager objectManager) {
        this.objectManager = objectManager;
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
            if (attackBox.intersects(container.getHitBox()) || isFlame) breakContainer(container);
        }
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
                    if (projectile.getHitBox().intersects(container.getHitBox())) breakContainer(container);
                }
            }
        }

        for (Brick brick : getObjects(Brick.class)) {
            if (!brick.isAlive() || brick.animate) continue;

            for (Projectile projectile : projectiles) {
                if (projectile.isAlive()) {
                    if (projectile.getHitBox().intersects(brick.getHitBox())) breakBrick(brick);
                }
            }
        }
    }

    private void breakContainer(Container container) {
        container.setAnimate(true);
        Audio.getInstance().getAudioPlayer().playCrateSound();
        Logger.getInstance().notify("Player breaks container.", Message.NOTIFICATION);
        generateCrateLoot(container);
    }

    private void breakBrick(Brick brick) {
        brick.setAnimate(true);
        Logger.getInstance().notify("Player breaks brick.", Message.NOTIFICATION);
    }

    /**
     * Generates loot from a broken container.
     *
     * @param container The broken container.
     */
    private void generateCrateLoot(Container container) {
        Random rand = new Random();
        int value = rand.nextInt(4)-1;
        ObjType obj = null;
        if (value == 0) obj = ObjType.STAMINA_POTION;
        else if (value == 1) obj = ObjType.HEAL_POTION;
        if (obj != null) {
            int xPos = (int)(container.getHitBox().x + container.getHitBox().width / 2);
            int yPos = (int)(container.getHitBox().y - container.getHitBox().height / 4);
            objectManager.addGameObject(new Potion(obj, xPos, yPos));
        }
    }

    /**
     * Generates loot from a defeated enemy.
     *
     * @param location The location of the defeated enemy.
     * @param enemyType The type of the defeated enemy.
     */
    public void generateEnemyLoot(Rectangle2D.Double location, EnemyType enemyType) {
       generateCoins(location);
       generateLoot(location, enemyType);
    }

    private void generateCoins(Rectangle2D.Double location) {
        Random rand = new Random();
        int n = rand.nextInt(7 + PerksBonus.getInstance().getBonusCoin());
        for (int i = 0; i < n; i++) {
            int x = rand.nextInt((int)location.width)+(int)location.x;
            int y = rand.nextInt((int)(location.height/3)) + (int)location.y + 2*(int)location.height/3;
            Coin coin = new Coin(ObjType.COIN, x, y);
            objectManager.addGameObject(coin);
        }
    }

    private void generateLoot(Rectangle2D.Double location, EnemyType enemyType) {
        int x = (int)(location.width / 4) + (int)location.x;
        int y = (int)(location.height / 2.3) + (int)location.y;
        Loot loot = new Loot(ObjType.LOOT, x, y, enemyType);
        objectManager.addGameObject(loot);
    }

    private <T> List<T> getObjects(Class<T> objectType) {
        return objectManager.getObjects(objectType);
    }

}
