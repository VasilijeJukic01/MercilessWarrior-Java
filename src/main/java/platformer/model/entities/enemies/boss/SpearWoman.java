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
import platformer.model.objects.ObjectManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static platformer.constants.Constants.*;

public class SpearWoman extends Enemy {

    private Anim prevAnim = Anim.IDLE;
    private final int attackBoxWid = (int)(96 * SCALE), attackBoxReducedWid = (int)(48 * SCALE);

    // Flags
    private boolean start, shooting, flash = true;
    private int multiShootFlag = 0;
    private int shootCount = 0, multiShootCount = 0;
    private int slashCount = 0, rapidSlashCount = 0;
    private int specialType;

    private int attackOffset;
    private final List<Anim> spellAnimations =
            new ArrayList<>(List.of(Anim.ATTACK_1, Anim.ATTACK_2, Anim.ATTACK_3, Anim.SPELL_1, Anim.SPELL_2, Anim.SPELL_3, Anim.SPELL_4));

    // Physics
    private double airSpeed = 0;
    private final double gravity = 0.1 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    // Overlay
    private final BossInterface bossInterface;

    public SpearWoman(int xPos, int yPos) {
        super(xPos, yPos, SW_WIDTH, SW_HEIGHT, EnemyType.SPEAR_WOMAN, 18);
        super.setDirection(Direction.LEFT);
        this.bossInterface = new BossInterface(this);
        int w = (int)(25 * SCALE);
        int h =  (int)(50 * SCALE);
        initHitBox(w, h);
        initAttackBox();
        super.cooldown = new double[1];
    }

