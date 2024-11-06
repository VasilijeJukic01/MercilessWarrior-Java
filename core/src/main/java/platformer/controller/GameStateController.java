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
import platformer.model.gameObjects.npc.Npc;
import platformer.state.GameState;
import platformer.state.PlayingState;
import platformer.ui.dialogue.Dialogue;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * The GameStateController class is responsible for handling game state changes.
 * It provides functionality to handle mouse and keyboard events, and perform actions based on these events.
 * It also interacts with the GameState, Game, and Player classes to manage the game state.
 * <p>
 * The class maintains a set of pressed keys to track which keys are currently pressed.
 */
public class GameStateController {

    private final ActionManager<Integer> pressActions = new ActionManager<>();
    private final ActionManager<Integer> releaseActions = new ActionManager<>();

    private final Set<Integer> pressedKeys = new TreeSet<>();

    private final Game game;
    private final GameState gameState;
    private final Player player;

    public GameStateController(GameState gameState) {
        this.gameState = gameState;
        this.player = gameState.getPlayer();
        this.game = gameState.getGame();
        initPressActions();
        initReleaseActions();
    }

    // Init
    private void initAction(ActionManager<Integer> actionManager, String command, Runnable action) {
        KeyboardController kc = Framework.getInstance().getKeyboardController();
        actionManager.addAction(kc.getKeyForCommand(command), action);
    }

    private void initPressActions() {
        KeyboardController kc = Framework.getInstance().getKeyboardController();

        initAction(pressActions, "Move Left", () -> player.addAction(PlayerAction.LEFT));
        initAction(pressActions, "Move Right", () -> player.addAction(PlayerAction.RIGHT));
        initAction(pressActions, "Jump", () -> {
            boolean onWall = player.checkAction(PlayerAction.ON_WALL);
            if (pressedKeys.contains(kc.getKeyForCommand("Jump")) && onWall) {
                player.removeAction(PlayerAction.JUMP);
                return;
            }
            player.addAction(PlayerAction.JUMP);
        });
        initAction(pressActions, "Dash", () -> {
            boolean canDash = player.checkAction(PlayerAction.CAN_DASH);
            if (canDash) player.getActionHandler().doDash();
        });
        initAction(pressActions, "Attack", () -> {
            if (gameState.getActiveState() != PlayingState.DIALOGUE)
                player.setPlayerAttackState(AttackState.ATTACK_1);
        });
        initAction(pressActions, "Flames", () -> {
            if (pressedKeys.contains(kc.getKeyForCommand("Flames")) && player.getSpellState() != 0) return;
            player.getActionHandler().doSpell();
        });
        initAction(pressActions, "Fireball", () -> player.getActionHandler().doFireBall());
        initAction(pressActions, "Shield", () -> {
            boolean block = player.checkAction(PlayerAction.BLOCK);
            if (block) return;
            player.setBlock(true);
        });
        initAction(pressActions, "Pause", () -> {
            if (isBreakableState(gameState.getActiveState())) gameState.setOverlay(null);
            else gameState.setOverlay(pause(gameState.getActiveState()));
        });
    }

    private void initReleaseActions() {
        initAction(releaseActions, "Move Left", () -> {
            player.removeAction(PlayerAction.LEFT);
            player.removeAction(PlayerAction.ON_WALL);
        });
        initAction(releaseActions, "Move Right", () -> {
            player.removeAction(PlayerAction.RIGHT);
            player.removeAction(PlayerAction.ON_WALL);
        });
        initAction(releaseActions, "Jump", () -> {
            player.removeAction(PlayerAction.JUMP);
            player.setCurrentJumps(player.getCurrentJumps()+1);
        });
        initAction(releaseActions, "Dash", () -> player.addAction(PlayerAction.CAN_DASH));
        initAction(releaseActions, "Transform", () -> player.setCanTransform(true));
        initAction(releaseActions, "Flames", () -> {
            if (player.getSpellState() == 1) player.setSpellState(2);
        });
        initAction(releaseActions, "Attack", () -> {
            if (gameState.getActiveState() == PlayingState.DIALOGUE) gameState.getDialogueManager().updateDialogue();
        });
        initAction(releaseActions, "Fireball", () -> player.getActionHandler().doFireBall());
        initAction(releaseActions, "Interact", this::interact);
        initAction(releaseActions, "Inventory", this::openInventory);
        initAction(releaseActions, "Quest", this::openQuests);
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

        pressActions.execute(key);
    }

    public void keyReleased(KeyEvent e, PlayingState state) {
        if (state == PlayingState.GAME_OVER) return;

        int key = e.getKeyCode();

        releaseActions.execute(key);

        switch (key) {
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
        Optional<String> object = Optional.ofNullable(gameState.getObjectManager().getIntersectingObject());
        object.ifPresent(this::handleInteraction);
    }

    private void handleInteraction(String id) {
        if (Objects.equals(id, "Loot")) {
            gameState.setOverlay(PlayingState.LOOTING);
        }
        else if (Objects.equals(id, "Table")) {
            gameState.setOverlay(PlayingState.CRAFTING);
        }
        else activateDialogue(id);
    }

    private void activateDialogue(String id) {
        gameState.setOverlay(PlayingState.DIALOGUE);
        Random random = new Random();
        int index = random.nextInt(gameState.getDialogueManager().getDialogues(id).size());
        GameObject object = gameState.getObjectManager().getIntersection();
        if (object instanceof Npc) {
            int newIndex = ((Npc) object).getDialogueIndicator();
            index = newIndex == -1 ? index : newIndex;
        }
        List<String> dialogues = gameState.getDialogueManager().getDialogues(id).get(index).getLines();
        gameState.getDialogueManager().setDialogueObject(dialogues, object);
    }

    private void openInventory() {
        gameState.setOverlay(PlayingState.INVENTORY);
    }

    private void openQuests() {
        gameState.setOverlay(PlayingState.QUEST);
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
                PlayingState.QUEST,
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
