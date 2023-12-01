package platformer.ui.buttons;

public interface GameButton<G> {

    void update();

    void render(G g);

}
