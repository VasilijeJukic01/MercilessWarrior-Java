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

// TODO:
public class Roric extends Enemy {

    private int attackOffset;
    private boolean start;
    private double xSpeed = 0;

    // Physics
    private final double upwardGravity = 0.016 * SCALE;
    private final double downwardGravity = 0.025 * SCALE;
    private final double collisionFallSpeed = 0.6 * SCALE;
    private final double jumpSpeed = -3.1 * SCALE;

    public Roric(int xPos, int yPos) {
        super(xPos, yPos, RORIC_WIDTH, RORIC_HEIGHT, EnemyType.RORIC, 16);
        super.setDirection(Direction.LEFT);
        initHitBox(RORIC_HB_WID, RORIC_HB_HEI);
        hitBox.x += RORIC_HB_OFFSET_X;
        hitBox.y += RORIC_HB_OFFSET_Y;
        initAttackBox();
        super.cooldown = new double[1];
        super.entityState = Anim.ATTACK_2;
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos - (15 * SCALE), yPos + (15 * SCALE), RORIC_AB_WID, RORIC_AB_HEI);
        this.attackOffset = (int)(33 * SCALE);
    }

    private void startFight(ObjectManager objectManager) {
        if (!start) setStart(true);
    }

    @Override
    public void update(BufferedImage[][] animations, int[][] levelData, Player player) {
        // Not used
    }

    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager, BossInterface bossInterface) {
        updateMove(levelData, player, spellManager, objectManager);
        updateAnimation(animations);
        if (!bossInterface.isActive() && start) bossInterface.setActive(true);
        else if (bossInterface.isActive() && !start) bossInterface.setActive(false);
    }

    private void updateMove(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData) && entityState != Anim.IDLE) {
            inAir = true;
        }
        if (inAir) updateInAir(levelData, 0, collisionFallSpeed);
        else updateBehavior(levelData, player, spellManager, objectManager);
    }

    /**
     * Overridden to use separate gravity values for ascending and descending.
     */
    protected void updateInAir(int[][] levelData, double gravity, double collisionFallSpeed) {
        // Jump
        if (airSpeed < 0) airSpeed += upwardGravity;
        // Fall
        else airSpeed += downwardGravity;

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
        if (cooldown[Cooldown.ATTACK.ordinal()] > 0) return;
        switch (entityState) {
            case IDLE:
                //idleAction(levelData, player, objectManager);
                break;
            case ATTACK_2: // Add this case
                handleArrowAttack(objectManager);
                break;
            case SPELL_1:
                beamAttack(spellManager);
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
            setEnemyAction(Anim.SPELL_3);
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
                if (airSpeed >= 0 && animIndex < 9) {
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
        if (entityState == Anim.JUMP_FALL || entityState == Anim.SPELL_3 || entityState == Anim.SPELL_1 || entityState == Anim.ATTACK_2) {
            entityState = Anim.IDLE;
        }
    }

    @Override
    public void reset() {
        super.reset();
        setDirection(Direction.LEFT);
        setEnemyAction(Anim.IDLE);
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
}
