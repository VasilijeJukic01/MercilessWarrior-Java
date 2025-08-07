package platformer.debug.logger;

import platformer.event.EventBus;
import platformer.event.events.logger.LogEvent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;

import static platformer.constants.Constants.*;

/**
 * A log event listener that appends formatted log messages to a file.
 * <p>
 * This class is instantiated by the {@link Logger} and registered with the {@link EventBus} to listen for {@link LogEvent}.
 */
public class FileLogger {

    public void onLogEvent(LogEvent event) {
        String message = event.message();
        Message type = event.type();
        String timestamp = " [" + LocalDate.now() + " " + ("" + LocalTime.now()).substring(0, 8) + "] ";
        String log = "";
        switch (type) {
            case ERROR:
                log = ERROR_PREFIX+timestamp+message;
                break;
            case WARNING:
                log = WARNING_PREFIX+timestamp+message;
                break;
            case NOTIFICATION:
                log = NOTIFICATION_PREFIX+timestamp+message;
                break;
            case INFORMATION:
                log = INFORMATION_PREFIX+timestamp+message;
                break;
            case DEBUG:
                log = DEBUG_PREFIX+timestamp+message;
                break;
            default: break;
        }
        try {
            Files.write(Paths.get("src/main/resources/log.txt"), (log+"\n").getBytes(), StandardOpenOption.APPEND);
        } catch (Exception ignored) {}
    }
}
