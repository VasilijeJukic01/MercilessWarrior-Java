package platformer.ui.options;

import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.state.types.ControlsState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.Constants.SMALL_BTN_SIZE;
import static platformer.constants.UI.*;

/**
 * A panel responsible for displaying and navigating game controls.
 * This component is designed to be reusable in different UI contexts (e.g., main menu, pause menu).
 */
public class ControlsPanel {

    private final KeyboardController kc;
    private final SmallButton[] navigationButtons = new SmallButton[2];
    private final Class<?> embeddedClass;

    private final Map<String, String> commands;
    private final List<String> commandDescriptions;
    private int currentPage = 0;
    private static final int COMMANDS_PER_PAGE = 8;

    public ControlsPanel(Class<?> embeddedClass) {
        this.embeddedClass = embeddedClass;
        this.kc = Framework.getInstance().getKeyboardController();
        this.commands = createCommands();
        this.commandDescriptions = new ArrayList<>(commands.keySet());
        loadButtons();
    }

    private void loadButtons() {
        this.navigationButtons[0] = new SmallButton(CONTROLS_BTN_PREV_X, CONTROLS_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        this.navigationButtons[1] = new SmallButton(CONTROLS_BTN_NEXT_X, CONTROLS_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
    }

    // Core
    public void update() {
        Arrays.stream(navigationButtons).forEach(SmallButton::update);
    }

    public void render(Graphics g) {
        Arrays.stream(navigationButtons).forEach(b -> b.render(g));
        renderTexts(g);
    }

    private void renderTexts(Graphics g) {
        int yStart;
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
            if (embeddedClass == ControlsState.class) yStart = OPTIONS_CTRL_ROW_TXT_Y;
            else yStart = PAUSE_CTRL_ROW_TXT_Y;
            int yPos = yStart + (i - start) * CTRL_TXT_Y_SPACING;

            g.setColor(Color.WHITE);
            g.drawString(descriptionText, CTRL_ROW_TXT_X, yPos);

            g.setColor(TAB_COLOR);
            g.drawString(commandText, CTRL_ROW_TXT_X + g.getFontMetrics().stringWidth(descriptionText), yPos);
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) nextPage();
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) prevPage();
    }

    public void mousePressed(MouseEvent e) {
        for (SmallButton b : navigationButtons) {
            if (isMouseInButton(e, b)) b.setMousePressed(true);
        }
    }

    public void mouseReleased(MouseEvent e) {
        for (SmallButton b : navigationButtons) {
            if (isMouseInButton(e, b) && b.isMousePressed()) {
                if (b.getButtonType() == ButtonType.NEXT) nextPage();
                else if (b.getButtonType() == ButtonType.PREV) prevPage();
            }
        }
        reset();
    }

    public void mouseMoved(MouseEvent e) {
        for (SmallButton b : navigationButtons) {
            b.setMouseOver(isMouseInButton(e, b));
        }
    }

    private boolean isMouseInButton(MouseEvent e, SmallButton b) {
        return b.getButtonHitBox().contains(e.getX(), e.getY());
    }

    public void reset() {
        Arrays.stream(navigationButtons).forEach(SmallButton::resetMouseSet);
    }

    public void nextPage() {
        int maxPage = (commandDescriptions.size() - 1) / COMMANDS_PER_PAGE;
        currentPage = Math.min(currentPage + 1, maxPage);
    }

    public void prevPage() {
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

}
