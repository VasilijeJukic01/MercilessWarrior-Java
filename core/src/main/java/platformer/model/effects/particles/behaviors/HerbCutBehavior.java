package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class HerbCutBehavior implements ParticleBehavior {

    private static final Color[] FOLIAGE_PALETTE = {
            new Color(40, 89, 87),
            new Color(62, 92, 56),
            new Color(101, 140, 70),
            new Color(138, 176, 84)
    };

    @Override
    public void init(DustParticle p, Random rand) {
        double bladeLength = (rand.nextInt(7) + 8) * SCALE;
        double bladeWidth = (rand.nextInt(2) + 2.5) * SCALE;
        p.getParticleShape().width = bladeLength;
        p.getParticleShape().height = bladeWidth;

        int dir = p.getFlipSign() != 0 ? p.getFlipSign() : (rand.nextBoolean() ? 1 : -1);
        double slashBias = dir * (rand.nextDouble() * 2.2 + 0.6) * SCALE;
        double randomSpread = (rand.nextDouble() - 0.5) * 1.6 * SCALE;

        p.setXSpeed(slashBias + randomSpread);
        p.setYSpeed(-(rand.nextDouble() * 2.6 + 1.0) * SCALE);
        p.setGravity(0.012 * SCALE);

        p.setAngle(rand.nextDouble() * Math.PI * 2);
        p.setPulsePhase(rand.nextDouble() * Math.PI * 2);
        p.setLineAngle((rand.nextDouble() * 0.06 + 0.04));

        p.setAlphaFadeSpeed(0.0045f);
        p.setCurrentAlpha(1.0f);
        p.setParticleColor(FOLIAGE_PALETTE[rand.nextInt(FOLIAGE_PALETTE.length)]);
    }

    @Override
    public void update(DustParticle p) {
        p.setXSpeed(p.getXSpeed() * 0.94);
        if (p.getYSpeed() < 0) p.setYSpeed(p.getYSpeed() * 0.92 + p.getGravity());
        else {
            double terminalFallSpeed = 0.28 * SCALE;
            p.setYSpeed(Math.min(terminalFallSpeed, p.getYSpeed() + p.getGravity() * 0.5));
        }

        p.setPulsePhase(p.getPulsePhase() + p.getLineAngle());
        double flutterX = Math.sin(p.getPulsePhase()) * (0.38 * SCALE);
        double flutterLift = Math.abs(Math.cos(p.getPulsePhase())) * (0.08 * SCALE);
        p.getParticleShape().x += p.getXSpeed() + flutterX;
        p.getParticleShape().y += p.getYSpeed() - flutterLift;

        double burstSpin = p.getXSpeed() * 0.04;
        double flutterRock = Math.cos(p.getPulsePhase()) * 0.04;
        p.setAngle(p.getAngle() + burstSpin + flutterRock);
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Graphics2D gBlade = (Graphics2D) g2d.create();
        gBlade.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = p.getParticleShape().x - xLevelOffset;
        double cy = p.getParticleShape().y - yLevelOffset;
        double halfLen = p.getParticleShape().width / 2.0;
        double halfWid = p.getParticleShape().height / 2.0;

        double tumbleScale = Math.max(0.25, Math.abs(Math.sin(p.getPulsePhase() * 1.3)));
        double currentHalfWid = halfWid * tumbleScale;

        gBlade.translate(cx, cy);
        gBlade.rotate(p.getAngle());

        Path2D.Double blade = new Path2D.Double();
        blade.moveTo(-halfLen, 0);
        blade.quadTo(0, -currentHalfWid * 1.4, halfLen, 0);
        blade.quadTo(0, currentHalfWid * 0.8, -halfLen, 0);
        blade.closePath();

        gBlade.fill(blade);
        gBlade.dispose();
    }
}