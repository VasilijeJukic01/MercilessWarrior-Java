package platformer.debug.logger;

import platformer.debug.logger.observer.Publisher;
import platformer.debug.logger.observer.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class Logger implements Publisher {

    private LoggerAbstraction consoleLogger;
    //private Logger fileLogger;

    private List<Subscriber> subscribers;

    private static volatile Logger instance = null;

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                    instance.initLogger();
                }
            }
        }
        return instance;
    }


    private Logger() {}

    private void initLogger() {
        this.consoleLogger = new ConsoleLogger(this);
        //this.fileLogger = new FileLogger();
    }

    // Observer
    @Override
    public void addSubscriber(Subscriber s) {
        if(s == null) return;
        if(this.subscribers == null) this.subscribers = new ArrayList<>();
        if(this.subscribers.contains(s)) return;
        this.subscribers.add(s);
    }

    @Override
    public void removeSubscriber(Subscriber s) {
        if(s == null ||  this.subscribers == null || !this.subscribers.contains(s)) return;
        this.subscribers.remove(s);
    }

    @Override
    public void notify(Object ... o) {
        if (o == null || this.subscribers == null || this.subscribers.isEmpty()) return;
        for (Subscriber subscriber : subscribers) {
            subscriber.update(o);
        }
    }

}
