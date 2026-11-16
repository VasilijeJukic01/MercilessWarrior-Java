package platformer.model.cutscene;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model representing a declarative cutscene script loaded from cutscenes.json.
 */
@Getter
@Setter
public class CutsceneDefinition {
    private String id;
    private String triggerType;
    private String levelId;
    private String requiredActiveQuest;
    private String onceFlag;
    private List<CutsceneStepData> steps = new ArrayList<>();
}