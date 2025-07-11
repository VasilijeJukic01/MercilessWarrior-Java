package platformer.model.gameObjects;

import platformer.model.entities.Direction;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.model.gameObjects.objects.*;
import platformer.model.gameObjects.projectiles.Projectile;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Stream;


/**
 * Handles intersections between game objects and entities.
 */
@SuppressWarnings({"unchecked"})
public class IntersectionHandler {

    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;
    private final LootHandler lootHandler;

    private final Class<? extends GameObject>[] classesToCheck = new Class[]{
            Shop.class, Blacksmith.class, SaveTotem.class, Loot.class, Spike.class,
            Blocker.class, SmashTrap.class, Table.class, Board.class, Dog.class, Npc.class,
            Lava.class, JumpPad.class, Herb.class, Container.class
    };

    public IntersectionHandler(EnemyManager enemyManager, ObjectManager objectManager, LootHandler lootHandler) {
        this.enemyManager = enemyManager;
        this.objectManager = objectManager;
        this.lootHandler = lootHandler;
    }

    // Checker
    /**
     * Checks if a GameObject causes instant death upon intersection.
     *
     * @param object The GameObject to check.
     * @return true if the GameObject causes instant death, false otherwise.
     */
    private boolean isInstantDeath(GameObject object) {
        if (object instanceof Spike) return true;
        if (object instanceof Blocker && object.getAnimIndex() > 2) return true;
        if (object instanceof SmashTrap && object.getAnimIndex() > 0 && object.getAnimIndex() < 6) return true;
        return false;
    }

    /**
     * Checks if the player intersects with a specific type of GameObject.
     *
     * @param p The player to check.
     * @param objectClass The class of the GameObject to check.
     * @return true if the player intersects with the GameObject, false otherwise.
     */
    private <T extends GameObject> boolean checkPlayerIntersection(Player p, Class<T> objectClass) {
        boolean check = false;
        for (T object : getObjects(objectClass)) {
            boolean intersect = p.getHitBox().intersects(object.getHitBox());
            if (intersect) {
                check = true;
                objectManager.setIntersection(object);
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
            else if (object instanceof Table) {
                ((Table) object).setActive(intersect);
            }
            else if (object instanceof Board) {
                ((Board) object).setActive(intersect);
            }
            else if (object instanceof JumpPad) {
                if (intersect) ((JumpPad) object).launchPlayer(p);
            }
            else if (object instanceof Dog) {
                ((Dog) object).setActive(intersect);
            }
            else if (object instanceof Npc) {
               if (p.getHitBox().x + p.getHitBox().width < object.getHitBox().x + object.getHitBox().width / 1.2) {
                   ((Npc) object).setDirection(Direction.RIGHT);
               }
               else {
                   ((Npc) object).setDirection(Direction.LEFT);
               }
            }
            else if (object instanceof Lava) {
                handleLavaIntersection(p);
            }
            else if (object instanceof Herb) {
                ((Herb) object).setActive(intersect);
            }
        }
        return check;
    }

    /**
     * Checks if the player intersects with any GameObject.
     *
     * @param player The player to check.
     */
    public void checkPlayerIntersection(Player player) {
        for (Class<? extends GameObject> c : classesToCheck) {
            if (checkPlayerIntersection(player, c)) {
                return;
            }
        }
        objectManager.setIntersection(null);
    }

    /**
     * Checks if any enemy intersects with a projectile.
     *
     * @param projectiles The list of projectiles to check.
     */
    public void checkEnemyIntersection(List<Projectile> projectiles) {
        getObjects(Spike.class).forEach(enemyManager::checkEnemyTrapHit);

        projectiles.stream()
                .filter(Projectile::isAlive)
                .forEach(enemyManager::checkEnemyProjectileHit);
    }

    // Handle
    /**
     * Handles the interaction between a player and a GameObject.
     *
     * @param hitBox The hitbox of the player.
     * @param player The player to handle the interaction for.
     */
    public void handleObjectInteraction(Rectangle2D.Double hitBox, Player player) {
        Stream.concat(getObjects(Potion.class).stream(), getObjects(Coin.class).stream())
                .filter(object -> object.isAlive() && hitBox.intersects(object.getHitBox()))
                .forEach(object -> lootHandler.collectItem(object, player));
    }

    private <T> List<T> getObjects(Class<T> objectType) {
        return objectManager.getObjects(objectType);
    }

    public String getIntersectingObject() {
        GameObject object = objectManager.getIntersection();
        if (object == null) return null;
        if (object instanceof Shop) return "Shop";
        if (object instanceof Blacksmith) return "Blacksmith";
        if (object instanceof SaveTotem) return "SaveTotem";
        if (object instanceof Loot) return "Loot";
        if (object instanceof Table) return "Table";
        if (object instanceof Board) return "Board";
        if (object instanceof Herb) return "Herb";
        if (object instanceof Dog) return "Dog";
        if (object instanceof Container) return "Container";
        if (object instanceof Npc && ((Npc)object).getNpcType() == NpcType.ANITA) return "NpcAnita";
        if (object instanceof Npc && ((Npc)object).getNpcType() == NpcType.NIKOLAS) return "NpcNikolas";
        if (object instanceof Npc && ((Npc)object).getNpcType() == NpcType.SIR_DEJANOVIC) return "NpcSirDejanovic";
        if (object instanceof Npc && ((Npc)object).getNpcType() == NpcType.KRYSANTHE) return "NpcKrysanthe";
        return null;
    }

    public Loot getIntersectingLoot(Player player) {
        Rectangle2D.Double hitBox = player.getHitBox();
        return getObjects(Loot.class).stream()
                .filter(loot -> loot.isAlive() && hitBox.intersects(loot.getHitBox()))
                .findFirst()
                .orElse(null);
    }

    // Helper
    private void handleLavaIntersection(Player player) {
        getObjects(Lava.class).stream()
                .filter(Lava::isAlive)
                .filter(lava -> player.getHitBox().intersects(lava.getHitBox()))
                .findFirst()
                .ifPresentOrElse(lava -> player.addAction(PlayerAction.LAVA), () -> player.removeAction(PlayerAction.LAVA));
    }

}
