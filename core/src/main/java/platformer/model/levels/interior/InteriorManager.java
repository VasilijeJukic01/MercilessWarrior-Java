package platformer.model.levels.interior;

import com.google.gson.Gson;
import platformer.animation.AssetManager;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.gameObjects.ObjType;
import platformer.model.levels.Level;
import platformer.model.levels.LvlTriggerType;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.ui.transition.TransitionDirection;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Auto-discovers interior metadata from /meta/*.json and lazily loads interior Levels into a bounded LRU cache.
 */
public class InteriorManager {

    private static final int MAX_CACHED_INTERIORS = 3;

    private GameContext context;

    private final Map<String, LevelMetadata> interiorsMetadata = new HashMap<>();
    private final Map<String, Level> interiorsCache = new LinkedHashMap<>(8, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Level> eldest) {
            return size() > MAX_CACHED_INTERIORS && !eldest.getKey().equals(currentInteriorId);
        }
    };

    private boolean isInterior = false;
    private String currentInteriorId = "";
    private Point previousLocation;

    public InteriorManager() {
        discoverInteriors();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    private void discoverInteriors() {
        Gson gson = new Gson();
        for (String metaPath : AssetManager.getInstance().getAssetPaths("/meta/", ".json")) {
            if (metaPath.equals("/meta/world_map.json") || metaPath.startsWith("/meta/blocks/")) continue;

            String id = metaPath.substring("/meta/".length(), metaPath.length() - ".json".length());
            String roomImgPath = "/images/levels/rooms/" + id + ".png";
            if (!AssetManager.getInstance().hasAsset(roomImgPath)) continue;

            try (InputStream is = AssetManager.getInstance().getResourceAsStream(metaPath);
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                LevelMetadata meta = gson.fromJson(reader, LevelMetadata.class);
                if (meta != null && ("Invisible".equalsIgnoreCase(meta.getTileset()) || Boolean.TRUE.equals(meta.getInterior()))) {
                    interiorsMetadata.put(id, meta);
                }
            } catch (Exception e) {
                Logger.getInstance().notify("Could not inspect interior metadata " + metaPath + ": " + e.getMessage(), Message.WARNING);
            }
        }
    }

    private Level getOrLoadInterior(String id) {
        Level cached = interiorsCache.get(id);
        if (cached != null) return cached;

        String path = "/images/levels/rooms/" + id + ".png";
        BufferedImage img = ImageUtils.importImage(path, -1, -1);
        if (img == null) return null;

        LevelMetadata meta = interiorsMetadata.get(id);
        String tileset = (meta != null && meta.getTileset() != null) ? meta.getTileset() : "Invisible";
        Level level = new Level(img, tileset);
        interiorsCache.put(id, level);
        return level;
    }

    public void loadInterior(String destination) {
        String id = destination;
        LvlTriggerType targetSpawn = LvlTriggerType.SPAWN_A;
        if (destination.contains(":")) {
            String[] parts = destination.split(":");
            id = parts[0];
            try {
                targetSpawn = LvlTriggerType.valueOf(parts[1]);
            } catch (IllegalArgumentException ignored) {}
        }

        if (interiorsMetadata.containsKey(id)) {
            final String targetId = id;
            final LvlTriggerType spawnType = targetSpawn;

            Runnable loadCallback = () -> {
                if (!this.isInterior) {
                    Rectangle2D.Double playerBox = context.getGameState().getPlayer().getHitBox();
                    this.previousLocation = new Point((int) playerBox.x, (int) playerBox.y);
                }

                this.isInterior = true;
                this.currentInteriorId = targetId;
                Level interiorLevel = getOrLoadInterior(targetId);
                if (interiorLevel == null) {
                    Logger.getInstance().notify("Failed to load interior image: " + targetId, Message.ERROR);
                    return;
                }
                LevelMetadata meta = interiorsMetadata.get(targetId);

                interiorLevel.gatherData();
                if (meta != null && meta.getClearEnemiesFlag() != null && context.getWorldMemoryManager().hasFlag(meta.getClearEnemiesFlag())) {
                    interiorLevel.getEnemiesMap().clear();
                }
                if (meta != null && meta.getClearLootFlag() != null && context.getWorldMemoryManager().hasFlag(meta.getClearLootFlag())) {
                    interiorLevel.getObjectsMap().remove(ObjType.LOOT);
                }

                boolean hideCombatHud = (meta != null && meta.getHideCombatHud() != null) ? meta.getHideCombatHud() : "tavern".equalsIgnoreCase(targetId);
                context.getWorldTransitionCoordinator().enterInterior(interiorLevel, meta, spawnType, hideCombatHud);
            };

            context.getGameState().getTransitionManager().startTransition(
                    TransitionDirection.FROM_BOTTOM,
                    loadCallback
            );
        }
        else {
            Logger.getInstance().notify("Interior not found: " + id, Message.ERROR);
        }
    }

    /**
     * Restores the physics map, respawns the overworld entities, and teleports the player back to the exact location they were standing before they entered the building.
     */
    public void returnToMainMap() {
        Runnable returnCallback = () -> {
            this.isInterior = false;
            this.currentInteriorId = "";
            context.getWorldTransitionCoordinator().exitInterior(previousLocation);
        };

        context.getGameState().getTransitionManager().startTransition(
                TransitionDirection.FROM_TOP,
                returnCallback
        );
    }

    /**
     * Resets interior tracking state and re-parses all cached interior levels so save-specific flags are evaluated cleanly when loading a save.
     */
    public void resetAllCachedInteriors() {
        this.isInterior = false;
        this.currentInteriorId = "";
        this.previousLocation = null;
        interiorsCache.clear();
    }

    public boolean isInterior() {
        return isInterior;
    }

    public String getCurrentInteriorId() {
        return currentInteriorId;
    }

    public Set<String> getInteriorIds() {
        return Collections.unmodifiableSet(interiorsMetadata.keySet());
    }

    public void clearInteriorState() {
        this.isInterior = false;
        this.currentInteriorId = "";
    }
}