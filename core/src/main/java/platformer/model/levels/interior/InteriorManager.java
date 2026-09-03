package platformer.model.levels.interior;

import com.google.gson.Gson;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
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
import java.util.HashMap;
import java.util.Map;

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
        String[] interiorNames = {"tavern"};

        for (String name : interiorNames) {
            BufferedImage img = ImageUtils.importImage("/images/levels/" + name + ".png", -1, -1);
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
        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, LevelMetadata.class);
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Could not load metadata for " + name + ": " + e.getMessage(), Message.ERROR);
            return null;
        }
    }

    /**
     * Orchestrates the transition from the overworld into an interior room.
     * Saves the player's current location, swaps the active level data, moves the player
     * to the interior spawn point, and adjusts lighting/effects for indoor rules.
     *
     * @param id The ID of the interior to load (e.g., "tavern").
     */
    public void loadInterior(String id) {
        if (interiors.containsKey(id)) {
            Runnable loadCallback = () -> {
                Rectangle2D.Double playerBox = context.getGameState().getPlayer().getHitBox();
                this.previousLocation = new Point((int)playerBox.x, (int)playerBox.y);

                this.isInterior = true;
                this.currentInteriorId = id;
                Level interiorLevel = interiors.get(id);
                LevelMetadata meta = interiorsMetadata.get(id);

                context.getLevelManager().setInteriorLevel(interiorLevel, meta);
                context.getGameState().getPlayer().loadLvlData(interiorLevel.getLvlData());
                context.getEnemyManager().loadEnemies(interiorLevel);
                context.getObjectManager().loadObjects(interiorLevel);

                context.getGameState().getPlayer().setSpawn(interiorLevel.getPlayerSpawn(LvlTriggerType.SPAWN_A));
                context.getGameState().getCamera().updateLevelBounds(interiorLevel);
                context.getGameState().getCamera().snapToPlayer(context.getGameState().getPlayer());

                context.getEffectManager().setAmbientEffectsActive(false);
                context.getLightManager().overrideAmbientDarkness(150);
                context.getGameState().getPlayer().getPlayerDataManager().getUserInterface().setInterior(true);

                context.getGameState().getPlayer().resetDirections();
                context.getGameState().flushAWTEventQueue();
            };

            context.getGameState().getTransitionManager().startTransition(
                    TransitionDirection.FROM_BOTTOM,
                    loadCallback
            );
        } else {
            Logger.getInstance().notify("Interior not found: " + id, Message.ERROR);
        }
    }

    /**
     * Orchestrates the transition from an interior room back to the overworld.
     * Restores the physics map, respawns the overworld entities, and teleports the
     * player back to the exact location they were standing before they entered the building.
     */
    public void returnToMainMap() {
        Runnable returnCallback = () -> {
            this.isInterior = false;
            this.currentInteriorId = "";
            context.getLevelManager().restoreMainMap();

            Level mainLevel = context.getLevelManager().getCurrentLevel();

            context.getGameState().getPlayer().loadLvlData(mainLevel.getLvlData());
            context.getEnemyManager().loadEnemies(mainLevel);
            context.getObjectManager().loadObjects(mainLevel);

            if (previousLocation != null) {
                context.getGameState().getPlayer().setSpawn(previousLocation);
                context.getGameState().getPlayer().resetDirections();
            }

            context.getGameState().getCamera().updateLevelBounds(mainLevel);
            context.getGameState().getPlayer().getPlayerDataManager().getUserInterface().setInterior(false);

            context.getGameState().flushAWTEventQueue();
        };

        context.getGameState().getTransitionManager().startTransition(
                TransitionDirection.FROM_TOP,
                returnCallback
        );
    }

    public boolean isInterior() {
        return isInterior;
    }

    public void setInterior(boolean interior) {
        isInterior = interior;
    }

    public String getCurrentInteriorId() {
        return currentInteriorId;
    }
}
