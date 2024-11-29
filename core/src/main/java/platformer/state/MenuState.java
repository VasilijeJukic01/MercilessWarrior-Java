package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.BigButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.LeaderboardButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.MENU_LOGO;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is in the main menu.
 * In this state, the player can navigate to different parts of the game such as play, options, controls, leaderboard, and quit.
 */
@SuppressWarnings("FieldCanBeLocal")
public class MenuState extends AbstractState implements State {

    private final BigButton[] buttons = new BigButton[4];
    private LeaderboardButton leaderboardBtn;
    private BufferedImage menuLogo;

    public MenuState(Game game) {
        super(game);
        loadImages();
        loadButtons();
    }

    // Init
    private void loadImages() {
        this.menuLogo = Utils.getInstance().importImage(MENU_LOGO, MENU_LOGO_WID, MENU_LOGO_HEI);
    }

    private void loadButtons() {
        buttons[0] = new BigButton(MENU_BTN_X, MENU_BTN1_Y, BIG_BTN_WID, BIG_BTN_HEI, ButtonType.PLAY);
        buttons[1] = new BigButton(MENU_BTN_X, MENU_BTN2_Y, BIG_BTN_WID, BIG_BTN_HEI, ButtonType.OPTIONS);
        buttons[2] = new BigButton(MENU_BTN_X, MENU_BTN3_Y, BIG_BTN_WID, BIG_BTN_HEI, ButtonType.CONTROLS);
        buttons[3] = new BigButton(MENU_BTN_X, MENU_BTN4_Y, BIG_BTN_WID, BIG_BTN_HEI, ButtonType.QUIT);
        this.leaderboardBtn = new LeaderboardButton(L_BUTTON_X, L_BUTTON_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.L_BOARD);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        Arrays.stream(buttons).forEach(BigButton::update);
        leaderboardBtn.update();
    }

    @Override
    public void render(Graphics g) {
        renderMenuImages(g);
        renderMenuButtons(g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    // Render
    private void renderMenuImages(Graphics g) {
        OverlayLayer.getInstance().renderMenu(g);
        g.drawImage(menuLogo, MENU_LOGO_X, MENU_LOGO_Y, MENU_LOGO_WID, MENU_LOGO_HEI, null);
    }

    private void renderMenuButtons(Graphics g) {
        Arrays.stream(buttons).forEach(buttons -> buttons.render(g));
        leaderboardBtn.render(g);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, leaderboardBtn)) leaderboardBtn.setMousePressed(true);
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, leaderboardBtn) && leaderboardBtn.isMousePressed()) {
            game.startLeaderboardState();
        }
        for (BigButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case PLAY:
                        game.startChoseGameState(); break;
                    case OPTIONS:
                        game.startOptionsState(); break;
                    case CONTROLS:
                        game.startControlsState(); break;
                    case QUIT:
                        game.startQuitState(); break;
                    default: break;
                }
                break;
            }
        }
        Arrays.stream(buttons).forEach(AbstractButton::resetMouseSet);
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        leaderboardBtn.setMouseOver(false);
        if (isMouseInButton(e, leaderboardBtn)) leaderboardBtn.setMouseOver(true);

        Arrays.stream(buttons).forEach(bigButton -> bigButton.setMouseOver(false));
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            game.startPlayingState();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }

    @Override
    public void reset() {
        leaderboardBtn.resetMouseSet();
    }
}
