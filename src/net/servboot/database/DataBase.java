package net.servboot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    public static Connection connection = null;

    public static Connection getConnection() {
        try{
            if(connection == null) {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/files?useSSL=false",  "root", "1234");
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        return connection;
    }
}
