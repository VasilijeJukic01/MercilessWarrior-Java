package platformer.state;

import platformer.debug.DebugSettings;
import platformer.model.entities.effects.Particle;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.AttackState;
import platformer.model.Tiles;
import platformer.core.Game;
import platformer.model.entities.Player;
import platformer.model.levels.LevelManager;
import platformer.model.objects.ObjectManager;
import platformer.model.spells.SpellManager;
import platformer.ui.GameOverOverlay;
import platformer.ui.PauseOverlay;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.TreeSet;

public class PlayingState extends StateAbstraction implements State {

    private final Set<Integer> pressedKeys = new TreeSet<>();

    private Player player;
    private BufferedImage background;
    private LevelManager levelManager;
    private ObjectManager objectManager;
    private EnemyManager enemyManager;
    private SpellManager spellManager;

    // Overlays
    private PauseOverlay pauseOverlay;
    private GameOverOverlay gameOverOverlay;

    // Flags
    private boolean paused, gameOver, dying;

    // Borders
    private int xLevelOffset;
    private final int leftBorder = (int)(0.2*Tiles.GAME_WIDTH.getValue());
    private final int rightBorder = (int)(0.8*Tiles.GAME_WIDTH.getValue());
    private int xMaxLevelOffset;

    private int yLevelOffset;
    private final int topBorder = (int)(0.4*Tiles.GAME_HEIGHT.getValue());
    private final int bottomBorder = (int)(0.6*Tiles.GAME_HEIGHT.getValue());
    private int yMaxLevelOffset;

    public PlayingState(Game game) {
        super(game);
        init();
        calculateOffset();
    }

    private void init() {
        this.background = Utils.getInstance().importImage("src/main/resources/images/background1.jpg", (int)Tiles.GAME_WIDTH.getValue(), (int)Tiles.GAME_HEIGHT.getValue());
        this.levelManager = new LevelManager(game, this);
        this.objectManager = new ObjectManager(this);
        this.enemyManager = new EnemyManager(this);
        int playerX = (int)(300 * Tiles.SCALE.getValue()), playerY = (int)(250 * Tiles.SCALE.getValue());
        int playerWid = (int)(125 * Tiles.SCALE.getValue()), playerHei = (int)(80 * Tiles.SCALE.getValue());
        this.player = new Player(playerX, playerY, playerWid, playerHei, enemyManager, objectManager, game);
        player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        this.pauseOverlay = new PauseOverlay(game);
        this.gameOverOverlay = new GameOverOverlay(game);
        this.spellManager = new SpellManager(this);
        loadStartLevel();
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
    }

    public void loadPrevLevel() {
        levelReset();
        levelManager.loadPrevLevel();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        calculateOffset();
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
        if (paused) this.pauseOverlay.update();
        else if (gameOver) this.gameOverOverlay.update();
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
        }
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(background, 0, 0, null);
        this.levelManager.render(g, xLevelOffset, yLevelOffset);
        this.player.render(g, xLevelOffset, yLevelOffset);
        this.objectManager.render(g, xLevelOffset, yLevelOffset);
        this.enemyManager.render(g, xLevelOffset, yLevelOffset);
        this.spellManager.render(g, xLevelOffset, yLevelOffset);
        if (paused) this.pauseOverlay.render(g);
        if (gameOver) this.gameOverOverlay.render(g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (paused) pauseOverlay.mousePressed(e);
        else if (gameOver) gameOverOverlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (paused) pauseOverlay.mouseReleased(e);
        else if (gameOver) gameOverOverlay.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (paused) pauseOverlay.mouseMoved(e);
        else if (gameOver) gameOverOverlay.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (paused) pauseOverlay.mouseDragged(e);
    }

    // Input
    @Override
    public void keyPressed(KeyEvent e) {
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
                player.setPlayerAttackState(AttackState.ATTACK_1);
                break;
            case KeyEvent.VK_C:
                if (pressedKeys.contains(key) && player.getSpellState() != 0) return;
                player.doSpell();
                break;
            case KeyEvent.VK_Z:
                player.setPlayerAttackState(AttackState.ATTACK_2);
                break;
            case KeyEvent.VK_V:
                if (player.canDash()) player.doDash();
                break;
            case KeyEvent.VK_S:
                if (player.isBlock()) return;
                player.setBlock(true);
                break;
            case KeyEvent.VK_ESCAPE:
                paused = !paused;
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
            case KeyEvent.VK_V:
                player.setCanDash(true);
                break;
            case KeyEvent.VK_C:
                if (player.getSpellState() == 1) player.setSpellState(2);
                break;
            case KeyEvent.VK_F1: // Show HitBox
                DebugSettings.getInstance().setDebugMode(!DebugSettings.getInstance().isDebugMode());
                break;
            case KeyEvent.VK_F2: // Stamina Cheat
                player.changeStamina(100);
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
}
