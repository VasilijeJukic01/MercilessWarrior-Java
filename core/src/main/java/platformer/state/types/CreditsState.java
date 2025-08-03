package platformer.state.types;

import com.google.gson.reflect.TypeToken;
import platformer.core.Game;
import platformer.model.credits.CreditEntry;
import platformer.model.credits.CreditSection;
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
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.CREDITS_PATH;
import static platformer.constants.FilePaths.CREDITS_TXT;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is viewing the credits.
 * In this state, the player can view the credits of the game.
 */
public class CreditsState extends AbstractState implements State {

    private BufferedImage creditsText;
    private SmallButton exitBtn;

    private List<CreditSection> sections;

    public CreditsState(Game game) {
        super(game);
        loadImages();
        loadButtons();
        loadCreditsData();
    }

    private void loadImages() {
        this.creditsText = ImageUtils.importImage(CREDITS_TXT, CREDITS_TXT_WID, CREDITS_TXT_HEI);
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    private void loadCreditsData() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(CREDITS_PATH)))) {
            Type listType = new TypeToken<List<CreditSection>>() {}.getType();
            this.sections = new com.google.gson.Gson().fromJson(reader, listType);
        } catch (Exception e) {
            this.sections = new ArrayList<>();
        }
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
        g.drawImage(creditsText, CREDITS_TXT_X, CREDITS_TXT_Y, creditsText.getWidth(), creditsText.getHeight(), null);
        exitBtn.render(g);
        renderInformation(g);
    }

    private void renderInformation(Graphics g) {
        int yPos = CREDITS_POSITION_Y;

        for (CreditSection section : sections) {
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM + 2));
            g.setColor(Color.WHITE);
            g.drawString(section.section() + ":", CREDITS_POSITION_X, yPos);
            yPos += CREDITS_SPACING;

            g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
            g.setColor(CREDITS_COLOR);
            for (CreditEntry entry : section.entries()) {
                String text = entry.role().isEmpty() ? entry.name() : entry.role() + " " + entry.name();
                g.drawString(text, CREDITS_POSITION_X + (int)(10 * SCALE), yPos);
                yPos += CREDITS_SPACING;
            }
            yPos += CREDITS_SPACING/2;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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
