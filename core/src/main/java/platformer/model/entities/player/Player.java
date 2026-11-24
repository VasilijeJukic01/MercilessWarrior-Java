package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.GameClock;
import platformer.core.GameContext;
import platformer.core.config.BalanceManager;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.effects.EffectManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.craft.BlacksmithRecipeManager;
import platformer.model.levels.LevelManager;
import platformer.model.levels.LvlTriggerType;
import platformer.model.minimap.MinimapManager;
import platformer.model.projectiles.Projectile;
import platformer.model.projectiles.ProjectileFactory;
import platformer.model.world.WorldMemoryManager;
import platformer.physics.DamageSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Core Player entity that coordinates movement, combat, rendering, feedback, and inventory sub-handlers.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Player extends Entity {

    private final LevelManager levelManager;
    private final ObjectManager objectManager;
    private final MinimapManager minimapManager;
    private final EffectManager effectManager;
    private final WorldMemoryManager worldMemoryManager;
    private int[][] levelData;

    // Sub-Handlers
    private PlayerRenderer playerRenderer;
    private PlayerMovementHandler movementHandler;
    private PlayerCombatHandler combatHandler;
    private PlayerUltimateHandler ultimateHandler;
    private PlayerActionHandler actionHandler;
    private PlayerFeedbackHandler feedbackHandler;
    private PlayerDataManager playerDataManager;
    private PlayerMinimapHandler minimapHandler;
    private final PlayerStats stats;
    private Inventory inventory;

    // Core State
    private int animSpeed = 20;
    private int animTick = 0, animIndex = 0;
    private final EnumSet<PlayerAction> actions = EnumSet.noneOf(PlayerAction.class);
    private double currentStamina = BalanceManager.getInstance().getPlayerBalance().getInitialStamina();
    private int spellState = 0;

    public Player(int xPos, int yPos, int width, int height, GameContext context) {
        super(xPos, yPos, width, height, BalanceManager.getInstance().getPlayerBalance().getBaseHealth());
        this.stats = new PlayerStats();
        this.levelManager = context.getLevelManager();
        this.objectManager = context.getObjectManager();
        this.minimapManager = context.getMinimapManager();
        this.effectManager = context.getEffectManager();
        this.worldMemoryManager = context.getWorldMemoryManager();
        init(context);
    }

    private void init(GameContext context) {
        this.cooldown = new double[4];
        addAction(PlayerAction.CAN_DASH);
        initHitBox(PLAYER_HB_WID, PLAYER_HB_HEI);
        initAttackBox();
        initManagers(context, context.getTimeCycleManager(), context.getGameState() == null);
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, PLAYER_AB_WID, PLAYER_AB_HEI);
    }

    private void initManagers(GameContext context, TimeCycleManager timeCycleManager, boolean isMenuDummy) {
        this.playerRenderer = new PlayerRenderer(this);
        this.movementHandler = new PlayerMovementHandler(this, context);
        this.combatHandler = new PlayerCombatHandler(this, context);
        this.playerDataManager = new PlayerDataManager(this, minimapManager, timeCycleManager);
        this.minimapHandler = new PlayerMinimapHandler(this, minimapManager);
        this.actionHandler = new PlayerActionHandler(this, effectManager);
        this.ultimateHandler = new PlayerUltimateHandler(this, context);
        this.feedbackHandler = new PlayerFeedbackHandler(this, context);
        this.inventory = new Inventory(!isMenuDummy, stats.getEquipment());
    }

    public void loadLvlData(int[][] levelData) {
        this.levelData = levelData;
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
    }

    public void setSpawn(Point p) {
        this.xPos = p.x;
        this.yPos = p.y;
        hitBox.x = xPos;
        hitBox.y = yPos;
        if (movementHandler != null) {
            movementHandler.setLastSafeGroundPoint(p);
        }
        if (minimapHandler != null) {
            minimapHandler.update();
        }
    }

    /**
     * Deals hazard damage and, if the player survives, teleports them back to their last safe ground ledge with refreshed aerial mobility and brief invulnerability
     */
    public void triggerHazardRespawn(double damage) {
        if (checkAction(PlayerAction.DYING) || checkAction(PlayerAction.GAME_OVER) || feedbackHandler.isInvulnerable()) {
            return;
        }
        double defenseBonus = stats.getDefenseReduction();
        double actualDamage = Math.max(1.0, Math.abs(damage) * (1.0 - defenseBonus));
        if (currentHealth - actualDamage <= 0) {
            kill();
            return;
        }

        feedbackHandler.onPlayerDamaged(actualDamage);
        changeHealth(-actualDamage);

        Point safePt = movementHandler.getHazardRespawnPoint();
        if (safePt != null) {
            clearTransitionMomentum();
            resetDirections();
            removeAction(PlayerAction.HIT);
            hitBox.x = safePt.x;
            hitBox.y = safePt.y;
            actionHandler.setDashCount(0);
        }
    }

    // Animation
    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (entityState == Anim.CROUCH && checkAction(PlayerAction.LOOK_DOWN) && animIndex > 2) {
                animIndex = 2;
            }
            if (entityState == Anim.BLOCK && checkAction(PlayerAction.BLOCK) && !checkAction(PlayerAction.CAN_BLOCK) && animIndex > 3) {
                animIndex = 3;
            }
            updateFireballAnimation();
            if (animIndex >= playerRenderer.getFrameCount(entityState)) {
                finishAnimation();
            }
        }
        blockWallFlip();
    }

    private void updateFireballAnimation() {
        boolean fireball = checkAction(PlayerAction.FIREBALL);
        if (spellState == 1 && animIndex >= playerRenderer.getFrameCount(entityState) - 5) {
            animIndex = 2;
        }
        else if (fireball && animIndex == 3) {
            ProjectileFactory.createFireball(this);
        }
    }

    private void finishAnimation() {
        if (entityState == Anim.DASH && checkAction(PlayerAction.DASH)) {
            animIndex = playerRenderer.getFrameCount(Anim.DASH) - 1;
            return;
        }

        boolean wasAttacking = checkAction(PlayerAction.ATTACK);
        combatHandler.finishAttackBlock();
        animIndex = 0;
        removeActions(PlayerAction.ATTACK, PlayerAction.DASH_HIT, PlayerAction.BLOCK, PlayerAction.FIREBALL, PlayerAction.CAN_BLOCK);
        attackCheck = false;
        boolean canTransform = checkAction(PlayerAction.CAN_TRANSFORM);
        if (canTransform) {
            removeAction(PlayerAction.CAN_TRANSFORM);
            addAction(PlayerAction.TRANSFORM);
        }
        boolean hit = checkAction(PlayerAction.HIT);
        if (hit) removeAction(PlayerAction.HIT);
        setSpellState(0);

        if (wasAttacking && combatHandler.consumeBufferedAttack()) {
            setAnimation();
        }
    }

    private void blockWallFlip() {
        if (movementHandler.isWallJumpLocked() || checkAction(PlayerAction.ATTACK)) return;
        boolean left = checkAction(PlayerAction.LEFT);
        boolean right = checkAction(PlayerAction.RIGHT);
        boolean moving = checkAction(PlayerAction.MOVE);
        boolean onWall = checkAction(PlayerAction.ON_WALL);
        if (moving && left && !onWall) setFacingDirection(-1);
        else if (moving && right && !onWall) setFacingDirection(1);
    }

    public void setFacingDirection(int dirSign) {
        if (dirSign == -1) {
            this.flipCoefficient = (int) (width - hitBox.width - 13 * SCALE);
            this.flipSign = -1;
        }
        else {
            this.flipCoefficient = 0;
            this.flipSign = 1;
        }
    }

    private void setAnimation() {
        if (spellState == 2) return;
        Anim previousAction = entityState;

        boolean moving = checkAction(PlayerAction.MOVE);
        boolean crouching = checkAction(PlayerAction.LOOK_DOWN) && !inAir && !moving;
        entityState = crouching ? Anim.CROUCH : (moving ? Anim.RUN : Anim.IDLE);

        if (inAir) {
            if (airSpeed < 0) entityState = Anim.JUMP;
            else if (airSpeed > 0) entityState = Anim.FALL;
        }
        boolean onWall = checkAction(PlayerAction.ON_WALL);
        boolean onObject = checkAction(PlayerAction.ON_OBJECT);
        if (onWall && !onObject) entityState = Anim.WALL;

        boolean dash = checkAction(PlayerAction.DASH);
        if (dash) {
            if (stats.getPerks().isDashSlash()) {
                entityState = Anim.ATTACK_1;
                animIndex = 1;
                animTick = 0;
                return;
            }
            entityState = Anim.DASH;
            if (previousAction != entityState) {
                animIndex = animTick = 0;
                setAnimSpeedByState();
            }
            return;
        }
        boolean attacking = checkAction(PlayerAction.ATTACK);
        boolean hit = checkAction(PlayerAction.HIT);
        boolean fireball = checkAction(PlayerAction.FIREBALL);
        boolean canTransform = checkAction(PlayerAction.CAN_TRANSFORM);
        boolean canBlock = checkAction(PlayerAction.CAN_BLOCK);
        boolean blocking = checkAction(PlayerAction.BLOCK);

        if (spellState == 1) entityState = Anim.SPELL_1;
        else if (fireball) entityState = Anim.SPELL_2;
        else if (canBlock) entityState = Anim.PARRY;
        else if (blocking) entityState = Anim.BLOCK;
        else if (hit) entityState = Anim.HIT;
        else if (attacking && !onWall) entityState = combatHandler.getAttackAnim();
        else if (canTransform) entityState = Anim.TRANSFORM;

        if (previousAction != entityState) {
            animIndex = animTick = 0;
            setAnimSpeedByState();
        }
    }

    private void setAnimSpeedByState() {
        switch (entityState) {
            case RUN, DASH, CROUCH, BLOCK, PARRY -> animSpeed = 12;
            case ATTACK_1, ATTACK_2, ATTACK_3 -> animSpeed = 16;
            case JUMP, FALL -> animSpeed = 25;
            case TRANSFORM -> animSpeed = 15;
            default -> animSpeed = 20;
        }
    }

    // Core
    public void update() {
        playerDataManager.update(!levelManager.isInArena());
        if (currentHealth <= 0) {
            if (ultimateHandler.isActive()) ultimateHandler.reset();
            updateDeath();
            return;
        }
        if (updateUltimate()) return;
        coolDownTickUpdate();
        combatHandler.update();
        combatHandler.updateAttackBox();
        setAnimation();
        updateHitBlockMove();
        updateMove();
        combatHandler.updateSpells();
        combatHandler.updateAttack();
        updateAnimation();
        if (checkAction(PlayerAction.HIT)) {
            objectManager.checkObjectBreakByPush(hitBox);
        }
        feedbackHandler.update();
    }

    private boolean updateUltimate() {
        if (ultimateHandler.isActive()) {
            ultimateHandler.update();
            playerRenderer.updateAfterimages(ultimateHandler.getState() == PlayerUltimateHandler.State.BLITZ, pushOffset);
            feedbackHandler.update();
            return true;
        }
        return false;
    }

    private void updateDeath() {
        if (checkAction(PlayerAction.GAME_OVER)) return;
        if (entityState != Anim.DEATH) {
            entityState = Anim.DEATH;
            animIndex = animTick = 0;
            addAction(PlayerAction.DYING);
            Logger.getInstance().notify("Player is dead.", Message.NOTIFICATION);
        }
        else if (animIndex == playerRenderer.getFrameCount(entityState) - 1 && animTick >= animSpeed - 1) {
            removeAction(PlayerAction.DYING);
            addAction(PlayerAction.GAME_OVER);
            Audio.getInstance().getAudioPlayer().stopSong();
            Audio.getInstance().getAudioPlayer().playSound(Sound.GAME_OVER);
        }
        else updateAnimation();
    }

    private void updateHitBlockMove() {
        boolean hit = checkAction(PlayerAction.HIT);
        boolean canBlock = checkAction(PlayerAction.CAN_BLOCK);

        if (hit) {
            setSpellState(0);
            combatHandler.cancelForMobility();
            combatHandler.clearImpulses();
            movementHandler.clearMomentum();
            removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT, PlayerAction.BLOCK);
            movementHandler.handlePushback(pushDirection, levelData);
            movementHandler.inAirUpdate(levelData);
        }
        else if (canBlock) {
            pushOffsetDirection = Direction.DOWN;
            pushDirection = (flipSign == 1) ? Direction.LEFT : Direction.RIGHT;
            if (animIndex <= playerRenderer.getFrameCount(entityState)) {
                pushBack(pushDirection, levelData, 0.1, PLAYER_SPEED);
            }
            updatePushOffset();
        }
        else movementHandler.updatePosition(levelData, combatHandler, minimapHandler);
    }

    private void updateMove() {
        playerRenderer.updateAfterimages(checkAction(PlayerAction.DASH), pushOffset);
        movementHandler.updateDashTimer();
    }

    public void updateDuringCinematic(boolean recordAfterimages) {
        playerDataManager.update(!levelManager.isInArena());
        playerRenderer.updateAfterimages(recordAfterimages, pushOffset);
        feedbackHandler.update();
    }

    @Override
    protected void coolDownTickUpdate() {
        double step = 1.0 / GameClock.TICKS_PER_SECOND;
        for (int i = 0; i < cooldown.length; i++) {
            if (cooldown[i] > 0) {
                cooldown[i] -= step;
                if (cooldown[i] < 0) cooldown[i] = 0;
            }
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        playerRenderer.render(g, xLevelOffset, yLevelOffset, pushOffset, feedbackHandler.getBurnTick(), feedbackHandler.getHitFlashTick(), feedbackHandler.getInvulnerabilityTick(), feedbackHandler.getParryFlashTick());
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
        attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    // Actions
    public void beginLevelTransition(LvlTriggerType exitType) {
        movementHandler.setTransitionExitType(exitType);
        addAction(PlayerAction.LEVEL_TRANSITION);
    }

    public void queueJumpBuffer() {
        movementHandler.queueJumpBuffer();
    }

    public void cancelAttackForMobility() {
        combatHandler.cancelForMobility();
    }

    public void performAttack() {
        combatHandler.performAttack();
    }

    public void launch(double launchSpeed) {
        movementHandler.launch(launchSpeed);
    }

    public void setBlock(boolean block) {
        combatHandler.setBlock(block);
    }

    // Status & Health
    public void changeHealth(double value) {
        if (checkAction(PlayerAction.DYING)) return;
        if (value < 0) {
            if (checkAction(PlayerAction.HIT)) return;
            addAction(PlayerAction.HIT);
        }
        else {
            String healText = String.format("+%.1f", value);
            effectManager.spawnDamageNumber(healText, getHitBox().getCenterX(), getHitBox().y, HEAL_COLOR);
        }
        currentHealth = Math.max(Math.min(currentHealth + value, getMaxHealth()), 0);
    }

    public void changeHealth(double value, DamageSource source) {
        if (checkAction(PlayerAction.HIT) || feedbackHandler.isInvulnerable() || (ultimateHandler != null && ultimateHandler.isActive())) return;
        if (value < 0) {
            double defenseBonus = stats.getDefenseReduction();
            double actualDamage = Math.abs(value) * (1.0 - defenseBonus);
            feedbackHandler.onPlayerDamaged(actualDamage);
            changeHealth(-actualDamage);
            Logger.getInstance().notify("Damage received: " + actualDamage, Message.INFORMATION);
        }
        else changeHealth(value);
        applyKnockback(source);
    }

    private void applyKnockback(DamageSource source) {
        Direction explicitKnockback = source.getKnockbackDirection();
        if (explicitKnockback != null) this.pushDirection = explicitKnockback;
        else {
            Rectangle2D sourceBounds = source instanceof Projectile ? source.getHitBox().getBounds2D() : source.getHitBox();
            this.pushDirection = (sourceBounds.getCenterX() < hitBox.getCenterX()) ? Direction.RIGHT : Direction.LEFT;
        }
        this.inAir = true;
        this.airSpeed = -1.2 * SCALE;
    }

    public void applyStatusDamage(double value) {
        if (value < 0) {
            String dmgText = String.format("%.1f", -value);
            effectManager.spawnDamageNumber(dmgText, getHitBox().getCenterX(), getHitBox().y, DAMAGE_COLOR);
        }
        currentHealth = Math.max(Math.min(currentHealth + value, getMaxHealth()), 0);
    }

    public void changeStamina(double value) {
        if (value > 0) {
            String staminaText = String.format("+%.1f", value);
            effectManager.spawnDamageNumber(staminaText, getHitBox().getCenterX(), getHitBox().y, STAMINA_COLOR);
        }
        currentStamina = Math.max(Math.min(currentStamina + value, getMaxStamina()), 0);
        if (currentStamina == 0) {
            removeAction(PlayerAction.TRANSFORM);
            if (spellState == 1) spellState = 2;
        }
    }

    public void kill() {
        currentHealth = 0;
    }

    // Reset
    /**
     * Clears active jump/dash momentum and wall/object flags when entering a new room.
     */
    public void clearTransitionMomentum() {
        removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT, PlayerAction.ON_WALL, PlayerAction.WALL_PUSH, PlayerAction.DOUBLE_JUMP, PlayerAction.ON_OBJECT, PlayerAction.LEVEL_TRANSITION);
        inAir = false;
        airSpeed = 0;
        movementHandler.reset();
        combatHandler.clearImpulses();
    }

    public void resetDirections() {
        removeActions(PlayerAction.LEFT, PlayerAction.RIGHT, PlayerAction.JUMP, PlayerAction.LOOK_DOWN);
        if (spellState == 1) setSpellState(2);
        animIndex = animTick = 0;
    }

    public void resetAnimTickAndIndex() {
        this.animIndex = 0;
        this.animTick = 0;
    }

    public void reset() {
        inAir = false;
        movementHandler.reset();
        combatHandler.reset();
        ultimateHandler.reset();
        playerRenderer.reset();
        feedbackHandler.reset();
        removeActions(PlayerAction.DYING, PlayerAction.GAME_OVER, PlayerAction.LAVA);
        removeActions(PlayerAction.HIT, PlayerAction.BLOCK, PlayerAction.ATTACK, PlayerAction.MOVE, PlayerAction.DASH);
        removeActions(PlayerAction.LEFT, PlayerAction.RIGHT, PlayerAction.JUMP, PlayerAction.ON_WALL, PlayerAction.ON_OBJECT, PlayerAction.LOOK_DOWN);

        setSpellState(0);
        animIndex = animTick = 0;
        entityState = Anim.IDLE;
        currentHealth = getMaxHealth();
        currentStamina = 0;
        hitBox.x = xPos;
        hitBox.y = yPos;
        initAttackBox();
        Arrays.fill(cooldown, 0);
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (minimapHandler != null) minimapHandler.update();
    }

    // Facade
    public void changeCoins(int value) {
        playerDataManager.changeCoins(value);
    }

    public void changeUpgradeTokens(int value) {
        playerDataManager.changeUpgradeTokens(value);
    }

    public void changeExp(double value) {
        playerDataManager.changeExp(value);
    }

    // Getters & Setters
    public void setAttacking(boolean attacking) {
        if (attacking) addAction(PlayerAction.ATTACK);
        else removeAction(PlayerAction.ATTACK);
        setSpellState(0);
    }

    public void setSpellState(int spellState) {
        if (spellState == 2 && this.spellState == 1) animIndex = 10;
        if (spellState == 0) Audio.getInstance().getAudioPlayer().stopSound(Sound.FIRE_SPELL_1);
        else if (spellState == 1) Audio.getInstance().getAudioPlayer().playSound(Sound.FIRE_SPELL_1);
        this.spellState = spellState;
    }

    public void setCanTransform(boolean canTransform) {
        if (!stats.getPerks().isTransform()) return;
        boolean transform = checkAction(PlayerAction.TRANSFORM);
        if (transform) removeAction(PlayerAction.TRANSFORM);
        else {
            if (canTransform) addAction(PlayerAction.CAN_TRANSFORM);
            else removeAction(PlayerAction.CAN_TRANSFORM);
            removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT);
        }
    }

    public void activateMinimap(boolean activateMinimap) {
        minimapHandler.activateMinimap(activateMinimap);
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public void setAirSpeed(double airSpeed) {
        this.airSpeed = airSpeed;
    }

    public boolean isAttackCheck() {
        return attackCheck;
    }

    public void setAttackCheck(boolean attackCheck) {
        this.attackCheck = attackCheck;
    }

    public int getAnimIndex() {
        return animIndex;
    }

    public double getMaxHealth() {
        return BalanceManager.getInstance().getPlayerBalance().getBaseHealth() * stats.getMaxHealthMultiplier();
    }

    public double getMaxStamina() {
        return BalanceManager.getInstance().getPlayerBalance().getBaseStamina() * stats.getMaxStaminaMultiplier();
    }

    public double getHorizontalSpeed() {
        return movementHandler.getHorizontalSpeed();
    }

    public double getAirSpeed() {
        return super.airSpeed;
    }

    public int getAttackDmg() {
        var pb = BalanceManager.getInstance().getPlayerBalance();
        int base = checkAction(PlayerAction.TRANSFORM) ? pb.getTransformedAttackDamage() : pb.getBaseAttackDamage();
        int swordUpgradeBonus = BlacksmithRecipeManager.getInstance().getTotalSwordBonusDamage(worldMemoryManager);
        return base + swordUpgradeBonus;
    }

    public double getCurrentStamina() {
        return currentStamina;
    }

    public int getSpellState() {
        return spellState;
    }

    public int getCoins() {
        return playerDataManager.getCoins();
    }

    public int getLevel() {
        return playerDataManager.getLevel();
    }

    public int getUpgradeTokens() {
        return playerDataManager.getUpgradeTokens();
    }

    public PlayerStats getStats() {
        return stats;
    }

    public PlayerDataManager getPlayerStatusManager() {
        return playerDataManager;
    }

    public PlayerActionHandler getActionHandler() {
        return actionHandler;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public PlayerCombatHandler getCombatHandler() {
        return combatHandler;
    }

    public PlayerFeedbackHandler getFeedbackHandler() {
        return feedbackHandler;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void addAction(PlayerAction action) {
        actions.add(action);
    }

    public void addActions(PlayerAction... stream) {
        actions.addAll(List.of(stream));
    }

    public void removeAction(PlayerAction action) {
        actions.remove(action);
    }

    public void removeActions(PlayerAction... stream) {
        List.of(stream).forEach(actions::remove);
    }

    public boolean checkAction(PlayerAction action) {
        return actions.contains(action);
    }

    public void setUltimateAnimation(Anim state, int frameIndex) {
        this.entityState = state;
        this.animIndex = frameIndex;
        this.animTick = 0;
    }

    public void recordUltimateAfterimage() {
        playerRenderer.recordInstantSnapshot(pushOffset);
    }

    public PlayerUltimateHandler getUltimateHandler() {
        return ultimateHandler;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }
}