package platformer.state;

import platformer.animation.AnimationUtils;
import platformer.model.Tiles;
import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MenuButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

@SuppressWarnings("FieldCanBeLocal")
public class MenuState extends StateAbstraction implements State{

    private final MenuButton[] buttons = new MenuButton[3];
    private final BufferedImage[] background;
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0;
    private BufferedImage menuLogo;

    public MenuState(Game game) {
        super(game);
        this.background = AnimationUtils.getInstance().loadMenuAnimation();
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.menuLogo = Utils.getInstance().importImage("src/main/resources/images/menu/menuLogo.png", 300, 150);
    }

    private void loadButtons() {
        buttons[0] = new MenuButton((int)(Tiles.GAME_WIDTH.getValue() / 2), (int)(170*Tiles.SCALE.getValue()), ButtonType.PLAY);
        buttons[1] = new MenuButton((int)(Tiles.GAME_WIDTH.getValue() / 2), (int)(240*Tiles.SCALE.getValue()), ButtonType.OPTIONS);
        buttons[2] = new MenuButton((int)(Tiles.GAME_WIDTH.getValue() / 2), (int)(310*Tiles.SCALE.getValue()), ButtonType.QUIT);
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= 24) {
                animIndex = 0;
            }
        }
    }

    @Override
    public void update() {
        updateAnimation();
        for (MenuButton button : buttons) {
            button.update();
        }
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(background[animIndex], 0, 0,null);
        int logoX = (int)(Tiles.GAME_WIDTH.getValue() / 3)- (int)(12*Tiles.SCALE.getValue()), logoY = (int)(10*Tiles.SCALE.getValue());
        int logoW = (int)(menuLogo.getWidth()*Tiles.SCALE.getValue()), logoH = (int)(menuLogo.getHeight()*Tiles.SCALE.getValue());
        g.drawImage(menuLogo, logoX, logoY, logoW, logoH, null);

        for (MenuButton button : buttons) {
            button.render(g);
        }

        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font("Arial", Font.PLAIN, (int)(7.5*Tiles.SCALE.getValue())));
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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
