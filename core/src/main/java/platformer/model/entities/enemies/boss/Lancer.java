package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.event.EventBus;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.event.events.lancer.LancerAuraEvent;
import platformer.event.events.lancer.LancerDashSlashEvent;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.boss.lancer.LancerAttackHandler;
import platformer.model.entities.enemies.boss.lancer.LancerAudioHandler;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.projectiles.ProjectileFactory;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

public class Lancer extends Enemy {

    private final LancerAttackHandler handler;
    private final LancerAudioHandler audioHandler;

    private int attackOffset;
    private boolean start = false;
    private boolean shooting = false;
    private boolean canFlash = true;
    private boolean slamTriggered = false;

    private int multiShootFlag = 0;
    private int shootCount = 0;
    private int multiShootCount = 0;
    private int slashCount = 0;
    private int rapidSlashCount = 0;
    private int specialAttackIndex = 0;

    // Physics
    private final double gravity = 0.1 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    public Lancer(int xPos, int yPos) {
        super(xPos, yPos, LANCER_WIDTH, LANCER_HEIGHT, EnemyType.LANCER, 18);
        super.setDirection(Direction.LEFT);
        initHitBox(LANCER_HB_WID, LANCER_HB_HEI);
        initAttackBox();
        super.cooldown = new double[1];
        this.audioHandler = new LancerAudioHandler();
        this.handler = new LancerAttackHandler(this, audioHandler);
    }

