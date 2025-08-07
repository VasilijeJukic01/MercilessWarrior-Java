package platformer.model.gameObjects;

import platformer.animation.SpriteManager;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.model.projectiles.ProjectileFactory;
import platformer.model.spells.SpellManager;
import platformer.storage.StorageStrategy;
import platformer.core.Framework;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.*;
import platformer.model.inventory.item.ShopItem;
import platformer.model.levels.Level;
import platformer.utils.CollectionUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.canLauncherSeeEntity;

/**
 * This class manages all the game objects in the game.
 * It handles the loading, updating, rendering and interactions between game objects and the player.
 */
@SuppressWarnings({"unchecked", "SameParameterValue"})
public class ObjectManager {

    private GameContext context;
    private CollisionHandler collisionHandler;
    private ObjectBreakHandler objectBreakHandler;
    private LootHandler lootHandler;

    private Interactable intersection = null;

    Class<? extends GameObject>[] updateClasses = new Class[]{
            Coin.class, Container.class, Potion.class, Spike.class,
            Shop.class, Blocker.class, Blacksmith.class, Dog.class,
            SaveTotem.class, SmashTrap.class, Candle.class, Loot.class,
            Table.class, Board.class, Npc.class, Lava.class, Brick.class,
            JumpPad.class, Herb.class, RoricTrap.class
    };
    Class<? extends GameObject>[] renderBelow = new Class[] {
            Container.class, Potion.class, Spike.class,
            Blocker.class, Dog.class, SmashTrap.class, Loot.class, Brick.class,
            SaveTotem.class, Shop.class, Blacksmith.class, Coin.class,
            Table.class, Board.class, JumpPad.class, Herb.class, RoricTrap.class
    };
    Class<? extends GameObject>[] renderAbove = new Class[] {
            Shop.class
    };
    Class<? extends GameObject>[] renderBehind = new Class[] {
            Lava.class
    };

    private Map<ObjType, List<GameObject>> objectsMap = new HashMap<>();

    public void wire(GameContext context) {
        this.context = context;
        this.collisionHandler = new CollisionHandler(context.getLevelManager(), this);
        this.lootHandler = new LootHandler(this, context.getEffectManager());
        this.objectBreakHandler = new ObjectBreakHandler(this);
    }

    public void loadObjects(Level level) {
        level.gatherData();
        this.objectsMap = level.getObjectsMap();
        configureObjects();
    }

    private void configureObjects() {
        if (lootHandler == null) return;
        getObjects(Container.class).forEach(container -> lootHandler.generateCrateLoot(container));
        StorageStrategy storageStrategy = Framework.getInstance().getStorageStrategy();
        getObjects(Shop.class).forEach(shop -> {
            List<ShopItem> inventory = storageStrategy.getShopInventory("DEFAULT_SHOP");
            if (inventory != null) {
                shop.getShopItems().clear();
                shop.getShopItems().addAll(inventory);
            }
        });
    }

    // Intersection Handler
    /**
     * Handles the interactions between the player and interactable objects in the game.
     * It checks if the player is intersecting with any interactable object and updates the intersection accordingly.
     *
     * @param player The player object that is interacting with the game world.
     */
    private void handleInteractions(Player player) {
        Interactable newIntersection = null;

        for (GameObject obj : getAllObjects()) {
            if (obj instanceof Interactable && obj.isAlive() && obj.getHitBox().intersects(player.getHitBox())) {
                newIntersection = (Interactable) obj;
                break;
            }
        }

        if (newIntersection != intersection) {
            if (intersection != null) intersection.onExit(player);
            if (newIntersection != null) newIntersection.onEnter(player);
            intersection = newIntersection;
        }
        if (intersection != null) intersection.onIntersect(player);
    }

    /**
     * Handles the collection of collectibles (coins and potions) by the player.
     * It checks if the player is intersecting with any collectible object and collects it.
     *
     * @param player The player object that is collecting items.
     */
    private void handleCollectibles(Player player) {
        Stream.concat(getObjects(Coin.class).stream(), getObjects(Potion.class).stream())
                .filter(object -> object.isAlive() && player.getHitBox().intersects(object.getHitBox()))
                .forEach(object -> lootHandler.collectItem(object, player));
    }

    /**
     * Handles the interaction of a player with a herb item.
     *
     * @param herb The Herb object to be harvested.
     */
    public void harvestHerb(Herb herb) {
        lootHandler.harvestHerb(herb, context.getGameState().getPlayer());
    }

    // Collision Handler
    /**
     * Checks if the player is colliding with a solid object.
     * This method is used to prevent the player from moving through solid objects.
     *
     * @param hitbox The hitbox of the player.
     * @param dx The desired change in the player's X coordinate.
     * @return The new X coordinate after collision resolution.
     */
    public double checkSolidObjectCollision(Rectangle2D.Double hitbox, double dx) {
        return collisionHandler.checkSolidObjectCollision(hitbox, dx);
    }

