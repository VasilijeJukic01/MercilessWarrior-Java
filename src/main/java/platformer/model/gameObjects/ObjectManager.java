package platformer.model.gameObjects;

import platformer.animation.Animation;
import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerBonus;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.gameObjects.objects.*;
import platformer.model.gameObjects.objects.Container;
import platformer.model.levels.Level;
import platformer.model.gameObjects.projectiles.*;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

@SuppressWarnings({"unchecked", "SameParameterValue"})
public class ObjectManager {

    private final GameState gameState;
    private CollisionHandler collisionHandler;
    private IntersectionHandler intersectionHandler;
    private ObjectBreakHandler objectBreakHandler;

    private final BufferedImage[][] objects;
    Class<? extends GameObject>[] classes = new Class[]{
            Coin.class,
            platformer.model.gameObjects.objects.Container.class,
            Potion.class,
            Spike.class,
            Shop.class,
            Blocker.class,
            Blacksmith.class,
            Dog.class
    };
    private Map<ObjType, List<GameObject>> objectsMap = new HashMap<>();

    private BufferedImage projectileArrow;
    private BufferedImage[] projectileLightningBall, projectileLightningBall2;
    private final List<Projectile> projectiles;

    private boolean shopVisible, blacksmithVisible;

    public ObjectManager(GameState gameState) {
        this.gameState = gameState;
        initHandlers();
        this.objects = Animation.getInstance().loadObjects();
        this.projectiles = new ArrayList<>();
        loadImages();
    }

    // Init
    private void loadImages() {
        this.projectileArrow = Utils.getInstance().importImage(ARROW_IMG, ARROW_WID, ARROW_HEI);
        this.projectileLightningBall = Animation.getInstance().loadLightningBall(LIGHTNING_BALL_1_SHEET);
        this.projectileLightningBall2 = Animation.getInstance().loadLightningBall(LIGHTNING_BALL_2_SHEET);
    }

    private void initHandlers() {
        this.collisionHandler = new CollisionHandler(gameState.getLevelManager(), this);
        this.intersectionHandler = new IntersectionHandler(gameState.getEnemyManager(), this);
        this.objectBreakHandler = new ObjectBreakHandler(this);
    }

    public void loadObjects(Level level) {
        level.gatherData();
        this.objectsMap = level.getObjectsMap();
        this.projectiles.clear();
    }

    // Intersection Handler
    public void checkPlayerIntersection(Player player) {
        intersectionHandler.checkPlayerIntersection(player);
    }

    public void checkEnemyIntersection() {
        intersectionHandler.checkEnemyIntersection(projectiles);
    }

    public void handleObjectInteraction(Rectangle2D.Double hitBox, Player player) {
        intersectionHandler.handleObjectInteraction(hitBox, player);
    }

    // Collision Handler
    public boolean isPlayerTouchingObject(Player player) {
        return collisionHandler.isTouchingObject(Container.class, player);
    }

    public boolean isPlayerGlitchedInObject(Player player) {
        return collisionHandler.isGlitchedInObject(Container.class, player);
    }

    public double getXObjectBound(Rectangle2D.Double hitBox, double dx) {
        return collisionHandler.getXObjectBound(hitBox, dx);
    }

    // Object Break Handler
    public void checkObjectBreak(Rectangle2D.Double attackBox) {
        objectBreakHandler.checkObjectBreak(attackBox, gameState.getSpellManager().getFlames());
    }

    public void generateCoins(Rectangle2D.Double location) {
        objectBreakHandler.generateCoins(location);
    }

