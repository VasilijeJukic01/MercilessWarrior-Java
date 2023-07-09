package platformer.model.entities;

import platformer.animation.AnimType;
import platformer.animation.AnimationUtils;
import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.core.Game;
import platformer.debug.Message;
import platformer.model.entities.effects.EffectType;
import platformer.model.Tiles;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.objects.ObjectManager;
import platformer.model.objects.projectiles.Projectile;
import platformer.ui.overlays.UserInterface;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

@SuppressWarnings("FieldCanBeLocal")
public class Player extends Entity {

    private final Game game;
    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;
    private int[][] levelData;
    // Core Variables
    private final BufferedImage[][] animations;
    private final BufferedImage[][] transformAnimations;
    private final BufferedImage[][] effects;
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0, effectIndex = 0;
    private EffectType playerEffect;
    private AttackState attackState;
    private final double playerSpeed = 0.5 * Tiles.SCALE.getValue();
    private final double playerBoostSpeed = 0.6 * Tiles.SCALE.getValue();
    private final double xHitBoxOffset = 42 * Tiles.SCALE.getValue(), yHitBoxOffset = 16 * Tiles.SCALE.getValue();
    // Physics
    private double airSpeed = 0;
    private final double gravity = 0.035 * Tiles.SCALE.getValue();
    private final double wallGravity = 0.0005 * Tiles.SCALE.getValue();
    private final double jumpSpeed = -2.25 * Tiles.SCALE.getValue();
    private final double collisionFallSpeed = 0.5 * Tiles.SCALE.getValue();
    // Flags
    private boolean left, right, jump, moving, attacking, dash, hit, block, transform;
    private int spellState = 0;
    private boolean doubleJump, onWall, onObject, wallPush, canDash = true, canBlock, canTransform;
    // Status
    private final UserInterface userInterface;
    private final int maxStamina = 100;
    private double currentStamina = 15;
    private int currentJumps = 0, dashCount = 0;
    private int dashTick = 0;
    private int coins = 0, exp = 0, level = 1;
    private final int maxExp = 10000;
    // Cooldowns
    private final double[] cooldown;

    // Init
    public Player(int xPos, int yPos, int width, int height, EnemyManager enemyManager, ObjectManager objectManager, Game game) {
        super(xPos, yPos, width, height, 100);
        this.game = game;
        this.enemyManager = enemyManager;
        this.objectManager = objectManager;
        this.animations = AnimationUtils.getInstance().loadPlayerAnimations(width, height, "player");
        this.transformAnimations = AnimationUtils.getInstance().loadPlayerAnimations(width, height, "transform");
        this.effects = AnimationUtils.getInstance().loadEffects();
        this.userInterface = new UserInterface(this);
        initHitBox((int)(15*Tiles.SCALE.getValue()), (int)(44*Tiles.SCALE.getValue()));
        initAttackBox();
        this.cooldown = new double[3];
    }

