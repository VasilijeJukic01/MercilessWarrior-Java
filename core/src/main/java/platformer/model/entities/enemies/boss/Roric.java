package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.debug.DebugSettings;
import platformer.event.EventBus;
import platformer.event.events.roric.RoricEffectEvent;
import platformer.event.events.roric.RoricPhaseChangeEvent;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.boss.roric.RoricAttackHandler;
import platformer.model.entities.enemies.boss.roric.RoricPhaseManager;
import platformer.model.entities.enemies.boss.roric.RoricState;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

/**
 * Represents the Roric boss entity in the game. This class manages Roric's physical presence,
 * state, rendering, and core mechanics like movement and physics. The complex AI, decision-making,
 * and attack sequencing are delegated to the {@link RoricAttackHandler} class, following a state machine pattern.
 * <p>
 * Roric is designed as a timed encounter and does not take damage. His behavior is characterized by unique
 * aerial mobility, a variety of ranged and area-denial attacks, and the ability to summon clones.
 * The physics model for his jump is custom, using an asymmetric gravity system to create a controlled, floaty arc.
 *
 * @see RoricAttackHandler
 * @see RoricState
 */
public class Roric extends Enemy {

    private final RoricAttackHandler attackHandler;
    private final RoricPhaseManager phaseManager;
    private RoricState state = RoricState.IDLE;
    private boolean start = false;
    private boolean isVisible = true;

    // Physics
    private static final double UPWARD_GRAVITY = 0.016 * SCALE;
    private static final double DOWNWARD_GRAVITY = 0.025 * SCALE;
    private static final double COLLISION_FALL_SPEED = 0.6 * SCALE;
    protected double xSpeed = 0;
    protected final double jumpSpeed = -3.0 * SCALE;

    // Current state
    private SpellManager spellManager;
    private Player currentPlayerTarget;
    private int[][] currentLevelData;

    // Offsets
    private final int attackBoxOffsetX = (int) (40 * SCALE);
    private final int jumpOffsetX = (int) (1.5 * SCALE);

    public Roric(int xPos, int yPos) {
        super(xPos, yPos, RORIC_WIDTH, RORIC_HEIGHT, EnemyType.RORIC, 16);
        super.setDirection(Direction.LEFT);
        super.inAir = true;
        initHitBox();
        initAttackBox();
        this.phaseManager = new RoricPhaseManager(this);
        this.attackHandler = new RoricAttackHandler(this, phaseManager);
        super.cooldown = new double[1];
    }

