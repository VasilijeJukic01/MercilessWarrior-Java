package platformer.core;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.state.*;
import platformer.ui.AudioOptions;
import platformer.ui.overlays.OverlayLayer;
import platformer.view.GameFrame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import static platformer.constants.Constants.*;

/**
 * Entry point for the game.
 * It initializes and manages the game's core components such as the GameFrame, StateManager, and AudioOptions.
 * <p>
 * The class interacts with the StateManager and Audio.
 */
@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
public class Game implements Runnable {

    private GameFrame gameFrame;
    private final Thread gameThread;

    private StateManager stateManager;
    private AudioOptions audioOptions;

    private final int FPS_LOCK = 144;
    private final int UPS_LOCK = 200;
    private int currentFps = 0;
    private int currentUpdates = 0;

    private float originalScale;
    private int originalGameWidth;
    private int originalGameHeight;

    public Game() {
        init();
        this.gameThread = new Thread(this);
        this.gameThread.start();
        Audio.getInstance().getAudioPlayer().playSong(Song.MENU);
    }

    private void init() {
        this.originalScale = SCALE;
        this.originalGameWidth = GAME_WIDTH;
        this.originalGameHeight = GAME_HEIGHT;
        this.gameFrame = new GameFrame(this);
        this.audioOptions = new AudioOptions();
        this.stateManager = new StateManager(this);
        OverlayLayer.getInstance().update(); // Prepare instance
    }

    public void start() {
        this.gameFrame.setVisible(true);
    }

    public void update() {
        stateManager.getCurrentState().update();
    }

    /**
     * Renders the current state of the game.
     * If the game is in full screen mode, it scales the graphics context to fit the current size of the game panel.
     * Otherwise, it renders the state directly.
     *
     * @param g the Graphics object used for rendering
     */
    public void render(Graphics g) {
        if (isFullScreen()) {
            Graphics2D g2d = (Graphics2D) g;
            Dimension currentSize = gameFrame.getGamePanel().getCurrentSize();
            double scaleX = (double) currentSize.width / originalGameWidth;
            double scaleY = (double) currentSize.height / originalGameHeight;

            g2d.scale(scaleX, scaleY);
            stateManager.getCurrentState().render(g2d);
            g2d.scale(1 / scaleX, 1 / scaleY);
        }
        else stateManager.getCurrentState().render(g);
    }

    /**
     * Toggles between windowed mode and full screen mode.
     */
    public void toggleFullScreen() {
        if (gameFrame != null) gameFrame.toggleFullScreen();
    }

    /**
     * Checks if the game is currently in full screen mode.
     * @return true if the game is in full screen mode, false otherwise
     */
    public boolean isFullScreen() {
        return gameFrame != null && gameFrame.isFullScreen();
    }

    // Core
    /**
     * Controlling the game's frame rate (FPS) and update speed (UPS).
     * It ensures that the game updates and renders at a consistent speed on different hardware.
     * <p>
     * The method calculates the time it should take for each frame and update based on the FPS_LOCK and UPS_LOCK values.
     * It then enters an infinite loop where it continuously checks the elapsed time, and if enough time has passed, it calls the update or render methods.
     * This loop continues until the game is closed.
     * <p>
     * This method is run in a separate thread when the game is started.
     */
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
        if (e.getKeyCode() == KeyEvent.VK_F11) {
            toggleFullScreen();
            return;
        }
        stateManager.getCurrentState().keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
        stateManager.getCurrentState().keyReleased(e);
    }

    public void mouseClicked(MouseEvent e) {
        stateManager.getCurrentState().mouseClicked(e);
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

    public void reset() {
        stateManager.getCurrentState().reset();
    }

    public void startMenuState() {
        State currentState = stateManager.getCurrentState();
        if (!(currentState instanceof OptionsState || currentState instanceof ControlsState || currentState instanceof LeaderboardState
                || currentState instanceof CreditsState || currentState instanceof ChoseGameState))
            Audio.getInstance().getAudioPlayer().playSong(Song.MENU);
        stateManager.setMenuState();
    }

    public void startPlayingState() {
        stateManager.setPlayingState();
        Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
    }

    public void startOptionsState() {
        stateManager.setOptionsState();
    }

    public void startControlsState() {
        stateManager.setControlsState();
    }

    public void startLeaderboardState() {
        stateManager.setLeaderboardState();
    }

    public void startChoseGameState() {
        stateManager.setChoseGameState();
    }

    public void startCreditsState() {
        stateManager.setCreditsState();
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

    public State getCurrentState() {
        return stateManager.getCurrentState();
    }

}
