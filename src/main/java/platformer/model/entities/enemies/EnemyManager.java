package platformer.model.entities.enemies;

import platformer.animation.AnimType;
import platformer.animation.AnimationUtils;
import platformer.audio.Audio;
import platformer.debug.Message;
import platformer.model.Tiles;
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
import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class EnemyManager {

    private final PlayingState playingState;
    private BufferedImage[][] skeletonAnimations;
    private BufferedImage[][] ghoulAnimations;
    private List<Skeleton> skeletons = new ArrayList<>();
    private List<Ghoul> ghouls = new ArrayList<>();

    public EnemyManager(PlayingState playingState) {
        this.playingState = playingState;
        init();
    }

    private void init() {
        this.skeletonAnimations = AnimationUtils.getInstance().loadSkeletonAnimations();
        this.ghoulAnimations = AnimationUtils.getInstance().loadGhoulAnimation();
    }

    public void loadEnemies(Level level) {
        this.skeletons = level.getSkeletons();
        this.ghouls = level.getGhouls();
    }

    private void renderSkeletons(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Skeleton s : skeletons) {
            if (s.isAlive()) {
                int fC = s.getFlipCoefficient(), fS = s.getFlipSign();
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

    private void renderGhouls(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Ghoul gh : ghouls) {
            if (gh.isAlive()) {
                int fC = gh.getFlipCoefficient(), fS = gh.getFlipSign();
                int x = (int) gh.getHitBox().x - EnemySize.GHOUL_X_OFFSET.getValue() - xLevelOffset + fC;
                int y = (int) gh.getHitBox().y - EnemySize.GHOUL_Y_OFFSET.getValue() - yLevelOffset+1 + (int)gh.getPushOffset();
                int w = EnemySize.GHOUL_WIDTH.getValue() * fS;
                int h = EnemySize.GHOUL_HEIGHT.getValue();
                g.drawImage(ghoulAnimations[gh.getEnemyAction().ordinal()][gh.getAnimIndex()], x, y, w, h, null);
                gh.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
                gh.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
                if (gh.getEnemyAction() == AnimType.HIDE || gh.getEnemyAction() == AnimType.REVEAL) {
                    int r = (gh.getAnimIndex() > 15 && gh.getEnemyAction() == AnimType.REVEAL) ? (255) : (0);
                    g.setColor(new Color(r, 0, 0, gh.getFadeCoefficient()));
                    g.fillRect(0, 0, (int)Tiles.GAME_WIDTH.getValue(), (int)Tiles.GAME_HEIGHT.getValue());
                }
            }
        }
    }

    // Enemy hit

    private void checkEnemyDying(Enemy e, Player player) {
        Random rand = new Random();
        if (e.getEnemyAction() == AnimType.DEATH) {
            playingState.getObjectManager().generateCoins(e.getHitBox());
            player.changeStamina(rand.nextInt(5));
            player.changeExp(rand.nextInt(50)+100);
        }
    }

    public void checkEnemyHit(Rectangle2D.Double attackBox, Player player) {
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH) {
                if (attackBox.intersects(skeleton.getHitBox())) {
                    skeleton.hit(5, true, true);
                    checkEnemyDying(skeleton, player);
                    if (skeleton.getEnemyAction() == AnimType.BLOCK) playingState.getGame().notifyLogger("Enemy blocks player's attack.", Message.NOTIFICATION);
                    else playingState.getGame().notifyLogger("Player gives damage to enemy: 5", Message.NOTIFICATION);
                    return;
                }
            }
        }
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive() && ghoul.getEnemyAction() != AnimType.DEATH) {
                if (attackBox.intersects(ghoul.getHitBox())) {
                    if (ghoul.getEnemyAction() == AnimType.HIDE || ghoul.getEnemyAction() == AnimType.REVEAL) return;
                    ghoul.hit(5, true, true);
                    checkEnemyDying(ghoul, player);
                    playingState.getGame().notifyLogger("Player gives damage to enemy: 5", Message.NOTIFICATION);
                    return;
                }
            }
        }
        if (!player.isDash() && !player.isOnWall()) Audio.getInstance().getAudioPlayer().playSlashSound();
    }

    public void checkEnemySpellHit() {
        Flames flames = playingState.getSpellManager().getFlames();
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH) {
                if (flames.isAlive() && flames.getHitBox().intersects(skeleton.getHitBox())) {
                    skeleton.spellHit(0.08);
                    checkEnemyDying(skeleton, playingState.getPlayer());
                    return;
                }
            }
        }
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive() && ghoul.getEnemyAction() != AnimType.DEATH) {
                if (flames.isAlive() && flames.getHitBox().intersects(ghoul.getHitBox())) {
                    ghoul.spellHit(0.16);
                    checkEnemyDying(ghoul, playingState.getPlayer());
                    return;
                }
            }
        }
    }

    public void checkEnemyTrapHit(GameObject object) {
        if (!(object instanceof Spike)) return;
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH && object.getHitBox().intersects(skeleton.getHitBox())) {
                skeleton.hit(500, false, false);
                return;
            }
        }
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive() && ghoul.getEnemyAction() != AnimType.DEATH && object.getHitBox().intersects(ghoul.getHitBox())) {
                ghoul.hit(500, false, false);
                return;
            }
        }
    }

    public void checkEnemyProjectileHit(Projectile projectile) {
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != AnimType.DEATH && projectile.getHitBox().intersects(skeleton.getHitBox())) {
                skeleton.hit(5, false, false);
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
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive()) ghoul.update(ghoulAnimations, levelData, player);
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderSkeletons(g, xLevelOffset, yLevelOffset);
        renderGhouls(g, xLevelOffset, yLevelOffset);
    }

    public void reset() {
        for (Skeleton skeleton : skeletons) {
            skeleton.reset();
        }
        for (Ghoul ghoul : ghouls) {
            ghoul.reset();
        }
    }

}
