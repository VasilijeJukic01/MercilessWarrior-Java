package platformer.launcher.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class DefaultVBox extends VBox {

    public DefaultVBox(Pos position, Node... children) {
        super(children);
        super.setPadding(new Insets(8));
        super.setSpacing(8);
        super.setAlignment(position);
    }

}
