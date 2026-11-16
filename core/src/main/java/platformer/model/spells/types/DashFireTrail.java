package platformer.model.spells.types;

import platformer.animation.Anim;
import platformer.core.GameContext;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.inventory.InventoryBonus;
import platformer.model.perks.PerksBonus;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * A lingering fire hazard left behind when the player dashes while transformed.
 * Renders a continuous plasma-fire beam in midair, and individual swaying flame tongues on the ground.
 */
public class DashFireTrail extends Spell {

    private static final int POST_DASH_LIFETIME = 95;
    private static final int DAMAGE_TICK_INTERVAL = 18;
    private static final int SAMPLE_POINTS = 28;
    private static final int MAX_GROUND_TONGUES = 48;
    private static final int EMBER_COUNT = 22;

    private final GameContext context;
    private final boolean aerial;
    private final int flipSign;

    private double startX, endX;
    private final double baseY;
    private boolean dashFinished = false;
    private int postDashTick = 0;
    private int damageTick = 0;
    private double waveTime = 0.0;

    // Properties
    private final double[] tongueWidth = new double[MAX_GROUND_TONGUES];
    private final double[] tongueHeight = new double[MAX_GROUND_TONGUES];
    private final double[] tonguePhase = new double[MAX_GROUND_TONGUES];
    private final double[] tongueSpeed = new double[MAX_GROUND_TONGUES];
    private final double[] tongueJitterX = new double[MAX_GROUND_TONGUES];

    private final double[] emberNormX = new double[EMBER_COUNT];
    private final double[] emberOffsetY = new double[EMBER_COUNT];
    private final double[] emberSpeedY = new double[EMBER_COUNT];
    private final double[] emberPhase = new double[EMBER_COUNT];
    private final int[] emberSize = new int[EMBER_COUNT];

    public DashFireTrail(int startX, int anchorY, boolean aerial, int flipSign, GameContext context) {
        super(SpellType.DASH_FIRE_TRAIL, startX, anchorY - scale(14), scale(8), scale(28));
        this.context = context;
        this.aerial = aerial;
        this.flipSign = (flipSign == 0) ? 1 : flipSign;
        this.startX = startX;
        this.endX = startX + this.flipSign * scale(8);
        this.baseY = aerial ? (anchorY + scale(6)) : anchorY;
        initHitBox(scale(8), scale(28));
        initGroundTongues();
        initEmbers();
        updateBounds();
    }

    // Init
    private void initGroundTongues() {
        Random rand = new Random();
        for (int i = 0; i < MAX_GROUND_TONGUES; i++) {
            tongueWidth[i] = (5.5 + rand.nextDouble() * 3.5) * SCALE;
            tongueHeight[i] = (12.0 + rand.nextDouble() * 9.5) * SCALE;
            tonguePhase[i] = rand.nextDouble() * Math.PI * 2.0;
            tongueSpeed[i] = 0.14 + rand.nextDouble() * 0.08;
            tongueJitterX[i] = (rand.nextDouble() - 0.5) * scale(2.5);
        }
    }

    private void initEmbers() {
        Random rand = new Random();
        for (int i = 0; i < EMBER_COUNT; i++) {
            emberNormX[i] = rand.nextDouble();
            double side = rand.nextBoolean() ? 1.0 : -1.0;
            emberOffsetY[i] = side * (scale(5) + rand.nextDouble() * scale(9));
            emberSpeedY[i] = (0.18 + rand.nextDouble() * 0.35) * SCALE;
            emberPhase[i] = rand.nextDouble() * Math.PI * 2.0;
            emberSize[i] = Math.max(2, scale(1.6 + rand.nextDouble() * 1.8));
        }
    }

    private void updateBounds() {
        double minX = Math.min(startX, endX), maxX = Math.max(startX, endX);
        double spanW = Math.max(scale(8), maxX - minX), beamH = aerial ? scale(24) : scale(22);
        hitBox.x = minX;
        hitBox.width = spanW;
        hitBox.y = aerial ? (baseY - beamH * 0.5) : (baseY - beamH);
        hitBox.height = beamH;
    }

