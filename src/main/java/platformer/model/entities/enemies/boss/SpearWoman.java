package platformer.model.entities.enemies.boss;

import platformer.animation.AnimType;
import platformer.audio.Audio;
import platformer.audio.Songs;
import platformer.audio.Sounds;
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
    private boolean start;

    private boolean shooting, flash = true;
    private int shootCount = 0;
    private int multiShootCount = 0;
    private int multiShootFlag = 0;
    private int slashCount = 0;

    private final int attackBoxWid = (int)(96 * Tiles.SCALE.getValue()), attackBoxReducedWid = (int)(48 * Tiles.SCALE.getValue());

    private int attackOffset;
    private final List<AnimType> spellAnimations =
            new ArrayList<>(List.of(AnimType.ATTACK_1, AnimType.ATTACK_2, AnimType.ATTACK_3, AnimType.SPELL_1, AnimType.SPELL_2, AnimType.SPELL_3, AnimType.SPELL_4));

    // Physics
    private double airSpeed = 0;
    private final double gravity = 0.1 * Tiles.SCALE.getValue();
    private final double collisionFallSpeed = 0.5 * Tiles.SCALE.getValue();


    public SpearWoman(int xPos, int yPos) {
        super(xPos, yPos, EnemySize.SW_WIDTH.getValue(), EnemySize.SW_HEIGHT.getValue(), EnemyType.SPEAR_WOMAN, 15);
        super.setDirection(Direction.LEFT);
        super.enemySpeed = 0.4*Tiles.SCALE.getValue();
        int w = (int)(25 * Tiles.SCALE.getValue());
        int h =  (int)(50 * Tiles.SCALE.getValue());
        initHitBox(w, h);
        initAttackBox();
        super.cooldown = new double[1];
    }

    private void initAttackBox() {
        int h = (int)(54 * Tiles.SCALE.getValue());
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, attackBoxWid, h);
        this.attackOffset = (int)(33*Tiles.SCALE.getValue());
    }

    // Checkers
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
        slashCount = 0;
        if (currentHealth <= 0) {
            setEnemyAction(AnimType.DEATH);
        }
        else {
            if (entityState != AnimType.IDLE) prevAnim = entityState;
            setEnemyAction(AnimType.HIT);
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

    private void dashSlash(int[][] levelData) {
        if (animIndex == 0) attackCheck = false;
        if (animIndex == 2 && !attackCheck) {
            double xSpeed;

            if (direction == Direction.LEFT) xSpeed = -enemySpeed;
            else xSpeed = enemySpeed;

            if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * 60, hitBox.y, hitBox.width, hitBox.height, levelData))
                if (Utils.getInstance().isFloor(hitBox, xSpeed * 60, levelData, direction)) {
                    hitBox.x += xSpeed * 60;
                }
        }
    }

    private void movingAttack(int[][] levelData, int speed) {
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
        attackBox.width = attackBoxWid;
        attackBox.x = xPos;
        shootCount = 0;
        AnimType next;
        do {
            next = spellAnimations.get(rand.nextInt(7));
        } while (next == prevAnim || next == AnimType.ATTACK_2);
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
            hitBox.y = yPos;
        }
        // Dash-slash
        else if (entityState == AnimType.ATTACK_3) {
            teleport(levelData, player, 8);
            changeAttackBox();
            Audio.getInstance().getAudioPlayer().playSound(Sounds.SW_ROAR_1.ordinal());
        }
        // Multi Lightning ball
        else if (entityState == AnimType.SPELL_4) {
            hitBox.x = 12.5*Tiles.TILES_SIZE.getValue();
            hitBox.y = 4*Tiles.TILES_SIZE.getValue();
        }
        else if (entityState == AnimType.ATTACK_1 || entityState == AnimType.SPELL_1) {
            changeAttackBox();
            animSpeed = 20;
            teleport(levelData, player, 3);
        }
    }

    private void updateBehavior(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        switch (entityState) {
            case IDLE:
                if (!start && isPlayerCloseForAttack(player)) {
                    setStart(true);
                    objectManager.activateBlockers(true);
                    setEnemyAction(AnimType.SPELL_3);
                    return;
                }
                if (start && cooldown[Cooldown.ATTACK.ordinal()] == 0)
                    attack(levelData, player);
                break;
            case ATTACK_1:
            case ATTACK_2:
                if (animIndex == 2) movingAttack(levelData, 30);
                if (animIndex == 0) {
                    Audio.getInstance().getAudioPlayer().playSound(Sounds.SW_ROAR_2.ordinal());
                    attackCheck = false;
                }
                if ((animIndex == 3 || animIndex == 2) && !attackCheck) checkPlayerHit(attackBox, player);
                break;
            case ATTACK_3:
                if (animIndex == 0) attackCheck = false;
                if (slashCount == 0) dashSlash(levelData);
                else if (animIndex == 2) {
                    movingAttack(levelData, 30);
                    Audio.getInstance().getAudioPlayer().playSound(Sounds.SW_ROAR_1.ordinal());
                }
                if (!attackCheck) checkPlayerHit(attackBox, player);
                break;
            case SPELL_1:
                if (animIndex == 0) Audio.getInstance().getAudioPlayer().playSound(Sounds.SW_ROAR_3.ordinal());
                movingAttack(levelData, 10);
                if (animIndex % 2 == 0) setDirection(Direction.LEFT);
                else setDirection(Direction.RIGHT);
                checkPlayerHit(attackBox, player);
                break;
            case SPELL_2:
                if (animIndex == 7 && !shooting) {
                    objectManager.shootLightningBall(this);
                    Audio.getInstance().getAudioPlayer().playSound(Sounds.LIGHTNING_2.ordinal());
                    shooting = true;
                }
                else if (animIndex == 9 && shootCount < 2) {
                    animIndex = 2;
                    shootCount++;
                    shooting = false;
                }
                break;
            case SPELL_3:
                if (animIndex > 5 && animIndex < 16) checkPlayerHit(attackBox, player);
                if (animIndex == 11) Audio.getInstance().getAudioPlayer().playSound(Sounds.SW_ROAR_2.ordinal());
                else if (animIndex == 13) spellManager.activateLightnings();
                break;
            case SPELL_4:
                if (animIndex == 1 && !shooting) {
                    if (multiShootFlag == 1) objectManager.multiLightningBallShoot(this);
                    if (multiShootFlag == 2 || multiShootFlag == 0) {
                        if (flash) spellManager.activateFlashes();
                        flash = !flash;
                    }
                    if (multiShootFlag == 3) objectManager.multiLightningBallShot2(this);
                    multiShootFlag = (multiShootFlag + 1) % 4;
                    shooting = true;
                }
                break;
            case DEATH:
                objectManager.activateBlockers(false);
                break;
            default: break;
        }
    }

    private void changeAttackBox() {
        if (entityState == AnimType.ATTACK_1 || entityState == AnimType.ATTACK_3) attackBox.width = attackBoxReducedWid;
        if (direction == Direction.RIGHT) attackBox.x = attackBoxWid/2;
        else attackBox.x = xPos;
    }

    private void updateAttackBox() {
        if (entityState == AnimType.ATTACK_1 || entityState == AnimType.ATTACK_2 || entityState == AnimType.ATTACK_3) {
            if (direction == Direction.RIGHT) attackBox.x = hitBox.x-hitBox.width + attackOffset*1.3;
            else attackBox.x = hitBox.x - attackOffset*1.3;
        }
        else {
            attackBox.x = hitBox.x - attackOffset;
        }
        attackBox.y = hitBox.y;
    }

    private void updateBoss(BufferedImage[][] animations) {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) {
                animIndex = 0;
                if (spellAnimations.contains(entityState) || entityState == AnimType.HIT) {
                    // Hit finish
                    if (entityState == AnimType.HIT) {
                        shootCount = 0;
                        setEnemyAction(AnimType.IDLE);
                        return;
                    }
                    if (shootCount >= 2) {
                        shootCount = 0;
                    }
                    // Multi shoot finish
                    if (entityState == AnimType.SPELL_4) {
                        multiShootCount++;
                        if (multiShootCount == 16) {
                            multiShootCount = 0;
                            flash = true;
                        }
                    }
                    // Attack finish
                    else if (entityState == AnimType.ATTACK_1 || entityState == AnimType.ATTACK_2 || (entityState == AnimType.ATTACK_3 && slashCount != 0)) {
                        // Slash count check
                        if (slashCount == 2) {
                            slashCount = 0;
                        }
                        // Change attacks
                        else {
                            if (slashCount == 0) setEnemyAction(AnimType.ATTACK_3);
                            else if (slashCount == 1) setEnemyAction(AnimType.ATTACK_2);
                            slashCount++;
                            return;
                        }
                    }
                    if (multiShootCount == 0 && entityState != AnimType.IDLE) {
                        cooldown[Cooldown.ATTACK.ordinal()] = 14;
                        if (entityState == AnimType.ATTACK_1 || entityState == AnimType.ATTACK_2) prevAnim = AnimType.ATTACK_1;
                        else prevAnim = entityState;
                        animSpeed = 25;
                        setEnemyAction(AnimType.IDLE);
                    }
                }
                else if (entityState == AnimType.DEATH) alive = false;
                shooting = false;
            }
        }
        if (cooldown != null) coolDownTickUpdate();
    }

    // Update
    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        updateMove(levelData, player, spellManager, objectManager);
        updateBoss(animations);
        updateAttackBox();
    }

    @Override
    public void reset() {
        super.reset();
        start = false;
        multiShootCount = shootCount  = 0;
        shooting = false;
        multiShootFlag = 0;
        slashCount = 0;
        setDirection(Direction.LEFT);
        setEnemyAction(AnimType.IDLE);
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
        if (start) Audio.getInstance().getAudioPlayer().playSong(Songs.BOSS_1.ordinal());
    }
}
