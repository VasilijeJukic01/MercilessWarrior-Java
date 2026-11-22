package platformer.core;

import platformer.state.*;
import platformer.ui.AudioOptions;
import platformer.ui.overlays.OverlayLayer;
import platformer.view.GameFrame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.concurrent.atomic.AtomicInteger;

import static platformer.constants.Constants.*;

/**
 * Entry point for the game.
 * Manages the fixed-timestep 200 UPS simulation loop and 144 FPS rendering cadence.
 */
@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
public class Game implements Runnable {

    private GameFrame gameFrame;
    private final Thread gameThread;

    private StateManager stateManager;
    private AudioOptions audioOptions;

    private final int FPS_LOCK = 144;
    private final int UPS_LOCK = 200;
    private volatile int currentFps = 0;
    private final AtomicInteger frameCountForDisplay = new AtomicInteger(0);
    private volatile int currentUpdates = 0;

    private int originalGameWidth;
    private int originalGameHeight;

    public Game() {
        init();
        this.gameThread = new Thread(this, "MW-GameLoop");
    }

    private void init() {
        this.originalGameWidth = GAME_WIDTH;
        this.originalGameHeight = GAME_HEIGHT;
        this.gameFrame = new GameFrame(this);
        this.audioOptions = new AudioOptions();
        this.stateManager = new StateManager(this);
        OverlayLayer.getInstance().update();
    }

    public void start() {
        this.gameFrame.setVisible(true);
        if (!this.gameThread.isAlive()) {
            this.gameThread.start();
        }
    }

    public synchronized void update() {
        stateManager.getCurrentState().update();
    }

    public synchronized void render(Graphics g) {
        if (isFullScreen()) {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform oldTransform = g2d.getTransform();
            Dimension currentSize = gameFrame.getGamePanel().getCurrentSize();
            double scaleX = (double) currentSize.width / originalGameWidth;
            double scaleY = (double) currentSize.height / originalGameHeight;

            g2d.scale(scaleX, scaleY);
            stateManager.getCurrentState().render(g2d);
            g2d.setTransform(oldTransform);
        }
        else stateManager.getCurrentState().render(g);
    }

    public void toggleFullScreen() {
        if (gameFrame != null) gameFrame.toggleFullScreen();
    }

    public boolean isFullScreen() {
        return gameFrame != null && gameFrame.isFullScreen();
    }

    @Override
    public void run() {
        final double timePerFrame = 1_000_000_000.0 / FPS_LOCK;
        final double timePerUpdate = 1_000_000_000.0 / UPS_LOCK;
        long previousTime = System.nanoTime();
        long lastCheck = System.currentTimeMillis();
        int updates = 0;
        double dF = 0, dU = 0;

        while (true) {
            long currentTime = System.nanoTime();
            long elapsedNs = currentTime - previousTime;
            previousTime = currentTime;

            if (elapsedNs > 250_000_000L) {
                elapsedNs = 250_000_000L;
            }

            dU += elapsedNs / timePerUpdate;
            dF += elapsedNs / timePerFrame;

            while (dU >= 1) {
                this.update();
                updates++;
                dU--;
            }

            if (dF >= 1) {
                this.gameFrame.getGamePanel().repaint();
                dF -= Math.floor(dF);
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                currentFps = frameCountForDisplay.getAndSet(0);
                currentUpdates = updates;
                updates = 0;
            }

            if (dU < 0.7 && dF < 0.7) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
            else {
                Thread.onSpinWait();
            }
        }
    }

    public void frameRendered() {
        frameCountForDisplay.incrementAndGet();
    }

    // Mediator
    public synchronized void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F11) {
            toggleFullScreen();
            return;
        }
        stateManager.getCurrentState().keyPressed(e);
    }

    public synchronized void keyReleased(KeyEvent e) {
        stateManager.getCurrentState().keyReleased(e);
    }

    public synchronized void mouseClicked(MouseEvent e) {
        stateManager.getCurrentState().mouseClicked(e);
    }

    public synchronized void mousePressed(MouseEvent e) {
        stateManager.getCurrentState().mousePressed(e);
    }

    public synchronized void mouseReleased(MouseEvent e) {
        stateManager.getCurrentState().mouseReleased(e);
    }

    public synchronized void mouseMoved(MouseEvent e) {
        stateManager.getCurrentState().mouseMoved(e);
    }

    public synchronized void mouseDragged(MouseEvent e) {
        stateManager.getCurrentState().mouseDragged(e);
    }

    public synchronized void windowFocusLost(WindowEvent e) {
        stateManager.getCurrentState().windowFocusLost(e);
    }

    public synchronized void reset() {
        stateManager.getCurrentState().reset();
    }

    public synchronized void startMenuState() {
        stateManager.setState(StateType.MENU);
    }

    public synchronized void startPlayingState() {
        stateManager.setState(StateType.PLAYING);
    }

    public synchronized void startOptionsState() {
        stateManager.setState(StateType.OPTIONS);
    }

    public synchronized void startControlsState() {
        stateManager.setState(StateType.CONTROLS);
    }

    public synchronized void startLeaderboardState() {
        stateManager.setState(StateType.LEADERBOARD);
    }

    public synchronized void startChoseGameState() {
        stateManager.setState(StateType.CHOOSE_GAME);
    }

    public synchronized void startCreditsState() {
        stateManager.setState(StateType.CREDITS);
    }

    public synchronized void startQuitState() {
        stateManager.setState(StateType.QUIT);
    }

    public int getCurrentFps() {
        return currentFps;
    }

    public int getCurrentUpdates() {
        return currentUpdates;
    }

    public AudioOptions getAudioOptions() {
        return audioOptions;
    }

    public Settings getSettings() {
        return Settings.getInstance();
    }

    public State getCurrentState() {
        return stateManager.getCurrentState();
    }

}
