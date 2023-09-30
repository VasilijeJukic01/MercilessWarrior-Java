package platformer.model.gameObjects;

import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.objects.*;
import platformer.model.gameObjects.projectiles.Projectile;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.HEAL_POTION_VAL;
import static platformer.constants.Constants.STAMINA_POTION_VAL;

@SuppressWarnings({"unchecked"})
public class IntersectionHandler {

    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;

    private Class<? extends GameObject> intersectingObject;

    private final Class<? extends GameObject>[] classesToCheck = new Class[]{
            Shop.class, Blacksmith.class, SaveTotem.class, Loot.class, Spike.class, Blocker.class, SmashTrap.class
    };

    public IntersectionHandler(EnemyManager enemyManager, ObjectManager objectManager) {
        this.enemyManager = enemyManager;
        this.objectManager = objectManager;
    }

    // Checker
    private boolean isInstantDeath(GameObject object) {
        if (object instanceof Spike) return true;
        if (object instanceof Blocker && object.getAnimIndex() > 2) return true;
        if (object instanceof SmashTrap && object.getAnimIndex() > 0 && object.getAnimIndex() < 6) return true;
        return false;
    }

    private <T extends GameObject> boolean checkPlayerIntersection(Player p, Class<T> objectClass) {
        boolean check = false;
        for (T object : getObjects(objectClass)) {
            boolean intersect = p.getHitBox().intersects(object.getHitBox());
            if (intersect) {
                check = true;
            }
            if (intersect && isInstantDeath(object)) {
                p.kill();
            }
            else if (object instanceof Shop) {
                ((Shop) object).setActive(intersect);
            }
            else if (object instanceof Blacksmith) {
                ((Blacksmith) object).setActive(intersect);
            }
            else if (object instanceof SaveTotem) {
                ((SaveTotem) object).setActive(intersect);
            }
            else if (object instanceof Loot) {
                ((Loot) object).setActive(intersect);
            }
        }
        return check;
    }

    public void checkPlayerIntersection(Player player) {
        for (Class<? extends GameObject> c : classesToCheck) {
            if (checkPlayerIntersection(player, c)) {
                intersectingObject = c;
                return;
            }
        }
        intersectingObject = null;
    }

    public void checkEnemyIntersection(List<Projectile> projectiles) {
        getObjects(Spike.class).forEach(enemyManager::checkEnemyTrapHit);

        projectiles.stream()
                .filter(Projectile::isAlive)
                .forEach(enemyManager::checkEnemyProjectileHit);
    }

    // Handle
    private <T extends GameObject> void handleObjectInteraction(Rectangle2D.Double hitBox, Class<T> objectType, Player player) {
        ArrayList<T> objects = new ArrayList<>(getObjects(objectType));
        for (T object : objects) {
            if (object.isAlive() && hitBox.intersects(object.getHitBox())) {
                object.setAlive(false);
                if (object instanceof Potion) {
                    applyPotionEffect((Potion) object, player);
                }
                else if (object instanceof Coin) {
                    objectManager.removeGameObject(object);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.COIN_PICK);
                    player.changeCoins(1);
                }
            }
        }
    }

    public void handleObjectInteraction(Rectangle2D.Double hitBox, Player player) {
        handleObjectInteraction(hitBox, Potion.class, player);
        handleObjectInteraction(hitBox, Coin.class, player);
    }

    // Apply
    public void applyPotionEffect(Potion potion, Player player) {
        if (potion == null) return;
        switch (potion.getObjType()) {
            case HEAL_POTION:
                player.changeHealth(HEAL_POTION_VAL); break;
            case STAMINA_POTION:
                player.changeStamina(STAMINA_POTION_VAL); break;
        }
    }

    private <T> List<T> getObjects(Class<T> objectType) {
        return objectManager.getObjects(objectType);
    }

    public Class<? extends GameObject> getIntersectingObject() {
        return intersectingObject;
    }

    public Loot getIntersectingLoot(Player player) {
        Rectangle2D.Double hitBox = player.getHitBox();
        for (Loot loot : getObjects(Loot.class)) {
            if (loot.isAlive() && hitBox.intersects(loot.getHitBox())) {
                return loot;
            }
        }
        return null;
    }

}
