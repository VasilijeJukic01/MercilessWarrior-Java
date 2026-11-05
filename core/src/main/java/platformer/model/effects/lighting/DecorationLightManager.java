package platformer.model.effects.lighting;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static platformer.constants.FilePaths.LIGHT_METADATA;

/**
 * Manages light properties for decorations by their string ID, loaded from decoration_lights.json.
 */
public class DecorationLightManager {

    private static volatile DecorationLightManager instance;
    private final Map<String, DecorationLightData> lightDataMap = new HashMap<>();

    private DecorationLightManager() {
        loadDecorationLights();
    }

    public static DecorationLightManager getInstance() {
        if (instance == null) {
            synchronized (DecorationLightManager.class) {
                if (instance == null) {
                    instance = new DecorationLightManager();
                }
            }
        }
        return instance;
    }

    private void loadDecorationLights() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(LIGHT_METADATA)))) {
            Type type = new TypeToken<Map<String, DecorationLightData>>() {}.getType();
            Map<String, DecorationLightData> loaded = new Gson().fromJson(reader, type);
            if (loaded != null) {
                lightDataMap.putAll(loaded);
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load decoration_lights.json: " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * Retrieves the light data for a given decoration ID.
     */
    public DecorationLightData getLightData(String decorationId) {
        if (decorationId == null) return null;
        return lightDataMap.get(decorationId);
    }
}