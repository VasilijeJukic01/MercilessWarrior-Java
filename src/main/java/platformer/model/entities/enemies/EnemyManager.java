package platformer.model.entities.enemies;

import platformer.animation.AnimType;
import platformer.animation.AnimationUtils;
import platformer.audio.Audio;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.model.levels.Level;
import platformer.model.objects.GameObject;
import platformer.model.objects.Projectile;
import platformer.model.objects.Spike;
import platformer.model.spells.Flames;
import platformer.state.PlayingState;


import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class EnemyManager {

    private final PlayingState playingState;
    private BufferedImage[][] skeletonAnimations;
    private List<Skeleton> skeletons = new ArrayList<>();

    public EnemyManager(PlayingState playingState) {
        this.playingState = playingState;
        init();
    }

    private void init() {
        this.skeletonAnimations = AnimationUtils.getInstance().loadSkeletonAnimations();
    }

    public void loadEnemies(Level level) {
        this.skeletons = level.getSkeletons();
    }

    private void renderSkeletons(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Skeleton s : skeletons) {
            int fC = s.getFlipCoefficient(), fS = s.getFlipSign();
            if (s.isAlive()) {
                int x = (int) s.getHitBox().x - EnemySize.SKELETON_X_OFFSET.getValue() - xLevelOffset + fC;
                int y = (int) s.getHitBox().y - EnemySize.SKELETON_Y_OFFSET.getValue() - yLevelOffset+1 + (int)s.getPushOffset();
                int w = EnemySize.SKELETON_WIDTH.getValue() * fS;
                int h = EnemySize.SKELETON_HEIGHT.getValue();
                g.drawImage(skeletonAnimations[s.getEnemyAction().ordinal()][s.getAnimIndex()], x, y, w, h, null);
                s.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
                s.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
            }
        }
    }

    // Enemy hit
    public void checkEnemyHit(Rectangle2D.Double attackBox, Player player) {
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH) {
                if (attackBox.intersects(skeleton.getHitBox())) {
                    skeleton.hit(5);
                    player.changeStamina(2);
                    Audio.getInstance().getAudioPlayer().playHitSound();
                    return;
                }
                if (!player.isDash() && !player.isOnWall()) Audio.getInstance().getAudioPlayer().playSlashSound();
            }
        }
    }

    public void checkEnemySpellHit() {
        Flames flames = playingState.getSpellManager().getFlames();
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH) {
                if (flames.isAlive() && flames.getHitBox().intersects(skeleton.getHitBox())) {
                    skeleton.hit(1);
                    return;
                }
            }
        }
    }

    public void checkEnemyTrapHit(GameObject object) {
        if (!(object instanceof Spike)) return;
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH && object.getHitBox().intersects(skeleton.getHitBox())) {
                skeleton.hit(500);
                return;
            }
        }
    }

    public void checkEnemyProjectileHit(Projectile projectile) {
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH && projectile.getHitBox().intersects(skeleton.getHitBox())) {
                skeleton.hit(5);
                Direction projectileDirection = projectile.getDirection();
                Direction skeletonDirection = skeleton.getDirection();
                if (projectileDirection == Direction.LEFT && skeletonDirection == Direction.LEFT) skeleton.setPushDirection(Direction.RIGHT);
                else if (projectileDirection == Direction.LEFT && skeletonDirection == Direction.RIGHT) skeleton.setPushDirection(Direction.RIGHT);
                else if (projectileDirection == Direction.RIGHT && skeletonDirection == Direction.LEFT) skeleton.setPushDirection(Direction.LEFT);
                else if (projectileDirection == Direction.RIGHT && skeletonDirection == Direction.RIGHT) skeleton.setPushDirection(Direction.LEFT);
                projectile.setAlive(false);
            }
        }
    }

    public void update(int[][] levelData, Player player) {
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive()) skeleton.update(skeletonAnimations, levelData, player);
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderSkeletons(g, xLevelOffset, yLevelOffset);
    }

    public void reset() {
        for (Skeleton skeleton : skeletons) {
            skeleton.reset();
        }
    }

}
