package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.animation.Animation;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.AttackState;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.effects.EffectManager;
import platformer.model.entities.effects.particles.DustType;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.gameObjects.projectiles.Projectile;
import platformer.model.gameObjects.projectiles.ProjectileManager;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryBonus;
import platformer.model.inventory.ItemRarity;
import platformer.model.minimap.MinimapManager;
import platformer.model.perks.PerksBonus;
import platformer.physics.DamageSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.PLAYER_SHEET;
import static platformer.constants.FilePaths.PLAYER_TRANSFORM_SHEET;
import static platformer.physics.CollisionDetector.*;

@SuppressWarnings("FieldCanBeLocal")
public class Player extends Entity {

    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;
    private final ProjectileManager projectileManager;
    private final MinimapManager minimapManager;
    private final EffectManager effectManager;
    private int[][] levelData;

    // Core Variables
    private BufferedImage[][] animations, transformAnimations;
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0;
    private AttackState attackState;
    private PlayerActionHandler actionHandler;
    private final EnumSet<PlayerAction> actions = EnumSet.noneOf(PlayerAction.class);

    // Physics
    private final double downwardGravity = 0.03 * SCALE;
    private final double upwardGravity = 0.022 * SCALE;
    private final double jumpCutGravityMultiplier = 3.0;

    private final double wallGravity = 0.0005 * SCALE;
    private final double jumpSpeed = -2.25 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    // Status
    private int currentJumps = 0;
    private int dashTick = 0;
    private final int attackDmg = 5, transformAttackDmg = 8;
    private double currentStamina = 15;
    private int spellState = 0;
    private PlayerDataManager playerDataManager;
    private PlayerMinimapHandler minimapHandler;
    private Inventory inventory;

    // Effect
    private int runDustTick = 0;
    private int wallSlideDustTick = 0;
    private int mythicAuraTick = 0;


    public Player(int xPos, int yPos, int width, int height, EnemyManager enemyManager, ObjectManager objectManager, ProjectileManager projectileManager, MinimapManager minimapManager, EffectManager effectManager) {
        super(xPos, yPos, width, height, PLAYER_MAX_HP);
        this.enemyManager = enemyManager;
        this.objectManager = objectManager;
        this.projectileManager = projectileManager;
        this.minimapManager = minimapManager;
        this.effectManager = effectManager;
        loadAnimations();
        init();
    }

    // Init
    private void loadAnimations() {
        this.animations = Animation.getInstance().loadPlayerAnimations(width, height, PLAYER_SHEET);
        this.transformAnimations = Animation.getInstance().loadPlayerAnimations(width, height, PLAYER_TRANSFORM_SHEET);
    }

