package platformer.state;

import platformer.audio.Audio;
import platformer.controller.GameStateController;
import platformer.core.Framework;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.effects.LightManager;
import platformer.model.entities.effects.Particle;
import platformer.model.entities.enemies.EnemyManager;
import platformer.core.Game;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.perks.PerksBonus;
import platformer.model.levels.LevelManager;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.perks.PerksManager;
import platformer.model.spells.SpellManager;
import platformer.ui.dialogue.DialogueManager;
import platformer.ui.overlays.OverlayManager;
import platformer.ui.overlays.hud.BossInterface;
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
    private LightManager lightManager;

    // State
    private PlayingState state;

    // Borders
    private int xLevelOffset;
    private int xMaxLevelOffset;
    private int yLevelOffset;
    private int yMaxLevelOffset;

    private BossInterface bossInterface;

    public GameState(Game game) {
        super(game);
        init();
        this.gameStateController = new GameStateController(this);
    }

    // Init
    private void init() {
        this.background = Utils.getInstance().importImage(BACKGROUND_1, GAME_WIDTH, GAME_HEIGHT);
        this.bossInterface = new BossInterface();
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
        this.lightManager = new LightManager(this);
    }

    private void initPlayer() {
        this.player = new Player(PLAYER_X, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, enemyManager, objectManager);
        this.player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        this.player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn("LEFT"));
    }

    // Save
    private void loadFromDatabase() {
        this.perksManager.loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
    }

    public void reloadSave() {
        reset();
        PerksBonus.getInstance().reset();
        this.perksManager = new PerksManager();
        this.perksManager.loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
        this.player.getPlayerDataManager().loadPlayerData();
        this.player.getInventory().reset();
        this.levelManager.loadSavePoint(Framework.getInstance().getAccount().getSpawn());
        this.overlayManager.reset();
        calculateLevelOffset();
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
    private void goToLevel(int I, int J, String message) {
        levelManager.loadNextLevel(I, J);
        String spawn = "";
        if (I == 0 && J == 1) spawn = "LEFT";
        else if (I == 0 && J == -1) spawn = "RIGHT";
        else if (I == -1 && J == 0) spawn = "BOTTOM";
        else if (I == 1 && J == 0) spawn = "UPPER";
        levelLoadReset(spawn);
        Logger.getInstance().notify(message, Message.NOTIFICATION);
    }

    public void goToRightLevel() {
        goToLevel(0, 1, "Right level loaded.");
    }

    public void goToLeftLevel() {
        goToLevel(0, -1, "Left level loaded.");
    }

    public void goToUpperLevel() {
        goToLevel(-1, 0, "Upper level loaded.");
    }

    public void goToBottomLevel() {
        goToLevel(1, 0, "Bottom level loaded.");
    }

    private void levelLoadReset(String spawn) {
        levelReset();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn(spawn));
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
        checkPlayerDeath();
        if (state == PlayingState.PAUSE)
            overlayManager.update(PlayingState.PAUSE);
        else if (state == PlayingState.SAVE)
            overlayManager.update(PlayingState.SAVE);
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
        this.enemyManager.render(g, xLevelOffset, yLevelOffset);
        this.lightManager.render(g, xLevelOffset, yLevelOffset);
        this.player.render(g, xLevelOffset, yLevelOffset);
        this.spellManager.render(g, xLevelOffset, yLevelOffset);
        this.player.getPlayerStatusManager().getUserInterface().render(g);
        this.bossInterface.render(g);
        this.overlayManager.render(g);
    }

    private void handleGameState() {
        try {
            checkLevelExit();
            updateParticles();
            updateManagers();
        }
        catch (Exception ignored) {}
    }

    private void checkLevelExit() {
        int exitStatus = Utils.getInstance().isEntityOnExit(levelManager.getCurrentLevel(), player.getHitBox());

        if (exitStatus == RIGHT_EXIT) goToRightLevel();
        else if (exitStatus == LEFT_EXIT) goToLeftLevel();
        else if (exitStatus == UPPER_EXIT) goToUpperLevel();
        else if (exitStatus == BOTTOM_EXIT) goToBottomLevel();
    }

    private void checkPlayerDeath() {
        boolean dying = player.checkAction(PlayerAction.DYING);
        boolean gameOver = player.checkAction(PlayerAction.GAME_OVER);
        if (dying) setOverlay(PlayingState.DYING);
        if (gameOver) setOverlay(PlayingState.GAME_OVER);
    }

    private void updateParticles() {
        Arrays.stream(levelManager.getParticles()).forEach(Particle::update);
    }

    private void updateManagers() {
        xBorderUpdate();
        yBorderUpdate();
        enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        objectManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        lightManager.update();
        spellManager.update();
        player.update();
        if (state == PlayingState.SHOP) overlayManager.update(PlayingState.SHOP);
        else if (state == PlayingState.BLACKSMITH) overlayManager.update(PlayingState.BLACKSMITH);
        else if (state == PlayingState.INVENTORY) overlayManager.update(PlayingState.INVENTORY);
        else if (state == PlayingState.LOOTING) overlayManager.update(PlayingState.LOOTING);
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
        overlayManager.reset();
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

    public LightManager getLightManager() {
        return lightManager;
    }

    public BossInterface getBossInterface() {
        return bossInterface;
    }

    public void setOverlay(PlayingState newOverlay) {
        if (state == PlayingState.PAUSE) Audio.getInstance().getAudioPlayer().unpauseSounds();
        this.state = newOverlay;
        if (state == PlayingState.PAUSE) Audio.getInstance().getAudioPlayer().pauseSounds();
    }
}
