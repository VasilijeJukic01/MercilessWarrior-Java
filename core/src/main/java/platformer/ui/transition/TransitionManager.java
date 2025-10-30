package platformer.ui.transition;

import platformer.audio.Audio;
import platformer.audio.types.Sound;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Random;

import static platformer.constants.Constants.GAME_HEIGHT;
import static platformer.constants.Constants.GAME_WIDTH;

/**
 * Manages a directional screen wipe transition for level changes.
 * The transition consists of two phases: FADE_OUT (screen is covered) and FADE_IN (screen is revealed).
 */
public class TransitionManager {

    private enum State {
        INACTIVE, FADE_OUT, FADE_IN
    }

    private State state = State.INACTIVE;
    private TransitionDirection direction;
    private float progress = 0.0f;
    private final float speed = 0.025f;
    private final Random random = new Random();

    private final float curveFactor = 150.0f;

    private Runnable onFadeOutComplete;

    public void startTransition(TransitionDirection direction, Runnable onFadeOutComplete) {
        if (state == State.INACTIVE) {
            this.direction = direction;
            this.progress = 0.0f;
            this.state = State.FADE_OUT;
            this.onFadeOutComplete = onFadeOutComplete;
            Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
        }
    }

    public void update() {
        if (state != State.INACTIVE) {
            progress += speed;
            if (progress >= 1.0f) {
                if (state == State.FADE_OUT) {
                    if (onFadeOutComplete != null) {
                        onFadeOutComplete.run();
                        onFadeOutComplete = null;
                    }
                    progress = 0.0f;
                    state = State.FADE_IN;
                }
                else {
                    progress = 1.0f;
                    state = State.INACTIVE;
                }
            }
        }
    }

    public void render(Graphics g) {
        if (state == State.INACTIVE) return;
        Graphics2D g2d = (Graphics2D) g.create();
        float easedProgress = easeInOutCubic(progress);
        renderWipe(g2d, easedProgress);
        renderNoise(g2d, easedProgress);
        g2d.dispose();
    }

    /**
     * Renders the main wipe effect using a curved path and a gradient for a soft fade.
     */
    private void renderWipe(Graphics2D g2d, float easedProgress) {
        float renderProgress = (state == State.FADE_IN) ? 1.0f - easedProgress : easedProgress;

        // The total distance to travel -> screen size + curve amount
        float totalWidth = GAME_WIDTH + curveFactor;
        float totalHeight = GAME_HEIGHT + curveFactor;

        GeneralPath path = new GeneralPath();
        GradientPaint paint = null;
        Color leadingEdgeColor = new Color(0, 0, 0, 150);
        Color trailingEdgeColor = new Color(0, 0, 0, 255);

        switch (direction) {
            case FROM_LEFT: {
                float wipeX = renderProgress * totalWidth;
                path.moveTo(0, 0);
                path.lineTo(wipeX - curveFactor, 0);
                path.quadTo(wipeX, GAME_HEIGHT / 2.0f, wipeX - curveFactor, GAME_HEIGHT);
                path.lineTo(0, GAME_HEIGHT);
                path.closePath();
                paint = new GradientPaint(wipeX - curveFactor, 0, trailingEdgeColor, wipeX, 0, leadingEdgeColor);
                break;
            }
            case FROM_RIGHT: {
                float wipeX = GAME_WIDTH - (renderProgress * totalWidth);
                path.moveTo(GAME_WIDTH, 0);
                path.lineTo(wipeX + curveFactor, 0);
                path.quadTo(wipeX, GAME_HEIGHT / 2.0f, wipeX + curveFactor, GAME_HEIGHT);
                path.lineTo(GAME_WIDTH, GAME_HEIGHT);
                path.closePath();
                paint = new GradientPaint(wipeX + curveFactor, 0, trailingEdgeColor, wipeX, 0, leadingEdgeColor);
                break;
            }
            case FROM_TOP: {
                float wipeY = renderProgress * totalHeight;
                path.moveTo(0, 0);
                path.lineTo(0, wipeY - curveFactor);
                path.quadTo(GAME_WIDTH / 2.0f, wipeY, GAME_WIDTH, wipeY - curveFactor);
                path.lineTo(GAME_WIDTH, 0);
                path.closePath();
                paint = new GradientPaint(0, wipeY - curveFactor, trailingEdgeColor, 0, wipeY, leadingEdgeColor);
                break;
            }
            case FROM_BOTTOM: {
                float wipeY = GAME_HEIGHT - (renderProgress * totalHeight);
                path.moveTo(0, GAME_HEIGHT);
                path.lineTo(0, wipeY + curveFactor);
                path.quadTo(GAME_WIDTH / 2.0f, wipeY, GAME_WIDTH, wipeY + curveFactor);
                path.lineTo(GAME_WIDTH, GAME_HEIGHT);
                path.closePath();
                paint = new GradientPaint(0, wipeY + curveFactor, trailingEdgeColor, 0, wipeY, leadingEdgeColor);
                break;
            }
        }

        if (paint != null) {
            g2d.setPaint(paint);
            g2d.fill(path);
        }
    }

    private void renderNoise(Graphics2D g2d, float easedProgress) {
        int particleCount = 200;
        float alpha = 0.1f;

        if (state == State.FADE_IN) {
            alpha *= (1.0f - easedProgress);
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        for (int i = 0; i < particleCount; i++) {
            int gray = random.nextInt(50) + 20;
            g2d.setColor(new Color(gray, gray, gray));
            int x = random.nextInt(GAME_WIDTH);
            int y = random.nextInt(GAME_HEIGHT);
            int size = random.nextInt(3) + 1;
            g2d.fillRect(x, y, size, size);
        }
    }

    private float easeInOutCubic(float t) {
        return (float) (t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2);
    }

    public boolean isActive() {
        return state != State.INACTIVE;
    }
}