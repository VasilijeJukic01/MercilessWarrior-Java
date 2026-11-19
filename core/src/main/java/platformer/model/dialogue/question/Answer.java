package platformer.model.dialogue.question;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Represents an answer in a dialogue question and its data-driven outcomes.
 */
@Getter
@Setter
@NoArgsConstructor
public class Answer {

    private String answer;
    private String next;
    private String quest;
    private Map<String, Integer> requiredItems;
    private int requiredCoins;
    private String setFlag;
    private int rewardCoins;
    private int rewardExp;
    private Map<String, Integer> rewardItems;
    private String completeQuest;
    private String followUpDialogue;
    private String cutscene;
    private String failureDialogue;
    private String objectiveQuest;
    private String objectiveText;

    public Answer(String answer, String next) {
        this.answer = answer;
        this.next = next;
    }

}
