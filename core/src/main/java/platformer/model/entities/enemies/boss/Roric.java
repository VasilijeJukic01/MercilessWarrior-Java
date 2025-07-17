package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Roric extends Enemy {

    private boolean start;
    private boolean preparingAerialAttack = false;
    private boolean isFloating = false;
    private double xSpeed = 0;

    // Physics
    private final double upwardGravity = 0.016 * SCALE;
    private final double downwardGravity = 0.025 * SCALE;
    private final double collisionFallSpeed = 0.6 * SCALE;
    private final double jumpSpeed = -3.0 * SCALE;

    private boolean isRepositioning = false;
    private double repositionTargetX = 0;
    private final double repositionSpeed = 3.5 * SCALE;
    private final double repositionDistance = 6.5 * TILES_SIZE;

    private boolean isVisible = true;
    private boolean isPerformingSkyfallBarrage = false;
    private int skyfallBeamCount = 0;
    private int skyfallBeamTimer = 0;
    private static final int SKYFALL_BEAM_COOLDOWN = 200;

    public Roric(int xPos, int yPos) {
        super(xPos, yPos, RORIC_WIDTH, RORIC_HEIGHT, EnemyType.RORIC, 16);
        super.setDirection(Direction.LEFT);
        initHitBox(RORIC_HB_WID, RORIC_HB_HEI);
        hitBox.x += RORIC_HB_OFFSET_X;
        hitBox.y += RORIC_HB_OFFSET_Y;
        initAttackBox();
        super.cooldown = new double[1];
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos - (15 * SCALE), yPos + (15 * SCALE), RORIC_AB_WID, RORIC_AB_HEI);
    }

    private void startFight(ObjectManager objectManager) {
        if (!start) setStart(true);
    }

    @Override
    public void update(BufferedImage[][] animations, int[][] levelData, Player player) {
        // Not used
    }

    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager, BossInterface bossInterface) {
        if (isPerformingSkyfallBarrage) {
            handleSkyfallBarrage(player, spellManager);
            return;
        }
        updateMove(levelData, player, spellManager, objectManager);
        updateAnimation(animations);
        updateAttackBox();
        if (!bossInterface.isActive() && start) bossInterface.setActive(true);
        else if (bossInterface.isActive() && !start) bossInterface.setActive(false);
    }

    private void updateMove(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        updateBehavior(levelData, player, spellManager, objectManager);
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData) && entityState != Anim.IDLE) {
            inAir = true;
        }
        if (inAir) updateInAir(levelData, 0, collisionFallSpeed);
    }

    private boolean isPlayerInBlindSpot(Player player) {
        double roricX = hitBox.getCenterX();
        double roricY = hitBox.getCenterY();
        double playerX = player.getHitBox().getCenterX();
        double playerY = player.getHitBox().getCenterY();
        if (playerY <= roricY) return false;

        double dx = Math.abs(playerX - roricX);
        double dy = playerY - roricY;

        return (dy > 0 && (dx / dy) < Math.tan(Math.toRadians(30)));
    }

    private void prepareToFire(Player player) {
        if (player.getHitBox().getCenterX() < this.hitBox.getCenterX()) setDirection(Direction.LEFT);
        else setDirection(Direction.RIGHT);
        setEnemyAction(Anim.SPELL_2);
        attackCheck = false;
    }

    /**
     * Overridden to use separate gravity values for ascending and descending.
     */
    @Override
    protected void updateInAir(int[][] levelData, double gravity, double collisionFallSpeed) {
        if (entityState == Anim.SPELL_2 && isFloating) return;
        if (!isFloating) {
            // Jump
            if (airSpeed < 0) airSpeed += upwardGravity;
            // Fall
            else airSpeed += downwardGravity;
        }

        if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x += xSpeed;
        }
        else xSpeed = 0;

        if (Utils.getInstance().canMoveHere(hitBox.x, hitBox.y + airSpeed, hitBox.width, hitBox.height, levelData)) {
            hitBox.y += airSpeed;
        }
        else {
            hitBox.y = Utils.getInstance().getYPosOnTheCeil(hitBox, airSpeed);
            if (airSpeed > 0) {
                airSpeed = 0;
                inAir = false;
                xSpeed = 0;
            }
            else airSpeed = collisionFallSpeed;
        }
    }

    private void updateBehavior(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (preparingAerialAttack) {
            handleAerialAttack(levelData, player, objectManager);
            return;
        }
        if (cooldown[Cooldown.ATTACK.ordinal()] > 0) return;
        switch (entityState) {
            case IDLE:
                idleAction(levelData, player, objectManager);
                break;
            case ATTACK_2:
                handleArrowAttack(objectManager);
                break;
            case SPELL_1:
                beamAttack(spellManager);
                break;
            case SPELL_2:
                // Handled by the aerial attack logic
                break;
            case SPELL_3:
                handleArrowRainAttack(spellManager, player);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean hit(double damage, boolean special, boolean hitSound) {
        return false;
    }

    @Override
    public void spellHit(double damage) {
        // No implementation (timed boss)
    }

    // Behavior
    private void idleAction(int[][] levelData, Player player, ObjectManager objectManager) {
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            startSkyfallBarrage();
        }
    }

    private void handleArrowAttack(ObjectManager objectManager) {
        if (animIndex == 0) attackCheck = false;
        if (animIndex == 9 && !attackCheck) {
            objectManager.shootRoricArrow(this);
            attackCheck = true;
        }
    }

    private void beamAttack(SpellManager spellManager) {
        if (animIndex == 9) {
            spellManager.activateRoricBeam(this);
        }
    }

    private void handleArrowRainAttack(SpellManager spellManager, Player player) {
        if (animIndex == 0) attackCheck = false;
        if (animIndex == 8 && !attackCheck) {
            spellManager.activateArrowRain(player);
            attackCheck = true;
        }
    }

    private void aerialArrowAttack(int[][] levelData) {
        preparingAerialAttack = true;
        jump(levelData);
    }

    private void startSkyfallBarrage() {
        this.isVisible = false;
        this.isPerformingSkyfallBarrage = true;
        this.skyfallBeamCount = 0;
        this.skyfallBeamTimer = 0;
        setAttackCooldown(8);
    }

    private void reappear() {
        this.isVisible = true;
        this.isPerformingSkyfallBarrage = false;
        this.hitBox.x = 12.5 * TILES_SIZE;
        this.setEnemyAction(Anim.IDLE);
    }

    private void handleAerialAttack(int[][] levelData, Player player, ObjectManager objectManager) {
        if (entityState == Anim.JUMP_FALL && airSpeed >= 0 && !isFloating) {
            isFloating = true;
            airSpeed = 0;
            xSpeed = 0;

            if (isPlayerInBlindSpot(player)) {
                isRepositioning = true;
                boolean playerIsToTheLeft = player.getHitBox().getCenterX() < hitBox.getCenterX();
                double idealDashTarget = playerIsToTheLeft ? hitBox.x + repositionDistance : hitBox.x - repositionDistance;
                double fallbackDashTarget = playerIsToTheLeft ? hitBox.x - repositionDistance : hitBox.x + repositionDistance;

                if (canDashTo(idealDashTarget, levelData)) {
                    repositionTargetX = idealDashTarget;
                }
                else if (canDashTo(fallbackDashTarget, levelData)) {
                    repositionTargetX = fallbackDashTarget;
                }
                else {
                    // If both dash options are blocked, don't reposition.
                    isRepositioning = false;
                }

                if (!isRepositioning) prepareToFire(player);
                else {
                    // Keeping the falling animation frame while repositioning
                    animIndex = 9;
                }
            }
            else {
                isRepositioning = false;
                prepareToFire(player);
            }
        }

        // Execute the dash
        if (isRepositioning) {
            boolean finishedReposition = false;
            if (hitBox.x < repositionTargetX) {
                hitBox.x += repositionSpeed;
                if (hitBox.x >= repositionTargetX) {
                    hitBox.x = repositionTargetX;
                    finishedReposition = true;
                }
            }
            else {
                hitBox.x -= repositionSpeed;
                if (hitBox.x <= repositionTargetX) {
                    hitBox.x = repositionTargetX;
                    finishedReposition = true;
                }
            }
            if (finishedReposition) {
                isRepositioning = false;
                prepareToFire(player);
            }
        }

        if (entityState == Anim.SPELL_2 && !isRepositioning) {
            if (animIndex == 6 && !attackCheck) {
                objectManager.shootRoricAngledArrow(this, player);
                attackCheck = true;
            }
        }

        if (!inAir && isFloating) {
            preparingAerialAttack = false;
            isFloating = false;
            isRepositioning = false;
            setEnemyAction(Anim.IDLE);
        }
    }

    private void jump(int[][] levelData) {
        if (!inAir) {
            inAir = true;
            airSpeed = jumpSpeed;
            setEnemyAction(Anim.JUMP_FALL);

            int currentTileX = (int) (hitBox.x / TILES_SIZE);
            int levelWidthInTiles = levelData.length;
            int spaceToLeft = currentTileX;
            int spaceToRight = levelWidthInTiles - currentTileX;

            double jumpHorizontalSpeed = 1.5 * SCALE;
            if (spaceToRight > spaceToLeft) {
                this.xSpeed = jumpHorizontalSpeed;
                setDirection(Direction.RIGHT);
            }
            else {
                this.xSpeed = -jumpHorizontalSpeed;
                setDirection(Direction.LEFT);
            }
        }
    }

    private void handleSkyfallBarrage(Player player, SpellManager spellManager) {
        skyfallBeamTimer++;
        if (skyfallBeamTimer >= SKYFALL_BEAM_COOLDOWN) {
            skyfallBeamTimer = 0;
            if (skyfallBeamCount < 4) {
                spellManager.spawnSkyBeamAt((int) player.getHitBox().getCenterX());
                skyfallBeamCount++;
            }
            else reappear();
        }
    }

    @Override
    protected void updateAnimation(BufferedImage[][] animations) {
        coolDownTickUpdate();

        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (entityState == Anim.JUMP_FALL) {
                if (airSpeed < 0 && animIndex > 8) {
                    animIndex = 8;
                }
                if (airSpeed >= 0 && animIndex < 9 && !isFloating) {
                    animIndex = 9;
                }
            }
            if (animIndex >= animations[entityState.ordinal()].length) {
                finishAnimation();
            }
        }
    }

    private void finishAnimation() {
        animIndex = 0;
        if (entityState == Anim.SPELL_2 && preparingAerialAttack) {
            isFloating = false;
            preparingAerialAttack = false;
            setEnemyAction(Anim.JUMP_FALL);
            animIndex = 9;
            return;
        }
        if (entityState == Anim.JUMP_FALL || entityState == Anim.SPELL_3 || entityState == Anim.SPELL_1 || entityState == Anim.ATTACK_2) {
            entityState = Anim.IDLE;
        }
    }

    private void updateAttackBox() {
        attackBox.x = hitBox.x - (40 * SCALE);
        attackBox.y = hitBox.y;
    }

    @Override
    public void reset() {
        super.reset();
        setDirection(Direction.LEFT);
        setEnemyAction(Anim.IDLE);
        preparingAerialAttack = false;
        isFloating = false;
        isRepositioning = false;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }

    public void setStart(boolean start) {
        this.start = start;
        if (start) Audio.getInstance().getAudioPlayer().playSong(Song.BOSS_1);
    }

    public void setAttackCooldown(double value) {
        cooldown[Cooldown.ATTACK.ordinal()] = value;
    }

    private boolean canDashTo(double targetX, int[][] levelData) {
        return Utils.getInstance().canMoveHere(targetX, hitBox.y, hitBox.width, hitBox.height, levelData);
    }

    public boolean isVisible() {
        return isVisible;
    }
}
