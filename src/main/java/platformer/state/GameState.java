package platformer.state;

import platformer.audio.Audio;
import platformer.controller.GameStateController;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.effects.Particle;
import platformer.model.entities.enemies.EnemyManager;
import platformer.core.Game;
import platformer.model.entities.player.Player;
import platformer.model.levels.LevelManager;
import platformer.model.objects.ObjectManager;
import platformer.model.perks.PerksManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.OverlayManager;
import platformer.ui.overlays.OverlayType;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.BACKGROUND_1;

public class GameState extends AbstractState implements State {

    private Player player;
    private BufferedImage background;

    private final GameStateController gameStateController;

    // Managers
    private LevelManager levelManager;
    private ObjectManager objectManager;
    private EnemyManager enemyManager;
    private SpellManager spellManager;
    private PerksManager perksManager;
    private OverlayManager overlayManager;

    // Flags
    private boolean paused, gameOver, dying, shopVisible, blacksmithVisible;

    // Borders
    private int xLevelOffset;
    private int xMaxLevelOffset;
    private int yLevelOffset;
    private int yMaxLevelOffset;

    public GameState(Game game) {
        super(game);
        init();
        this.gameStateController = new GameStateController(this);
    }

    // Init
    private void init() {
        this.background = Utils.getInstance().importImage(BACKGROUND_1, GAME_WIDTH, GAME_HEIGHT);
        initManagers();
        initPlayer();
        loadStartLevel();
        loadFromDatabase();
        calculateLevelOffset();
    }

    private void initManagers() {
        this.perksManager = new PerksManager();
        this.levelManager = new LevelManager(this);
        this.objectManager = new ObjectManager(this);
        this.enemyManager = new EnemyManager(this);
        this.overlayManager = new OverlayManager(this);
        this.spellManager = new SpellManager(this);
    }

    private void initPlayer() {
        this.player = new Player(PLAYER_X, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, enemyManager, objectManager, game);
        this.player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        this.player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
    }

    private void loadFromDatabase() {
        this.perksManager.loadUnlockedPerks(game.getAccount().getPerks());
    }

    private void calculateLevelOffset() {
        this.xMaxLevelOffset = levelManager.getCurrentLevel().getXMaxLevelOffset();
        this.yMaxLevelOffset = levelManager.getCurrentLevel().getYMaxLevelOffset();
    }

    private void loadStartLevel() {
        this.enemyManager.loadEnemies(levelManager.getCurrentLevel());
        this.objectManager.loadObjects(levelManager.getCurrentLevel());
    }

    // Level Flow
    public void goToNextLevel() {
        levelManager.loadNextLevel();
        levelLoadReset();
        Logger.getInstance().notify("Next level loaded.", Message.NOTIFICATION);
    }

    public void goToPrevLevel() {
        levelManager.loadPrevLevel();
        levelLoadReset();
        Logger.getInstance().notify("Previous level loaded.", Message.NOTIFICATION);
    }

    private void levelLoadReset() {
        levelReset();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        calculateLevelOffset();
        overlayManager.reset();
    }

    // Level Borders
    private void xBorderUpdate() {
        int playerXPos = (int)player.getHitBox().x;
        int dx = playerXPos - xLevelOffset;

        if (dx > RIGHT_BORDER) xLevelOffset += dx - RIGHT_BORDER;
        else if (dx < LEFT_BORDER) xLevelOffset += dx - LEFT_BORDER;
        xLevelOffset = Math.max(Math.min(xLevelOffset, xMaxLevelOffset), 0);
    }

    private void yBorderUpdate() {
        int playerYPos = (int)player.getHitBox().y;
        int dy = playerYPos - yLevelOffset;

        if (dy < TOP_BORDER) yLevelOffset += dy - TOP_BORDER;
        else if (dy > BOTTOM_BORDER) yLevelOffset += dy - BOTTOM_BORDER;
        yLevelOffset = Math.max(Math.min(yLevelOffset, yMaxLevelOffset), 0);
    }

    // Core
    @Override
    public void update() {
        if (paused)
            overlayManager.update(OverlayType.PAUSE);
        else if (gameOver)
            overlayManager.update(OverlayType.GAME_OVER);
        else if (dying)
            player.update();
        else handleGameState();
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(background, 0, 0, null);
        this.levelManager.render(g, xLevelOffset, yLevelOffset);
        this.objectManager.render(g, xLevelOffset, yLevelOffset);
        this.player.render(g, xLevelOffset, yLevelOffset);
        this.enemyManager.render(g, xLevelOffset, yLevelOffset);
        this.spellManager.render(g, xLevelOffset, yLevelOffset);
        this.player.getPlayerStatusManager().getUserInterface().render(g);
        this.overlayManager.render(g);
    }

    private void handleGameState() {
        checkLevelExit();
        updateParticles();
        updateManagers();
    }

    private void checkLevelExit() {
        int exitStatus = Utils.getInstance().isEntityOnExit(levelManager.getCurrentLevel(), player.getHitBox());
        if (exitStatus == 1) goToNextLevel();
        else if (exitStatus == -1) goToPrevLevel();
    }

    private void updateParticles() {
        for (Particle particle : levelManager.getParticles()) {
            particle.update();
        }
    }

    private void updateManagers() {
        xBorderUpdate();
        yBorderUpdate();
        enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        objectManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        spellManager.update();
        player.update();
        if (shopVisible) overlayManager.update(OverlayType.SHOP);
        if (blacksmithVisible) overlayManager.update(OverlayType.BLACKSMITH);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        gameStateController.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        gameStateController.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gameStateController.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gameStateController.mouseDragged(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        gameStateController.keyPressed(e, shopVisible, blacksmithVisible, paused, gameOver);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        gameStateController.keyReleased(e, gameOver, shopVisible, blacksmithVisible);
    }

    @Override
    public void reset() {
        gameOver = paused = dying = false;
        enemyManager.reset();
        player.reset();
        objectManager.reset();
        spellManager.reset();
    }

    private void levelReset() {
        gameOver = paused = dying = false;
        enemyManager.reset();
        objectManager.reset();
    }

    @Override
    public void windowFocusLost(WindowEvent e) {
        player.resetDirections();
    }

    // Getters & Setters
    public OverlayType getActiveOverlay() {
        if (paused) return OverlayType.PAUSE;
        else if (gameOver) return OverlayType.GAME_OVER;
        else if (shopVisible) return OverlayType.SHOP;
        else if (blacksmithVisible) return OverlayType.BLACKSMITH;
        return null;
    }

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

    public OverlayManager getOverlayManager() {
        return overlayManager;
    }

    public void setPaused(boolean value) {
        this.paused = value;
        if (paused) Audio.getInstance().getAudioPlayer().pauseSounds();
        else Audio.getInstance().getAudioPlayer().unpauseSounds();
    }

    public void setGameOver(boolean value) {
        this.gameOver = value;
    }

    public void setDying(boolean value) {
        this.dying = value;
    }

    public void setShopVisible(boolean shopVisible) {
        this.shopVisible = shopVisible;
    }

    public void setBlacksmithVisible(boolean blacksmithVisible) {
        this.blacksmithVisible = blacksmithVisible;
    }
}
