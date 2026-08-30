package platformer.model.levels.interior;

import com.google.gson.Gson;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.Level;
import platformer.model.levels.LvlTriggerType;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class InteriorManager {

    private GameContext context;

    private final Map<String, Level> interiors = new HashMap<>();
    private final Map<String, LevelMetadata> interiorsMetadata = new HashMap<>();
    private boolean isInterior = false;
    private String currentInteriorId = "";

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

    public void loadInterior(String id) {
        if (interiors.containsKey(id)) {
            this.isInterior = true;
            this.currentInteriorId = id;
            Level interiorLevel = interiors.get(id);
            LevelMetadata meta = interiorsMetadata.get(id);

            // Delegate the responsibility to the LevelManager
            context.getLevelManager().setInteriorLevel(interiorLevel, meta);

            context.getGameState().getPlayer().loadLvlData(interiorLevel.getLvlData());
            context.getEnemyManager().loadEnemies(interiorLevel);
            context.getObjectManager().loadObjects(interiorLevel);

            context.getGameState().getPlayer().setSpawn(interiorLevel.getPlayerSpawn(LvlTriggerType.SPAWN_A));
            context.getGameState().getCamera().updateLevelBounds(interiorLevel);

            context.getEffectManager().setAmbientEffectsActive(false);
            context.getLightManager().overrideAmbientDarkness(150);
        } else {
            Logger.getInstance().notify("Interior not found: " + id, Message.ERROR);
        }
    }

    public void returnToMainMap() {
        this.isInterior = false;
        this.currentInteriorId = "";
        context.getLevelManager().restoreMainMap();
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
