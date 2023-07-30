package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.animation.AnimUtils;
import platformer.audio.Audio;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.model.entities.PlayerBonus;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.levels.Level;
import platformer.model.objects.GameObject;
import platformer.model.objects.projectiles.Projectile;
import platformer.model.objects.Spike;
import platformer.model.spells.Flames;
import platformer.state.PlayingState;


import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;

@SuppressWarnings("FieldCanBeLocal")
public class EnemyManager {

    private final PlayingState playingState;
    // Animations
    private BufferedImage[][] skeletonAnimations, ghoulAnimations, spearWomanAnimations;
    // Enemies
    private List<Skeleton> skeletons = new ArrayList<>();
    private List<Ghoul> ghouls = new ArrayList<>();
    private SpearWoman spearWoman;

    public EnemyManager(PlayingState playingState) {
        this.playingState = playingState;
        init();
    }

    // Init
    private void init() {
        this.skeletonAnimations = AnimUtils.getInstance().loadSkeletonAnimations(SKELETON_WIDTH, SKELETON_HEIGHT);
        this.ghoulAnimations = AnimUtils.getInstance().loadGhoulAnimation(GHOUL_WIDTH, GHOUL_HEIGHT);
        this.spearWomanAnimations = AnimUtils.getInstance().loadSpearWomanAnimations(SW_WIDTH, SW_HEIGHT);
    }

    public void loadEnemies(Level level) {
        this.skeletons = level.getSkeletons();
        this.ghouls = level.getGhouls();
        this.spearWoman = level.getSpearWoman();
    }

    private void renderCriticalHit(Graphics g, int xLevelOffset, int yLevelOffset, Enemy e) {
        if (e.isCriticalHit()) {
            int xCritical = (int)(e.getHitBox().x - xLevelOffset);
            int yCritical = (int)(e.getHitBox().y - 5*SCALE - yLevelOffset);
            g.setColor(Color.RED);
            g.drawString("CRITICAL", xCritical, yCritical);
        }
    }

