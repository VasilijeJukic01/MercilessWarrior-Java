package platformer.model.world;

import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.npc.NpcType;
import platformer.model.levels.Level;
import platformer.model.levels.LvlTriggerType;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.TILES_SIZE;

/**
 * Centralizes all room, interior, arena, save-load, and fast-travel transitions into a single deterministic pipeline.
 */
public class WorldTransitionCoordinator {

    private GameContext context;

    public void wire(GameContext context) {
        this.context = context;
    }

    /**
     * Loads the player's saved totem room and resets player combat state to full health.
     */
    public void loadSavedRoom(int savedSpawnId) {
        context.getLevelManager().loadSavePoint(savedSpawnId);
        Level level = context.getLevelManager().getCurrentLevel();
        Point spawnPoint = context.getLevelManager().getTotemSpawnPoint(savedSpawnId);
        bindLevel(level, spawnPoint, true, true, false, false, false);
        context.getLightManager().snapToTimeCycle(context.getTimeCycleManager());
        context.getEffectManager().updateTimeOfDay(context.getTimeCycleManager());
    }

    /**
     * Transitions through a world-map room exit trigger.
     */
    public void enterRoom(String targetLevelId, LvlTriggerType targetSpawn, Integer spawnX, Integer spawnY) {
        context.getPlayer().removeAction(PlayerAction.LEVEL_TRANSITION);
        context.getLevelManager().loadLevelById(targetLevelId);
        Level level = context.getLevelManager().getCurrentLevel();
        Point spawnPoint = level.getPlayerSpawn(targetSpawn, spawnX, spawnY);
        bindLevel(level, spawnPoint, false, true, false, false, false);
        Logger.getInstance().notify("Loaded level: " + targetLevelId + " at " + targetSpawn, Message.NOTIFICATION);
    }

    /**
     * Fast-travels to an unlocked Save Totem room and updates the active spawn point.
     */
    public void travelToTotem(int totemId, String targetLevelId, String totemName) {
        context.getLevelManager().loadLevelById(targetLevelId);
        Level level = context.getLevelManager().getCurrentLevel();
        Point spawnPoint = context.getLevelManager().getTotemSpawnPoint(totemId);
        bindLevel(level, spawnPoint, false, true, false, false, false);

        Framework.getInstance().getAccount().setSpawn(totemId);
        Logger.getInstance().notify("Fast traveled to Save Totem: " + totemName, Message.INFORMATION);
    }

    /**
     * Advances time, fully heals the player, and respawns regular enemies in the current room.
     */
    public void restInCurrentRoom() {
        Player player = context.getPlayer();
        player.resetDirections();
        context.getGameState().getStateController().resetKeys();
        context.getGameState().flushAWTEventQueue();

        context.getTimeCycleManager().advanceTime(0.45);
        context.getLightManager().snapToTimeCycle(context.getTimeCycleManager());
        context.getEffectManager().updateTimeOfDay(context.getTimeCycleManager());

        if (player.getCurrentHealth() < player.getMaxHealth()) {
            player.changeHealth(player.getMaxHealth() - player.getCurrentHealth());
        }

        context.getWorldMemoryManager().reset();
        Level currentLevel = context.getLevelManager().getCurrentLevel();
        String currentLevelId = context.getLevelManager().getCurrentLevelId();
        currentLevel.gatherData();
        context.getWorldMemoryManager().onRoomEntered(currentLevelId, currentLevel);

        context.getEnemyManager().loadEnemies(currentLevel);
        context.getObjectManager().loadObjects(currentLevel);
        context.getProjectileManager().reset();
        context.getSpellManager().reset();
    }

    /**
     * Binds an interior diorama level and applies interior lighting and HUD rules.
     */
    public void enterInterior(Level interiorLevel, LevelMetadata meta, LvlTriggerType spawnType, boolean hideCombatHud) {
        context.getLevelManager().setInteriorLevel(interiorLevel, meta);
        Point spawnPoint = interiorLevel.getPlayerSpawn(spawnType);
        bindLevel(interiorLevel, spawnPoint, false, false, true, hideCombatHud, false);
    }

    /**
     * Restores the main overworld level and places the player back at their entry door position.
     */
    public void exitInterior(Point returnLocation) {
        context.getLevelManager().restoreMainMap();
        Level mainLevel = context.getLevelManager().getCurrentLevel();
        Point targetSpawn = returnLocation != null ? returnLocation : mainLevel.getPlayerSpawn(LvlTriggerType.SPAWN_A);
        bindLevel(mainLevel, targetSpawn, false, true, false, false, false);
    }

