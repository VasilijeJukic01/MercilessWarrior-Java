package platformer.model.levels.metadata;

import lombok.Getter;

@Getter
public class TilesetMetadata {

    private String name;
    private String spritePath;
    private int rows, columns;
    private int tileSizeInSprite;
    private int tileCount;

}