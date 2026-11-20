package platformer.model.entities.enemies.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.animation.AssetManager;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.enemies.EnemyType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static platformer.constants.FilePaths.ENEMIES_PATH;

/**
 * Loads and caches all {@link EnemyDefinition} entries from enemies.json.
 */
public class EnemyRegistry {

    private static volatile EnemyRegistry instance;
    private final Map<EnemyType, EnemyDefinition> definitions = new EnumMap<>(EnemyType.class);

    private EnemyRegistry() {
        loadDefinitions();
    }

    public static EnemyRegistry getInstance() {
        if (instance == null) {
            synchronized (EnemyRegistry.class) {
                if (instance == null) {
                    instance = new EnemyRegistry();
                }
            }
        }
        return instance;
    }

    private void loadDefinitions() {
        try (InputStream is = AssetManager.getInstance().getResourceAsStream(ENEMIES_PATH)) {
            if (is == null) {
                Logger.getInstance().notify("enemies.json not found at " + ENEMIES_PATH, Message.ERROR);
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Type mapType = new TypeToken<Map<String, EnemyDefinition>>() {}.getType();
                Map<String, EnemyDefinition> raw = new Gson().fromJson(reader, mapType);
                if (raw == null) return;

                for (Map.Entry<String, EnemyDefinition> entry : raw.entrySet()) {
                    try {
                        EnemyType type = EnemyType.valueOf(entry.getKey().toUpperCase());
                        definitions.put(type, entry.getValue());
                    }
                    catch (IllegalArgumentException e) {
                        Logger.getInstance().notify("Unknown enemy type in enemies.json: " + entry.getKey(), Message.WARNING);
                    }
                }
            }
        }
        catch (Exception e) {
            Logger.getInstance().notify("Failed to load enemies.json: " + e.getMessage(), Message.ERROR);
        }
    }

    public EnemyDefinition get(EnemyType type) {
        return definitions.getOrDefault(type, new EnemyDefinition());
    }

    public Set<EnemyType> getRegisteredTypes() {
        return Collections.unmodifiableSet(definitions.keySet());
    }
}