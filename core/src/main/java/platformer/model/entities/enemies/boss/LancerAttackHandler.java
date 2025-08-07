package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.event.EventBus;
import platformer.event.events.lancer.LancerTeleportEvent;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.canMoveHere;

public class LancerAttackHandler {

    private final Lancer lancer;
    private final Rectangle2D.Double hitBox;

    private final List<Anim> actions;

    public LancerAttackHandler(Lancer lancer, List<Anim> actions) {
        this.lancer = lancer;
        this.hitBox = lancer.getHitBox();
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
        lancer.setDirection((playerX < hitBox.x) ? Direction.LEFT : Direction.RIGHT);
    }

    private void doTeleport(int[][] levelData, int k, double rightTeleport, double leftTeleport) {
        double targetX = hitBox.x;
        if (k == 0 && canMoveHere(rightTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            targetX = rightTeleport;
        else if (k == 0 && canMoveHere(leftTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            targetX = leftTeleport;
        else if (k == 1 && canMoveHere(leftTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            targetX = leftTeleport;
        else if (k == 1 && canMoveHere(rightTeleport, hitBox.y, hitBox.width, hitBox.height, levelData))
            targetX = rightTeleport;
        performTeleport(targetX, hitBox.y);
    }

    // Attacks
    private void thunderSlamAttack() {
        performTeleport(12.5 * TILES_SIZE, 4 * TILES_SIZE);
    }

    private void lightningBallAttack() {
        Random rand = new Random();
        double targetX;
        if (rand.nextInt(2) == 0) {
            lancer.setDirection(Direction.LEFT);
            targetX = 23 * TILES_SIZE;
        }
        else {
            lancer.setDirection(Direction.RIGHT);
            targetX = 3 * TILES_SIZE;
        }
        performTeleport(targetX, lancer.getYPos());
    }

    private void dashSlashAttack(int[][] levelData, Player player) {
        teleport(levelData, player, 8);
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_ROAR_1);
        lancer.setAttackCooldown(5.5);
    }

    private int multiLightningBallAttack() {
        performTeleport(12.5 * TILES_SIZE, 4 * TILES_SIZE);
        return new Random().nextInt(2);
    }

    private void classicAttack(int[][] levelData, Player player) {
        teleport(levelData, player, 3);
        lancer.setAttackCooldown(5.5);
    }

    public void attack(int[][] levelData, Player player, Anim prevAnim) {
        Random rand = new Random();
        lancer.attackReset();

        Anim next;
        do {
            next = actions.get(rand.nextInt(actions.size()));
        } while (next == prevAnim || next == Anim.ATTACK_2);
        lancer.setEnemyAction(next);

        switch (lancer.getEnemyAction()) {
            case ATTACK_1:
            case SPELL_1:
                lancer.prepareForClassicAttack();
                classicAttack(levelData, player); break;
            case ATTACK_3:
                lancer.changeAttackBox();
                dashSlashAttack(levelData, player); break;
            case SPELL_2:
                lightningBallAttack(); break;
            case SPELL_3:
                thunderSlamAttack(); break;
            case SPELL_4:
                lancer.setSpecialAttackIndex(multiLightningBallAttack()); break;
            default: break;

        }

    }

    private void performTeleport(double newX, double newY) {
        EventBus.getInstance().publish(new LancerTeleportEvent(new Point((int) hitBox.getCenterX(), (int) hitBox.getCenterY()), false));
        hitBox.x = newX;
        hitBox.y = newY;
        EventBus.getInstance().publish(new LancerTeleportEvent(new Point((int) hitBox.getCenterX(), (int) hitBox.getCenterY()), true));
    }

}
