package platformer.model.entities.enemies.types;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.*;

/**
 * Agile undead enemy that stalks the target from mid-range, maintains pounce distance by backstepping if crowded,
 * performs a blockable lunge attack, and can phase-shift once behind the player when wounded.
 */
public class Ghoul extends Enemy {

    private static final double STALK_DASH_CD = 1.6;
    private static final double LUNGE_SPEED_MULTIPLIER = 1.85;
    private static final double MIN_POUNCE_DIST = 1.35 * TILES_SIZE;
    private static final double REVEAL_OFFSET_DIST = 2.2 * TILES_SIZE;

    private boolean blocker;
    private boolean hasHidden = false;

    public Ghoul(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.GHOUL);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
    }

    @Override
    public boolean hit(double damage, boolean enableRevive, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        if (enableRevive && !hasHidden && currentHealth < maxHealth * 0.75) {
            tryPhaseShift();
            if (entityState == Anim.HIDE) return false;
        }
        currentHealth -= damage;
        blocker = false;
        releaseCombatToken();
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) checkDeath();
        else setEnemyAction(Anim.HIT);
        return true;
    }

    @Override
    public void spellHit(double damage) {
        if (entityState == Anim.DEATH || !alive || entityState == Anim.HIDE) return;
        currentHealth -= damage;
        blocker = false;
        releaseCombatToken();
        enemySpeed = 0;
        if (currentHealth <= 0) checkDeath();
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
    }

    private void tryPhaseShift() {
        if (rand.nextDouble() < 0.25) {
            hasHidden = true;
            blocker = false;
            releaseCombatToken();
            setEnemyAction(Anim.HIDE);
            Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_HIDE);
        }
    }

    private void checkDeath() {
        blocker = false;
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    // Core
    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> attackAction(levelData, target);
            case REVEAL -> revealAction(levelData, target);
            case HIT -> pushBack(pushDirection, levelData, 1.2, enemySpeed);
            default -> {}
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (blocker) {
            faceEntity(target);
            double dx = target.getHitBox().getCenterX() - hitBox.getCenterX();
            double absDist = Math.abs(dx);

            if (absDist < MIN_POUNCE_DIST) {
                double awayDirSign = (dx >= 0) ? -1.0 : 1.0;
                Direction awayDir = (awayDirSign < 0) ? Direction.LEFT : Direction.RIGHT;
                double step = awayDirSign * definition.getScaledWalkSpeed() * 1.15;
                if (canMoveHere(hitBox.x + step, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, step, levelData, awayDir)) {
                    hitBox.x += step;
                    setLocomotionState(Anim.WALK);
                }
                else setLocomotionState(Anim.IDLE);
            }
            else setLocomotionState(Anim.IDLE);

            if (cooldown[Cooldown.DASH.ordinal()] == 0) {
                blocker = false;
                setEnemyAction(Anim.ATTACK_1);
            }
            return;
        }

        if (canSeeEntity(levelData, target)) {
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            if (dist >= MIN_POUNCE_DIST && isEntityCloseForAttack(target) && cooldown[Cooldown.ATTACK.ordinal()] == 0 && requestMeleeToken()) {
                faceEntity(target);
                cooldown[Cooldown.DASH.ordinal()] = STALK_DASH_CD;
                setEnemyAction(Anim.IDLE);
                blocker = true;
                return;
            }
            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }

        patrolStep(levelData);
    }

    private void attackAction(int[][] levelData, Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            blocker = false;
        }
        else if ((animIndex == 1 || animIndex == 2) && checkPlayerParry(target)) {
            attackCheck = true;
        }

        if (animIndex >= 2 && animIndex <= 5) {
            stepForwardClamped(levelData, target, definition.getScaledRunSpeed() * LUNGE_SPEED_MULTIPLIER);
            if (animIndex >= 3 && !attackCheck && attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
                attackCheck = true;
            }
        }
    }

    /**
     * Repositions the Ghoul behind or beside the target on valid ground rather than directly inside the target's hitbox.
     */
    private void revealAction(int[][] levelData, Entity target) {
        if (animIndex == 0 && animTick == 0) {
            double targetCenterX = target.getHitBox().getCenterX();
            double searchStartY = target.getHitBox().y;
            double dirSign = (target.getFlipSign() == 1) ? -1.0 : 1.0;

            double[] candidateOffsets = {
                    dirSign * REVEAL_OFFSET_DIST,
                    -dirSign * REVEAL_OFFSET_DIST,
                    dirSign * (REVEAL_OFFSET_DIST * 1.4),
                    -dirSign * (REVEAL_OFFSET_DIST * 1.4),
                    dirSign * (1.75 * TILES_SIZE),
                    -dirSign * (1.75 * TILES_SIZE)
            };

            for (double offset : candidateOffsets) {
                double candidateX = targetCenterX + offset - (hitBox.width / 2.0);
                double groundY = findValidThreeTileGroundY(candidateX, searchStartY, levelData);
                if (groundY != -1) {
                    hitBox.x = candidateX;
                    hitBox.y = groundY - hitBox.height - 1;
                    inAir = false;
                    airSpeed = 0;
                    break;
                }
            }
            faceEntity(target);
        }
        else if (animIndex == 5 && animTick == 0) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_REVEAL);
        }
    }

    private double findValidThreeTileGroundY(double candidateX, double startY, int[][] levelData) {
        int tileX = (int) ((candidateX + hitBox.width / 2.0) / TILES_SIZE);
        if (tileX < 0 || tileX >= levelData.length) return -1;

        int startTileY = Math.max(1, (int) (startY / TILES_SIZE));
        int maxTileY = Math.min(levelData[0].length - 1, startTileY + 8);
        for (int floorTileY = startTileY; floorTileY <= maxTileY; floorTileY++) {
            if (isSolidFloorTile(tileX, floorTileY, levelData)) {
                double spawnY = (floorTileY * TILES_SIZE) - hitBox.height - 1;
                if (isValidThreeTilePlatform(candidateX, spawnY, floorTileY, levelData)) {
                    return floorTileY * TILES_SIZE;
                }
            }
        }
        return -1;
    }

    private boolean isValidThreeTilePlatform(double spawnX, double spawnY, int floorTileY, int[][] levelData) {
        if (!canMoveHere(spawnX, spawnY, hitBox.width, hitBox.height, levelData)) return false;

        int leftFootTile = (int) (spawnX / TILES_SIZE);
        int rightFootTile = (int) ((spawnX + hitBox.width) / TILES_SIZE);
        if (!isSolidFloorTile(leftFootTile, floorTileY, levelData) || !isSolidFloorTile(rightFootTile, floorTileY, levelData)) {
            return false;
        }

        int contiguousTiles = 1;
        for (int tx = leftFootTile - 1; tx >= 0 && isSolidFloorTile(tx, floorTileY, levelData); tx--) {
            contiguousTiles++;
        }
        for (int tx = leftFootTile + 1; tx < levelData.length && isSolidFloorTile(tx, floorTileY, levelData); tx++) {
            contiguousTiles++;
        }
        return contiguousTiles >= 3;
    }

    private boolean isSolidFloorTile(int tileX, int floorTileY, int[][] levelData) {
        if (tileX < 0 || tileX >= levelData.length || floorTileY <= 0 || floorTileY >= levelData[0].length) {
            return false;
        }
        return isTileSolid(tileX, floorTileY, levelData) && !isTileSolid(tileX, floorTileY - 1, levelData);
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (handleStunTick(levelData)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateForwardAttackBox(definition.getAttackBoxWidth());
    }

    public void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }

    @Override
    protected void reset() {
        super.reset();
        this.blocker = false;
        this.hasHidden = false;
    }
}