    // Core
    @Override
    public void update(Player player) {
        if (!active) return;
        waveTime += 0.16;
        if (!aerial) {
            for (int i = 0; i < MAX_GROUND_TONGUES; i++) {
                tonguePhase[i] += tongueSpeed[i];
            }
        }
        if (!dashFinished) {
            if (player.checkAction(PlayerAction.DASH) && player.checkAction(PlayerAction.TRANSFORM)) {
                this.endX = player.getHitBox().getCenterX() - flipSign * scale(4);
                updateBounds();
                context.getObjectManager().checkObjectBreak(hitBox);
                context.getLevelManager().checkDecorationCut(hitBox);
            }
            else dashFinished = true;
        }
        else {
            postDashTick++;
            if (aerial) {
                this.startX += (endX - startX) * 0.024;
            }
            updateBounds();
            if (postDashTick >= POST_DASH_LIFETIME) {
                active = false;
                return;
            }
        }

        for (int i = 0; i < EMBER_COUNT; i++) {
            emberPhase[i] += 0.08;
            emberOffsetY[i] -= emberSpeedY[i];
            if (emberOffsetY[i] < -scale(26)) {
                emberOffsetY[i] = scale(8);
            }
        }

        damageTick++;
        if (damageTick >= DAMAGE_TICK_INTERVAL) {
            damageTick = 0;
            applyBurnToEnemies();
        }
    }

