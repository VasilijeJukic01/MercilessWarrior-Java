package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.core.Game;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.options.ControlsPanel;
import platformer.ui.options.GameSettingsPanel;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * PauseOverlay class is an overlay that is displayed when the game is paused.
 * It allows the player to interact with pause game options.
 */
public class PauseOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final Game game;
    private final GameSettingsPanel settingsPanel;
    private final ControlsPanel controlsPanel;
    private BufferedImage pauseText;

    private int currentPage = 0;
    private final Rectangle2D.Double[] tabs = new Rectangle2D.Double[4];

    private SmallButton continueBtn, retryBtn, exitBtn;

    public PauseOverlay(Game game) {
        this.game = game;
        this.settingsPanel = new GameSettingsPanel(game);
        this.controlsPanel = new ControlsPanel(this.getClass());
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.pauseText = ImageUtils.importImage(PAUSE_TXT, PAUSE_TEXT_WID, PAUSE_TEXT_HEI);
    }

    private void loadButtons() {
        continueBtn = new SmallButton(CONTINUE_BTN_X, CONTINUE_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.CONTINUE);
        retryBtn = new SmallButton(RETRY_BTN_X, RETRY_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.RETRY);
        exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
        tabs[0] = new Rectangle2D.Double(scale(305), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        tabs[1] = new Rectangle2D.Double(scale(365), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        tabs[2] = new Rectangle2D.Double(scale(425), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        tabs[3] = new Rectangle2D.Double(scale(485), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
    }

    @Override
    public void update() {
        switch (currentPage) {
            case 0 -> {
                continueBtn.update();
                retryBtn.update();
                exitBtn.update();
            }
            case 1 -> settingsPanel.updateAudio();
            case 2 -> settingsPanel.updateGameplay();
            case 3 -> controlsPanel.update();
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        OverlayLayer.getInstance().renderOverlay(g);
        renderPageTabs(g);

        switch (currentPage) {
            case 0 -> renderMenuPage(g);
            case 1 -> settingsPanel.renderAudioPage(g);
            case 2 -> settingsPanel.renderGameplayPage(g);
            case 3 -> controlsPanel.render(g);
        }
    }

    private void renderPageTabs(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawImage(pauseText, PAUSE_TEXT_X, PAUSE_TEXT_Y, pauseText.getWidth(), pauseText.getHeight(), null);
        g.drawString("Menu", scale(318), scale(147));
        g.drawString("Audio", scale(377), scale(147));
        g.drawString("Gameplay", scale(425), scale(147));
        g.drawString("Controls", scale(490), scale(147));

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(TAB_COLOR);
        g2d.setStroke(new BasicStroke(2));
        Rectangle2D.Double activeTab = tabs[currentPage];
        g2d.drawLine((int)activeTab.x, (int)(activeTab.y + activeTab.height), (int)(activeTab.x + activeTab.width), (int)(activeTab.y + activeTab.height));
        g2d.setStroke(new BasicStroke(1));
    }

    private void renderMenuPage(Graphics g) {
        continueBtn.render(g);
        retryBtn.render(g);
        exitBtn.render(g);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i].contains(e.getPoint())) {
                currentPage = i;
                return;
            }
        }
        switch (currentPage) {
            case 0 -> {
                if (isMouseInButton(e, continueBtn)) continueBtn.setMousePressed(true);
                else if (isMouseInButton(e, retryBtn)) retryBtn.setMousePressed(true);
                else if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
            }
            case 1 -> settingsPanel.mousePressedAudio(e);
            case 2 -> settingsPanel.mousePressedGameplay(e);
            case 3 -> controlsPanel.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (currentPage) {
            case 0 -> handleMenuButtonRelease(e);
            case 1 -> settingsPanel.mouseReleasedAudio(e);
            case 2 -> settingsPanel.mouseReleasedGameplay(e);
            case 3 -> controlsPanel.mouseReleased(e);
        }
        reset();
    }

    private void handleMenuButtonRelease(MouseEvent e) {
        if (isMouseInButton(e, continueBtn) && continueBtn.isMousePressed()) {
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
        }
        else if (isMouseInButton(e, retryBtn) && retryBtn.isMousePressed()) {
            game.reset();
            Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
        }
        else if (isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
            game.startMenuState();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        List<AbstractButton> allButtons = new ArrayList<>();
        allButtons.add(continueBtn);
        allButtons.add(retryBtn);
        allButtons.add(exitBtn);

        allButtons.forEach(b -> b.setMouseOver(false));
        allButtons.stream().filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMouseOver(true));

        if (currentPage == 1) settingsPanel.mouseMovedAudio(e);
        else if (currentPage == 2) settingsPanel.mouseMovedGameplay(e);
        else if (currentPage == 3) controlsPanel.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentPage == 1) settingsPanel.mouseDraggedAudio(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentPage == 3) controlsPanel.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void reset() {
        continueBtn.resetMouseSet();
        retryBtn.resetMouseSet();
        exitBtn.resetMouseSet();
        settingsPanel.resetAudio();
        settingsPanel.resetGameplay();
        controlsPanel.reset();
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}
}
