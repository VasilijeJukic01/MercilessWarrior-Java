package platformer.model.objects;

import platformer.animation.AnimationUtils;
import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.model.entities.PlayerBonus;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.levels.Level;
import platformer.model.objects.projectiles.*;
import platformer.model.spells.Flames;
import platformer.state.PlayingState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import static platformer.constants.Constants.*;

public class ObjectManager {

    private final PlayingState playingState;
    private final BufferedImage[][] objects; // ObjType Dependency
    // Projectiles
    private final BufferedImage projectileArrow;
    private final BufferedImage[] projectileLightningBall, projectileLightningBall2;
    private final ArrayList<Projectile> projectiles;
    // Objects
    private ArrayList<Potion> potions;
    private ArrayList<Container> containers;
    private ArrayList<Spike> spikes;
    private ArrayList<ArrowLauncher> arrowLaunchers;
    private final ArrayList<Coin> coins;
    private ArrayList<Shop> shops;
    private ArrayList<Blocker> blockers;
    private ArrayList<Blacksmith> blacksmiths;
    private ArrayList<Dog> dogs;
    // Flags
    private boolean shopVisible, blacksmithVisible;

    public ObjectManager(PlayingState playingState) {
        this.playingState = playingState;
        this.objects = AnimationUtils.getInstance().loadObjects();
        this.projectiles = new ArrayList<>();
        this.coins = new ArrayList<>();
        this.shops = new ArrayList<>();
        this.blockers = new ArrayList<>();
        this.blacksmiths = new ArrayList<>();
        this.dogs = new ArrayList<>();
        this.projectileArrow = Utils.getInstance().importImage("src/main/resources/images/objs/arrow.png", ARROW_WID, ARROW_HEI);
        this.projectileLightningBall = AnimationUtils.getInstance().loadLightningBall(1);
        this.projectileLightningBall2 = AnimationUtils.getInstance().loadLightningBall(2);
    }

    public void loadObjects(Level level) {
        level.getObjectData();
        this.potions = new ArrayList<>(level.getPotions());
        this.containers = new ArrayList<>(level.getContainers());
        this.spikes = level.getSpikes();
        this.arrowLaunchers = level.getArrowLaunchers();
        this.shops = level.getShops();
        this.blockers = level.getBlockers();
        this.blacksmiths = level.getBlacksmiths();
        this.dogs = level.getDogs();
        projectiles.clear();
    }

