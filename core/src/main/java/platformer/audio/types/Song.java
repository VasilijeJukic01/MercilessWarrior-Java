package platformer.audio.types;

import platformer.utils.ValueEnum;

public enum Song implements ValueEnum<String> {
    MENU("MenuTheme"),          // 0
    FOREST_1("ForestTheme"),    // 1
    BOSS_1("LancerTheme");      // 2

    private final String value;

    Song(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
