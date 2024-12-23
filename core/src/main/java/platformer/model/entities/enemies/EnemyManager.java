package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.animation.Animation;
import platformer.audio.Audio;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.entities.enemies.renderer.*;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.Spike;
import platformer.model.gameObjects.projectiles.Fireball;
import platformer.model.gameObjects.projectiles.Projectile;
import platformer.model.inventory.InventoryBonus;
import platformer.model.levels.Level;
import platformer.model.perks.PerksBonus;
import platformer.model.quests.QuestManager;
import platformer.model.spells.Flame;
import platformer.observer.Publisher;
import platformer.observer.Subscriber;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;

/**
 * Manages all the enemies in the game.
 */
@SuppressWarnings("unchecked")
public class EnemyManager implements Publisher {

    private final GameState gameState;

    private BufferedImage[][] skeletonAnimations, ghoulAnimations, knightAnimations, wraithAnimations, spearWomanAnimations;

    private Map<EnemyType, List<Enemy>> enemies = new HashMap<>();
    private final Map<Class<? extends Enemy>, EnemyRenderer<? extends Enemy>> enemyRenderers = new HashMap<>();

    private final List<Subscriber> subscribers = new ArrayList<>();

    public EnemyManager(GameState gameState) {
        this.gameState = gameState;
        init();
    }

    // Init
    private void init() {
        initAnimations();
        initRenderers();
    }

    private void initAnimations() {
        this.skeletonAnimations = Animation.getInstance().loadSkeletonAnimations(SKELETON_WIDTH, SKELETON_HEIGHT);
        this.ghoulAnimations = Animation.getInstance().loadGhoulAnimation(GHOUL_WIDTH, GHOUL_HEIGHT);
        this.knightAnimations = Animation.getInstance().loadKnightAnimation(KNIGHT_WIDTH, KNIGHT_HEIGHT);
        this.spearWomanAnimations = Animation.getInstance().loadSpearWomanAnimations(SW_WIDTH, SW_HEIGHT);
        this.wraithAnimations = Animation.getInstance().loadWraithAnimation(WRAITH_WIDTH, WRAITH_HEIGHT);
    }

    private void initRenderers() {
        this.enemyRenderers.put(Skeleton.class, new SkeletonRenderer(skeletonAnimations));
        this.enemyRenderers.put(Ghoul.class, new GhoulRenderer(ghoulAnimations));
        this.enemyRenderers.put(SpearWoman.class, new SpearWomanRenderer(spearWomanAnimations));
        this.enemyRenderers.put(Knight.class, new KnightRenderer(knightAnimations));
        this.enemyRenderers.put(Wraith.class, new WraithRenderer(wraithAnimations));
    }

    public void loadEnemies(Level level) {
        this.enemies = level.getEnemiesMap();
        reset();
    }

    /**
     * Renders the critical hit indicator for an enemy.
     *
     * @param g The graphics object to draw on.
     * @param xLevelOffset The x offset for rendering.
     * @param yLevelOffset The y offset for rendering.
     * @param e The enemy to render the critical hit indicator for.
     */
    private void renderCriticalHit(Graphics g, int xLevelOffset, int yLevelOffset, Enemy e) {
        if (e.isCriticalHit()) {
            int xCritical = (int)(e.getHitBox().x - xLevelOffset);
            int yCritical = (int)(e.getHitBox().y - 2*SCALE - yLevelOffset);
            g.setColor(Color.RED);
            g.drawString("CRITICAL", xCritical, yCritical);
        }
    }

    // Render
    /**
     * Renders all enemies of a specific type in the game.
     *
     * @param <T> The specific type of enemy to render. This type must extend from the Enemy class.
     * @param enemyClass The Class object representing the type of enemy to render.
     * @param g The graphics object to draw on.
     * @param xLevelOffset The x offset for rendering.
     * @param yLevelOffset The y offset for rendering.
     */
    private <T extends Enemy> void renderEnemies(Class<T> enemyClass, Graphics g, int xLevelOffset, int yLevelOffset) {
        for (T enemy : getEnemies(enemyClass)) {
            if (enemy.isAlive()) {
                EnemyRenderer<T> renderer = (EnemyRenderer<T>) enemyRenderers.get(enemyClass);
                renderer.render(g, enemy, xLevelOffset, yLevelOffset);
                renderCriticalHit(g, xLevelOffset, yLevelOffset, enemy);
            }
        }
    }

