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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.FilePaths.CONTROLS_TXT;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is viewing the controls.
 * It includes displaying the controls to the user and handling user interactions within this state.
 */
// TODO: Implement more pages for controls
public class ControlsState extends AbstractState implements State {

    private final KeyboardController kc;
    private BufferedImage controlsText;

    private SmallButton exitBtn;

    public ControlsState(Game game) {
        super(game);
        this.kc = Framework.getInstance().getKeyboardController();
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.controlsText = Utils.getInstance().importImage(CONTROLS_TXT, CONTROLS_TXT_WID, CONTROLS_TXT_HEI);
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        g.drawImage(controlsText, CONTROLS_TXT_X, CONTROLS_TXT_Y, controlsText.getWidth(), controlsText.getHeight(), null);
        exitBtn.render(g);
        renderControls(g);
    }

    // Render
    private void renderControls(Graphics g) {
        renderTexts(g);
    }

    private void renderTexts(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

        Map<String, String> commands = createCommands();

        AtomicInteger i = new AtomicInteger();
        commands.forEach((description, command) -> {
            String[] commandParts = Arrays.stream(command.split("&"))
                    .map(String::trim)
                    .map(kc::getKeyName)
                    .toArray(String[]::new);

            String descriptionText = description + ": ";
            String commandText = String.join(" & ", commandParts);

            g.setColor(Color.WHITE);
            g.drawString(descriptionText, CTRL_ROW_TXT_X, CTRL_ROW_TXT_Y + i.get() * CTRL_TXT_Y_SPACING);

            g.setColor(Color.YELLOW);
            g.drawString(commandText, CTRL_ROW_TXT_X + g.getFontMetrics().stringWidth(descriptionText), CTRL_ROW_TXT_Y + i.get() * CTRL_TXT_Y_SPACING);

            i.incrementAndGet();
        });
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.startMenuState();
        }
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(false);
        if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) game.startMenuState();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }

    @Override
    public void reset() {
        exitBtn.resetMouseSet();
    }

    private static Map<String, String> createCommands() {
        Map<String, String> commands = new LinkedHashMap<>();
        commands.put("Move/Wall Slide", "Move Left & Move Right");
        commands.put("Jump", "Jump");
        commands.put("Dash", "Dash");
        commands.put("Attack", "Attack");
        commands.put("Shield", "Shield");
        commands.put("Flames", "Flames");
        commands.put("Fireball", "Fireball");
        commands.put("Transform", "Transform");
        commands.put("Interact", "Interact");
        commands.put("Inventory", "Inventory");
        return commands;
    }

}
