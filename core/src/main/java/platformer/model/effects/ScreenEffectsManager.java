package platformer.model.effects;

import platformer.core.Game;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.GAME_HEIGHT;
import static platformer.constants.Constants.GAME_WIDTH;

/**
 * Manages global, screen-wide visual effects like screen shake and full-screen flashes.
 */
public class ScreenEffectsManager {

    private final Game game;

    // Screen Flash
    private static final int FLASH_FADE_SPEED = 25;
    private int screenFlashAlpha = 0;

    // Screen Shake
    private int screenShakeDuration = 0;
    private double screenShakeIntensity = 0;

    private final Random random = new Random();

    public ScreenEffectsManager(Game game) {
        this.game = game;
    }

    public void update() {
        updateFlash();
        updateScreenShake();
    }

    /**
     * Applies the screen shake translation at the beginning of a render frame.
     * Must be paired with a call to endFrame().
     *
     * @param g The graphics context to translate.
     */
    public void beginFrame(Graphics g) {
        if (screenShakeDuration > 0) {
            int shakeOffsetX = (int) ((random.nextDouble() - 0.5) * screenShakeIntensity);
            int shakeOffsetY = (int) ((random.nextDouble() - 0.5) * screenShakeIntensity);
            g.translate(shakeOffsetX, shakeOffsetY);
        }
    }

    /**
     * Renders the full-screen flash effect. This should be called after rendering the world but before rendering the final UI, depending on desired effect.
     *
     * @param g The graphics context to draw on.
     */
    public void renderFlash(Graphics g) {
        if (screenFlashAlpha > 0) {
            g.setColor(new Color(255, 255, 255, screenFlashAlpha));
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }
    }

    /**
     * Reverts the screen shake translation at the end of a render frame.
     *
     * @param g The graphics context to revert.
     */
    public void endFrame(Graphics g) {
        if (screenShakeDuration > 0) {
            // We let GameState handle the translate reversion.
        }
    }

    private void updateFlash() {
        if (screenFlashAlpha > 0) {
            screenFlashAlpha -= FLASH_FADE_SPEED;
            if (screenFlashAlpha < 0) {
                screenFlashAlpha = 0;
            }
        }
    }

    private void updateScreenShake() {
        if (screenShakeDuration > 0) {
            screenShakeDuration--;
        }
    }

    public void triggerFlash() {
        this.screenFlashAlpha = 200;
    }

    public void triggerShake(int duration, double intensity) {
        if (!game.getSettings().isScreenShake()) return;
        this.screenShakeDuration = Math.max(this.screenShakeDuration, duration);
        this.screenShakeIntensity = Math.max(this.screenShakeIntensity, intensity);
    }

    // Reset
    public void reset() {
        screenFlashAlpha = 0;
        screenShakeDuration = 0;
        screenShakeIntensity = 0;
    }

}