    private void renderSkeletons(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEnemies(Skeleton.class, g, xLevelOffset, yLevelOffset);
    }

    private void renderGhouls(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEnemies(Ghoul.class, g, xLevelOffset, yLevelOffset);
    }

    private void renderSpearWoman(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEnemies(SpearWoman.class, g, xLevelOffset, yLevelOffset);
    }

    private void renderKnights(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEnemies(Knight.class, g, xLevelOffset, yLevelOffset);
    }

    private void renderWraiths(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEnemies(Wraith.class, g, xLevelOffset, yLevelOffset);
    }

    // Enemy hit
    /**
     * Checks if an enemy is dying and handles the event of an enemy's death.
     *
     * @param e The enemy to check.
     * @param player The Player object representing the player in the game.
     */
    private void checkEnemyDying(Enemy e, Player player) {
        Random rand = new Random();
        if (e.getEnemyAction() == Anim.DEATH) {
            gameState.getObjectManager().generateLoot(e);
            player.changeStamina(rand.nextInt(5));
            player.changeExp(rand.nextInt(50)+100);
            checkForEvent(e);
        }
    }

    private void checkForEvent(Enemy e) {
        if (e.getEnemyType() == EnemyType.SKELETON) notify("Kill Skeletons");
        else if (e.getEnemyType() == EnemyType.GHOUL) notify("Kill Ghouls");
    }

    private double[] damage(Player player) {
        double critical = 0;
        double dmg = player.getAttackDmg() + PerksBonus.getInstance().getBonusAttack();
        double equipmentBonus = InventoryBonus.getInstance().getAttack() * dmg;
        dmg += equipmentBonus;

        Random rand = new Random();
        double criticalHit = rand.nextInt(100 - PerksBonus.getInstance().getCriticalHitChance());
        if (criticalHit >= 1 && criticalHit <= 10) {
            dmg *= 2;
            critical = 1;
        }
        return new double[] {dmg, critical};
    }

