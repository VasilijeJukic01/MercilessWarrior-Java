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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages the loading, storage, and state transitions for interior levels.
 * <p>
 * Interiors use a Diorama Architecture:
 * A single high-resolution image is used as the background, and an invisible collision map is layered over it.
 * This manager handles swapping the engine's active level to the interior and restoring the player's exact position when they exit.
 */
public class InteriorManager {

    private GameContext context;

    private final Map<String, Level> interiors = new HashMap<>();
    private final Map<String, LevelMetadata> interiorsMetadata = new HashMap<>();
    private boolean isInterior = false;
    private String currentInteriorId = "";

    private Point previousLocation;

    public InteriorManager() {
        buildInteriors();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    private void buildInteriors() {
        String[] interiorNames = {"tavern", "crypt1", "crypt2", "goblin_cave"};

        for (String name : interiorNames) {
            String path = "/images/levels/rooms/" + name + ".png";
            if (AssetManager.getInstance().getAsset(path) == null) continue;
            BufferedImage img = ImageUtils.importImage(path, -1, -1);
            if (img != null) {
                LevelMetadata meta = loadMetadataForInterior(name);
                String tileset = (meta != null && meta.getTileset() != null) ? meta.getTileset() : "Invisible";
                interiorsMetadata.put(name, meta);
                interiors.put(name, new Level(img, tileset));
            }
        }
    }

    private LevelMetadata loadMetadataForInterior(String name) {
        String jsonPath = "/meta/" + name + ".json";
        try (InputStream is = AssetManager.getInstance().getResourceAsStream(jsonPath)) {
            if (is == null) return null;
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, LevelMetadata.class);
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Could not load metadata for " + name + ": " + e.getMessage(), Message.ERROR);
            return null;
        }
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

        if (interiors.containsKey(id)) {
            final String targetId = id;
            final LvlTriggerType spawnType = targetSpawn;

            Runnable loadCallback = () -> {
                if (!this.isInterior) {
                    Rectangle2D.Double playerBox = context.getGameState().getPlayer().getHitBox();
                    this.previousLocation = new Point((int) playerBox.x, (int) playerBox.y);
                }

                this.isInterior = true;
                this.currentInteriorId = targetId;
                Level interiorLevel = interiors.get(targetId);
                LevelMetadata meta = interiorsMetadata.get(targetId);

                interiorLevel.gatherData();
                if (meta != null && meta.getClearEnemiesFlag() != null && context.getWorldMemoryManager().hasFlag(meta.getClearEnemiesFlag())) {
                    interiorLevel.getEnemiesMap().clear();
                }
                if (meta != null && meta.getClearLootFlag() != null && context.getWorldMemoryManager().hasFlag(meta.getClearLootFlag())) {
                    interiorLevel.getObjectsMap().remove(ObjType.LOOT);
                }

                boolean hideCombatHud = "tavern".equalsIgnoreCase(targetId);
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
        for (Level interior : interiors.values()) {
            interior.gatherData();
        }
    }

    public boolean isInterior() {
        return isInterior;
    }

    public String getCurrentInteriorId() {
        return currentInteriorId;
    }

    public Set<String> getInteriorIds() {
        return Collections.unmodifiableSet(interiors.keySet());
    }

    public void clearInteriorState() {
        this.isInterior = false;
        this.currentInteriorId = "";
    }
}
