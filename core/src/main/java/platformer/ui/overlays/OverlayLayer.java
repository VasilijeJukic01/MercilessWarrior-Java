package platformer.ui.overlays;

import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.MENU_FRAMES;
import static platformer.constants.AnimConstants.OVERLAY_ANIM_SPEED;
import static platformer.constants.Constants.GAME_HEIGHT;
import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.FilePaths.MENU_SPRITES;
import static platformer.constants.UI.*;

/**
 * This class is responsible for rendering the surface for overlay of the game.
 */
public class OverlayLayer {

    public static volatile OverlayLayer instance = null;

    private BufferedImage[] background;
    private Rectangle2D overlay;
    private int animTick = 0, animIndex = 0;

    private OverlayLayer() {}

    public static OverlayLayer getInstance() {
        if (instance == null) {
            synchronized (OverlayLayer.class) {
                if (instance == null) {
                    instance = new OverlayLayer();
                    instance.init();
                }
            }
        }
        return instance;
    }

    private BufferedImage[] loadMenuAnimation() {
        BufferedImage[] anim = new BufferedImage[MENU_FRAMES];
        for (int i = 0; i < anim.length; i++) {
            anim[i] = ImageUtils.importImage(MENU_SPRITES.replace("$", i+""), GAME_WIDTH, GAME_HEIGHT);
        }
        return anim;
    }

    private void init() {
        this.background = loadMenuAnimation();
        this.overlay = new Rectangle2D.Double(OVERLAY_X, OVERLAY_Y, OVERLAY_WID, OVERLAY_HEI);
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= OVERLAY_ANIM_SPEED) {
            animTick = 0;
            animIndex++;
            if (animIndex >= MENU_FRAMES) {
                animIndex = 0;
            }
        }
    }

    // Core
    public void update() {
        updateAnimation();
    }

    public void render(Graphics g) {
        renderMenu(g);
        renderOverlay(g);
    }

    public void renderMenu(Graphics g) {
        g.drawImage(background[animIndex], 0, 0,null);
    }

    public void renderOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(OVERLAY_COLOR);
        g2d.fill(overlay);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(overlay);
    }

}
