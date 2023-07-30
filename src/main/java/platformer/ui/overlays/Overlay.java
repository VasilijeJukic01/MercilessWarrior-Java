package platformer.ui.overlays;

import platformer.animation.AnimUtils;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.SCALE;

public class Overlay {

    public static Overlay instance = null;

    private BufferedImage[] background;
    private BufferedImage overlay;
    private final int overlayWid = (int)(300*SCALE);
    private final int overlayHei = (int)(350*SCALE);
    private final int overlayX = (int)(270*SCALE);
    private final int overlayY = (int)(50*SCALE);
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0;

    private Overlay() {}

    private void init() {
        this.background = AnimUtils.getInstance().loadMenuAnimation();
        this.overlay = Utils.instance.importImage("/images/overlay1.png",overlayWid, overlayHei);
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= 24) {
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
        try {
            g.drawImage(background[animIndex], 0, 0,null);
        }
        catch (Exception ignored) {}
    }

    public void renderOverlay(Graphics g) {
        g.drawImage(overlay,  overlayX,  overlayY, overlay.getWidth(), overlay.getHeight(), null);
    }

    public static Overlay getInstance() {
        if (instance == null) {
            instance = new Overlay();
            instance.init();
        }
        return instance;
    }

}
