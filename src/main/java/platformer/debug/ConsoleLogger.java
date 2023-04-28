package platformer.debug;

import platformer.core.Game;

import java.time.LocalDate;
import java.time.LocalTime;

public class ConsoleLogger implements Logger, Subscriber {

    public ConsoleLogger(Game game) {
        game.addSubscriber(this);
    }

    @Override
    public void log(String message, Message type) {
        String timestamp = " ["+ LocalDate.now()+" " +(""+ LocalTime.now()).substring(0, 8)+"] ";
        switch (type) {
            case ERROR:
                System.out.println("[ERROR]        "+timestamp+message);
                break;
            case WARNING:
                System.out.println("[WARNING]      "+timestamp+message);
                break;
            case NOTIFICATION:
                System.out.println("[NOTIFICATION] "+timestamp+message);
                break;
            case INFORMATION:
                System.out.println("[INFORMATION]  "+timestamp+message);
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