    /**
     * Checks if the player is touching an object.
     *
     * @param player The player whose position is to be checked.
     * @return true if the player is touching an object, false otherwise.
     */
    public boolean isPlayerTouchingObject(Player player) {
        return (collisionHandler.isTouchingObject(Container.class, player) || collisionHandler.isTouchingObject(Brick.class, player));
    }

    /**
     * Checks if the player is glitched inside an object.
     * This method is used to prevent the player from getting stuck inside game objects.
     *
     * @param player The player whose position is to be checked.
     * @return true if the player is glitched inside an object, false otherwise.
     */
    public boolean isPlayerGlitchedInObject(Player player) {
        return collisionHandler.isGlitchedInObject(Container.class, player);
    }

    /**
     * Gets the X coordinate of the boundary of an object that the player is colliding with.
     * This method is used to prevent the player from moving through game objects.
     *
     * @param hitBox The hitbox of the player.
     * @param dx The desired change in the player's X coordinate.
     * @return The X coordinate of the object's boundary.
     */
    public double getXObjectBound(Rectangle2D.Double hitBox, double dx) {
        return collisionHandler.getXObjectBound(hitBox, dx);
    }

    // Object Break Handler
    /**
     * Checks if an object is broken.
     *
     * @param attackBox The attack box of the player.
     */
    public void checkObjectBreak(Rectangle2D.Double attackBox) {
        SpellManager spellManager = context.getSpellManager();
        objectBreakHandler.checkObjectBreak(attackBox, spellManager.getFlames());
    }

    /**
     * Checks if an object is broken by an enemy attack.
     *
     * @param attackBox The attack box of the enemy.
     */
    public void checkObjectBreakByEnemy(Rectangle2D.Double attackBox) {
        objectBreakHandler.checkObjectBreakByEnemy(attackBox);
    }


    /**
     * Checks if an object is broken by a push action.
     *
     * @param hitBox The hitbox of the player or object that is pushing.
     */
    public void checkObjectBreakByPush(Rectangle2D.Double hitBox) {
        List<Container> containers = getObjects(Container.class);
        for (Container container : containers) {
            if (container.isAlive() && !container.isAnimate() && hitBox.intersects(container.getHitBox()))
                objectBreakHandler.breakContainerOnPush(container);
        }
    }

    /**
     * Generates loot for a defeated enemy.
     *
     * @param e The enemy for which the loot is to be generated.
     */
    public void generateLoot(Enemy e) {
        Rectangle2D.Double location = e.getHitBox();
        lootHandler.generateEnemyLoot(location, e.getEnemyType());
    }

    // Launchers
    private boolean isPlayerInRangeForTrap(ArrowLauncher arrowLauncher, Player player) {
        int distance = (int)Math.abs(player.getHitBox().x - arrowLauncher.getHitBox().x);
        return distance <= TILES_SIZE * 5;
    }

    private boolean isPlayerInFrontOfTrap(ArrowLauncher arrowLauncher, Player player) {
        if (arrowLauncher.getObjType() == ObjType.ARROW_TRAP_LEFT) {
            return arrowLauncher.getHitBox().x > player.getHitBox().x;
        }
        else if (arrowLauncher.getObjType() == ObjType.ARROW_TRAP_RIGHT) {
            return arrowLauncher.getHitBox().x < player.getHitBox().x;
        }
        return false;
    }

    public void activateBlockers(boolean value) {
        for (Blocker blocker : getObjects(Blocker.class)) {
            blocker.setAnimate(value);
            if (!value) blocker.stop();
        }
    }

    public void spawnRoricTrap(int x, int y, Direction direction) {
        addGameObject(new RoricTrap(x, y - (int)(65 * SCALE), direction));
    }

    // Core
    private <T extends GameObject> void updateObjects(Class<T> objectClass, int[][] lvlData) {
        getObjects(objectClass).forEach(obj -> obj.update(lvlData));
    }

    private <T extends GameObject> void renderObjects(Graphics g, int xLevelOffset, int yLevelOffset, Class<T> objectType) {
        try {
            if (objectType.equals(Coin.class)) return;
            getObjects(objectType).stream()
                    .filter(GameObject::isAlive)
                    .forEach(obj -> {
                        BufferedImage[] animations = SpriteManager.getInstance().getObjectAnimations(obj.getObjType());
                        if (animations != null) obj.render(g, xLevelOffset, yLevelOffset, animations);
                    });
        }
        catch (Exception ignored) {}
    }

    public void update(int[][] lvlData, Player player) {
        Arrays.stream(updateClasses).forEach(clazz -> updateObjects(clazz, lvlData));
        updateArrowLaunchers(lvlData, player);
        handleInteractions(player);
        handleCollectibles(player);
        collisionHandler.updateObjectInAir();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Arrays.stream(renderBelow).forEach(renderClass -> renderObjects(g, xLevelOffset, yLevelOffset, renderClass));
        renderArrowLaunchers(g, xLevelOffset, yLevelOffset);
    }

