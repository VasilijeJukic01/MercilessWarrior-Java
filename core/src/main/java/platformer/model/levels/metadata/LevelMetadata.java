package platformer.model.levels.metadata;

import java.util.ArrayList;
import java.util.List;

public class LevelMetadata {

    private String backgroundId;
    private List<ObjectMetadata> decorations = new ArrayList<>();

    public List<ObjectMetadata> getDecorations() {
        return decorations;
    }

    public void setDecorations(List<ObjectMetadata> decorations) {
        this.decorations = decorations;
    }

    public String getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(String backgroundId) {
        this.backgroundId = backgroundId;
    }
}
