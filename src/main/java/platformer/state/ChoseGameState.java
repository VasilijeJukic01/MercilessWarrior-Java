package platformer.state;

import platformer.core.Account;
import platformer.core.Game;
import platformer.serialization.GameSlot;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.CREButton;
import platformer.ui.overlays.OverlayLayer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.UI.*;
import static platformer.constants.UI.CONTINUE_BTN_Y;

public class ChoseGameState extends AbstractState implements State {

    private CREButton playBtn, exitBtn;
    private List<GameSlot> gameSlots;

    public ChoseGameState(Game game) {
        super(game);
        loadButtons();
        initSlots();
    }

    // Init
    private void loadButtons() {
        this.playBtn = new CREButton(CONTINUE_BTN_X, CONTINUE_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.CONTINUE);
        this.exitBtn = new CREButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

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

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        playBtn.update();
        exitBtn.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        renderSlots(g);
        playBtn.render(g);
        exitBtn.render(g);
    }

    private void renderSlots(Graphics g) {
        gameSlots.forEach(s -> s.render(g));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        else if (isMouseInButton(e, playBtn)) playBtn.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.startMenuState();
        }
        else if(isMouseInButton(e, playBtn) && playBtn.isMousePressed()) {
            for (GameSlot s : gameSlots) {
                if (s.isSelected()) {
                    getSlotAccountData(s);
                    game.startPlayingState();
                    game.reloadSave();
                    reset();
                    return;
                }
            }
        }
        checkSlotSelection(e);
        reset();
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

    private void checkSlotSelection(MouseEvent e) {
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

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(false);
        playBtn.setMouseOver(false);
        if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);
        else if (isMouseInButton(e, playBtn)) playBtn.setMouseOver(true);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) game.startMenuState();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }

    @Override
    public void reset() {
        playBtn.resetMouseSet();
        exitBtn.resetMouseSet();
    }

}
