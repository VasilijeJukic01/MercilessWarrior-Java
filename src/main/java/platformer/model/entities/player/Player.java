package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.animation.AnimUtils;
import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.core.Game;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.*;
import platformer.model.entities.effects.EffectType;
import platformer.model.entities.effects.PlayerEffectController;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.objects.ObjectManager;
import platformer.model.objects.projectiles.Projectile;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.PLAYER_SHEET;
import static platformer.constants.FilePaths.PLAYER_TRANSFORM_SHEET;

@SuppressWarnings("FieldCanBeLocal")
public class Player extends Entity {

    private final Game game;
    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;
    private int[][] levelData;
    // Core Variables
    private BufferedImage[][] animations, transformAnimations;
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0;
    private AttackState attackState;
    // Physics
    private final double gravity = 0.035 * SCALE;
    private final double wallGravity = 0.0005 * SCALE;
    private final double jumpSpeed = -2.25 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;
    // Flags
    private boolean left, right, jump, moving, attacking, dash, dashHit, hit, block, transform;
    private int spellState = 0;
    private boolean doubleJump, onWall, onObject, wallPush, canDash = true, canBlock, canTransform;
    // Status
    private int currentJumps = 0, dashCount = 0;
    private int dashTick = 0;
    private final int attackDmg = 5, transformAttackDmg = 8;
    private double currentStamina = 15;
    private final PlayerDataManager playerDataManager;
    // Effect
    private final PlayerEffectController effectController;

    public Player(int xPos, int yPos, int width, int height, EnemyManager enemyManager, ObjectManager objectManager, Game game) {
        super(xPos, yPos, width, height, PLAYER_MAX_HP);
        this.game = game;
        this.enemyManager = enemyManager;
        this.objectManager = objectManager;
        loadAnimations();
        initHitBox(PLAYER_HB_WID, PLAYER_HB_HEI);
        initAttackBox(PLAYER_AB_WID, PLAYER_AB_HEI);
        this.cooldown = new double[3];
        this.playerDataManager = new PlayerDataManager(game.getAccount(), this);
        this.effectController = new PlayerEffectController(this);
    }

