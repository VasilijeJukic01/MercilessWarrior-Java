package platformer.state;

import platformer.audio.Audio;
import platformer.core.Game;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class QuitState extends AbstractState implements State {

    public QuitState(Game game) {
        super(game);
    }

    @Override
    public void update() {
        Audio.getInstance().getAudioPlayer().destroy();
        Logger.getInstance().notify("Destroying.", Message.WARNING);
        System.exit(0);
    }

    @Override
    public void render(Graphics g) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }

    @Override
    public void reset() {

    }

}