    private void initHitBox() {
        super.initHitBox(RORIC_HB_WID, RORIC_HB_HEI);
        hitBox.x += RORIC_HB_OFFSET_X;
        hitBox.y += RORIC_HB_OFFSET_Y;
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos - (15 * SCALE), yPos + (15 * SCALE), RORIC_AB_WID, RORIC_AB_HEI);
    }

    @Override
    public void update(int[][] levelData, Player player) {
        // Not used
    }

    /**
     * The main update loop for Roric. This method orchestrates calls to update movement, animations, and the AI state machine.
     * It serves as the integration point for physics (from {@code updateMove}) and AI (from {@code attackHandler.update}),
     * and also manages the boss HUD visibility.
     *
     * @param levelData The collision map of the current level.
     * @param player The player entity.
     * @param spellManager Manager for spell effects.
     * @param enemyManager Manager for enemy entities (used for spawning clones).
     * @param projectileManager Manager for projectiles.
     * @param bossInterface The UI component for the boss health bar.
     */
    public void update(int[][] levelData, Player player, SpellManager spellManager, EnemyManager enemyManager, ProjectileManager projectileManager, BossInterface bossInterface) {
        this.spellManager = spellManager;
        this.currentPlayerTarget = player;
        this.currentLevelData = levelData;
        if (!start && isPlayerCloseForAttack(player)) {
            start = true;
            phaseManager.startFight();
            EventBus.getInstance().publish(new RoricPhaseChangeEvent(RoricPhaseManager.RoricPhase.INTRO));
            if (DebugSettings.getInstance().isRoricDebugMode()) {
                Audio.getInstance().getAudioPlayer().playSong(Song.BOSS_2, DebugSettings.getInstance().getRoricFightStartOffsetMs());
            }
            else Audio.getInstance().getAudioPlayer().playSong(Song.BOSS_2);
            attackHandler.startOpeningAttack(player);
        }
        phaseManager.update();
        // Delegate to handler (for specials)
        if (state == RoricState.SKYFALL_BARRAGE || state == RoricState.CELESTIAL_RAIN) {
            attackHandler.update(levelData, player, spellManager, enemyManager, projectileManager);
            return;
        }
        updateMove(levelData, player, spellManager, projectileManager, enemyManager);
        updateAnimation();
        updateAttackBox();
        updateBossInterface(bossInterface);
    }

    /**
     * Manages Roric's movement and physics updates.
     * It delegates state-based decisions to the {@link RoricAttackHandler} and applies kinematic updates if Roric is airborne.
     *
     * @param levelData Collision data for the level.
     * @param player The player entity.
     * @param spellManager The spell manager for handling spells.
     * @param projectileManager The projectile manager for handling projectiles.
     * @param enemyManager The enemy manager for handling other enemies.
     */
    protected void updateMove(int[][] levelData, Player player, SpellManager spellManager, ProjectileManager projectileManager, EnemyManager enemyManager) {
        if (!isEntityOnFloor(hitBox, levelData) && !inAir) inAir = true;
        attackHandler.update(levelData, player, spellManager, enemyManager, projectileManager);
        if (inAir) {
            updateInAir(levelData, 0, COLLISION_FALL_SPEED);
        }
        else {
            if (state != RoricState.JUMPING && state != RoricState.AERIAL_ATTACK) {
                airSpeed = 0;
                xSpeed = 0;
            }
        }
    }

    /**
     * Overrides the default entity air physics to implement Roric's unique jump arc.
     * This method applies a simple kinematic model using an asymmetric gravitational system.
     * A lower gravity (upwardGravity) is applied during ascent, while a higher gravity (downwardGravity)
     * is applied during descent. This creates a controlled "floaty" jump parabola.
     * <p>
     * The AI can also pause vertical movement midair by setting the `isFloating` flag in the attack handler.
     *
     * @param levelData The level's collision map.
     * @param gravity Unused in this override; custom gravity values are applied internally.
     * @param collisionFallSpeed The vertical speed to set when hitting a ceiling.
     */
    @Override
    protected void updateInAir(int[][] levelData, double gravity, double collisionFallSpeed) {
        if (attackHandler.isFloating() || state == RoricState.CELESTIAL_RAIN) return;

        if (airSpeed < 0) airSpeed += UPWARD_GRAVITY;
        else airSpeed += DOWNWARD_GRAVITY;

        if (canMoveHere(hitBox.x + xSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) hitBox.x += xSpeed;
        else xSpeed = 0;

        if (canMoveHere(hitBox.x, hitBox.y + airSpeed, hitBox.width, hitBox.height, levelData)) hitBox.y += airSpeed;
        else {
            hitBox.y = getYPosOnTheCeil(hitBox, airSpeed);
            if (airSpeed > 0) {
                inAir = false;
                airSpeed = 0;
                xSpeed = 0;
                boolean actionQueuedAndHandled = attackHandler.onLanding();
                if (!actionQueuedAndHandled) {
                    setState(RoricState.IDLE);
                    setEnemyAction(Anim.IDLE);
                    setAttackCooldown(RORIC_IDLE_COOLDOWN * phaseManager.getCooldownModifier());
                }
            }
            else airSpeed = collisionFallSpeed;
        }
    }

    /**
     * Updates Roric's animation frame.
     * <p>
     * This logic is coupled with the physics state for the `JUMP_FALL` animation, it selects the correct sprite based on the sign
     * of the vertical velocity (`airSpeed`). This ensures the visual representation matches the kinematic state.
     */
    @Override
    protected void updateAnimation() {
        coolDownTickUpdate();
        animTick++;
        double effectiveAnimSpeed = animSpeed * phaseManager.getAnimationSpeedModifier();
        if (animTick >= effectiveAnimSpeed) {
            animTick = 0;
            animIndex++;
            if (entityState == Anim.JUMP_FALL) {
                if (airSpeed < 0 && animIndex > 8) {
                    animIndex = 8;
                }
                else if (airSpeed >= 0 && animIndex < 9) {
                    animIndex = 9;
                }
            }
            if (animIndex >= SpriteManager.getInstance().getAnimFrames(getEnemyType(), entityState)) {
                if (entityState != Anim.JUMP_FALL) attackHandler.finishAnimation();
            }
        }
    }

    /**
     * Dynamically updates the position and size of Roric's attack box based on the current attack animation.
     * For the {@code ATTACK_1} (Arrow Strike), the hitbox is narrowed and positioned directly in front of Roric
     * to match the swing of his blade. For other states, it reverts to a default configuration.
     */
    protected void updateAttackBox() {
        if (entityState == Anim.ATTACK_1) {
            attackBox.width = RORIC_AB_WID_REDUCE;
            if (getDirection() == Direction.RIGHT) {
                attackBox.x = hitBox.x + hitBox.width;
            }
            else attackBox.x = hitBox.x - RORIC_AB_WID_REDUCE;
        }
        else {
            attackBox.width = RORIC_AB_WID;
            attackBox.x = hitBox.x - attackBoxOffsetX;
        }
        attackBox.y = hitBox.y;
    }

    private void updateBossInterface(BossInterface bossInterface) {
        if (bossInterface != null) {
            if (!bossInterface.isActive() && start) bossInterface.injectBoss(this);
            else if (bossInterface.isActive() && !start) bossInterface.reset();
        }
    }

    /**
     * Initiates a ballistic trajectory (a jump). Sets the initial vertical velocity and calculates a horizontal velocity component
     * aimed towards the side of the arena with more open space, making the jump a strategic repositioning maneuver
     * rather than a simple vertical movement.
     *
     * @param levelData The level's collision map.
     */
    public void jump(int[][] levelData) {
        if (inAir) return;
        inAir = true;
        airSpeed = jumpSpeed;
        setEnemyAction(Anim.JUMP_FALL);
        int currentTileX = (int) (hitBox.x / TILES_SIZE);
        int spaceToRight = levelData.length - currentTileX;
        this.xSpeed = (spaceToRight > currentTileX) ? jumpOffsetX : -jumpOffsetX;
        setDirection(xSpeed > 0 ? Direction.RIGHT : Direction.LEFT);
        EventBus.getInstance().publish(new RoricEffectEvent(this, RoricEffectEvent.RoricEffectType.JUMP));
    }

    /**
     * A setup method for ranged attacks. It makes Roric face the player and sets the appropriate animation.
     *
     * @param player The player entity to aim at.
     */
    public void prepareToFire(Player player) {
        if (player.getHitBox().getCenterX() < this.getHitBox().getCenterX()) setDirection(Direction.LEFT);
        else setDirection(Direction.RIGHT);
        setEnemyAction(Anim.SPELL_2);
        attackCheck = false;
    }

    @Override
    public boolean hit(double damage, boolean special, boolean hitSound) {
        return false;
    }

    @Override
    public void spellHit(double damage) {
        // Timed boss, does not take damage
    }

    /**
     * Resets Roric to his initial state for retrying the fight.
     */
    @Override
    public void reset() {
        super.reset();
        setDirection(Direction.LEFT);
        setEnemyAction(Anim.IDLE);
        this.state = RoricState.IDLE;
        this.attackHandler.reset();
        this.phaseManager.reset();
        this.start = false;
        this.inAir = false;
        this.airSpeed = 0;
        this.xSpeed = 0;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (isVisible) renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (isVisible) renderAttackBox(g, xLevelOffset, yLevelOffset);
    }

    // Getters & Setters
    @Override
    public boolean isVisible() {
        return isVisible;
    }

    public RoricState getState() {
        return state;
    }

    public RoricAttackHandler getAttackHandler() {
        return attackHandler;
    }

    public RoricPhaseManager getPhaseManager() {
        return phaseManager;
    }

    public void setState(RoricState state) {
        this.state = state;
    }

    public double getAirSpeed() {
        return airSpeed;
    }

    public void setAirSpeed(double speed) {
        this.airSpeed = speed;
    }

    public void setXSpeed(double speed) {
        this.xSpeed = speed;
    }

    public boolean isAttackCheck() {
        return attackCheck;
    }

    public void setAttackCheck(boolean check) {
        this.attackCheck = check;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public void setAnimIndex(int index) {
        this.animIndex = index;
    }

    public boolean isStart() {
        return start;
    }

    public void setAlive(boolean alive) {
        super.alive = alive;
    }

    // Dependency Injection
    public Player getCurrentPlayerTarget() {
        return currentPlayerTarget;
    }

    public int[][] getLevelDataForAI() {
        return currentLevelData;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    public void setAttackCooldown(double value) {
        cooldown[Cooldown.ATTACK.ordinal()] = value;
    }
}
