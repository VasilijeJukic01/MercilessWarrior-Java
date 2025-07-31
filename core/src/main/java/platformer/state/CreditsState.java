package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.FilePaths.CREDITS_TXT;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is viewing the credits.
 * In this state, the player can view the credits of the game.
 */
public class CreditsState extends AbstractState implements State {

    private BufferedImage creditsText;
    private SmallButton exitBtn;

    public CreditsState(Game game) {
        super(game);
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.creditsText = ImageUtils.importImage(CREDITS_TXT, CREDITS_TXT_WID, CREDITS_TXT_HEI);
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
        g.drawImage(creditsText, CREDITS_TXT_X, CREDITS_TXT_Y, creditsText.getWidth(), creditsText.getHeight(), null);
        exitBtn.render(g);
        renderInformation(g);
    }

    private void renderInformation(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

        // Game Design & Programming
        g.setColor(Color.WHITE);
        g.drawString("Game Design & Programming: ", CREDITS_POSITION_X, CREDITS_POSITION_Y);
        g.setColor(CREDITS_COLOR);
        g.drawString("Vaske", CREDITS_POSITION_X + g.getFontMetrics().stringWidth("Game Design & Programming: "), CREDITS_POSITION_Y);

        // Art
        g.setColor(Color.WHITE);
        g.drawString("Art: ", CREDITS_POSITION_X, CREDITS_POSITION_Y + CREDITS_SPACING);
        g.setColor(CREDITS_COLOR);
        g.drawString("Dreamir, Maaot, brullov, CreativeKind", CREDITS_POSITION_X + g.getFontMetrics().stringWidth("Art: "), CREDITS_POSITION_Y + CREDITS_SPACING);

        // Music
        g.setColor(Color.WHITE);
        g.drawString("Music: ", CREDITS_POSITION_X, CREDITS_POSITION_Y + CREDITS_SPACING * 2);
        g.setColor(CREDITS_COLOR);
        g.drawString("Mattashi", CREDITS_POSITION_X + g.getFontMetrics().stringWidth("Music: "), CREDITS_POSITION_Y + CREDITS_SPACING * 2);

        // Special Thanks
        g.setColor(Color.WHITE);
        g.drawString("Special Thanks: ", CREDITS_POSITION_X, CREDITS_POSITION_Y + CREDITS_SPACING * 3);
        g.setColor(CREDITS_COLOR);
        g.drawString("Danilo, Kacusa", CREDITS_POSITION_X + g.getFontMetrics().stringWidth("Special Thanks: "), CREDITS_POSITION_Y + CREDITS_SPACING * 3);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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
}
