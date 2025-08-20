package platformer.ui.overlays;

/**
 * Interface for overlay classes.
 * @param <E> The event type.
 * @param <F> The key event type.
 * @param <G> The graphics type.
 */
public interface Overlay<E, F, G> {

    /**
     * Called when the mouse is dragged.
     * @param e The event.
     */
    void mouseDragged(E e);

    /**
     * Called when the mouse is clicked.
     * @param e The event.
     */
    void mouseClicked(E e);

    /**
     * Called when the mouse is pressed.
     * @param e The event.
     */
    void mousePressed(E e);

    /**
     * Called when the mouse is released.
     * @param e The event.
     */
    void mouseReleased(E e);

    /**
     * Called when the mouse is moved.
     * @param e The event.
     */
    void mouseMoved(E e);

    /**
     * Called when a key is pressed.
     * @param e The event.
     */
    void keyPressed(F e);

    /**
     * Called when a key is released.
     * @param e The event.
     */
    void keyReleased(F e);

    /**
     * Updates the overlay.
     */
    void update();

    /**
     * Renders the overlay.
     * @param g The graphics.
     */
    void render(G g);

    /**
     * Resets the overlay.
     */
    void reset();

}
