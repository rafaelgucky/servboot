package net.servboot.orm;

import net.servboot.database.ConnectionManager;

import java.sql.*;
import java.util.List;
import java.util.function.Consumer;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;

public class Query {

    public static void executeQuery(String sql, Consumer<ResultSet> consumer)
            throws SQLException, InterruptedException {
        try (
            Statement statement = ConnectionManager.getConnection().createStatement(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE);
        ) {
            ResultSet resultSet = statement.executeQuery(sql);
            try {
                consumer.accept(resultSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void executePreparedQuery(String sql, List<Object> values, Consumer<ResultSet> consumer)
            throws SQLException, InterruptedException {
        try (
                PreparedStatement statement = ConnectionManager.getConnection().prepareStatement(sql, TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE);
        ) {
            for (int i = 1; i <= values.size(); i++) {
                Object value = values.get(i - 1);
                switch (value) {
                    case null -> statement.setNull(i, Types.NULL);
                    case Long longValue -> statement.setLong(i, longValue);
                    case Integer intValue -> statement.setInt(i, intValue);
                    case Float floatValue -> statement.setFloat(i, floatValue);
                    case Double doubleValue -> statement.setDouble(i, doubleValue);
                    case Short shortValue -> statement.setShort(i, shortValue);
                    case Byte byteValue -> statement.setByte(i, byteValue);
                    case Character charValue -> statement.setString(i, value.toString());
                    case Boolean booleanValue -> statement.setBoolean(i, booleanValue);
                    default -> statement.setObject(i, value);
                }
            }

            ResultSet resultSet = statement.executeQuery();
            consumer.accept(resultSet);
        }
    }

    public static boolean execute(String sql)
            throws SQLException, InterruptedException{
        try (
            Statement statement = ConnectionManager.getConnection().createStatement();
        ) {
            return statement.execute(sql);
        }
    }

    public static int executeUpdate(String sql)
            throws SQLException, InterruptedException{
        try (
            Statement statement = ConnectionManager.getConnection().createStatement();
        ) {
            return statement.executeUpdate(sql);
        }
    }

}
