package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.Song;
import platformer.audio.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static platformer.constants.Constants.*;

public class SpearWoman extends Enemy {

    private final BossAttackHandler handler;

    private int attackOffset;
    private Anim prevAnim = Anim.IDLE;

    // Flags
    private boolean start, shooting, canFlash = true;
    private int multiShootFlag = 0;
    private int shootCount = 0, multiShootCount = 0;
    private int slashCount = 0, rapidSlashCount = 0;
    private int specialAttackIndex;

    private final List<Anim> actions =
            new ArrayList<>(List.of(Anim.ATTACK_1, Anim.ATTACK_2, Anim.ATTACK_3, Anim.SPELL_1, Anim.SPELL_2, Anim.SPELL_3, Anim.SPELL_4));

    // Physics
    private final double gravity = 0.1 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    public SpearWoman(int xPos, int yPos) {
        super(xPos, yPos, SW_WIDTH, SW_HEIGHT, EnemyType.SPEAR_WOMAN, 18);
        super.setDirection(Direction.LEFT);
        initHitBox(SW_HB_WID, SW_HB_HEI);
        initAttackBox();
        super.cooldown = new double[1];
        this.handler = new BossAttackHandler(this, actions);
    }

    // Init
    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, SW_AB_WID, SW_AB_HEI);
        this.attackOffset = (int)(33 * SCALE);
    }

    // Checkers
    private boolean isAirFreeze() {
        if (entityState == Anim.SPELL_3 && animIndex < 2) return true;
        return (entityState == Anim.SPELL_4);
    }

    // Hit
    @Override
    public void hit(double damage, boolean block, boolean hitSound) {
        currentHealth -= damage;
        slashCount = 0;
        if (currentHealth <= 0) setEnemyAction(Anim.DEATH);
        else {
            if (entityState != Anim.IDLE) prevAnim = entityState;
            setEnemyAction(Anim.HIT);
        }
    }

    @Override
    public void spellHit(double damage) {

    }

    // Core
    private void updateBehavior(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        switch (entityState) {
            case IDLE:
                idleAction(levelData, player, objectManager); break;
            case ATTACK_1:
            case ATTACK_2:
                classicAttackAction(levelData, player); break;
            case ATTACK_3:
                dashSlashAction(levelData, player); break;
            case SPELL_1:
                rapidSlashAction(levelData, player); break;
            case SPELL_2:
                lightningBallAction(objectManager); break;
            case SPELL_3:
                thunderSlamAction(player, spellManager); break;
            case SPELL_4:
                multiLightningBallAction(objectManager, spellManager);
                break;
            case DEATH:
                objectManager.activateBlockers(false);
                break;
            default: break;
        }
    }

    // Behavior
    private void idleAction(int[][] levelData, Player player, ObjectManager objectManager) {
        if (!start && isPlayerCloseForAttack(player)) {
            setStart(true);
            objectManager.activateBlockers(true);
            setEnemyAction(Anim.SPELL_3);
            return;
        }
        if (start && cooldown[Cooldown.ATTACK.ordinal()] == 0)
            handler.attack(levelData, player, prevAnim);
    }

    private void classicAttackAction(int[][] levelData, Player player) {
        if (animIndex == 0) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_2);
            attackCheck = false;
        }
        if (animIndex == 2) movingAttack(levelData, 30);
        if ((animIndex == 3 || animIndex == 2) && !attackCheck) checkPlayerHit(attackBox, player);
    }

    private void dashSlashAction(int[][] levelData, Player player) {
        if (animIndex == 0) attackCheck = false;
        if (slashCount == 0) dashSlash(levelData);
        else if (animIndex == 2) {
            movingAttack(levelData, 30);
            Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_1);
        }
        if (!attackCheck) checkPlayerHit(attackBox, player);
    }

    private void lightningBallAction(ObjectManager objectManager) {
        if (animIndex == 7 && !shooting) {
            objectManager.shootLightningBall(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.LIGHTNING_2);
            shooting = true;
        }
        else if (animIndex == 9 && shootCount < 3) {
            animIndex = 2;
            shootCount++;
            shooting = false;
        }
    }

    private void rapidSlashAction(int[][] levelData, Player player) {
        if (animIndex == 0) Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_3);
        else if (animIndex == 12 && rapidSlashCount < 1) {
            rapidSlashCount++;
            animIndex = 2;
        }
        movingAttack(levelData, 10);
        if (animIndex % 2 == 0) setDirection(Direction.LEFT);
        else setDirection(Direction.RIGHT);
        if (animIndex >= 2 && animIndex <= 11) checkPlayerHit(attackBox, player);
    }

    private void thunderSlamAction(Player player, SpellManager spellManager) {
        if (animIndex > 5 && animIndex < 16) checkPlayerHit(attackBox, player);
        if (animIndex == 11) Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_2);
        else if (animIndex == 13) spellManager.activateLightnings();
    }

    private void multiLightningBallAction(ObjectManager objectManager, SpellManager spellManager) {
        if (specialAttackIndex == 1) oscillationProjectiles(objectManager);
        else trackingProjectiles(spellManager, objectManager);
    }

    private void dashSlash(int[][] levelData) {
        if (animIndex == 0) attackCheck = false;
        if (animIndex == 2 && !attackCheck) {
            double xSpeed;

            if (direction == Direction.LEFT) xSpeed = -enemySpeed;
            else xSpeed = enemySpeed;

            if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * 80, hitBox.y, hitBox.width, hitBox.height, levelData))
                if (Utils.getInstance().isFloor(hitBox, xSpeed * 80, levelData, direction))
                    hitBox.x += xSpeed * 80;
        }
    }

    private void movingAttack(int[][] levelData, int speed) {
        double xSpeed;

        if (direction == Direction.LEFT) xSpeed = -enemySpeed;
        else xSpeed = enemySpeed;

        if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * speed, hitBox.y, hitBox.width, hitBox.height, levelData))
            if (Utils.getInstance().isFloor(hitBox, xSpeed * speed, levelData, direction))
                hitBox.x += xSpeed * speed;
    }

    // Update animation
    @Override
    protected void updateAnimation(BufferedImage[][] animations) {
        if (cooldown != null) {             // Pre-Attack cooldown check
            coolDownTickUpdate();
            if ((entityState != Anim.IDLE && entityState != Anim.HIT) && cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        }
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) finishAnimation();
        }
    }

    private void finishAnimation() {
        animIndex = 0;
        if (actions.contains(entityState) || entityState == Anim.HIT) {

            // Hit finish
            if (entityState == Anim.HIT) {
                shootCount = 0;
                setEnemyAction(Anim.IDLE);
                return;
            }
            if (shootCount >= 2) shootCount = 0;

            // Multi shoot finish
            if (entityState == Anim.SPELL_4) finishMultiShoot();

            // Attack finish
            else if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || (entityState == Anim.ATTACK_3 && slashCount != 0)) {
                // Slash count check
                if (slashCount == 2) slashCount = 0;
                // Change attacks
                else {
                    if (slashCount == 0) setEnemyAction(Anim.ATTACK_3);
                    else if (slashCount == 1) setEnemyAction(Anim.ATTACK_2);
                    slashCount++;
                    return;
                }
            }
            if (multiShootCount == 0 && entityState != Anim.IDLE) {
                Objects.requireNonNull(cooldown)[Cooldown.ATTACK.ordinal()] = 14;
                if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2) prevAnim = Anim.ATTACK_1;
                else prevAnim = entityState;
                animSpeed = 18;
                setEnemyAction(Anim.IDLE);
            }
        }
        else if (entityState == Anim.DEATH) {
            setStart(false);
            alive = false;
        }
        shooting = false;
    }

    private void finishMultiShoot() {
        multiShootCount++;
        if (multiShootCount == 16) {
            multiShootCount = 0;
            canFlash = true;
        }
    }


    // Projectiles
    private void oscillationProjectiles(ObjectManager objectManager) {
        if (animIndex == 1 && !shooting) {
            if (multiShootFlag == 1) objectManager.multiLightningBallShoot(this);
            if (multiShootFlag == 4) objectManager.multiLightningBallShoot2(this);
            multiShootFlag = (multiShootFlag + 1) % 5;
            shooting = true;
        }
    }

    private void trackingProjectiles(SpellManager spellManager, ObjectManager objectManager) {
        if (animIndex == 1 && !shooting) {
            if (multiShootFlag == 1) objectManager.followingLightningBallShoot(this);
            if (multiShootFlag == 0) {
                if (canFlash) spellManager.activateFlashes();
                canFlash = !canFlash;
            }
            multiShootFlag = (multiShootFlag + 1) % 5;
            shooting = true;
        }
    }

    // Update
    @Override
    public void update(BufferedImage[][] animations, int[][] levelData, Player player) {}

    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager, BossInterface bossInterface) {
        updateMove(levelData, player, spellManager, objectManager);
        updateAnimation(animations);
        updateAttackBox();
        if (!bossInterface.isActive() && start) bossInterface.setActive(true);
        else if (bossInterface.isActive() && !start) bossInterface.setActive(false);
    }

    public void updateMove(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData) && !isAirFreeze()) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, player, spellManager, objectManager);
    }

    private void updateAttackBox() {
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.ATTACK_3) {
            if (direction == Direction.RIGHT) attackBox.x = hitBox.x-hitBox.width + attackOffset * 1.3;
            else attackBox.x = hitBox.x - attackOffset * 1.3;
        }
        else attackBox.x = hitBox.x - attackOffset;
        attackBox.y = hitBox.y;
    }

    // Other
    public void prepareForClassicAttack() {
        rapidSlashCount = 0;
        changeAttackBox();
        animSpeed = 18;
    }

    public void changeAttackBox() {
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_3) attackBox.width = SW_AB_WID_REDUCE;
        if (direction == Direction.RIGHT) attackBox.x = SW_AB_WID / 2.0;
        else attackBox.x = xPos;
    }

    // Reset
    public void attackReset() {
        attackBox.width = SW_AB_WID;
        attackBox.x = xPos;
        shootCount = 0;
    }

    @Override
    public void reset() {
        super.reset();
        setStart(false);
        multiShootCount = shootCount  = 0;
        shooting = false;
        multiShootFlag = 0;
        slashCount = 0;
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

    // Setters
    public void setStart(boolean start) {
        this.start = start;
        if (start) Audio.getInstance().getAudioPlayer().playSong(Song.BOSS_1);
    }

    public void setAttackCooldown(double value) {
        cooldown[Cooldown.ATTACK.ordinal()] = value;
    }

    public void setSpecialAttackIndex(int specialAttackIndex) {
        this.specialAttackIndex = specialAttackIndex;
    }
}
