package platformer.model.effects.particles;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Procedural ambient falling leaf particle for daytime forest atmosphere.
 */
public class FallingLeafParticle {

    private static final Color[] LEAF_COLORS = {
            new Color(62, 92, 56),
            new Color(101, 140, 70),
            new Color(40, 89, 87),
            new Color(138, 176, 84),
            new Color(168, 132, 62)
    };

    private final int levelWidth, levelHeight;
    private final Random rand = new Random();

    private double baseX, x, y;
    private double xWindDrift, ySpeed;
    private double length, width;
    private double swayPhase, swaySpeed, swayAmplitude;
    private double tumblePhase, tumbleSpeed;
    private float baseAlpha;
    private Color color;

    private final double parallaxFactor = 0.85;

    public FallingLeafParticle(int levelWidth, int levelHeight) {
        this.levelWidth = Math.max(100, levelWidth);
        this.levelHeight = Math.max(100, levelHeight);
        reset(true);
    }

    public void reset(boolean isInitial) {
        this.length = (rand.nextInt(5) + 7) * SCALE;
        this.width = (rand.nextInt(3) + 3.5) * SCALE;

        this.ySpeed = (0.25 + rand.nextDouble() * 0.30) * SCALE;
        this.xWindDrift = (0.12 + rand.nextDouble() * 0.20) * SCALE;

        this.swayPhase = rand.nextDouble() * Math.PI * 2;
        this.swaySpeed = 0.015 + rand.nextDouble() * 0.015;
        this.swayAmplitude = (12 + rand.nextDouble() * 16) * SCALE;

        this.tumblePhase = rand.nextDouble() * Math.PI * 2;
        this.tumbleSpeed = 0.025 + rand.nextDouble() * 0.025;

        this.baseAlpha = 0.55f + rand.nextFloat() * 0.35f;
        this.color = LEAF_COLORS[rand.nextInt(LEAF_COLORS.length)];

        this.baseX = rand.nextInt(this.levelWidth);
        this.y = isInitial ? rand.nextInt(this.levelHeight) : -length * 2 - rand.nextInt(60);
        this.x = this.baseX;
    }

    public void update() {
        baseX += xWindDrift;
        swayPhase += swaySpeed;
        tumblePhase += tumbleSpeed;

        double swayOffset = Math.sin(swayPhase) * swayAmplitude;
        double verticalLift = Math.abs(Math.cos(swayPhase)) * (0.08 * SCALE);

        x = baseX + swayOffset;
        y += Math.max(0.05 * SCALE, ySpeed - verticalLift);

        if (baseX > levelWidth + 100) baseX = -50;
        if (y > levelHeight + 50) reset(false);
    }

    public void render(Graphics2D g2d, int xLevelOffset, int yLevelOffset, float dayVisibility) {
        float finalAlpha = baseAlpha * dayVisibility;
        if (finalAlpha <= 0.02f) return;

        double renderX = this.x - (xLevelOffset * parallaxFactor);
        double renderY = this.y - (yLevelOffset * parallaxFactor);

        Graphics2D gLeaf = (Graphics2D) g2d.create();
        gLeaf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gLeaf.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1.0f, finalAlpha)));
        gLeaf.setColor(this.color);

        double tiltAngle = Math.cos(swayPhase) * 0.55 + 0.25;
        double tumbleScale = Math.max(0.25, Math.abs(Math.sin(tumblePhase)));
        double halfLen = length / 2.0;
        double halfWid = (width / 2.0) * tumbleScale;

        gLeaf.translate(renderX, renderY);
        gLeaf.rotate(tiltAngle);

        Path2D.Double leaf = new Path2D.Double();
        leaf.moveTo(-halfLen, 0);
        leaf.quadTo(0, -halfWid * 1.4, halfLen, 0);
        leaf.quadTo(0, halfWid * 0.9, -halfLen, 0);
        leaf.closePath();

        gLeaf.fill(leaf);
        gLeaf.dispose();
    }
}