    /**
     * Enters the boss arena level with clean combat state and no follower.
     */
    public void enterArena() {
        context.getLevelManager().switchToArena();
        Level arena = context.getLevelManager().getCurrentLevel();
        Point spawnPoint = arena.getPlayerSpawn(LvlTriggerType.SPAWN_A);
        context.getGameState().reset();
        bindLevel(arena, spawnPoint, false, false, false, false, true);
    }

    /**
     * Returns from the boss arena to the player's last Save Totem on the main map.
     */
    public void exitArena(int savedSpawnId) {
        context.getLevelManager().restoreMainMap();
        Level originalLevel = context.getLevelManager().getCurrentLevel();
        Point spawnPoint = context.getLevelManager().getTotemSpawnPoint(savedSpawnId);
        if (spawnPoint == null) {
            spawnPoint = originalLevel.getPlayerSpawn(LvlTriggerType.SPAWN_A);
        }
        context.getGameState().reset();
        bindLevel(originalLevel, spawnPoint, false, true, false, false, false);
    }

    private void bindLevel(Level level, Point spawnPoint, boolean resetPlayer, boolean trackMinimap, boolean isInterior, boolean hideCombatHud, boolean isArena) {
        Player player = context.getPlayer();
        GameWorld world = context.getGameState().getWorld();

        player.activateMinimap(false);
        context.getProjectileManager().reset();
        context.getSpellManager().initBossSpells();

        player.loadLvlData(level.getLvlData());
        context.getEnemyManager().loadEnemies(level);
        context.getObjectManager().loadObjects(level);
        context.getLevelManager().prewarmCurrentLevelDecorations();
        context.getLightManager().ensureStaticRoomLightsBuilt();
        context.getDecorationEffectManager().rebuildForCurrentLevel();

        player.setSpawn(spawnPoint);
        ensureSpawnClearOfFloor(player, level.getLvlData());
        if (resetPlayer) {
            player.reset();
        }
        else {
            player.clearTransitionMomentum();
            player.resetDirections();
            if (!CollisionDetector.isEntityOnFloor(player.getHitBox(), level.getLvlData())) {
                player.setInAir(true);
            }
        }

        syncFollower(spawnPoint, isArena);
        player.getPlayerDataManager().getUserInterface().setInterior(hideCombatHud);

        if (isInterior) {
            context.getEffectManager().setAmbientEffectsActive(false);
            context.getLightManager().overrideAmbientDarkness(150);
            context.getDecorationEffectManager().setActive(false);
        }
        else {
            context.getLightManager().releaseAmbientDarkness();
            context.getDecorationEffectManager().setActive(true);
            world.reinitializeParticles();
        }

        context.getMinimapManager().changeLevel();
        player.activateMinimap(trackMinimap);
        if (trackMinimap) {
            context.getMinimapManager().updatePlayerPosition(player.getHitBox().getCenterX(), player.getHitBox().getCenterY());
        }

        context.getCamera().updateLevelBounds(level);
        context.getCamera().snapToPlayer(player);
        context.getGameState().getOverlayManager().reset();
        context.getGameState().getStateController().resetKeys();
        context.getGameState().flushAWTEventQueue();
    }

    private void syncFollower(Point spawnPoint, boolean isArena) {
        if (isArena) {
            context.getObjectManager().despawnFollower();
            return;
        }
        NpcType followerType = context.getQuestManager().getActiveFollowerType();
        if (followerType != null) {
            context.getObjectManager().spawnFollower(followerType, spawnPoint.x, spawnPoint.y);
        }
        else {
            context.getObjectManager().despawnFollower();
        }
    }

    private void ensureSpawnClearOfFloor(Player player, int[][] levelData) {
        Rectangle2D.Double hb = player.getHitBox();
        int maxLift = TILES_SIZE * 2;
        int lifted = 0;
        while (!CollisionDetector.canMoveHere(hb.x, hb.y, hb.width, hb.height, levelData) && lifted < maxLift) {
            hb.y -= 2;
            lifted += 2;
        }
        if (lifted > 0) {
            player.setSpawn(new Point((int) hb.x, (int) hb.y));
        }
    }
}