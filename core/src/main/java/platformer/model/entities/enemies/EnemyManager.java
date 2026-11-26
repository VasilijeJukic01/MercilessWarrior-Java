package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.core.GameContext;
import platformer.core.config.BalanceManager;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.EnemyDefeatedEvent;
import platformer.event.events.effects.HitStopEvent;
import platformer.event.events.ui.GamePausedEvent;
import platformer.event.events.ui.GameResumedEvent;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.roric.RoricClone;
import platformer.model.entities.enemies.renderer.*;
import platformer.model.entities.player.HitResult;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.Lava;
import platformer.model.gameObjects.objects.traps.SmashTrap;
import platformer.model.gameObjects.objects.traps.Spike;
import platformer.model.levels.Level;
import platformer.model.projectiles.Projectile;
import platformer.model.projectiles.types.Arrow;
import platformer.model.projectiles.types.Fireball;
import platformer.model.spells.types.Flame;
import platformer.state.types.PlayingState;
import platformer.utils.CollectionUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;

/**
 * Manages all enemies in the active level, including polymorphic updates, rendering, combat hits, and boss clones.
 */
@SuppressWarnings("unchecked")
public class EnemyManager {

    private GameContext context;
    private ScreenEffectsManager screenEffectsManager;

    private Map<EnemyType, List<Enemy>> enemies = new HashMap<>();
    private final Map<EnemyType, EnemyRenderer<? extends Enemy>> enemyRenderers = new EnumMap<>(EnemyType.class);
    private final Map<Enemy, Integer> spellHitTimers = new HashMap<>();
    private final List<RoricClone> roricClones = new ArrayList<>();
    private final Random rand = new Random();

    private final CombatDirector combatDirector = new CombatDirector();
    private final Set<Enemy> processedDeaths = new HashSet<>();

    // Debug
    private long lastRenderErrorLogMs = 0;

