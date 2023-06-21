package platformer.model.entities.enemies.boss;

import platformer.animation.AnimType;
import platformer.model.Tiles;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemySize;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.objects.ObjectManager;
import platformer.model.spells.SpellManager;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SpearWoman extends Enemy {

    private AnimType prevAnim = AnimType.IDLE;

    private boolean shooting;
    private int shootCount = 0;
    private int multiShootCount = 0;
    private int multiShootFlag = 0;
    private int slashCount = 0;

    private int attackOffset;
    private final List<AnimType> spellAnimations =
            new ArrayList<>(List.of(AnimType.ATTACK_3, AnimType.SPELL_2, AnimType.SPELL_3, AnimType.SPELL_4, AnimType.ATTACK_1, AnimType.ATTACK_2, AnimType.SPELL_1));

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
        if (entityState == AnimType.SPELL_4) return true;
        return false;
    }

    public void updateMove(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
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
        else updateBehavior(levelData, player, spellManager, objectManager);
    }

    // Attack
    public void hit(double damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            setEnemyAction(AnimType.DEATH);
        }
        else {
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

    private void teleport(int[][] levelData, Player player, int tiles) {
        double playerX = player.getHitBox().x;
        int k = rand.nextInt(2);
        if (k == 0 && Utils.getInstance().canMoveHere(playerX+tiles*Tiles.TILES_SIZE.getValue(), hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX+tiles*Tiles.TILES_SIZE.getValue();
        }
        else if (k == 0 && Utils.getInstance().canMoveHere(playerX-tiles*Tiles.TILES_SIZE.getValue(), hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX-tiles*Tiles.TILES_SIZE.getValue();
        }
        if (k == 1 && Utils.getInstance().canMoveHere(playerX-tiles*Tiles.TILES_SIZE.getValue(), hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX-tiles*Tiles.TILES_SIZE.getValue();
        }
        else if (k == 1 && Utils.getInstance().canMoveHere(playerX+tiles*Tiles.TILES_SIZE.getValue(), hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX+tiles*Tiles.TILES_SIZE.getValue();
        }
        if (playerX < hitBox.x) setDirection(Direction.LEFT);
        else setDirection(Direction.RIGHT);
    }

    private void dashSlash(int[][] levelData, Player player) {
        if (animIndex == 0) attackCheck = false;
        if (animIndex == 2 && !attackCheck) {
            //checkPlayerHit(attackBox, player);
            double xSpeed;

            if (direction == Direction.LEFT) xSpeed = -enemySpeed;
            else xSpeed = enemySpeed;

            if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * 60, hitBox.y, hitBox.width, hitBox.height, levelData))
                if (Utils.getInstance().isFloor(hitBox, xSpeed * 60, levelData, direction)) {
                    hitBox.x += xSpeed * 60;
                }
        }
    }

    private void movingAttack(int[][] levelData, Player player, int speed) {
        double xSpeed;

        if (direction == Direction.LEFT) xSpeed = -enemySpeed;
        else xSpeed = enemySpeed;

        if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * speed, hitBox.y, hitBox.width, hitBox.height, levelData))
            if (Utils.getInstance().isFloor(hitBox, xSpeed * speed, levelData, direction)) {
                hitBox.x += xSpeed * speed;
            }
    }

    // Core
    private void attack(int[][] levelData, Player player) {
        AnimType next;
        do {
            next = spellAnimations.get(rand.nextInt(7));
        } while (next == prevAnim);
        setEnemyAction(next);
        // Thunder slam
        if (entityState == AnimType.SPELL_3) {
            hitBox.x = 12.5*Tiles.TILES_SIZE.getValue();
            hitBox.y = 4*Tiles.TILES_SIZE.getValue();
        }
        // Lightning ball
        else if (entityState == AnimType.SPELL_2) {
            int dir = rand.nextInt(2);
            if (dir == 0) {
                setDirection(Direction.LEFT);
                hitBox.x = 23*Tiles.TILES_SIZE.getValue();
            }
            else {
                setDirection(Direction.RIGHT);
                hitBox.x = 3*Tiles.TILES_SIZE.getValue();
            }
            hitBox.y = player.getHitBox().y-Tiles.TILES_SIZE.getValue();
        }
        // Dash-slash
        else if (entityState == AnimType.ATTACK_3) {
            teleport(levelData, player, 8);
        }
        // Multi Lightning ball
        else if (entityState == AnimType.SPELL_4) {
            hitBox.x = 12.5*Tiles.TILES_SIZE.getValue();
            hitBox.y = 4*Tiles.TILES_SIZE.getValue();
        }
        else if (entityState == AnimType.ATTACK_1 || entityState == AnimType.SPELL_1) {
            teleport(levelData, player, 3);
        }
    }

    private void updateBehavior(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        switch (entityState) {
            case IDLE:
                if (cooldown[Cooldown.ATTACK.ordinal()] == 0)
                    attack(levelData, player);
                //if (canSeePlayer(levelData, player)) directToPlayer(player);
                break;
            case RUN:
                if (canSeePlayer(levelData, player)) directToPlayer(player);
                if (canSeePlayer(levelData, player) && isPlayerCloseForAttack(player) && cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                    attack(levelData, player);
                    break;
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
            case ATTACK_2:
                if (animIndex == 3) movingAttack(levelData, player, 15);
                if (animIndex == 0) {
                    attackCheck = false;
                }
                //if (animIndex == 3 && !attackCheck) checkPlayerHit(attackBox, player);
                break;
            case ATTACK_3:
                dashSlash(levelData, player);
                break;
            case SPELL_1:
                movingAttack(levelData, player, 4);
                if (animIndex % 2 == 0) setDirection(Direction.LEFT);
                else setDirection(Direction.RIGHT);
                break;
            case SPELL_2:
                if (animIndex == 7 && !shooting) {
                    objectManager.shootLightningBall(this);
                    shooting = true;
                }
                else if (animIndex == 9 && shootCount < 2) {
                    animIndex = 2;
                    shootCount++;
                    shooting = false;
                }
                break;
            case SPELL_3:
                if (animIndex == 13) spellManager.activateLightnings();
                break;
            case SPELL_4:
                if (animIndex == 1 && !shooting) {
                    if (multiShootFlag == 1) objectManager.multiLightningBallShoot(this);
                    if (multiShootFlag == 2) spellManager.activateFlashes();
                    if (multiShootFlag == 3) objectManager.multiLightningBallShot2(this);
                    multiShootFlag = (multiShootFlag + 1) % 4;
                    shooting = true;
                }
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
                if (spellAnimations.contains(entityState) || entityState == AnimType.HIT) {
                    if (entityState == AnimType.HIT) {
                        prevAnim = entityState;
                        setEnemyAction(AnimType.IDLE);
                        return;
                    }
                    if (shootCount >= 2) {
                        shootCount = 0;
                    }
                    if (entityState == AnimType.SPELL_4 && !inAir) {
                        multiShootCount++;
                        if (multiShootCount == 16) {
                            multiShootCount = 0;
                        }
                    }
                    else if (entityState == AnimType.ATTACK_1 || entityState == AnimType.ATTACK_2) {
                        if (slashCount == 2) {
                            slashCount = 0;
                            prevAnim = AnimType.ATTACK_1;
                            setEnemyAction(AnimType.IDLE);
                            return;
                        }
                        if (entityState == AnimType.ATTACK_1) setEnemyAction(AnimType.ATTACK_2);
                        else setEnemyAction(AnimType.ATTACK_1);
                        slashCount++;
                        return;
                    }
                    if (multiShootCount == 0 && entityState != AnimType.IDLE) {
                        cooldown[Cooldown.ATTACK.ordinal()] = 14;
                        prevAnim = entityState;
                        setEnemyAction(AnimType.IDLE);
                    }
                }
                else if (entityState == AnimType.DEATH) alive = false;
                shooting = false;
            }
        }
        if (cooldown != null) coolDownTickUpdate();
    }

    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        updateMove(levelData, player, spellManager, objectManager);
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
