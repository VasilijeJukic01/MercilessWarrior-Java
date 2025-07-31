package platformer.launcher.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import platformer.launcher.controller.controls.ApplyControlsController;
import platformer.launcher.controller.controls.ResetControlsController;
import platformer.launcher.controller.controls.UniqueKeyController;
import platformer.launcher.core.KeyboardConfigurator;
import platformer.launcher.view.component.DefaultHBox;
import platformer.launcher.view.component.DefaultVBox;
import platformer.launcher.view.styler.FXStyler;
import platformer.launcher.view.styler.Styler;

import java.util.*;

import static platformer.launcher.constants.LauncherConstants.LAUNCHER_STYLES_PATH;

@SuppressWarnings({"FieldCanBeLocal"})
public class ControlsView extends BaseView {

    // Components
    private final DefaultVBox root = new DefaultVBox(Pos.CENTER);
    private final Map<String, TextField> commandFields = new LinkedHashMap<>();
    private final Map<String, Label> commandLabels = new LinkedHashMap<>();
    private final Button btnApply = new Button("Apply");
    private final Button btnReset = new Button("Reset");
    private final Button btnBack = new Button("Back");

    public ControlsView() {
        super.loadImages();
        init();
    }

    private void init() {
        initCommandComponents();
        configureTextFields();
        super.initScene(root, 590, 560, "Controls");
        initRoot();
        initButtons();
        initStyles(super.getScene());
    }

    private void initCommandComponents() {
        String[] commands = {"Move Left", "Move Right", "Jump", "Dash", "Attack", "Flames", "Fireball", "Shield", "Interact",
                "Quest", "Transform", "Inventory", "Accept", "Decline", "Minimap", "Pause", "QuickUse1", "QuickUse2", "QuickUse3", "QuickUse4"};
        KeyboardConfigurator configurator = KeyboardConfigurator.getInstance();

        for (String command : commands) {
            TextField textField = new TextField();
            textField.setText(configurator.getKeyForCommand(command).getName());
            commandFields.put(command, textField);

            Label label = new Label(command + ":");
            label.setPrefWidth(70);
            commandLabels.put(command, label);
        }
    }

    private void configureTextFields() {
        UniqueKeyController uniqueKeyController = new UniqueKeyController(commandFields.values().toArray(new TextField[0]));

        commandFields.values().forEach(textField -> {
            textField.setEditable(false);
            textField.setOnKeyPressed(e -> uniqueKeyController.handleKeyInput(e.getCode(), textField));
        });
    }

    private void initRoot() {
        DefaultVBox leftColumn = new DefaultVBox(Pos.CENTER);
        DefaultVBox rightColumn = new DefaultVBox(Pos.CENTER);

        List<String> commands = new ArrayList<>(commandFields.keySet());
        int midIndex = commands.size() / 2;

        for (int i = 0; i < midIndex; i++) {
            String command = commands.get(i);
            Label label = commandLabels.get(command);
            TextField textField = commandFields.get(command);
            leftColumn.getChildren().add(new DefaultHBox(Pos.CENTER, label, textField));
        }

        for (int i = midIndex; i < commands.size(); i++) {
            String command = commands.get(i);
            Label label = commandLabels.get(command);
            TextField textField = commandFields.get(command);
            rightColumn.getChildren().add(new DefaultHBox(Pos.CENTER, label, textField));
        }

        DefaultHBox columns = new DefaultHBox(Pos.CENTER, leftColumn, rightColumn);
        columns.setSpacing(20);

        root.getChildren().add(columns);
        root.getChildren().add(new DefaultHBox(Pos.CENTER, btnApply, btnReset, btnBack));
    }

    private void initButtons() {
        btnApply.setOnAction(new ApplyControlsController(commandFields.values().toArray(new TextField[0])));
        btnReset.setOnAction(new ResetControlsController(commandFields.values().toArray(new TextField[0])));
        btnBack.setOnAction(e -> super.close());
    }

    private void initStyles(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(LAUNCHER_STYLES_PATH)).toExternalForm());

        Styler styler = new FXStyler();
        List<Label> labels = Arrays.asList(commandLabels.values().toArray(new Label[0]));
        labels.forEach(styler::setBoldStyle);
    }
}