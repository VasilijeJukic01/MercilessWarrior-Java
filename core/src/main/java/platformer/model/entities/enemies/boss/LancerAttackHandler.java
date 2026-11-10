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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.canMoveHere;

public class LancerAttackHandler {

    private final Lancer lancer;
    private final Rectangle2D.Double hitBox;
    private final Random rand = new Random();

    private final List<Anim> moveBag = new ArrayList<>();
    private Anim lastMove = null;

    public LancerAttackHandler(Lancer lancer) {
        this.lancer = lancer;
        this.hitBox = lancer.getHitBox();
        refillBag();
    }

    private void refillBag() {
        moveBag.clear();
        moveBag.add(Anim.ATTACK_1);
        moveBag.add(Anim.ATTACK_3);
        moveBag.add(Anim.SPELL_1);
        moveBag.add(Anim.SPELL_2);
        moveBag.add(Anim.SPELL_3);
        moveBag.add(Anim.SPELL_4);
        Collections.shuffle(moveBag);
        if (lastMove != null && moveBag.size() > 1 && moveBag.get(0) == lastMove) {
            Collections.swap(moveBag, 0, moveBag.size() - 1);
        }
    }

    private Anim drawNextMove() {
        if (moveBag.isEmpty()) refillBag();
        Anim next = moveBag.remove(0);
        lastMove = next;
        return next;
    }

    public void attack(int[][] levelData, Player player) {
        lancer.attackReset();
        Anim next = drawNextMove();
        lancer.setEnemyAction(next);
        switch (next) {
            case ATTACK_1, SPELL_1 -> {
                lancer.prepareForClassicAttack();
                classicAttack(levelData, player);
            }
            case ATTACK_3 -> {
                lancer.changeAttackBox();
                dashSlashAttack(levelData, player);
            }
            case SPELL_2 -> lightningBallAttack();
            case SPELL_3 -> thunderSlamAttack();
            case SPELL_4 -> lancer.setSpecialAttackIndex(multiLightningBallAttack());
            default -> {}
        }
    }

    public void startThunderSlam() {
        lancer.attackReset();
        lancer.setEnemyAction(Anim.SPELL_3);
        thunderSlamAttack();
    }

    private void thunderSlamAttack() {
        performTeleport(13.5 * TILES_SIZE, 4.5 * TILES_SIZE);
    }

    private void lightningBallAttack() {
        double targetX;
        if (rand.nextInt(2) == 0) {
            lancer.setDirection(Direction.LEFT);
            targetX = 22 * TILES_SIZE;
        }
        else {
            lancer.setDirection(Direction.RIGHT);
            targetX = 3.5 * TILES_SIZE;
        }
        performTeleport(targetX, lancer.getYPos());
    }

    private void dashSlashAttack(int[][] levelData, Player player) {
        teleport(levelData, player, 8);
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_ROAR_1);
        lancer.setAttackCooldown(5.5);
    }

    private int multiLightningBallAttack() {
        performTeleport(13.5 * TILES_SIZE, 4.5 * TILES_SIZE);
        return rand.nextInt(2);
    }

    private void classicAttack(int[][] levelData, Player player) {
        teleport(levelData, player, 3);
        lancer.setAttackCooldown(5.5);
    }

    public void teleport(int[][] levelData, Player player, int tiles) {
        double playerX = player.getHitBox().x;
        double rightTeleport = playerX + tiles * TILES_SIZE;
        double leftTeleport = playerX - tiles * TILES_SIZE;
        int k = rand.nextInt(2);
        doTeleport(levelData, k, rightTeleport, leftTeleport);
        lancer.setDirection((playerX < hitBox.x) ? Direction.LEFT : Direction.RIGHT);
    }

    private void doTeleport(int[][] levelData, int k, double rightTeleport, double leftTeleport) {
        double targetX = hitBox.x;
        if (k == 0 && canMoveHere(rightTeleport, lancer.getYPos(), hitBox.width, hitBox.height, levelData))
            targetX = rightTeleport;
        else if (k == 0 && canMoveHere(leftTeleport, lancer.getYPos(), hitBox.width, hitBox.height, levelData))
            targetX = leftTeleport;
        else if (k == 1 && canMoveHere(leftTeleport, lancer.getYPos(), hitBox.width, hitBox.height, levelData))
            targetX = leftTeleport;
        else if (k == 1 && canMoveHere(rightTeleport, lancer.getYPos(), hitBox.width, hitBox.height, levelData))
            targetX = rightTeleport;
        performTeleport(targetX, lancer.getYPos());
    }

    private void performTeleport(double newX, double newY) {
        EventBus.getInstance().publish(new LancerTeleportEvent(new Point((int) hitBox.getCenterX(), (int) hitBox.getCenterY()), false));
        hitBox.x = newX;
        hitBox.y = newY;
        EventBus.getInstance().publish(new LancerTeleportEvent(new Point((int) hitBox.getCenterX(), (int) hitBox.getCenterY()), true));
    }
}
