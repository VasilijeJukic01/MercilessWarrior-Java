package platformer.model.gameObjects;

import platformer.animation.Animation;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.*;
import platformer.model.levels.Level;
import platformer.model.quests.QuestManager;
import platformer.observer.Publisher;
import platformer.observer.Subscriber;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.canLauncherSeeEntity;

/**
 * This class manages all the game objects in the game.
 * It handles the loading, updating, rendering and interactions between game objects and the player.
 */
@SuppressWarnings({"unchecked", "SameParameterValue"})
public class ObjectManager implements Publisher {

    private final GameState gameState;
    private CollisionHandler collisionHandler;
    private IntersectionHandler intersectionHandler;
    private ObjectBreakHandler objectBreakHandler;
    private LootHandler lootHandler;

    private GameObject intersection;

    private final BufferedImage[][] objects;
    private final BufferedImage[][] npcs;
    private final BufferedImage[][] coinAnimations;

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

    private final List<Subscriber> subscribers = new ArrayList<>();

    public ObjectManager(GameState gameState) {
        this.gameState = gameState;
        this.objects = Animation.getInstance().loadObjects();
        this.coinAnimations = Animation.getInstance().getCoinAnimations();
        this.npcs = Animation.getInstance().loadNpcs();
    }

    // Init
    public void lateInit() {
        this.collisionHandler = new CollisionHandler(gameState.getLevelManager(), this);
        this.lootHandler = new LootHandler(this, gameState.getEffectManager());
        this.intersectionHandler = new IntersectionHandler(gameState.getEnemyManager(), this, lootHandler);
        this.objectBreakHandler = new ObjectBreakHandler(this, lootHandler);
    }

    public void loadObjects(Level level) {
        level.gatherData();
        this.objectsMap = level.getObjectsMap();
        configureObjects();
        embedSubscribers();
    }

    private void configureObjects() {
        getObjects(Container.class).forEach(container -> lootHandler.generateCrateLoot(container));
    }

    // Intersection Handler
    /**
     * Checks if the player intersects with any object.
     *
     * @param player The player whose intersection is to be checked.
     */
    public void checkPlayerIntersection(Player player) {
        intersectionHandler.checkPlayerIntersection(player);
    }

    public void checkEnemyIntersection() {
        intersectionHandler.checkEnemyIntersection();
    }

    /**
     * Handles the interaction of a player with an object.
     *
     * @param hitBox The hitbox of the player.
     * @param player The player whose interaction is to be handled.
     */
    public void handleObjectInteraction(Rectangle2D.Double hitBox, Player player) {
        intersectionHandler.handleObjectInteraction(hitBox, player);
    }

    /**
     * Handles the interaction of a player with a herb item.
     *
     * @param herb The Herb object to be harvested.
     */
    public void harvestHerb(Herb herb) {
        lootHandler.harvestHerb(herb, gameState.getPlayer());
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
        objectBreakHandler.checkObjectBreak(attackBox, gameState.getSpellManager().getFlames());
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
                    .forEach(obj -> obj.render(g, xLevelOffset, yLevelOffset, objects[obj.getObjType().ordinal()]));
        }
        catch (Exception ignored) {}
    }

    public void update(int[][] lvlData, Player player) {
        Arrays.stream(updateClasses).forEach(clazz -> updateObjects(clazz, lvlData));
        updateArrowLaunchers(lvlData, player);
        checkEnemyIntersection();
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
        c.render(g, xLevelOffset, yLevelOffset, objects[c.getObjType().ordinal()]);
    }

    public void glowingRender(Graphics g, int xLevelOffset, int yLevelOffset) {
        Arrays.stream(renderAbove).forEach(renderClass -> renderObjects(g, xLevelOffset, yLevelOffset, renderClass));
    }

    private void renderCoins(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Coin coin : getObjects(Coin.class)) {
            if (coin.isAlive()) {
                BufferedImage[] anims = coinAnimations[coin.getCoinType().getAnimationRow()];
                coin.render(g, xLevelOffset, yLevelOffset, anims);
            }
        }
    }

    private void renderNpcs(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Npc npc : getObjects(Npc.class)) {
            if (npc.isAlive()) {
                BufferedImage[] anims = npcs[npc.getNpcType().ordinal()];
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
                gameState.getProjectileManager().activateArrow(new Point((int)arrowLauncher.getHitBox().x, (int)arrowLauncher.getHitBox().y), direction);
            }
        }
    }

    private void renderArrowLaunchers(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (ArrowLauncher al : getObjects(ArrowLauncher.class)) {
            if (al.isAlive()) {
                BufferedImage[] anims = al.getObjType() == ObjType.ARROW_TRAP_RIGHT ? objects[al.getObjType().ordinal()-1] : objects[al.getObjType().ordinal()];
                al.render(g, xLevelOffset, yLevelOffset, anims);
            }
        }
    }

    // Reset
    public void reset() {
        objectsMap.clear();
        loadObjects(gameState.getLevelManager().getCurrentLevel());
    }

    // Hashmap operations
    private List<GameObject> getAllObjects() {
        return Utils.getInstance().getAllItems(objectsMap);
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

    public void removeGameObject(GameObject gameObject) {
        ObjType type = gameObject.getObjType();
        List<GameObject> objectsOfType = objectsMap.get(type);
        if (objectsOfType != null) {
            objectsOfType.remove(gameObject);
        }
    }

    public String getIntersectingObject() {
        return intersectionHandler.getIntersectingObject();
    }

    public void setIntersection(GameObject object) {
        this.intersection = object;
    }

    public GameObject getIntersection() {
        return intersection;
    }

    public ObjectBreakHandler getObjectBreakHandler() {
        return objectBreakHandler;
    }

    // Observer
    private void embedSubscribers() {
        getObjects(Shop.class).forEach(shop -> shop.addSubscriber(gameState.getQuestManager()));
    }

    @Override
    public void addSubscriber(Subscriber s) {
        this.subscribers.add(s);
    }

    @Override
    public void removeSubscriber(Subscriber s) {
        this.subscribers.remove(s);
    }

    @Override
    public <T> void notify(T... o) {
        subscribers.stream()
                .filter(s -> s instanceof QuestManager)
                .findFirst()
                .ifPresent(s -> s.update(o));
    }
}
