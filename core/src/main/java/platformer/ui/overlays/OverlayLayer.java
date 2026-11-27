package platformer.ui.overlays;

import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.UI.*;

/**
 * Responsible for rendering menu background scenes and runic modal overlay plaques.
 * Lazily creates and disposes {@link MenuSceneRenderer} so gameplay doesn't keep a duplicate engine in RAM.
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
        this.overlay = new Rectangle2D.Double(OVERLAY_X, OVERLAY_Y, OVERLAY_WID, OVERLAY_HEI);
    }

    private MenuSceneRenderer ensureMenuScene() {
        if (menuSceneRenderer == null) {
            menuSceneRenderer = new MenuSceneRenderer();
        }
        return menuSceneRenderer;
    }

    /**
     * Releases the menu scene's duplicate managers when entering active gameplay.
     */
    public void releaseMenuScene() {
        this.menuSceneRenderer = null;
    }

    public void update() {
        ensureMenuScene().update();
    }

    public void render(Graphics g) {
        renderMenu(g);
        renderOverlay(g);
    }

    public void renderMenu(Graphics g) {
        ensureMenuScene().render(g);
    }

    public void renderOverlay(Graphics g) {
        UIButtonRenderer.drawRunicPanel(g, overlay);
    }
}