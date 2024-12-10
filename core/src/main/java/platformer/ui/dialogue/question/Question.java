package platformer.ui.dialogue.question;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a question in a dialogue, containing multiple possible answers.
 */
@Getter
@Setter
public class Question {

    private final List<Answer> answers;

    public Question() {
        this.answers = new ArrayList<>();
    }

}
