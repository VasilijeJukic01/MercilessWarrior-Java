package platformer.model.cutscene;

import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer model representing a single step in a JSON cutscene definition.
 */
@Getter
@Setter
public class CutsceneStepData {
    private String type;
    private Boolean active;
    private Integer tileX;
    private Integer tileY;
    private Integer duration;
    private Float targetAlpha;
    private String npcType;
    private String direction;
    private String dialogueId;
    private String speaker;
    private String questName;
    private String objectiveType;
    private String target;
    private Integer stepIndex;
}