package platformer.ui.dialogue.question;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Answer {

    private String answer;
    private String next;

    public Answer(String answer, String next) {
        this.answer = answer;
        this.next = next;
    }

}
