package platformer.state.types;

import platformer.core.Framework;
import platformer.core.Game;
import platformer.model.BoardItem;
import platformer.state.AbstractState;
import platformer.state.State;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.FilePaths.BOARD_TXT;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is viewing the leaderboard.
 * In this state, the player can view the leaderboard which displays the top players based on their level and experience.
 */
public class LeaderboardState extends AbstractState implements State {

    private BufferedImage leaderboardText;
    private SmallButton exitBtn;

    public LeaderboardState(Game game) {
        super(game);
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.leaderboardText = ImageUtils.importImage(BOARD_TXT, BOARD_TXT_WID, BOARD_TXT_HEI);
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        g.drawImage(leaderboardText, BOARD_TXT_X, BOARD_TXT_Y, leaderboardText.getWidth(), leaderboardText.getHeight(), null);
        exitBtn.render(g);
        renderLeaderboard(g);
    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    // Render
    private void renderLeaderboard(Graphics g) {
        List<BoardItem> data = Framework.getInstance().getLeaderboard();
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.setColor(Color.WHITE);
        renderHeaders(g);
        for (int i = 2; i < data.size() + 2; i++) {
            setGraphicsColor(g, i-2);
            g.drawString((i-1)+".  "+data.get(i-2).getName(), BOARD_X1, BOARD_START_Y + (i * BOARD_SPACING));
            g.drawString(data.get(i-2).getLevel(), BOARD_X2, BOARD_START_Y + (i * BOARD_SPACING));
            g.drawString(data.get(i-2).getExp(), BOARD_X3, BOARD_START_Y + (i * BOARD_SPACING));
        }
    }

    private void setGraphicsColor(Graphics g, int index) {
        switch (index) {
            case 0: g.setColor(BOARD_COLOR_TOP); break;
            case 1: g.setColor(BOARD_COLOR_SECOND); break;
            case 2: g.setColor(BOARD_COLOR_THIRD); break;
            default: g.setColor(BOARD_COLOR_TOP_10); break;
        }
    }

    private void renderHeaders(Graphics g) {
        g.drawString("Name", BOARD_X1, BOARD_START_Y);
        g.drawString("Level", BOARD_X2, BOARD_START_Y);
        g.drawString("Exp", BOARD_X3, BOARD_START_Y);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.startMenuState();
        }
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(false);
        if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);
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
        exitBtn.resetMouseSet();
    }

}
