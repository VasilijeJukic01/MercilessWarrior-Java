package platformer.state;

import platformer.audio.Audio;
import platformer.debug.DebugSettings;
import platformer.debug.Message;
import platformer.model.entities.Cooldown;
import platformer.model.entities.PlayerBonus;
import platformer.model.entities.effects.Particle;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.AttackState;
import platformer.core.Game;
import platformer.model.entities.Player;
import platformer.model.levels.LevelManager;
import platformer.model.objects.ObjectManager;
import platformer.model.perks.PerksManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.OverlayManager;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.TreeSet;

import static platformer.constants.Constants.*;

public class PlayingState extends StateAbstraction implements State {

    private final Set<Integer> pressedKeys = new TreeSet<>();

    private Player player;
    private BufferedImage background;

    // Managers
    private LevelManager levelManager;
    private ObjectManager objectManager;
    private EnemyManager enemyManager;
    private SpellManager spellManager;
    private PerksManager perksManager;
    private OverlayManager overlayManager;

    // Flags
    private boolean paused, gameOver, dying, shopVisible, bmVisible;

    // Borders
    private int xLevelOffset;
    private final int leftBorder = (int)(0.2*GAME_WIDTH);
    private final int rightBorder = (int)(0.8*GAME_WIDTH);
    private int xMaxLevelOffset;

    private int yLevelOffset;
    private final int topBorder = (int)(0.4*GAME_HEIGHT);
    private final int bottomBorder = (int)(0.6*GAME_HEIGHT);
    private int yMaxLevelOffset;

    public PlayingState(Game game) {
        super(game);
        init();
        calculateOffset();
    }

    private void init() {
        this.perksManager = new PerksManager();
        this.background = Utils.getInstance().importImage("src/main/resources/images/background1.jpg", GAME_WIDTH, GAME_HEIGHT);
        this.levelManager = new LevelManager(game, this);
        this.objectManager = new ObjectManager(this);
        this.enemyManager = new EnemyManager(this);
        int playerX = (int)(300 * SCALE), playerY = (int)(250 * SCALE);
        int playerWid = (int)(125 * SCALE), playerHei = (int)(80 * SCALE);
        this.player = new Player(playerX, playerY, playerWid, playerHei, enemyManager, objectManager, game);
        player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        this.overlayManager = new OverlayManager(this);
        this.spellManager = new SpellManager(this);
        loadStartLevel();
        loadData();
    }

    private void loadData() {
        perksManager.loadUnlockedPerks(game.getAccount().getPerks());
    }

    private void calculateOffset() {
        this.xMaxLevelOffset = levelManager.getCurrentLevel().getXMaxLevelOffset();
        this.yMaxLevelOffset = levelManager.getCurrentLevel().getYMaxLevelOffset();
    }

    private void loadStartLevel() {
        enemyManager.loadEnemies(levelManager.getCurrentLevel());
        objectManager.loadObjects(levelManager.getCurrentLevel());
    }

    // Level Flow
    public void loadNextLevel() {
        levelReset();
        levelManager.loadNextLevel();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        calculateOffset();
        overlayManager.reset();
        game.notifyLogger("Next level loaded.", Message.NOTIFICATION);
    }

    public void loadPrevLevel() {
        levelReset();
        levelManager.loadPrevLevel();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        calculateOffset();
        overlayManager.reset();
        game.notifyLogger("Previous level loaded.", Message.NOTIFICATION);
    }

    // Level Borders
    private void xBorderUpdate() {
        int playerXPos = (int)player.getHitBox().x;
        int dx = playerXPos - xLevelOffset;
        if (dx > rightBorder) xLevelOffset += dx-rightBorder;
        else if (dx < leftBorder) xLevelOffset += dx-leftBorder;

        xLevelOffset = Math.max(Math.min(xLevelOffset, xMaxLevelOffset), 0);
    }

    private void yBorderUpdate() {
        int playerYPos = (int)player.getHitBox().y;
        int dy = playerYPos - yLevelOffset;
        if (dy < topBorder) yLevelOffset += dy-topBorder;
        else if (dy > bottomBorder) yLevelOffset += dy-bottomBorder;

        yLevelOffset = Math.max(Math.min(yLevelOffset, yMaxLevelOffset), 0);
    }

