package platformer.controller;

import platformer.core.Framework;
import platformer.core.Game;
import platformer.debug.DebugSettings;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.AttackState;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.Loot;
import platformer.model.gameObjects.objects.Table;
import platformer.state.GameState;
import platformer.state.PlayingState;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

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
    public void keyPressed(KeyEvent e, PlayingState state) {
        if (isBreakableState(state) && e.getKeyCode() != KeyEvent.VK_ESCAPE) return;
        if (state == PlayingState.GAME_OVER && e.getKeyCode() != KeyEvent.VK_ESCAPE) return;

        if (state == PlayingState.GAME_OVER && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameState.reset();
            game.startMenuState();
            return;
        }

        int key = e.getKeyCode();
        if (pressedKeys.contains(key)) return;
        pressedKeys.add(key);
        switch (key) {
            case KeyEvent.VK_UP:
                boolean onWall = player.checkAction(PlayerAction.ON_WALL);
                if (pressedKeys.contains(key) && onWall) {
                    player.removeAction(PlayerAction.JUMP);
                    return;
                }
                player.addAction(PlayerAction.JUMP);
                break;
            case KeyEvent.VK_LEFT:
                player.addAction(PlayerAction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                player.addAction(PlayerAction.RIGHT);
                break;
            case KeyEvent.VK_X:
                if (state != PlayingState.DIALOGUE)
                    player.setPlayerAttackState(AttackState.ATTACK_1);
                break;
            case KeyEvent.VK_C:
                if (pressedKeys.contains(key) && player.getSpellState() != 0) return;
                player.getActionHandler().doSpell();
                break;
            case KeyEvent.VK_V:
                boolean canDash = player.checkAction(PlayerAction.CAN_DASH);
                if (canDash) player.getActionHandler().doDash();
                break;
            case KeyEvent.VK_S:
                boolean block = player.checkAction(PlayerAction.BLOCK);
                if (block) return;
                player.setBlock(true);
                break;
            case KeyEvent.VK_ESCAPE:
                if (isBreakableState(state)) gameState.setOverlay(null);
                else gameState.setOverlay(pause(state));
                break;
            default: break;
        }
    }

    public void keyReleased(KeyEvent e, PlayingState state) {
        if (state == PlayingState.GAME_OVER) return;

        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                player.removeAction(PlayerAction.JUMP);
                player.setCurrentJumps(player.getCurrentJumps()+1);
                break;
            case KeyEvent.VK_RIGHT:
                player.removeAction(PlayerAction.RIGHT);
                player.removeAction(PlayerAction.ON_WALL);
                break;
            case KeyEvent.VK_LEFT:
                player.removeAction(PlayerAction.LEFT);
                player.removeAction(PlayerAction.ON_WALL);
                break;
            case KeyEvent.VK_Q:
                player.setCanTransform(true);
                break;
            case KeyEvent.VK_V:
                player.addAction(PlayerAction.CAN_DASH);
                break;
            case KeyEvent.VK_C:
                if (player.getSpellState() == 1) player.setSpellState(2);
                break;
            case KeyEvent.VK_X:
                if (state == PlayingState.DIALOGUE) gameState.getDialogueManager().updateDialogue();
                break;
            case KeyEvent.VK_Z:
                player.getActionHandler().doFireBall();
                break;
            case KeyEvent.VK_F:
                interact();
                break;
            case KeyEvent.VK_I:
                openInventory();
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
        gameState.getOverlayManager().keyPressed(e);
    }

    // Actions
    private void interact() {
        if (gameState.getActiveState() == PlayingState.DIALOGUE) return;
        Optional<? extends Class<? extends GameObject>> object = Optional.ofNullable(gameState.getObjectManager().getIntersectingObject());
        object.ifPresent(this::handleInteraction);
    }

    private void handleInteraction(Class<? extends GameObject> objectClass) {
        if (objectClass == Loot.class) {
            gameState.setOverlay(PlayingState.LOOTING);
        }
        else if (objectClass == Table.class) {
            gameState.setOverlay(PlayingState.CRAFTING);
        }
        else activateDialogue(objectClass);
    }

    private void activateDialogue(Class<? extends GameObject> objectClass) {
        gameState.setOverlay(PlayingState.DIALOGUE);
        Random random = new Random();
        int index = random.nextInt(gameState.getDialogueManager().getDialogues(objectClass).size());
        List<String> dialogues = gameState.getDialogueManager().getDialogues(objectClass).get(index);
        gameState.getDialogueManager().setDialogueObject(dialogues, objectClass);
    }

    private void openInventory() {
        gameState.setOverlay(PlayingState.INVENTORY);
    }

    private void showHitBox() {
        if (!Framework.getInstance().getAccount().isEnableCheats()) return;
        DebugSettings.getInstance().setDebugMode(!DebugSettings.getInstance().isDebugMode());
        Logger.getInstance().notify("HitBox functionality changed.", Message.WARNING);
    }

    private void activateHealthCheat() {
        if (!Framework.getInstance().getAccount().isEnableCheats()) return;
        player.changeHealth(100);
        Logger.getInstance().notify("Health cheat activated.", Message.WARNING);
    }

    private void activateStaminaCheat() {
        if (!Framework.getInstance().getAccount().isEnableCheats()) return;
        player.changeStamina(100);
        Logger.getInstance().notify("Stamina cheat activated.", Message.WARNING);
    }

    private void activateCoinsCheat() {
        if (!Framework.getInstance().getAccount().isEnableCheats()) return;
        player.changeCoins(99999);
        player.changeUpgradeTokens(50);
        Logger.getInstance().notify("Coins cheat activated.", Message.WARNING);
    }

    // Helper
    private boolean isBreakableState(PlayingState state) {
        PlayingState[] breakableStates = {
                PlayingState.SHOP,
                PlayingState.BLACKSMITH,
                PlayingState.DIALOGUE,
                PlayingState.SAVE,
                PlayingState.INVENTORY,
                PlayingState.CRAFTING,
                PlayingState.LOOTING
        };

        return Arrays.stream(breakableStates).anyMatch(breakableState -> breakableState == state);
    }

    private PlayingState pause(PlayingState state) {
        if (state == PlayingState.PAUSE) return null;
        else return PlayingState.PAUSE;
    }

}
