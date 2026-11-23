package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.debug.Debug;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.loader.EnemyDefinition;
import platformer.model.entities.enemies.loader.EnemyRegistry;
import platformer.model.entities.follower.Follower;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.world.WorldMemoryManager;
import platformer.physics.DamageSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.*;

/**
 * Abstract base class for all enemies in the game, configured via {@link EnemyDefinition}.
 */
public abstract class Enemy extends Entity implements DamageSource, Debug<Graphics> {

    private final EnemyType enemyType;
    protected final EnemyDefinition definition;
    protected CombatDirector combatDirector;
    protected double enemySpeed;
    protected int originalAnimSpeed, animSpeed, animIndex, animTick;
    protected Direction direction = Direction.RIGHT;
    protected boolean alive = true;
    private boolean criticalHit;
    protected int freezeTick = 0;
    protected int stunImmunityTick = 0;

    private static final double PATROL_TETHER_RADIUS = 3.2 * TILES_SIZE;
    protected int patrolPauseTicks = 0;
    protected boolean terrified = false;
    protected boolean cowering = false;

    protected Random rand = new Random();

    public Enemy(int xPos, int yPos, EnemyType enemyType) {
        this(xPos, yPos, enemyType, EnemyRegistry.getInstance().get(enemyType));
    }

    private Enemy(int xPos, int yPos, EnemyType enemyType, EnemyDefinition definition) {
        super(xPos, yPos, definition.getDrawWidth(), definition.getDrawHeight());
        this.enemyType = enemyType;
        this.definition = definition;
        this.animSpeed = this.originalAnimSpeed = definition.getDefaultAnimSpeed();
        this.animTick = rand.nextInt(Math.max(1, this.animSpeed));
        this.enemySpeed = definition.getScaledWalkSpeed();
        this.maxHealth = enemyType.getHealth();
        this.currentHealth = maxHealth;
        this.cooldown = new double[Cooldown.values().length];
        initHitBox(definition.getHitboxWidth(), definition.getHitboxHeight());
        if (!definition.isBoss()) {
            setDirection(rand.nextBoolean() ? Direction.LEFT : Direction.RIGHT);
            this.patrolPauseTicks = rand.nextInt(120);
        }
    }

