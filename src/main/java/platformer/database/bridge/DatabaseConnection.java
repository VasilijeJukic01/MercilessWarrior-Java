package platformer.database.bridge;

import platformer.database.Settings;

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
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            connection = null;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