    private void initAttackBox() {
        int h = (int)(54 * SCALE);
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, attackBoxWid, h);
        this.attackOffset = (int)(33 * SCALE);
    }

    // Checkers
    private boolean isAirFreeze() {
        if (entityState == Anim.SPELL_3 && animIndex < 2) return true;
        if (entityState == Anim.SPELL_4) return true;
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
            setEnemyAction(Anim.DEATH);
        }
        else {
            if (entityState != Anim.IDLE) prevAnim = entityState;
            setEnemyAction(Anim.HIT);
        }
    }

    private void teleport(int[][] levelData, Player player, int tiles) {
        Random rand = new Random();
        double playerX = player.getHitBox().x;
        int k = rand.nextInt(2);
        if (k == 0 && Utils.getInstance().canMoveHere(playerX+tiles*TILES_SIZE, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX+tiles*TILES_SIZE;
        }
        else if (k == 0 && Utils.getInstance().canMoveHere(playerX-tiles*TILES_SIZE, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX-tiles*TILES_SIZE;
        }
        if (k == 1 && Utils.getInstance().canMoveHere(playerX-tiles*TILES_SIZE, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX-tiles*TILES_SIZE;
        }
        else if (k == 1 && Utils.getInstance().canMoveHere(playerX+tiles*TILES_SIZE, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            hitBox.x = playerX+tiles*TILES_SIZE;
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

    // Core
    private void attack(int[][] levelData, Player player) {
        Random rand = new Random();
        attackBox.width = attackBoxWid;
        attackBox.x = xPos;
        shootCount = 0;

        Anim next;
        do {
            next = spellAnimations.get(rand.nextInt(7));
        } while (next == prevAnim || next == Anim.ATTACK_2);
        setEnemyAction(next);

        // Thunder slam
        if (entityState == Anim.SPELL_3) {
            hitBox.x = 12.5*TILES_SIZE;
            hitBox.y = 4*TILES_SIZE;
        }
        // Lightning ball
        else if (entityState == Anim.SPELL_2) {
            int dir = rand.nextInt(2);
            if (dir == 0) {
                setDirection(Direction.LEFT);
                hitBox.x = 23*TILES_SIZE;
            }
            else {
                setDirection(Direction.RIGHT);
                hitBox.x = 3*TILES_SIZE;
            }
            hitBox.y = yPos;
        }
        // Dash-slash
        else if (entityState == Anim.ATTACK_3) {
            teleport(levelData, player, 8);
            changeAttackBox();
            Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_1);
            cooldown[Cooldown.ATTACK.ordinal()] = 5.5;
        }
        // Multi Lightning ball
        else if (entityState == Anim.SPELL_4) {
            specialType = rand.nextInt(2);
            hitBox.x = 12.5*TILES_SIZE;
            hitBox.y = 4*TILES_SIZE;
        }
        // Classic Attack
        else if (entityState == Anim.ATTACK_1 || entityState == Anim.SPELL_1) {
            rapidSlashCount = 0;
            changeAttackBox();
            animSpeed = 18;
            teleport(levelData, player, 3);
            cooldown[Cooldown.ATTACK.ordinal()] = 5.5;
        }
    }

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
            if (multiShootFlag == 2 || multiShootFlag == 0) {
                if (flash) spellManager.activateFlashes();
                flash = !flash;
            }
            multiShootFlag = (multiShootFlag + 1) % 5;
            shooting = true;
        }
    }

    private void updateBehavior(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        switch (entityState) {
            case IDLE:
                if (!start && isPlayerCloseForAttack(player)) {
                    setStart(true);
                    objectManager.activateBlockers(true);
                    setEnemyAction(Anim.SPELL_3);
                    return;
                }
                if (start && cooldown[Cooldown.ATTACK.ordinal()] == 0)
                    attack(levelData, player);
                break;
            case ATTACK_1:
            case ATTACK_2:
                if (animIndex == 0) {
                    Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_2);
                    attackCheck = false;
                }
                if (animIndex == 2) movingAttack(levelData, 30);
                if ((animIndex == 3 || animIndex == 2) && !attackCheck) checkPlayerHit(attackBox, player);
                break;
            case ATTACK_3:
                if (animIndex == 0) attackCheck = false;
                if (slashCount == 0) dashSlash(levelData);
                else if (animIndex == 2) {
                    movingAttack(levelData, 30);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_1);
                }
                if (!attackCheck) checkPlayerHit(attackBox, player);
                break;
            case SPELL_1:
                if (animIndex == 0) Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_3);
                if (animIndex == 12 && rapidSlashCount < 1) {
                    rapidSlashCount++;
                    animIndex = 2;
                }
                movingAttack(levelData, 10);
                if (animIndex % 2 == 0) setDirection(Direction.LEFT);
                else setDirection(Direction.RIGHT);
                if (animIndex >= 2 && animIndex <= 11) checkPlayerHit(attackBox, player);
                break;
            case SPELL_2:
                if (animIndex == 7 && !shooting) {
                    objectManager.shootLightningBall(this);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.LIGHTNING_2);
                    shooting = true;
                }
                else if (animIndex == 9 && shootCount < 4) {
                    animIndex = 2;
                    shootCount++;
                    shooting = false;
                }
                break;
            case SPELL_3:
                if (animIndex > 5 && animIndex < 16) checkPlayerHit(attackBox, player);
                if (animIndex == 11) Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_2);
                else if (animIndex == 13) spellManager.activateLightnings();
                break;
            case SPELL_4:
                if (specialType == 1) oscillationProjectiles(objectManager);
                else trackingProjectiles(spellManager, objectManager);
                break;
            case DEATH:
                objectManager.activateBlockers(false);
                break;
            default: break;
        }
    }

    private void changeAttackBox() {
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_3) attackBox.width = attackBoxReducedWid;
        if (direction == Direction.RIGHT) attackBox.x = attackBoxWid/2;
        else attackBox.x = xPos;
    }

    private void updateAttackBox() {
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.ATTACK_3) {
            if (direction == Direction.RIGHT) attackBox.x = hitBox.x-hitBox.width + attackOffset*1.3;
            else attackBox.x = hitBox.x - attackOffset*1.3;
        }
        else {
            attackBox.x = hitBox.x - attackOffset;
        }
        attackBox.y = hitBox.y;
    }

    private void updateBoss(BufferedImage[][] animations) {
        if (cooldown != null) {             // Pre-Attack cooldown check
            coolDownTickUpdate();
            if ((entityState != Anim.IDLE && entityState != Anim.HIT) && cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        }
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) {
                animIndex = 0;
                if (spellAnimations.contains(entityState) || entityState == Anim.HIT) {
                    // Hit finish
                    if (entityState == Anim.HIT) {
                        shootCount = 0;
                        setEnemyAction(Anim.IDLE);
                        return;
                    }
                    if (shootCount >= 2) {
                        shootCount = 0;
                    }
                    // Multi shoot finish
                    if (entityState == Anim.SPELL_4) {
                        multiShootCount++;
                        if (multiShootCount == 16) {
                            multiShootCount = 0;
                            flash = true;
                        }
                    }
                    // Attack finish
                    else if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || (entityState == Anim.ATTACK_3 && slashCount != 0)) {
                        // Slash count check
                        if (slashCount == 2) {
                            slashCount = 0;
                        }
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
                else if (entityState == Anim.DEATH) alive = false;
                shooting = false;
            }
        }
    }

    // Update
    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        updateMove(levelData, player, spellManager, objectManager);
        updateBoss(animations);
        updateAttackBox();
    }

    public void overlayRender(Graphics g) {
        if (start) bossInterface.render(g);
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
}
