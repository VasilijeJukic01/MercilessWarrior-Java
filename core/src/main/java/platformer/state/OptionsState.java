package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.options.GameSettingsPanel;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

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

    private BufferedImage optionsText;

    private final Rectangle2D.Double[] tabs = new Rectangle2D.Double[2];

    private SmallButton exitBtn;

    private final GameSettingsPanel settingsPanel;

    public OptionsState(Game game) {
        super(game);
        this.settingsPanel = new GameSettingsPanel(game);
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.optionsText = ImageUtils.importImage(OPTIONS_TXT, OPTIONS_TEXT_WID, OPTIONS_TEXT_HEI);
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
        tabs[0] = new Rectangle2D.Double(scale(320), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
        tabs[1] = new Rectangle2D.Double(scale(480), scale(135), TINY_BTN_WID, TINY_BTN_HEI);
    }

    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
        if (currentPage == 0) settingsPanel.updateAudio();
        else if (currentPage == 1) settingsPanel.updateGameplay();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        g.drawImage(optionsText, OPTIONS_TEXT_X, OPTIONS_TEXT_Y, optionsText.getWidth(), optionsText.getHeight(), null);
        exitBtn.render(g);
        renderPageTabs(g);

        if (currentPage == 0) settingsPanel.renderAudioPage(g);
        else if (currentPage == 1) settingsPanel.renderGameplayPage(g);
    }

    private void renderPageTabs(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("Audio", scale(333), scale(147));
        g.drawString("Gameplay", scale(480), scale(147));

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(TAB_COLOR);
        g2d.setStroke(new BasicStroke(2));
        Rectangle2D.Double activeTab = tabs[currentPage];
        g2d.drawLine((int)activeTab.x, (int)(activeTab.y + activeTab.height), (int)(activeTab.x + activeTab.width), (int)(activeTab.y + activeTab.height));
        g2d.setStroke(new BasicStroke(1));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        else {
            if (currentPage == 0) settingsPanel.mousePressedAudio(e);
            else if (currentPage == 1) settingsPanel.mousePressedGameplay(e);
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
        else {
            if (currentPage == 0) settingsPanel.mouseReleasedAudio(e);
            else if (currentPage == 1) settingsPanel.mouseReleasedGameplay(e);
        }
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(isMouseInButton(e, exitBtn));
        if (currentPage == 0) settingsPanel.mouseMovedAudio(e);
        else if (currentPage == 1) settingsPanel.mouseMovedGameplay(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentPage == 0) settingsPanel.mouseDraggedAudio(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) game.startMenuState();
    }

    @Override
    public void reset() {
        exitBtn.resetMouseSet();
        settingsPanel.resetAudio();
        settingsPanel.resetGameplay();
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
