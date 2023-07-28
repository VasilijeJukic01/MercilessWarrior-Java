package platformer.audio;

import platformer.utils.ValueEnum;

public enum Songs implements ValueEnum {
    MENU("menuTheme"),          // 0
    FOREST_1("forestTheme"),    // 1
    BOSS_1("spearWoman");       // 2

    private final String value;

    Songs(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
