package platformer.model.entities;

import platformer.animation.AnimType;
import platformer.animation.AnimationUtils;
import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.core.Game;
import platformer.model.entities.effects.EffectType;
import platformer.model.Tiles;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.objects.ObjectManager;
import platformer.model.objects.Projectile;
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
    private final BufferedImage[][] effects;
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0, effectIndex = 0;
    private EffectType playerEffect;
    private AttackState attackState;
    private final double playerSpeed = 0.5 * Tiles.SCALE.getValue();
    private final double playerBoostSpeed = 0.6 * Tiles.SCALE.getValue();
    private final double xHitBoxOffset = 36 * Tiles.SCALE.getValue(), yHitBoxOffset = 16 * Tiles.SCALE.getValue();
    // Physics
    private double airSpeed = 0;
    private final double gravity = 0.035 * Tiles.SCALE.getValue();
    private final double wallGravity = 0.0005 * Tiles.SCALE.getValue();
    private final double jumpSpeed = -2.25 * Tiles.SCALE.getValue();
    private final double collisionFallSpeed = 0.5 * Tiles.SCALE.getValue();
    // Flags
    private boolean left, right, jump, moving, attacking, dash, hit, block;
    private int spellState = 0;
    private boolean doubleJump, onWall, onObject, wallPush, canDash = true, canBlock;
    // Status
    private final BufferedImage statusBar;
    private int healthWidth = (int)(150*Tiles.SCALE.getValue()), staminaWidth = (int)(115*Tiles.SCALE.getValue());
    private final int maxStamina = 100;
    private double currentStamina = 15;
    private int currentJumps = 0, dashCount = 0;
    private int dashTick = 0;

    // Init
    public Player(int xPos, int yPos, int width, int height, EnemyManager enemyManager, ObjectManager objectManager, Game game) {
        super(xPos, yPos, width, height, 100);
        this.game = game;
        this.enemyManager = enemyManager;
        this.objectManager = objectManager;
        this.animations = AnimationUtils.getInstance().loadPlayerAnimations(width, height);
        this.effects = AnimationUtils.getInstance().loadEffects();
        initHitBox((int)(19*Tiles.SCALE.getValue()), (int)(44*Tiles.SCALE.getValue()));
        initAttackBox();
        this.statusBar = Utils.getInstance().importImage("src/main/resources/images/health_power_bar.png",-1,-1);
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
                setSpellState(0);
                if (hit) {
                    hit = false;
                    airSpeed = 0;
                    if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData)) inAir = true;
                }
            }
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
        AnimType previousAction = entityState;

        if (spellState == 2) return;

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
        if (spellState == 1) {
            entityState = AnimType.SPELL_1;
        }
        if (canBlock) entityState = AnimType.BLOCK;
        if (hit) entityState = AnimType.HIT;
        if (attacking && !onWall) {
            if (attackState == AttackState.ATTACK_1) entityState = AnimType.ATTACK_1;
            else if (attackState == AttackState.ATTACK_2) entityState = AnimType.ATTACK_2;
            else if (attackState == AttackState.ATTACK_3) entityState = AnimType.ATTACK_3;
        }
        if (previousAction != entityState) animIndex = animTick = 0;
    }

    // Positioning
    private void updatePosition() {
        moving = false;
        if (spellState != 0 || canBlock) return;
        if (jump) doJump();
        if (!inAir || onWall) dashCount = 0;
        if (!left && !right && !inAir && !dash) return;
        if (left && right && !inAir && !dash) return;

        double dx = 0;

        updateWallPosition();
        checkOnObject();

        if (left) dx -= playerSpeed;
        if (right) dx += playerSpeed;
        if (right && inAir && wallPush && !dash) dx += playerBoostSpeed;
        if (left && inAir && wallPush && !dash) dx -= playerBoostSpeed;

        if (dash) {
            if (((!left && !right) || (right && left)) && flipSign == -1) dx = -playerSpeed;
            else if (((!left && !right) || (right && left)) && flipSign == 1) dx = playerSpeed;
            dx *= 6;
        }

        if (!inAir && !Utils.getInstance().isEntityOnFloor(hitBox, levelData) && !onObject) inAir = true;

        if (inAir && !dash) {
            if (Utils.getInstance().canMoveHere(hitBox.x, hitBox.y + airSpeed, hitBox.width, hitBox.height, levelData)) {
                if (onWall && airSpeed > 0) airSpeed += wallGravity;
                else airSpeed += gravity;
                hitBox.y += airSpeed;
            }
            else {
                if (onObject) hitBox.y = objectManager.getYObjectBound(hitBox, airSpeed);
                else hitBox.y = Utils.getInstance().getYPosOnTheCeil(hitBox, airSpeed);
                if (airSpeed > 0) {
                    inAir = false;
                    wallPush = false;
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
        updateX(dx);
        moving = true;
    }

    private void updateWallPosition() {
        if (onWall) attacking = false;
        if (!onWall && ((left && Utils.getInstance().isOnWall(hitBox, levelData, Direction.LEFT)) || (right && Utils.getInstance().isOnWall(hitBox, levelData, Direction.RIGHT))) &&
                !Utils.getInstance().isEntityOnFloor(hitBox, levelData)) {
            onWall = true;
            wallPush = true;
            currentJumps = 1;
            airSpeed = 0.1;
        }
        if (onWall && !Utils.getInstance().isOnWall(hitBox, levelData, Direction.LEFT) && !Utils.getInstance().isOnWall(hitBox, levelData, Direction.RIGHT)) {
            if (onWall) currentJumps = 0;
            onWall = false;
        }
    }

    private void updateX(double dx) {
        if (!onObject && !onWall && Utils.getInstance().canMoveHere(hitBox.x+dx, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            this.hitBox.x += dx;
        }
        else if (dash && Utils.getInstance().canMoveHere(hitBox.x+dx, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            this.hitBox.x += dx;
        }
        else {
            if (onObject) this.hitBox.x = objectManager.getXObjectBound(hitBox, inAir, dx);
            else if (!onWall) this.hitBox.x = Utils.getInstance().getXPosOnTheWall(hitBox, dx);
            if (dash) {
                dash = false;
                dashTick = 0;
            }
        }
    }

    private void updateBars() {
        this.healthWidth = (int)((currentHealth / (double)maxHealth) * (int)(150*Tiles.SCALE.getValue()));
        this.staminaWidth = (int)((currentStamina / (double)maxStamina) * (int)(115*Tiles.SCALE.getValue()));
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
        attackCheck = true;
        if (dash) attackCheck = false;
        enemyManager.checkEnemyHit(attackBox, this);
        objectManager.checkObjectBreak(attackBox);
    }

    private void checkOnObject() {
        if (objectManager.isPlayerTouchingObject() && !onObject) onObject = true;
        else if (onObject && !objectManager.isPlayerTouchingObject()) onObject = false;
    }

    private void checkPotionCollide() {
        objectManager.checkObjectPick(hitBox);
    }

    private void checkTrapCollide() {
        objectManager.checkSpikeHit(this);
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
        }
    }

    public void doSpell() {
        if (inAir) return;
        if (currentStamina >= 5) {
            setSpellState(1);
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

    public void changeHealth(int value, Enemy e) {
        if (hit) return;
        changeHealth(value);
        pushOffsetDirection = Direction.UP;
        pushOffset = 0;
        if (e.getHitBox().x < hitBox.x) pushDirection = Direction.RIGHT;
        else pushDirection = Direction.LEFT;
    }

    public void changeHealth(int value, Projectile p) {
        if (hit) return;
        changeHealth(value);
        pushOffsetDirection = Direction.UP;
        pushOffset = 0;
        if (p.getHitBox().x < hitBox.x) pushDirection = Direction.RIGHT;
        else pushDirection = Direction.LEFT;
    }

    public void changeStamina(double value) {
        currentStamina += value;
        currentStamina = Math.max(Math.min(currentStamina, maxStamina), 0);
        if (currentStamina == 0) spellState = 2;
    }

    public void kill() {
        currentHealth = 0;
    }

    // Core
    public void update() {
        updateBars();
        if (currentHealth <= 0) {
            if (entityState != AnimType.DEATH) {
                entityState = AnimType.DEATH;
                animIndex = animTick = 0;
                game.setDying(true);
            }
            else if (animIndex == animations[entityState.ordinal()].length-1 && animTick >= animSpeed-1) {
                game.setGameOver(true);
                Audio.getInstance().getAudioPlayer().stopSong();
                Audio.getInstance().getAudioPlayer().playSound(Sounds.GAME_OVER.ordinal());
            }
            else updateAnimation();
            return;
        }
        updateAttackBox();
        setAnimation();
        if (hit) {
            setSpellState(0);
            if (animIndex <= animations[entityState.ordinal()].length - 2)
                pushBack(pushDirection, levelData, 1.2, playerSpeed);
            updatePushOffset();
        }
        else if (canBlock) {
            pushOffsetDirection = Direction.DOWN;
            pushDirection = (flipSign == 1) ? Direction.LEFT : Direction.RIGHT;
            if (animIndex <= animations[entityState.ordinal()].length )
                pushBack(pushDirection, levelData, 0.1, playerSpeed);
            updatePushOffset();
        }
        else updatePosition();
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
        if (spellState == 1) {
            changeStamina(-0.20);
            enemyManager.checkEnemySpellHit();
            objectManager.checkObjectBreak(attackBox);
        }
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
                int effectXPos = (int)(hitBox.x-xHitBoxOffset-xLevelOffset)+(int)(newSign*22*Tiles.SCALE.getValue())+newFlip;
                int effectYPos = (int)(hitBox.y-yHitBoxOffset-yLevelOffset)-(int)(Tiles.SCALE.getValue());
                int effectWid = newSign*(effects[1][effectIndex].getWidth()+(int)(10*Tiles.SCALE.getValue()));
                int effectHei = effects[1][effectIndex].getHeight()+(int)(50*Tiles.SCALE.getValue());
                g.drawImage(effects[1][effectIndex], effectXPos, effectYPos, effectWid, effectHei, null);
            }
        }
        catch (Exception ignored) {}
    }

    private void renderStatusBar(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)(44*Tiles.SCALE.getValue()), (int)(29*Tiles.SCALE.getValue()), healthWidth, (int)(4*Tiles.SCALE.getValue()));
        g.setColor(Color.BLUE);
        g.fillRect((int)(44*Tiles.SCALE.getValue()), (int)(48*Tiles.SCALE.getValue()), staminaWidth, (int)(4*Tiles.SCALE.getValue()));
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderEffects(g, xLevelOffset, yLevelOffset);
        try {
            int playerXPos = (int)(hitBox.x-xHitBoxOffset-xLevelOffset+flipCoefficient);
            int playerYPos = (int)(hitBox.y-yHitBoxOffset-yLevelOffset)+(int)pushOffset;
            g.drawImage(animations[entityState.ordinal()][animIndex], playerXPos, playerYPos, flipSign*width, height, null);
        }
        catch (Exception ignored) {}
        g.drawImage(statusBar,(int)(10*Tiles.SCALE.getValue()), (int)(15*Tiles.SCALE.getValue()), (int)(192*Tiles.SCALE.getValue()), (int)(58*Tiles.SCALE.getValue()), null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
        attackBoxRenderer(g, xLevelOffset, yLevelOffset);
        renderStatusBar(g);
    }

    // Getters & Setters
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

    public void setPlayerStateSecondary(AttackState playerPlayerStateSecondary) {
        this.attackState = playerPlayerStateSecondary;
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
    }

    public boolean canBlock() {
        return canBlock;
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
