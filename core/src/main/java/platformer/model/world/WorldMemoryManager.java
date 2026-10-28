package platformer.model.world;

import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.BrickDestroyedEvent;
import platformer.event.events.EnemyDefeatedEvent;
import platformer.event.events.PreSaveEvent;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.objects.Brick;
import platformer.model.levels.Level;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Centralizes all short-term and long-term world state persistence.
 */
public class WorldMemoryManager {

    private static final int MAX_RECENT_ROOMS = 2;

    private GameContext context;

    // Short-term memory -> the last 2 visited room coordinates (A <-> B)
    private final Deque<Point> recentRooms = new ArrayDeque<>();
    // Long-term memory -> permanent flags saved to Account
    private final Set<String> permanentFlags = new HashSet<>();

    public WorldMemoryManager() {
        registerEvents();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    private void registerEvents() {
        EventBus.getInstance().register(EnemyDefeatedEvent.class, this::onEnemyDefeated);
        EventBus.getInstance().register(BrickDestroyedEvent.class, e -> recordBrickBroken(e.brick()));
        EventBus.getInstance().register(PreSaveEvent.class, e -> syncToAccount());
    }

    /**
     * Called when the player enters a room at (levelI, levelJ).
     * Manages the A -> B -> C queue and evicts/resets the oldest room if needed.
     */
    public void onRoomEntered(int levelI, int levelJ, Level currentLevel) {
        Point coords = new Point(levelI, levelJ);

        if (!currentLevel.isInitialized()) currentLevel.gatherData();

        // Strip out permanently broken bricks / dead bosses
        applyPermMemory(coords, currentLevel);

        // If moving back and forth between the 2 most recent rooms (A <-> B), don't reset anything!
        if (recentRooms.contains(coords)) {
            recentRooms.remove(coords);
            recentRooms.addLast(coords);
            return;
        }

        // Entered a 3rd room (C) -> Evict the oldest room (A) so it respawns next time!
        if (recentRooms.size() >= MAX_RECENT_ROOMS) {
            Point evictedCoords = recentRooms.removeFirst();
            Level evictedLevel = context.getLevelManager().getLevel(evictedCoords.x, evictedCoords.y);
            evictedLevel.gatherData();
            applyPermMemory(evictedCoords, evictedLevel);
            Logger.getInstance().notify("Room (" + evictedCoords.x + "," + evictedCoords.y + ") evicted from short-term memory and respawned.", Message.INFORMATION);
        }

        recentRooms.addLast(coords);
    }

    /**
     * Removes any permanently destroyed objects or defeated bosses from a Level.
     * This keeps Level.java completely clean of save-data logic!
     */
    public void applyPermMemory(Point coords, Level level) {
        List<GameObject> bricks = level.getObjectsMap().get(ObjType.BRICK);
        if (bricks != null) {
            bricks.removeIf(obj -> permanentFlags.contains(createBrickFlag(coords.x, coords.y, obj)));
        }
        List<Enemy> lancers = level.getEnemiesMap().get(EnemyType.LANCER);
        if (lancers != null) {
            lancers.removeIf(boss -> permanentFlags.contains(createBossFlag(coords.x, coords.y, EnemyType.LANCER)));
        }
    }

    /**
     * Records a permanently broken shortcut brick.
     */
    public void recordBrickBroken(Brick brick) {
        int i = context.getLevelManager().getLevelIndexI();
        int j = context.getLevelManager().getLevelIndexJ();
        String flag = createBrickFlag(i, j, brick);
        permanentFlags.add(flag);
        syncToAccount();
    }

    private void onEnemyDefeated(EnemyDefeatedEvent event) {
        if (event.defeatedEnemy().getEnemyType() == EnemyType.LANCER) {
            int i = context.getLevelManager().getLevelIndexI();
            int j = context.getLevelManager().getLevelIndexJ();
            permanentFlags.add(createBossFlag(i, j, EnemyType.LANCER));
            syncToAccount();
        }
    }

    /**
     * Clears short-term memory (when the player dies and respawns, all regular enemies respawn).
     */
    public void reset() {
        for (Point coords : recentRooms) {
            Level level = context.getLevelManager().getLevel(coords.x, coords.y);
            if (level != null) {
                level.gatherData();
                applyPermMemory(coords, level);
            }
        }
        recentRooms.clear();
    }

    // Account Syncing
    public void loadFromAccount(List<String> savedFlags) {
        permanentFlags.clear();
        recentRooms.clear();
        if (savedFlags != null) permanentFlags.addAll(savedFlags);
    }

    public void syncToAccount() {
        Framework.getInstance().getAccount().setWorldFlags(new ArrayList<>(permanentFlags));
    }

    // Key Generators
    private String createBrickFlag(int levelI, int levelJ, GameObject brick) {
        return "BRICK_" + levelI + "_" + levelJ + "_" + (int) brick.getHitBox().x + "_" + (int) brick.getHitBox().y;
    }

    private String createBossFlag(int levelI, int levelJ, EnemyType type) {
        return "BOSS_" + type.name() + "_" + levelI + "_" + levelJ;
    }
}