    // Intersections
    public void checkEnemyIntersection() {
        for (Spike spike : spikes) {
            playingState.getEnemyManager().checkEnemyTrapHit(spike);
        }
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) playingState.getEnemyManager().checkEnemyProjectileHit(projectile);
        }
    }

    public void checkPlayerIntersection(Player p) {
        for (Spike spike : spikes) {
            if (p.getHitBox().intersects(spike.getHitBox()))
                p.kill();
        }
        for (Blocker blocker : blockers) {
            if (blocker.getAnimIndex() > 2 && p.getHitBox().intersects(blocker.getHitBox()))
                p.kill();
        }
        for (Shop shop : shops) {
            shop.setActive(p.getHitBox().intersects(shop.getHitBox()));
            shopVisible = p.getHitBox().intersects(shop.getHitBox());
        }
        for (Blacksmith blacksmith : blacksmiths) {
            blacksmith.setActive(p.getHitBox().intersects(blacksmith.getHitBox()));
            blacksmithVisible = p.getHitBox().intersects(blacksmith.getHitBox());
        }
    }

    public void checkObjectPick(Rectangle2D.Double hitBox) {
        for (Potion potion : potions) {
            if (potion.isAlive() && hitBox.intersects(potion.getHitBox())) {
                potion.setAlive(false);
                applyPotionEffect(potion);
            }
        }
        ArrayList<Coin> copy = new ArrayList<>(coins);
        for (Coin coin : copy) {
            if (coin.isAlive() && hitBox.intersects(coin.getHitBox())) {
                coins.remove(coin);
                Audio.getInstance().getAudioPlayer().playSound(Sounds.COIN_PICK.ordinal());
                playingState.getPlayer().changeCoins(1);
            }
        }
    }

    // Apply Effects
    public void applyPotionEffect(Potion potion) {
        switch (potion.getObjType()) {
            case HEAL_POTION: playingState.getPlayer().changeHealth(HEAL_POTION_VAL); break;
            case STAMINA_POTION: playingState.getPlayer().changeStamina(STAMINA_POTION_VAL); break;
        }
    }

    // Object Break
    public void checkObjectBreak(Rectangle2D.Double attackBox) {
        Flames flames = playingState.getSpellManager().getFlames();
        for (Container container : containers) {
            boolean isFlame = flames.getHitBox().intersects(container.getHitBox()) && flames.isAlive();
            if (container.isAlive() && !container.animate && (attackBox.intersects(container.getHitBox()) || isFlame)) {
                Logger.getInstance().notify("Player breaks container.", Message.NOTIFICATION);
                container.setAnimate(true);
                Audio.getInstance().getAudioPlayer().playCrateSound();
                Random rand = new Random();
                int value = rand.nextInt(4)-1;
                ObjType potion = null;
                if (value == 0) potion = ObjType.STAMINA_POTION;
                else if (value == 1) potion = ObjType.HEAL_POTION;
                if (potion != null) potions.add(new Potion(potion, (int)(container.getHitBox().x+container.getHitBox().width/2), (int)(container.getHitBox().y-container.getHitBox().height/4)));
            }
        }
    }

    public void checkArrowDeflect(Rectangle2D.Double attackBox) {
        if (!PlayerBonus.getInstance().isDeflect()) return;
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive() && attackBox.intersects(projectile.getHitBox())) {
                projectile.setAlive(false);
            }
        }
    }

    // Object Touch & Bounds
    public boolean isPlayerTouchingObject() {
        for (Container c : containers) {
            if (c.isAlive() && c.getAnimIndex() < 1) {
                if (c.getHitBox().intersects(playingState.getPlayer().getHitBox())) return true;
            }
        }
        return false;
    }

    private boolean checkTouch(GameObject c, Rectangle2D.Double hitBox, String axis) {
        int x = (int)hitBox.x, y = (int)hitBox.y;
        int width = (int)hitBox.width, height = (int)hitBox.height;
        if (c.isAlive() && c.getAnimIndex() < 1) {
            if (axis.equals("X")) {
                if (c.getHitBox().contains(x, y)) return true;
                if (c.getHitBox().contains(x+width, y)) return true;
                if (c.getHitBox().contains(x, y+height-5)) return true;
                return c.getHitBox().contains(x+width, y+height-5);
            }
            else if (axis.equals("Y")) {
                if (c.getHitBox().contains(x, y+height)) return true;
                return c.getHitBox().contains(x+width, y+height);
            }
        }
        return false;
    }

    private GameObject canMove(Direction direction, Rectangle2D.Double hitBox) {
        for (Container c : containers) {
            if (c.isAlive() && checkTouch(c, hitBox, "X")) {
                if (c.getHitBox().x < hitBox.x && direction == Direction.LEFT) return c;
                else if(c.getHitBox().x > hitBox.x && direction == Direction.RIGHT) return c;
            }
        }
        return null;
    }

    public double getXObjectBound(Rectangle2D.Double hitBox, double dx) {
        for (Container c : containers) {
            if (c.isAlive() && checkTouch(c, hitBox, "X")) {
                if (c.getHitBox().x < hitBox.x && dx > 0) {
                    if (Utils.getInstance().canMoveHere(hitBox.x+1, hitBox.y, hitBox.width, hitBox.height, playingState.getLevelManager().getCurrentLevel().getLvlData()))
                        return hitBox.x+1;
                }
                else if(c.getHitBox().x > hitBox.x && dx < 0) {
                    if (Utils.getInstance().canMoveHere(hitBox.x-1, hitBox.y, hitBox.width, hitBox.height, playingState.getLevelManager().getCurrentLevel().getLvlData()))
                        return hitBox.x-1;
                }
                else return hitBox.x;

            }
            if (c.isAlive() && checkTouch(c, hitBox, "Y")) {
                GameObject left = canMove(Direction.LEFT, hitBox);
                GameObject right = canMove(Direction.RIGHT, hitBox);
                if (dx < 0 && left == null) {
                    if (Utils.getInstance().canMoveHere(hitBox.x-1, hitBox.y, hitBox.width, hitBox.height, playingState.getLevelManager().getCurrentLevel().getLvlData()))
                        return hitBox.x-1;
                }
                else if(dx > 0 && right == null) {
                    if (Utils.getInstance().canMoveHere(hitBox.x+1, hitBox.y, hitBox.width, hitBox.height, playingState.getLevelManager().getCurrentLevel().getLvlData()))
                        return hitBox.x+1;
                }
                else if (dx < 0 && left.isAlive()) {
                    double x = left.getHitBox().x+left.getHitBox().width;
                    if (Utils.getInstance().canMoveHere(x, hitBox.y, hitBox.width, hitBox.height, playingState.getLevelManager().getCurrentLevel().getLvlData()))
                        return x;
                }
                else if (dx > 0 && right.isAlive()) {
                    double x = right.getHitBox().x-hitBox.width;
                    if (Utils.getInstance().canMoveHere(x, hitBox.y, hitBox.width, hitBox.height, playingState.getLevelManager().getCurrentLevel().getLvlData()))
                            return x;
                }
                else return hitBox.x;
            }
        }
        return hitBox.x;
    }

    // Physics Checks
    private boolean isObjectInAir(GameObject object) {
        for (Container container : containers) {
            if (container != object && !Utils.getInstance().canMoveHere(object.getHitBox().x, object.getHitBox().y + 1, object.getHitBox().width, object.getHitBox().height,
                    playingState.getLevelManager().getCurrentLevel().getLvlData())) return false;
            if (container.isAlive() && container != object && container.getHitBox().intersects(object.getHitBox())) return false;
        }
        return true;
    }

    private void landObject(GameObject gameObject) {
        boolean isSafe = true;
        while(isSafe) {
            isSafe = isObjectInAir(gameObject);
            if (isSafe) {
                if (Utils.getInstance().canMoveHere(gameObject.getHitBox().x, gameObject.getHitBox().y + 1, gameObject.getHitBox().width, gameObject.getHitBox().height,
                        playingState.getLevelManager().getCurrentLevel().getLvlData())) {
                    gameObject.getHitBox().y += 1;
                }
            }
            else {
                gameObject.getHitBox().y += 2;
                gameObject.setOnGround(true);
            }
        }
    }

    private void updateObjectInAir() {
        for (Container container : containers) {
            if (isObjectInAir(container)) container.setOnGround(false);
            if (!container.isOnGround) landObject(container);
        }
        for (Blacksmith blacksmith : blacksmiths) {
            if (isObjectInAir(blacksmith)) blacksmith.setOnGround(false);
            if (!blacksmith.isOnGround) landObject(blacksmith);
        }
    }

    // Launchers
    private boolean isPlayerInRange(ArrowLauncher arrowLauncher, Player player) {
        int distance = (int)Math.abs(player.getHitBox().x - arrowLauncher.getHitBox().x);
        return distance <= TILES_SIZE * 5;
    }

    private boolean isPlayerInFront(ArrowLauncher arrowLauncher, Player player) {
        if (arrowLauncher.getObjType() == ObjType.ARROW_LAUNCHER_LEFT) {
            return arrowLauncher.getHitBox().x > player.getHitBox().x;
        }
        else if (arrowLauncher.getObjType() == ObjType.ARROW_LAUNCHER_RIGHT) {
            return arrowLauncher.getHitBox().x < player.getHitBox().x;
        }
        return false;
    }

    // Projectiles/Activators
    private void shootArrow(ArrowLauncher arrowLauncher) {
        Audio.getInstance().getAudioPlayer().playSound(Sounds.ARROW.ordinal());
        Direction direction = (arrowLauncher.getObjType() == ObjType.ARROW_LAUNCHER_RIGHT) ? Direction.LEFT : Direction.RIGHT;
        projectiles.add(new Arrow((int)arrowLauncher.getHitBox().x, (int)arrowLauncher.getHitBox().y, direction));
    }

    public void shootLightningBall(SpearWoman spearWoman) {
        Direction direction = (spearWoman.getFlipSign() == 1) ? Direction.LEFT : Direction.RIGHT;
        projectiles.add(new LightningBall((int)spearWoman.getHitBox().x, (int)spearWoman.getHitBox().y, direction));
    }

    public void multiLightningBallShoot(SpearWoman spearWoman) {
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.1), (int)(spearWoman.getHitBox().y*1.3), Direction.DOWN));
        projectiles.add(new LightningBall((int)spearWoman.getHitBox().x, (int)(spearWoman.getHitBox().y*1.3), Direction.DEGREE_45));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x*1.1), (int)(spearWoman.getHitBox().y*1.2), Direction.DEGREE_30));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.23), (int)(spearWoman.getHitBox().y*1.3), Direction.N_DEGREE_45));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.38), (int)(spearWoman.getHitBox().y*1.2), Direction.N_DEGREE_30));
    }

    public void multiLightningBallShoot2(SpearWoman spearWoman) {
        projectiles.add(new LightningBall((int)spearWoman.getHitBox().x, (int)(spearWoman.getHitBox().y*1.3), Direction.DEGREE_60));
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.15), (int)(spearWoman.getHitBox().y*1.3), Direction.N_DEGREE_60));
    }

    public void followingLightningBallShoot(SpearWoman spearWoman) {
        projectiles.add(new LightningBall((int)(spearWoman.getHitBox().x/1.1), (int)(spearWoman.getHitBox().y*1.3), Direction.TRACK));
    }

    public void activateBlockers(boolean value) {
        for (Blocker blocker : blockers) {
            blocker.setAnimate(value);
            if (!value) blocker.stop();
        }
    }

    // Updates
    private void updateArrowLaunchers(int[][] lvlData, Player player) {
        for (ArrowLauncher arrowLauncher : arrowLaunchers) {
            boolean flag = true;
            if (arrowLauncher.animate) flag = false;
            if ((arrowLauncher.getYTile() < player.getHitBox().y/TILES_SIZE) ||
                    (arrowLauncher.getYTile() > (player.getHitBox().y+player.getHitBox().height)/TILES_SIZE)) {
                flag = false;
            }
            if (!isPlayerInRange(arrowLauncher, player) || !isPlayerInFront(arrowLauncher, player)) flag = false;
            if (!Utils.getInstance().canLauncherSeePlayer(lvlData, player.getHitBox(), arrowLauncher.getHitBox(), arrowLauncher.getYTile())) flag = false;
            if (flag) arrowLauncher.setAnimate(true);
            arrowLauncher.update();
            if (arrowLauncher.getAnimIndex() == 9 && arrowLauncher.getAnimTick() == 0) {
                shootArrow(arrowLauncher);
            }
        }
    }

    private void updateProjectiles(int[][] lvlData, Player player) {
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) {
                projectile.updatePosition(player);
                if (projectile.getHitBox().intersects(player.getHitBox())) {
                    player.changeHealth(-10, projectile);
                    projectile.setAlive(false);
                }
                else if (Utils.getInstance().isProjectileHitLevel(lvlData, projectile)) {
                    projectile.setAlive(false);
                }
            }
        }
    }

    private void updateCoins() {
        for (Coin coin : coins) {
            if (coin.isAlive()) coin.update();
        }
    }

    private void updateShops() {
        for (Shop shop : shops) {
            shop.update();
        }
    }

    private void updateBlockers() {
        for (Blocker blocker : blockers) {
            blocker.update();
        }
    }

    private void updateBlacksmiths() {
        for (Blacksmith blacksmith : blacksmiths) {
            blacksmith.update();
        }
        for (Dog dog : dogs) {
            dog.update();
        }
    }

    // Core
    public void update(int[][] lvlData, Player player) {
        for (Potion potion : potions) if (potion.isAlive()) potion.update();
        for (Container container : containers) if (container.isAlive()) container.update();
        updateObjectInAir();
        updateArrowLaunchers(lvlData, player);
        updateProjectiles(lvlData, player);
        updateCoins();
        updateShops();
        updateBlockers();
        updateBlacksmiths();
        checkEnemyIntersection();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderPotions(g, xLevelOffset, yLevelOffset);
        renderContainers(g, xLevelOffset, yLevelOffset);
        renderTraps(g, xLevelOffset, yLevelOffset);
        renderArrowLaunchers(g, xLevelOffset, yLevelOffset);
        renderProjectiles(g, xLevelOffset, yLevelOffset);
        renderCoins(g, xLevelOffset, yLevelOffset);
        renderShops(g, xLevelOffset, yLevelOffset);
        renderBlockers(g, xLevelOffset, yLevelOffset);
        renderBlacksmiths(g, xLevelOffset, yLevelOffset);
    }

    // Render
    public void generateCoins(Rectangle2D.Double location) {
        Random rand = new Random();
        int n = rand.nextInt(7+PlayerBonus.getInstance().getBonusCoin());
        for (int i = 0; i < n; i++) {
            int x = rand.nextInt((int)location.width)+(int)location.x;
            int y = rand.nextInt((int)(location.height/3)) + (int)location.y + 2*(int)location.height/3;
            Coin coin = new Coin(ObjType.COIN, x, y);
            coins.add(coin);
        }
    }

    private void renderCoins(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Coin c : coins) {
            if (c.isAlive()) {
                int x = (int)c.getHitBox().x-c.getXOffset()-xLevelOffset;
                int y = (int)c.getHitBox().y-c.getYOffset()-yLevelOffset;
                g.drawImage(objects[c.getObjType().ordinal()][c.getAnimIndex()], x, y, COIN_WID, COIN_HEI, null);
                c.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
            }
        }
    }

    private void renderContainers(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Container c : containers) {
            if (c.isAlive()) {
                int x = (int)c.getHitBox().x-c.getXOffset()-xLevelOffset;
                int y = (int)c.getHitBox().y-c.getYOffset()-yLevelOffset;
                g.drawImage(objects[c.getObjType().ordinal()][c.getAnimIndex()], x, y, CONTAINER_WID, CONTAINER_HEI, null);
                c.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
            }
        }
    }

    private void renderPotions(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Potion p : potions) {
            if (p.isAlive()) {
                int x = (int)p.getHitBox().x-p.getXOffset()-xLevelOffset;
                int y = (int)p.getHitBox().y-p.getYOffset()-yLevelOffset;
                g.drawImage(objects[p.getObjType().ordinal()][p.getAnimIndex()], x, y, POTION_WID, POTION_HEI, null);
                p.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
            }
        }
    }
    
    private void renderTraps(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Spike s : spikes) {
            int x = (int)s.getHitBox().x-s.getXOffset()-xLevelOffset;
            int y = (int)s.getHitBox().y-s.getYOffset()-yLevelOffset+(int)(12*SCALE);
            g.drawImage(objects[s.getObjType().ordinal()][4], x, y, SPIKE_WID, SPIKE_HEI, null);
            s.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
        }
    }

    private void renderArrowLaunchers(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (ArrowLauncher al : arrowLaunchers) {
            int fS = 1, fC = 0;
            int sideOffset = 5;
            int index = al.getObjType().ordinal();
            if (al.getObjType() == ObjType.ARROW_LAUNCHER_RIGHT) {
                fS = -1;
                fC = ARROW_LAUNCHER_WID;
                sideOffset = -5;
                index--;
            }
            int x = (int)al.getHitBox().x-al.getXOffset()-xLevelOffset+fC-(int)(sideOffset*SCALE);
            int y = (int)al.getHitBox().y-al.getYOffset()-yLevelOffset+(int)(1*SCALE);
            g.drawImage(objects[index][al.getAnimIndex()], x, y, fS*ARROW_LAUNCHER_WID, ARROW_LAUNCHER_HEI, null);
            al.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        }
    }

    private void renderProjectiles(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Projectile p : projectiles) {
            if (!p.isAlive()) continue;
            int fS = 1, fC = 0;

            // Arrow
            if (p instanceof Arrow) {
                if (p.getDirection() == Direction.LEFT) {
                    fS = -1;
                    fC = ARROW_LAUNCHER_WID;
                }
                int x = (int)p.getHitBox().x-xLevelOffset+fC;
                int y = (int)p.getHitBox().y-yLevelOffset;
                g.drawImage(projectileArrow, x, y, fS*ARROW_WID, ARROW_HEI, null);
            }
            // Lightning Ball
            else {
                BufferedImage object = projectileLightningBall2[p.getAnimIndex()];
                if (p.getDirection() == Direction.LEFT || p.getDirection() == Direction.RIGHT)
                    object = projectileLightningBall[p.getAnimIndex()];
                if (p.getDirection() == Direction.RIGHT) {
                    fS = -1;
                    fC = LB_WID;
                }

                int x = (int)(p.getHitBox().x-xLevelOffset+fC-22*SCALE);
                int y = (int)(p.getHitBox().y-yLevelOffset-20*SCALE);
                g.drawImage(object, x, y, fS*LB_WID, LB_HEI, null);
            }
            p.renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
        }
    }

    private void renderShops(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Shop s : shops) {
            int x = (int)s.getHitBox().x-s.getXOffset()-xLevelOffset;
            int y = (int)s.getHitBox().y-s.getYOffset()-yLevelOffset+(int)(1*SCALE);
            g.drawImage(objects[s.getObjType().ordinal()][s.getAnimIndex()], x, y, SHOP_WID, SHOP_HEI, null);
            s.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
            s.render(g, xLevelOffset, yLevelOffset);
        }
    }

    private void renderBlockers(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Blocker b : blockers) {
            int x = (int)b.getHitBox().x-b.getXOffset()-xLevelOffset;
            int y = (int)b.getHitBox().y-b.getYOffset()-yLevelOffset+(int)(12*SCALE);
            g.drawImage(objects[b.getObjType().ordinal()][b.getAnimIndex()], x, y, BLOCKER_WID, BLOCKER_HEI, null);
            b.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
        }
    }

    private void renderBlacksmiths(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Blacksmith b : blacksmiths) {
            int x = (int)b.getHitBox().x-b.getXOffset()-xLevelOffset;
            int y = (int)b.getHitBox().y-b.getYOffset()-yLevelOffset+(int)(1*SCALE);
            g.drawImage(objects[b.getObjType().ordinal()][b.getAnimIndex()], x, y, BLACKSMITH_WID, BLACKSMITH_HEI, null);
            b.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
            b.render(g, xLevelOffset, yLevelOffset);
        }
        for (Dog d : dogs) {
            int x = (int)d.getHitBox().x-d.getXOffset()-xLevelOffset;
            int y = (int)d.getHitBox().y-d.getYOffset()-yLevelOffset;
            g.drawImage(objects[d.getObjType().ordinal()][d.getAnimIndex()], x, y, DOG_WID, DOG_HEI, null);
            d.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
        }
    }

    // Reset
    public void reset() {
        loadObjects(playingState.getLevelManager().getCurrentLevel());
        for (Potion potion : potions) potion.reset();
        for (Container container : containers) container.reset();
        for (ArrowLauncher arrowLauncher : arrowLaunchers) arrowLauncher.reset();
        coins.clear();
    }

    public boolean isShopVisible() {
        return shopVisible;
    }

    public boolean isBlacksmithVisible() {
        return blacksmithVisible;
    }
}
