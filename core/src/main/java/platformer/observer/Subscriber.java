package platformer.observer;

public interface Subscriber {

    <T> void update(T ... o);

}
