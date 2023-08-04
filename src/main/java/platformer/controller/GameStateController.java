package platformer.controller;

import platformer.core.Game;
import platformer.debug.DebugSettings;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.AttackState;
import platformer.model.entities.player.Player;
import platformer.state.GameState;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.TreeSet;

public class GameStateController {

    private final Set<Integer> pressedKeys = new TreeSet<>();

    private final Game game;
    private final GameState gameState;
    private final Player player;

    public GameStateController(GameState gameState) {
        this.gameState = gameState;
        this.player = gameState.getPlayer();
        this.game = gameState.getGame();
    }

    // Mouse
    public void mousePressed(MouseEvent e) {
        gameState.getOverlayManager().mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        gameState.getOverlayManager().mouseReleased(e);
    }

    public void mouseMoved(MouseEvent e) {
        gameState.getOverlayManager().mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e) {
        gameState.getOverlayManager().mouseDragged(e);
    }

    // Keyboard
    public void keyPressed(KeyEvent e, boolean shopVisible, boolean blacksmithVisible, boolean paused, boolean gameOver) {
        if ((shopVisible || blacksmithVisible) && e.getKeyCode() != KeyEvent.VK_ESCAPE) return;
        if (gameOver && e.getKeyCode() != KeyEvent.VK_ESCAPE) return;

        if (gameOver && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameState.reset();
            gameState.getGame().startMenuState();
            return;
        }

        int key = e.getKeyCode();
        if (pressedKeys.contains(key)) return;
        pressedKeys.add(key);
        switch (key) {
            case KeyEvent.VK_UP:
                if (pressedKeys.contains(key) && player.isOnWall()) {
                    gameState.getPlayer().setJump(false);
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
                if (shopVisible || blacksmithVisible) {
                    if (shopVisible) gameState.setShopVisible(false);
                    else gameState.setBlacksmithVisible(false);
                }
                else gameState.setPaused(!paused);
                break;
            default: break;
        }
    }

    public void keyReleased(KeyEvent e, boolean gameOver, boolean shopVisible, boolean blacksmithVisible) {
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
                interact(shopVisible, blacksmithVisible);
                break;
            case KeyEvent.VK_F6:
                saveToDatabase();
                break;
            case KeyEvent.VK_F1:
                showHitBox();
                break;
            case KeyEvent.VK_F2:
                activateStaminaCheat();
                break;
            case KeyEvent.VK_F3:
                activateHealthCheat();
                break;
            case KeyEvent.VK_F4:
                activateCoinsCheat();
                break;
            default: break;
        }
        pressedKeys.remove(key);
    }

    // Actions
    private void interact(boolean shopVisible, boolean blacksmithVisible) {
        if (gameState.getObjectManager().isShopVisible() && !shopVisible) gameState.setShopVisible(true);
        if (gameState.getObjectManager().isBlacksmithVisible() && !blacksmithVisible) gameState.setBlacksmithVisible(true);
    }

    private void saveToDatabase() {
        player.getPlayerStatusManager().saveData();
        game.saveProgress();
    }

    private void showHitBox() {
        if (!game.getAccount().isEnableCheats()) return;
        DebugSettings.getInstance().setDebugMode(!DebugSettings.getInstance().isDebugMode());
        Logger.getInstance().notify("HitBox functionality changed.", Message.WARNING);
    }

    private void activateHealthCheat() {
        if (!game.getAccount().isEnableCheats()) return;
        player.changeHealth(100);
        Logger.getInstance().notify("Health cheat activated.", Message.WARNING);
    }

    private void activateStaminaCheat() {
        if (!game.getAccount().isEnableCheats()) return;
        player.changeStamina(100);
        Logger.getInstance().notify("Stamina cheat activated.", Message.WARNING);
    }

    private void activateCoinsCheat() {
        if (!game.getAccount().isEnableCheats()) return;
        player.changeCoins(99999);
        player.changeUpgradeTokens(50);
        Logger.getInstance().notify("Coins cheat activated.", Message.WARNING);
    }

}
