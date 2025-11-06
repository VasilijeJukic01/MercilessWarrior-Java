package platformer.model.levels.metadata;

import lombok.Getter;

@Getter
public class DecorationMetadata {

    private int colorValue;
    private String id;
    private int wid, hei;
    private int xOffset, yOffset;

    // Optional for special cases
    private String filename;
    private boolean flipped;

}