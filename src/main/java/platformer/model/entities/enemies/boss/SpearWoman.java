package platformer.model.entities.enemies.boss;

import platformer.animation.AnimType;
import platformer.audio.Audio;
import platformer.model.Tiles;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemySize;
import platformer.model.entities.enemies.EnemyType;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SpearWoman extends Enemy {

    private int attackOffset;
    private final List<AnimType> attackAnimations =
            new ArrayList<>(java.util.List.of(AnimType.ATTACK_1, AnimType.ATTACK_2, AnimType.ATTACK_3, AnimType.SPELL_1, AnimType.SPELL_2, AnimType.SPELL_3, AnimType.BLOCK));

    // Physics
    private double airSpeed = 0;
    private final double gravity = 0.1 * Tiles.SCALE.getValue();
    private final double collisionFallSpeed = 0.5 * Tiles.SCALE.getValue();


    public SpearWoman(int xPos, int yPos) {
        super(xPos, yPos, EnemySize.SW_WIDTH.getValue(), EnemySize.SW_HEIGHT.getValue(), EnemyType.SPEAR_WOMAN, 15);
        super.enemySpeed = 0.4*Tiles.SCALE.getValue();
        int w = (int)(25 * Tiles.SCALE.getValue());
        int h =  (int)(50 * Tiles.SCALE.getValue());
        initHitBox(w, h);
        initAttackBox();
        super.cooldown = new double[1];
    }

    private void initAttackBox() {
        int w = (int)(96 * Tiles.SCALE.getValue());
        int h = (int)(54 * Tiles.SCALE.getValue());
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, w, h);
        this.attackOffset = (int)(33*Tiles.SCALE.getValue());
    }

    private boolean isAirFreeze() {
        if (entityState == AnimType.SPELL_3 && animIndex < 2) return true;
        return false;
    }

    public void updateMove(int[][] levelData, Player player) {
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData) && !isAirFreeze()) inAir = true;
        if (inAir) {
            if (Utils.getInstance().canMoveHere(hitBox.x, hitBox.y + airSpeed, hitBox.width, hitBox.height, levelData)) {
                hitBox.y += airSpeed;
                airSpeed += gravity;
            }
            else {
                hitBox.y = Utils.getInstance().getYPosOnTheCeil(hitBox, airSpeed);
                if (airSpeed > 0) {
                    airSpeed = 0;
                    inAir = false;
                }
                else {
                    airSpeed = collisionFallSpeed;
                }
            }
        }
        else updateBehavior(levelData, player);
    }

    // Attack
    public void hit(double damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            setEnemyAction(AnimType.DEATH);
        }
        else {
            // TODO: Change action after
            setEnemyAction(AnimType.HIT);
        }
    }

    public void spellHit(double damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            setEnemyAction(AnimType.DEATH);
        }
        else {
            if (entityState == AnimType.HIT) setEnemyActionNoReset(AnimType.HIT);
            else setEnemyAction(AnimType.HIT);
        }
    }

    // Core
    private void updateBehavior(int[][] levelData, Player player) {
        switch (entityState) {
            case IDLE:
                if (canSeePlayer(levelData, player)) directToPlayer(player);
                break;
            case RUN:
                if (canSeePlayer(levelData, player)) directToPlayer(player);
                if (canSeePlayer(levelData, player) && isPlayerCloseForAttack(player) && cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                    setEnemyAction(AnimType.ATTACK_1);
                    animSpeed = 20;
                }
                double enemyXSpeed = 0;

                if (direction == Direction.LEFT) enemyXSpeed = -enemySpeed;
                else if (direction == Direction.RIGHT) enemyXSpeed = enemySpeed;

                if (Utils.getInstance().canMoveHere(hitBox.x + enemyXSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                    if (Utils.getInstance().isFloor(hitBox, enemyXSpeed, levelData, direction)) {
                        hitBox.x += enemyXSpeed;
                        return;
                    }
                }

                changeDirection();
                break;
            case ATTACK_1:
                if (animIndex == 0) {
                    player.setCanBlock(false);
                    attackCheck = false;
                }
                if ((animIndex == 1 || animIndex == 2) && player.isBlock()) {
                    if ((player.getHitBox().x < this.hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > this.hitBox.x && player.getFlipSign() == -1)) {
                        player.setCanBlock(true);
                        Audio.getInstance().getAudioPlayer().playBlockSound("Player");
                    }
                }
                if (animIndex == 3 && !attackCheck) checkPlayerHit(attackBox, player);
                player.setBlock(false);
            default: break;
        }
    }

    private void updateAttackBox() {
        this.attackBox.x = hitBox.x - attackOffset;
        this.attackBox.y = hitBox.y;
    }

    private void updateBoss(BufferedImage[][] animations) {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) {
                animIndex = 0;
                if (attackAnimations.contains(entityState) || entityState == AnimType.HIT) {
                    entityState = attackAnimations.get(rand.nextInt(7));
                    if (entityState == AnimType.SPELL_3) {
                        hitBox.x = 13*Tiles.TILES_SIZE.getValue();
                        hitBox.y = 4*Tiles.TILES_SIZE.getValue();
                    }
                }
                else if (entityState == AnimType.DEATH) alive = false;
            }
        }
        if (cooldown != null) coolDownTickUpdate();
    }

    public void update(BufferedImage[][] animations, int[][] levelData, Player player) {
        updateMove(levelData, player);
        updateBoss(animations);
        updateAttackBox();
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
