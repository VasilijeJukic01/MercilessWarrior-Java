package platformer.launcher.view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class DefaultHBox extends HBox {

    public DefaultHBox(Pos position, Node... children) {
        super(children);
        super.setPadding(new Insets(8));
        super.setSpacing(8);
        super.setAlignment(position);
    }

}
