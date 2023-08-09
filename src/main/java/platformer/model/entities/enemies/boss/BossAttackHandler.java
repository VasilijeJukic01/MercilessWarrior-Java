package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.utils.Utils;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;

public class BossAttackHandler {

    private final SpearWoman spearWoman;
    private final Rectangle2D.Double hitBox;

    private final List<Anim> actions;

    public BossAttackHandler(SpearWoman spearWoman, List<Anim> actions) {
        this.spearWoman = spearWoman;
        this.hitBox = spearWoman.getHitBox();
        this.actions = actions;
    }

    // Teleport
    public void teleport(int[][] levelData, Player player, int tiles) {
        Random rand = new Random();
        double playerX = player.getHitBox().x;
        double rightTeleport = playerX + tiles * TILES_SIZE;
        double leftTeleport = playerX - tiles * TILES_SIZE;
        int k = rand.nextInt(2);
        doTeleport(levelData, k, rightTeleport, leftTeleport);
        spearWoman.setDirection((playerX < hitBox.x) ? Direction.LEFT : Direction.RIGHT);
    }

    private void doTeleport(int[][] levelData, int k, double rightTeleport, double leftTeleport) {
        if (k == 0 && Utils.getInstance().canMoveHere(rightTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x = rightTeleport;
        else if (k == 0 && Utils.getInstance().canMoveHere(leftTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x = leftTeleport;
        else if (k == 1 && Utils.getInstance().canMoveHere(leftTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x = leftTeleport;
        else if (k == 1 && Utils.getInstance().canMoveHere(rightTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            hitBox.x = rightTeleport;
    }

    // Attacks
    private void thunderSlamAttack() {
        spearWoman.getHitBox().x = 12.5 * TILES_SIZE;
        spearWoman.getHitBox().y = 4 * TILES_SIZE;
    }

    private void lightningBallAttack() {
        Random rand = new Random();
        int dir = rand.nextInt(2);
        if (dir == 0) {
            spearWoman.setDirection(Direction.LEFT);
            hitBox.x = 23 * TILES_SIZE;
        }
        else {
            spearWoman.setDirection(Direction.RIGHT);
            hitBox.x = 3 * TILES_SIZE;
        }
        hitBox.y = spearWoman.getYPos();
    }

    private void dashSlashAttack(int[][] levelData, Player player) {
        teleport(levelData, player, 8);
        Audio.getInstance().getAudioPlayer().playSound(Sound.SW_ROAR_1);
        spearWoman.setAttackCooldown(5.5);
    }

    private int multiLightningBallAttack() {
        Random rand = new Random();
        hitBox.x = 12.5 * TILES_SIZE;
        hitBox.y = 4 * TILES_SIZE;
        return rand.nextInt(2);
    }

    private void classicAttack(int[][] levelData, Player player) {
        teleport(levelData, player, 3);
        spearWoman.setAttackCooldown(5.5);
    }

    public void attack(int[][] levelData, Player player, Anim prevAnim) {
        Random rand = new Random();
        spearWoman.attackReset();

        Anim next;
        do {
            next = actions.get(rand.nextInt(actions.size()));
        } while (next == prevAnim || next == Anim.ATTACK_2);
        spearWoman.setEnemyAction(next);

        switch (spearWoman.getEnemyAction()) {
            case ATTACK_1:
            case SPELL_1:
                spearWoman.prepareForClassicAttack();
                classicAttack(levelData, player); break;
            case ATTACK_3:
                spearWoman.changeAttackBox();
                dashSlashAttack(levelData, player); break;
            case SPELL_2:
                lightningBallAttack(); break;
            case SPELL_3:
                thunderSlamAttack(); break;
            case SPELL_4:
                spearWoman.setSpecialAttackIndex(multiLightningBallAttack()); break;
            default: break;

        }

    }


}
