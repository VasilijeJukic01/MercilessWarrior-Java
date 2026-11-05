package platformer.model.effects;

import platformer.core.Game;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.*;

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

    // Cutscene Visuals
    private boolean letterboxActive = false;
    private float letterboxProgress = 0.0f;
    private float cutsceneFadeAlpha = 0.0f;

    // Hit Stop
    private int hitStopDuration = 0;

    private final Random random = new Random();

    public ScreenEffectsManager(Game game) {
        this.game = game;
    }

    public void update() {
        if (updateHitStop()) return;
        updateFlash();
        updateScreenShake();
        updateCutscene();
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
     * Renders cutscene-related visual effects like letterboxing and fade-in/out.
     *
     * @param g The graphics context to draw on.
     */
    public void renderCutsceneEffects(Graphics g) {
        if (cutsceneFadeAlpha > 0.0f) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 0, 0, Math.min(255, (int) (cutsceneFadeAlpha * 255))));
            g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            g2d.dispose();
        }
        if (letterboxProgress > 0.0f) {
            int barHeight = (int) (28 * SCALE * letterboxProgress);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GAME_WIDTH, barHeight);
            g.fillRect(0, GAME_HEIGHT - barHeight, GAME_WIDTH, barHeight);
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

    private void updateCutscene() {
        if (letterboxActive) letterboxProgress = Math.min(1.0f, letterboxProgress + 0.04f);
        else letterboxProgress = Math.max(0.0f, letterboxProgress - 0.04f);
    }

    private boolean updateHitStop() {
        if (hitStopDuration > 0) {
            hitStopDuration--;
            return true;
        }
        return false;
    }

    public void triggerFlash() {
        this.screenFlashAlpha = 200;
    }

    public void triggerShake(int duration, double intensity) {
        if (!game.getSettings().isScreenShake()) return;
        this.screenShakeDuration = Math.max(this.screenShakeDuration, duration);
        this.screenShakeIntensity = Math.max(this.screenShakeIntensity, intensity);
    }

    public void triggerHitStop(int durationFrames) {
        this.hitStopDuration = durationFrames;
    }

    public boolean isHitStopActive() {
        return hitStopDuration > 0;
    }

    public void setLetterboxActive(boolean active) {
        this.letterboxActive = active;
    }

    public float getCutsceneFadeAlpha() {
        return cutsceneFadeAlpha;
    }

    public void setCutsceneFadeAlpha(float alpha) {
        this.cutsceneFadeAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    // Reset
    public void reset() {
        screenFlashAlpha = 0;
        screenShakeDuration = 0;
        screenShakeIntensity = 0;
        letterboxActive = false;
        letterboxProgress = 0.0f;
        cutsceneFadeAlpha = 0.0f;
    }

}