    private void initAttackBox() {
        int w = (int)(20*Tiles.SCALE.getValue());
        int h =  (int)(35*Tiles.SCALE.getValue());
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, w, h);
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
            effectIndex++;
            if (spellState == 1 && animIndex >= animations[entityState.ordinal()].length-5) {
                animIndex = 2;
            }
            if (animIndex >= animations[entityState.ordinal()].length) {
                animIndex = 0;
                attacking = false;
                attackCheck = false;
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
            this.flipCoefficient = (int)(width-hitBox.width-13*Tiles.SCALE.getValue());
            this.flipSign = -1;
        }
        else if (moving && right && !onWall) {
            this.flipCoefficient = 0;
            this.flipSign = 1;
        }
    }

    private void updateEffectAnimation() {
        if (playerEffect == EffectType.DOUBLE_JUMP) {
            if (effectIndex >= effects[EffectType.DOUBLE_JUMP.ordinal()].length) {
                effectIndex = 0;
                doubleJump = false;
            }
        }
        else if (playerEffect == EffectType.WALL_SLIDE) {
            if (effectIndex >= effects[EffectType.WALL_SLIDE.ordinal()].length) {
                effectIndex = 2;
            }
        }
    }

    private void setAnimation() {
        if (spellState == 2) return;
        AnimType previousAction = entityState;

        if (moving) entityState = AnimType.RUN;
        else entityState = AnimType.IDLE;

        if (inAir) {
            if (airSpeed < 0) {
                entityState = AnimType.JUMP;
                setPlayerEffect(EffectType.DOUBLE_JUMP);
            }
            else if (airSpeed > 0) entityState = AnimType.FALL;
        }
        if (onWall && !onObject) {
            entityState = AnimType.WALL;
            setPlayerEffect(EffectType.WALL_SLIDE);
        }
        if (dash) {
            entityState = AnimType.ATTACK_1;
            animIndex = 1;
            animTick = 0;
            return;
        }
        if (spellState == 1) entityState = AnimType.SPELL_1;
        else if (canBlock) entityState = AnimType.BLOCK;
        else if (hit) entityState = AnimType.HIT;
        else if (attacking && !onWall) {
            if (attackState == AttackState.ATTACK_1) entityState = AnimType.ATTACK_1;
            else if (attackState == AttackState.ATTACK_2) entityState = AnimType.ATTACK_2;
            else if (attackState == AttackState.ATTACK_3) entityState = AnimType.ATTACK_3;
        }
        else if (canTransform) entityState = AnimType.TRANSFORM;
        if (previousAction != entityState) animIndex = animTick = 0;
    }

    private void coolDownTickUpdate() {
        for (int i = 0; i < cooldown.length; i++) {
            if (cooldown[i] > 0) {
                cooldown[i] -= 0.1;
                if (cooldown[i] < 0) cooldown[i] = 0;
            }
        }
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

        if (left) dx -= playerSpeed;
        if (right) dx += playerSpeed;
        if (right && inAir && wallPush && !dash) dx += playerBoostSpeed;
        if (left && inAir && wallPush && !dash) dx -= playerBoostSpeed;

        if (dash) {
            if (((!left && !right) || (left && right)) && flipSign == -1) dx = -playerSpeed;
            else if (((!left && !right) || (left && right)) && flipSign == 1) dx = playerSpeed;
            dx *= 6;
        }

        if (inAir && !dash) {
            inAirUpdate();
        }
        updateX(dx);
        moving = true;
    }

    private void inAirUpdate() {
        if (Utils.getInstance().canMoveHere(hitBox.x, hitBox.y + airSpeed, hitBox.width, hitBox.height, levelData)) {
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
                dash = false;
                dashTick = 0;
            }
        }
    }

    private void updateAttackBox() {
        if (spellState != 0) return;
        if ((right && left) || (!right && !left)) {
            if (flipSign == 1) attackBox.x = hitBox.x + hitBox.width + (int)(10*Tiles.SCALE.getValue());
            else attackBox.x = hitBox.x - hitBox.width - (int)(10*Tiles.SCALE.getValue());
        }
        if (right || (dash && flipSign == 1)) attackBox.x = hitBox.x + hitBox.width + (int)(10*Tiles.SCALE.getValue());
        else if (left || (dash && flipSign == -1)) attackBox.x = hitBox.x - hitBox.width - (int)(10*Tiles.SCALE.getValue());
        attackBox.y = hitBox.y + (int)(10*Tiles.SCALE.getValue());
    }

    private void updateAttack() {
        if (attacking || dash) checkAttack();
    }

    // Checks
    private void checkAttack() {
        if (attackCheck || animIndex != 1) return;
        attackCheck = !dash;
        enemyManager.checkEnemyHit(attackBox, this);
        objectManager.checkObjectBreak(attackBox);
    }

    private void checkOnObject() {
        if (objectManager.isPlayerTouchingObject() && !onObject) {
            inAir = wallPush = false;
            playerEffect = null;
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
        if (onObject && Utils.getInstance().isTileSolid((int)(hitBox.x/Tiles.TILES_SIZE.getValue()), (int)((hitBox.y-5)/Tiles.TILES_SIZE.getValue()), levelData)) return;

        if (currentJumps == 1) {
            doubleJump = true;
            animIndex = animTick = 0;
            effectIndex = 0;
            currentJumps++;
        }
        inAir = true;
        airSpeed = jumpSpeed;
    }

    public void doDash() {
        if (Utils.getInstance().isTouchingWall(hitBox,Direction.LEFT) || Utils.getInstance().isTouchingWall(hitBox,Direction.RIGHT)) return;
        if (dashCount > 0) return;
        if (dash || !canDash) return;
        if (currentStamina >= 3) {
            dash = true;
            dashCount++;
            canDash = false;
            Audio.getInstance().getAudioPlayer().playSound(Sounds.DASH.ordinal());
            changeStamina(-3);
            cooldown[Cooldown.DASH.ordinal()] = 1;
        }
    }

    public void doSpell() {
        if (inAir) return;
        if (currentStamina >= 5) {
            setSpellState(1);
            game.notifyLogger("Player has used FLAME spell!", Message.INFORMATION);
        }
    }

    public void changeHealth(int value) {
        if (value < 0) {
            if (hit) return;
            else hit = true;
        }
        currentHealth += value;
        currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
    }

    public void changeHealth(int value, Object o) {
        if (!(o instanceof Enemy) && !(o instanceof Projectile)) return;
        if (hit) return;
        changeHealth(value);
        pushOffsetDirection = Direction.UP;
        pushOffset = 0;
        Rectangle2D.Double hBox =  (o instanceof Enemy) ? (((Enemy) o).getHitBox()) : (((Projectile) o).getHitBox());
        game.notifyLogger("Damage received: "+value, Message.INFORMATION);
        if (hBox.x < hitBox.x && (o instanceof SpearWoman)) pushDirection = Direction.RIGHT;
        else pushDirection = Direction.LEFT;
    }

    public void changeStamina(double value) {
        currentStamina += value;
        currentStamina = Math.max(Math.min(currentStamina, maxStamina), 0);
        if (currentStamina == 0) {
            transform = false;
            if (spellState == 1) spellState = 2;
        }
    }

    public void changeExp(double value) {
        exp += value;
        exp = Math.max(Math.min(exp, maxExp), 0);
        if (exp > 1000*level) {
            exp = exp % (1000*level);
            level++;
        }
    }

    public void changeCoins(int value) {
        coins += value;
        coins = Math.max(coins, 0);
    }

    public void kill() {
        currentHealth = 0;
    }

    // Updates
    private void updateDeath() {
        if (entityState != AnimType.DEATH) {
            entityState = AnimType.DEATH;
            animIndex = animTick = 0;
            game.setDying(true);
            game.notifyLogger("Player is dead.", Message.NOTIFICATION);
        }
        else if (animIndex == animations[entityState.ordinal()].length-1 && animTick >= animSpeed-1) {
            game.setGameOver(true);
            Audio.getInstance().getAudioPlayer().stopSong();
            Audio.getInstance().getAudioPlayer().playSound(Sounds.GAME_OVER.ordinal());
        }
        else updateAnimation();
    }

    private void updateHitBlockMove() {
        if (hit) {
            setSpellState(0);
            dash = false;
            if (animIndex <= animations[entityState.ordinal()].length - 2)
                pushBack(pushDirection, levelData, 1.2, playerSpeed);
            updatePushOffset();
            inAirUpdate();
        }
        else if (canBlock) {
            pushOffsetDirection = Direction.DOWN;
            pushDirection = (flipSign == 1) ? Direction.LEFT : Direction.RIGHT;
            if (animIndex <= animations[entityState.ordinal()].length )
                pushBack(pushDirection, levelData, 0.1, playerSpeed);
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
                    dash = false;
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
        userInterface.update(currentHealth, maxHealth, currentStamina, maxStamina, exp, 1000*level);
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
        updateEffectAnimation();
    }

    private void renderEffects(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            if (playerEffect == EffectType.DOUBLE_JUMP && doubleJump) {
                int effectXPos = (int)(hitBox.x-xHitBoxOffset-xLevelOffset)+(int)(20*Tiles.SCALE.getValue());
                int effectYPos = (int)(hitBox.y-yHitBoxOffset-yLevelOffset)+(int)(55*Tiles.SCALE.getValue());
                g.drawImage(effects[0][effectIndex], effectXPos, effectYPos, effects[0][effectIndex].getWidth(), effects[0][effectIndex].getHeight(), null);
            }
            else if (playerEffect == EffectType.WALL_SLIDE && onWall) {
                int newFlip = (flipCoefficient != 0) ? (0) : (int)(width-hitBox.width-10*Tiles.SCALE.getValue()), newSign = (flipSign == 1) ? (-1) : (1);
                int effectXPos = (int)(hitBox.x-xHitBoxOffset-xLevelOffset)+(int)(newSign*27*Tiles.SCALE.getValue())+newFlip;
                int effectYPos = (int)(hitBox.y-yHitBoxOffset-yLevelOffset)-(int)(Tiles.SCALE.getValue());
                int effectWid = newSign*(effects[1][effectIndex].getWidth()+(int)(10*Tiles.SCALE.getValue()));
                int effectHei = effects[1][effectIndex].getHeight()+(int)(50*Tiles.SCALE.getValue());
                g.drawImage(effects[1][effectIndex], effectXPos, effectYPos, effectWid, effectHei, null);
            }
        }
        catch (Exception ignored) {}
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEffects(g, xLevelOffset, yLevelOffset);
        try {
            int playerXPos = (int)(hitBox.x-xHitBoxOffset-xLevelOffset+flipCoefficient);
            int playerYPos = (int)(hitBox.y-yHitBoxOffset-yLevelOffset)+(int)pushOffset;

            if (!transform) g.drawImage(animations[entityState.ordinal()][animIndex], playerXPos, playerYPos, flipSign*width, height, null);
            else g.drawImage(transformAnimations[entityState.ordinal()][animIndex], playerXPos, playerYPos, flipSign*width, height, null);
        }
        catch (Exception ignored) {}
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
        attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    // Getters & Setters & Reset
    public void resetDirections() {
        left = right = false;
        animIndex = animTick = 0;
    }

    public void reset() {
        moving = attacking = inAir = hit = block = false;
        setSpellState(0);
        left = right = jump = false;
        animIndex = animTick = 0;
        entityState = AnimType.IDLE;
        currentHealth = maxHealth;
        currentStamina = 0;
        hitBox.x = xPos;
        hitBox.y = yPos;
        initAttackBox();
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData)) inAir = true;
    }

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
        this.attackState = playerAttackState;
        this.setAttacking(true);
    }

    public void setPlayerEffect(EffectType playerEffect) {
        if (playerEffect != this.playerEffect) effectIndex = 0;
        this.playerEffect = playerEffect;
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

    public boolean isOnWall() {
        return onWall;
    }

    public boolean canDash() {
        return canDash;
    }

    public void setCanDash(boolean canDash) {
        this.canDash = canDash;
    }

    public boolean isDash() {
        return dash;
    }

    public int getSpellState() {
        return spellState;
    }

    public void setSpellState(int spellState) {
        if (spellState == 2 && this.spellState == 1) animIndex = 10;
        if (spellState == 0) Audio.getInstance().getAudioPlayer().stopSound(Sounds.FIRE_SPELL_1.ordinal());
        else if (spellState == 1) Audio.getInstance().getAudioPlayer().playSound(Sounds.FIRE_SPELL_1.ordinal());
        this.spellState = spellState;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public boolean isBlock() {
        return block;
    }

    public void setCanBlock(boolean canBlock) {
        this.canBlock = canBlock;
        if (canBlock) {
            game.notifyLogger("Damage blocked successfully!", Message.INFORMATION);
            cooldown[Cooldown.BLOCK.ordinal()] = 1.2;
        }
    }

    public boolean canBlock() {
        return canBlock;
    }

    public int getCoins() {
        return coins;
    }

    public int getLevel() {
        return level;
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }

    public double[] getCooldown() {
        return cooldown;
    }

    public void setCanTransform(boolean canTransform) {
        if (transform) transform = false;
        else {
            this.canTransform = canTransform;
            dash = false;
        }
    }

    public boolean isTransform() {
        return transform;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.GREEN);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }
}
