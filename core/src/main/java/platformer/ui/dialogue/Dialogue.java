package platformer.ui.dialogue;

import lombok.Getter;
import platformer.ui.dialogue.question.Question;

import java.util.List;

@Getter
public class Dialogue {

    private String object;
    private String id;
    private boolean once;
    private List<String> lines;
    private Question question;

    private boolean activated;

    public void setActivated() {
        if (once && !activated) {
            this.activated = true;
        }
    }
}