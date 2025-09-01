package platformer.model.levels.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LevelMetadata {

    private String backgroundId;
    private Boolean ambientParticlesEnabled;
    private List<ObjectMetadata> decorations = new ArrayList<>();

}
