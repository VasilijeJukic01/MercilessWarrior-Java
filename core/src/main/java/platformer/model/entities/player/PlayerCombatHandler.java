package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.audio.Audio;
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
import platformer.model.inventory.InventoryBonus;
import platformer.model.levels.LevelManager;
import platformer.model.perks.PerksBonus;
import platformer.model.projectiles.ProjectileManager;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.isFloor;

/**
 * Manages the player's melee combo chain, attack hitbox, forward slash lunge, hit recoil, active parry window, and spell stamina drain.
 */
public class PlayerCombatHandler {

    private static final int PARRY_WINDOW_TICKS = 45;
    private static final long COMBO_RESET_TIME_MS = 1500;
    private static final double RECOIL_DECAY = 0.85;
    private static final double LUNGE_DECAY = 0.86;

    private static final AttackState[] COMBO_SEQUENCE = {
            AttackState.ATTACK_1,
            AttackState.ATTACK_2,
            AttackState.ATTACK_3
    };

    private final Player player;
    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;
    private final LevelManager levelManager;
    private final ProjectileManager projectileManager;

    private int comboStep = 0;
    private int activeComboIndex = 0;
    private long lastAttackEndTime = 0;
    private boolean attackBuffered = false;
    private AttackState attackState = AttackState.ATTACK_1;

    private int blockWindowTimer = 0;
    private double recoilSpeedX = 0.0;
    private double attackLungeSpeedX = 0.0;

    public PlayerCombatHandler(Player player, GameContext context) {
        this.player = player;
        this.enemyManager = context.getEnemyManager();
        this.objectManager = context.getObjectManager();
        this.levelManager = context.getLevelManager();
        this.projectileManager = context.getProjectileManager();
    }

    /**
     * Cancels any active melee swing or parry stance so the player can immediately dash or jump.
     */
    public void cancelForMobility() {
        attackBuffered = false;
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
        long now = System.currentTimeMillis();
        boolean comboExpired = (now - lastAttackEndTime) > COMBO_RESET_TIME_MS;
        if (comboExpired) {
            comboStep = 0;
        }

        if (comboStep == 0) {
            if (currentCd > 0) return;
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
        if (player.checkAction(PlayerAction.BLOCK)) {
            player.removeAction(PlayerAction.BLOCK);
            blockWindowTimer = 0;
        }

        long now = System.currentTimeMillis();
        if (now - lastAttackEndTime > COMBO_RESET_TIME_MS) {
            comboStep = 0;
        }

        activeComboIndex = comboStep;
        AttackState nextAttack = COMBO_SEQUENCE[activeComboIndex];
        setPlayerAttackState(nextAttack);

        comboStep = (comboStep + 1) % COMBO_SEQUENCE.length;
        lastAttackEndTime = now;
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
        double cd = ((baseCd * stepFactor) + PerksBonus.getInstance().getBonusCooldown()) * (1.0 - InventoryBonus.getInstance().getCooldown());
        player.getCooldown()[Cooldown.ATTACK.ordinal()] = Math.max(0.15, cd);
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
        lastAttackEndTime = System.currentTimeMillis();
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
        }
        else {
            player.removeAction(PlayerAction.BLOCK);
            this.blockWindowTimer = 0;
        }
    }

    public void updateParryWindow() {
        if (player.checkAction(PlayerAction.CAN_BLOCK)) {
            blockWindowTimer = 0;
            return;
        }
        if (player.checkAction(PlayerAction.BLOCK)) {
            if (blockWindowTimer > 0) {
                blockWindowTimer--;
                if (blockWindowTimer == 0) {
                    player.removeAction(PlayerAction.BLOCK);
                    BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
                    double blockCooldown = pb.getBlockCooldown() * (1.0 - InventoryBonus.getInstance().getCooldown());
                    player.getCooldown()[Cooldown.BLOCK.ordinal()] = Math.max(0.1, blockCooldown);
                }
            }
        }
    }

    public void finishAttackBlock() {
        boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
        if (canBlock) {
            Logger.getInstance().notify("Damage blocked successfully!", Message.INFORMATION);
            player.getUltimateHandler().addChargeFromParry();
            BalanceManager.PlayerBalance pb = BalanceManager.getInstance().getPlayerBalance();
            double blockCooldown = (pb.getBlockCooldown() * 0.45) * (1.0 - InventoryBonus.getInstance().getCooldown());
            player.getCooldown()[Cooldown.BLOCK.ordinal()] = Math.max(0.1, blockCooldown);

            boolean hasRipostePerk = PerksBonus.getInstance().isRestorePower();
            if (hasRipostePerk) {
                player.changeStamina(pb.getParryStaminaRestore());
            }
            int stunTicks = hasRipostePerk ? 220 : 150;
            for (Enemy enemy : enemyManager.getAllEnemies()) {
                if (enemy.isAlive() && Math.abs(enemy.getHitBox().getCenterX() - player.getHitBox().getCenterX()) < TILES_SIZE * 2.5) {
                    enemy.freeze(stunTicks);
                }
            }
        }
        blockWindowTimer = 0;
    }

    public void updateAttackBox() {
        if (player.getSpellState() != 0) return;

        Rectangle2D.Double hitBox = player.getHitBox();
        Rectangle2D.Double attackBox = player.getAttackBox();
        int flipSign = player.getFlipSign();

        boolean left = player.checkAction(PlayerAction.LEFT);
        boolean right = player.checkAction(PlayerAction.RIGHT);

        if ((right && left) || (!right && !left)) {
            if (flipSign == 1) attackBox.x = hitBox.x + hitBox.width + (int) (10 * SCALE);
            else attackBox.x = hitBox.x - hitBox.width - (int) (10 * SCALE);
        }

        boolean dash = player.checkAction(PlayerAction.DASH);
        if (right || (dash && flipSign == 1)) attackBox.x = hitBox.x + hitBox.width + (int) (10 * SCALE);
        else if (left || (dash && flipSign == -1)) attackBox.x = hitBox.x - hitBox.width - (int) (10 * SCALE);

        attackBox.y = hitBox.y + (int) (10 * SCALE);
    }

    public void updateAttack() {
        boolean attacking = player.checkAction(PlayerAction.ATTACK);
        boolean dashSlash = player.checkAction(PlayerAction.DASH) && PerksBonus.getInstance().isDashSlash();
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
            double drain = pb.getFlameStaminaDrainPerTick() * (1.0 - PerksBonus.getInstance().getFlameCostReduction());
            player.changeStamina(-drain);
            enemyManager.checkEnemySpellHit();
            levelManager.checkDecorationCut(player.getAttackBox());
            objectManager.checkObjectBreak(player.getAttackBox());
        }
        if (player.checkAction(PlayerAction.TRANSFORM)) {
            double drain = pb.getTransformStaminaDrainPerTick() * (1.0 - PerksBonus.getInstance().getTransformCostReduction());
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

    public void reset() {
        blockWindowTimer = 0;
        recoilSpeedX = 0.0;
        attackLungeSpeedX = 0.0;
        comboStep = 0;
        activeComboIndex = 0;
        attackBuffered = false;
    }
}