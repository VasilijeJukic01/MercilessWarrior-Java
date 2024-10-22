package platformer.debug.logger;

import platformer.observer.Subscriber;

import java.time.LocalDate;
import java.time.LocalTime;

import static platformer.constants.Constants.*;

public class ConsoleLogger implements LoggerAbstraction, Subscriber {

    public ConsoleLogger(Logger logger) {
        logger.addSubscriber(this);
    }

    @Override
    public void log(String message, Message type) {
        String timestamp = " ["+ LocalDate.now()+" " +(""+ LocalTime.now()).substring(0, 8)+"] ";
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

    @Override
    public void update(Object ... o) {
        if (o.length < 1) return;
        if (o[0] instanceof String && o[1] instanceof Message) {
            log((String)o[0], (Message)o[1]);
        }
    }

}
