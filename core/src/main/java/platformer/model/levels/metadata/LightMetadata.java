package platformer.model.levels.metadata;

import lombok.Getter;

@Getter
public class LightMetadata {

    private int x, y;
    private int radius;
    // Hex Code
    private String color;
}