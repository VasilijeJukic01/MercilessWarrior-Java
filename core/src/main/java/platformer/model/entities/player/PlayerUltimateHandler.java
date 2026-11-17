package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.core.config.BalanceManager;
import platformer.event.EventBus;
import platformer.event.events.effects.HitStopEvent;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.inventory.InventoryBonus;
import platformer.model.perks.PerksBonus;
import platformer.physics.CollisionDetector;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Manages the Player's Ultimate ability (Judgement Blitz):
 * 1. Charge accumulation from melee hits, crits, and parries.
 * 2. TARGETING phase: Time-frozen rhythm circles over on-screen enemies.
 * 3. BLITZ phase: High-speed anime dash between marked targets using AIR_ATTACK frames.
 * 4. SHEATHE_POSE phase: Dramatic low pose while slash marks hum.
 * 5. DETONATION: Simultaneous explosion of all slash marks based on rhythm accuracy.
 */
public class PlayerUltimateHandler {

    public enum State {
        INACTIVE,
        TARGETING,
        BLITZ,
        SHEATHE_POSE
    }

    public enum Judgment {
        UNJUDGED,
        PERFECT,
        GOOD,
        MISS
    }

    private static class RhythmTarget {
        final Enemy enemy;
        final double worldX, worldY;
        final double slashAngle;
        final int number;
        int tick = 0;
        final int maxTicks;
        Judgment judgment = Judgment.UNJUDGED;
        int feedbackTick = 0;
        boolean slashed = false;

        RhythmTarget(Enemy enemy, double worldX, double worldY, double slashAngle, int number, int maxTicks) {
            this.enemy = enemy;
            this.worldX = worldX;
            this.worldY = worldY;
            this.slashAngle = slashAngle;
            this.number = number;
            this.maxTicks = maxTicks;
        }
    }

    private static final int MAX_TARGETS = 5;
    private static final int BLITZ_TICKS_PER_TARGET = 14;
    private static final int SHEATHE_DURATION_TICKS = 85;
    private static final double PERFECT_WINDOW = 0.16;
    private static final double GOOD_WINDOW = 0.34;

    private static final Color CYAN_GLOW = new Color(55, 240, 255);
    private static final Color GOLD_PERFECT = new Color(255, 215, 65);
    private static final Color CRIMSON_SLASH = new Color(255, 50, 80);

    private final Player player;
    private final GameContext context;
    private final Random rand = new Random();

    private State state = State.INACTIVE;
    private double currentCharge = 0.0;

    private final List<RhythmTarget> targets = new ArrayList<>();
    private int activeTargetIndex = 0;

    // Blitz & Sheathe State
    private int blitzTargetIndex = 0;
    private int blitzTick = 0;
    private double blitzStartX, blitzStartY;
    private double blitzEndX, blitzEndY;
    private int sheatheTick = 0;

    public PlayerUltimateHandler(Player player, GameContext context) {
        this.player = player;
        this.context = context;
    }

    public void addChargeFromHit(boolean isCritical) {
        if (isActive()) return;
        BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
        double gain = isCritical ? pb.getUltimateCritCharge() : pb.getUltimateHitCharge();
        addCharge(gain);
    }

    public void addChargeFromParry() {
        if (isActive()) return;
        BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
        addCharge(pb.getUltimateParryCharge());
    }

    public void addCharge(double amount) {
        double max = BalanceManager.getInstance().getPlayerBalance().getUltimateMaxCharge();
        this.currentCharge = Math.min(max, Math.max(0.0, this.currentCharge + amount));
    }

    public boolean isReady() {
        double max = BalanceManager.getInstance().getPlayerBalance().getUltimateMaxCharge();
        return currentCharge >= max && state == State.INACTIVE;
    }

    public boolean isActive() {
        return state != State.INACTIVE;
    }

