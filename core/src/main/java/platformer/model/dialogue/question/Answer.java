package platformer.model.dialogue.question;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an answer in a dialogue question.
 */
@Getter
@Setter
public class Answer {

    private String answer;
    private String next;

    /**
     * Constructs an Answer with the specified answer text and the next dialogue ID.
     *
     * @param answer the text of the answer
     * @param next the action to take after selecting the answer
     */
    public Answer(String answer, String next) {
        this.answer = answer;
        this.next = next;
    }

}
