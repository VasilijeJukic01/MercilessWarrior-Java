package platformer.database.bridge;

import platformer.database.Settings;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private Connection connection;

    public void initConnection(Settings settings) throws SQLException {
        String ip = (String) settings.getParameter("IP");
        String database = (String) settings.getParameter("DATABASE");
        String username = (String) settings.getParameter("USERNAME");
        String password = (String) settings.getParameter("PASSWORD");
        this.connection = DriverManager.getConnection("jdbc:mysql://"+ip+"/"+database,username,password);
    }

    public void closeConnection(){
        try{
            connection.close();
            Logger.getInstance().notify("Database connection closed successfully!", Message.INFORMATION);
        }
        catch (SQLException e){
            Logger.getInstance().notify("Failed to close database connection!", Message.ERROR);
        }
        finally {
            connection = null;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}