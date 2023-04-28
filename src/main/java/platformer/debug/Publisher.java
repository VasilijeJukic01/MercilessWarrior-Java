package platformer.debug;

public interface Publisher {

    void addSubscriber(Subscriber s);

    void removeSubscriber(Subscriber s);

    void notifyLogger(Object ... o);

}
