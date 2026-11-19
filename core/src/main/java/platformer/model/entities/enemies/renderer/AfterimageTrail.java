package platformer.model.entities.enemies.renderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Per-entity ring buffer that records and renders fading sprite afterimages without sharing mutable state across renderers.
 */
public class AfterimageTrail {

    private final int capacity;
    private final int recordInterval;

    private final int[] worldX;
    private final int[] worldY;
    private final int[] flipSign;
    private final int[] stateOrdinal;
    private final int[] frameIndex;

    private int head = 0;
    private int size = 0;
    private int recordTick = 0;

    public AfterimageTrail(int capacity, int recordInterval) {
        this.capacity = capacity;
        this.recordInterval = recordInterval;
        this.worldX = new int[capacity];
        this.worldY = new int[capacity];
        this.flipSign = new int[capacity];
        this.stateOrdinal = new int[capacity];
        this.frameIndex = new int[capacity];
    }

    /**
     * Records a new ghost snapshot when the interval elapses.
     */
    public void record(int x, int y, int fSign, int stateOrd, int frameIdx) {
        recordTick++;
        if (recordTick >= recordInterval) {
            recordTick = 0;
            worldX[head] = x;
            worldY[head] = y;
            flipSign[head] = fSign;
            stateOrdinal[head] = stateOrd;
            frameIndex[head] = frameIdx;
            head = (head + 1) % capacity;
            if (size < capacity) size++;
        }
    }

    /**
     * Renders all stored afterimage frames from oldest to newest.
     */
    public void render(Graphics g, BufferedImage[][] animations, int drawWidth, int drawHeight, int xLevelOffset, int yLevelOffset, float maxAlpha) {
        if (size == 0 || animations == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        for (int i = size - 1; i >= 0; i--) {
            int idx = (head - 1 - i + capacity) % capacity;
            int sOrd = stateOrdinal[idx];
            int fIdx = frameIndex[idx];
            if (sOrd >= animations.length || animations[sOrd] == null || fIdx >= animations[sOrd].length) continue;

            BufferedImage frame = animations[sOrd][fIdx];
            if (frame == null) continue;

            float alpha = (1.0f - (float) i / capacity) * maxAlpha;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(frame, worldX[idx] - xLevelOffset, worldY[idx] - yLevelOffset, drawWidth * flipSign[idx], drawHeight, null);
        }
        g2d.dispose();
    }

    public void clear() {
        size = 0;
        head = 0;
        recordTick = 0;
    }
}