    /**
     * Attempts to activate the Ultimate if charge is full and at least one valid enemy is in the camera view.
     */
    public void tryActivate() {
        if (state == State.TARGETING) {
            handleRhythmInput();
            return;
        }
        if (!isReady() || player.checkAction(PlayerAction.DYING) || player.checkAction(PlayerAction.HIT)) return;

        List<Enemy> visibleEnemies = findVisibleTargetableEnemies();
        if (visibleEnemies.isEmpty()) return;

        buildRhythmTargets(visibleEnemies);
        if (targets.isEmpty()) return;

        this.currentCharge = 0;
        this.state = State.TARGETING;
        this.activeTargetIndex = 0;

        player.cancelAttackForMobility();
        player.resetDirections();
        player.removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT, PlayerAction.BLOCK, PlayerAction.MOVE);

        context.getScreenEffectsManager().setLetterboxActive(true);
        context.getLightManager().overrideAmbientDarkness(215);
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_CHARGE);
    }

    private List<Enemy> findVisibleTargetableEnemies() {
        int camX = context.getCamera().getXOffset();
        int camY = context.getCamera().getYOffset();
        Rectangle viewport = new Rectangle(camX - scale(20), camY - scale(20), GAME_WIDTH + scale(40), GAME_HEIGHT + scale(40));

        List<Enemy> list = new ArrayList<>();
        for (Enemy e : context.getEnemyManager().getAllEnemies()) {
            if (!e.isAlive() || !e.isVisible() || e instanceof Roric) continue;
            Anim action = e.getEnemyAction();
            if (action == Anim.DEATH || action == Anim.HIDE || action == Anim.REVEAL) continue;
            if (viewport.intersects(e.getHitBox())) {
                list.add(e);
            }
        }
        list.sort(Comparator.comparingDouble(e -> Math.hypot(e.getHitBox().getCenterX() - player.getHitBox().getCenterX(), e.getHitBox().getCenterY() - player.getHitBox().getCenterY())));
        return list;
    }

    private void buildRhythmTargets(List<Enemy> visibleEnemies) {
        targets.clear();
        int shrinkTicks = BalanceManager.getInstance().getPlayerBalance().getUltimateCircleShrinkTicks();
        int count = Math.min(MAX_TARGETS, Math.max(3, visibleEnemies.size() == 1 ? 4 : visibleEnemies.size() + 1));

        double[][] offsets = {
                {0.0, -0.22},
                {-0.35, 0.15},
                {0.35, 0.10},
                {-0.20, -0.30},
                {0.25, 0.28}
        };

        for (int i = 0; i < count; i++) {
            Enemy enemy = visibleEnemies.get(i % visibleEnemies.size());
            double[] off = offsets[i % offsets.length];
            double spreadX = off[0] * Math.max(scale(26), enemy.getHitBox().width * 0.9);
            double spreadY = off[1] * Math.max(scale(26), enemy.getHitBox().height * 0.75);
            double wx = enemy.getHitBox().getCenterX() + spreadX;
            double wy = enemy.getHitBox().getCenterY() + spreadY;

            double angle = Math.toRadians((i % 2 == 0 ? -28 : 28) + (rand.nextInt(25) - 12));
            int circleSpeed = Math.max(48, shrinkTicks - (i * 5));
            targets.add(new RhythmTarget(enemy, wx, wy, angle, i + 1, circleSpeed));
        }
    }

    /**
     * Evaluates the player's keyboard press during the TARGETING phase.
     */
    public boolean handleRhythmInput() {
        if (state != State.TARGETING || activeTargetIndex >= targets.size()) return false;

        RhythmTarget current = targets.get(activeTargetIndex);
        double progress = (double) current.tick / current.maxTicks;
        double delta = Math.abs(1.0 - progress);

        if (delta <= PERFECT_WINDOW) {
            current.judgment = Judgment.PERFECT;
            Audio.getInstance().getAudioPlayer().playBlockSound("Player");
            int sx = (int) current.worldX - context.getCamera().getXOffset();
            int sy = (int) current.worldY - context.getCamera().getYOffset();
            context.getScreenEffectsManager().triggerShockwave(sx, sy, scale(55), 14, GOLD_PERFECT);
        }
        else if (delta <= GOOD_WINDOW) {
            current.judgment = Judgment.GOOD;
            Audio.getInstance().getAudioPlayer().playSound(Sound.BTN_CLICK);
        }
        else {
            current.judgment = Judgment.MISS;
        }

        current.feedbackTick = 32;
        advanceToNextCircle();
        return true;
    }

    private void advanceToNextCircle() {
        activeTargetIndex++;
        if (activeTargetIndex >= targets.size()) {
            startBlitzPhase();
        }
    }

    public void update() {
        if (state == State.INACTIVE) return;

        for (RhythmTarget t : targets) {
            if (t.feedbackTick > 0) t.feedbackTick--;
        }

        switch (state) {
            case TARGETING -> updateTargetingPhase();
            case BLITZ -> updateBlitzPhase();
            case SHEATHE_POSE -> updateSheathePhase();
            default -> {}
        }
    }

    private void updateTargetingPhase() {
        if (activeTargetIndex >= targets.size()) {
            startBlitzPhase();
            return;
        }

        RhythmTarget current = targets.get(activeTargetIndex);
        current.tick++;
        if (current.tick > current.maxTicks + (int) (current.maxTicks * GOOD_WINDOW)) {
            current.judgment = Judgment.MISS;
            current.feedbackTick = 30;
            advanceToNextCircle();
        }
    }

    private void startBlitzPhase() {
        this.state = State.BLITZ;
        this.blitzTargetIndex = 0;
        setupBlitzSegment(0);
    }

    private void setupBlitzSegment(int index) {
        if (index >= targets.size()) {
            startSheathePhase();
            return;
        }
        this.blitzTick = 0;
        this.blitzStartX = player.getHitBox().x;
        this.blitzStartY = player.getHitBox().y;

        RhythmTarget target = targets.get(index);
        double dirSign = (target.worldX >= blitzStartX) ? 1.0 : -1.0;
        player.setFacingDirection((int) dirSign);

        this.blitzEndX = target.worldX + (dirSign * scale(22)) - (player.getHitBox().width / 2.0);
        this.blitzEndY = target.worldY - (player.getHitBox().height / 2.0);
        player.setUltimateAnimation(Anim.AIR_ATTACK, 0);
        Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
    }

    private void updateBlitzPhase() {
        if (blitzTargetIndex >= targets.size()) {
            startSheathePhase();
            return;
        }

        blitzTick++;
        double t = Math.min(1.0, (double) blitzTick / BLITZ_TICKS_PER_TARGET);
        double eased = 1.0 - Math.pow(1.0 - t, 3);

        player.getHitBox().x = blitzStartX + (blitzEndX - blitzStartX) * eased;
        player.getHitBox().y = blitzStartY + (blitzEndY - blitzStartY) * eased;

        int frame = Math.min(4, (int) (t * 5));
        player.setUltimateAnimation(Anim.AIR_ATTACK, frame);
        player.recordUltimateAfterimage();

        RhythmTarget currentTarget = targets.get(blitzTargetIndex);
        if (!currentTarget.slashed && t >= 0.5) {
            currentTarget.slashed = true;
            Audio.getInstance().getAudioPlayer().playSlashSound();
            context.getScreenEffectsManager().triggerShake(8, 5.0);
            context.getEffectManager().spawnDustParticles(currentTarget.worldX, currentTarget.worldY, 12, DustType.SW_DASH_SLASH, player.getFlipSign(), null);
        }

        if (blitzTick >= BLITZ_TICKS_PER_TARGET) {
            blitzTargetIndex++;
            setupBlitzSegment(blitzTargetIndex);
        }
    }

    private void startSheathePhase() {
        this.state = State.SHEATHE_POSE;
        this.sheatheTick = 0;

        int[][] lvlData = context.getLevelManager().getCurrentLevel().getLvlData();
        RhythmTarget last = targets.get(targets.size() - 1);
        int dirSign = player.getFlipSign();

        double[] candidateOffsets = {
                dirSign * scale(36),
                -dirSign * scale(36),
                0.0
        };

        for (double off : candidateOffsets) {
            double cx = last.enemy.getHitBox().getCenterX() + off - (player.getHitBox().width / 2.0);
            double groundY = CollisionDetector.getGroundY(cx + player.getHitBox().width / 2.0, last.enemy.getHitBox().y, lvlData);
            double cy = groundY - player.getHitBox().height - 1;
            if (CollisionDetector.canMoveHere(cx, cy, player.getHitBox().width, player.getHitBox().height, lvlData)) {
                player.getHitBox().x = cx;
                player.getHitBox().y = cy;
                player.setInAir(false);
                player.setAirSpeed(0);
                break;
            }
        }

        player.setUltimateAnimation(Anim.CROUCH_ATTACK, 0);
    }

    private void updateSheathePhase() {
        sheatheTick++;
        int frame = Math.min(4, sheatheTick / 10);
        player.setUltimateAnimation(Anim.CROUCH_ATTACK, frame);

        if (sheatheTick >= SHEATHE_DURATION_TICKS) {
            detonateAllMarks();
        }
    }

    private void detonateAllMarks() {
        BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
        double baseDmg = player.getAttackDmg();
        double perkBonusPct = PerksBonus.getInstance().getBonusAttack() / 100.0;
        double equipBonusPct = InventoryBonus.getInstance().getAttack();
        double rawAttack = baseDmg * (1.0 + perkBonusPct + equipBonusPct);

        boolean anyPerfect = false;
        for (RhythmTarget t : targets) {
            double mult = switch (t.judgment) {
                case PERFECT -> {
                    anyPerfect = true;
                    yield pb.getUltimatePerfectDamageMult();
                }
                case GOOD -> pb.getUltimateGoodDamageMult();
                default -> pb.getUltimateMissDamageMult();
            };

            double finalDamage = rawAttack * mult;
            boolean isCrit = (t.judgment == Judgment.PERFECT);
            context.getEnemyManager().applyUltimateStrike(t.enemy, finalDamage, isCrit, player);

            int sx = (int) t.worldX - context.getCamera().getXOffset();
            int sy = (int) t.worldY - context.getCamera().getYOffset();
            Color waveCol = isCrit ? GOLD_PERFECT : CYAN_GLOW;
            context.getScreenEffectsManager().triggerShockwave(sx, sy, scale(95), 24, waveCol);
        }

        Audio.getInstance().getAudioPlayer().playHitSound();
        Audio.getInstance().getAudioPlayer().playSound(Sound.LIGHTNING_1);
        EventBus.getInstance().publish(new HitStopEvent(14));
        context.getScreenEffectsManager().triggerShake(28, 14.0);
        context.getScreenEffectsManager().triggerLightningStrobe(anyPerfect ? GOLD_PERFECT : CYAN_GLOW, 195, true);

        finishUltimate();
    }

    private void finishUltimate() {
        this.state = State.INACTIVE;
        this.targets.clear();
        this.activeTargetIndex = 0;
        context.getScreenEffectsManager().setLetterboxActive(false);
        context.getLightManager().releaseAmbientDarkness();
        player.setUltimateAnimation(Anim.IDLE, 0);
    }

    // Rendering
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (state == State.INACTIVE) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        renderSlashMarks(g2d, xLevelOffset, yLevelOffset);
        if (state == State.TARGETING) {
            renderRhythmCircles(g2d, xLevelOffset, yLevelOffset);
            renderTargetingBanner(g2d);
        }
        renderJudgmentFeedback(g2d, xLevelOffset, yLevelOffset);

        g2d.dispose();
    }

    private void renderRhythmCircles(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        int baseRadius = scale(19);
        double rotTime = System.currentTimeMillis() * 0.004;

        for (int i = targets.size() - 1; i >= activeTargetIndex; i--) {
            RhythmTarget t = targets.get(i);
            int sx = (int) (t.worldX - xLevelOffset);
            int sy = (int) (t.worldY - yLevelOffset);
            boolean isCurrent = (i == activeTargetIndex);

            if (!isCurrent) {
                g2d.setColor(new Color(10, 18, 24, 150));
                g2d.fillOval(sx - baseRadius, sy - baseRadius, baseRadius * 2, baseRadius * 2);
                g2d.setColor(new Color(55, 235, 255, 80));
                g2d.setStroke(new BasicStroke(1.2f * SCALE));
                g2d.drawOval(sx - baseRadius, sy - baseRadius, baseRadius * 2, baseRadius * 2);

                g2d.setFont(UIButtonRenderer.getFont(9.5f * SCALE));
                g2d.setColor(new Color(180, 235, 240, 150));
                String numStr = String.valueOf(t.number);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(numStr, sx - fm.stringWidth(numStr) / 2, sy + (int) (fm.getAscent() * 0.36f));
                continue;
            }

            double progress = Math.min(1.2, (double) t.tick / t.maxTicks);
            double approachScale = Math.max(0.85, 2.75 - 1.75 * progress);
            int approachRadius = (int) (baseRadius * approachScale);
            boolean inSweetSpot = Math.abs(1.0 - progress) <= PERFECT_WINDOW;
            Color ringColor = inSweetSpot ? GOLD_PERFECT : CYAN_GLOW;

            RadialGradientPaint innerGlow = new RadialGradientPaint(
                    new Point2D.Float(sx, sy),
                    baseRadius,
                    new float[]{0.0f, 0.7f, 1.0f},
                    new Color[]{new Color(16, 30, 38, 225), new Color(12, 22, 28, 210), new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), 95)}
            );
            g2d.setPaint(innerGlow);
            g2d.fillOval(sx - baseRadius, sy - baseRadius, baseRadius * 2, baseRadius * 2);

            g2d.setColor(new Color(255, 215, 65, 70));
            g2d.setStroke(new BasicStroke(3.2f * SCALE));
            g2d.drawOval(sx - baseRadius, sy - baseRadius, baseRadius * 2, baseRadius * 2);

            g2d.setColor(ringColor);
            g2d.setStroke(new BasicStroke(1.8f * SCALE));
            g2d.drawOval(sx - baseRadius, sy - baseRadius, baseRadius * 2, baseRadius * 2);

            for (int k = 0; k < 4; k++) {
                double a = rotTime + k * (Math.PI / 2.0);
                int tx1 = sx + (int) (Math.cos(a) * (baseRadius - scale(2)));
                int ty1 = sy + (int) (Math.sin(a) * (baseRadius - scale(2)));
                int tx2 = sx + (int) (Math.cos(a) * (baseRadius + scale(3)));
                int ty2 = sy + (int) (Math.sin(a) * (baseRadius + scale(3)));
                g2d.drawLine(tx1, ty1, tx2, ty2);
            }

            g2d.setColor(new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), 60));
            g2d.setStroke(new BasicStroke(4.0f * SCALE));
            g2d.drawOval(sx - approachRadius, sy - approachRadius, approachRadius * 2, approachRadius * 2);

            g2d.setColor(new Color(235, 255, 255, 235));
            g2d.setStroke(new BasicStroke(1.8f * SCALE));
            g2d.drawOval(sx - approachRadius, sy - approachRadius, approachRadius * 2, approachRadius * 2);

            g2d.setFont(UIButtonRenderer.getFont(12f * SCALE));
            String numStr = String.valueOf(t.number);
            FontMetrics fm = g2d.getFontMetrics();
            int nx = sx - fm.stringWidth(numStr) / 2;
            int ny = sy + (int) (fm.getAscent() * 0.36f);
            g2d.setColor(new Color(0, 0, 0, 220));
            g2d.drawString(numStr, nx + 1, ny + 1);
            g2d.setColor(inSweetSpot ? GOLD_PERFECT : Color.WHITE);
            g2d.drawString(numStr, nx, ny);
        }
    }

    private void renderSlashMarks(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        double time = System.currentTimeMillis() * 0.04;

        for (RhythmTarget t : targets) {
            if (!t.slashed && state == State.TARGETING && t.judgment == Judgment.UNJUDGED) continue;

            int sx = (int) (t.worldX - xLevelOffset);
            int sy = (int) (t.worldY - yLevelOffset);
            if (state == State.SHEATHE_POSE) {
                sx += (int) (Math.sin(time + t.number) * 1.5 * SCALE);
                sy += (int) (Math.cos(time * 1.3 + t.number) * 1.5 * SCALE);
            }

            double len = (t.slashed ? 44.0 : 24.0) * SCALE;
            double thick = (t.slashed ? 3.4 : 2.0) * SCALE;

            Color outerColor = switch (t.judgment) {
                case PERFECT -> GOLD_PERFECT;
                case GOOD -> CYAN_GLOW;
                default -> CRIMSON_SLASH;
            };

            AffineTransform old = g2d.getTransform();
            g2d.translate(sx, sy);
            g2d.rotate(t.slashAngle);

            Path2D.Double slash = new Path2D.Double();
            slash.moveTo(-len, 0);
            slash.quadTo(0, -thick, len, 0);
            slash.quadTo(0, thick, -len, 0);
            slash.closePath();

            g2d.setColor(new Color(outerColor.getRed(), outerColor.getGreen(), outerColor.getBlue(), 185));
            g2d.fill(slash);

            Path2D.Double core = new Path2D.Double();
            core.moveTo(-len * 0.82, 0);
            core.quadTo(0, -thick * 0.42, len * 0.82, 0);
            core.quadTo(0, thick * 0.42, -len * 0.82, 0);
            core.closePath();
            g2d.setColor(Color.WHITE);
            g2d.fill(core);

            g2d.setTransform(old);
        }
    }

    private void renderJudgmentFeedback(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.setFont(UIButtonRenderer.getFont(10f * SCALE));
        FontMetrics fm = g2d.getFontMetrics();

        for (RhythmTarget t : targets) {
            if (t.feedbackTick <= 0 || t.judgment == Judgment.UNJUDGED) continue;

            float alpha = Math.min(1.0f, t.feedbackTick / 20.0f);
            int floatOffset = (int) ((32 - t.feedbackTick) * 0.45 * SCALE);
            int sx = (int) (t.worldX - xLevelOffset);
            int sy = (int) (t.worldY - yLevelOffset) - scale(22) - floatOffset;

            String label = t.judgment.name() + (t.judgment == Judgment.PERFECT ? "!" : "");
            Color baseCol = switch (t.judgment) {
                case PERFECT -> GOLD_PERFECT;
                case GOOD -> CYAN_GLOW;
                default -> new Color(180, 180, 180);
            };

            g2d.setColor(new Color(0, 0, 0, (int) (alpha * 200)));
            g2d.drawString(label, sx - fm.stringWidth(label) / 2 + 1, sy + 1);
            g2d.setColor(new Color(baseCol.getRed(), baseCol.getGreen(), baseCol.getBlue(), (int) (alpha * 255)));
            g2d.drawString(label, sx - fm.stringWidth(label) / 2, sy);
        }
    }

    private void renderTargetingBanner(Graphics2D g2d) {
        String keyAttack = platformer.core.Framework.getInstance().getKeyboardController().getKeyName("Attack").toUpperCase();
        String prompt = "PRESS [" + keyAttack + "] TO MARK TARGETS!";
        g2d.setFont(UIButtonRenderer.getFont(11f * SCALE));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (GAME_WIDTH - fm.stringWidth(prompt)) / 2;
        int y = scale(48);

        float pulse = (float) (0.75 + 0.25 * Math.sin(System.currentTimeMillis() * 0.015));
        int radius = scale(90);
        RadialGradientPaint bgGlow = new RadialGradientPaint(
                new Point2D.Float(GAME_WIDTH / 2.0f, y - scale(4)),
                radius,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(0, 20, 30, 180), new Color(0, 0, 0, 0)}
        );
        g2d.setPaint(bgGlow);
        g2d.fillRect(x - scale(20), y - scale(16), fm.stringWidth(prompt) + scale(40), scale(24));

        g2d.setColor(new Color(55, 240, 255, (int) (255 * pulse)));
        g2d.drawString(prompt, x, y);
    }

    public double getCurrentCharge() {
        return currentCharge;
    }

    public double getMaxCharge() {
        return BalanceManager.getInstance().getPlayerBalance().getUltimateMaxCharge();
    }

    public State getState() {
        return state;
    }

    public void reset() {
        if (state != State.INACTIVE) {
            context.getScreenEffectsManager().setLetterboxActive(false);
            context.getLightManager().releaseAmbientDarkness();
        }
        this.state = State.INACTIVE;
        this.currentCharge = 0;
        this.targets.clear();
        this.activeTargetIndex = 0;
    }
}