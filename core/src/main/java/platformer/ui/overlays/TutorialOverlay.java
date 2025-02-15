package platformer.ui.overlays;

import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.TUTORIAL_BLOCK_PATH;
import static platformer.constants.UI.*;

/**
 * TutorialOverlay class is an overlay that is displayed when the player is in the tutorial.
 * It displays the tutorial image and the instructions.
 */
public class TutorialOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;

    private final BufferedImage[] tutorialImages;

    public TutorialOverlay(GameState gameState) {
        this.gameState = gameState;
        this.tutorialImages = new BufferedImage[1];
        this.tutorialImages[0] = Utils.getInstance().importImage(TUTORIAL_BLOCK_PATH, -1, -1);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) gameState.setOverlay(null);
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g) {
        KeyboardController kbc = Framework.getInstance().getKeyboardController();
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g.drawImage(tutorialImages[gameState.getTutorialManager().getCurrentTutorial()], TUTORIAL_IMAGE_X, TUTORIAL_IMAGE_Y, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("(Press ESC to exit)", TUTORIAL_EXIT_X, TUTORIAL_EXIT_Y);

        String helpText = switch (gameState.getTutorialManager().getCurrentTutorial()) {
            case 0 -> "You can block the enemy attack by pressing " + kbc.getKeyName("Shield") + " at the right timing.";
            default -> "Follow the instructions.";
        };
        g.drawString(helpText, TUTORIAL_TXT_X, TUTORIAL_TXT_Y);
    }

    @Override
    public void reset() {

    }
}
