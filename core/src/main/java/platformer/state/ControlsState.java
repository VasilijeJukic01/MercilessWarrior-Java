package platformer.state;

import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.CONTROLS_TXT;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is viewing the controls.
 * It includes displaying the controls to the user and handling user interactions within this state.
 */
public class ControlsState extends AbstractState implements State {

    private final KeyboardController kc;
    private BufferedImage controlsText;

    private final SmallButton[] navigationButtons = new SmallButton[2];
    private SmallButton exitBtn;

    private final Map<String, String> commands;
    private final List<String> commandDescriptions;
    private int currentPage = 0;
    private static final int COMMANDS_PER_PAGE = 8;

    public ControlsState(Game game) {
        super(game);
        this.kc = Framework.getInstance().getKeyboardController();
        this.commands = createCommands();
        this.commandDescriptions = new ArrayList<>(commands.keySet());
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.controlsText = Utils.getInstance().importImage(CONTROLS_TXT, CONTROLS_TXT_WID, CONTROLS_TXT_HEI);
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
        this.navigationButtons[0] = new SmallButton(CONTROLS_BTN_PREV_X, CONTROLS_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        this.navigationButtons[1] = new SmallButton(CONTROLS_BTN_NEXT_X, CONTROLS_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
        Arrays.stream(navigationButtons).forEach(SmallButton::update);
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        g.drawImage(controlsText, CONTROLS_TXT_X, CONTROLS_TXT_Y, controlsText.getWidth(), controlsText.getHeight(), null);
        exitBtn.render(g);
        Arrays.stream(navigationButtons).forEach(b -> b.render(g));
        renderControls(g);
    }

    private void renderControls(Graphics g) {
        renderTexts(g);
    }

    private void renderTexts(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

        int start = currentPage * COMMANDS_PER_PAGE;
        int end = Math.min(start + COMMANDS_PER_PAGE, commandDescriptions.size());

        for (int i = start; i < end; i++) {
            String description = commandDescriptions.get(i);
            String command = commands.get(description);
            String[] commandParts = Arrays.stream(command.split("&"))
                    .map(String::trim)
                    .map(part -> {
                        if (part.startsWith("LITERAL:")) return part.substring(8);
                        else return kc.getKeyName(part);
                    })
                    .toArray(String[]::new);

            String descriptionText = description + ": ";
            String commandText = String.join(" & ", commandParts);
            int yPos = CTRL_ROW_TXT_Y + (i - start) * CTRL_TXT_Y_SPACING;

            g.setColor(Color.WHITE);
            g.drawString(descriptionText, CTRL_ROW_TXT_X, yPos);

            g.setColor(TAB_COLOR);
            g.drawString(commandText, CTRL_ROW_TXT_X + g.getFontMetrics().stringWidth(descriptionText), yPos);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        Arrays.stream(navigationButtons)
                .filter(b -> isMouseInButton(e, b))
                .findFirst()
                .ifPresent(b -> b.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.startMenuState();
        }
        for (SmallButton b : navigationButtons) {
            if (isMouseInButton(e, b) && b.isMousePressed()) {
                if (b.getButtonType() == ButtonType.NEXT) nextPage();
                else if (b.getButtonType() == ButtonType.PREV) prevPage();
            }
        }
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(false);
        if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);

        Arrays.stream(navigationButtons).forEach(b -> b.setMouseOver(false));
        Arrays.stream(navigationButtons)
                .filter(b -> isMouseInButton(e, b))
                .findFirst()
                .ifPresent(b -> b.setMouseOver(true));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) game.startMenuState();
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) nextPage();
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) prevPage();
    }

    @Override
    public void reset() {
        exitBtn.resetMouseSet();
        Arrays.stream(navigationButtons).forEach(SmallButton::resetMouseSet);
    }

    private void nextPage() {
        int maxPage = (commandDescriptions.size() - 1) / COMMANDS_PER_PAGE;
        currentPage = Math.min(currentPage + 1, maxPage);
    }

    private void prevPage() {
        currentPage = Math.max(currentPage - 1, 0);
    }

    private static Map<String, String> createCommands() {
        Map<String, String> commands = new LinkedHashMap<>();
        commands.put("Move/Wall Slide", "Move Left & Move Right");
        commands.put("Jump", "Jump");
        commands.put("Dash", "Dash");
        commands.put("Attack", "Attack");
        commands.put("Shield", "Shield");
        commands.put("Interact", "Interact");
        commands.put("Inventory", "Inventory");
        commands.put("Quests", "Quest");
        commands.put("Minimap", "Minimap");
        commands.put("Flames Spell", "Flames");
        commands.put("Fireball Spell", "Fireball");
        commands.put("Transform", "Transform");
        commands.put("Accept", "Accept");
        commands.put("Decline", "Decline");
        commands.put("Minimap Zoom In", "LITERAL:=");
        commands.put("Minimap Zoom Out", "LITERAL:-");
        commands.put("Toggle Minimap Legend", "LITERAL:L");
        commands.put("Pause Game", "Pause");
        return commands;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }

}
