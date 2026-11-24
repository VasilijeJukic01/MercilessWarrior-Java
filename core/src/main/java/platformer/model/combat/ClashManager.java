package platformer.model.combat;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.ClashRequestedEvent;
import platformer.event.events.effects.HitStopEvent;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * First-class manager that orchestrates interactive mid-air anime steel-clash minigames between the Player and any Enemy that publishes a {@link ClashRequestedEvent}.
 */
public class ClashManager {

    private enum State {
        INACTIVE,
        LAUNCH_UP,
        ZIP_TO_POINT,
        CLASH_WINDOW,
        CLASH_PAUSE,
        FINISHER_SLAM,
        DEFEAT_KNOCKBACK
    }

    private static class RibbonSegment {
        final double x1, y1, x2, y2;
        final Color color;
        float alpha;
        final float thickness;

        RibbonSegment(double x1, double y1, double x2, double y2, Color color, float alpha, float thickness) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
            this.alpha = alpha;
            this.thickness = thickness;
        }
    }

    private static final int LAUNCH_TICKS = 16;
    private static final int ZIP_TICKS = 15;
    private static final int PAUSE_TICKS = 14;
    private static final int SLAM_TICKS = 20;

    private static final double PERFECT_DELTA = 0.16;
    private static final double GOOD_DELTA = 0.36;
    private static final Color PLAYER_RIBBON_COLOR = new Color(255, 80, 70);
    private static final Color CRIMSON_FAIL = new Color(255, 55, 75);

    private GameContext context;
    private final Random rand = new Random();

    private State state = State.INACTIVE;
    private Enemy opponent;
    private ClashConfig config;

    private double arenaLeftX, arenaRightX, arenaCeilingY, arenaFloorY;

    private int clashIndex = 0;
    private int stateTick = 0;
    private int cooldownTicks = 0;
    private int perfectCount = 0;
    private boolean glintChimePlayed = false;
    private boolean playerOnLeft = true;

    private double pStartX, pStartY, pEndX, pEndY;
    private double bStartX, bStartY, bEndX, bEndY;
    private double prevPlayerCenterX, prevPlayerCenterY;
    private double prevBossCenterX, prevBossCenterY;
    private double clashMidX, clashMidY;

    private final List<RibbonSegment> ribbonTrails = new ArrayList<>();
    private final List<double[]> clashNodes = new ArrayList<>();
    private int flareTick = 0;

    public void wire(GameContext context) {
        this.context = context;
    }

    public void onClashRequested(ClashRequestedEvent event) {
        if (event == null || event.opponent() == null || event.config() == null) return;
        tryStartClash(event.opponent(), event.config());
    }

    public boolean tryStartClash(Enemy targetEnemy, ClashConfig clashConfig) {
        if (context == null || state != State.INACTIVE || cooldownTicks > 0) return false;
        Player player = context.getPlayer();
        if (player == null || !targetEnemy.isAlive() || targetEnemy.getCurrentHealth() <= 0) return false;
        if (player.getUltimateHandler() != null && player.getUltimateHandler().isActive()) return false;
        if (clashConfig.normalizedAnchors() == null || clashConfig.normalizedAnchors().length == 0) return false;

        this.opponent = targetEnemy;
        this.config = clashConfig;
        this.clashIndex = 0;
        this.perfectCount = 0;
        this.ribbonTrails.clear();
        this.clashNodes.clear();
        this.flareTick = 18;
        this.cooldownTicks = clashConfig.cooldownTicks();

        detectArenaInteriorBounds(player);

        player.cancelAttackForMobility();
        player.resetDirections();
        player.removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT, PlayerAction.BLOCK, PlayerAction.CAN_BLOCK, PlayerAction.MOVE);
        player.setInAir(true);
        player.setAirSpeed(0);

        context.getProjectileManager().reset();
        context.getSpellManager().reset();
        context.getScreenEffectsManager().setLetterboxActive(true);
        context.getLightManager().overrideAmbientDarkness(205);
        context.getScreenEffectsManager().triggerShake(16, 9.0);
        context.getScreenEffectsManager().triggerLightningStrobe(config.primaryColor(), 140, false);

        Audio.getInstance().getAudioPlayer().playBlockSound("Player");
        if (config.clashSound() != null) {
            Audio.getInstance().getAudioPlayer().playSound(config.clashSound());
        }

        setupLaunchPhase(player);
        return true;
    }

    private void detectArenaInteriorBounds(Player player) {
        int[][] lvlData = context.getLevelManager().getCurrentLevel().getLvlData();
        int cols = lvlData.length;
        int rows = lvlData[0].length;

        int startTx = Math.max(1, Math.min(cols - 2, (int) (player.getHitBox().getCenterX() / TILES_SIZE)));
        int startTy = Math.max(1, Math.min(rows - 2, (int) (player.getHitBox().getCenterY() / TILES_SIZE)));

        int floorTileY = rows - 1;
        for (int ty = startTy; ty < rows; ty++) {
            if (CollisionDetector.isTileSolid(startTx, ty, lvlData)) {
                floorTileY = ty;
                break;
            }
        }
        int openRowY = Math.max(1, floorTileY - 1);

        int leftWallTx = 0;
        for (int tx = startTx; tx >= 0; tx--) {
            if (CollisionDetector.isTileSolid(tx, openRowY, lvlData)) {
                leftWallTx = tx;
                break;
            }
        }
        int rightWallTx = cols - 1;
        for (int tx = startTx; tx < cols; tx++) {
            if (CollisionDetector.isTileSolid(tx, openRowY, lvlData)) {
                rightWallTx = tx;
                break;
            }
        }

        int midTx = (leftWallTx + rightWallTx) / 2;
        int ceilingTileY = 0;
        for (int ty = openRowY; ty >= 0; ty--) {
            if (CollisionDetector.isTileSolid(midTx, ty, lvlData)) {
                ceilingTileY = ty;
                break;
            }
        }

        double marginX = 2.6 * TILES_SIZE;
        this.arenaLeftX = (leftWallTx + 1) * TILES_SIZE + marginX;
        this.arenaRightX = rightWallTx * TILES_SIZE - marginX;
        if (this.arenaRightX <= this.arenaLeftX + TILES_SIZE) {
            this.arenaLeftX = (leftWallTx + 1.5) * TILES_SIZE;
            this.arenaRightX = (rightWallTx - 0.5) * TILES_SIZE;
        }

        this.arenaFloorY = floorTileY * TILES_SIZE;
        this.arenaCeilingY = (ceilingTileY + 1) * TILES_SIZE + (2.0 * TILES_SIZE);
        if (this.arenaCeilingY >= this.arenaFloorY - (2.5 * TILES_SIZE)) {
            this.arenaCeilingY = Math.max(TILES_SIZE * 2.0, this.arenaFloorY - (3.5 * TILES_SIZE));
        }
    }

    private double[] getAnchorWorldPos(int idx) {
        double[][] anchors = config.normalizedAnchors();
        double normX = anchors[idx][0];
        double normY = anchors[idx][1];

        double wx = arenaLeftX + normX * (arenaRightX - arenaLeftX);
        double safeLowY = arenaFloorY - (1.8 * TILES_SIZE);
        double wy = arenaCeilingY + normY * Math.max(TILES_SIZE, safeLowY - arenaCeilingY);
        return new double[]{wx, wy};
    }

    private void snapshotCenters(Player player) {
        this.prevPlayerCenterX = player.getHitBox().getCenterX();
        this.prevPlayerCenterY = player.getHitBox().getCenterY();
        this.prevBossCenterX = opponent.getHitBox().getCenterX();
        this.prevBossCenterY = opponent.getHitBox().getCenterY();
    }

    private void triggerDepartureBurst(double worldX, double worldY, Player player) {
        int sx = (int) worldX - context.getCamera().getXOffset();
        int sy = (int) worldY - context.getCamera().getYOffset();
        context.getScreenEffectsManager().triggerShockwave(sx, sy, scale(52), 14, config.accentColor());
        context.getEffectManager().spawnDustParticles(worldX, worldY, 10, DustType.DASH, player.getFlipSign(), player);
        context.getEffectManager().spawnDustParticles(worldX, worldY, 8, DustType.SW_AURA_CRACKLE, 0, null);
    }

    private void setupLaunchPhase(Player player) {
        this.state = State.LAUNCH_UP;
        this.stateTick = 0;

        this.pStartX = player.getHitBox().x;
        this.pStartY = player.getHitBox().y;
        this.bStartX = opponent.getHitBox().x;
        this.bStartY = opponent.getHitBox().y;
        snapshotCenters(player);
        triggerDepartureBurst((prevPlayerCenterX + prevBossCenterX) * 0.5, (prevPlayerCenterY + prevBossCenterY) * 0.5, player);

        double[] firstAnchor = getAnchorWorldPos(0);
        setTargetPositionsAroundAnchor(player, firstAnchor[0], firstAnchor[1], 0);

        player.setUltimateAnimation(Anim.DASH, 2);
        opponent.setCinematicPose(config.bossZipAnim(), config.bossZipFrame(), (bEndX >= pEndX) ? Direction.LEFT : Direction.RIGHT, true);
    }

    private void setupZipToNextAnchor(Player player, int nextIdx) {
        this.state = State.ZIP_TO_POINT;
        this.stateTick = 0;

        this.pStartX = player.getHitBox().x;
        this.pStartY = player.getHitBox().y;
        this.bStartX = opponent.getHitBox().x;
        this.bStartY = opponent.getHitBox().y;
        snapshotCenters(player);
        triggerDepartureBurst(clashMidX, clashMidY, player);

        double[] anchor = getAnchorWorldPos(nextIdx);
        setTargetPositionsAroundAnchor(player, anchor[0], anchor[1], nextIdx);

        Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
        player.setUltimateAnimation(Anim.DASH, 3);
        opponent.setCinematicPose(config.bossZipAnim(), config.bossZipFrame(), (bEndX >= pEndX) ? Direction.LEFT : Direction.RIGHT, true);
    }

    private void setTargetPositionsAroundAnchor(Player player, double anchorX, double anchorY, int idx) {
        double sep = 21.0 * SCALE;
        this.playerOnLeft = (idx % 2 == 0);

        this.clashMidX = anchorX;
        this.clashMidY = anchorY;

        if (playerOnLeft) {
            this.pEndX = anchorX - sep - player.getHitBox().width * 0.5;
            this.bEndX = anchorX + sep - opponent.getHitBox().width * 0.5;
            player.setFacingDirection(1);
        }
        else {
            this.pEndX = anchorX + sep - player.getHitBox().width * 0.5;
            this.bEndX = anchorX - sep - opponent.getHitBox().width * 0.5;
            player.setFacingDirection(-1);
        }
        this.pEndY = anchorY - player.getHitBox().height * 0.5;
        this.bEndY = anchorY - opponent.getHitBox().height * 0.5;
    }

    public boolean handleAttackInput() {
        if (state != State.CLASH_WINDOW) return false;

        int duration = getWindowDuration(clashIndex);
        double progress = (double) stateTick / duration;
        double delta = Math.abs(1.0 - progress);

        if (delta <= PERFECT_DELTA) {
            perfectCount++;
            onClashExchangeWon(true);
        }
        else if (delta <= GOOD_DELTA) {
            onClashExchangeWon(false);
        }
        else {
            onClashExchangeLost();
        }
        return true;
    }

    private int getWindowDuration(int idx) {
        int[] durations = config.windowDurations();
        if (durations == null || durations.length == 0) return 64;
        return durations[Math.min(idx, durations.length - 1)];
    }

    private void onClashExchangeWon(boolean isPerfect) {
        Player player = context.getPlayer();
        this.flareTick = 22;
        clashNodes.add(new double[]{clashMidX, clashMidY, 1.0});

        Color burstColor = isPerfect ? config.primaryColor() : config.accentColor();
        Audio.getInstance().getAudioPlayer().playBlockSound("Player");
        if (config.clashSound() != null) {
            Audio.getInstance().getAudioPlayer().playSound(config.clashSound());
        }

        EventBus.getInstance().publish(new HitStopEvent(isPerfect ? 10 : 7));
        context.getScreenEffectsManager().triggerShake(14, isPerfect ? 10.5 : 7.5);
        context.getScreenEffectsManager().triggerLightningStrobe(burstColor, isPerfect ? 155 : 115, false);

        int sx = (int) clashMidX - context.getCamera().getXOffset();
        int sy = (int) clashMidY - context.getCamera().getYOffset();
        context.getScreenEffectsManager().triggerShockwave(sx, sy, scale(isPerfect ? 110 : 78), 20, burstColor);

        context.getEffectManager().spawnDustParticles(clashMidX, clashMidY, isPerfect ? 26 : 16, DustType.IMPACT_SPARK, player.getFlipSign(), null);
        context.getEffectManager().spawnDustParticles(clashMidX, clashMidY, isPerfect ? 16 : 8, DustType.CRITICAL_HIT, -player.getFlipSign(), null);

        double dmg = isPerfect ? config.perfectChipDamage() : config.chipDamage();
        applyOpponentDamage(dmg, isPerfect);
        player.changeStamina(isPerfect ? 8 : 5);
        player.getUltimateHandler().addCharge(isPerfect ? 12 : 8);

        if (opponent.getCurrentHealth() <= 0 || !opponent.isAlive()) {
            setupFinisherSlam(player);
            return;
        }

        this.state = State.CLASH_PAUSE;
        this.stateTick = 0;

        player.getHitBox().x = pEndX;
        opponent.getHitBox().x = bEndX;
        player.setUltimateAnimation(Anim.AIR_ATTACK, 2);
        Direction oppFace = playerOnLeft ? Direction.LEFT : Direction.RIGHT;
        opponent.setCinematicPose(config.bossRecoilAnim(), config.bossRecoilFrame(), oppFace, true);
    }

    private void onClashExchangeLost() {
        Player player = context.getPlayer();

        Audio.getInstance().getAudioPlayer().playHitSound();
        EventBus.getInstance().publish(new HitStopEvent(10));
        context.getScreenEffectsManager().triggerShake(18, 11.5);
        context.getScreenEffectsManager().triggerLightningStrobe(CRIMSON_FAIL, 165, false);

        int sx = (int) clashMidX - context.getCamera().getXOffset();
        int sy = (int) clashMidY - context.getCamera().getYOffset();
        context.getScreenEffectsManager().triggerShockwave(sx, sy, scale(90), 20, CRIMSON_FAIL);
        context.getEffectManager().spawnDustParticles(clashMidX, clashMidY, 20, DustType.PLAYER_HIT, -player.getFlipSign(), player);

        player.applyStatusDamage(-config.missDamage());
        setupDefeatKnockback(player);
    }

    private void setupFinisherSlam(Player player) {
        this.state = State.FINISHER_SLAM;
        this.stateTick = 0;

        this.pStartX = player.getHitBox().x;
        this.pStartY = player.getHitBox().y;
        this.bStartX = opponent.getHitBox().x;
        this.bStartY = opponent.getHitBox().y;
        snapshotCenters(player);

        double clampedBossX = Math.max(arenaLeftX, Math.min(arenaRightX - opponent.getHitBox().width, bStartX));
        double desiredPlayerX = clampedBossX - player.getFlipSign() * (3.2 * TILES_SIZE);
        double clampedPlayerX = Math.max(arenaLeftX, Math.min(arenaRightX - player.getHitBox().width, desiredPlayerX));

        this.bEndX = clampedBossX;
        this.bEndY = arenaFloorY - opponent.getHitBox().height - 1;
        this.pEndX = clampedPlayerX;
        this.pEndY = arenaFloorY - player.getHitBox().height - 1;

        if (config.clashSound() != null) {
            Audio.getInstance().getAudioPlayer().playSound(config.clashSound());
        }
        player.setUltimateAnimation(Anim.AIR_ATTACK, 3);
        opponent.setCinematicPose(config.bossRecoilAnim(), config.bossRecoilFrame(), opponent.getDirection(), true);
    }

    private void setupDefeatKnockback(Player player) {
        this.state = State.DEFEAT_KNOCKBACK;
        this.stateTick = 0;

        this.pStartX = player.getHitBox().x;
        this.pStartY = player.getHitBox().y;
        this.bStartX = opponent.getHitBox().x;
        this.bStartY = opponent.getHitBox().y;
        snapshotCenters(player);

        this.pEndX = Math.max(arenaLeftX, Math.min(arenaRightX - player.getHitBox().width, pStartX));
        this.pEndY = arenaFloorY - player.getHitBox().height - 1;
        this.bEndX = Math.max(arenaLeftX, Math.min(arenaRightX - opponent.getHitBox().width, bStartX));
        this.bEndY = arenaFloorY - opponent.getHitBox().height - 1;

        player.setUltimateAnimation(Anim.FALL, 1);
        opponent.setCinematicPose(config.bossRecoilAnim(), config.bossRecoilFrame(), opponent.getDirection(), true);
    }

    public void update() {
        if (state == State.INACTIVE) {
            if (cooldownTicks > 0) cooldownTicks--;
            return;
        }

        Player player = context.getPlayer();
        if (player == null || player.getCurrentHealth() <= 0 || opponent == null) {
            reset();
            return;
        }

        if (flareTick > 0) flareTick--;
        fadeTrailsAndNodes();

        switch (state) {
            case LAUNCH_UP, ZIP_TO_POINT -> updateZipMotion(player);
            case CLASH_WINDOW -> updateClashWindow(player);
            case CLASH_PAUSE -> updateClashPause(player);
            case FINISHER_SLAM -> updateFinisherSlam(player);
            case DEFEAT_KNOCKBACK -> updateDefeatKnockback(player);
            default -> {}
        }

        player.updateDuringCinematic(isPlayerZipping());
        context.getCamera().update(player);
    }

    private void fadeTrailsAndNodes() {
        for (int i = ribbonTrails.size() - 1; i >= 0; i--) {
            RibbonSegment seg = ribbonTrails.get(i);
            seg.alpha -= 0.018f;
            if (seg.alpha <= 0.02f) ribbonTrails.remove(i);
        }
        for (int i = clashNodes.size() - 1; i >= 0; i--) {
            clashNodes.get(i)[2] -= 0.012;
            if (clashNodes.get(i)[2] <= 0.02) clashNodes.remove(i);
        }
    }

    private void appendTwinRibbons(Player player, float thickness) {
        double px = player.getHitBox().getCenterX();
        double py = player.getHitBox().getCenterY();
        double bx = opponent.getHitBox().getCenterX();
        double by = opponent.getHitBox().getCenterY();

        ribbonTrails.add(new RibbonSegment(prevPlayerCenterX, prevPlayerCenterY, px, py, PLAYER_RIBBON_COLOR, 0.95f, thickness));
        ribbonTrails.add(new RibbonSegment(prevBossCenterX, prevBossCenterY, bx, by, config.accentColor(), 0.95f, thickness));

        this.prevPlayerCenterX = px;
        this.prevPlayerCenterY = py;
        this.prevBossCenterX = bx;
        this.prevBossCenterY = by;
    }

    private void updateZipMotion(Player player) {
        stateTick++;
        int duration = (state == State.LAUNCH_UP) ? LAUNCH_TICKS : ZIP_TICKS;
        double t = Math.min(1.0, (double) stateTick / duration);
        double eased = 1.0 - Math.pow(1.0 - t, 3);

        int travelDir = (pEndX >= pStartX) ? 1 : -1;
        player.setFacingDirection(travelDir);

        double windupGap = 15.0 * SCALE;
        double pTargetWithGap = pEndX + (playerOnLeft ? -windupGap : windupGap);
        double bTargetWithGap = bEndX + (playerOnLeft ? windupGap : -windupGap);
        double arcAmp = 22.0 * SCALE;
        double arcWave = Math.sin(t * Math.PI) * arcAmp;

        player.getHitBox().x = pStartX + (pTargetWithGap - pStartX) * eased;
        player.getHitBox().y = pStartY + (pEndY - pStartY) * eased + arcWave;
        opponent.getHitBox().x = bStartX + (bTargetWithGap - bStartX) * eased;
        opponent.getHitBox().y = bStartY + (bEndY - bStartY) * eased - arcWave;

        appendTwinRibbons(player, 4.6f * SCALE);

        if (stateTick % 2 == 0) {
            context.getEffectManager().spawnDustParticles(player.getHitBox().getCenterX(), player.getHitBox().getCenterY(), 2, DustType.DASH, -travelDir, player);
            context.getEffectManager().spawnDustParticles(opponent.getHitBox().getCenterX(), opponent.getHitBox().getCenterY(), 2, DustType.SW_AURA_CRACKLE, 0, null);
        }

        int dashFrame = 2 + Math.min(2, (int) (t * 3));
        player.setUltimateAnimation(Anim.DASH, dashFrame);
        player.recordUltimateAfterimage();

        Direction oppTravelFace = (bEndX >= bStartX) ? Direction.RIGHT : Direction.LEFT;
        opponent.setCinematicPose(config.bossZipAnim(), config.bossZipFrame(), oppTravelFace, true);

        if (stateTick >= duration) {
            this.state = State.CLASH_WINDOW;
            this.stateTick = 0;
            this.glintChimePlayed = false;

            player.setFacingDirection(playerOnLeft ? 1 : -1);
            player.setUltimateAnimation(Anim.AIR_ATTACK, 1);
            opponent.setCinematicPose(config.bossRecoilAnim(), config.bossRecoilFrame(), playerOnLeft ? Direction.LEFT : Direction.RIGHT, true);
        }
    }

    private void updateClashWindow(Player player) {
        stateTick++;
        int duration = getWindowDuration(clashIndex);
        double progress = (double) stateTick / duration;

        if (!glintChimePlayed && progress >= (1.0 - GOOD_DELTA)) {
            glintChimePlayed = true;
            Audio.getInstance().getAudioPlayer().playSound(Sound.BTN_CLICK);
        }

        double clamped = Math.min(1.0, progress);
        double easeIn = clamped * clamped;
        double windupGap = (1.0 - easeIn) * (15.0 * SCALE);
        double floatBobY = Math.sin(stateTick * 0.14) * (3.2 * SCALE);

        player.setFacingDirection(playerOnLeft ? 1 : -1);
        player.getHitBox().x = pEndX + (playerOnLeft ? -windupGap : windupGap);
        player.getHitBox().y = pEndY + floatBobY;

        opponent.getHitBox().x = bEndX + (playerOnLeft ? windupGap : -windupGap);
        opponent.getHitBox().y = bEndY - floatBobY * 0.8;

        Direction oppFace = playerOnLeft ? Direction.LEFT : Direction.RIGHT;
        if (progress >= 0.62) {
            player.setUltimateAnimation(Anim.AIR_ATTACK, 1);
            opponent.setCinematicPose(config.bossClashAnim(), config.bossClashFrame(), oppFace, true);
        }
        else {
            player.setUltimateAnimation(Anim.JUMP, 1);
            opponent.setCinematicPose(config.bossRecoilAnim(), config.bossRecoilFrame(), oppFace, true);
        }

        if (stateTick % 9 == 0) {
            context.getEffectManager().spawnDustParticles(clashMidX, clashMidY, 2, DustType.IMPACT_SPARK, 0, null);
        }
        if (stateTick > duration + (int) (duration * GOOD_DELTA)) {
            onClashExchangeLost();
        }
    }

    private void updateClashPause(Player player) {
        stateTick++;
        double pushOut = Math.min(1.0, (double) stateTick / PAUSE_TICKS) * (12.0 * SCALE);
        player.getHitBox().x = pEndX + (playerOnLeft ? -pushOut : pushOut);
        opponent.getHitBox().x = bEndX + (playerOnLeft ? pushOut : -pushOut);

        if (stateTick == 6) {
            player.setUltimateAnimation(Anim.AIR_ATTACK, 4);
        }

        if (stateTick >= PAUSE_TICKS) {
            clashIndex++;
            if (clashIndex >= config.normalizedAnchors().length) {
                setupFinisherSlam(player);
            }
            else setupZipToNextAnchor(player, clashIndex);
        }
    }

    private void updateFinisherSlam(Player player) {
        stateTick++;
        double t = Math.min(1.0, (double) stateTick / SLAM_TICKS);
        double eased = t * t;

        player.getHitBox().x = pStartX + (pEndX - pStartX) * t;
        player.getHitBox().y = pStartY + (pEndY - pStartY) * eased;
        opponent.getHitBox().x = bStartX + (bEndX - bStartX) * t;
        opponent.getHitBox().y = bStartY + (bEndY - bStartY) * eased;

        appendTwinRibbons(player, 5.8f * SCALE);

        if (stateTick % 2 == 0) {
            context.getEffectManager().spawnDustParticles(opponent.getHitBox().getCenterX(), opponent.getHitBox().getCenterY(), 3, DustType.SW_AURA_CRACKLE, 0, null);
        }

        if (t < 0.75) player.setUltimateAnimation(Anim.FALL, 1);
        else player.setUltimateAnimation(Anim.CROUCH_ATTACK, 2);

        player.recordUltimateAfterimage();
        opponent.setCinematicPose(config.bossRecoilAnim(), config.bossRecoilFrame(), opponent.getDirection(), true);

        if (stateTick >= SLAM_TICKS) {
            double finisherDamage = config.finisherBaseDamage() + (perfectCount * config.finisherPerPerfectBonus());
            applyOpponentDamage(finisherDamage, true);

            if (config.finishSound() != null) {
                Audio.getInstance().getAudioPlayer().playSound(config.finishSound());
            }
            Audio.getInstance().getAudioPlayer().playHitSound();
            EventBus.getInstance().publish(new HitStopEvent(14));
            context.getScreenEffectsManager().triggerShake(26, 14.0);
            context.getScreenEffectsManager().triggerLightningStrobe(config.primaryColor(), 195, true);

            int sx = (int) opponent.getHitBox().getCenterX() - context.getCamera().getXOffset();
            int sy = (int) opponent.getHitBox().getMaxY() - context.getCamera().getYOffset();
            context.getScreenEffectsManager().triggerShockwave(sx, sy, scale(125), 26, config.primaryColor());
            context.getScreenEffectsManager().triggerShockwave(sx, sy, scale(75), 18, config.accentColor());

            context.getEffectManager().spawnDustParticles(opponent.getHitBox().getCenterX(), opponent.getHitBox().getMaxY(), 25, DustType.CRITICAL_HIT, 0, null);
            context.getEffectManager().spawnDustParticles(opponent.getHitBox().getCenterX(), opponent.getHitBox().getMaxY(), 18, DustType.IMPACT_SPARK, 0, null);

            if (opponent.isAlive() && opponent.getCurrentHealth() > 0) {
                opponent.freeze(config.stunRewardTicks());
            }
            finishClash(player, true);
        }
    }

    private void updateDefeatKnockback(Player player) {
        stateTick++;
        double t = Math.min(1.0, (double) stateTick / SLAM_TICKS);
        double eased = t * t;

        player.getHitBox().x = pStartX + (pEndX - pStartX) * t;
        player.getHitBox().y = pStartY + (pEndY - pStartY) * eased;
        opponent.getHitBox().x = bStartX + (bEndX - bStartX) * t;
        opponent.getHitBox().y = bStartY + (bEndY - bStartY) * eased;

        if (stateTick >= SLAM_TICKS) {
            finishClash(player, false);
        }
    }

    private void applyOpponentDamage(double dmg, boolean isCrit) {
        if (opponent == null || !opponent.isAlive()) return;
        opponent.hit(dmg, false, false);
        String text = isCrit ? String.format("%.0f!", dmg) : String.format("%.0f", dmg);
        context.getEffectManager().spawnDamageNumber(text, opponent.getHitBox().getCenterX(), opponent.getHitBox().y, isCrit ? CRITICAL_COLOR : DAMAGE_COLOR);
    }

    private void finishClash(Player player, boolean playerWon) {
        player.getHitBox().y = arenaFloorY - player.getHitBox().height - 1;
        player.setInAir(false);
        player.setAirSpeed(0);
        player.setUltimateAnimation(Anim.IDLE, 0);

        if (opponent != null) {
            opponent.getHitBox().y = arenaFloorY - opponent.getHitBox().height - 1;
            opponent.setCinematicPose(Anim.IDLE, 0, opponent.getDirection(), false);
            if (!playerWon && opponent.isAlive()) {
                opponent.getCooldown()[0] = 10;
            }
        }

        context.getScreenEffectsManager().setLetterboxActive(false);
        context.getLightManager().releaseAmbientDarkness();

        this.state = State.INACTIVE;
        this.opponent = null;
        this.config = null;
        this.ribbonTrails.clear();
        this.clashNodes.clear();
    }

    // Rendering
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (state == State.INACTIVE || config == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        renderTwinCometRibbons(g2d, xLevelOffset, yLevelOffset);
        if (isPlayerZipping()) {
            renderAnimeSpeedLines(g2d);
        }
        if (flareTick > 0) {
            renderClashImpactStar(g2d, xLevelOffset, yLevelOffset);
        }
        if (state == State.CLASH_WINDOW) {
            renderAnimeBladeTension(g2d, xLevelOffset, yLevelOffset);
        }

        g2d.dispose();
    }

    private void renderTwinCometRibbons(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        for (RibbonSegment seg : ribbonTrails) {
            int x1 = (int) (seg.x1 - xLevelOffset);
            int y1 = (int) (seg.y1 - yLevelOffset);
            int x2 = (int) (seg.x2 - xLevelOffset);
            int y2 = (int) (seg.y2 - yLevelOffset);

            float a = Math.max(0.0f, Math.min(1.0f, seg.alpha));
            int glowAlpha = (int) (a * 125);
            int coreAlpha = (int) (a * 235);

            g2d.setColor(new Color(seg.color.getRed(), seg.color.getGreen(), seg.color.getBlue(), glowAlpha));
            g2d.setStroke(new BasicStroke(seg.thickness * (0.45f + 0.55f * a), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);

            g2d.setColor(new Color(255, 250, 235, coreAlpha));
            g2d.setStroke(new BasicStroke(Math.max(1.2f, seg.thickness * 0.34f * a), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);
        }

        Color primary = config.primaryColor();
        for (double[] node : clashNodes) {
            int nx = (int) (node[0] - xLevelOffset);
            int ny = (int) (node[1] - yLevelOffset);
            int alpha = Math.max(0, Math.min(255, (int) (node[2] * 170)));
            int mark = scale(7);
            g2d.setColor(new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), alpha));
            g2d.setStroke(new BasicStroke(2.0f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(nx - mark, ny - mark, nx + mark, ny + mark);
            g2d.drawLine(nx - mark, ny + mark, nx + mark, ny - mark);
        }
    }

    /**
     * Draws kinetic anime motion-streaks across the viewport aligned with the fighters' travel vector during high-speed zips.
     */
    private void renderAnimeSpeedLines(Graphics2D g2d) {
        double dx = pEndX - pStartX;
        double dy = pEndY - pStartY;
        double len = Math.hypot(dx, dy);
        if (len < 1.0) return;

        double ux = dx / len;
        double uy = dy / len;

        Color accent = config.accentColor();
        g2d.setStroke(new BasicStroke(1.5f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i < 10; i++) {
            int sx = rand.nextInt(GAME_WIDTH);
            int sy = scale(32) + rand.nextInt(Math.max(1, GAME_HEIGHT - scale(64)));
            double streakLen = (45 + rand.nextInt(65)) * SCALE;
            int ex = (int) (sx + ux * streakLen);
            int ey = (int) (sy + uy * streakLen);

            int alpha = 45 + rand.nextInt(65);
            g2d.setColor((i % 2 == 0) ? new Color(255, 255, 255, alpha) : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha));
            g2d.drawLine(sx, sy, ex, ey);
        }
    }

    private void renderAnimeBladeTension(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        int cx = (int) (clashMidX - xLevelOffset);
        int cy = (int) (clashMidY - yLevelOffset);

        int duration = getWindowDuration(clashIndex);
        double progress = Math.min(1.30, (double) stateTick / duration);
        double remaining = Math.max(0.0, 1.0 - progress);
        double delta = Math.abs(1.0 - progress);

        boolean inPerfect = delta <= PERFECT_DELTA;
        boolean inGood = delta <= GOOD_DELTA;

        double maxOffset = 72.0 * SCALE;
        double currentOffset = remaining * maxOffset;
        double bladeLen = (24.0 + (1.0 - remaining) * 14.0) * SCALE;
        double bladeThick = (2.6 + (inGood ? 1.4 : 0.0)) * SCALE;

        Color leftCol = playerOnLeft ? new Color(255, 195, 75) : config.accentColor();
        Color rightCol = playerOnLeft ? config.accentColor() : new Color(255, 195, 75);

        drawConvergingNeedle(g2d, cx - currentOffset, cy, -1, bladeLen, bladeThick, leftCol);
        drawConvergingNeedle(g2d, cx + currentOffset, cy, 1, bladeLen, bladeThick, rightCol);

        int axisH = scale(22);
        g2d.setColor(new Color(255, 255, 255, inGood ? 185 : 70));
        g2d.setStroke(new BasicStroke(1.2f * SCALE));
        g2d.drawLine(cx, cy - axisH, cx, cy + axisH);

        if (inGood) {
            float intensity = inPerfect ? 1.0f : (float) (1.0 - (delta - PERFECT_DELTA) / (GOOD_DELTA - PERFECT_DELTA)) * 0.78f;
            double outerArm = (inPerfect ? 44.0 : 28.0) * SCALE * (0.75 + 0.35 * intensity);
            double innerCore = (inPerfect ? 4.8 : 3.2) * SCALE;
            Color glintCol = inPerfect ? config.primaryColor() : config.accentColor();

            AffineTransform old = g2d.getTransform();
            g2d.translate(cx, cy);
            g2d.rotate(Math.toRadians(inPerfect ? 45.0 : 22.0));

            Path2D.Double outer = buildFourPointStar(outerArm, innerCore * 1.4);
            g2d.setColor(new Color(glintCol.getRed(), glintCol.getGreen(), glintCol.getBlue(), Math.min(255, (int) (intensity * 235))));
            g2d.fill(outer);

            Path2D.Double inner = buildFourPointStar(outerArm * 0.72, innerCore * 0.8);
            g2d.setColor(new Color(255, 255, 255, Math.min(255, (int) (intensity * 255))));
            g2d.fill(inner);
            g2d.setTransform(old);
        }
    }

    private void drawConvergingNeedle(Graphics2D g2d, double tipX, double cy, int tailDir, double len, double halfH, Color glowColor) {
        Path2D.Double outer = new Path2D.Double();
        outer.moveTo(tipX, cy);
        outer.quadTo(tipX + tailDir * (len * 0.35), cy - halfH, tipX + tailDir * len, cy);
        outer.quadTo(tipX + tailDir * (len * 0.35), cy + halfH, tipX, cy);
        outer.closePath();

        g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 200));
        g2d.fill(outer);

        Path2D.Double core = new Path2D.Double();
        core.moveTo(tipX, cy);
        core.quadTo(tipX + tailDir * (len * 0.35), cy - halfH * 0.42, tipX + tailDir * (len * 0.85), cy);
        core.quadTo(tipX + tailDir * (len * 0.35), cy + halfH * 0.42, tipX, cy);
        core.closePath();
        g2d.setColor(Color.WHITE);
        g2d.fill(core);
    }

    private void renderClashImpactStar(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        float life = flareTick / 22.0f;
        int sx = (int) (clashMidX - xLevelOffset);
        int sy = (int) (clashMidY - yLevelOffset);
        double outerR = (42.0 * Math.sin(life * Math.PI * 0.75) + 10.0) * SCALE;
        double innerR = 4.5 * life * SCALE;

        AffineTransform old = g2d.getTransform();
        g2d.translate(sx, sy);
        g2d.rotate(Math.toRadians(22 + (1.0 - life) * 35));

        Path2D.Double star = buildFourPointStar(outerR, innerR);
        Color primary = config.primaryColor();
        g2d.setColor(new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), Math.max(0, Math.min(255, (int) (life * 220)))));
        g2d.fill(star);
        g2d.setColor(new Color(255, 255, 255, Math.max(0, Math.min(255, (int) (life * 255)))));
        g2d.scale(0.65, 0.65);
        g2d.fill(star);

        g2d.setTransform(old);
    }

    private Path2D.Double buildFourPointStar(double outerR, double innerR) {
        Path2D.Double star = new Path2D.Double();
        for (int i = 0; i < 8; i++) {
            double a = i * (Math.PI / 4.0);
            double r = (i % 2 == 0) ? outerR : innerR;
            double px = Math.cos(a) * r;
            double py = Math.sin(a) * r;
            if (i == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();
        return star;
    }

    public boolean isActive() {
        return state != State.INACTIVE;
    }

    private boolean isPlayerZipping() {
        return state == State.LAUNCH_UP || state == State.ZIP_TO_POINT || state == State.FINISHER_SLAM;
    }

    public void reset() {
        if (state != State.INACTIVE && context != null) {
            context.getScreenEffectsManager().setLetterboxActive(false);
            context.getLightManager().releaseAmbientDarkness();
        }
        this.state = State.INACTIVE;
        this.opponent = null;
        this.config = null;
        this.cooldownTicks = 0;
        this.ribbonTrails.clear();
        this.clashNodes.clear();
        this.flareTick = 0;
    }
}