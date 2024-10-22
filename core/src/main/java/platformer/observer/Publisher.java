package platformer.observer;

public interface Publisher {

    void addSubscriber(Subscriber s);

    void removeSubscriber(Subscriber s);

    <T> void notify(T ... o);

}
