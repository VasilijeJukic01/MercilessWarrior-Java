package platformer.audio.types;

import platformer.utils.ValueEnum;

/**
 * Enum representing all available songs (music tracks) in the game.
 * Implements {@link platformer.utils.ValueEnum} to associate each enum constant with its corresponding file name (without the .wav extension).
 */
public enum Song implements ValueEnum<String> {
    MENU("MenuTheme"),          // 0
    FOREST_1("ForestTheme"),    // 1
    BOSS_1("LancerTheme"),      // 2
    BOSS_2("RoricTheme");       // 3

    private static final String PREFIX = "soundtracks/";
    private final String value;

    Song(String fileName) {
        this.value = PREFIX + fileName;
    }

    @Override
    public String getValue() {
        return value;
    }
}
