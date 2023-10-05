package platformer.controller;

import platformer.core.Account;
import platformer.core.Framework;
import platformer.core.Game;
import platformer.ui.GameSlot;

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
        List<Account> saves = Framework.getInstance().getAllSaves();
        gameSlots.get(0).setAccount(Framework.getInstance().getCloud());
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
            if (Framework.getInstance().getCloud().getName().equals("Default")) return;
            Framework.getInstance().cloudSave();
            gameSlots.get(0).setAccount(Framework.getInstance().getCloud());
        }
        else {
            Framework.getInstance().localSave(slot);
            initSlots();
        }
    }

    private void getSlotAccountData(GameSlot s) {
        Framework.getInstance().getAccount().unload();
        if (s.getAccount() == null) return;
        Framework.getInstance().getAccount().setPerks(s.getAccount().getPerks());
        Framework.getInstance().getAccount().setLevel(s.getAccount().getLevel());
        Framework.getInstance().getAccount().setExp(s.getAccount().getExp());
        Framework.getInstance().getAccount().setCoins(s.getAccount().getCoins());
        Framework.getInstance().getAccount().setSpawn(s.getAccount().getSpawn());
        Framework.getInstance().getAccount().setTokens(s.getAccount().getTokens());
        Framework.getInstance().getAccount().setPlaytime(s.getAccount().getPlaytime());
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
        for (GameSlot s : gameSlots) {
            if (s != selectedSlot) s.setSelected(false);
        }
    }

    public List<GameSlot> getGameSlots() {
        return gameSlots;
    }

}
