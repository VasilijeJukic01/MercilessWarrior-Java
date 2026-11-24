package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.core.GameClock;
import platformer.core.GameContext;
import platformer.core.config.BalanceManager;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.AttackState;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.LevelManager;
import platformer.model.projectiles.ProjectileManager;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.isFloor;

/**
 * Manages the player's melee combo chain, attack hitbox, forward slash lunge, hit recoil, active parry window, recovery indicator state, and spell stamina drain.
 */
public class PlayerCombatHandler {

    private static final long COMBO_RESET_TICKS = GameClock.secondsToTicks(1.5);
    private static final int PARRY_WINDOW_TICKS = 45;
    private static final double RECOVERY_BUFFER_WINDOW = 0.16;
    private static final int RECOVERY_BUFFER_MAX_TICKS = 36;
    private static final int DENIED_FLASH_TICKS = 24;
    private static final int READY_FLASH_TICKS = 28;
    private static final double RECOIL_DECAY = 0.85;
    private static final double LUNGE_DECAY = 0.86;

    private static final AttackState[] COMBO_SEQUENCE = {
            AttackState.ATTACK_1,
            AttackState.ATTACK_2,
            AttackState.ATTACK_3
    };

    private final Player player;
    private final GameClock gameClock;
    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;
    private final LevelManager levelManager;
    private final ProjectileManager projectileManager;

    private int comboStep = 0;
    private int activeComboIndex = 0;
    private long lastAttackEndTick = 0;
    private boolean attackBuffered = false;
    private AttackState attackState = AttackState.ATTACK_1;

    private double maxAttackCooldown = 0.0;
    private boolean finisherCooldownActive = false;
    private int recoveryBufferTicks = 0;
    private int cooldownDeniedFlashTicks = 0;
    private int readyFlashTicks = 0;

    private boolean parryTriggeredThisBlock = false;
    private int blockWindowTimer = 0;
    private double recoilSpeedX = 0.0;
    private double attackLungeSpeedX = 0.0;

    public PlayerCombatHandler(Player player, GameContext context) {
        this.player = player;
        this.gameClock = context.getGameClock();
        this.enemyManager = context.getEnemyManager();
        this.objectManager = context.getObjectManager();
        this.levelManager = context.getLevelManager();
        this.projectileManager = context.getProjectileManager();
    }

    /**
     * Advances parry timers, combo expiration, overhead recovery feedback, and buffered post-cooldown swings.
     */
    public void update() {
        updateParryWindow();

        if (cooldownDeniedFlashTicks > 0) cooldownDeniedFlashTicks--;
        if (readyFlashTicks > 0) readyFlashTicks--;

        double currentCd = player.getCooldown()[Cooldown.ATTACK.ordinal()];
        if (finisherCooldownActive && currentCd <= 0) {
            finisherCooldownActive = false;
            maxAttackCooldown = 0.0;
            readyFlashTicks = READY_FLASH_TICKS;
        }

        if (!player.checkAction(PlayerAction.ATTACK) && comboStep > 0) {
            if (currentTick() - lastAttackEndTick > COMBO_RESET_TICKS) {
                comboStep = 0;
            }
        }

        if (recoveryBufferTicks > 0) {
            recoveryBufferTicks--;
            if (currentCd <= 0
                    && !player.checkAction(PlayerAction.ATTACK)
                    && !player.checkAction(PlayerAction.HIT)
                    && !player.checkAction(PlayerAction.DYING)
                    && !player.checkAction(PlayerAction.DASH)
                    && !player.checkAction(PlayerAction.ON_WALL)
                    && !player.checkAction(PlayerAction.BLOCK)
                    && player.getSpellState() == 0) {
                recoveryBufferTicks = 0;
                executeNextComboSwing();
            }
        }
    }

    /**
     * Cancels any active melee swing or parry stance so the player can immediately dash or jump.
     */
    public void cancelForMobility() {
        attackBuffered = false;
        recoveryBufferTicks = 0;
        if (player.checkAction(PlayerAction.ATTACK)) {
            player.removeAction(PlayerAction.ATTACK);
            player.setAttackCheck(false);
            recoilSpeedX = 0;
            attackLungeSpeedX = 0;
        }
        if (player.checkAction(PlayerAction.BLOCK) && !player.checkAction(PlayerAction.CAN_BLOCK)) {
            player.removeAction(PlayerAction.BLOCK);
            blockWindowTimer = 0;
        }
    }

