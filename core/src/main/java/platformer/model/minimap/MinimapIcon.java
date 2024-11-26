package platformer.model.minimap;

import lombok.Getter;

import java.awt.*;

@Getter
public class MinimapIcon {

    private final Point position;
    private final MinimapIconType type;

    public MinimapIcon(Point position, MinimapIconType type) {
        this.position = position;
        this.type = type;
    }
}
