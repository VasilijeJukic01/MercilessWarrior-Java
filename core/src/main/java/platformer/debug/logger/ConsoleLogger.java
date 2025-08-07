package platformer.debug.logger;

import platformer.event.EventBus;
import platformer.event.events.logger.LogEvent;

import java.time.LocalDate;
import java.time.LocalTime;

import static platformer.constants.Constants.*;

/**
 * A log event listener that writes formatted log messages to the standard console output.
 * <p>
 * This class is instantiated by the {@link Logger} and registered with the {@link EventBus} to listen for {@link LogEvent}.
 */
public class ConsoleLogger {

    public void onLogEvent(LogEvent event) {
        String message = event.message();
        Message type = event.type();
        String timestamp = " [" + LocalDate.now() + " " + ("" + LocalTime.now()).substring(0, 8) + "] ";
        switch (type) {
            case ERROR:
                System.out.println(ERROR_PREFIX+timestamp+message);
                break;
            case WARNING:
                System.out.println(WARNING_PREFIX+timestamp+message);
                break;
            case NOTIFICATION:
                System.out.println(NOTIFICATION_PREFIX+timestamp+message);
                break;
            case INFORMATION:
                System.out.println(INFORMATION_PREFIX+timestamp+message);
                break;
            case DEBUG:
                System.out.println(DEBUG_PREFIX+timestamp+message);
                break;
            default: break;
        }
    }

}
