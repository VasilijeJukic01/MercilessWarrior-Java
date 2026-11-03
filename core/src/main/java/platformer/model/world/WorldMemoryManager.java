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

import java.util.*;

/**
 * Centralizes all short-term and long-term world state persistence across named rooms.
 */
public class WorldMemoryManager {

    private static final int MAX_RECENT_ROOMS = 2;

    private GameContext context;

    // Short-term memory -> the last 2 visited room coordinates (A <-> B)
    private final Deque<String> recentRooms = new ArrayDeque<>();
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
    public void onRoomEntered(String levelId, Level currentLevel) {
        if (!currentLevel.isInitialized()) currentLevel.gatherData();

        // Strip out permanently broken bricks / dead bosses
        applyPermMemory(levelId, currentLevel);

        // If moving back and forth between the 2 most recent rooms (A <-> B), don't reset anything!
        if (recentRooms.contains(levelId)) {
            recentRooms.remove(levelId);
            recentRooms.addLast(levelId);
            return;
        }

        // Entered a 3rd room (C) -> Evict the oldest room (A) so it respawns next time!
        if (recentRooms.size() >= MAX_RECENT_ROOMS) {
            String evictedLevelId = recentRooms.removeFirst();
            Level evictedLevel = context.getLevelManager().getLevel(evictedLevelId);
            if (evictedLevel != null) {
                evictedLevel.gatherData();
                applyPermMemory(evictedLevelId, evictedLevel);
                Logger.getInstance().notify("Room '" + evictedLevelId + "' evicted from short-term memory and respawned.", Message.INFORMATION);
            }
        }

        recentRooms.addLast(levelId);
    }

    /**
     * Removes any permanently destroyed objects or defeated bosses from a Level.
     * This keeps Level.java completely clean of save-data logic!
     */
    public void applyPermMemory(String levelId, Level level) {
        List<GameObject> bricks = level.getObjectsMap().get(ObjType.BRICK);
        if (bricks != null) {
            bricks.removeIf(obj -> permanentFlags.contains(createBrickFlag(levelId, obj)));
        }
        List<Enemy> lancers = level.getEnemiesMap().get(EnemyType.LANCER);
        if (lancers != null) {
            lancers.removeIf(boss -> permanentFlags.contains(createBossFlag(levelId, EnemyType.LANCER)));
        }
    }

    /**
     * Records a permanently broken shortcut brick.
     */
    public void recordBrickBroken(Brick brick) {
        String levelId = context.getLevelManager().getCurrentLevelId();
        permanentFlags.add(createBrickFlag(levelId, brick));
        syncToAccount();
    }

    private void onEnemyDefeated(EnemyDefeatedEvent event) {
        if (event.defeatedEnemy().getEnemyType() == EnemyType.LANCER) {
            String levelId = context.getLevelManager().getCurrentLevelId();
            permanentFlags.add(createBossFlag(levelId, EnemyType.LANCER));
            syncToAccount();
        }
    }

    /**
     * Clears short-term memory (when the player dies and respawns, all regular enemies respawn).
     */
    public void reset() {
        for (String levelId : recentRooms) {
            Level level = context.getLevelManager().getLevel(levelId);
            if (level != null) {
                level.gatherData();
                applyPermMemory(levelId, level);
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

    private String createBrickFlag(String levelId, GameObject brick) {
        return "BRICK_" + levelId + "_" + (int) brick.getHitBox().x + "_" + (int) brick.getHitBox().y;
    }

    private String createBossFlag(String levelId, EnemyType type) {
        return "BOSS_" + type.name() + "_" + levelId;
    }
}