    public void performAttack() {
        if (player.checkAction(PlayerAction.ATTACK)) {
            if (activeComboIndex < 2 && player.getAnimIndex() >= 1) {
                attackBuffered = true;
            }
            return;
        }

        double currentCd = player.getCooldown()[Cooldown.ATTACK.ordinal()];
        long now = currentTick();
        boolean comboExpired = (now - lastAttackEndTick) > COMBO_RESET_TICKS;
        if (comboExpired) {
            comboStep = 0;
        }

        if (comboStep == 0) {
            if (currentCd > 0) {
                if (currentCd <= RECOVERY_BUFFER_WINDOW) {
                    recoveryBufferTicks = RECOVERY_BUFFER_MAX_TICKS;
                }
                else cooldownDeniedFlashTicks = DENIED_FLASH_TICKS;
                return;
            }
        }
        else {
            if (currentCd > 0.35) {
                attackBuffered = true;
                return;
            }
        }
        executeNextComboSwing();
    }

    private void executeNextComboSwing() {
        attackBuffered = false;
        recoveryBufferTicks = 0;
        if (player.checkAction(PlayerAction.BLOCK)) {
            player.removeAction(PlayerAction.BLOCK);
            blockWindowTimer = 0;
        }

        long now = currentTick();
        if (now - lastAttackEndTick > COMBO_RESET_TICKS) {
            comboStep = 0;
        }

        activeComboIndex = comboStep;
        AttackState nextAttack = COMBO_SEQUENCE[activeComboIndex];
        setPlayerAttackState(nextAttack);

        comboStep = (comboStep + 1) % COMBO_SEQUENCE.length;
        lastAttackEndTick = now;
    }

    public void setPlayerAttackState(AttackState state) {
        this.attackState = state;
        player.setAttacking(true);
        player.setAttackCheck(false);
        player.resetAnimTickAndIndex();

        int flipSign = player.getFlipSign();
        boolean movingIntoSlash = (flipSign == 1 && player.checkAction(PlayerAction.RIGHT)) || (flipSign == -1 && player.checkAction(PlayerAction.LEFT));
        if (!player.isInAir()) {
            double lungeMult = movingIntoSlash ? 0.55 : 1.0;
            switch (state) {
                case ATTACK_1 -> this.attackLungeSpeedX = 0.55 * SCALE * lungeMult;
                case ATTACK_2 -> this.attackLungeSpeedX = 0.72 * SCALE * lungeMult;
                case ATTACK_3 -> this.attackLungeSpeedX = 1.05 * SCALE * lungeMult;
                default -> {}
            }
        }

        double baseCd = BalanceManager.getInstance().getPlayerBalance().getAttackCooldown();
        double stepFactor = (state == AttackState.ATTACK_3) ? 1.45 : 0.45;
        double cd = ((baseCd * stepFactor) + player.getStats().getPerks().getBonusCooldown()) * player.getStats().getCooldownMultiplier();
        double appliedCd = Math.max(0.15, cd);
        player.getCooldown()[Cooldown.ATTACK.ordinal()] = appliedCd;
        this.maxAttackCooldown = appliedCd;
        this.finisherCooldownActive = (state == AttackState.ATTACK_3);
        this.readyFlashTicks = 0;
    }

    /**
     * Returns the damage multiplier for the current step of the 3-hit combo chain.
     */
    public double getComboDamageMultiplier() {
        return switch (activeComboIndex) {
            case 1 -> 1.15;
            case 2 -> 1.40;
            default -> 1.0;
        };
    }

    public boolean isComboFinisher() {
        return activeComboIndex == 2;
    }

    public boolean consumeBufferedAttack() {
        lastAttackEndTick = currentTick();
        if (activeComboIndex >= 2) {
            attackBuffered = false;
            return false;
        }
        if (attackBuffered && !player.checkAction(PlayerAction.HIT) && !player.checkAction(PlayerAction.DYING)) {
            executeNextComboSwing();
            return true;
        }
        return false;
    }

    public void setBlock(boolean block) {
        if (player.getCooldown()[Cooldown.BLOCK.ordinal()] != 0 || player.checkAction(PlayerAction.ON_WALL)) return;
        if (block) {
            cancelForMobility();
            player.addAction(PlayerAction.BLOCK);
            this.blockWindowTimer = PARRY_WINDOW_TICKS;
            this.parryTriggeredThisBlock = false;
        }
        else {
            player.removeAction(PlayerAction.BLOCK);
            this.blockWindowTimer = 0;
            this.parryTriggeredThisBlock = false;
        }
    }

