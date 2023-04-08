package platformer.core;

import platformer.audio.Audio;
import platformer.audio.Songs;
import platformer.state.OptionsState;
import platformer.state.StateManager;
import platformer.ui.AudioOptions;
import platformer.view.GameFrame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
public class Game implements Runnable{

    private GameFrame gameFrame;
    private final Thread gameThread;

    private StateManager stateManager;
    private AudioOptions audioOptions;

    private final int FPS_LOCK = 144;
    private final int UPS_LOCK = 200;
    private int currentFps = 0;
    private int currentUpdates = 0;

    public Game() {
        init();
        this.gameThread = new Thread(this);
        this.gameThread.start();
        Audio.getInstance().getAudioPlayer().playSong(Songs.MENU.ordinal());
    }

    private void init() {
        this.gameFrame = new GameFrame(this);
        this.audioOptions = new AudioOptions();
        this.stateManager = new StateManager(this);
    }

    public void start() {
        this.gameFrame.setVisible(true);
    }

    public void update() {
        stateManager.getCurrentState().update();
    }

    public void render(Graphics g) {
        stateManager.getCurrentState().render(g);
    }

    // Core
    @Override
    public void run() {
        final double timePerFrame = 1000000000.0 / FPS_LOCK;
        final double timePerUpdate = 1000000000.0 / UPS_LOCK;
        long previousTime = System.nanoTime();
        long lastCheck = System.currentTimeMillis();
        int frames = 0, updates = 0;
        double dF = 0, dU = 0;

        while (true) {
            long currentTime = System.nanoTime();

            dU += (currentTime - previousTime) / timePerUpdate;
            dF += (currentTime - previousTime) / timePerFrame;

            previousTime = currentTime;

            if (dU >= 1) {
                this.update();
                updates++;
                dU--;
            }

            if (dF >= 1) {
                this.gameFrame.getGamePanel().repaint();
                frames++;
                dF--;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                currentFps = frames;
                currentUpdates = updates;
                frames = 0;
                updates = 0;
            }
        }
    }

    // Mediator
    public void keyPressed(KeyEvent e) {
        stateManager.getCurrentState().keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
        stateManager.getCurrentState().keyReleased(e);
    }

    public void mousePressed(MouseEvent e) {
        stateManager.getCurrentState().mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        stateManager.getCurrentState().mouseReleased(e);
    }

    public void mouseMoved(MouseEvent e) {
        stateManager.getCurrentState().mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e) {
        stateManager.getCurrentState().mouseDragged(e);
    }

    public void windowFocusLost(WindowEvent e) {
        stateManager.getCurrentState().windowFocusLost(e);
    }

    public void setPaused(boolean value) {
        stateManager.getCurrentState().setPaused(value);
    }

    public void setGameOver(boolean value) {
        stateManager.getCurrentState().setGameOver(value);
    }

    public void setDying(boolean value) {
        stateManager.getCurrentState().setDying(value);
    }

    public void reset() {
        stateManager.getCurrentState().reset();
    }

    public void startMenuState() {
        if (!(stateManager.getCurrentState() instanceof OptionsState)) Audio.getInstance().getAudioPlayer().playSong(Songs.MENU.ordinal());
        stateManager.setMenuState();
    }

    public void startPlayingState() {
        stateManager.setPlayingState();
        Audio.getInstance().getAudioPlayer().playSong(Songs.FOREST_1.ordinal());
    }

    public void startOptionsState() {
        stateManager.setOptionsState();
    }

    public void startQuitState() {
        stateManager.setQuitState();
    }

    // Getters
    public int getCurrentFps() {
        return currentFps;
    }

    public int getCurrentUpdates() {
        return currentUpdates;
    }

    public AudioOptions getAudioOptions() {
        return audioOptions;
    }

}