    // Render
    private void renderSkeletons(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Skeleton s : skeletons) {
            if (s.isAlive()) {
                int fC = s.getFlipCoefficient(), fS = s.getFlipSign();
                int x = (int) s.getHitBox().x - SKELETON_X_OFFSET - xLevelOffset + fC;
                int y = (int) s.getHitBox().y - SKELETON_Y_OFFSET - yLevelOffset+1 + (int)s.getPushOffset();
                int w = SKELETON_WIDTH * fS;
                int h = SKELETON_HEIGHT;
                g.drawImage(skeletonAnimations[s.getEnemyAction().ordinal()][s.getAnimIndex()], x, y, w, h, null);
                renderCriticalHit(g, xLevelOffset, yLevelOffset, s);
                s.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
                s.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
            }
        }
    }

    private void renderGhouls(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Ghoul gh : ghouls) {
            if (gh.isAlive()) {
                int fC = gh.getFlipCoefficient(), fS = gh.getFlipSign();
                int x = (int) gh.getHitBox().x - GHOUL_X_OFFSET - xLevelOffset + fC;
                int y = (int) gh.getHitBox().y - GHOUL_Y_OFFSET - yLevelOffset+1 + (int)gh.getPushOffset();
                int w = GHOUL_WIDTH * fS;
                int h = GHOUL_HEIGHT;
                g.drawImage(ghoulAnimations[gh.getEnemyAction().ordinal()][gh.getAnimIndex()], x, y, w, h, null);
                renderCriticalHit(g, xLevelOffset, yLevelOffset, gh);
                gh.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
                gh.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
                // Ghoul special
                if (gh.getEnemyAction() == Anim.HIDE || gh.getEnemyAction() == Anim.REVEAL) {
                    int r = (gh.getAnimIndex() > 15 && gh.getEnemyAction() == Anim.REVEAL) ? (255) : (0);
                    g.setColor(new Color(r, 0, 0, gh.getFadeCoefficient()));
                    g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
                }
            }
        }
    }

    private void renderSpearWoman(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (spearWoman == null) return;
        if (spearWoman.isAlive()) {
            int fC = spearWoman.getFlipCoefficient(), fS = spearWoman.getFlipSign();
            int x = (int) spearWoman.getHitBox().x - SW_X_OFFSET - xLevelOffset + fC;
            int y = (int) spearWoman.getHitBox().y - SW_Y_OFFSET - yLevelOffset+1 + (int)spearWoman.getPushOffset();
            int w = SW_WIDTH * fS;
            int h = SW_HEIGHT;
            if (fS == -1) x -= 21*SCALE;
            g.drawImage(spearWomanAnimations[spearWoman.getEnemyAction().ordinal()][spearWoman.getAnimIndex()], x, y, w, h, null);
            renderCriticalHit(g, xLevelOffset, yLevelOffset, spearWoman);
            spearWoman.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
            spearWoman.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
            spearWoman.overlayRender(g);
        }
    }

    // Enemy hit
    private void checkEnemyDying(Enemy e, Player player) {
        Random rand = new Random();
        if (e.getEnemyAction() == Anim.DEATH) {
            playingState.getObjectManager().generateCoins(e.getHitBox());
            player.changeStamina(rand.nextInt(5));
            player.changeExp(rand.nextInt(50)+100);
        }
    }

    private int[] damage(Player player) {
        int critical = 0;
        int dmg = player.isTransform() ? player.getTransformAttackDmg() : player.getAttackDmg();
        dmg += PlayerBonus.getInstance().getBonusAttack();
        Random rand = new Random();
        int criticalHit = rand.nextInt(100-PlayerBonus.getInstance().getCriticalHitChance());
        if (criticalHit >= 1 && criticalHit <= 10) {
            dmg *= 2;
            critical = 1;
        }
        return new int[] {dmg, critical};
    }

    public void checkEnemyHit(Rectangle2D.Double attackBox, Player player) {
        int[] dmg = damage(player);

        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != Anim.DEATH) {
                if (attackBox.intersects(skeleton.getHitBox())) {
                    skeleton.hit(dmg[0], true, true);
                    skeleton.setCriticalHit(dmg[1] == 1);
                    checkEnemyDying(skeleton, player);
                    writeHitLog(skeleton.getEnemyAction(), dmg[0]);
                    player.setDashHit(true);
                    return;
                }
            }
        }
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive() && ghoul.getEnemyAction() != Anim.DEATH) {
                if (attackBox.intersects(ghoul.getHitBox())) {
                    if (ghoul.getEnemyAction() == Anim.HIDE || ghoul.getEnemyAction() == Anim.REVEAL) return;
                    ghoul.hit(dmg[0], true, true);
                    ghoul.setCriticalHit(dmg[1] == 1);
                    checkEnemyDying(ghoul, player);
                    writeHitLog(ghoul.getEnemyAction(), dmg[0]);
                    player.setDashHit(true);
                    return;
                }
            }
        }
        if (!player.isDash() && !player.isOnWall()) Audio.getInstance().getAudioPlayer().playSlashSound();
        if (spearWoman == null) return;
        if (spearWoman.isAlive() && spearWoman.getEnemyAction() != Anim.DEATH) {
            if (attackBox.intersects(spearWoman.getHitBox())) {
                spearWoman.hit(dmg[0]);
                checkEnemyDying(spearWoman, player);
                writeHitLog(spearWoman.getEnemyAction(), dmg[0]);
                player.setDashHit(true);
            }
        }
    }

    private void writeHitLog(Anim anim, int dmg) {
        if (anim == Anim.BLOCK) Logger.getInstance().notify("Enemy blocks player's attack.", Message.NOTIFICATION);
        else Logger.getInstance().notify("Player gives damage to enemy: "+dmg, Message.NOTIFICATION);
    }

    public void checkEnemySpellHit() {
        Flames flames = playingState.getSpellManager().getFlames();
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != Anim.DEATH) {
                if (flames.isAlive() && flames.getHitBox().intersects(skeleton.getHitBox())) {
                    skeleton.spellHit(0.08);
                    checkEnemyDying(skeleton, playingState.getPlayer());
                    return;
                }
            }
        }
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive() && ghoul.getEnemyAction() != Anim.DEATH) {
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
            if (skeleton.isAlive() && skeleton.getEnemyAction() != Anim.DEATH && object.getHitBox().intersects(skeleton.getHitBox())) {
                skeleton.hit(500, false, false);
                return;
            }
        }
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive() && ghoul.getEnemyAction() != Anim.DEATH && object.getHitBox().intersects(ghoul.getHitBox())) {
                ghoul.hit(500, false, false);
                return;
            }
        }
    }

    public void checkEnemyProjectileHit(Projectile projectile) {
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive() && skeleton.getEnemyAction() != Anim.DEATH && projectile.getHitBox().intersects(skeleton.getHitBox())) {
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

    // Core
    public void update(int[][] levelData, Player player) {
        for (Skeleton skeleton : skeletons) {
            if (skeleton.isAlive()) skeleton.update(skeletonAnimations, levelData, player);
        }
        for (Ghoul ghoul : ghouls) {
            if (ghoul.isAlive()) ghoul.update(ghoulAnimations, levelData, player);
        }
        if (spearWoman == null) return;
        spearWoman.update(spearWomanAnimations, levelData, player, playingState.getSpellManager(), playingState.getObjectManager());
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            renderSkeletons(g, xLevelOffset, yLevelOffset);
            renderGhouls(g, xLevelOffset, yLevelOffset);
        }
        catch (Exception ignored) {}
        if (spearWoman == null) return;
        renderSpearWoman(g, xLevelOffset, yLevelOffset);
    }

    public void reset() {
        for (Skeleton skeleton : skeletons) {
            skeleton.reset();
        }
        for (Ghoul ghoul : ghouls) {
            ghoul.reset();
        }
        if (spearWoman == null) return;
        spearWoman.reset();
    }

}
