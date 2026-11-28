package platformer.model.levels;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import platformer.animation.AssetManager;
import platformer.audio.types.Ambience;
import platformer.audio.types.Song;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.metadata.LevelMetadata;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.FilePaths.BIOMES_PATH;

/**
 * Loads biome definitions from /meta/biomes.json and resolves music, ambience, tint, and weather with optional per-room overrides from {@link LevelMetadata}.
 */
public class BiomeManager {

    private static volatile BiomeManager instance;

    @Getter
    public static class BiomeDefinition {
        private String song = "FOREST_1";
        private String ambience = "FOREST";
        private String ambientTint = "#018278";
        private int ambientTintAlpha = 60;
        private boolean stormMode = false;

        private transient Color cachedTint;

        public Color getTintColor() {
            if (ambientTintAlpha <= 0 || ambientTint == null || ambientTint.isBlank()) return null;
            if (cachedTint == null) {
                try {
                    Color base = Color.decode(ambientTint.trim());
                    cachedTint = new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.min(255, ambientTintAlpha));
                } catch (NumberFormatException ignored) {
                    cachedTint = new Color(1, 130, 120, 60);
                }
            }
            return cachedTint;
        }
    }

    private final Map<String, BiomeDefinition> biomes = new HashMap<>();
    private final BiomeDefinition defaultBiome = new BiomeDefinition();

    private BiomeManager() {
        loadBiomes();
    }

    public static BiomeManager getInstance() {
        if (instance == null) {
            synchronized (BiomeManager.class) {
                if (instance == null) instance = new BiomeManager();
            }
        }
        return instance;
    }

    private void loadBiomes() {
        try (InputStream is = AssetManager.getInstance().getResourceAsStream(BIOMES_PATH)) {
            if (is == null) return;
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, BiomeDefinition>>() {}.getType();
                Map<String, BiomeDefinition> loaded = new Gson().fromJson(reader, type);
                if (loaded != null) {
                    loaded.forEach((k, v) -> biomes.put(k.toUpperCase(), v));
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load biomes.json: " + e.getMessage(), Message.ERROR);
        }
    }

    public BiomeDefinition getBiome(String biomeId) {
        if (biomeId == null || biomeId.isBlank()) return biomes.getOrDefault("FOREST", defaultBiome);
        return biomes.getOrDefault(biomeId.toUpperCase(), biomes.getOrDefault("FOREST", defaultBiome));
    }

    public Song resolveSong(LevelMetadata meta) {
        String songName = (meta != null && meta.getMusic() != null && !meta.getMusic().isBlank()) ? meta.getMusic() : getBiome(meta != null ? meta.getBiome() : null).getSong();
        if (songName == null || songName.equalsIgnoreCase("NONE")) return null;
        try {
            return Song.valueOf(songName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Song.FOREST_1;
        }
    }

    public Ambience resolveAmbience(LevelMetadata meta, boolean isInterior) {
        if (isInterior) return null;
        String ambName = (meta != null && meta.getAmbience() != null && !meta.getAmbience().isBlank()) ? meta.getAmbience() : getBiome(meta != null ? meta.getBiome() : null).getAmbience();
        if (ambName == null || ambName.equalsIgnoreCase("NONE")) return null;
        try {
            return Ambience.valueOf(ambName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Color resolveAmbientTint(LevelMetadata meta) {
        return getBiome(meta != null ? meta.getBiome() : null).getTintColor();
    }

    public boolean isStormMode(LevelMetadata meta) {
        if (meta != null && meta.getStormMode() != null) return meta.getStormMode();
        return getBiome(meta != null ? meta.getBiome() : null).isStormMode();
    }
}