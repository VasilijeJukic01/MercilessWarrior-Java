package platformer.ui.buttons;

/**
 * Defines the essential contract for all interactive button components in the game.
 * Any class that acts as a button must implement this interface, ensuring it has
 * methods for updating its state and rendering itself to the screen.
 *
 * @param <G> The type of the graphics context used for rendering (e.g., {@link java.awt.Graphics}).
 */
public interface GameButton<G> {

    /**
     * Updates the button's internal state. This is typically called once per frame
     * and is used to handle logic such as changing appearance based on mouse interaction.
     */
    void update();

    /**
     * Renders the button to the screen using the provided graphics context.
     *
     * @param g The graphics object used to draw the button.
     */
    void render(G g);

}
