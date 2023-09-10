package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.animation.Animation;
import platformer.audio.Audio;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.renderer.*;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerBonus;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.levels.Level;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.projectiles.Projectile;
import platformer.model.gameObjects.objects.Spike;
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

@SuppressWarnings("unchecked")
public class EnemyManager {

    private final GameState gameState;

    BufferedImage[][] skeletonAnimations, ghoulAnimations, knightAnimations, wraithAnimations, spearWomanAnimations;

    private Map<EnemyType, List<Enemy>> enemies = new HashMap<>();
    private final Map<Class<? extends Enemy>, EnemyRenderer<? extends Enemy>> enemyRenderers = new HashMap<>();


    public EnemyManager(GameState gameState) {
        this.gameState = gameState;
        init();
    }

    // Init
    private void init() {
        this.skeletonAnimations = Animation.getInstance().loadSkeletonAnimations(SKELETON_WIDTH, SKELETON_HEIGHT);
        this.ghoulAnimations = Animation.getInstance().loadGhoulAnimation(GHOUL_WIDTH, GHOUL_HEIGHT);
        this.knightAnimations = Animation.getInstance().loadKnightAnimation(KNIGHT_WIDTH, KNIGHT_HEIGHT);
        this.spearWomanAnimations = Animation.getInstance().loadSpearWomanAnimations(SW_WIDTH, SW_HEIGHT);
        this.wraithAnimations = Animation.getInstance().loadWraithAnimation(WRAITH_WIDTH, WRAITH_HEIGHT);

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

    private void renderCriticalHit(Graphics g, int xLevelOffset, int yLevelOffset, Enemy e) {
        if (e.isCriticalHit()) {
            int xCritical = (int)(e.getHitBox().x - xLevelOffset);
            int yCritical = (int)(e.getHitBox().y - 2*SCALE - yLevelOffset);
            g.setColor(Color.RED);
            g.drawString("CRITICAL", xCritical, yCritical);
        }
    }

    // Render
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
    private void checkEnemyDying(Enemy e, Player player) {
        Random rand = new Random();
        if (e.getEnemyAction() == Anim.DEATH) {
            gameState.getObjectManager().generateCoins(e.getHitBox());
            player.changeStamina(rand.nextInt(5));
            player.changeExp(rand.nextInt(50)+100);
        }
    }

    private int[] damage(Player player) {
        int critical = 0;
        int dmg = player.getAttackDmg();
        dmg += PlayerBonus.getInstance().getBonusAttack();
        Random rand = new Random();
        int criticalHit = rand.nextInt(100-PlayerBonus.getInstance().getCriticalHitChance());
        if (criticalHit >= 1 && criticalHit <= 10) {
            dmg *= 2;
            critical = 1;
        }
        return new int[] {dmg, critical};
    }

    private <T extends Enemy> void handleEnemyHit(Rectangle2D.Double attackBox, Player player, Class<T> enemyClass) {
        int[] dmg = damage(player);
        for (T enemy : getEnemies(enemyClass)) {
            if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH) {
                if (attackBox.intersects(enemy.getHitBox())) {
                    if (enemy.getEnemyAction() == Anim.HIDE || enemy.getEnemyAction() == Anim.REVEAL) return;
                    enemy.hit(dmg[0], true, true);
                    enemy.setCriticalHit(dmg[1] == 1);
                    checkEnemyDying(enemy, player);
                    writeHitLog(enemy.getEnemyAction(), dmg[0]);
                    player.setDashHit(true);
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
        if (!player.isDash() && !player.isOnWall()) Audio.getInstance().getAudioPlayer().playSlashSound();
    }

    private void writeHitLog(Anim anim, int dmg) {
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
    }

    // Core
    public void update(int[][] levelData, Player player) {
        getEnemies(Skeleton.class).stream()
                .filter(Skeleton::isAlive)
                .forEach(skeleton -> skeleton.update(skeletonAnimations, levelData, player));

        getEnemies(Ghoul.class).stream()
                .filter(Ghoul::isAlive)
                .forEach(ghoul -> ghoul.update(ghoulAnimations, levelData, player));

        getEnemies(Knight.class).stream()
                .filter(Knight::isAlive)
                .forEach(knight -> knight.update(knightAnimations, levelData, player));

        getEnemies(Wraith.class).stream()
                .filter(Wraith::isAlive)
                .forEach(wraith -> wraith.update(wraithAnimations, levelData, player));

        getEnemies(SpearWoman.class).stream()
                .filter(SpearWoman::isAlive)
                .forEach(spearWoman -> spearWoman.update(spearWomanAnimations, levelData, player, gameState.getSpellManager(), gameState.getObjectManager()));
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

    private List<Enemy> getAllEnemies() {
        return Utils.getInstance().getAllItems(enemies);
    }

    private <T> List<T> getEnemies(Class<T> enemyType) {
        return getAllEnemies().stream()
                .filter(enemyType::isInstance)
                .map(enemyType::cast)
                .collect(Collectors.toList());
    }

}
