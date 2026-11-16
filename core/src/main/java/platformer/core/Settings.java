package platformer.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import platformer.core.loading.PathManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manages global, user-configurable game settings that affect the player's experience.
 * <p>
 * This class serves as a simple data container for preferences.
 * These settings are distinct from the {@link Account} data, as they do not track game progress and are typically saved locally to persist between sessions.
 */
@Getter
@Setter
public class Settings {

    private static volatile Settings instance = null;

    // Gameplay Settings
    private boolean screenShake = true;
    private double particleDensity = 1.0;
    private boolean showDamageCounters = true;

    // Audio Settings
    private float musicVolume = 0.2f;
    private float sfxVolume = 0.2f;
    private boolean musicMute = false;
    private boolean sfxMute = false;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Settings() {}

    public static Settings getInstance() {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    instance = loadSettings();
                }
            }
        }
        return instance;
    }

    private static Settings loadSettings() {
        File file = new File(PathManager.getSettingsPath());
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, Settings.class);
            } catch (IOException ignored) {}
        }
        return new Settings();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(PathManager.getSettingsPath())) {
            gson.toJson(this, writer);
        } catch (IOException ignored) {}
    }
}
