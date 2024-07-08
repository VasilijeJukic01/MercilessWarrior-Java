package platformer.debug.logger.observer;

public interface Publisher {

    void addSubscriber(Subscriber s);

    void removeSubscriber(Subscriber s);

    void notify(Object ... o);

}