    public void updateParryWindow() {
        if (player.checkAction(PlayerAction.CAN_BLOCK)) {
            blockWindowTimer = 0;
            if (!parryTriggeredThisBlock) {
                parryTriggeredThisBlock = true;
                onParrySuccess();
            }
            return;
        }
        if (player.checkAction(PlayerAction.BLOCK)) {
            if (blockWindowTimer > 0) {
                blockWindowTimer--;
                if (blockWindowTimer == 0) {
                    player.removeAction(PlayerAction.BLOCK);
                    BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
                    double blockCooldown = (pb.getBlockCooldown() * 0.45) * player.getStats().getCooldownMultiplier();
                    player.getCooldown()[Cooldown.BLOCK.ordinal()] = Math.max(0.1, blockCooldown);
                }
            }
        }
    }

    /**
     * Triggers the exact frame a parry connects: fires clash VFX/camera punch, stuns nearby enemies immediately, restores stamina, and charges ultimate.
     */
    private void onParrySuccess() {
        Logger.getInstance().notify("Damage blocked successfully!", Message.INFORMATION);
        player.getFeedbackHandler().onParrySuccess();
        player.getUltimateHandler().addChargeFromParry();
        BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
        double blockCooldown = (pb.getBlockCooldown() * 0.35) * player.getStats().getCooldownMultiplier();
        player.getCooldown()[Cooldown.BLOCK.ordinal()] = Math.max(0.08, blockCooldown);

        boolean hasRipostePerk = player.getStats().getPerks().isRestorePower();
        if (hasRipostePerk) {
            player.changeStamina(pb.getParryStaminaRestore());
        }
        int stunTicks = hasRipostePerk ? 230 : 165;
        for (Enemy enemy : enemyManager.getAllEnemies()) {
            if (enemy.isAlive() && Math.abs(enemy.getHitBox().getCenterX() - player.getHitBox().getCenterX()) < TILES_SIZE * 2.75) {
                enemy.freeze(stunTicks);
            }
        }
    }

    public void finishAttackBlock() {
        blockWindowTimer = 0;
        parryTriggeredThisBlock = false;
    }

    public void updateAttackBox() {
        if (player.getSpellState() != 0) return;

        Rectangle2D.Double hitBox = player.getHitBox();
        Rectangle2D.Double attackBox = player.getAttackBox();
        int flipSign = player.getFlipSign();
        boolean left = player.checkAction(PlayerAction.LEFT);
        boolean right = player.checkAction(PlayerAction.RIGHT);
        boolean dash = player.checkAction(PlayerAction.DASH);

        double reachWidth = 30.0 * SCALE;
        attackBox.width = reachWidth;

        int facing = flipSign;
        if (right && !left) facing = 1;
        else if (left && !right) facing = -1;
        else if (dash) facing = flipSign;

        if (facing == 1) attackBox.x = hitBox.getMaxX();
        else attackBox.x = hitBox.x - reachWidth;
        attackBox.y = hitBox.y + (int) (10 * SCALE);
    }

    public void updateAttack() {
        boolean attacking = player.checkAction(PlayerAction.ATTACK);
        boolean dashSlash = player.checkAction(PlayerAction.DASH) && player.getStats().getPerks().isDashSlash();
        if (attacking || dashSlash) checkAttack();
    }

    private void checkAttack() {
        boolean dash = player.checkAction(PlayerAction.DASH);
        boolean dashHit = player.checkAction(PlayerAction.DASH_HIT);

        if (player.isAttackCheck() || player.getAnimIndex() != 1 || dashHit) return;
        player.setAttackCheck(!dash);

        Rectangle2D.Double attackBox = player.getAttackBox();
        HitResult hitResult = enemyManager.checkEnemyHit(attackBox, player);
        switch (hitResult) {
            case HIT -> {
                Audio.getInstance().getAudioPlayer().playHitSound();
                if (!dash) {
                    attackLungeSpeedX = 0;
                    applyRecoil(0.55 * SCALE);
                }
            }
            case BLOCK -> {
                if (!dash) {
                    attackLungeSpeedX = 0;
                    applyRecoil(1.05 * SCALE);
                }
            }
            case MISS -> {
                if (!dash) Audio.getInstance().getAudioPlayer().playSlashSound();
            }
        }

        objectManager.checkObjectBreak(attackBox);
        levelManager.checkDecorationCut(attackBox);
        projectileManager.checkProjectileDeflect(attackBox);
    }