    // Init
    private void initAttackBox(int wid, int hei) {
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, wid, hei);
    }

    private void loadAnimations() {
        this.animations = AnimUtils.getInstance().loadPlayerAnimations(width, height, PLAYER_SHEET);
        this.transformAnimations = AnimUtils.getInstance().loadPlayerAnimations(width, height, PLAYER_TRANSFORM_SHEET);
    }

    public void loadLvlData(int[][] levelData) {
        this.levelData = levelData;
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData)) inAir = true;
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
            if (spellState == 1 && animIndex >= animations[entityState.ordinal()].length-5) {
                animIndex = 2;
            }
            if (animIndex >= animations[entityState.ordinal()].length) {
                animIndex = 0;
                attacking = attackCheck = false;
                dashHit = false;
                block = canBlock = false;
                if (canTransform) {
                    canTransform = false;
                    transform = true;
                }
                setSpellState(0);
                if (hit) {
                    hit = false;
                    airSpeed = 0;
                }
            }
            coolDownTickUpdate();
        }
        // Wall flip lock
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

        if (moving) entityState = Anim.RUN;
        else entityState = Anim.IDLE;

        if (inAir) {
            if (airSpeed < 0) entityState = Anim.JUMP;
            else if (airSpeed > 0) entityState = Anim.FALL;
        }
        if (onWall && !onObject) {
            entityState = Anim.WALL;
            effectController.setPlayerEffect(EffectType.WALL_SLIDE);
        }
        if (dash) {
            entityState = Anim.ATTACK_1;
            animIndex = 1;
            animTick = 0;
            return;
        }
        if (spellState == 1) entityState = Anim.SPELL_1;
        else if (canBlock) entityState = Anim.BLOCK;
        else if (hit) entityState = Anim.HIT;
        else if (attacking && !onWall) {
            if (attackState == AttackState.ATTACK_1) entityState = Anim.ATTACK_1;
            else if (attackState == AttackState.ATTACK_2) entityState = Anim.ATTACK_2;
            else if (attackState == AttackState.ATTACK_3) entityState = Anim.ATTACK_3;
        }
        else if (canTransform) entityState = Anim.TRANSFORM;
        if (previousAction != entityState) animIndex = animTick = 0;
    }

    // Positioning
    private void updatePosition() {
        moving = false;
        checkOnObject();
        if (!inAir && !Utils.getInstance().isEntityOnFloor(hitBox, levelData) && !onObject) inAir = true;

        if (spellState != 0 || canBlock || canTransform) return;
        if (jump) doJump();
        if (!inAir || onWall) dashCount = 0;
        if (((!left && !right) || (left && right)) && !inAir && !dash) return;

        double dx = 0;

        updateWallPosition();

        if (left) dx -= PLAYER_SPEED;
        if (right) dx += PLAYER_SPEED;
        if (right && inAir && wallPush && !dash) dx += PLAYER_BOOST;
        if (left && inAir && wallPush && !dash) dx -= PLAYER_BOOST;

        if (dash) {
            if (((!left && !right) || (left && right)) && flipSign == -1) dx = -PLAYER_SPEED;
            else if (((!left && !right) || (left && right)) && flipSign == 1) dx = PLAYER_SPEED;
            dx *= 6;
        }

        if (inAir && !dash) {
            inAirUpdate();
        }
        updateX(dx);
        moving = true;
    }

    private void inAirUpdate() {
        if (Utils.getInstance().canMoveHere(hitBox.x, hitBox.y + airSpeed + 1, hitBox.width, hitBox.height, levelData)) {
            if (onWall && airSpeed > 0) airSpeed += wallGravity;
            else airSpeed += gravity;
            hitBox.y += airSpeed;
        }
        else {
            hitBox.y = Utils.getInstance().getYPosOnTheCeil(hitBox, airSpeed);
            if (airSpeed > 0) {
                inAir = wallPush = false;
                airSpeed = 0;
                currentJumps = 0;
            }
            else {
                if (onWall) airSpeed = wallGravity;
                else airSpeed = collisionFallSpeed;
                doubleJump = false;
            }
        }
    }

    private void updateWallPosition() {
        if (onWall) attacking = false;
        boolean leftSideCheck = Utils.getInstance().isOnWall(hitBox, levelData, Direction.LEFT);
        boolean rightSideCheck = Utils.getInstance().isOnWall(hitBox, levelData, Direction.RIGHT);
        if (!onWall && ((left && leftSideCheck) || (right && rightSideCheck)) && !Utils.getInstance().isEntityOnFloor(hitBox, levelData)) {
            if ((flipSign == -1 && rightSideCheck) || (flipSign == 1 && leftSideCheck)) return;
            onWall = wallPush = true;
            currentJumps = 1;
            airSpeed = 0.1;
        }
        if (onWall && !leftSideCheck && !rightSideCheck) {
            currentJumps = 0;
            onWall = false;
        }
    }

    private void updateX(double dx) {
        if (!onObject && !onWall && Utils.getInstance().canMoveHere(hitBox.x+dx, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x += dx;
        else if (dash && Utils.getInstance().canMoveHere(hitBox.x+dx, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x += dx;
        else {
            if (onObject) hitBox.x = objectManager.getXObjectBound(hitBox, dx);
            else if (!onWall) hitBox.x = Utils.getInstance().getXPosOnTheWall(hitBox, dx);
            if (dash) {
                dash = dashHit = false;
                dashTick = 0;
            }
        }
    }

    private void updateAttackBox() {
        if (spellState != 0) return;
        if ((right && left) || (!right && !left)) {
            if (flipSign == 1) attackBox.x = hitBox.x + hitBox.width + (int)(10*SCALE);
            else attackBox.x = hitBox.x - hitBox.width - (int)(10*SCALE);
        }
        if (right || (dash && flipSign == 1)) attackBox.x = hitBox.x + hitBox.width + (int)(10*SCALE);
        else if (left || (dash && flipSign == -1)) attackBox.x = hitBox.x - hitBox.width - (int)(10*SCALE);
        attackBox.y = hitBox.y + (int)(10*SCALE);
    }

    private void updateAttack() {
        if (attacking || dash) checkAttack();
    }

    // Checks
    private void checkAttack() {
        if (attackCheck || animIndex != 1 || dashHit) return;
        attackCheck = !dash;
        enemyManager.checkEnemyHit(attackBox, this);
        objectManager.checkObjectBreak(attackBox);
        objectManager.checkArrowDeflect(attackBox);
    }

    private void checkOnObject() {
        if (objectManager.isPlayerTouchingObject() && !onObject) {
            inAir = wallPush = false;
            entityEffect = null;
            airSpeed = 0;
            currentJumps = 0;
            onObject = true;
        }
        else if (onObject && !objectManager.isPlayerTouchingObject()) onObject = false;
    }

    private void checkPotionCollide() {
        objectManager.checkObjectPick(hitBox);
    }

    private void checkTrapCollide() {
        objectManager.checkPlayerIntersection(this);
    }

    // Actions
    public void doJump() {
        if (Utils.getInstance().isOnWall(hitBox, levelData, Direction.LEFT) && left && !right) return;
        if (Utils.getInstance().isOnWall(hitBox, levelData, Direction.RIGHT) && right && !left) return;
        if (inAir && doubleJump && onWall) return;
        if (inAir && currentJumps != 1) return;
        if (onObject && Utils.getInstance().isTileSolid((int)(hitBox.x/TILES_SIZE), (int)((hitBox.y-5)/TILES_SIZE), levelData)) return;

        if (currentJumps == 1) {
            doubleJump = true;
            animIndex = animTick = 0;
            currentJumps++;
        }
        inAir = true;
        airSpeed = jumpSpeed;
    }

    public void doDash() {
        if (cooldown[Cooldown.DASH.ordinal()] != 0) return;
        if (Utils.getInstance().isTouchingWall(hitBox,Direction.LEFT) || Utils.getInstance().isTouchingWall(hitBox,Direction.RIGHT)) return;
        if (dashCount > 0) return;
        if (dash || !canDash) return;
        if (currentStamina >= 3) {
            dash = true;
            dashCount++;
            canDash = false;
            Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
            changeStamina(-3);
            cooldown[Cooldown.DASH.ordinal()] = 1.75 + PlayerBonus.getInstance().getDashCooldown();
        }
    }

    public void doSpell() {
        if (inAir) return;
        if (currentStamina >= 5) {
            setSpellState(1);
            Logger.getInstance().notify("Player has used FLAME spell!", Message.INFORMATION);
        }
    }

    // Status Changes
    public void changeHealth(int value) {
        if (value < 0) {
            if (hit) return;
            else hit = true;
        }
        currentHealth += value;
        currentHealth = Math.max(Math.min(currentHealth, maxHealth+PlayerBonus.getInstance().getBonusHealth()), 0);
    }

    public void changeHealth(int value, Object o) {
        if (!(o instanceof Enemy) && !(o instanceof Projectile)) return;
        if (hit) return;
        changeHealth(value);
        pushOffsetDirection = Direction.UP;
        pushOffset = 0;
        Rectangle2D.Double hBox =  (o instanceof Enemy) ? (((Enemy) o).getHitBox()) : (((Projectile) o).getHitBox());
        Logger.getInstance().notify("Damage received: "+value, Message.INFORMATION);
        if (hBox.x < hitBox.x) pushDirection = Direction.RIGHT;
        else pushDirection = Direction.LEFT;
    }

    public void changeStamina(double value) {
        currentStamina += value;
        currentStamina = Math.max(Math.min(currentStamina, PLAYER_MAX_ST+PlayerBonus.getInstance().getBonusPower()), 0);
        if (currentStamina == 0) {
            transform = false;
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
            game.setDying(true);
            Logger.getInstance().notify("Player is dead.", Message.NOTIFICATION);
        }
        else if (animIndex == animations[entityState.ordinal()].length-1 && animTick >= animSpeed-1) {
            game.setGameOver(true);
            Audio.getInstance().getAudioPlayer().stopSong();
            Audio.getInstance().getAudioPlayer().playSound(Sound.GAME_OVER);
        }
        else updateAnimation();
    }

    private void updateHitBlockMove() {
        if (hit) {
            setSpellState(0);
            dash = dashHit = false;
            if (animIndex <= animations[entityState.ordinal()].length - 2)
                pushBack(pushDirection, levelData, 1.2, PLAYER_SPEED);
            updatePushOffset();
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
        if (moving) {
            checkPotionCollide();
            checkTrapCollide();
            if (dash) {
                dashTick++;
                if (dashTick >= 40) {
                    dashTick = 0;
                    dash = dashHit = false;
                }
            }
        }
    }

    private void updateSpells() {
        if (spellState == 1) {
            changeStamina(-0.20);
            enemyManager.checkEnemySpellHit();
            objectManager.checkObjectBreak(attackBox);
        }
        if (transform) {
            changeStamina(-0.025);
        }
    }

    // Core
    public void update() {
        playerDataManager.update();
        if (currentHealth <= 0) {
            updateDeath();
            return;
        }
        updateAttackBox();
        setAnimation();
        updateHitBlockMove();
        updateMove();
        updateSpells();
        updateAttack();
        updateAnimation();
        effectController.update();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        effectController.render(g, xLevelOffset, yLevelOffset);
        try {
            int playerXPos = (int)(hitBox.x-PLAYER_HB_OFFSET_X-xLevelOffset+flipCoefficient);
            int playerYPos = (int)(hitBox.y-PLAYER_HB_OFFSET_Y-yLevelOffset)+(int)pushOffset;

            if (!transform) g.drawImage(animations[entityState.ordinal()][animIndex], playerXPos, playerYPos, flipSign*width, height, null);
            else g.drawImage(transformAnimations[entityState.ordinal()][animIndex], playerXPos, playerYPos, flipSign*width, height, null);
        }
        catch (Exception ignored) {}
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
        attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    // Reset
    public void resetDirections() {
        left = right = false;
        animIndex = animTick = 0;
    }

    public void reset() {
        resetFlags();
        setSpellState(0);
        animIndex = animTick = 0;
        entityState = Anim.IDLE;
        currentHealth = maxHealth+PlayerBonus.getInstance().getBonusHealth();
        currentStamina = 0;
        hitBox.x = xPos;
        hitBox.y = yPos;
        initAttackBox(PLAYER_AB_WID, PLAYER_AB_HEI);
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData)) inAir = true;
    }

    private void resetFlags() {
        moving = attacking = inAir = hit = block = false;
        left = right = jump = false;
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
        this.attacking = attacking;
        setSpellState(0);
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setPlayerAttackState(AttackState playerAttackState) {
        if (cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        this.attackState = playerAttackState;
        this.setAttacking(true);
        cooldown[Cooldown.ATTACK.ordinal()] = 0.75 + PlayerBonus.getInstance().getBonusCooldown();
    }

    public void setCanDash(boolean canDash) {
        this.canDash = canDash;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public int getCurrentJumps() {
        return currentJumps;
    }

    public void setCurrentJumps(int currentJumps) {
        this.currentJumps = currentJumps;
    }

    public void setOnWall(boolean onWall) {
        this.onWall = onWall;
    }

    public void setDashHit(boolean dashHit) {
        this.dashHit = dashHit;
    }

    public void setSpellState(int spellState) {
        if (spellState == 2 && this.spellState == 1) animIndex = 10;
        if (spellState == 0) Audio.getInstance().getAudioPlayer().stopSound(Sound.FIRE_SPELL_1);
        else if (spellState == 1) Audio.getInstance().getAudioPlayer().playSound(Sound.FIRE_SPELL_1);
        this.spellState = spellState;
    }

    public void setBlock(boolean block) {
        if (cooldown[Cooldown.BLOCK.ordinal()] != 0) return;
        this.block = block;
    }

    public boolean isBlock() {
        return block;
    }

    public void setCanBlock(boolean canBlock) {
        this.canBlock = canBlock;
        if (canBlock) {
            Logger.getInstance().notify("Damage blocked successfully!", Message.INFORMATION);
            cooldown[Cooldown.BLOCK.ordinal()] = 1.2;
            if (PlayerBonus.getInstance().isRestorePower()) changeStamina(5);
        }
    }

    public void setCanTransform(boolean canTransform) {
        if (!PlayerBonus.getInstance().isTransform()) return;
        if (transform) transform = false;
        else {
            this.canTransform = canTransform;
            dash = dashHit = false;
        }
    }

    // Getters
    public double getCurrentStamina() {
        return currentStamina;
    }

    public boolean isOnWall() {
        return onWall;
    }

    public boolean canDash() {
        return canDash;
    }

    public boolean isDash() {
        return dash;
    }

    public int getSpellState() {
        return spellState;
    }

    public int getAttackDmg() {
        return attackDmg;
    }

    public int getTransformAttackDmg() {
        return transformAttackDmg;
    }

    public boolean canBlock() {
        return canBlock;
    }

    public int getCoins() {
        return playerDataManager.getCoins();
    }

    public int getLevel() {
        return playerDataManager.getLevel();
    }

    public boolean isTransform() {
        return transform;
    }

    public int getUpgradeTokens() {
        return playerDataManager.getUpgradeTokens();
    }

    public PlayerDataManager getPlayerStatusManager() {
        return playerDataManager;
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
