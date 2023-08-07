package platformer.model.objects;

import platformer.animation.Animation;
import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerBonus;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.levels.Level;
import platformer.model.objects.projectiles.*;
import platformer.model.spells.Flame;
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

@SuppressWarnings("unchecked")
public class ObjectManager {

    private final GameState gameState;

    private final BufferedImage[][] objects;
    Class<? extends GameObject>[] classes = new Class[]{
            Coin.class,
            Container.class,
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

    public void loadObjects(Level level) {
        level.gatherData();
        this.objectsMap = level.getObjectsMap();
        this.projectiles.clear();
    }

    // Intersections
    public <T extends GameObject> void checkPlayerIntersection(Player p, Class<T> objectClass) {
        for (T object : getObjects(objectClass)) {
            boolean intersect = p.getHitBox().intersects(object.getHitBox());
            if (intersect && object instanceof Spike || (object instanceof Blocker && object.getAnimIndex() > 2)) {
                p.kill();
            }
            else if (object instanceof Shop) {
                ((Shop) object).setActive(intersect);
                shopVisible = intersect;
            }
            else if (object instanceof Blacksmith) {
                ((Blacksmith) object).setActive(intersect);
                blacksmithVisible = intersect;
            }
        }
    }

    public void checkPlayerIntersection(Player player) {
        checkPlayerIntersection(player, Spike.class);
        checkPlayerIntersection(player, Blocker.class);
        checkPlayerIntersection(player, Shop.class);
        checkPlayerIntersection(player, Blacksmith.class);
    }

    public void checkEnemyIntersection() {
        for (Spike spike : getObjects(Spike.class)) {
            gameState.getEnemyManager().checkEnemyTrapHit(spike);
        }
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) gameState.getEnemyManager().checkEnemyProjectileHit(projectile);
        }
    }

    public <T extends GameObject> void handleObjectInteraction(Rectangle2D.Double hitBox, Class<T> objectType) {
        ArrayList<T> objects = new ArrayList<>(getObjects(objectType));
        for (T object : objects) {
            if (object.isAlive() && hitBox.intersects(object.getHitBox())) {
                object.setAlive(false);
                if (object instanceof Potion) {
                    applyPotionEffect((Potion) object);
                }
                else if (object instanceof Coin) {
                    removeGameObject(object);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.COIN_PICK);
                    gameState.getPlayer().changeCoins(1);
                }
            }
        }
    }

    public void handleObjectInteraction(Rectangle2D.Double hitBox) {
        handleObjectInteraction(hitBox, Potion.class);
        handleObjectInteraction(hitBox, Coin.class);
    }

    // Apply Effects
    public void applyPotionEffect(Potion potion) {
        if (potion == null) return;
        switch (potion.getObjType()) {
            case HEAL_POTION:
                gameState.getPlayer().changeHealth(HEAL_POTION_VAL); break;
            case STAMINA_POTION:
                gameState.getPlayer().changeStamina(STAMINA_POTION_VAL); break;
        }
    }

    // Object Break
    public void checkObjectBreak(Rectangle2D.Double attackBox) {
        Flame flame = gameState.getSpellManager().getFlames();
        for (Container container : getObjects(Container.class)) {
            boolean isFlame = flame.getHitBox().intersects(container.getHitBox()) && flame.isActive();
            if (container.isAlive() && !container.animate && (attackBox.intersects(container.getHitBox()) || isFlame)) {
                container.setAnimate(true);
                Audio.getInstance().getAudioPlayer().playCrateSound();
                Logger.getInstance().notify("Player breaks container.", Message.NOTIFICATION);
                generateLoot(container);
            }
        }
    }

    private void generateLoot(Container container) {
        Random rand = new Random();
        int value = rand.nextInt(4)-1;
        ObjType obj = null;
        if (value == 0) obj = ObjType.STAMINA_POTION;
        else if (value == 1) obj = ObjType.HEAL_POTION;
        if (obj != null) {
            int xPos = (int)(container.getHitBox().x + container.getHitBox().width / 2);
            int yPos = (int)(container.getHitBox().y - container.getHitBox().height / 4);
            addGameObject(new Potion(obj, xPos, yPos));
        }
    }

    public void generateCoins(Rectangle2D.Double location) {
        Random rand = new Random();
        int n = rand.nextInt(7+PlayerBonus.getInstance().getBonusCoin());
        for (int i = 0; i < n; i++) {
            int x = rand.nextInt((int)location.width)+(int)location.x;
            int y = rand.nextInt((int)(location.height/3)) + (int)location.y + 2*(int)location.height/3;
            Coin coin = new Coin(ObjType.COIN, x, y);
            addGameObject(coin);
        }
    }

    public void checkProjectileDeflect(Rectangle2D.Double attackBox) {
        if (!PlayerBonus.getInstance().isDeflect()) return;
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive() && attackBox.intersects(projectile.getHitBox())) {
                projectile.setAlive(false);
            }
        }
    }

    // Object Touch & Bounds
    private <T extends GameObject> boolean isPlayerTouchingObject(Class<T> objectClass) {
        for (T o : getObjects(objectClass)) {
            if (o.isAlive() && o.getAnimIndex() < 1) {
                if (o.getHitBox().intersects(gameState.getPlayer().getHitBox())) return true;
            }
        }
        return false;
    }

    private <T extends GameObject> boolean isPlayerGlitchedInObject(Class<T> objectClass) {
        for (T o : getObjects(objectClass)) {
            if (o.isAlive() && o.getAnimIndex() < 1) {
                if (checkTouch(o, gameState.getPlayer().getHitBox(), "Y", 2)) return true;
            }
        }
        return false;
    }

    public boolean isPlayerTouchingObject() {
        return isPlayerTouchingObject(Container.class);
    }

    public boolean isPlayerGlitchedInObject() {
        return isPlayerGlitchedInObject(Container.class);
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

    public double getXObjectBound(Rectangle2D.Double hitBox, double dx) {
        for (Container o : getObjects(Container.class)) {
            if (!o.isAlive()) continue;

            if (checkTouch(o, hitBox, "X", 0)) {
                return playerHittingObjectBySide(o, hitBox, dx);
            }
            if (checkTouch(o, hitBox, "Y", 0)) {
                return playerStandingOnObject(hitBox, dx);
            }

        }
        return hitBox.x;
    }

    private double playerHittingObjectBySide(GameObject o, Rectangle2D.Double hitBox, double dx) {
        if (o.getHitBox().x < hitBox.x && dx > 0) {
            if (canMove(hitBox.x+1, hitBox.y, hitBox.width, hitBox.height)) return hitBox.x+1;
        }
        else if(o.getHitBox().x > hitBox.x && dx < 0) {
            if (canMove(hitBox.x-1, hitBox.y, hitBox.width, hitBox.height)) return hitBox.x-1;
        }
        return hitBox.x;
    }

    private double playerStandingOnObject(Rectangle2D.Double hitBox, double dx) {
        GameObject left = getCollidingObject(Direction.LEFT, hitBox, Container.class);
        GameObject right = getCollidingObject(Direction.RIGHT, hitBox, Container.class);

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

    // Physics Checks
    private boolean canMove(double x, double y, double w, double h) {
        return Utils.getInstance().canMoveHere(x, y, w, h, gameState.getLevelManager().getCurrentLevel().getLvlData());
    }

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

    private void updateObjectInAir() {
        updateObjectInAir(Container.class);
        updateObjectInAir(Blacksmith.class);
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
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.1), (int)(spearWoman.getHitBox().y*1.3), Direction.DOWN));
        projectiles.add(new LightningBall((int)spearWoman.getHitBox().x, (int)(spearWoman.getHitBox().y*1.3), Direction.DEGREE_45));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x*1.1), (int)(spearWoman.getHitBox().y*1.2), Direction.DEGREE_30));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.23), (int)(spearWoman.getHitBox().y*1.3), Direction.N_DEGREE_45));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.38), (int)(spearWoman.getHitBox().y*1.2), Direction.N_DEGREE_30));
    }

    public void multiLightningBallShoot2(SpearWoman spearWoman) {
        projectiles.add(new LightningBall((int)spearWoman.getHitBox().x, (int)(spearWoman.getHitBox().y*1.3), Direction.DEGREE_60));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.15), (int)(spearWoman.getHitBox().y*1.3), Direction.N_DEGREE_60));
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

    // Updates
    private void updateArrowLaunchers(int[][] lvlData, Player player) {
        for (ArrowLauncher arrowLauncher : getObjects(ArrowLauncher.class)) {
            boolean flag = true;
            if (arrowLauncher.animate) flag = false;
            if ((arrowLauncher.getYTile() < player.getHitBox().y/TILES_SIZE) ||
                    (arrowLauncher.getYTile() > (player.getHitBox().y+player.getHitBox().height)/TILES_SIZE)) {
                flag = false;
            }
            if (!isPlayerInRangeForTrap(arrowLauncher, player) || !isPlayerInFrontOfTrap(arrowLauncher, player)) flag = false;
            if (!Utils.getInstance().canLauncherSeePlayer(lvlData, player.getHitBox(), arrowLauncher.getHitBox(), arrowLauncher.getYTile())) flag = false;
            if (flag) arrowLauncher.setAnimate(true);
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

    private <T extends GameObject> void updateObjects(Class<T> objectClass) {
        for (T object : getObjects(objectClass)) {
            object.update();
        }
    }

    private <T extends GameObject> void renderObjects(Graphics g, int xLevelOffset, int yLevelOffset, Class<T> objectType) {
        for (T obj : getObjects(objectType)) {
            if (obj.isAlive()) {
                obj.render(g, xLevelOffset, yLevelOffset, objects[obj.getObjType().ordinal()]);
            }
        }
    }

    // Core
    public void update(int[][] lvlData, Player player) {
        for (Class<? extends GameObject> renderClass : classes) {
            updateObjects(renderClass);
        }
        updateArrowLaunchers(lvlData, player);
        checkEnemyIntersection();
        updateObjectInAir();
        updateProjectiles(lvlData, player);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Class<? extends GameObject> renderClass : classes) {
            renderObjects(g, xLevelOffset, yLevelOffset, renderClass);
        }
        renderArrowLaunchers(g, xLevelOffset, yLevelOffset);
        renderProjectiles(g, xLevelOffset, yLevelOffset);
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
            int fS = 1, fC = 0;
            // Arrow
            if (p instanceof Arrow) {
                if (p.getDirection() == Direction.LEFT) {
                    fS = -1;
                    fC = ARROW_TRAP_WID;
                }
                int x = (int)p.getHitBox().x - xLevelOffset + fC;
                int y = (int)p.getHitBox().y - yLevelOffset;
                g.drawImage(projectileArrow, x, y, fS * ARROW_WID, ARROW_HEI, null);
            }
            // Lightning Ball
            else {
                BufferedImage object = projectileLightningBall2[p.getAnimIndex()];
                if (p.getDirection() == Direction.LEFT || p.getDirection() == Direction.RIGHT)
                    object = projectileLightningBall[p.getAnimIndex()];
                if (p.getDirection() == Direction.RIGHT) {
                    fS = -1;
                    fC = LB_WID;
                }

                int x = (int)(p.getHitBox().x - xLevelOffset + fC - 22*SCALE);
                int y = (int)(p.getHitBox().y - yLevelOffset - 20*SCALE);
                g.drawImage(object, x, y, fS * LB_WID, LB_HEI, null);
            }
            p.renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
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

    private void addGameObject(GameObject gameObject) {
        ObjType type = gameObject.getObjType();
        objectsMap.computeIfAbsent(type, k -> new ArrayList<>()).add(gameObject);
    }

    private void removeGameObject(GameObject gameObject) {
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
}
