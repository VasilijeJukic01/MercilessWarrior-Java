package platformer.ui.dialogue.question;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Question {

    private final List<Answer> answers;

    public Question() {
        this.answers = new ArrayList<>();
    }

}
