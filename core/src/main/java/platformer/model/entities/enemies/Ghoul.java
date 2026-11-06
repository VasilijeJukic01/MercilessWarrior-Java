package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

/**
 * Agile undead enemy that stalks the target, performs a blockable lunge attack, and can phase-shift once behind the player when wounded.
 */
public class Ghoul extends Enemy {

    private static final double STALK_DASH_CD = 1.6;
    private static final double LUNGE_SPEED_MULTIPLIER = 2.2;
    private static final double REVEAL_OFFSET_DIST = 2.2 * TILES_SIZE;

    private int attackOffset;
    private boolean blocker;
    private boolean hasHidden = false;

    private final double gravity = 0.025 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    public Ghoul(int xPos, int yPos) {
        super(xPos, yPos, GHOUL_WIDTH, GHOUL_HEIGHT, EnemyType.GHOUL, 25);
        initHitBox(GHOUL_HB_WID, GHOUL_HB_HEI);
        initAttackBox();
        super.cooldown = new double[3];
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, GHOUL_AB_WID, GHOUL_AB_HEI);
        this.attackOffset = (int) (20 * SCALE);
    }

    // Attack
    @Override
    public boolean hit(double damage, boolean enableRevive, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        if (enableRevive && !hasHidden && currentHealth < maxHealth * 0.75) {
            tryPhaseShift();
            if (entityState == Anim.HIDE) return false;
        }
        currentHealth -= damage;
        blocker = false;
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
            setEnemyAction(Anim.HIDE);
            Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_HIDE);
        }
    }

    private void checkDeath() {
        blocker = false;
        Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_DEATH);
        setEnemyAction(Anim.DEATH);
    }

    // Ghoul Core
    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE -> idleAction(target);
            case RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> attackAction(levelData, target);
            case REVEAL -> revealAction(levelData, target);
            case HIT -> pushBack(pushDirection, levelData, 1.2, enemySpeed);
            default -> {}
        }
    }

    private void idleAction(Entity target) {
        if (blocker) {
            faceEntity(target);
            if (cooldown[Cooldown.DASH.ordinal()] == 0) {
                blocker = false;
                setEnemyAction(Anim.ATTACK_1);
            }
            return;
        }
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            setEnemyAction(Anim.WALK);
        }
    }

    private void moveAction(int[][] levelData, Entity entity) {
        if (blocker || entityState == Anim.ATTACK_1) return;
        if (canSeeEntity(levelData, entity)) {
            directToEntity(entity);
            if (isEntityCloseForAttack(entity) && cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                cooldown[Cooldown.DASH.ordinal()] = STALK_DASH_CD;
                setEnemyAction(Anim.IDLE);
                blocker = true;
                return;
            }
        }
        double enemyXSpeed = (direction == Direction.LEFT) ? -enemySpeed : enemySpeed;
        if (canMoveHere(hitBox.x + enemyXSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            if (isFloor(hitBox, enemyXSpeed, levelData, direction)) {
                hitBox.x += enemyXSpeed;
                return;
            }
        }
        changeDirection();
    }

    /**
     * Executes a parryable, smooth-velocity lunge across frames 2..5.
     */
    private void attackAction(int[][] levelData, Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            blocker = false;
        }
        else if ((animIndex == 1 || animIndex == 2) && target instanceof Player player) {
            boolean block = player.checkAction(PlayerAction.BLOCK);
            boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
            if (block && !canBlock) {
                boolean facingGhoul = (player.getHitBox().x < hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1);
                if (facingGhoul) {
                    player.addAction(PlayerAction.CAN_BLOCK);
                    Audio.getInstance().getAudioPlayer().playBlockSound("Player");
                    attackCheck = true;
                }
            }
        }

        if (animIndex >= 2 && animIndex <= 5) {
            performSmoothLungeStep(levelData);
            if (animIndex >= 3 && !attackCheck && attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
                attackCheck = true;
            }
        }
    }

    private void performSmoothLungeStep(int[][] levelData) {
        double lungeSpeed = ((direction == Direction.LEFT) ? -enemySpeed : enemySpeed) * LUNGE_SPEED_MULTIPLIER;
        if (canMoveHere(hitBox.x + lungeSpeed, hitBox.y, hitBox.width, hitBox.height, levelData) && isFloor(hitBox, lungeSpeed, levelData, direction)) {
            hitBox.x += lungeSpeed;
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
                    dirSign * (1.5 * TILES_SIZE),
                    -dirSign * (1.5 * TILES_SIZE)
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

    /**
     * Finds the Y pixel coordinate of a valid floor below (candidateX, startY) that is at least 3 tiles wide.
     * Returns -1 if no valid 3-tile floor exists at that X coordinate.
     */
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

    private void faceEntity(Entity entity) {
        if (entity.getHitBox().x > hitBox.x) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (freezeTick > 0) {
            freezeTick--;
            return;
        }
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
    }

    public void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, target);
    }

    private void updateAttackBox() {
        this.attackBox.x = hitBox.x - attackOffset;
        this.attackBox.y = hitBox.y;
    }

    @Override
    protected void reset() {
        super.reset();
        this.blocker = false;
        this.hasHidden = false;
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