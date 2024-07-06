package platformer.ui.buttons;

/**
 * Interface for all buttons in the game.
 * @param <G> The graphics object used to render the button.
 */
public interface GameButton<G> {

    /**
     * Updates the button.
     */
    void update();

    /**
     * Renders the button.
     * @param g The graphics object used to render the button.
     */
    void render(G g);

}
