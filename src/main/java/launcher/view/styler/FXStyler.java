package launcher.view.styler;

import javafx.scene.control.Label;

public class FXStyler implements Styler{

    @Override
    public void setBoldStyle(Object o) {
        if (!(o instanceof Label)) return;
        ((Label)o).getStyleClass().add("bold");
    }

}
