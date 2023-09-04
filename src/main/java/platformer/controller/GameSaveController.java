package platformer.controller;

import platformer.core.Account;
import platformer.core.Game;
import platformer.serialization.GameSlot;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.UI.*;
import static platformer.constants.UI.GAME_SLOT_SPACING;

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
            gameSlots.add(new GameSlot(null, GAME_SLOT_X, GAME_SLOT_Y + i * GAME_SLOT_SPACING, databaseSlot));
        }
        List<Account> saves = game.getAllSaves();
        gameSlots.get(0).setAccount(game.getCloud());
        for (int i = 0; i < saves.size(); i++) {
            gameSlots.get(i+1).setAccount(saves.get(i));
        }
    }

    // Operations
    public void loadSlot() {
        for (GameSlot s : gameSlots) {
            if (s.isSelected()) {
                getSlotAccountData(s);
                game.startPlayingState();
                game.reloadSave();
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
        if (slot == 0) {
            if (game.getCloud().getName().equals("Default")) return;
            game.cloudSave();
            gameSlots.get(0).setAccount(game.getCloud());
        }
        else {
            game.localSave(slot);
            initSlots();
        }
    }

    private void getSlotAccountData(GameSlot s) {
        game.getAccount().unload();
        if (s.getAccount() == null) return;
        game.getAccount().setPerks(s.getAccount().getPerks());
        game.getAccount().setLevel(s.getAccount().getLevel());
        game.getAccount().setExp(s.getAccount().getExp());
        game.getAccount().setCoins(s.getAccount().getCoins());
        game.getAccount().setSpawn(s.getAccount().getSpawn());
        game.getAccount().setTokens(s.getAccount().getTokens());
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
        for (GameSlot s : gameSlots) {
            if (s != selectedSlot) s.setSelected(false);
        }
    }

    public List<GameSlot> getGameSlots() {
        return gameSlots;
    }

}
