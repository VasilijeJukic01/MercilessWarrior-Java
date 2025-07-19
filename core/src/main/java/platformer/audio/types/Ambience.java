package platformer.audio.types;

import platformer.utils.ValueEnum;

/**
 * Enum representing all available ambient soundtracks in the game.
 * Implements {@link platformer.utils.ValueEnum} to associate each enum constant with its corresponding file name (without the .wav extension).
 */
public enum Ambience implements ValueEnum<String> {
    FOREST("ForestAmbient");   // 0

    private static final String PREFIX = "ambient/";
    private final String value;

    Ambience(String fileName) {
        this.value = PREFIX + fileName;
    }

    @Override
    public String getValue() {
        return value;
    }

}
