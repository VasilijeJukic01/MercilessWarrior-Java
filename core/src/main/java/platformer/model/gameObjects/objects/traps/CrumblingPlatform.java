package platformer.model.gameObjects.objects.traps;

import platformer.audio.Audio;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * A floating one-way moss platform that shakes when stepped on, shatters into 4 falling stone chunks, and reforms after a short cooldown.
 */
public class CrumblingPlatform extends GameObject {

    public enum State {
        IDLE,
        SHAKING,
        CRUMBLED,
        REFORMING
    }

    private static final int SHAKE_DURATION_TICKS = 75;
    private static final int CRUMBLED_DURATION_TICKS = 300;
    private static final int REFORM_DURATION_TICKS = 40;
    private static final int CHUNK_COUNT = 4;

    private State state = State.IDLE;
    private int stateTimer = 0;
    private double floatPhase;

    private BufferedImage trimmedSprite;
    private BufferedImage[] cachedChunks;
    private final double[] chunkOffsetX = new double[CHUNK_COUNT];
    private final double[] chunkOffsetY = new double[CHUNK_COUNT];
    private final double[] chunkVelX = new double[CHUNK_COUNT];
    private final double[] chunkVelY = new double[CHUNK_COUNT];
    private final double[] chunkAngle = new double[CHUNK_COUNT];
    private final double[] chunkAngularVel = new double[CHUNK_COUNT];

    private EffectManager effectManager;
    private final Random rand = new Random();

    public CrumblingPlatform(int xPos, int yPos) {
        super(ObjType.CRUMBLING_PLATFORM, xPos, yPos);
        this.floatPhase = (xPos * 0.05) % (Math.PI * 2.0);
        initHitBox(CRUMBLING_PLAT_HB_WID, CRUMBLING_PLAT_HB_HEI);
        super.xOffset = CRUMBLING_PLAT_OFFSET_X;
        super.yOffset = CRUMBLING_PLAT_OFFSET_Y;
        this.hitBox.x = xPos + xOffset;
        this.hitBox.y = yPos + yOffset;
    }

    public void setEffectManager(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void triggerSteppedOn() {
        if (state == State.IDLE) {
            state = State.SHAKING;
            stateTimer = 0;
        }
    }

    public boolean isSolidActive() {
        return alive && (state == State.IDLE || state == State.SHAKING);
    }

    /**
     * Checks whether this one-way platform can support an entity's feet from above without blocking horizontal dashes or upward jumps.
     */
    public boolean canSupportFeet(Rectangle2D.Double entityBox, double entityAirSpeed) {
        if (!isSolidActive() || entityAirSpeed < 0) return false;
        boolean horizontalOverlap = entityBox.getMaxX() > hitBox.x + (2 * SCALE) && entityBox.x < hitBox.getMaxX() - (2 * SCALE);
        if (!horizontalOverlap) return false;

        double feetY = entityBox.getMaxY();
        double topY = hitBox.y;
        double landingBand = Math.max(hitBox.height, entityAirSpeed + (5 * SCALE));
        return feetY >= topY - (3 * SCALE) && feetY <= topY + landingBand;
    }

    @Override
    public void update() {
        floatPhase += 0.035;

        switch (state) {
            case IDLE -> {
                hitBox.y = yPos + yOffset;
            }
            case SHAKING -> {
                stateTimer++;
                if (stateTimer % 12 == 0 && effectManager != null) {
                    double px = hitBox.x + rand.nextDouble() * hitBox.width;
                    double py = hitBox.getMaxY();
                    effectManager.spawnDustParticles(px, py, 2, DustType.IMPACT, 0, null);
                }
                if (stateTimer >= SHAKE_DURATION_TICKS) {
                    shatterIntoChunks();
                }
            }
            case CRUMBLED -> {
                stateTimer++;
                for (int i = 0; i < CHUNK_COUNT; i++) {
                    chunkOffsetX[i] += chunkVelX[i];
                    chunkOffsetY[i] += chunkVelY[i];
                    chunkVelY[i] += 0.055 * SCALE;
                    chunkAngle[i] += chunkAngularVel[i];
                }
                if (stateTimer >= CRUMBLED_DURATION_TICKS) {
                    state = State.REFORMING;
                    stateTimer = 0;
                }
            }
            case REFORMING -> {
                stateTimer++;
                if (stateTimer >= REFORM_DURATION_TICKS) {
                    state = State.IDLE;
                    stateTimer = 0;
                }
            }
        }
    }

    private void shatterIntoChunks() {
        state = State.CRUMBLED;
        stateTimer = 0;
        Audio.getInstance().getAudioPlayer().playCrateSound();

        if (effectManager != null) {
            effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.getCenterY(), 12, DustType.IMPACT, 0, null);
            effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.y, 8, DustType.HERB_CUT, 0, null);
        }

