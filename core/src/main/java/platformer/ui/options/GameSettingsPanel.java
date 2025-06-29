package platformer.ui.options;

import platformer.core.Framework;
import platformer.core.Game;
import platformer.ui.AudioOptions;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

public class GameSettingsPanel {

    private final Game game;
    private final AudioOptions audioOptions;

    private final SmallButton[] particleButtons = new SmallButton[2];
    private final SmallButton[] shakeButtons = new SmallButton[2];

    private final String[] particleLevels = {"0.25", "0.50", "1.00"};
    private int particleIndex = 2;

    public GameSettingsPanel(Game game) {
        this.game = game;
        this.audioOptions = game.getAudioOptions();
        loadButtons();
        setInitialSettings();
    }

    private void loadButtons() {
        particleButtons[0] = new SmallButton(PARTICLE_BTN_PREV_X, PARTICLE_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        particleButtons[1] = new SmallButton(PARTICLE_BTN_NEXT_X, PARTICLE_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
        shakeButtons[0] = new SmallButton(SHAKE_BTN_PREV_X, SHAKE_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        shakeButtons[1] = new SmallButton(SHAKE_BTN_NEXT_X, SHAKE_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
    }

    private void setInitialSettings() {
        double density = game.getSettings().getParticleDensity();
        if (density == 0.25) particleIndex = 0;
        else if (density == 0.5) particleIndex = 1;
        else particleIndex = 2;
    }

    // Update
    public void updateAudio() {
        audioOptions.update();
    }

    public void updateGameplay() {
        Arrays.stream(particleButtons).forEach(SmallButton::update);
        Arrays.stream(shakeButtons).forEach(SmallButton::update);
    }

    // Render
    public void renderAudioPage(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_BIG));
        g.drawString("Volume", VOLUME_TEXT_X, VOLUME_TEXT_Y);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("SFX", SFX_TEXT_X, SFX_TEXT_Y);
        g.drawString("Music", MUSIC_TEXT_X, MUSIC_TEXT_Y);
        audioOptions.render(g);
    }

    public void renderGameplayPage(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

        g.drawString("Particle Density", PARTICLE_TEXT_X, PARTICLE_TEXT_Y);
        g.drawString(particleLevels[particleIndex], PARTICLE_LEVEL_X, PARTICLE_LEVEL_Y);
        Arrays.stream(particleButtons).forEach(b -> b.render(g));

        g.drawString("Screen Shake", SHAKE_TEXT_X, SHAKE_TEXT_Y);
        String shakeStatus = Framework.getInstance().getGame().getSettings().isScreenShake() ? "ON" : "OFF";
        g.drawString(shakeStatus, SHAKE_STATUS_X, SHAKE_STATUS_Y);
        Arrays.stream(shakeButtons).forEach(b -> b.render(g));
    }

    // Event Handling
    public void mousePressedAudio(MouseEvent e) {
        audioOptions.mousePressed(e);
    }

    public void mouseReleasedAudio(MouseEvent e) {
        audioOptions.mouseReleased(e);
    }

    public void mouseMovedAudio(MouseEvent e) {
        audioOptions.mouseMoved(e);
    }

    public void mouseDraggedAudio(MouseEvent e) {
        audioOptions.mouseDragged(e);
    }

    public void mousePressedGameplay(MouseEvent e) {
        Arrays.stream(particleButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMousePressed(true));
        Arrays.stream(shakeButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMousePressed(true));
    }

    public void mouseReleasedGameplay(MouseEvent e) {
        for (int i = 0; i < particleButtons.length; i++) {
            if (isMouseInButton(e, particleButtons[i]) && particleButtons[i].isMousePressed())
                changeParticleDensity(i == 1);
        }
        for (SmallButton shakeButton : shakeButtons) {
            if (isMouseInButton(e, shakeButton) && shakeButton.isMousePressed())
                toggleScreenShake();
        }
    }

    public void mouseMovedGameplay(MouseEvent e) {
        Arrays.stream(particleButtons).forEach(b -> b.setMouseOver(false));
        Arrays.stream(shakeButtons).forEach(b -> b.setMouseOver(false));
        Arrays.stream(particleButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMouseOver(true));
        Arrays.stream(shakeButtons).filter(b -> isMouseInButton(e, b)).findFirst().ifPresent(b -> b.setMouseOver(true));
    }

    // Reset
    public void resetAudio() {
        audioOptions.resetButtons();
    }

    public void resetGameplay() {
        Arrays.stream(particleButtons).forEach(SmallButton::resetMouseSet);
        Arrays.stream(shakeButtons).forEach(SmallButton::resetMouseSet);
    }

    // Helpers
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

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

}
