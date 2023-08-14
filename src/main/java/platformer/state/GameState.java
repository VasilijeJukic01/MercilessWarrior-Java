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
import platformer.model.gameObjects.ObjectManager;
import platformer.model.perks.PerksManager;
import platformer.model.spells.SpellManager;
import platformer.ui.dialogue.DialogueManager;
import platformer.ui.overlays.OverlayManager;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

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
    private DialogueManager dialogueManager;

    // State
    private PlayingState state;

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
        this.enemyManager = new EnemyManager(this);
        this.objectManager = new ObjectManager(this);
        this.overlayManager = new OverlayManager(this);
        this.spellManager = new SpellManager(this);
        this.dialogueManager = new DialogueManager(this);
    }

    private void initPlayer() {
        this.player = new Player(PLAYER_X, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, enemyManager, objectManager, game);
        this.player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        this.player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
    }

    private void loadFromDatabase() {
        this.perksManager.loadUnlockedPerks(game.getAccount().getPerks());
    }

    public void saveToDatabase() {
        game.getAccount().setPerks(perksManager.getUpgradedPerks());
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
        if (state == PlayingState.PAUSE)
            overlayManager.update(PlayingState.PAUSE);
        else if (state == PlayingState.GAME_OVER)
            overlayManager.update(PlayingState.GAME_OVER);
        else if (state == PlayingState.DYING)
            player.update();
        else handleGameState();

        if (state == PlayingState.DIALOGUE)
            overlayManager.update(PlayingState.DIALOGUE);
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
        Arrays.stream(levelManager.getParticles()).forEach(Particle::update);
    }

    private void updateManagers() {
        xBorderUpdate();
        yBorderUpdate();
        enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        objectManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        spellManager.update();
        player.update();
        if (state == PlayingState.SHOP) overlayManager.update(PlayingState.SHOP);
        else if (state == PlayingState.BLACKSMITH) overlayManager.update(PlayingState.BLACKSMITH);
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
        gameStateController.keyPressed(e, state);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        gameStateController.keyReleased(e, state);
    }

    @Override
    public void reset() {
        state = null;
        enemyManager.reset();
        player.reset();
        objectManager.reset();
        spellManager.reset();
    }

    private void levelReset() {
        state = null;
        enemyManager.reset();
        objectManager.reset();
    }

    @Override
    public void windowFocusLost(WindowEvent e) {
        player.resetDirections();
    }

    // Getters & Setters
    public PlayingState getActiveState() {
        if (state == PlayingState.DYING) return null;
        return state;
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

    public DialogueManager getDialogueManager() {
        return dialogueManager;
    }

    public void setOverlay(PlayingState newOverlay) {
        if (state == PlayingState.PAUSE) Audio.getInstance().getAudioPlayer().unpauseSounds();
        this.state = newOverlay;
        if (state == PlayingState.PAUSE) Audio.getInstance().getAudioPlayer().pauseSounds();
    }
}
