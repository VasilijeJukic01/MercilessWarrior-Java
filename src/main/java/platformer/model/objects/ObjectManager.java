package platformer.model.objects;

import platformer.animation.AnimationUtils;
import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.debug.Message;
import platformer.model.Tiles;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.model.levels.Level;
import platformer.model.spells.Flames;
import platformer.state.PlayingState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class ObjectManager {

    private final PlayingState playingState;
    private final BufferedImage[][] objects; // ObjType Dependency
    private final BufferedImage projectileObject;
    private ArrayList<Potion> potions;
    private ArrayList<Container> containers;
    private ArrayList<Spike> spikes;
    private ArrayList<ArrowLauncher> arrowLaunchers;
    private final ArrayList<Projectile> projectiles;

    public ObjectManager(PlayingState playingState) {
        this.playingState = playingState;
        this.objects = AnimationUtils.getInstance().loadObjects();
        this.projectiles = new ArrayList<>();
        this.projectileObject = Utils.getInstance().importImage("src/main/resources/images/objs/arrow.png", (int)PRSet.ARROW_WID.getValue(), (int)PRSet.ARROW_HEI.getValue());
    }

    public void loadObjects(Level level) {
        level.loadObjectData();
        this.potions = new ArrayList<>(level.getPotions());
        this.containers = new ArrayList<>(level.getContainers());
        this.spikes = level.getSpikes();
        this.arrowLaunchers = level.getArrowLaunchers();
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

    public void checkSpikeHit(Player p) {
        for (Spike spike : spikes) {
            if (p.getHitBox().intersects(spike.getHitBox()))
                p.kill();
        }
    }

    public void checkObjectPick(Rectangle2D.Double hitBox) {
        for (Potion potion : potions) {
            if (potion.isAlive() && hitBox.intersects(potion.getHitBox())) {
                potion.setAlive(false);
                applyPotionEffect(potion);
            }
        }
    }

    // Apply Effects
    public void applyPotionEffect(Potion potion) {
        switch (potion.getObjType()) {
            case HEAL_POTION: playingState.getPlayer().changeHealth(ObjValue.HEAL_POTION_VAL.getValue()); break;
            case STAMINA_POTION: playingState.getPlayer().changeStamina(ObjValue.STAMINA_POTION_VAL.getValue()); break;
        }
    }

    // Object Break
    public void checkObjectBreak(Rectangle2D.Double attackBox) {
        Flames flames = playingState.getSpellManager().getFlames();
        for (Container container : containers) {
            boolean isFlame = flames.getHitBox().intersects(container.getHitBox()) && flames.isAlive();
            if (container.isAlive() && !container.animate && (attackBox.intersects(container.getHitBox()) || isFlame)) {
                playingState.getGame().notifyLogger("Player breaks container.", Message.NOTIFICATION);
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

    // Object Touch & Bounds
    public boolean isPlayerTouchingObject() {
        for (Container c : containers) {
            int x = (int)playingState.getPlayer().getHitBox().x, y = (int)playingState.getPlayer().getHitBox().y;
            int width = (int)playingState.getPlayer().getHitBox().width, height = (int)playingState.getPlayer().getHitBox().height;
            if (c.isAlive() && c.getAnimIndex() < 1) {
                if (c.getHitBox().contains(x, y)) return true;
                if (c.getHitBox().contains(x+width, y)) return true;
                if (c.getHitBox().contains(x, y+height)) return true;
                if (c.getHitBox().contains(x+width, y+height)) return true;

                if (c.getHitBox().contains(x, y+height+1)) return true;
                if (c.getHitBox().contains(x+width, y+height+1)) return true;
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
                return c.getHitBox().contains(x+width, y + height - 5);
            }
            else if (axis.equals("Y")) {
                if (c.getHitBox().contains(x, y+height+5)) return true;
                return c.getHitBox().contains(x+width, y+height+5);
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

    public double getYObjectBound(Rectangle2D.Double hitBox, double airSpeed) {
        for (Container c : containers) {
            if (c.isAlive() && checkTouch(c, hitBox, "Y")) {
                if (airSpeed > 0) {
                    if (c.getHitBox().y > hitBox.y) return c.getHitBox().y-hitBox.height;
                    else return hitBox.y;
                }
            }
        }
        return hitBox.y;
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
    }

    // Launchers
    private boolean isPlayerInRange(ArrowLauncher arrowLauncher, Player player) {
        int distance = (int)Math.abs(player.getHitBox().x - arrowLauncher.getHitBox().x);
        return distance <= (int)(Tiles.TILES_SIZE.getValue()) * 5;
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

    private void shoot(ArrowLauncher arrowLauncher) {
        Audio.getInstance().getAudioPlayer().playSound(Sounds.ARROW_SOUND.ordinal());
        Direction direction = (arrowLauncher.getObjType() == ObjType.ARROW_LAUNCHER_RIGHT) ? Direction.LEFT : Direction.RIGHT;
        projectiles.add(new Projectile((int)arrowLauncher.getHitBox().x, (int)arrowLauncher.getHitBox().y, direction));
    }

    private void updateArrowLaunchers(int[][] lvlData, Player player) {
        for (ArrowLauncher arrowLauncher : arrowLaunchers) {
            boolean flag = true;
            if (arrowLauncher.animate) flag = false;
            if ((arrowLauncher.getYTile() < player.getHitBox().y/(Tiles.TILES_SIZE.getValue())) ||
                    (arrowLauncher.getYTile() > (player.getHitBox().y+player.getHitBox().height)/(Tiles.TILES_SIZE.getValue()))) {
                flag = false;
            }
            if (!isPlayerInRange(arrowLauncher, player)) flag = false;
            if (!isPlayerInFront(arrowLauncher, player)) flag = false;
            if (!Utils.getInstance().canLauncherSeePlayer(lvlData, player.getHitBox(), arrowLauncher.getHitBox(), arrowLauncher.getYTile())) flag = false;
            if (flag) arrowLauncher.setAnimate(true);
            arrowLauncher.update();
            if (arrowLauncher.getAnimIndex() == 9 && arrowLauncher.getAnimTick() == 0) {
                shoot(arrowLauncher);
            }
        }
    }

    private void updateProjectiles(int[][] lvlData, Player player) {
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) {
                projectile.updatePosition();
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

    // Core
    public void update(int[][] lvlData, Player player) {
        for (Potion potion : potions) if (potion.isAlive()) potion.update();
        for (Container container : containers) if (container.isAlive()) container.update();
        updateObjectInAir();
        updateArrowLaunchers(lvlData, player);
        updateProjectiles(lvlData, player);
        checkEnemyIntersection();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderPotions(g, xLevelOffset, yLevelOffset);
        renderContainers(g, xLevelOffset, yLevelOffset);
        renderTraps(g, xLevelOffset, yLevelOffset);
        renderArrowLaunchers(g, xLevelOffset, yLevelOffset);
        renderProjectiles(g, xLevelOffset, yLevelOffset);
    }

    // Render
    private void renderContainers(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Container c : containers) {
            if (c.isAlive()) {
                int x = (int)c.getHitBox().x-c.getXOffset()-xLevelOffset;
                int y = (int)c.getHitBox().y-c.getYOffset()-yLevelOffset;
                g.drawImage(objects[c.getObjType().ordinal()][c.getAnimIndex()], x, y, ObjValue.CONTAINER_WID.getValue(), ObjValue.CONTAINER_HEI.getValue(), null);
                c.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
            }
        }
    }

    private void renderPotions(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Potion p : potions) {
            if (p.isAlive()) {
                int x = (int)p.getHitBox().x-p.getXOffset()-xLevelOffset;
                int y = (int)p.getHitBox().y-p.getYOffset()-yLevelOffset;
                g.drawImage(objects[p.getObjType().ordinal()][p.getAnimIndex()], x, y, ObjValue.POTION_WID.getValue(), ObjValue.POTION_HEI.getValue(), null);
                p.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
            }
        }
    }
    
    private void renderTraps(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Spike s : spikes) {
            int x = (int)s.getHitBox().x-s.getXOffset()-xLevelOffset;
            int y = (int)s.getHitBox().y-s.getYOffset()-yLevelOffset+(int)(12*Tiles.SCALE.getValue());
            g.drawImage(objects[s.getObjType().ordinal()][4], x, y, ObjValue.SPIKE_WID.getValue(), ObjValue.SPIKE_HEI.getValue(), null);
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
                fC = ObjValue.ARROW_LAUNCHER_WID.getValue();
                sideOffset = -5;
                index--;
            }
            int x = (int)al.getHitBox().x-al.getXOffset()-xLevelOffset+fC-(int)(sideOffset*Tiles.SCALE.getValue());
            int y = (int)al.getHitBox().y-al.getYOffset()-yLevelOffset+(int)(1*Tiles.SCALE.getValue());
            g.drawImage(objects[index][al.getAnimIndex()], x, y, fS*ObjValue.ARROW_LAUNCHER_WID.getValue(), ObjValue.ARROW_LAUNCHER_HEI.getValue(), null);
            al.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        }
    }

    private void renderProjectiles(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Projectile p : projectiles) {
            if (!p.isAlive()) continue;
            int fS = 1, fC = 0;
            if (p.getDirection() == Direction.LEFT) {
                fS = -1;
                fC = ObjValue.ARROW_LAUNCHER_WID.getValue();
            }
            int x = (int)p.getHitBox().x-xLevelOffset+fC;
            int y = (int)p.getHitBox().y-yLevelOffset;
            g.drawImage(projectileObject, x, y, fS*(int)PRSet.ARROW_WID.getValue(), (int)PRSet.ARROW_HEI.getValue(), null);
            p.renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
        }
    }

    // Reset
    public void reset() {
        loadObjects(playingState.getLevelManager().getCurrentLevel());
        for (Potion potion : potions) potion.reset();
        for (Container container : containers) container.reset();
        for (ArrowLauncher arrowLauncher : arrowLaunchers) arrowLauncher.reset();
    }

}
