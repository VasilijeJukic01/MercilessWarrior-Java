package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.tutorial.TutorialTip;
import platformer.state.types.GameState;
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
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * PauseOverlay class is an overlay that is displayed when the game is paused.
 * It allows the player to interact with pause game options.
 */
public class PauseOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;
    private final GameSettingsPanel settingsPanel;
    private final ControlsPanel controlsPanel;
    private BufferedImage pauseText;

    private int currentPage = 0;
    private final Rectangle2D.Double[] tabs = new Rectangle2D.Double[4];

    private int tipIndex = 0;
    private List<TutorialTip> tips;
    private final Map<String, BufferedImage> tipImages = new HashMap<>();

    private SmallButton continueBtn, retryBtn, exitBtn;

    private int tipTimer = 0;
    private static final int TIP_SWITCH_INTERVAL = 8 * 200;
    private static final float FADE_SPEED = 0.05f;
    private float tipAlpha = 1.0f;
    private boolean fadingOut = false, fadingIn = false;

    public PauseOverlay(GameState gameState) {
        this.gameState = gameState;
        this.settingsPanel = new GameSettingsPanel(gameState.getGame());
        this.controlsPanel = new ControlsPanel(this.getClass());
        loadImages();
        loadButtons();
        loadTips();
    }

    private void loadImages() {
        this.pauseText = ImageUtils.importImage(PAUSE_TXT, PAUSE_TEXT_WID, PAUSE_TEXT_HEI);
    }

    private void loadTips() {
        this.tips = gameState.getContext().getTutorialManager().getTips();
        for (TutorialTip tip : tips) {
            if (tip.image != null && !tip.image.isEmpty()) {
                tipImages.put(tip.image, ImageUtils.importImage(tip.image, TUTORIAL_IMAGE_WID, TUTORIAL_IMAGE_HEI));
            }
        }
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
                updateTipCycle();
            }
            case 1 -> settingsPanel.updateAudio();
            case 2 -> settingsPanel.updateGameplay();
            case 3 -> controlsPanel.update();
        }
    }

    private void updateTipCycle() {
        if (fadingOut) {
            tipAlpha -= FADE_SPEED;
            if (tipAlpha <= 0) {
                tipAlpha = 0;
                fadingOut = false;
                fadingIn = true;
                tipIndex = (tipIndex + 1) % tips.size();
            }
        }
        else if (fadingIn) {
            tipAlpha += FADE_SPEED;
            if (tipAlpha >= 1.0f) {
                tipAlpha = 1.0f;
                fadingIn = false;
            }
        }
        else {
            tipTimer++;
            if (tipTimer >= TIP_SWITCH_INTERVAL) {
                tipTimer = 0;
                fadingOut = true;
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
        renderTipSection(g);
    }

    private void renderTipSection(Graphics g) {
        if (tips == null || tips.isEmpty()) return;
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tipAlpha));

        int contentX = OVERLAY_X + (int)(20 * SCALE);
        int contentWidth = OVERLAY_WID - (int)(40 * SCALE);
        int centerX = contentX + contentWidth / 2;
        int y = PAUSE_TEXT_Y + (int)(100 * SCALE);

        TutorialTip currentTip = tips.get(tipIndex);
        FontMetrics fm;

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(currentTip.title);
        g2d.drawString(currentTip.title, centerX - titleWidth / 2, y);

        y += (int)(25 * SCALE);

        // Description
        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
        fm = g2d.getFontMetrics();
        g2d.setColor(new Color(200, 200, 200));
        String formattedContent = formatContent(currentTip.content);
        for (String line : wrapText(formattedContent, contentWidth, fm)) {
            int lineWidth = fm.stringWidth(line);
            g2d.drawString(line, centerX - lineWidth / 2, y);
            y += g2d.getFontMetrics().getHeight();
        }

        // Image
        y += (int)(10 * SCALE);
        if (currentTip.image != null) {
            BufferedImage tipImage = tipImages.get(currentTip.image);
            if (tipImage != null) {
                int imgX = centerX - tipImage.getWidth() / 2;
                int imgY = y;
                int cornerRadius = (int)(15 * SCALE);

                Shape clipShape = new RoundRectangle2D.Float(imgX, imgY, tipImage.getWidth(), tipImage.getHeight(), cornerRadius, cornerRadius);
                Shape oldClip = g2d.getClip();
                g2d.setClip(clipShape);
                g2d.drawImage(tipImage, imgX, imgY, null);
                g2d.setClip(oldClip);
                g2d.setColor(new Color(150, 150, 150, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(clipShape);
                y += tipImage.getHeight() + (int)(15 * SCALE);
            }
        }
        g2d.dispose();
    }

    private String formatContent(String content) {
        KeyboardController kc = Framework.getInstance().getKeyboardController();
        return content.replace("{Shield}", kc.getKeyName("Shield"))
                .replace("{Minimap}", kc.getKeyName("Minimap"));
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (!currentLine.isEmpty() && fm.stringWidth(currentLine + " " + word) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
            if (!currentLine.isEmpty()) currentLine.append(" ");
            currentLine.append(word);
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i].contains(e.getPoint())) {
                if (currentPage != i) {
                    currentPage = i;
                    tipTimer = 0;
                    tipAlpha = 1.0f;
                    fadingIn = false;
                    fadingOut = false;
                }
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
            gameState.getGame().reset();
            Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
        }
        else if (isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            EventBus.getInstance().publish(new OverlayChangeEvent(null));
            gameState.getGame().startMenuState();
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
