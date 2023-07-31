package platformer.ui.overlays;

import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.MENU_FRAMES;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.OVERLAY;

public class OverlayLayer {

    public static volatile OverlayLayer instance = null;

    private BufferedImage[] background;
    private BufferedImage overlay;
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0;

    // Size Variables [Init]
    private static final int OVERLAY_WID = (int)(300*SCALE);
    private static final int OVERLAY_HEI = (int)(350*SCALE);

    // Size Variables [Render]
    private static final int OVERLAY_X = (int)(270*SCALE);
    private static final int OVERLAY_Y = (int)(50*SCALE);

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
            anim[i] = Utils.getInstance().importImage("/images/menu/background/Background"+i+".png", GAME_WIDTH, GAME_HEIGHT);
        }
        return anim;
    }

    private void init() {
        this.background = loadMenuAnimation();
        this.overlay = Utils.getInstance().importImage(OVERLAY, OVERLAY_WID, OVERLAY_HEI);
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
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
        g.drawImage(overlay, OVERLAY_X, OVERLAY_Y, overlay.getWidth(), overlay.getHeight(), null);
    }

}
