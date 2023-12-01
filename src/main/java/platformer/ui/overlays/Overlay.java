package platformer.ui.overlays;

public interface Overlay<E, G> {

    void mouseDragged(E e);

    void mousePressed(E e);

    void mouseReleased(E e);

    void mouseMoved(E e);

    void update();

    void render(G g);

    void reset();

}
