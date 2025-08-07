package platformer.debug.logger;

import platformer.event.EventBus;
import platformer.event.events.logger.LogEvent;

/**
 * The central entry point for all logging operations in the application.
 * <p>
 * This logger uses a global {@link EventBus} to publish {@link LogEvent}.
 * Log consumers, such as {@link ConsoleLogger} and {@link FileLogger}, register themselves with the EventBus to receive these events.
 *
 * @see EventBus
 * @see LogEvent
 * @see ConsoleLogger
 * @see FileLogger
 */
public class Logger {

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
        ConsoleLogger consoleLogger = new ConsoleLogger();
        // FileLogger fileLogger = new FileLogger();

        EventBus eventBus = EventBus.getInstance();
        eventBus.register(LogEvent.class, consoleLogger::onLogEvent);
        // eventBus.register(LogEvent.class, fileLogger::onLogEvent);
    }

    @SuppressWarnings("unchecked")
    public <T> void notify(T... o) {
        if (o == null || o.length < 2 || !(o[0] instanceof String) || !(o[1] instanceof Message)) return;
        EventBus.getInstance().publish(new LogEvent((String) o[0], (Message) o[1]));
    }

}