    /**
     * Handles the event of an enemy being hit by the player's attack.
     *
     * @param <T> The specific type of enemy that was hit. This type must extend from the Enemy class.
     * @param attackBox The hitbox of the player's attack.
     * @param player The Player object representing the player in the game.
     * @param enemyClass The Class object representing the type of enemy that was hit.
     */
    private <T extends Enemy> void handleEnemyHit(Rectangle2D.Double attackBox, Player player, Class<T> enemyClass) {
        double[] dmg = damage(player);
        for (T enemy : getEnemies(enemyClass)) {
            if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH) {
                if (attackBox.intersects(enemy.getHitBox())) {
                    if (enemy.getEnemyAction() == Anim.HIDE || enemy.getEnemyAction() == Anim.REVEAL) return;
                    enemy.hit(dmg[0], true, true);
                    enemy.setCriticalHit(dmg[1] == 1);
                    player.changeStamina(new Random().nextInt(3) + 1);
                    checkEnemyDying(enemy, player);
                    writeHitLog(enemy.getEnemyAction(), dmg[0]);
                    player.addAction(PlayerAction.DASH_HIT);
                    return;
                }
            }
        }
    }

    public void checkEnemyHit(Rectangle2D.Double attackBox, Player player) {
        handleEnemyHit(attackBox, player, Skeleton.class);
        handleEnemyHit(attackBox, player, Ghoul.class);
        handleEnemyHit(attackBox, player, Knight.class);
        handleEnemyHit(attackBox, player, Wraith.class);
        handleEnemyHit(attackBox, player, SpearWoman.class);
        boolean dash = player.checkAction(PlayerAction.DASH);
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);
        if (!dash && !onWall) Audio.getInstance().getAudioPlayer().playSlashSound();
    }

    private void writeHitLog(Anim anim, double dmg) {
        if (anim == Anim.BLOCK) Logger.getInstance().notify("Enemy blocks player's attack.", Message.NOTIFICATION);
        else Logger.getInstance().notify("Player gives damage to enemy: "+dmg, Message.NOTIFICATION);
    }

    public <T extends Enemy> void handleEnemySpellHit(Class<T> enemyClass, double dmg) {
        Flame flame = gameState.getSpellManager().getFlames();
        for (T enemy : getEnemies(enemyClass)) {
            if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH) {
                if (flame.isActive() && flame.getHitBox().intersects(enemy.getHitBox())) {
                    enemy.spellHit(dmg);
                    checkEnemyDying(enemy, gameState.getPlayer());
                    return;
                }
            }
        }
    }

    public void checkEnemySpellHit() {
        handleEnemySpellHit(Skeleton.class, 0.08);
        handleEnemySpellHit(Ghoul.class, 0.16);
        handleEnemySpellHit(Knight.class, 0.08);
        handleEnemySpellHit(Wraith.class, 0.16);
    }

    public void checkEnemyTrapHit(GameObject object) {
        if (!(object instanceof Spike)) return;
        for (List<Enemy> enemies : enemies.values()) {
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH && object.getHitBox().intersects(enemy.getHitBox())) {
                    enemy.hit(500, false, false);
                }
            }
        }
    }

    public void checkEnemyProjectileHit(Projectile projectile) {
        for (Skeleton skeleton : getEnemies(Skeleton.class)) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != Anim.DEATH && projectile.getHitBox().intersects(skeleton.getHitBox())) {
                skeleton.hit(ENEMY_PROJECTILE_DMG, false, false);
                Direction projectileDirection = projectile.getDirection();
                skeleton.setPushDirection(projectileDirection == Direction.LEFT ? Direction.RIGHT : Direction.LEFT);
                projectile.setAlive(false);
            }
        }
        for (SpearWoman spearWoman : getEnemies(SpearWoman.class)) {
            if (projectile instanceof Fireball && spearWoman.isAlive() && spearWoman.getEnemyAction() != Anim.DEATH) {
                if (!projectile.getHitBox().intersects(spearWoman.getHitBox())) continue;
                spearWoman.hit(FIREBALL_PROJECTILE_DMG, false, false);
                projectile.setAlive(false);
            }
        }
    }

    // Core
    /**
     * Updates the state of all enemies of a specific type in the game.
     * This includes updating their animations, checking for collisions, and handling their behavior.
     *
     * @param <T> The specific type of enemy to update. This type must extend from the Enemy class.
     * @param enemyType The Class object representing the type of enemy to update.
     * @param animations The 2D array of BufferedImages representing the animations for the enemy type.
     * @param levelData The 2D array representing the current level's layout.
     * @param player The Player object representing the player in the game.
     */
    private <T extends Enemy> void updateEnemies(Class<T> enemyType, BufferedImage[][] animations, int[][] levelData, Player player) {
        getEnemies(enemyType).stream()
                .filter(Enemy::isAlive)
                .forEach(enemy -> enemy.update(animations, levelData, player));
    }

    public void update(int[][] levelData, Player player) {
        updateEnemies(Skeleton.class, skeletonAnimations, levelData, player);
        updateEnemies(Ghoul.class, ghoulAnimations, levelData, player);
        updateEnemies(Knight.class, knightAnimations, levelData, player);
        updateEnemies(Wraith.class, wraithAnimations, levelData, player);

        getEnemies(SpearWoman.class).stream()
                .filter(SpearWoman::isAlive)
                .forEach(spearWoman -> spearWoman.update(spearWomanAnimations, levelData, player, gameState.getSpellManager(), gameState.getObjectManager(), gameState.getBossInterface()));
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            renderSkeletons(g, xLevelOffset, yLevelOffset);
            renderGhouls(g, xLevelOffset, yLevelOffset);
            renderKnights(g, xLevelOffset, yLevelOffset);
            renderWraiths(g, xLevelOffset, yLevelOffset);
            renderSpearWoman(g, xLevelOffset, yLevelOffset);
        }
        catch (Exception ignored) {}
    }

    // Reset
    public void reset() {
        enemies.values().stream()
                .flatMap(List::stream)
                .forEach(Enemy::reset);
    }

    public List<Enemy> getAllEnemies() {
        return Utils.getInstance().getAllItems(enemies);
    }

    private <T> List<T> getEnemies(Class<T> enemyType) {
        return getAllEnemies().stream()
                .filter(enemyType::isInstance)
                .map(enemyType::cast)
                .collect(Collectors.toList());
    }

    // Emit Events
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
                .ifPresent(s -> s.update(o[0]));
    }
}
