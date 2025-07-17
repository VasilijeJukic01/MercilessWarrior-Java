package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.animation.Animation;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.Direction;
import platformer.model.entities.effects.particles.DustType;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.RoricClone;
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
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestManager;
import platformer.model.quests.QuestObjectiveType;
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

    private BufferedImage[][] skeletonAnimations, ghoulAnimations, knightAnimations, wraithAnimations, spearWomanAnimations, roricAnimations;

    private Map<EnemyType, List<Enemy>> enemies = new HashMap<>();
    private final Map<Class<? extends Enemy>, EnemyRenderer<? extends Enemy>> enemyRenderers = new HashMap<>();
    private final Map<Enemy, Integer> spellHitTimers = new HashMap<>();
    private final List<RoricClone> roricClones = new ArrayList<>();

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
        this.roricAnimations = Animation.getInstance().loadRoricAnimations(RORIC_WIDTH, RORIC_HEIGHT);
    }

    private void initRenderers() {
        this.enemyRenderers.put(Skeleton.class, new SkeletonRenderer(skeletonAnimations));
        this.enemyRenderers.put(Ghoul.class, new GhoulRenderer(ghoulAnimations));
        this.enemyRenderers.put(SpearWoman.class, new SpearWomanRenderer(spearWomanAnimations));
        this.enemyRenderers.put(Knight.class, new KnightRenderer(knightAnimations));
        this.enemyRenderers.put(Wraith.class, new WraithRenderer(wraithAnimations));
        this.enemyRenderers.put(Roric.class, new RoricRenderer(roricAnimations));
    }

    public void loadEnemies(Level level) {
        this.enemies = level.getEnemiesMap();
        reset();
        getEnemies(SpearWoman.class).forEach(spearWoman -> spearWoman.addSubscriber(gameState));
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

    private void renderRoric(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEnemies(Roric.class, g, xLevelOffset, yLevelOffset);
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
            gameState.getTutorialManager().activateBlockTutorial();
            player.changeStamina(rand.nextInt(5));
            player.changeExp(rand.nextInt(50)+100);
            checkForEvent(e);
        }
    }

    private void checkForEvent(Enemy e) {
        switch (e.getEnemyType()) {
            case SKELETON:
                notify(QuestObjectiveType.KILL, ObjectiveTarget.SKELETON);
                break;
            case GHOUL:
                notify(QuestObjectiveType.KILL, ObjectiveTarget.GHOUL);
                break;
            case SPEAR_WOMAN:
                notify(QuestObjectiveType.KILL, ObjectiveTarget.LANCER);
                break;
            default: break;
        }
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
     * Handles hitting enemies within a specified attack box.
     * This method implements a "cleave" mechanic, allowing a single swing to hit multiple enemies.
     * To maintain game balance, damage falloff is applied:
     * The first enemy hit takes full damage, and each subsequent enemy hit in the same swing takes progressively less damage.
     * <p>
     * The process is as follows:
     * 1. Gathers all living enemies that intersect with the player's attack box.
     * 2. Sorts the intersected enemies by their distance from the player to ensure a consistent and fair application of damage falloff.
     * 3. Iterates through list, applying damage with a falloff multiplier (100%, 75%, 56%, etc.).
     *
     * @param attackBox player's attack hitbox.
     * @param player    entity who is performing the attack.
     */
    public boolean checkEnemyHit(Rectangle2D.Double attackBox, Player player) {
        boolean contactMade = false;

        List<Enemy> intersectingEnemies = new ArrayList<>();
        for (Enemy enemy : getAllEnemies()) {
            if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH && attackBox.intersects(enemy.getHitBox())) {
                if (enemy.getEnemyAction() == Anim.HIDE || enemy.getEnemyAction() == Anim.REVEAL) continue;
                intersectingEnemies.add(enemy);
            }
        }
        if (intersectingEnemies.isEmpty()) return false;
        // TODO: Will disable attacking roric later
        if (intersectingEnemies.stream().anyMatch(e -> e instanceof Roric)) return false;

        intersectingEnemies.sort(Comparator.comparingDouble(e -> e.getHitBox().getCenterX() - player.getHitBox().getCenterX()));

        double damageModifier = 1.0;
        for (Enemy enemy : intersectingEnemies) {
            double[] dmg = damage(player);
            double finalDamage = dmg[0] * damageModifier;
            boolean isCritical = (dmg[1] == 1);

            Rectangle2D intersection = attackBox.createIntersection(enemy.getHitBox());
            spawnParticles((Rectangle2D.Double) intersection, player, enemy, isCritical);

            if (enemy.hit(finalDamage, true, false)) {
                contactMade = true;
                if (enemy.getEnemyAction() != Anim.BLOCK) {
                    double displayDmg = Math.round(finalDamage * 10.0) / 10.0;
                    String dmgText = String.valueOf(displayDmg);
                    Color dmgColor = isCritical ? CRITICAL_COLOR : DAMAGE_COLOR;
                    gameState.getEffectManager().spawnDamageNumber(dmgText, enemy.getHitBox().getCenterX(), enemy.getHitBox().y, dmgColor);
                    if (damageModifier > 0.1) damageModifier *= 0.75;
                    player.changeStamina(new Random().nextInt(3) + 1);
                }
            }

            enemy.setCriticalHit(isCritical);
            checkEnemyDying(enemy, player);
            writeHitLog(enemy.getEnemyAction(), finalDamage);
            player.addAction(PlayerAction.DASH_HIT);
        }
        return contactMade;
    }

    /**
     * Spawns particles at the intersection of the attack box and the enemy's hitbox.
     * This method is called when an enemy is hit by the player's attack.
     *
     * @param box The intersection rectangle where the particles will be spawned.
     * @param player The Player object representing the player in the game.
     * @param enemy The Enemy object that was hit.
     * @param isCritical Indicates if the hit was a critical hit.
     */
    private void spawnParticles(Rectangle2D.Double box, Player player, Enemy enemy, boolean isCritical) {
        if (isCritical) {
            gameState.triggerScreenFlash();
            gameState.getEffectManager().spawnDustParticles(box.getCenterX(), box.getCenterY(), 25, DustType.CRITICAL_HIT, player.getFlipSign(), null);
        }
        else if (enemy.getEnemyAction() != Anim.BLOCK) {
            gameState.getEffectManager().spawnDustParticles(box.getCenterX(), box.getCenterY(), 10, DustType.IMPACT_SPARK, player.getFlipSign(), null);
        }
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
                    if (!spellHitTimers.containsKey(enemy) || spellHitTimers.get(enemy) == 0) {
                        String dmgText = String.format("%.1f", dmg * SPELL_HIT_DISPLAY_COOLDOWN);
                        gameState.getEffectManager().spawnDamageNumber(dmgText, enemy.getHitBox().getCenterX(), enemy.getHitBox().y, DAMAGE_COLOR);
                        spellHitTimers.put(enemy, SPELL_HIT_DISPLAY_COOLDOWN);
                    }
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
                checkEnemyDying(skeleton, gameState.getPlayer());
            }
        }
        for (SpearWoman spearWoman : getEnemies(SpearWoman.class)) {
            if (projectile instanceof Fireball && spearWoman.isAlive() && spearWoman.getEnemyAction() != Anim.DEATH) {
                if (!projectile.getHitBox().intersects(spearWoman.getHitBox())) continue;
                spearWoman.hit(FIREBALL_PROJECTILE_DMG, false, false);
                projectile.setAlive(false);
                checkEnemyDying(spearWoman, gameState.getPlayer());
            }
        }
    }

    private void checkEnemyAttackObject(Enemy enemy) {
        if (!enemy.isAttacking()) return;
        boolean isAttackFrame = false;
        int animIndex = enemy.getAnimIndex();

        if (enemy instanceof Skeleton && animIndex == 3) isAttackFrame = true;
        else if (enemy instanceof Ghoul && animIndex == 3) isAttackFrame = true;
        else if (enemy instanceof Knight && animIndex == 4) isAttackFrame = true;
        else if (enemy instanceof Wraith && animIndex == 3) isAttackFrame = true;

        if (isAttackFrame) gameState.getObjectManager().checkObjectBreakByEnemy(enemy.getAttackBox());
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
                .forEach(enemy -> {
                    enemy.update(animations, levelData, player);
                    checkEnemyAttackObject(enemy);
                });
    }

    public void update(int[][] levelData, Player player) {
        updateSpellHitTimers();
        updateEnemies(Skeleton.class, skeletonAnimations, levelData, player);
        updateEnemies(Ghoul.class, ghoulAnimations, levelData, player);
        updateEnemies(Knight.class, knightAnimations, levelData, player);
        updateEnemies(Wraith.class, wraithAnimations, levelData, player);

        getEnemies(SpearWoman.class).stream()
                .filter(SpearWoman::isAlive)
                .forEach(spearWoman -> spearWoman.update(spearWomanAnimations, levelData, player, gameState.getSpellManager(), gameState.getObjectManager(), gameState.getBossInterface()));

        getEnemies(Roric.class).stream()
                .filter(Roric::isAlive)
                .forEach(roric -> roric.update(roricAnimations, levelData, player, gameState.getSpellManager(), this, gameState.getObjectManager(), gameState.getBossInterface()));

        updateClones(levelData, player);
    }

    private void updateSpellHitTimers() {
        spellHitTimers.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
        spellHitTimers.keySet().removeIf(enemy -> !enemy.isAlive());
    }

    private void updateClones(int[][] levelData, Player player) {
        roricClones.forEach(clone -> clone.update(roricAnimations, levelData, player, gameState.getSpellManager(), this,  gameState.getObjectManager(), gameState.getBossInterface()));
        roricClones.removeIf(clone -> !clone.isAlive());
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            renderSkeletons(g, xLevelOffset, yLevelOffset);
            renderGhouls(g, xLevelOffset, yLevelOffset);
            renderKnights(g, xLevelOffset, yLevelOffset);
            renderWraiths(g, xLevelOffset, yLevelOffset);
            renderSpearWoman(g, xLevelOffset, yLevelOffset);
            renderRoric(g, xLevelOffset, yLevelOffset);
            renderClones(g, xLevelOffset, yLevelOffset);
        }
        catch (Exception ignored) {}
    }

    private void renderClones(Graphics g, int xLevelOffset, int yLevelOffset) {
        EnemyRenderer<Roric> renderer = (EnemyRenderer<Roric>) enemyRenderers.get(Roric.class);
        roricClones.stream()
                .filter(RoricClone::isAlive)
                .forEach(clone -> renderer.render(g, clone, xLevelOffset, yLevelOffset));
    }

    // Activators
    /**
     * Method to spawn the clone, called by the real Roric
     *
     * @param roric The Roric instance that is being cloned.
     * @param levelData The 2D array representing the current level's layout.
     */
    public void spawnRoricClone(Roric roric, int[][] levelData) {
        Player player = gameState.getPlayer();
        double playerX = player.getHitBox().getCenterX();
        double roricX = roric.getHitBox().getCenterX();
        int spawnX = (playerX < roricX) ? (int)(roricX + 5 * TILES_SIZE) : (int)(roricX - 5 * TILES_SIZE);
        int spawnY = (int)roric.getHitBox().y - (int)(TILES_SIZE/2.2);
        int maxPixelX = levelData.length * TILES_SIZE - RORIC_WIDTH;
        spawnX = Math.max(0, Math.min(spawnX, maxPixelX));

        RoricClone clone = new RoricClone(spawnX, spawnY);
        clone.aimAtPlayer(player);
        roricClones.add(clone);
    }

    // Reset
    public void reset() {
        enemies.values().stream()
                .flatMap(List::stream)
                .forEach(Enemy::reset);
        spellHitTimers.clear();
        roricClones.clear();
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
                .ifPresent(s -> s.update(o));
    }
}