    public void secondRender(Graphics g, int xLevelOffset, int yLevelOffset) {
        Arrays.stream(renderBehind).forEach(renderClass -> renderObjects(g, xLevelOffset, yLevelOffset, renderClass));
        renderCoins(g, xLevelOffset, yLevelOffset);
        renderNpcs(g, xLevelOffset, yLevelOffset);
    }

    public void candleRender(Graphics g, int xLevelOffset, int yLevelOffset, Candle c) {
        BufferedImage[] animations = SpriteManager.getInstance().getObjectAnimations(c.getObjType());
        if (animations != null) c.render(g, xLevelOffset, yLevelOffset, animations);
    }

    public void glowingRender(Graphics g, int xLevelOffset, int yLevelOffset) {
        Arrays.stream(renderAbove).forEach(renderClass -> renderObjects(g, xLevelOffset, yLevelOffset, renderClass));
    }

    private void renderCoins(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Coin coin : getObjects(Coin.class)) {
            if (coin.isAlive()) {
                BufferedImage[][] coinAnimations = SpriteManager.getInstance().getCoinAnimations();
                BufferedImage[] anims = coinAnimations[coin.getCoinType().getAnimationRow()];
                coin.render(g, xLevelOffset, yLevelOffset, anims);
            }
        }
    }

    private void renderNpcs(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Npc npc : getObjects(Npc.class)) {
            if (npc.isAlive()) {
                BufferedImage[] anims = SpriteManager.getInstance().getNpcAnimations(npc.getNpcType());
                npc.render(g, xLevelOffset, yLevelOffset, anims);
            }
        }
    }

    private void updateArrowLaunchers(int[][] lvlData, Player player) {
        for (ArrowLauncher arrowLauncher : getObjects(ArrowLauncher.class)) {
            boolean ready = !arrowLauncher.animate;

            double playerTopTile = player.getHitBox().y / TILES_SIZE;
            double playerBottomTile = (player.getHitBox().y + player.getHitBox().height) / TILES_SIZE;
            int arrowLauncherTile = arrowLauncher.getYTile();

            if (arrowLauncherTile < playerTopTile || arrowLauncherTile > playerBottomTile) ready = false;
            if (!isPlayerInRangeForTrap(arrowLauncher, player) || !isPlayerInFrontOfTrap(arrowLauncher, player)) ready = false;
            if (!canLauncherSeeEntity(lvlData, player.getHitBox(), arrowLauncher.getHitBox(), arrowLauncherTile)) ready = false;

            if (ready) arrowLauncher.setAnimate(true);
            arrowLauncher.update();
            if (arrowLauncher.getAnimIndex() == 9 && arrowLauncher.getAnimTick() == 0) {
                Audio.getInstance().getAudioPlayer().playSound(Sound.ARROW);
                Direction direction = (arrowLauncher.getObjType() == ObjType.ARROW_TRAP_RIGHT) ? Direction.LEFT : Direction.RIGHT;
                ProjectileFactory.createArrow(new Point((int)arrowLauncher.getHitBox().x, (int)arrowLauncher.getHitBox().y), direction);
            }
        }
    }

    private void renderArrowLaunchers(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (ArrowLauncher al : getObjects(ArrowLauncher.class)) {
            if (al.isAlive()) {
                BufferedImage[] anims = SpriteManager.getInstance().getObjectAnimations(al.getObjType());
                al.render(g, xLevelOffset, yLevelOffset, anims);
            }
        }
    }

    // Reset
    public void reset() {
        objectsMap.clear();
        loadObjects(context.getLevelManager().getCurrentLevel());
    }

    public void refreshShopData() {
        StorageStrategy storageStrategy = Framework.getInstance().getStorageStrategy();
        getObjects(Shop.class).forEach(shop -> {
            List<ShopItem> inventory = storageStrategy.getShopInventory("DEFAULT_SHOP");
            if (inventory != null) {
                shop.getShopItems().clear();
                shop.getShopItems().addAll(inventory);
            }
        });
    }

    // Hashmap operations
    private List<GameObject> getAllObjects() {
        return CollectionUtils.getAllItems(objectsMap);
    }

    public <T> List<T> getObjects(Class<T> objectType) {
        return getAllObjects().stream()
                .filter(objectType::isInstance)
                .map(objectType::cast)
                .collect(Collectors.toList());
    }

    public void addGameObject(GameObject gameObject) {
        ObjType type = gameObject.getObjType();
        objectsMap.computeIfAbsent(type, k -> new ArrayList<>()).add(gameObject);
    }

    public String getIntersectingObject() {
        if (intersection != null) {
            return intersection.getInteractionPrompt();
        }
        return null;
    }

    public GameObject getIntersection() {
        return (GameObject) intersection;
    }

    public void setIntersection(Interactable intersection) {
        this.intersection = intersection;
    }

    public ObjectBreakHandler getObjectBreakHandler() {
        return objectBreakHandler;
    }
}
