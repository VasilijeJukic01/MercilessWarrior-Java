package platformer.model.effects.lighting;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.LvlObjType;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages light properties for decorations, loaded from an external configuration file.
 */
public class DecorationLightManager {

    private static volatile DecorationLightManager instance;
    private final Map<LvlObjType, DecorationLightData> lightDataMap = new EnumMap<>(LvlObjType.class);

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
        String path = "/light/decoration_lights.json";
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(path)))) {
            Type type = new TypeToken<Map<String, DecorationLightData>>() {}.getType();
            Map<String, DecorationLightData> stringMap = new Gson().fromJson(reader, type);

            for (Map.Entry<String, DecorationLightData> entry : stringMap.entrySet()) {
                try {
                    LvlObjType objType = LvlObjType.valueOf(entry.getKey());
                    lightDataMap.put(objType, entry.getValue());
                } catch (IllegalArgumentException e) {
                    Logger.getInstance().notify("Invalid LvlObjType in decoration_lights.json: " + entry.getKey(), Message.WARNING);
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load decoration_lights.json: " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * Retrieves the light data for a given decoration type.
     *
     * @param type The LvlObjType to look up.
     * @return The DecorationLightData if it exists, otherwise null.
     */
    public DecorationLightData getLightData(LvlObjType type) {
        return lightDataMap.get(type);
    }
}