    public void checkProjectileDeflect(Rectangle2D.Double attackBox) {
        if (!PlayerBonus.getInstance().isDeflect()) return;
        projectiles.stream()
                .filter(projectile -> projectile.isAlive() && attackBox.intersects(projectile.getHitBox()))
                .forEach(projectile -> projectile.setAlive(false));
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

    // Projectiles / Activators
    private void shootArrow(ArrowLauncher arrowLauncher) {
        Audio.getInstance().getAudioPlayer().playSound(Sound.ARROW);
        Direction direction = (arrowLauncher.getObjType() == ObjType.ARROW_TRAP_RIGHT) ? Direction.LEFT : Direction.RIGHT;
        projectiles.add(new Arrow((int)arrowLauncher.getHitBox().x, (int)arrowLauncher.getHitBox().y, direction));
    }

    public void shootLightningBall(SpearWoman spearWoman) {
        Direction direction = (spearWoman.getFlipSign() == 1) ? Direction.LEFT : Direction.RIGHT;
        projectiles.add(new LightningBall((int)spearWoman.getHitBox().x, (int)spearWoman.getHitBox().y, direction));
    }

    public void multiLightningBallShoot(SpearWoman spearWoman) {
        double x = spearWoman.getHitBox().x;
        double y = spearWoman.getHitBox().y;
        projectiles.add(new LightningBall((int)(x / 1.1),   (int)(y * 1.3), Direction.DOWN));
        projectiles.add(new LightningBall((int)x,           (int)(y * 1.3), Direction.DEGREE_45));
        projectiles.add(new LightningBall((int)(x * 1.1),   (int)(y * 1.2), Direction.DEGREE_30));
        projectiles.add(new LightningBall((int)(x / 1.23),  (int)(y * 1.3), Direction.N_DEGREE_45));
        projectiles.add(new LightningBall((int)(x / 1.38),  (int)(y * 1.2), Direction.N_DEGREE_30));
    }

    public void multiLightningBallShoot2(SpearWoman spearWoman) {
        double x = spearWoman.getHitBox().x;
        double y = spearWoman.getHitBox().y;
        projectiles.add(new LightningBall((int)x,           (int)(y * 1.3), Direction.DEGREE_60));
        projectiles.add(new LightningBall((int)(x / 1.15),  (int)(y * 1.3), Direction.N_DEGREE_60));
    }

    public void followingLightningBallShoot(SpearWoman spearWoman) {
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.1), (int)(spearWoman.getHitBox().y*1.3), Direction.TRACK));
    }

    public void activateBlockers(boolean value) {
        for (Blocker blocker : getObjects(Blocker.class)) {
            blocker.setAnimate(value);
            if (!value) blocker.stop();
        }
    }

    // Core
    private <T extends GameObject> void updateObjects(Class<T> objectClass) {
        getObjects(objectClass).forEach(GameObject::update);
    }

    private <T extends GameObject> void renderObjects(Graphics g, int xLevelOffset, int yLevelOffset, Class<T> objectType) {
        try {
            getObjects(objectType).stream()
                    .filter(GameObject::isAlive)
                    .forEach(obj -> obj.render(g, xLevelOffset, yLevelOffset, objects[obj.getObjType().ordinal()]));
        }
        catch (Exception ignored) {}
    }

    public void update(int[][] lvlData, Player player) {
        Arrays.stream(classes).forEach(this::updateObjects);
        updateArrowLaunchers(lvlData, player);
        checkEnemyIntersection();
        collisionHandler.updateObjectInAir();
        updateProjectiles(lvlData, player);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Arrays.stream(classes).forEach(renderClass -> renderObjects(g, xLevelOffset, yLevelOffset, renderClass));
        renderArrowLaunchers(g, xLevelOffset, yLevelOffset);
        renderProjectiles(g, xLevelOffset, yLevelOffset);
    }

    private void updateArrowLaunchers(int[][] lvlData, Player player) {
        for (ArrowLauncher arrowLauncher : getObjects(ArrowLauncher.class)) {
            boolean ready = !arrowLauncher.animate;

            double playerTopTile = player.getHitBox().y / TILES_SIZE;
            double playerBottomTile = (player.getHitBox().y + player.getHitBox().height) / TILES_SIZE;
            int arrowLauncherTile = arrowLauncher.getYTile();

            if (arrowLauncherTile < playerTopTile || arrowLauncherTile > playerBottomTile) ready = false;
            if (!isPlayerInRangeForTrap(arrowLauncher, player) || !isPlayerInFrontOfTrap(arrowLauncher, player)) ready = false;
            if (!Utils.getInstance().canLauncherSeePlayer(lvlData, player.getHitBox(), arrowLauncher.getHitBox(), arrowLauncherTile)) ready = false;

            if (ready) arrowLauncher.setAnimate(true);
            arrowLauncher.update();
            if (arrowLauncher.getAnimIndex() == 9 && arrowLauncher.getAnimTick() == 0) {
                shootArrow(arrowLauncher);
            }
        }
    }

    private void updateProjectiles(int[][] lvlData, Player player) {
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) {
                projectile.updatePosition(player);
                if (projectile.getHitBox().intersects(player.getHitBox())) {
                    player.changeHealth(-PLAYER_PROJECTILE_DMG, projectile);
                    projectile.setAlive(false);
                }
                else if (Utils.getInstance().isProjectileHitLevel(lvlData, projectile)) {
                    projectile.setAlive(false);
                }
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

    private void renderProjectiles(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Projectile p : projectiles) {
            if (!p.isAlive()) continue;
            // Arrow
            if (p instanceof Arrow) {
                p.render(g, xLevelOffset, yLevelOffset, projectileArrow);
            }
            // Lightning Ball
            else {
                if (p.getDirection() == Direction.LEFT || p.getDirection() == Direction.RIGHT)
                    p.render(g, xLevelOffset, yLevelOffset, projectileLightningBall);
                else p.render(g, xLevelOffset, yLevelOffset, projectileLightningBall2);
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

    public boolean isShopVisible() {
        return shopVisible;
    }

    public boolean isBlacksmithVisible() {
        return blacksmithVisible;
    }

    public void setShopVisible(boolean shopVisible) {
        this.shopVisible = shopVisible;
    }

    public void setBlacksmithVisible(boolean blacksmithVisible) {
        this.blacksmithVisible = blacksmithVisible;
    }
}
