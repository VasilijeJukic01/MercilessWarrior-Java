package platformer.ui.dialogue;

import lombok.Getter;
import platformer.ui.dialogue.question.Question;

import java.util.List;

/**
 * Represents a dialogue in the game, containing lines of text and a question.
 */
@Getter
public class Dialogue {

    private String object;
    private String id;
    private boolean once;
    private List<String> lines;
    private Question question;

    private boolean activated;

    /**
     * Sets the dialogue as activated if it is marked to be activated only once and has not been activated yet.
     */
    public void setActivated() {
        if (once && !activated) {
            this.activated = true;
        }
    }
}