        for (int i = 0; i < CHUNK_COUNT; i++) {
            chunkOffsetX[i] = 0;
            chunkOffsetY[i] = 0;
            double dirFromCenter = (i - 1.5);
            chunkVelX[i] = dirFromCenter * (0.28 + rand.nextDouble() * 0.25) * SCALE;
            chunkVelY[i] = -(0.35 + rand.nextDouble() * 0.45) * SCALE;
            chunkAngle[i] = 0;
            chunkAngularVel[i] = dirFromCenter * (0.015 + rand.nextDouble() * 0.015);
        }
    }

    private void ensurePrepared(BufferedImage rawSprite) {
        if (trimmedSprite != null || rawSprite == null) return;
        trimmedSprite = trimTransparentBounds(rawSprite);
        cachedChunks = new BufferedImage[CHUNK_COUNT];
        int chunkW = trimmedSprite.getWidth() / CHUNK_COUNT;
        int h = trimmedSprite.getHeight();
        for (int i = 0; i < CHUNK_COUNT; i++) {
            int x = i * chunkW;
            int w = (i == CHUNK_COUNT - 1) ? (trimmedSprite.getWidth() - x) : chunkW;
            cachedChunks[i] = trimmedSprite.getSubimage(x, 0, Math.max(1, w), h);
        }
    }

    private static BufferedImage trimTransparentBounds(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int minX = w, minY = h, maxX = 0, maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >>> 24) & 0xFF) > 16) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (minX > maxX || minY > maxY) return img;
        return img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        if (animations == null || animations.length == 0 || animations[0] == null) return;
        ensurePrepared(animations[0]);

        int floatOffsetY = (state == State.IDLE) ? (int) Math.round(Math.sin(floatPhase) * (1.5 * SCALE)) : 0;
        int baseDrawX = (int) Math.round(hitBox.getCenterX() - (CRUMBLING_PLAT_WID / 2.0) - xLevelOffset);
        int baseDrawY = (int) Math.round(hitBox.y - scale(4) - yLevelOffset + floatOffsetY);

        if (state == State.IDLE) {
            g.drawImage(trimmedSprite, baseDrawX, baseDrawY, CRUMBLING_PLAT_WID, CRUMBLING_PLAT_HEI, null);
        }
        else if (state == State.SHAKING) {
            double progress = (double) stateTimer / SHAKE_DURATION_TICKS;
            double shakeAmp = (1.2 + progress * 2.5) * SCALE;
            int jitterX = (int) Math.round(Math.sin(stateTimer * 1.4) * shakeAmp);
            int jitterY = (int) Math.round(Math.cos(stateTimer * 1.9) * (shakeAmp * 0.4));
            g.drawImage(trimmedSprite, baseDrawX + jitterX, baseDrawY + jitterY, CRUMBLING_PLAT_WID, CRUMBLING_PLAT_HEI, null);
        }
        else if (state == State.CRUMBLED && cachedChunks != null) {
            float alpha = Math.max(0.0f, 1.0f - (stateTimer / 55.0f));
            if (alpha > 0.02f) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                int chunkDrawW = CRUMBLING_PLAT_WID / CHUNK_COUNT;
                for (int i = 0; i < CHUNK_COUNT; i++) {
                    int cx = baseDrawX + (i * chunkDrawW) + (int) chunkOffsetX[i];
                    int cy = baseDrawY + (int) chunkOffsetY[i];
                    AffineTransform old = g2d.getTransform();
                    g2d.translate(cx + chunkDrawW / 2.0, cy + CRUMBLING_PLAT_HEI / 2.0);
                    g2d.rotate(chunkAngle[i]);
                    g2d.drawImage(cachedChunks[i], -chunkDrawW / 2, -CRUMBLING_PLAT_HEI / 2, chunkDrawW, CRUMBLING_PLAT_HEI, null);
                    g2d.setTransform(old);
                }
                g2d.dispose();
            }
        }
        else if (state == State.REFORMING) {
            float progress = Math.min(1.0f, (float) stateTimer / REFORM_DURATION_TICKS);
            int riseOffset = (int) ((1.0f - progress) * 10 * SCALE);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, progress));
            g2d.drawImage(trimmedSprite, baseDrawX, baseDrawY + riseOffset, CRUMBLING_PLAT_WID, CRUMBLING_PLAT_HEI, null);
            g2d.dispose();
        }

        if (isSolidActive()) {
            hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
        }
    }

    @Override
    public void reset() {
        super.reset();
        state = State.IDLE;
        stateTimer = 0;
        hitBox.x = xPos + xOffset;
        hitBox.y = yPos + yOffset;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}