    private void applyBurnToEnemies() {
        double spellMult = 1.0 + InventoryBonus.getInstance().getSpell() + (PerksBonus.getInstance().getSpellDamageBonus() / 100.0);
        double burnDmg = 4.0 * spellMult;
        for (Enemy enemy : context.getEnemyManager().getAllEnemies()) {
            if (enemy instanceof Roric) continue;
            if (enemy.isAlive() && enemy.getEnemyAction() != Anim.DEATH && enemy.getEnemyAction() != Anim.HIDE) {
                if (hitBox.intersects(enemy.getHitBox())) {
                    enemy.spellHit(burnDmg);
                    String dmgText = String.format("%.1f", burnDmg);
                    context.getEffectManager().spawnDamageNumber(dmgText, enemy.getHitBox().getCenterX(), enemy.getHitBox().y, CRITICAL_COLOR);
                    context.getEffectManager().spawnDustParticles(enemy.getHitBox().getCenterX(), enemy.getHitBox().getCenterY(), 3, DustType.PLAYER_BURN, 0, enemy);
                }
            }
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        double span = Math.abs(endX - startX);
        if (span < scale(4)) return;

        float fadeProgress = dashFinished ? ((float) postDashTick / POST_DASH_LIFETIME) : 0.0f;
        float heat = 1.0f - fadeProgress;
        float alpha = Math.min(1.0f, heat * 1.3f);
        if (alpha <= 0.03f) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        double sx = startX - xLevelOffset;
        double ex = endX - xLevelOffset;
        double sy = baseY - yLevelOffset;

        if (aerial) renderContinuousAerialBeam(g2d, sx, ex, sy, heat);
        else renderIndividualGroundFlames(g2d, sx, ex, sy, fadeProgress);
        g2d.dispose();
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    private void renderContinuousAerialBeam(Graphics2D g2d, double sx, double ex, double sy, float heat) {
        double maxHalfThick = (11.0 * SCALE) * (0.35 + 0.65 * heat);
        renderBeamLayer(g2d, sx, ex, sy, maxHalfThick * 1.45, 1.0, 4.0 * SCALE, new Color(210, 30, 5, 85));
        renderEmbers(g2d, sx, ex, sy, heat);
        renderBeamLayer(g2d, sx, ex, sy, maxHalfThick * 1.05, 1.3, 3.2 * SCALE, new Color(245, 65, 10, 195));
        renderBeamLayer(g2d, sx, ex, sy, maxHalfThick * 0.68, 1.7, 2.2 * SCALE, new Color(255, 165, 25, 230));
        renderBeamLayer(g2d, sx, ex, sy, maxHalfThick * 0.34, 2.1, 1.1 * SCALE, new Color(255, 248, 185, 245));
    }

    private void renderBeamLayer(Graphics2D g2d, double sx, double ex, double sy, double maxHalfThick, double freqMult, double waveAmp, Color color) {
        double[] topX = new double[SAMPLE_POINTS];
        double[] topY = new double[SAMPLE_POINTS];
        double[] botX = new double[SAMPLE_POINTS];
        double[] botY = new double[SAMPLE_POINTS];

        for (int i = 0; i < SAMPLE_POINTS; i++) {
            double t = (double) i / (SAMPLE_POINTS - 1);
            double x = sx + (ex - sx) * t;
            double tailTaper = Math.pow(t, 0.58);
            double headCap = (t > 0.85) ? Math.sqrt(Math.max(0.0, 1.0 - Math.pow((t - 0.85) / 0.15, 2.0))) : 1.0;
            double envelope = tailTaper * headCap;
            double halfT = maxHalfThick * envelope;
            double centerWave = Math.sin(t * Math.PI * 3.5 * freqMult - waveTime * 1.4) * (waveAmp * 0.60 * (1.0 - t * 0.55));
            double topRipple = Math.cos(t * Math.PI * 6.0 * freqMult - waveTime * 1.9) * (waveAmp * 0.45 * envelope);
            double botRipple = Math.sin(t * Math.PI * 5.5 * freqMult - waveTime * 1.7) * (waveAmp * 0.45 * envelope);

            topX[i] = x;
            botX[i] = x;
            topY[i] = sy + centerWave - halfT + topRipple;
            botY[i] = sy + centerWave + halfT + botRipple;
        }

        Path2D.Double beam = new Path2D.Double();
        beam.moveTo(topX[0], topY[0]);
        for (int i = 1; i < SAMPLE_POINTS; i++) {
            beam.lineTo(topX[i], topY[i]);
        }
        for (int i = SAMPLE_POINTS - 1; i >= 0; i--) {
            beam.lineTo(botX[i], botY[i]);
        }
        beam.closePath();

        g2d.setColor(color);
        g2d.fill(beam);
    }

    /**
     * Renders the individual swaying flame teardrops along the floor (the original ground look).
     */
    private void renderIndividualGroundFlames(Graphics2D g2d, double sx, double ex, double floorY, float fadeProgress) {
        float lifeScale = 1.0f - (fadeProgress * 0.65f);
        double dir = (ex >= sx) ? 1.0 : -1.0;
        double span = Math.abs(ex - sx);
        double spacing = 5.5 * SCALE;
        int count = Math.min(MAX_GROUND_TONGUES, Math.max(2, (int) (span / spacing)));

        int glowW = (int) span;
        int glowH = Math.max(2, scale(5));
        int minScreenX = (int) Math.min(sx, ex);
        g2d.setColor(new Color(255, 80, 10, (int) (105 * lifeScale)));
        g2d.fillOval(minScreenX, (int) floorY - glowH / 2, glowW, glowH);

        for (int i = 0; i < count; i++) {
            double cx = sx + dir * (i * spacing) + tongueJitterX[i];
            double sway = Math.sin(tonguePhase[i]) * (2.5 * SCALE);
            double stretch = 0.82 + 0.22 * Math.cos(tonguePhase[i] * 1.3);
            double w = tongueWidth[i] * lifeScale, h = tongueHeight[i] * stretch * lifeScale;
            renderFlameTear(g2d, cx, floorY, w * 1.15, h * 1.1, sway, new Color(220, 40, 10, 195));
            renderFlameTear(g2d, cx, floorY, w * 0.80, h * 0.82, sway * 0.7, new Color(255, 140, 20, 225));
            renderFlameTear(g2d, cx, floorY, w * 0.45, h * 0.50, sway * 0.4, new Color(255, 240, 130, 240));
        }

        renderEmbers(g2d, sx, ex, floorY, 1.0f - fadeProgress);
    }

    private void renderFlameTear(Graphics2D g2d, double baseX, double baseY, double width, double height, double tipSway, Color color) {
        double hw = width * 0.5;
        Path2D.Double path = new Path2D.Double();
        path.moveTo(baseX - hw, baseY);
        path.quadTo(baseX - hw * 1.1, baseY - height * 0.55, baseX + tipSway, baseY - height);
        path.quadTo(baseX + hw * 1.1, baseY - height * 0.55, baseX + hw, baseY);
        path.closePath();

        g2d.setColor(color);
        g2d.fill(path);
    }

    private void renderEmbers(Graphics2D g2d, double sx, double ex, double sy, float heat) {
        for (int i = 0; i < EMBER_COUNT; i++) {
            double exPos = sx + (ex - sx) * emberNormX[i] + Math.sin(emberPhase[i]) * scale(3);
            double eyPos = (aerial ? sy : sy - scale(8)) + emberOffsetY[i];
            int s = Math.max(2, (int) (emberSize[i] * heat));

            g2d.setColor(new Color(255, 115, 15, 160));
            g2d.fillOval((int) exPos - 1, (int) eyPos - 1, s + 2, s + 2);
            g2d.setColor(new Color(255, 235, 130, 235));
            g2d.fillOval((int) exPos, (int) eyPos, s, s);
        }
    }

    @Override
    protected BufferedImage[] getAnimations() {
        return null;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}