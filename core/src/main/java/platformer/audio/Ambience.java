package platformer.audio;

import platformer.utils.ValueEnum;

public enum Ambience implements ValueEnum<String> {
    FOREST("ForestAmbient");   // 0

    private final String value;

    Ambience(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