    private void init() {
        this.cooldown = new double[4];
        addAction(PlayerAction.CAN_DASH);
        initHitBox(PLAYER_HB_WID, PLAYER_HB_HEI);
        initAttackBox();
        initManagers();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, PLAYER_AB_WID, PLAYER_AB_HEI);
    }

    private void initManagers() {
        this.playerDataManager = new PlayerDataManager(this, minimapManager);
        this.minimapHandler = new PlayerMinimapHandler(this, minimapManager);
        this.actionHandler = new PlayerActionHandler(this, effectManager);
        this.inventory = new Inventory();
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
    }

    // Animation
    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            updateFireballAnimation();
            if (animIndex >= animations[entityState.ordinal()].length) {
                finishAnimation();
            }
            coolDownTickUpdate();
        }
        blockWallFlip();
    }

    private void updateFireballAnimation() {
        boolean fireball = checkAction(PlayerAction.FIREBALL);
        if (spellState == 1 && animIndex >= animations[entityState.ordinal()].length-5)
            animIndex = 2;
        else if (fireball && animIndex == 3)
            projectileManager.activateFireball(this);
    }

    private void finishAnimation() {
        finishAttackBlock();
        animIndex = 0;
        removeActions(PlayerAction.ATTACK, PlayerAction.DASH_HIT, PlayerAction.BLOCK, PlayerAction.FIREBALL, PlayerAction.CAN_BLOCK);
        attackCheck = false;
        boolean canTransform = checkAction(PlayerAction.CAN_TRANSFORM);
        if (canTransform) {
            removeAction(PlayerAction.CAN_TRANSFORM);
            addAction(PlayerAction.TRANSFORM);
        }
        boolean hit = checkAction(PlayerAction.HIT);
        if (hit) {
            removeAction(PlayerAction.HIT);
            airSpeed = 0;
        }
        setSpellState(0);
    }

    private void finishAttackBlock() {
        boolean canBlock = checkAction(PlayerAction.CAN_BLOCK);
        if (canBlock) {
            Logger.getInstance().notify("Damage blocked successfully!", Message.INFORMATION);
            double blockCooldown =  PLAYER_BLOCK_CD;
            double equipmentBonus = InventoryBonus.getInstance().getCooldown() * blockCooldown;
            blockCooldown -= equipmentBonus;
            cooldown[Cooldown.BLOCK.ordinal()] = blockCooldown;
            if (PerksBonus.getInstance().isRestorePower()) changeStamina(5);
        }
    }

    private void blockWallFlip() {
        boolean left = checkAction(PlayerAction.LEFT);
        boolean right = checkAction(PlayerAction.RIGHT);
        boolean moving = checkAction(PlayerAction.MOVE);
        boolean onWall = checkAction(PlayerAction.ON_WALL);

        if (moving && left && !onWall) {
            this.flipCoefficient = (int)(width-hitBox.width-13*SCALE);
            this.flipSign = -1;
        }
        else if (moving && right && !onWall) {
            this.flipCoefficient = 0;
            this.flipSign = 1;
        }
    }

    private void setAnimation() {
        if (spellState == 2) return;
        Anim previousAction = entityState;

        boolean moving = checkAction(PlayerAction.MOVE);
        entityState = (moving) ? Anim.RUN : Anim.IDLE;

        if (inAir) {
            if (airSpeed < 0) entityState = Anim.JUMP;
            else if (airSpeed > 0) entityState = Anim.FALL;
        }
        boolean onWall = checkAction(PlayerAction.ON_WALL);
        boolean onObject = checkAction(PlayerAction.ON_OBJECT);
        if (onWall && !onObject) setWallSlideAnimation();
        boolean dash = checkAction(PlayerAction.DASH);
        if (dash) {
            setDashAnimation();
            return;
        }
        boolean attacking = checkAction(PlayerAction.ATTACK);
        boolean hit = checkAction(PlayerAction.HIT);
        boolean fireball = checkAction(PlayerAction.FIREBALL);
        boolean canTransform = checkAction(PlayerAction.CAN_TRANSFORM);
        boolean canBlock = checkAction(PlayerAction.CAN_BLOCK);

        if (spellState == 1) entityState = Anim.SPELL_1;
        else if (fireball) entityState = Anim.SPELL_2;
        else if (canBlock) entityState = Anim.BLOCK;
        else if (hit) entityState = Anim.HIT;
        else if (attacking && !onWall) setAttackAnimation();
        else if (canTransform) entityState = Anim.TRANSFORM;

        if (previousAction != entityState) animIndex = animTick = 0;
    }

    private void setWallSlideAnimation() {
        entityState = Anim.WALL;
        wallSlideDustTick++;
        if (wallSlideDustTick > 3) {
            wallSlideDustTick = 0;
            double dustX = (flipSign == 1) ? hitBox.x + hitBox.width : hitBox.x;
            double dustY = hitBox.y + (new Random().nextDouble() * hitBox.height);
            effectManager.spawnDustParticles(dustX, dustY, 1, DustType.WALL_SLIDE, flipSign, this);
        }
    }

    private void setDashAnimation() {
        entityState = Anim.ATTACK_1;
        animIndex = 1;
        animTick = 0;
    }

    private void setAttackAnimation() {
        switch (attackState) {
            case ATTACK_1:
                entityState = Anim.ATTACK_1;
                break;
            case ATTACK_2:
                entityState = Anim.ATTACK_2;
                break;
            case ATTACK_3:
                entityState = Anim.ATTACK_3;
                break;
            default: break;
        }
    }

    // Positioning
    private void updatePosition() {
        removeAction(PlayerAction.MOVE);
        checkOnObject();
        boolean onObject = checkAction(PlayerAction.ON_OBJECT);
        if (!inAir && !isEntityOnFloor(hitBox, levelData) && !onObject) inAir = true;

        boolean canTransform = checkAction(PlayerAction.CAN_TRANSFORM);
        boolean canBlock = checkAction(PlayerAction.CAN_BLOCK);

        if (spellState != 0 || canBlock || canTransform) return;
        boolean jump = checkAction(PlayerAction.JUMP);
        boolean onWall = checkAction(PlayerAction.ON_WALL);
        if (!onWall) wallSlideDustTick = 0;
        if (jump) doJump();
        if (!inAir || onWall) actionHandler.setDashCount(0);
        boolean left = checkAction(PlayerAction.LEFT);
        boolean right = checkAction(PlayerAction.RIGHT);
        boolean dash = checkAction(PlayerAction.DASH);
        if (((!left && !right) || (left && right)) && !inAir && !dash) return;

        double dx = 0;

        updateWallPosition();

        boolean wallPush = checkAction(PlayerAction.WALL_PUSH);
        if (left) dx -= checkAction(PlayerAction.LAVA) ? LAVA_PLAYER_SPEED : PLAYER_SPEED;
        if (right) dx += checkAction(PlayerAction.LAVA) ? LAVA_PLAYER_SPEED : PLAYER_SPEED;
        if (right && inAir && wallPush && !dash) dx += PLAYER_BOOST;
        if (left && inAir && wallPush && !dash) dx -= PLAYER_BOOST;

        if (dash) {
            if (((!left && !right) || (left && right)) && flipSign == -1) dx = -PLAYER_SPEED;
            else if (((!left && !right) || (left && right)) && flipSign == 1) dx = PLAYER_SPEED;
            dx *= DASH_SPEED;
        }

        if (inAir && !dash) inAirUpdate();

        updateX(dx);
        minimapHandler.update();
        addAction(PlayerAction.MOVE);
    }

    private void inAirUpdate() {
        boolean onWall = checkAction(PlayerAction.ON_WALL);

        if (airSpeed < 0) {
            if (checkAction(PlayerAction.JUMP)) airSpeed += upwardGravity;
            else airSpeed += upwardGravity * jumpCutGravityMultiplier;
        }
        else {
            if (onWall) airSpeed += wallGravity;
            else airSpeed += downwardGravity;
        }

        if (canMoveHere(hitBox.x, hitBox.y + airSpeed + 1, hitBox.width, hitBox.height, levelData)) {
            hitBox.y += airSpeed;
        }
        else {
            hitBox.y = getYPosOnTheCeil(hitBox, airSpeed);
            if (airSpeed > 0) {
                removeAction(PlayerAction.WALL_PUSH);
                inAir = false;
                airSpeed = 0;
                currentJumps = 0;

                boolean isHit = checkAction(PlayerAction.HIT);
                if (!isHit)
                    effectManager.spawnDustParticles(hitBox.x + hitBox.width / 2, hitBox.y + hitBox.height, LAND_DUST_BURST, DustType.IMPACT, flipSign, this);
            }
            else {
                airSpeed = (onWall) ? wallGravity : collisionFallSpeed;
                removeAction(PlayerAction.DOUBLE_JUMP);
            }
        }
    }

    private void updateWallPosition() {
        boolean left = checkAction(PlayerAction.LEFT);
        boolean right = checkAction(PlayerAction.RIGHT);
        boolean onWall = checkAction(PlayerAction.ON_WALL);

        if (onWall) removeAction(PlayerAction.ATTACK);

        boolean leftSideCheck = isOnWall(hitBox, levelData, Direction.LEFT);
        boolean rightSideCheck = isOnWall(hitBox, levelData, Direction.RIGHT);

        if (!onWall && ((left && leftSideCheck) || (right && rightSideCheck)) && !isEntityOnFloor(hitBox, levelData)) {
            if ((flipSign == -1 && rightSideCheck) || (flipSign == 1 && leftSideCheck)) return;
            addActions(PlayerAction.ON_WALL, PlayerAction.WALL_PUSH);
            currentJumps = 1;
            airSpeed = 0.1;
        }
        if (onWall && !leftSideCheck && !rightSideCheck) {
            currentJumps = 0;
            removeAction(PlayerAction.ON_WALL);
        }
    }

    private void updateX(double dx) {
        boolean dash = checkAction(PlayerAction.DASH);
        boolean onWall = checkAction(PlayerAction.ON_WALL);
        boolean onObject = checkAction(PlayerAction.ON_OBJECT);

        double actualDx = objectManager.checkSolidObjectCollision(hitBox, dx);
        if (dash && actualDx != dx) {
            removeAction(PlayerAction.DASH);
            removeAction(PlayerAction.DASH_HIT);
            dashTick = 0;
        }

        if (!onObject && !onWall && canMoveHere(hitBox.x + actualDx, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x += actualDx;
        else if (dash && canMoveHere(hitBox.x + actualDx, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x += actualDx;
        else {
            if (onObject) hitBox.x = objectManager.getXObjectBound(hitBox, actualDx);
            else if (!onWall) hitBox.x = getXPosOnTheWall(hitBox, actualDx);

            if (dash) {
                removeAction(PlayerAction.DASH);
                removeAction(PlayerAction.DASH_HIT);
                dashTick = 0;
            }
        }
    }

    private void updateAttackBox() {
        if (spellState != 0) return;

        boolean left = checkAction(PlayerAction.LEFT);
        boolean right = checkAction(PlayerAction.RIGHT);

        if ((right && left) || (!right && !left)) {
            if (flipSign == 1) attackBox.x = hitBox.x + hitBox.width + (int)(10*SCALE);
            else attackBox.x = hitBox.x - hitBox.width - (int)(10*SCALE);
        }

        boolean dash = checkAction(PlayerAction.DASH);
        if (right || (dash && flipSign == 1)) attackBox.x = hitBox.x + hitBox.width + (int)(10*SCALE);
        else if (left || (dash && flipSign == -1)) attackBox.x = hitBox.x - hitBox.width - (int)(10*SCALE);

        attackBox.y = hitBox.y + (int)(10*SCALE);
    }

    private void updateAttack() {
        boolean attacking = checkAction(PlayerAction.ATTACK);
        boolean dash = checkAction(PlayerAction.DASH);
        if (attacking || dash) checkAttack();
    }

    public void launch(double launchSpeed) {
        effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.y + hitBox.height, 25, DustType.JUMP_PAD, 0, this);
        inAir = true;
        airSpeed = launchSpeed;
    }

    // Checks
    private void checkAttack() {
        boolean dash = checkAction(PlayerAction.DASH);
        boolean dashHit = checkAction(PlayerAction.DASH_HIT);

        if (attackCheck || animIndex != 1 || dashHit) return;
        attackCheck = !dash;
        boolean contactMade = enemyManager.checkEnemyHit(attackBox, this);
        if (contactMade) Audio.getInstance().getAudioPlayer().playHitSound();
        else if (!dash) Audio.getInstance().getAudioPlayer().playSlashSound();
        objectManager.checkObjectBreak(attackBox);
        projectileManager.checkProjectileDeflect(attackBox);
    }

    private void checkOnObject() {
        boolean onObject = checkAction(PlayerAction.ON_OBJECT);

        if (objectManager.isPlayerTouchingObject(this) && !onObject) {
            inAir = !objectManager.isPlayerGlitchedInObject(this);
            removeAction(PlayerAction.WALL_PUSH);
            airSpeed = currentJumps = 0;
            addAction(PlayerAction.ON_OBJECT);
        }
        else if (onObject && !objectManager.isPlayerTouchingObject(this)) removeAction(PlayerAction.ON_OBJECT);

        if (onObject) removeAction(PlayerAction.WALL_PUSH);
    }

    private void checkBreakablesOnPush() {
        if (!checkAction(PlayerAction.HIT)) return;
        objectManager.checkObjectBreakByPush(hitBox);
    }

    // Actions
    public void doJump() {
        boolean left = checkAction(PlayerAction.LEFT);
        boolean right = checkAction(PlayerAction.RIGHT);
        boolean doubleJump = checkAction(PlayerAction.DOUBLE_JUMP);
        boolean onWall = checkAction(PlayerAction.ON_WALL);

        if (!actionHandler.canJump(levelData, left, right, doubleJump)) return;

        if (onWall) {
            double dustX = (flipSign == 1) ? hitBox.x + hitBox.width : hitBox.x;
            double dustY = hitBox.y + hitBox.height / 2.0;
            effectManager.spawnDustParticles(dustX, dustY, 6, DustType.WALL_JUMP, flipSign, this);
        }

        if (currentJumps == 1) {
            addAction(PlayerAction.DOUBLE_JUMP);
            animIndex = animTick = 0;
            currentJumps++;
        }

        effectManager.spawnDustParticles(hitBox.x + hitBox.width / 2, hitBox.y + hitBox.height, JUMP_DUST_BURST, DustType.IMPACT, flipSign, this);

        inAir = true;
        airSpeed = jumpSpeed;
    }

    // Status Changes
    public void changeHealth(double value) {
        if (checkAction(PlayerAction.DYING)) return;
        if (value < 0) {
            boolean hit = checkAction(PlayerAction.HIT);
            if (hit) return;
            else addAction(PlayerAction.HIT);
        }
        else {
            String healText = String.format("+%.1f", value);
            effectManager.spawnDamageNumber(healText, getHitBox().getCenterX(), getHitBox().y, HEAL_COLOR);
        }
        currentHealth += value;
        double healthCap = maxHealth + PerksBonus.getInstance().getBonusHealth();
        double equipmentBonus = InventoryBonus.getInstance().getHealth() * healthCap;
        healthCap += equipmentBonus;
        currentHealth = Math.max(Math.min(currentHealth, healthCap), 0);
    }

    public void changeHealth(double value, DamageSource source) {
        boolean hit = checkAction(PlayerAction.HIT);
        if (hit) return;
        if (value < 0) {
            effectManager.spawnDustParticles(hitBox.getCenterX(), hitBox.getCenterY(), 15 + new Random().nextInt(6), DustType.PLAYER_HIT, flipSign, this);
            double defenseBonus = InventoryBonus.getInstance().getDefense() * value * (-1);
            value += defenseBonus;
            String dmgText = String.format("%.1f", -value);
            effectManager.spawnDamageNumber(dmgText, getHitBox().getCenterX(), getHitBox().y, DAMAGE_COLOR);
        }
        changeHealth(value);
        Rectangle2D sourceBounds = source.getHitBox();
        if (source instanceof Projectile) sourceBounds = source.getHitBox().getBounds2D();
        if (sourceBounds.getCenterX() < hitBox.getCenterX()) pushDirection = Direction.RIGHT;
        else pushDirection = Direction.LEFT;
        this.inAir = true;
        this.airSpeed = -1.2 * SCALE;
        Logger.getInstance().notify("Damage received: " + value, Message.INFORMATION);
    }

    private void changeHealthNoKnockback(double value) {
        if (value < 0) {
            String dmgText = String.format("%.1f", -value);
            effectManager.spawnDamageNumber(dmgText, getHitBox().getCenterX(), getHitBox().y, DAMAGE_COLOR);
        }
        currentHealth += value;
        double healthCap = maxHealth + PerksBonus.getInstance().getBonusHealth();
        double equipmentBonus = InventoryBonus.getInstance().getHealth() * healthCap;
        healthCap += equipmentBonus;
        currentHealth = Math.max(Math.min(currentHealth, healthCap), 0);
    }

    public void changeStamina(double value) {
        if (value > 0) {
            String staminaText = String.format("+%.1f", value);
            effectManager.spawnDamageNumber(staminaText, getHitBox().getCenterX(), getHitBox().y, STAMINA_COLOR);
        }
        currentStamina += value;
        double staminaCap = PLAYER_MAX_ST + PerksBonus.getInstance().getBonusPower();
        double equipmentBonus = InventoryBonus.getInstance().getStamina() * staminaCap;
        staminaCap += equipmentBonus;
        currentStamina = Math.max(Math.min(currentStamina, staminaCap), 0);
        if (currentStamina == 0) {
            removeAction(PlayerAction.TRANSFORM);
            if (spellState == 1) spellState = 2;
        }
    }

    public void kill() {
        currentHealth = 0;
    }

    // Updates
    private void updateDeath() {
        if (entityState != Anim.DEATH) {
            entityState = Anim.DEATH;
            animIndex = animTick = 0;
            addAction(PlayerAction.DYING);
            Logger.getInstance().notify("Player is dead.", Message.NOTIFICATION);
        }
        else if (animIndex == animations[entityState.ordinal()].length-1 && animTick >= animSpeed-1) {
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
            removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT);
            double pushForce = 1.2;
            pushBack(pushDirection, levelData, pushForce, PLAYER_SPEED);
            inAirUpdate();
        }
        else if (canBlock) {
            pushOffsetDirection = Direction.DOWN;
            pushDirection = (flipSign == 1) ? Direction.LEFT : Direction.RIGHT;
            if (animIndex <= animations[entityState.ordinal()].length)
                pushBack(pushDirection, levelData, 0.1, PLAYER_SPEED);
            updatePushOffset();
        }
        else updatePosition();
    }

    private void updateMove() {
        actionHandler.handleObjectActions(objectManager);
        boolean moving = checkAction(PlayerAction.MOVE);
        if (moving) {
            boolean dash = checkAction(PlayerAction.DASH);
            if (dash) {
                dashTick++;
                if (dashTick >= 40) {
                    dashTick = 0;
                    removeActions(PlayerAction.DASH, PlayerAction.DASH_HIT);
                }
            }
        }
    }

    private void updateSpells() {
        if (spellState == 1) {
            changeStamina(FLAME_COST);
            enemyManager.checkEnemySpellHit();
            objectManager.checkObjectBreak(attackBox);
        }
        boolean transform = checkAction(PlayerAction.TRANSFORM);
        if (transform) {
            changeStamina(TRANSFORM_COST);
        }
    }

    private void updateStatus() {
        if (checkAction(PlayerAction.LAVA)) changeHealthNoKnockback(-LAVA_DMG);
    }

    // Core
    public void update() {
        playerDataManager.update();
        if (currentHealth <= 0) {
            updateDeath();
            return;
        }
        updateMythicAura();
        updateAttackBox();
        setAnimation();
        updateHitBlockMove();
        updateMove();
        updateSpells();
        updateAttack();
        updateAnimation();
        updateStatus();
        checkBreakablesOnPush();

        // TODO: Isolate this somewhere
        if (checkAction(PlayerAction.MOVE) && !inAir) {
            runDustTick++;
            if (runDustTick >= 15) {
                double dustX = (flipSign == 1) ? hitBox.x : hitBox.x + hitBox.width;
                effectManager.spawnDustParticles(dustX, hitBox.y + hitBox.height, RUN_DUST_BURST, DustType.RUNNING, flipSign, this);
                runDustTick = 0;
            }
        }
        else runDustTick = 0;
    }

    private void updateMythicAura() {
        boolean hasMythic = Arrays.stream(inventory.getEquipped())
                .anyMatch(item -> item != null && item.getData() != null && item.getData().rarity == ItemRarity.MYTHIC);

        if (!hasMythic) return;

        mythicAuraTick++;
        if (mythicAuraTick >= 5) {
            mythicAuraTick = 0;
            double x = getHitBox().getCenterX();
            double y = getHitBox().getCenterY();
            effectManager.spawnDustParticles(x, y, 1, DustType.THUNDERBOLT_AURA, flipSign, this);
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            int playerXPos = (int)(hitBox.x-PLAYER_HB_OFFSET_X-xLevelOffset+flipCoefficient);
            int playerYPos = (int)(hitBox.y-PLAYER_HB_OFFSET_Y-yLevelOffset)+(int)pushOffset;

            boolean transform = checkAction(PlayerAction.TRANSFORM);
            if (!transform) g.drawImage(animations[entityState.ordinal()][animIndex], playerXPos, playerYPos, flipSign*width, height, null);
            else g.drawImage(transformAnimations[entityState.ordinal()][animIndex], playerXPos, playerYPos, flipSign*width, height, null);
        }
        catch (Exception ignored) {}
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
        attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    // Reset
    public void resetDirections() {
        removeActions(PlayerAction.LEFT, PlayerAction.RIGHT);
        animIndex = animTick = 0;
    }

    public void reset() {
        resetFlags();
        setSpellState(0);
        animIndex = animTick = 0;
        entityState = Anim.IDLE;
        resetHealth();
        currentStamina = 0;
        hitBox.x = xPos;
        hitBox.y = yPos;
        initAttackBox();
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
    }

    private void resetHealth() {
        currentHealth = maxHealth + PerksBonus.getInstance().getBonusHealth();
        double equipmentBonus = InventoryBonus.getInstance().getHealth() * currentHealth;
        currentHealth += equipmentBonus;
    }

    private void resetFlags() {
        inAir = false;
        removeActions(PlayerAction.DYING, PlayerAction.GAME_OVER);
        removeActions(PlayerAction.HIT, PlayerAction.BLOCK, PlayerAction.ATTACK, PlayerAction.MOVE, PlayerAction.DASH);
        removeActions(PlayerAction.LEFT, PlayerAction.RIGHT, PlayerAction.JUMP);
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

    // Setters
    public void setAttacking(boolean attacking) {
        if (attacking) addAction(PlayerAction.ATTACK);
        else removeAction(PlayerAction.ATTACK);
        setSpellState(0);
    }

    public void setPlayerAttackState(AttackState playerAttackState) {
        if (cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        this.attackState = playerAttackState;
        this.setAttacking(true);
        double cd =  PLAYER_ATTACK_CD + PerksBonus.getInstance().getBonusCooldown();
        double equipmentBonus = InventoryBonus.getInstance().getCooldown() * cd;
        cd -= equipmentBonus;
        cooldown[Cooldown.ATTACK.ordinal()] = cd;
    }

    public void setCurrentJumps(int currentJumps) {
        this.currentJumps = currentJumps;
    }

    public void setSpellState(int spellState) {
        if (spellState == 2 && this.spellState == 1) animIndex = 10;
        if (spellState == 0) Audio.getInstance().getAudioPlayer().stopSound(Sound.FIRE_SPELL_1);
        else if (spellState == 1) Audio.getInstance().getAudioPlayer().playSound(Sound.FIRE_SPELL_1);
        this.spellState = spellState;
    }

    public void setBlock(boolean block) {
        if (cooldown[Cooldown.BLOCK.ordinal()] != 0) return;
        if (block) addAction(PlayerAction.BLOCK);
        else removeAction(PlayerAction.BLOCK);
    }

    public void setCanTransform(boolean canTransform) {
        if (!PerksBonus.getInstance().isTransform()) return;
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

    // Getters
    public double getHorizontalSpeed() {
        double currentSpeed = checkAction(PlayerAction.LAVA) ? LAVA_PLAYER_SPEED : PLAYER_SPEED;
        if (checkAction(PlayerAction.DASH)) currentSpeed *= DASH_SPEED;
        if (checkAction(PlayerAction.LEFT) && !checkAction(PlayerAction.RIGHT)) return -currentSpeed;
        else if (checkAction(PlayerAction.RIGHT) && !checkAction(PlayerAction.LEFT)) return currentSpeed;
        return 0;
    }

    public int getAttackDmg() {
        return checkAction(PlayerAction.TRANSFORM) ? transformAttackDmg : attackDmg;
    }

    public double getCurrentStamina() {
        return currentStamina;
    }

    public int getCurrentJumps() {
        return currentJumps;
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

    public PlayerDataManager getPlayerStatusManager() {
        return playerDataManager;
    }

    public PlayerActionHandler getActionHandler() {
        return actionHandler;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public Inventory getInventory() {
        return inventory;
    }

    // Action Controller
    public void addAction(PlayerAction action) {
        actions.add(action);
    }

    public void addActions(PlayerAction ... stream) {
        actions.addAll(List.of(stream));
    }

    public void removeAction(PlayerAction action) {
        actions.remove(action);
    }

    public void removeActions(PlayerAction ... stream) {
        List.of(stream).forEach(actions::remove);
    }

    public boolean checkAction(PlayerAction action) {
        return actions.contains(action);
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
