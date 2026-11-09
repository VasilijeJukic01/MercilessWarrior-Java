package platformer.ui.overlays;

import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.UI.*;

/**
 * This class is responsible for rendering the surface for overlay of the game.
 */
public class OverlayLayer {

    public static volatile OverlayLayer instance = null;

    private MenuSceneRenderer menuSceneRenderer;
    private Rectangle2D overlay;

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

    private void init() {
        this.menuSceneRenderer = new MenuSceneRenderer();
        this.overlay = new Rectangle2D.Double(OVERLAY_X, OVERLAY_Y, OVERLAY_WID, OVERLAY_HEI);
    }

    // Core
    public void update() {
        menuSceneRenderer.update();
    }

    public void render(Graphics g) {
        renderMenu(g);
        renderOverlay(g);
    }

    public void renderMenu(Graphics g) {
        menuSceneRenderer.render(g);
    }

    public void renderOverlay(Graphics g) {
        UIButtonRenderer.drawRunicPanel(g, overlay);
    }

}
