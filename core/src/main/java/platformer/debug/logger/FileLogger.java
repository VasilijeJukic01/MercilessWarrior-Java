package platformer.debug.logger;

import platformer.observer.Subscriber;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;

import static platformer.constants.Constants.*;

public class FileLogger implements LoggerAbstraction, Subscriber {

    public FileLogger(Logger logger) {
        logger.addSubscriber(this);
    }

    @Override
    public void log(String message, Message type) {
        String timestamp = " ["+ LocalDate.now()+" " +(""+ LocalTime.now()).substring(0, 8)+"] ";
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

    @Override
    public void update(Object... o) {
        if (o.length < 1) return;
        if (o[0] instanceof String && o[1] instanceof Message) {
            log((String)o[0], (Message)o[1]);
        }
    }
}
