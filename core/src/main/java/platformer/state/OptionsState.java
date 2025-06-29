package platformer.state;

import platformer.core.Framework;
import platformer.core.Game;
import platformer.ui.AudioOptions;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is in the options menu.
 * In this state, the player can adjust various settings such as audio options.
 */
@SuppressWarnings("FieldCanBeLocal")
public class OptionsState extends AbstractState implements State {

    private int currentPage = 0;

    private final AudioOptions audioOptions;
    private BufferedImage optionsText;

    private final Rectangle2D.Double[] tabs = new Rectangle2D.Double[2];

    private final SmallButton[] particleButtons = new SmallButton[2];
    private final SmallButton[] shakeButtons = new SmallButton[2];
    private SmallButton exitBtn;

    private final String[] particleLevels = {"0.25", "0.50", "1.00"};
    private int particleIndex = 2;

    public OptionsState(Game game) {
        super(game);
        this.audioOptions = game.getAudioOptions();
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.optionsText = Utils.getInstance().importImage(OPTIONS_TXT, OPTIONS_TEXT_WID, OPTIONS_TEXT_HEI);
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
        tabs[0] = new Rectangle2D.Double(scale(320), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        tabs[1] = new Rectangle2D.Double(scale(480), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        particleButtons[0] = new SmallButton(scale(390), scale(175), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        particleButtons[1] = new SmallButton(scale(510), scale(175), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
        shakeButtons[0] = new SmallButton(scale(390), scale(225), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        shakeButtons[1] = new SmallButton(scale(510), scale(225), SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
    }

    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
        if (currentPage == 0) audioOptions.update();
        else if (currentPage == 1) {
            Arrays.stream(particleButtons).forEach(SmallButton::update);
            Arrays.stream(shakeButtons).forEach(SmallButton::update);
        }
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        g.drawImage(optionsText, OPTIONS_TEXT_X, OPTIONS_TEXT_Y, optionsText.getWidth(), optionsText.getHeight(), null);
        exitBtn.render(g);
        renderPageTabs(g);

        if (currentPage == 0) renderAudioOptions(g);
        else if (currentPage == 1) renderGameplayOptions(g);
    }

    private void renderPageTabs(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("Audio", scale(333), scale(147));
        g.drawString("Gameplay", scale(480), scale(147));

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(144, 9, 97));
        g2d.setStroke(new BasicStroke(2));
        Rectangle2D.Double activeTab = tabs[currentPage];
        g2d.drawLine((int)activeTab.x, (int)(activeTab.y + activeTab.height), (int)(activeTab.x + activeTab.width), (int)(activeTab.y + activeTab.height));
        g2d.setStroke(new BasicStroke(1));
    }

    private void renderAudioOptions(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_BIG));
        g.drawString("Volume", scale(390), scale(195));
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("SFX", scale(310), scale(224));
        g.drawString("Music", scale(310), scale(274));
        audioOptions.render(g);
    }

    private void renderGameplayOptions(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

        g.drawString("Particle Density", scale(290), scale(192));
        g.drawString(particleLevels[particleIndex], scale(452), scale(192));
        Arrays.stream(particleButtons).forEach(b -> b.render(g));

        g.drawString("Screen Shake", scale(290), scale(242));
        String shakeStatus = Framework.getInstance().getGame().getSettings().isScreenShake() ? "ON" : "OFF";
        g.drawString(shakeStatus, scale(452), scale(242));
        Arrays.stream(shakeButtons).forEach(b -> b.render(g));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        else if (currentPage == 0) audioOptions.mousePressed(e);
        else if (currentPage == 1) {
            Arrays.stream(particleButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMousePressed(true));
            Arrays.stream(shakeButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMousePressed(true));
        }
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i].contains(e.getPoint())) {
                currentPage = i;
                break;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) game.startMenuState();
        else if (currentPage == 0) audioOptions.mouseReleased(e);
        else if (currentPage == 1) handleGameplayButtonRelease(e);
        reset();
    }

    private void handleGameplayButtonRelease(MouseEvent e) {
        for (int i = 0; i < particleButtons.length; i++) {
            if (isMouseInButton(e, particleButtons[i]) && particleButtons[i].isMousePressed())
                changeParticleDensity(i == 1);
        }
        for (SmallButton shakeButton : shakeButtons) {
            if (isMouseInButton(e, shakeButton) && shakeButton.isMousePressed())
                toggleScreenShake();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        List<AbstractButton> gameplayButtons = new ArrayList<>();
        gameplayButtons.addAll(Arrays.asList(particleButtons));
        gameplayButtons.addAll(Arrays.asList(shakeButtons));

        exitBtn.setMouseOver(isMouseInButton(e, exitBtn));

        if (currentPage == 0) audioOptions.mouseMoved(e);
        else {
            gameplayButtons.forEach(b -> b.setMouseOver(false));
            gameplayButtons.stream().filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMouseOver(true));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentPage == 0) audioOptions.mouseDragged(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) game.startMenuState();
    }

    @Override
    public void reset() {
        exitBtn.resetMouseSet();
        Arrays.stream(particleButtons).forEach(SmallButton::resetMouseSet);
        Arrays.stream(shakeButtons).forEach(SmallButton::resetMouseSet);
    }

    private void changeParticleDensity(boolean next) {
        if (next) particleIndex = (particleIndex + 1) % particleLevels.length;
        else particleIndex = (particleIndex - 1 + particleLevels.length) % particleLevels.length;
        double density = switch (particleIndex) {
            case 0 -> 0.25;
            case 1 -> 0.5;
            default -> 1.0;
        };
        Framework.getInstance().getGame().getSettings().setParticleDensity(density);
    }

    private void toggleScreenShake() {
        boolean currentSetting = Framework.getInstance().getGame().getSettings().isScreenShake();
        Framework.getInstance().getGame().getSettings().setScreenShake(!currentSetting);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }
}
