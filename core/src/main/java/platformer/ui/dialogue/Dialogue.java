package platformer.ui.dialogue;

import lombok.Getter;

import java.util.List;

@Getter
public class Dialogue {

    private String object;
    private String id;
    private boolean once;
    private List<String> lines;

    private boolean activated;

    public void setActivated() {
        if (once && !activated) {
            this.activated = true;
        }
    }
}