    /**
     * Updates the enemy's state for a single engine tick.
     */
    public abstract void update(int[][] levelData, Player player, Entity follower);

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= SpriteManager.getInstance().getAnimFrames(enemyType, entityState)) {
                finishAnimation();
            }
        }
        if (cooldown != null) coolDownTickUpdate();
    }

    private void finishAnimation() {
        animIndex = 0;
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.SPELL_1 || entityState == Anim.HIT || entityState == Anim.BLOCK || entityState == Anim.REVEAL) {
            if (isAttacking()) {
                double minCd = definition.getAttackCooldownMin();
                double range = Math.max(0.0, definition.getAttackCooldownMax() - minCd);
                cooldown[Cooldown.ATTACK.ordinal()] = minCd + rand.nextDouble() * range;
                releaseCombatToken();
            }
            if (entityState == Anim.HIT) {
                enemySpeed = definition.getScaledWalkSpeed();
            }
            entityState = Anim.IDLE;
        }
        else if (entityState == Anim.HIDE) entityState = Anim.REVEAL;
        else if (entityState == Anim.DEATH) {
            releaseCombatToken();
            alive = false;
        }
    }

    // Combat Director Token Helpers
    public void setCombatDirector(CombatDirector combatDirector) {
        this.combatDirector = combatDirector;
    }

    protected boolean requestMeleeToken() {
        return combatDirector == null || combatDirector.requestMeleeAttack(this);
    }

    protected boolean requestRangedToken() {
        return combatDirector == null || combatDirector.requestRangedAttack(this);
    }

    protected void releaseCombatToken() {
        if (combatDirector != null) {
            combatDirector.releaseToken(this);
        }
    }

    // Targeting
    protected Entity getCloseTarget(Player player, Entity follower) {
        if (follower == null) return player;
        if (follower instanceof Follower f && f.isKnockedDown()) {
            return player;
        }

        double distToPlayer = Math.abs(player.getHitBox().x - hitBox.x);
        double distToFollower = Math.abs(follower.getHitBox().x - hitBox.x);
        return (distToFollower < distToPlayer) ? follower : player;
    }

    protected boolean canSeeEntity(int[][] levelData, Entity entity) {
        int yTilePlayer = (int) (entity.getHitBox().y / TILES_SIZE) + 1;
        int yTileEnemy = (int) (hitBox.y / TILES_SIZE) + 1;
        if (yTilePlayer != yTileEnemy) return false;
        if (!isEntityInSight(entity)) return false;
        return isSightClear(levelData, hitBox, entity.getHitBox(), yTileEnemy);
    }

    protected void directToEntity(Entity entity) {
        entityState = Anim.RUN;
        enemySpeed = definition.getScaledRunSpeed();
        faceEntity(entity);
    }

    protected void faceEntity(Entity entity) {
        if (entity.getHitBox().getCenterX() > hitBox.getCenterX()) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    protected boolean isEntityCloseForAttack(Entity entity) {
        double distance = Math.abs(entity.getHitBox().getCenterX() - hitBox.getCenterX());
        return distance <= definition.getAttackRange();
    }

    protected boolean isEntityInSight(Entity entity) {
        double distance = Math.abs(entity.getHitBox().getCenterX() - hitBox.getCenterX());
        return distance <= definition.getSightRange();
    }

    protected void changeDirection() {
        if (direction == Direction.LEFT) setDirection(Direction.RIGHT);
        else if (direction == Direction.RIGHT) setDirection(Direction.LEFT);
        entityState = Anim.WALK;
        enemySpeed = definition.getScaledWalkSpeed();
    }

    /**
     * Switches between IDLE, WALK, and RUN without resetting the animation index if already in that state.
     */
    protected void setLocomotionState(Anim nextState) {
        if (entityState != nextState) {
            setEnemyAction(nextState);
        }
    }

    /**
     * Advances toward, holds ground at, or steps back from the target to maintain clean weapon-tip spacing.
     */
    protected void moveWithCombatSpacing(int[][] levelData, Entity target, double chaseSpeed, double minSpacing, double retreatSpacing) {
        boolean waiting = (combatDirector != null) && combatDirector.isWaitingGuard(this);
        double effectiveMin = (combatDirector != null) ? combatDirector.getAssignedMinSpacing(this, minSpacing) : minSpacing;
        double effectiveRetreat = waiting ? Math.max(retreatSpacing, effectiveMin - (4.0 * SCALE)) : retreatSpacing;

        double dx = target.getHitBox().getCenterX() - hitBox.getCenterX();
        double absDist = Math.abs(dx);
        double facingDeadzone = 4.0 * SCALE;

        if (absDist > facingDeadzone) {
            faceEntity(target);
        }

        if (absDist < effectiveRetreat) {
            double awayDirSign = (dx >= 0) ? -1.0 : 1.0;
            Direction awayDir = (awayDirSign < 0) ? Direction.LEFT : Direction.RIGHT;
            double backSpeed = waiting ? definition.getScaledWalkSpeed() * 0.70 : definition.getScaledWalkSpeed() * 0.90;
            double step = awayDirSign * Math.min(backSpeed, effectiveRetreat - absDist);
            if (Math.abs(step) > 0.15 * SCALE && canMoveHere(hitBox.x + step, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, step, levelData, awayDir)) {
                hitBox.x += step;
                setLocomotionState(Anim.WALK);
            }
            else setLocomotionState(Anim.IDLE);
            return;
        }

        if (absDist <= effectiveMin + (1.5 * SCALE)) {
            setLocomotionState(Anim.IDLE);
            return;
        }

        double moveDirSign = (dx >= 0) ? 1.0 : -1.0;
        Direction moveDir = (moveDirSign > 0) ? Direction.RIGHT : Direction.LEFT;
        boolean closeToWaitingBand = waiting && (absDist <= effectiveMin + (24.0 * SCALE));
        double moveSpeed = closeToWaitingBand ? definition.getScaledWalkSpeed() * 0.75 : chaseSpeed;
        double stepMagnitude = Math.min(moveSpeed, absDist - effectiveMin);
        double step = moveDirSign * stepMagnitude;

        if (stepMagnitude > 0.15 * SCALE && canMoveHere(hitBox.x + step, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, step, levelData, moveDir)) {
            hitBox.x += step;
            setLocomotionState((!closeToWaitingBand && moveSpeed > definition.getScaledWalkSpeed() * 1.1) ? Anim.RUN : Anim.WALK);
        }
        else {
            setLocomotionState(Anim.IDLE);
        }
    }

    /**
     * Moves the enemy back and forth within its home patrol territory, pausing naturally at turnaround points
     * so enemies guard their posts instead of clumping together at one end of the room.
     */
    protected void patrolStep(int[][] levelData) {
        cowering = false;
        if (patrolPauseTicks > 0) {
            patrolPauseTicks--;
            setLocomotionState(Anim.IDLE);
            return;
        }

        double distFromHome = hitBox.x - xPos;
        if (direction == Direction.LEFT && distFromHome <= -PATROL_TETHER_RADIUS) {
            setDirection(Direction.RIGHT);
            patrolPauseTicks = 120 + rand.nextInt(160);
            setLocomotionState(Anim.IDLE);
            return;
        }
        else if (direction == Direction.RIGHT && distFromHome >= PATROL_TETHER_RADIUS) {
            setDirection(Direction.LEFT);
            patrolPauseTicks = 120 + rand.nextInt(160);
            setLocomotionState(Anim.IDLE);
            return;
        }

        enemySpeed = definition.getScaledWalkSpeed();
        setLocomotionState(Anim.WALK);
        double enemyXSpeed = (direction == Direction.LEFT) ? -enemySpeed : enemySpeed;
        if (canMoveHere(hitBox.x + enemyXSpeed, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, enemyXSpeed, levelData, direction)) {
            hitBox.x += enemyXSpeed;
        }
        else {
            changeDirection();
            patrolPauseTicks = 100 + rand.nextInt(140);
            setLocomotionState(Anim.IDLE);
        }
    }

    /**
     * Steps forward during a melee lunge while clamping movement so the enemy never slides through or overlaps the target's hitbox.
     */
    protected void stepForwardClamped(int[][] levelData, Entity target, double stepSpeed) {
        double dirSign = (direction == Direction.LEFT) ? -1.0 : 1.0;
        double dx = dirSign * stepSpeed;
        double minBodyGap = (hitBox.width + target.getHitBox().width) * 0.5 + (6.0 * SCALE);

        double targetCenterX = target.getHitBox().getCenterX();
        double currentCenterX = hitBox.getCenterX();
        double currentDist = Math.abs(targetCenterX - currentCenterX);
        boolean movingTowardTarget = (targetCenterX - currentCenterX) * dirSign > 0;

        if (movingTowardTarget) {
            if (currentDist <= minBodyGap) return;
            double allowed = currentDist - minBodyGap;
            if (Math.abs(dx) > allowed) {
                dx = dirSign * allowed;
            }
        }

        if (canMoveHere(hitBox.x + dx, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, dx, levelData, direction)) {
            hitBox.x += dx;
        }
    }

    /**
     * Anchors the enemy's attackBox forward in the direction it is facing with a slight body overlap so melee hits are readable and fair.
     */
    protected void updateForwardAttackBox(double width) {
        attackBox.width = width;
        double overlap = hitBox.width * 0.25;
        if (direction == Direction.RIGHT) {
            attackBox.x = hitBox.getMaxX() - overlap;
        }
        else attackBox.x = hitBox.x - width + overlap;
        attackBox.y = hitBox.y;
    }

    // Combat Helpers
    /**
     * Checks whether the target player successfully parried this enemy's windup attack.
     * Supports 360-degree multi-parry protection if the player is already mid-parry (CAN_BLOCK).
     */
    protected boolean checkPlayerParry(Entity target) {
        if (target instanceof Player player) {
            if (player.checkAction(PlayerAction.CAN_BLOCK)) {
                releaseCombatToken();
                return true;
            }
            boolean block = player.checkAction(PlayerAction.BLOCK);
            if (block) {
                double dx = hitBox.getCenterX() - player.getHitBox().getCenterX();
                boolean facingEnemy = (dx >= -14.0 * SCALE && player.getFlipSign() == 1)
                        || (dx <= 14.0 * SCALE && player.getFlipSign() == -1);
                if (facingEnemy) {
                    player.addAction(PlayerAction.CAN_BLOCK);
                    Audio.getInstance().getAudioPlayer().playBlockSound("Player");
                    releaseCombatToken();
                    return true;
                }
            }
        }
        return false;
    }

    protected void playDeathSound() {
        Sound deathSound = definition.getDeathSoundEnum();
        if (deathSound != null) {
            Audio.getInstance().getAudioPlayer().playSound(deathSound);
        }
    }

    public void checkEntityHit(Rectangle2D.Double attackBox, Entity target) {
        if (attackBox.intersects(target.getHitBox())) {
            if (target instanceof Player player) {
                boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
                if (!canBlock) player.changeHealth(-enemyType.getDamage(), this);
            }
            else if (target instanceof Follower follower) {
                if (follower.isKnockedDown()) return;
                follower.hit(enemyType.getDamage(), this);
            }
        }
        else if (definition.isMultiFrameHitCheck()) return;
        attackCheck = true;
    }

    public boolean isAttacking() {
        return entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.ATTACK_3 || entityState == Anim.SPELL_1 || entityState == Anim.SPELL_3;
    }

    public boolean isObjectBreakFrame() {
        int breakFrame = definition.getAttackObjectBreakFrame();
        return breakFrame >= 0 && animIndex == breakFrame;
    }

    public void freeze(int ticks) {
        if (entityState == Anim.DEATH || !alive || stunImmunityTick > 0) return;
        this.freezeTick = Math.max(this.freezeTick, ticks);
        this.attackCheck = true;
        releaseCombatToken();
        if (entityState != Anim.HIT && !definition.isBoss()) {
            this.entityState = Anim.HIT;
            this.animIndex = 0;
            this.animTick = 0;
        }
    }

    /**
     * Immediately breaks a stun when the enemy takes a wake-up hit and grants brief stun immunity.
     */
    public void wakeUp() {
        if (freezeTick > 0) {
            this.freezeTick = 0;
            this.stunImmunityTick = 260;
        }
    }

    protected boolean handleStunTick(int[][] levelData) {
        return handleStunTick(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
    }

    protected boolean handleStunTick(int[][] levelData, double gravity, double collisionFallSpeed) {
        if (stunImmunityTick > 0) stunImmunityTick--;
        if (freezeTick > 0) {
            freezeTick--;
            if (!isEntityOnFloor(hitBox, levelData)) {
                inAir = true;
            }
            if (inAir) {
                updateInAir(levelData, gravity, collisionFallSpeed);
            }
            updateAnimation();
            if (freezeTick == 0) {
                this.stunImmunityTick = 260;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns true during the windup animation frames when the player can parry this enemy's attack.
     */
    public boolean isParryWindup() {
        if (!alive || freezeTick > 0 || attackCheck) return false;
        if (definition.getParryWindupStart() < 0) return false;
        if (entityState != Anim.ATTACK_1 && entityState != Anim.ATTACK_2) return false;
        return animIndex >= definition.getParryWindupStart() && animIndex <= definition.getParryWindupEnd();
    }

    /**
     * Evaluates whether this enemy should be terrified of the player based on its JSON fearFlag and fearChance.
     */
    public void evaluateFearState(WorldMemoryManager memory) {
        terrified = false;
        cowering = false;
        if (memory == null || definition.getFearFlag() == null || definition.getFearChance() <= 0.0) return;
        if (memory.hasFlag(definition.getFearFlag())) {
            int seed = Math.abs((xPos * 73856093) ^ (yPos * 19349663) ^ enemyType.ordinal());
            double roll = (seed % 1000) / 1000.0;
            terrified = (roll < definition.getFearChance());
        }
    }

    /**
     * Makes a terrified enemy turn away and sprint from the player, cowering in place if cornered against a wall or ledge.
     */
    protected void fleeFromEntity(int[][] levelData, Entity target) {
        double awayDirSign = (target.getHitBox().getCenterX() <= hitBox.getCenterX()) ? 1.0 : -1.0;
        Direction awayDir = (awayDirSign > 0) ? Direction.RIGHT : Direction.LEFT;
        double fleeSpeed = definition.getScaledRunSpeed() * 1.10;
        double step = awayDirSign * fleeSpeed;

        if (canMoveHere(hitBox.x + step, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, step, levelData, awayDir)) {
            cowering = false;
            setDirection(awayDir);
            hitBox.x += step;
            setLocomotionState(Anim.RUN);
        }
        else {
            cowering = true;
            faceEntity(target);
            setLocomotionState(Anim.IDLE);
        }
    }

    public abstract boolean hit(double damage, boolean special, boolean hitSound);

    public abstract void spellHit(double damage);

    // Reset
    protected void reset() {
        releaseCombatToken();
        hitBox.x = xPos;
        hitBox.y = yPos;
        currentHealth = maxHealth;
        entityState = Anim.IDLE;
        alive = true;
        animIndex = animTick = 0;
        animSpeed = originalAnimSpeed;
        enemySpeed = definition.getScaledWalkSpeed();
        pushOffset = 0;
        freezeTick = 0;
        stunImmunityTick = 0;
    }

    // Debug Rendering
    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }

    // Getters & Setters
    public EnemyDefinition getDefinition() {
        return definition;
    }

    public boolean isUndead() {
        return definition.isUndead();
    }

    public boolean isBoss() {
        return definition.isBoss();
    }

    public int getAnimIndex() {
        return animIndex;
    }

    public Anim getEnemyAction() {
        return entityState;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isVisible() {
        return true;
    }

    public boolean isTerrified() {
        return terrified;
    }

    public boolean isCowering() {
        return cowering;
    }

    /**
     * Dynamically adjusts animation speed from {@link EnemyDefinition} based on the current state.
     */
    protected void setAnimSpeedByState() {
        if (definition.isBoss()) return;

        switch (entityState) {
            case RUN -> animSpeed = definition.getRunAnimSpeed();
            case WALK -> animSpeed = definition.getWalkAnimSpeed();
            case ATTACK_1, ATTACK_2 -> animSpeed = definition.getAttackAnimSpeed();
            case HIT -> animSpeed = definition.getHitAnimSpeed();
            default -> animSpeed = originalAnimSpeed;
        }
    }

    public void setEnemyAction(Anim enemyAction) {
        this.entityState = enemyAction;
        this.animIndex = 0;
        this.animTick = 0;
        criticalHit = false;
        setAnimSpeedByState();
    }

    public void setEnemyActionNoReset(Anim enemyAction) {
        this.entityState = enemyAction;
        criticalHit = false;
        setAnimSpeedByState();
    }

    public void setDirection(Direction direction) {
        if (direction == Direction.RIGHT) {
            this.flipCoefficient = 0;
            this.flipSign = 1;
            this.pushDirection = Direction.LEFT;
        }
        else if (direction == Direction.LEFT) {
            this.flipCoefficient = width;
            this.flipSign = -1;
            this.pushDirection = Direction.RIGHT;
        }
        this.direction = direction;
    }

    public void setCriticalHit(boolean criticalHit) {
        this.criticalHit = criticalHit;
    }

    public double getPushOffset() {
        return pushOffset;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isCriticalHit() {
        return criticalHit;
    }

    public boolean isStunned() {
        return alive && entityState != Anim.DEATH && freezeTick > 0;
    }

    public int getFreezeTick() {
        return freezeTick;
    }

    public int getAnimTick() {
        return animTick;
    }

    public int getAnimSpeed() {
        return animSpeed;
    }
}
