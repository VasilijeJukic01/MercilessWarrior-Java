package platformer.model.levels.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Root metadata model for /meta/world_map.json defining the world room graph.
 */
@Getter
@Setter
public class WorldMapMetadata {

    private String startLevel = "begin";
    private Map<String, RoomNodeMetadata> rooms = new HashMap<>();

}