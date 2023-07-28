package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MenuButton;
import platformer.ui.overlays.Overlay;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.SCALE;

@SuppressWarnings("FieldCanBeLocal")
public class MenuState extends StateAbstraction implements State{

    private final MenuButton[] buttons = new MenuButton[4];
    private BufferedImage menuLogo;

    public MenuState(Game game) {
        super(game);
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.menuLogo = Utils.getInstance().importImage("src/main/resources/images/menu/menuLogo.png", 300, 150);
    }

    private void loadButtons() {
        buttons[0] = new MenuButton(GAME_WIDTH / 2, (int)(170*SCALE), ButtonType.PLAY);
        buttons[1] = new MenuButton(GAME_WIDTH / 2, (int)(225*SCALE), ButtonType.OPTIONS);
        buttons[2] = new MenuButton(GAME_WIDTH / 2, (int)(280*SCALE), ButtonType.CONTROLS);
        buttons[3] = new MenuButton(GAME_WIDTH / 2, (int)(335*SCALE), ButtonType.QUIT);
    }

    @Override
    public void update() {
        Overlay.getInstance().update();
        for (MenuButton button : buttons) {
            button.update();
        }
    }

    @Override
    public void render(Graphics g) {
        Overlay.getInstance().renderMenu(g);
        int logoX = (GAME_WIDTH / 3) - (int)(12*SCALE), logoY = (int)(10*SCALE);
        int logoW = (int)(menuLogo.getWidth()*SCALE), logoH = (int)(menuLogo.getHeight()*SCALE);
        g.drawImage(menuLogo, logoX, logoY, logoW, logoH, null);

        for (MenuButton button : buttons) {
            button.render(g);
        }

        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font("Arial", Font.PLAIN, (int)(7.5*SCALE)));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (MenuButton button : buttons) {
            if (isMouseInButton(e, button)) {
                button.setMousePressed(true);
                break;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (MenuButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case PLAY: game.startPlayingState(); break;
                    case OPTIONS: game.startOptionsState(); break;
                    case CONTROLS: game.startControlsState(); break;
                    case QUIT: game.startQuitState(); break;
                    default: break;
                }
                break;
            }
        }
        for (MenuButton button : buttons) {
            button.resetMouseSet();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for (MenuButton button : buttons) {
            button.setMouseOver(false);
        }
        for (MenuButton button : buttons) {
            if (isMouseInButton(e, button)) {
                button.setMouseOver(true);
                break;
            }
        }
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
    public void setPaused(boolean value) {

    }

    @Override
    public void setGameOver(boolean value) {

    }

    @Override
    public void setDying(boolean value) {

    }

    @Override
    public void reset() {

    }
}
