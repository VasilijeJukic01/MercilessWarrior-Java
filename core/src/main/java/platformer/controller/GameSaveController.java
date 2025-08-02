package platformer.controller;

import platformer.core.Account;
import platformer.core.Framework;
import platformer.core.Game;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.LevelManager;
import platformer.model.levels.Spawn;
import platformer.state.GameState;
import platformer.state.State;
import platformer.ui.components.slots.GameSlot;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.UI.*;

/**
 * The GameSaveController class is responsible for managing the game's save slots.
 * It provides functionality to initialize slots, load and save game data to slots, and check slot selection based on mouse events.
 * <p>
 * Each slot is represented by a GameSlot object, and all slots are stored in a list.
 * The class also interacts with the Framework and Account classes to handle game data.
 */
public class GameSaveController {

    private final Game game;
    private List<GameSlot> gameSlots;

    public GameSaveController(Game game) {
        this.game = game;
        initSlots();
    }

    // Init
    private void initSlots() {
        this.gameSlots = new ArrayList<>();
        for (int i = 1; i <= GAME_SLOT_CAP; i++) {
            boolean databaseSlot = (i == 1);
            gameSlots.add(new GameSlot(null, GAME_SLOT_X, GAME_SLOT_Y + i * GAME_SLOT_SPACING, databaseSlot, i));
        }
        List<Account> saves = Framework.getInstance().getAllSaves();
        gameSlots.get(0).setAccount(Framework.getInstance().getCloud());
        for (int i = 0; i < saves.size(); i++) {
            if (saves.get(i) != null)
                gameSlots.get(i+1).setAccount(saves.get(i));
        }
    }

    // Operations
    public void loadSlot() {
        for (GameSlot s : gameSlots) {
            if (s.isSelected()) {
                getSlotAccountData(s);
                game.startPlayingState();
                return;
            }
        }
    }

    public void deleteSlot() {
        for (GameSlot s : gameSlots) {
            if (s.isSelected()) {
                if (s.getAccount() == null) return;
                Framework.getInstance().localDelete(s.getIndex() - 1);
                initSlots();
                return;
            }
        }
    }

    public void saveSlot() {
        int slot = -1;
        for (int i = 0; i < GAME_SLOT_CAP; i++) {
            if (gameSlots.get(i).isSelected()) {
                slot = i;
                break;
            }
        }
        configureSpawnPoint();
        if (slot == 0) {
            if (Framework.getInstance().getAccount().isEnableCheats()) return;
            if (Framework.getInstance().getCloud().getName().equals("Default")) return;
            Framework.getInstance().cloudSave();
            gameSlots.get(0).setAccount(Framework.getInstance().getCloud());
        }
        else {
            Framework.getInstance().localSave(slot);
            initSlots();
        }
    }

    private void configureSpawnPoint() {
        State state = game.getCurrentState();
        if (!(state instanceof GameState)) return;
        LevelManager levelManager = ((GameState) state).getLevelManager();
        int currentSpawnId = -1;
        for (Spawn s : Spawn.values()) {
            if (s.getLevelI() == levelManager.getLevelIndexI() && s.getLevelJ() == levelManager.getLevelIndexJ()) {
                currentSpawnId = s.getId();
                break;
            }
        }
        Framework.getInstance().getAccount().setSpawn(currentSpawnId);
    }

    private void getSlotAccountData(GameSlot s) {
        Framework.getInstance().getAccount().unload();

        if (s.isDatabaseSlot()) {
            Logger.getInstance().notify("Cloud save selected. Fetching latest data from server...", Message.INFORMATION);
            Account freshCloudAccount = Framework.getInstance().getStorageStrategy().fetchAccountData(Framework.getInstance().getAccount().getName(), 0);
            Framework.getInstance().getAccount().copyFromSlot(freshCloudAccount);
            Framework.getInstance().getCloud().copyFromSlot(freshCloudAccount);
        }
        else {
            if (s.getAccount() == null) return;
            Framework.getInstance().getAccount().copyFromSlot(s.getAccount());
        }

        if (game.getCurrentState() instanceof GameState gameState) {
            gameState.refreshAllFromAccount();
        }

        Framework.getInstance().getAccount().startGameTimer();
    }

    public void checkSlotSelection(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        GameSlot selectedSlot = null;
        for (GameSlot s : gameSlots) {
            if (s.isPointInSlot(x, y)) {
                s.setSelected(true);
                selectedSlot = s;
            }
        }
        if (selectedSlot != null) {
            for (GameSlot s : gameSlots) {
                if (s != selectedSlot) s.setSelected(false);
            }
        }
    }

    public List<GameSlot> getGameSlots() {
        return gameSlots;
    }

}
