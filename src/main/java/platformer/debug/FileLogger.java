package platformer.debug;

import platformer.core.Game;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;

public class FileLogger implements Logger, Subscriber {

    public FileLogger(Game game) {
        game.addSubscriber(this);
    }

    @Override
    public void log(String message, Message type) {
        String timestamp = " ["+ LocalDate.now()+" " +(""+ LocalTime.now()).substring(0, 8)+"] ";
        String log = "";
        switch (type) {
            case ERROR:
                log = "[ERROR]         "+timestamp+message;
                break;
            case WARNING:
                log = "[WARNING]       "+timestamp+message;
                break;
            case NOTIFICATION:
                log = "[NOTIFICATION]  "+timestamp+message;
                break;
            case INFORMATION:
                log = "[INFORMATION]   "+timestamp+message;
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
