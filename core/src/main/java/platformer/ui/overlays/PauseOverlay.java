package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.core.Game;
import platformer.state.GameState;
import platformer.ui.AudioOptions;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final GameState gameState;
    private final AudioOptions audioOptions;
    private BufferedImage pauseText;

    private int currentPage = 0;
    private final Rectangle2D.Double[] tabs = new Rectangle2D.Double[3];

    private SmallButton continueBtn, retryBtn, exitBtn;

    private final SmallButton[] particleButtons = new SmallButton[2];
    private final SmallButton[] shakeButtons = new SmallButton[2];

    private final String[] particleLevels = {"0.25", "0.50", "1.00"};
    private int particleIndex = 2;

    public PauseOverlay(Game game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;
        this.audioOptions = game.getAudioOptions();
        loadImages();
        loadButtons();
        setInitialSettings();
    }

    private void loadImages() {
        this.pauseText = Utils.getInstance().importImage(PAUSE_TXT, PAUSE_TEXT_WID, PAUSE_TEXT_HEI);
    }

    private void loadButtons() {
        continueBtn = new SmallButton(CONTINUE_BTN_X, CONTINUE_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.CONTINUE);
        retryBtn = new SmallButton(RETRY_BTN_X, RETRY_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.RETRY);
        exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
        tabs[0] = new Rectangle2D.Double(scale(320), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        tabs[1] = new Rectangle2D.Double(scale(400), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        tabs[2] = new Rectangle2D.Double(scale(480), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        particleButtons[0] = new SmallButton(scale(390), scale(175), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        particleButtons[1] = new SmallButton(scale(510), scale(175), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
        shakeButtons[0] = new SmallButton(scale(390), scale(225), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        shakeButtons[1] = new SmallButton(scale(510), scale(225), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
    }

    private void setInitialSettings() {
        double density = game.getSettings().getParticleDensity();
        if (density == 0.25) particleIndex = 0;
        else if (density == 0.5) particleIndex = 1;
        else particleIndex = 2;
    }

    @Override
    public void update() {
        switch (currentPage) {
            case 0 -> {
                continueBtn.update();
                retryBtn.update();
                exitBtn.update();
            }
            case 1 -> audioOptions.update();
            case 2 -> {
                Arrays.stream(particleButtons).forEach(SmallButton::update);
                Arrays.stream(shakeButtons).forEach(SmallButton::update);
            }
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
            case 1 -> renderAudioPage(g);
            case 2 -> renderGameplayPage(g);
        }
    }

    private void renderPageTabs(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawImage(pauseText, PAUSE_TEXT_X, PAUSE_TEXT_Y, pauseText.getWidth(), pauseText.getHeight(), null);
        g.drawString("Menu", scale(333), scale(147));
        g.drawString("Audio", scale(413), scale(147));
        g.drawString("Gameplay", scale(480), scale(147));

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(144, 9, 97));
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

    private void renderAudioPage(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_BIG));
        g.drawString("Volume", scale(390), scale(195));
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("SFX", scale(310), scale(224));
        g.drawString("Music", scale(310), scale(274));
        audioOptions.render(g);
    }

    private void renderGameplayPage(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

        g.drawString("Particle Density", scale(290), scale(192));
        g.drawString(particleLevels[particleIndex], scale(452), scale(192));
        Arrays.stream(particleButtons).forEach(b -> b.render(g));

        g.drawString("Screen Shake", scale(290), scale(242));
        String shakeStatus = game.getSettings().isScreenShake() ? "ON" : "OFF";
        g.drawString(shakeStatus, scale(452), scale(242));
        Arrays.stream(shakeButtons).forEach(b -> b.render(g));
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
            case 1 -> audioOptions.mousePressed(e);
            case 2 -> {
                Arrays.stream(particleButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMousePressed(true));
                Arrays.stream(shakeButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMousePressed(true));
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (currentPage) {
            case 0 -> handleMenuButtonRelease(e);
            case 1 -> audioOptions.mouseReleased(e);
            case 2 -> handleGameplayButtonRelease(e);
        }
        reset();
    }

    private void handleMenuButtonRelease(MouseEvent e) {
        if (isMouseInButton(e, continueBtn) && continueBtn.isMousePressed()) {
            gameState.setOverlay(null);
        }
        else if (isMouseInButton(e, retryBtn) && retryBtn.isMousePressed()) {
            game.reset();
            Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
        }
        else if (isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            gameState.setOverlay(null);
            game.startMenuState();
        }
    }

    private void handleGameplayButtonRelease(MouseEvent e) {
        for (int i = 0; i < particleButtons.length; i++) {
            if (isMouseInButton(e, particleButtons[i]) && particleButtons[i].isMousePressed()) {
                changeParticleDensity(i == 1);
            }
        }
        for (SmallButton shakeButton : shakeButtons) {
            if (isMouseInButton(e, shakeButton) && shakeButton.isMousePressed()) {
                toggleScreenShake();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        List<AbstractButton> allButtons = new ArrayList<>();
        allButtons.add(continueBtn);
        allButtons.add(retryBtn);
        allButtons.add(exitBtn);
        allButtons.addAll(Arrays.asList(particleButtons));
        allButtons.addAll(Arrays.asList(shakeButtons));

        allButtons.forEach(b -> b.setMouseOver(false));
        allButtons.stream().filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMouseOver(true));

        if (currentPage == 1) audioOptions.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentPage == 1) audioOptions.mouseDragged(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    private void changeParticleDensity(boolean next) {
        if (next) particleIndex = (particleIndex + 1) % particleLevels.length;
        else particleIndex = (particleIndex - 1 + particleLevels.length) % particleLevels.length;
        double density = switch (particleIndex) {
            case 0 -> 0.25;
            case 1 -> 0.5;
            default -> 1.0;
        };
        game.getSettings().setParticleDensity(density);
    }

    private void toggleScreenShake() {
        boolean currentSetting = game.getSettings().isScreenShake();
        game.getSettings().setScreenShake(!currentSetting);
    }

    @Override
    public void reset() {
        continueBtn.resetMouseSet();
        retryBtn.resetMouseSet();
        exitBtn.resetMouseSet();
        Arrays.stream(particleButtons).forEach(SmallButton::resetMouseSet);
        Arrays.stream(shakeButtons).forEach(SmallButton::resetMouseSet);
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}
}
