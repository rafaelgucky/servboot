package net.servboot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/orm", "root", "1234");
    }

    public static Connection getPostgreSqlConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/eventer", "postgres", "1234");
    }
}
