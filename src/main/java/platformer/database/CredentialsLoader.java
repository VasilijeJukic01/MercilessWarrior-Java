package platformer.database;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CredentialsLoader {

    private final Properties properties;

    public CredentialsLoader() {
        this.properties = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/database.config");
            properties.load(fileInputStream);
        }
        catch (IOException e) {
            Logger.getInstance().notify("Error with database.config file!", Message.ERROR);
        }
    }

    public String getDatabaseIP() {
        return properties.getProperty("database_ip");
    }

    public String getDatabaseName() {
        return properties.getProperty("database_name");
    }

    public String getDatabaseUsername() {
        return properties.getProperty("database_username");
    }

    public String getDatabasePassword() {
        return properties.getProperty("database_password");
    }

}