    // Init
    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, LANCER_AB_WID, LANCER_AB_HEI);
        this.attackOffset = (int) (33 * SCALE);
    }

    // Checkers
    private boolean isAirFreeze() {
        if (entityState == Anim.SPELL_3 && animIndex < 5) return true;
        return entityState == Anim.SPELL_4;
    }

    public void engageFight(ObjectManager objectManager) {
        if (start || !alive) return;
        setStart(true);
        objectManager.activateBlockers(true);
        EventBus.getInstance().publish(new ScreenShakeEvent(25, 10.0));
        handler.startThunderSlam();
    }

    // Hit
    @Override
    public boolean hit(double damage, boolean block, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        slashCount = 0;
        audioHandler.playHitGrunt();
        if (currentHealth <= 0) {
            EventBus.getInstance().publish(new LancerAuraEvent(this, false));
            setEnemyAction(Anim.DEATH);
        }
        else setEnemyAction(Anim.HIT);
        return true;
    }

    @Override
    public void spellHit(double damage) {
        hit(damage, false, false);
    }

    // Core
    private void updateBehavior(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (!start) {
            if (player.getHitBox().x > 7.5 * TILES_SIZE && isEntityInSight(player)) {
                engageFight(objectManager);
            }
            return;
        }
        if (cooldown[Cooldown.ATTACK.ordinal()] != 0) return;

        switch (entityState) {
            case IDLE -> idleAction(levelData, player);
            case ATTACK_1, ATTACK_2 -> classicAttackAction(levelData, player);
            case ATTACK_3 -> dashSlashAction(levelData, player);
            case SPELL_1 -> rapidSlashAction(levelData, player);
            case SPELL_2 -> lightningBallAction();
            case SPELL_3 -> thunderSlamAction(player, spellManager);
            case SPELL_4 -> multiLightningBallAction(spellManager);
            case DEATH -> objectManager.activateBlockers(false);
            default -> {}
        }
    }

    private void idleAction(int[][] levelData, Player player) {
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) handler.attack(levelData, player);
    }

    private void classicAttackAction(int[][] levelData, Player player) {
        if (animIndex == 0 && animTick == 0) {
            audioHandler.playBattleCry();
            attackCheck = false;
        }
        if (animIndex == 2) {
            if (animTick == 0) {
                audioHandler.playClassicSwing();
                EventBus.getInstance().publish(new ScreenShakeEvent(7, 4.5));
                EventBus.getInstance().publish(new LancerAuraEvent(this, LancerAuraEvent.EffectType.BLADE_SWING));
            }
            movingAttack(levelData, 30);
        }
        if ((animIndex == 3 || animIndex == 2) && !attackCheck) checkEntityHit(attackBox, player);
    }

    private void dashSlashAction(int[][] levelData, Player player) {
        if (animIndex == 0) attackCheck = false;
        if (slashCount == 0) dashSlash(levelData);
        else if (animIndex == 2) {
            if (animTick == 0) {
                audioHandler.playFollowUpSwing();
                EventBus.getInstance().publish(new ScreenShakeEvent(7, 4.5));
                EventBus.getInstance().publish(new LancerAuraEvent(this, LancerAuraEvent.EffectType.BLADE_SWING));
            }
            movingAttack(levelData, 30);
        }
        if (!attackCheck) checkEntityHit(attackBox, player);
    }

    private void dashSlash(int[][] levelData) {
        if (animIndex == 0) attackCheck = false;
        if (animIndex == 2 && !attackCheck) {
            if (animTick == 0) audioHandler.playDashSlash();
            Point2D.Double startPos = new Point2D.Double(hitBox.getCenterX(), hitBox.y);
            double xSpeed = (direction == Direction.LEFT) ? -enemySpeed : enemySpeed;
            if (canMoveHere(hitBox.x + xSpeed * 80, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                if (isFloor(hitBox, xSpeed * 80, levelData, direction)) {
                    hitBox.x += xSpeed * 80;
                }
            }
            Point2D.Double endPos = new Point2D.Double(hitBox.getCenterX(), hitBox.y);
            EventBus.getInstance().publish(new LancerDashSlashEvent(startPos, endPos, hitBox.height));
        }
    }

    private void movingAttack(int[][] levelData, int speed) {
        double xSpeed = (direction == Direction.LEFT) ? -enemySpeed : enemySpeed;
        if (canMoveHere(hitBox.x + xSpeed * speed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            if (isFloor(hitBox, xSpeed * speed, levelData, direction)) {
                hitBox.x += xSpeed * speed;
            }
        }
    }

    private void rapidSlashAction(int[][] levelData, Player player) {
        if (animIndex == 0 && animTick == 0) {
            facePlayer(player);
            audioHandler.playRapidFlurryStart();
        }
        else if (animIndex == 12 && rapidSlashCount < 1) {
            rapidSlashCount++;
            animIndex = 2;
            facePlayer(player);
            audioHandler.playBattleCry();
        }
        if (animIndex >= 2 && animIndex <= 11 && animTick == 0 && animIndex % 2 == 0) {
            audioHandler.playRapidCut(animIndex);
            EventBus.getInstance().publish(new LancerAuraEvent(this, LancerAuraEvent.EffectType.RAPID_THRUST));
            if (animIndex % 4 == 0) {
                EventBus.getInstance().publish(new ScreenShakeEvent(6, 4.0));
            }
        }
        movingAttack(levelData, 15);
        if (animIndex >= 2 && animIndex <= 11) checkEntityHit(attackBox, player);
    }

    private void lightningBallAction() {
        if (animIndex == 2 && animTick == 0) audioHandler.playCharge();
        if (animIndex == 7 && !shooting) {
            ProjectileFactory.createLightningBall(this, shootCount);
            audioHandler.playOrbCast();
            EventBus.getInstance().publish(new ScreenShakeEvent(9, 6.0));
            EventBus.getInstance().publish(new LancerAuraEvent(this, LancerAuraEvent.EffectType.ORB_CAST));
            shooting = true;
        }
        else if (animIndex == 9 && shootCount < 2) {
            animIndex = 2;
            shootCount++;
            shooting = false;
        }
    }

    private void thunderSlamAction(Player player, SpellManager spellManager) {
        if (animIndex == 0) {
            slamTriggered = false;
            attackCheck = false;
        }
        if (animIndex == 1 && animTick == 0) audioHandler.playCharge();
        if (animIndex >= 12 && animIndex <= 15 && !attackCheck) {
            checkEntityHit(attackBox, player);
        }
        if (animIndex == 11 && animTick == 0) audioHandler.playBattleCry();
        else if (animIndex >= 13 && !slamTriggered) {
            slamTriggered = true;
            audioHandler.playThunderSlamImpact();
            spellManager.activateLightnings();
            EventBus.getInstance().publish(new LancerAuraEvent(this, LancerAuraEvent.EffectType.THUNDER_SLAM_IMPACT));
            EventBus.getInstance().publish(new ScreenShakeEvent(30, 14.0));
        }
    }

    private void multiLightningBallAction(SpellManager spellManager) {
        if (multiShootCount == 0 && multiShootFlag == 0 && animIndex == 0 && animTick == 0) {
            audioHandler.playCharge();
            EventBus.getInstance().publish(new LancerAuraEvent(this, true));
        }
        if (specialAttackIndex == 1) oscillationProjectiles();
        else trackingProjectiles(spellManager);
    }

    @Override
    protected void updateAnimation() {
        if (cooldown != null) {
            coolDownTickUpdate();
            if ((entityState != Anim.IDLE && entityState != Anim.HIT) && cooldown[Cooldown.ATTACK.ordinal()] != 0) {
                return;
            }
        }
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= SpriteManager.getInstance().getAnimFrames(getEnemyType(), entityState)) {
                finishAnimation();
            }
        }
    }

    private void finishAnimation() {
        animIndex = 0;
        shooting = false;

        if (entityState == Anim.DEATH) {
            setStart(false);
            alive = false;
            return;
        }

        if (entityState == Anim.HIT) {
            shootCount = 0;
            setEnemyAction(Anim.IDLE);
            return;
        }

        if (shootCount >= 2) shootCount = 0;

        if (entityState == Anim.SPELL_4) {
            finishMultiShoot();
        }
        else if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || (entityState == Anim.ATTACK_3 && slashCount != 0)) {
            if (slashCount == 2) slashCount = 0;
            else {
                if (slashCount == 0) setEnemyAction(Anim.ATTACK_3);
                else if (slashCount == 1) setEnemyAction(Anim.ATTACK_2);
                slashCount++;
                return;
            }
        }

        if (multiShootCount == 0 && entityState != Anim.IDLE) {
            cooldown[Cooldown.ATTACK.ordinal()] = 14;
            animSpeed = 18;
            setEnemyAction(Anim.IDLE);
        }
    }

    private void finishMultiShoot() {
        multiShootCount++;
        if (multiShootCount >= 12) {
            multiShootCount = 0;
            multiShootFlag = 0;
            canFlash = true;
            EventBus.getInstance().publish(new LancerAuraEvent(this, false));
        }
    }


    // Projectiles
    private void oscillationProjectiles() {
        if (animIndex == 1 && !shooting) {
            if (multiShootFlag == 1) {
                ProjectileFactory.createMultiLightningBallVariation1(this);
                audioHandler.playOrbVolley();
            }
            else if (multiShootFlag == 5) {
                ProjectileFactory.createMultiLightningBallVariation2(this);
                audioHandler.playOrbVolley();
            }
            multiShootFlag = (multiShootFlag + 1) % 8;
            shooting = true;
        }
    }

    private void trackingProjectiles(SpellManager spellManager) {
        if (animIndex == 1 && !shooting) {
            if (multiShootFlag == 2) {
                ProjectileFactory.createTrackingLightningBall(this);
                audioHandler.playOrbVolley();
            }
            if (multiShootFlag == 0) {
                if (canFlash) spellManager.activateFlashes();
                canFlash = !canFlash;
            }
            multiShootFlag = (multiShootFlag + 1) % 6;
            shooting = true;
        }
    }

    public void facePlayer(Player player) {
        setDirection(player.getHitBox().getCenterX() < hitBox.getCenterX() ? Direction.LEFT : Direction.RIGHT);
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {}

    public void update(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager, BossInterface bossInterface) {
        audioHandler.update();
        updateMove(levelData, player, spellManager, objectManager);
        updateAnimation();
        updateAttackBox();
        if (!bossInterface.isActive() && start) bossInterface.injectBoss(this);
        else if (bossInterface.isActive() && !start) bossInterface.reset();
    }

    public void updateMove(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (!isEntityOnFloor(hitBox, levelData) && !isAirFreeze()) inAir = true;
        if (inAir) {
            updateInAir(levelData, gravity, collisionFallSpeed);
            if (entityState == Anim.SPELL_3) {
                thunderSlamAction(player, spellManager);
            }
        }
        else updateBehavior(levelData, player, spellManager, objectManager);
    }

    private void updateAttackBox() {
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.ATTACK_3) {
            if (direction == Direction.RIGHT) attackBox.x = hitBox.x - hitBox.width + attackOffset * 1.3;
            else attackBox.x = hitBox.x - attackOffset * 1.3;
        }
        else if (entityState == Anim.SPELL_1) {
            attackBox.width = LANCER_AB_WID_REDUCE;
            if (direction == Direction.RIGHT) attackBox.x = hitBox.x + hitBox.width * 0.4;
            else attackBox.x = hitBox.x - attackBox.width + hitBox.width * 0.6;
        }
        else {
            attackBox.width = LANCER_AB_WID;
            attackBox.x = hitBox.x - attackOffset;
        }
        attackBox.y = hitBox.y;
    }

    // Other
    public void prepareForClassicAttack() {
        rapidSlashCount = 0;
        changeAttackBox();
        animSpeed = (entityState == Anim.SPELL_1) ? 12 : 18;
    }

    public void changeAttackBox() {
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_3) attackBox.width = LANCER_AB_WID_REDUCE;
        if (direction == Direction.RIGHT) attackBox.x = LANCER_AB_WID / 2.0;
        else attackBox.x = xPos;
    }

    // Reset
    public void attackReset() {
        attackBox.width = LANCER_AB_WID;
        attackBox.x = xPos;
        shootCount = 0;
        rapidSlashCount = 0;
        slamTriggered = false;
    }

    @Override
    public void reset() {
        super.reset();
        if (start) setStart(false);
        audioHandler.reset();
        EventBus.getInstance().publish(new LancerAuraEvent(this, false));
        slamTriggered = false;
        multiShootCount = shootCount = 0;
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
        audioHandler.playBossTheme(start);
    }

    public void setAttackCooldown(double value) {
        cooldown[Cooldown.ATTACK.ordinal()] = value;
    }

    public void setSpecialAttackIndex(int specialAttackIndex) {
        this.specialAttackIndex = specialAttackIndex;
    }
}