    // Core
    @Override
    public void update() {
        if (paused) overlayManager.update("PAUSE");
        else if (gameOver) overlayManager.update("GAME_OVER");
        else if (dying) this.player.update();
        else {
            if (Utils.getInstance().isOnExit(levelManager.getCurrentLevel(), player.getHitBox()) == 1) loadNextLevel();
            else if (Utils.getInstance().isOnExit(levelManager.getCurrentLevel(), player.getHitBox()) == -1) loadPrevLevel();
            for (Particle particle : levelManager.getParticles()) {
                particle.update();
            }
            this.enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player);
            this.objectManager.update(levelManager.getCurrentLevel().getLvlData(), player);
            this.spellManager.update();
            xBorderUpdate();
            yBorderUpdate();
            this.player.update();
            if (shopVisible) overlayManager.update("SHOP");
            if (bmVisible) overlayManager.update("BLACKSMITH");
        }
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(background, 0, 0, null);
        this.levelManager.render(g, xLevelOffset, yLevelOffset);
        this.objectManager.render(g, xLevelOffset, yLevelOffset);
        this.player.render(g, xLevelOffset, yLevelOffset);
        this.enemyManager.render(g, xLevelOffset, yLevelOffset);
        this.spellManager.render(g, xLevelOffset, yLevelOffset);
        this.player.getUserInterface().render(g);
        this.overlayManager.render(g, paused, gameOver, shopVisible, bmVisible);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        overlayManager.mousePressed(e, paused, gameOver, shopVisible, bmVisible);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        overlayManager.mouseReleased(e, paused, gameOver, shopVisible, bmVisible);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        overlayManager.mouseMoved(e, paused, gameOver, shopVisible, bmVisible);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        overlayManager.mouseDragged(e, paused);
    }

    // Input
    @Override
    public void keyPressed(KeyEvent e) {
        if ((shopVisible || bmVisible) && e.getKeyCode() != KeyEvent.VK_ESCAPE) return;
        if (gameOver && e.getKeyCode() != KeyEvent.VK_ESCAPE) return;
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            reset();
            game.startMenuState();
            return;
        }
        int key = e.getKeyCode();
        if (pressedKeys.contains(key)) return;
        pressedKeys.add(key);
        switch (key) {
            case KeyEvent.VK_UP:
                if (pressedKeys.contains(key) && player.isOnWall()) {
                    player.setJump(false);
                    return;
                }
                player.setJump(true);
                break;
            case KeyEvent.VK_LEFT:
                player.setLeft(true);
                break;
            case KeyEvent.VK_RIGHT:
                player.setRight(true);
                break;
            case KeyEvent.VK_X:
                if (player.getCooldown()[Cooldown.ATTACK.ordinal()] != 0) return;
                player.setPlayerAttackState(AttackState.ATTACK_1);
                player.getCooldown()[Cooldown.ATTACK.ordinal()] = 0.75+PlayerBonus.getInstance().getBonusCooldown();
                break;
            case KeyEvent.VK_C:
                if (pressedKeys.contains(key) && player.getSpellState() != 0) return;
                player.doSpell();
                break;
            case KeyEvent.VK_Z:
                player.setPlayerAttackState(AttackState.ATTACK_2);
                break;
            case KeyEvent.VK_V:
                if (player.getCooldown()[Cooldown.DASH.ordinal()] != 0) return;
                if (player.canDash()) player.doDash();
                break;
            case KeyEvent.VK_S:
                if (player.getCooldown()[Cooldown.BLOCK.ordinal()] != 0) return;
                if (player.isBlock()) return;
                player.setBlock(true);
                break;
            case KeyEvent.VK_ESCAPE:
                if (shopVisible || bmVisible) {
                    if (shopVisible) shopVisible = false;
                    else bmVisible = false;
                }
                else paused = !paused;
                if (paused) Audio.getInstance().getAudioPlayer().pauseSounds();
                else Audio.getInstance().getAudioPlayer().unpauseSounds();
                break;
            default: break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) return;
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                player.setJump(false);
                player.setCurrentJumps(player.getCurrentJumps()+1);
                break;
            case KeyEvent.VK_RIGHT:
                player.setRight(false);
                player.setOnWall(false);
                break;
            case KeyEvent.VK_LEFT:
                player.setLeft(false);
                player.setOnWall(false);
                break;
            case KeyEvent.VK_Q:
                player.setCanTransform(true);
                break;
            case KeyEvent.VK_V:
                player.setCanDash(true);
                break;
            case KeyEvent.VK_C:
                if (player.getSpellState() == 1) player.setSpellState(2);
                break;
            case KeyEvent.VK_F:
                if (objectManager.isShopVisible() && !shopVisible) shopVisible = true;
                if (objectManager.isBlacksmithVisible() && !bmVisible) bmVisible = true;
                break;
            case KeyEvent.VK_F6:
                player.saveData();
                game.saveProgress();
                break;
            case KeyEvent.VK_F1: // Show HitBox
                if (!game.getAccount().isEnableCheats()) break;
                DebugSettings.getInstance().setDebugMode(!DebugSettings.getInstance().isDebugMode());
                game.notifyLogger("HitBox functionality changed.", Message.WARNING);
                break;
            case KeyEvent.VK_F2: // Stamina Cheat
                if (!game.getAccount().isEnableCheats()) break;
                player.changeStamina(100);
                game.notifyLogger("Stamina cheat activated.", Message.WARNING);
                break;
            case KeyEvent.VK_F3: // Health Cheat
                if (!game.getAccount().isEnableCheats()) break;
                player.changeHealth(100);
                game.notifyLogger("Health cheat activated.", Message.WARNING);
                break;
            case KeyEvent.VK_F4: // Coins Cheat
                if (!game.getAccount().isEnableCheats()) break;
                player.changeCoins(99999);
                player.changeUpgradeTokens(50);
                game.notifyLogger("Coins cheat activated.", Message.WARNING);
                break;
            default: break;
        }
        pressedKeys.remove(key);
    }

    @Override
    public void reset() {
        gameOver = false;
        paused = false;
        dying = false;
        enemyManager.reset();
        player.reset();
        objectManager.reset();
        spellManager.reset();
    }

    public void levelReset() {
        gameOver = false;
        paused = false;
        dying = false;
        enemyManager.reset();
        objectManager.reset();
    }


    @Override
    public void windowFocusLost(WindowEvent e) {
        player.resetDirections();
    }

    @Override
    public void setPaused(boolean value) {
        this.paused = value;
    }

    @Override
    public void setGameOver(boolean value) {
        this.gameOver = value;
    }

    @Override
    public void setDying(boolean value) {
        this.dying = value;
    }


    // Getters & Setters
    public Player getPlayer() {
        return player;
    }

    public EnemyManager getEnemyManager() {
        return enemyManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    public PerksManager getPerksManager() {
        return perksManager;
    }

    public void setShopVisible(boolean shopVisible) {
        this.shopVisible = shopVisible;
    }

    public void setBmVisible(boolean bmVisible) {
        this.bmVisible = bmVisible;
    }
}