    public void updateSpells() {
        BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
        if (player.getSpellState() == 1) {
            double drain = pb.getFlameStaminaDrainPerTick() * (1.0 - player.getStats().getPerks().getFlameCostReduction());
            player.changeStamina(-drain);
            enemyManager.checkEnemySpellHit();
            levelManager.checkDecorationCut(player.getAttackBox());
            objectManager.checkObjectBreak(player.getAttackBox());
        }
        if (player.checkAction(PlayerAction.TRANSFORM)) {
            double drain = pb.getTransformStaminaDrainPerTick() * (1.0 - player.getStats().getPerks().getTransformCostReduction());
            player.changeStamina(-drain);
        }
    }

    /**
     * Computes and decays the horizontal velocity contribution from hit recoil and forward attack lunge.
     */
    public double consumeCombatDisplacementX(int[][] levelData) {
        double dx = 0.0;
        int flipSign = player.getFlipSign();

        if (recoilSpeedX > 0) {
            dx += recoilSpeedX * -flipSign;
            recoilSpeedX *= RECOIL_DECAY;
            if (recoilSpeedX < 0.08) {
                recoilSpeedX = 0;
            }
        }

        if (attackLungeSpeedX > 0) {
            double lungeStep = attackLungeSpeedX * flipSign;
            Direction lungeDir = (flipSign == 1) ? Direction.RIGHT : Direction.LEFT;
            if (!player.isInAir() && isFloor(player.getHitBox(), lungeStep, levelData, lungeDir)) {
                dx += lungeStep;
            }
            attackLungeSpeedX *= LUNGE_DECAY;
            if (attackLungeSpeedX < 0.08) {
                attackLungeSpeedX = 0;
            }
        }

        return dx;
    }

    /**
     * Returns true when the overhead recovery bar should be displayed (during finisher recovery or after a denied attack press).
     */
    public boolean isShowingCooldownBar() {
        double currentCd = player.getCooldown()[Cooldown.ATTACK.ordinal()];
        return currentCd > 0 && (finisherCooldownActive || cooldownDeniedFlashTicks > 0);
    }

    /**
     * Returns the remaining attack cooldown ratio in [0.0, 1.0].
     */
    public float getAttackCooldownRatio() {
        double currentCd = player.getCooldown()[Cooldown.ATTACK.ordinal()];
        if (currentCd <= 0 || maxAttackCooldown <= 0) return 0.0f;
        return (float) Math.max(0.0, Math.min(1.0, currentCd / maxAttackCooldown));
    }

    /**
     * Returns the number of active combo pips (1 or 2) when building toward the exhaustion finisher, or 0 otherwise.
     */
    public int getActiveComboPips() {
        if (isShowingCooldownBar()) return 0;
        return comboStep;
    }

    public float getDeniedFlashRatio() {
        return (float) cooldownDeniedFlashTicks / DENIED_FLASH_TICKS;
    }

    public float getReadyFlashRatio() {
        return (float) readyFlashTicks / READY_FLASH_TICKS;
    }

    public boolean hasActiveImpulse() {
        return recoilSpeedX > 0.05 || attackLungeSpeedX > 0.05;
    }

    public void applyRecoil(double initialSpeed) {
        this.recoilSpeedX = initialSpeed;
    }

    public void clearImpulses() {
        this.recoilSpeedX = 0.0;
        this.attackLungeSpeedX = 0.0;
    }

    public void clearAttackLunge() {
        this.attackLungeSpeedX = 0.0;
    }

    public Anim getAttackAnim() {
        if (attackState == null) return Anim.ATTACK_1;
        return switch (attackState) {
            case ATTACK_1 -> Anim.ATTACK_1;
            case ATTACK_2 -> Anim.ATTACK_2;
            case ATTACK_3 -> Anim.ATTACK_3;
            default -> Anim.ATTACK_1;
        };
    }

    private long currentTick() {
        return gameClock != null ? gameClock.getElapsedTicks() : 0;
    }

    public void reset() {
        blockWindowTimer = 0;
        recoilSpeedX = 0.0;
        attackLungeSpeedX = 0.0;
        comboStep = 0;
        activeComboIndex = 0;
        attackBuffered = false;
        maxAttackCooldown = 0.0;
        finisherCooldownActive = false;
        recoveryBufferTicks = 0;
        cooldownDeniedFlashTicks = 0;
        readyFlashTicks = 0;
    }
}