    public EnemyManager() {
        initRenderers();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    // Init
    private void initRenderers() {
        this.enemyRenderers.put(EnemyType.SKELETON, new SkeletonRenderer());
        this.enemyRenderers.put(EnemyType.GHOUL, new GhoulRenderer());
        this.enemyRenderers.put(EnemyType.KNIGHT, new KnightRenderer());
        this.enemyRenderers.put(EnemyType.WRAITH, new WraithRenderer());
        this.enemyRenderers.put(EnemyType.CORRUPTED_KNIGHT, new CorruptedKnightRenderer());
        this.enemyRenderers.put(EnemyType.LANCER, new LancerRenderer());
        this.enemyRenderers.put(EnemyType.RORIC, new RoricRenderer());
        this.enemyRenderers.put(EnemyType.GOBLIN, new GoblinRenderer(EnemyType.GOBLIN));
        this.enemyRenderers.put(EnemyType.GOBLIN_SOLDIER, new GoblinRenderer(EnemyType.GOBLIN_SOLDIER));
        this.enemyRenderers.put(EnemyType.GOBLIN_ASSASSIN, new GoblinRenderer(EnemyType.GOBLIN_ASSASSIN));
        this.enemyRenderers.put(EnemyType.GOBLIN_SPEARMAN, new GoblinRenderer(EnemyType.GOBLIN_SPEARMAN));
        this.enemyRenderers.put(EnemyType.GOBLIN_MAGE, new GoblinRenderer(EnemyType.GOBLIN_MAGE));
        this.enemyRenderers.put(EnemyType.BRIAR_HUSK, new StandardEnemyRenderer<>(EnemyType.BRIAR_HUSK));
        this.enemyRenderers.put(EnemyType.BLIGHT_BEAR, new StandardEnemyRenderer<>(EnemyType.BLIGHT_BEAR));
        this.enemyRenderers.put(EnemyType.WIDOW_SPIDER, new StandardEnemyRenderer<>(EnemyType.WIDOW_SPIDER));
        this.enemyRenderers.put(EnemyType.DEATH_RIDER, new StandardEnemyRenderer<>(EnemyType.DEATH_RIDER));
        this.enemyRenderers.put(EnemyType.ROOT_WEAVER, new StandardEnemyRenderer<>(EnemyType.ROOT_WEAVER));
        this.enemyRenderers.put(EnemyType.CRIMSON_SKELETON, new StandardEnemyRenderer<>(EnemyType.CRIMSON_SKELETON));
        this.enemyRenderers.put(EnemyType.ASHEN_GHOUL, new StandardEnemyRenderer<>(EnemyType.ASHEN_GHOUL));
    }

    public void loadEnemies(Level level) {
        this.enemies = level.getEnemiesMap();
        this.combatDirector.reset();
        processedDeaths.clear();
        for (Enemy enemy : getAllEnemies()) {
            enemy.setCombatDirector(combatDirector);
            enemy.evaluateFearState(context != null ? context.getWorldMemoryManager() : null);
        }
    }

    private <T extends Enemy> void renderEnemy(T enemy, Graphics g, int xLevelOffset, int yLevelOffset) {
        EnemyRenderer<T> renderer = (EnemyRenderer<T>) enemyRenderers.get(enemy.getEnemyType());
        if (renderer == null) return;
        renderer.render(g, enemy, xLevelOffset, yLevelOffset);
        renderer.renderStunStars(g, enemy, xLevelOffset, yLevelOffset);
    }

    /**
     * Checks if an enemy is dying and handles the event of an enemy's death.
     *
     * @param e The enemy to check.
     */
    private void checkEnemyDying(Enemy e) {
        if (e.getEnemyAction() == Anim.DEATH && processedDeaths.add(e)) {
            Player player = context.getPlayer();
            context.getObjectManager().generateLoot(e);
            player.changeStamina(rand.nextInt(5) + 2);
            BalanceManager.EnemyBalance eb = BalanceManager.getInstance().getEnemyBalance(e.getEnemyType());
            int expRange = Math.max(1, eb.getMaxExp() - eb.getMinExp() + 1);
            int expGain = eb.getMinExp() + rand.nextInt(expRange);
            player.changeExp(expGain);
            if (eb.getTokenReward() > 0) {
                player.changeUpgradeTokens(eb.getTokenReward());
            }
            double vamp = player.getStats().getPerks().getVampiricHealPct();
            if (vamp > 0) {
                player.changeHealth(player.getMaxHealth() * vamp);
            }
            EventBus.getInstance().publish(new EnemyDefeatedEvent(e));
        }
    }

    private double[] damage(Player player) {
        double critical = 0;
        double baseDmg = player.getAttackDmg();
        double comboMult = (player.getCombatHandler() != null && !player.checkAction(PlayerAction.DASH)) ? player.getCombatHandler().getComboDamageMultiplier() : 1.0;
        double dmg = baseDmg * player.getStats().getAttackMultiplier() * comboMult;

        if (rand.nextDouble() * 100.0 < player.getStats().getCritChance()) {
            dmg *= player.getStats().getCritMultiplier();
            critical = 1;
        }
        return new double[]{dmg, critical};
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
    public HitResult checkEnemyHit(Rectangle2D.Double attackBox, Player player) {
        HitResult result = HitResult.MISS;

        List<Enemy> intersectingEnemies = new ArrayList<>();
        for (Enemy enemy : getAllEnemies()) {
            if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH && attackBox.intersects(enemy.getHitBox())) {
                if (enemy.getEnemyAction() == Anim.HIDE || enemy.getEnemyAction() == Anim.REVEAL) continue;
                intersectingEnemies.add(enemy);
            }
        }

        if (intersectingEnemies.isEmpty()) return HitResult.MISS;
        if (intersectingEnemies.stream().anyMatch(e -> e instanceof Roric)) return HitResult.MISS;

        intersectingEnemies.sort(Comparator.comparingDouble(e -> Math.abs(e.getHitBox().getCenterX() - player.getHitBox().getCenterX())));
        boolean isFinisher = player.getCombatHandler() != null && !player.checkAction(PlayerAction.DASH) && player.getCombatHandler().isComboFinisher();

        double damageModifier = 1.0;
        for (Enemy enemy : intersectingEnemies) {
            double[] dmg = damage(player);
            boolean wasStunned = enemy.isStunned();
            double staggerMult = wasStunned ? 1.25 : 1.0;
            double finalDamage = dmg[0] * damageModifier * staggerMult;
            boolean isCritical = (dmg[1] == 1);

            Rectangle2D intersection = attackBox.createIntersection(enemy.getHitBox());
            spawnParticles((Rectangle2D.Double) intersection, player, enemy, isCritical, isFinisher);

            enemy.setPushDirection(enemy.getHitBox().getCenterX() >= player.getHitBox().getCenterX() ? Direction.RIGHT : Direction.LEFT);
            if (enemy.hit(finalDamage, true, false)) {
                if (enemy.getEnemyAction() == Anim.BLOCK) {
                    if (result != HitResult.HIT) result = HitResult.BLOCK;
                }
                else {
                    result = HitResult.HIT;
                    int stopFrames = isCritical ? 11 : (isFinisher ? 8 : 6);
                    EventBus.getInstance().publish(new HitStopEvent(stopFrames));

                    if (wasStunned) enemy.wakeUp();
                    else if (isFinisher && enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH && !(enemy instanceof Lancer)) {
                        if (rand.nextDouble() < 0.40) {
                            enemy.freeze(150);
                        }
                    }
                    double displayDmg = Math.round(finalDamage * 10.0) / 10.0;
                    String dmgText = isCritical ? displayDmg + "!" : String.valueOf(displayDmg);
                    Color dmgColor = isCritical ? CRITICAL_COLOR : DAMAGE_COLOR;
                    context.getEffectManager().spawnDamageNumber(dmgText, enemy.getHitBox().getCenterX(), enemy.getHitBox().y, dmgColor);
                    if (damageModifier > 0.1) damageModifier *= 0.75;
                    BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
                    int stRange = Math.max(1, pb.getMeleeHitStaminaMax() - pb.getMeleeHitStaminaMin() + 1);
                    player.changeStamina(pb.getMeleeHitStaminaMin() + rand.nextInt(stRange));
                    player.getUltimateHandler().addChargeFromHit(isCritical);
                }
            }

            enemy.setCriticalHit(isCritical);
            checkEnemyDying(enemy);
            writeHitLog(enemy.getEnemyAction(), finalDamage);
            player.addAction(PlayerAction.DASH_HIT);
        }
        return result;
    }

    private void spawnParticles(Rectangle2D.Double box, Player player, Enemy enemy, boolean isCritical, boolean isFinisher) {
        int screenX = (int) box.getCenterX() - context.getCamera().getXOffset();
        int screenY = (int) box.getCenterY() - context.getCamera().getYOffset();
        if (isCritical) {
            screenEffectsManager.triggerFlash();
            screenEffectsManager.triggerShake(14, 7.5);
            screenEffectsManager.triggerShockwave(screenX, screenY, scale(65), 18, CRITICAL_COLOR);
            context.getEffectManager().spawnDustParticles(box.getCenterX(), box.getCenterY(), 25, DustType.CRITICAL_HIT, player.getFlipSign(), null);
        }
        else if (enemy.getEnemyAction() != Anim.BLOCK) {
            if (isFinisher) {
                screenEffectsManager.triggerShake(8, 4.2);
                screenEffectsManager.triggerShockwave(screenX, screenY, scale(42), 14, new Color(255, 210, 120));
            }
            context.getEffectManager().spawnDustParticles(box.getCenterX(), box.getCenterY(), isFinisher ? 16 : 10, DustType.IMPACT_SPARK, player.getFlipSign(), null);
        }
    }

    private void writeHitLog(Anim anim, double dmg) {
        if (anim == Anim.BLOCK) Logger.getInstance().notify("Enemy blocks player's attack.", Message.NOTIFICATION);
        else Logger.getInstance().notify("Player gives damage to enemy: " + dmg, Message.NOTIFICATION);
    }

    /**
     * Checks whether the player's active Flame spell hits any non-boss enemies, applying bonus undead damage where configured.
     */
    public void checkEnemySpellHit() {
        Flame flame = context.getSpellManager().getFlames();
        if (!flame.isActive()) return;

        BalanceManager.CombatBalance cb = BalanceManager.getInstance().getCombat();
        double spellMult = context.getPlayer().getStats().getSpellMultiplier();
        double baseFlame = cb.getFlameTickDamage() * spellMult;
        double undeadFlame = baseFlame * cb.getFlameUndeadMultiplier();

        for (Enemy enemy : getAllEnemies()) {
            if (enemy.isBoss() || !enemy.isAlive()) continue;
            Anim action = enemy.getEnemyAction();
            if (action == Anim.DEATH || action == Anim.HIDE) continue;

            if (flame.getHitBox().intersects(enemy.getHitBox())) {
                double dmg = enemy.isUndead() ? undeadFlame : baseFlame;
                enemy.spellHit(dmg);
                if (!spellHitTimers.containsKey(enemy) || spellHitTimers.get(enemy) == 0) {
                    String dmgText = String.format("%.1f", dmg * SPELL_HIT_DISPLAY_COOLDOWN);
                    context.getEffectManager().spawnDamageNumber(dmgText, enemy.getHitBox().getCenterX(), enemy.getHitBox().y, DAMAGE_COLOR);
                    spellHitTimers.put(enemy, SPELL_HIT_DISPLAY_COOLDOWN);
                }
                checkEnemyDying(enemy);
            }
        }
    }

    public void checkEnemyTrapHit(GameObject object) {
        if (!(object instanceof Spike) && !(object instanceof SmashTrap) && !(object instanceof Lava)) return;
        if (object instanceof SmashTrap) {
            if (object.getAnimIndex() < 1 || object.getAnimIndex() > 7) {
                return;
            }
        }
        for (List<Enemy> enemyList : enemies.values()) {
            for (Enemy enemy : enemyList) {
                if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH && object.getHitBox().intersects(enemy.getHitBox())) {
                    enemy.hit(BalanceManager.getInstance().getCombat().getTrapInstantKillDamage(), false, false);
                }
            }
        }
    }

    public void checkEnemyProjectileHit(List<Projectile> projectiles) {
        projectiles.stream()
                .filter(Projectile::isAlive)
                .forEach(this::checkEnemyProjectileHit);
    }

    public void checkEnemyProjectileHit(Projectile projectile) {
        BalanceManager.CombatBalance cb = BalanceManager.getInstance().getCombat();
        if (projectile instanceof Fireball) {
            double spellMult = context.getPlayer().getStats().getSpellMultiplier();
            double fbDamage = cb.getPlayerFireballDamage() * spellMult;
            for (Enemy enemy : getAllEnemies()) {
                if (enemy instanceof Roric) continue;
                if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH && projectile.getShapeBounds().intersects(enemy.getHitBox())) {
                    if (enemy.hit(fbDamage, false, true)) {
                        String dmgText = String.format("%.1f", fbDamage);
                        context.getEffectManager().spawnDamageNumber(dmgText, enemy.getHitBox().getCenterX(), enemy.getHitBox().y, DAMAGE_COLOR);
                    }
                    checkEnemyDying(enemy);
                    if (!context.getPlayer().getStats().getPerks().isPiercingFireball()) {
                        projectile.setAlive(false);
                        break;
                    }
                }
            }
            return;
        }

        if (projectile instanceof Arrow) {
            for (Enemy enemy : getAllEnemies()) {
                if (enemy.isBoss() || !enemy.isAlive() || enemy.getEnemyAction() == Anim.DEATH) continue;
                if (projectile.getShapeBounds().intersects(enemy.getHitBox())) {
                    enemy.hit(cb.getTrapProjectileToEnemyDamage(), false, false);
                    Direction projectileDirection = projectile.getDirection();
                    enemy.setPushDirection(projectileDirection == Direction.LEFT ? Direction.RIGHT : Direction.LEFT);
                    projectile.setAlive(false);
                    checkEnemyDying(enemy);
                    break;
                }
            }
        }
    }

    private void checkEnemyAttackObject(Enemy enemy) {
        if (!enemy.isAttacking()) return;
        if (enemy.isObjectBreakFrame()) {
            context.getObjectManager().checkObjectBreakByEnemy(enemy.getAttackBox());
        }
    }

    /**
     * Applies damage, visual feedback, and death/loot checks for a single strike of the Player's Ultimate ability.
     *
     * @param enemy      The target enemy.
     * @param damage     The calculated damage for this slash mark.
     * @param isCritical True if the slash was judged PERFECT.
     * @param player     The player entity.
     */
    public void applyUltimateStrike(Enemy enemy, double damage, boolean isCritical, Player player) {
        if (enemy == null || !enemy.isAlive() || enemy.getEnemyAction() == Anim.DEATH || enemy instanceof Roric) return;

        enemy.wakeUp();
        enemy.setPushDirection(enemy.getHitBox().getCenterX() >= player.getHitBox().getCenterX() ? Direction.RIGHT : Direction.LEFT);
        if (enemy.hit(damage, false, false)) {
            double displayDmg = Math.round(damage * 10.0) / 10.0;
            String dmgText = isCritical ? displayDmg + "!" : String.valueOf(displayDmg);
            Color dmgColor = isCritical ? CRITICAL_COLOR : DAMAGE_COLOR;
            context.getEffectManager().spawnDamageNumber(dmgText, enemy.getHitBox().getCenterX(), enemy.getHitBox().y, dmgColor);
        }

        int particleCount = isCritical ? 22 : 12;
        DustType particleType = isCritical ? DustType.CRITICAL_HIT : DustType.IMPACT_SPARK;
        context.getEffectManager().spawnDustParticles(enemy.getHitBox().getCenterX(), enemy.getHitBox().getCenterY(), particleCount, particleType, player.getFlipSign(), null);
        context.getEffectManager().spawnDustParticles(enemy.getHitBox().getCenterX(), enemy.getHitBox().getCenterY(), 8, DustType.SW_DASH_SLASH, player.getFlipSign(), null);

        enemy.setCriticalHit(isCritical);
        checkEnemyDying(enemy);
    }

    // Core
    /**
     * Updates the state of all enemies of a specific type in the game.
     * This includes updating their animations, checking for collisions, and handling their behavior.
     *
     * @param levelData The 2D array representing the current level's layout.
     * @param player The Player object representing the player in the game.
     * @param follower The Follower object representing the player's follower
     */
    public void update(int[][] levelData, Player player, Entity follower) {
        updateSpellHitTimers();
        List<Enemy> allEnemies = getAllEnemies();
        boolean cinematicFreeze = (context.getCutsceneManager() != null && context.getCutsceneManager().isActive()) || (context.getGameState() != null && context.getGameState().getActiveState() == PlayingState.DIALOGUE);

        if (!cinematicFreeze) combatDirector.update(allEnemies, player, follower);
        for (Enemy enemy : allEnemies) {
            if (!enemy.isAlive()) continue;
            if (enemy.getEnemyAction() == Anim.DEATH) {
                checkEnemyDying(enemy);
            }
            if (cinematicFreeze && enemy.getEnemyAction() != Anim.DEATH) {
                enemy.updateCinematicIdle(levelData);
                continue;
            }
            if (enemy instanceof Lancer lancer) {
                lancer.update(levelData, player, context.getSpellManager(), context.getObjectManager(), context.getGameState().getBossInterface());
            }
            else if (enemy instanceof Roric roric) {
                roric.update(levelData, player, context.getSpellManager(), this, context.getObjectManager(), context.getGameState().getBossInterface());
            }
            else {
                enemy.update(levelData, player, follower);
                checkEnemyAttackObject(enemy);
                if (enemy.getEnemyAction() == Anim.DEATH) {
                    checkEnemyDying(enemy);
                }
            }
        }
        updateClones(levelData, player);
    }

    private void updateSpellHitTimers() {
        spellHitTimers.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
        spellHitTimers.keySet().removeIf(enemy -> !enemy.isAlive());
    }

    private void updateClones(int[][] levelData, Player player) {
        roricClones.forEach(clone -> clone.update(levelData, player, context.getSpellManager(), this, context.getObjectManager(), context.getGameState().getBossInterface()));
        roricClones.removeIf(clone -> !clone.isAlive());
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            for (Enemy enemy : getAllEnemies()) {
                if (enemy.isAlive()) {
                    renderEnemy(enemy, g, xLevelOffset, yLevelOffset);
                }
            }
            for (RoricClone clone : roricClones) {
                if (clone.isAlive()) {
                    renderEnemy(clone, g, xLevelOffset, yLevelOffset);
                }
            }
        }
        catch (Exception e) {
            long now = System.currentTimeMillis();
            if (now - lastRenderErrorLogMs >= 1000) {
                lastRenderErrorLogMs = now;
                Logger.getInstance().notify("Render error in " + getClass().getSimpleName() + ": " + e.getMessage(), Message.ERROR);
            }
        }
    }

    /**
     * Method to spawn the clone, called by the real Roric
     *
     * @param spawnX The x-coordinate where the clone should spawn.
     * @param spawnY The y-coordinate where the clone should spawn.
     */
    public void spawnRoricClone(int spawnX, int spawnY) {
        Player player = context.getGameState().getPlayer();
        RoricClone clone = new RoricClone(spawnX, spawnY);
        clone.aimAtPlayer(player);
        roricClones.add(clone);
    }

    public void pauseRoricTimer() {
        Roric roric = getRoricInstance();
        if (roric != null) roric.getPhaseManager().pauseFightTimer();
    }

    public void unpauseRoricTimer() {
        Roric roric = getRoricInstance();
        if (roric != null) roric.getPhaseManager().unpauseFightTimer();
    }

    public void onGamePaused(GamePausedEvent event) {
        pauseRoricTimer();
    }

    public void onGameResumed(GameResumedEvent event) {
        unpauseRoricTimer();
    }

    // Reset
    public void reset() {
        combatDirector.reset();
        enemies.values().stream()
                .flatMap(List::stream)
                .forEach(Enemy::reset);
        spellHitTimers.clear();
        roricClones.clear();
        processedDeaths.clear();
    }

    public List<Enemy> getAllEnemies() {
        return CollectionUtils.getAllItems(enemies);
    }

    public Roric getRoricInstance() {
        return getEnemies(Roric.class).stream().findFirst().orElse(null);
    }

    private <T> List<T> getEnemies(Class<T> enemyType) {
        return getAllEnemies().stream()
                .filter(enemyType::isInstance)
                .map(enemyType::cast)
                .collect(Collectors.toList());
    }

    public void injectScreenEffectsManager(ScreenEffectsManager screenEffectsManager) {
        this.screenEffectsManager = screenEffectsManager;
    }

    // Debug
    public Enemy spawnEnemy(EnemyType type, int worldX, int worldY) {
        if (type == null || type == EnemyType.MAX) return null;
        Enemy enemy = EnemyFactory.create(type, worldX, worldY);
        if (enemy == null) return null;

        if (context != null && context.getLevelManager() != null && context.getLevelManager().getCurrentLevel() != null) {
            int[][] lvlData = context.getLevelManager().getCurrentLevel().getLvlData();
            double groundY = platformer.physics.CollisionDetector.getGroundY(worldX, worldY, lvlData);
            enemy.getHitBox().x = worldX;
            enemy.getHitBox().y = groundY - enemy.getHitBox().height - 1;
        }

        enemy.setCombatDirector(combatDirector);
        enemy.evaluateFearState(context != null ? context.getWorldMemoryManager() : null);
        enemies.computeIfAbsent(type, k -> new ArrayList<>()).add(enemy);
        return enemy;
    }
}
