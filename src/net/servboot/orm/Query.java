package net.servboot.orm;

import net.servboot.database.ConnectionManager;
import net.servboot.function.ThrowingConsumer;
import net.servboot.function.ThrowingFunction;
import java.sql.*;
import java.util.List;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;

public class Query {

    public static void executeQuery(String sql, ThrowingConsumer<ResultSet> consumer)
            throws SQLException, InterruptedException {
        try (
            Statement statement = ConnectionManager.getConnection().createStatement(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE);
        ) {
            ResultSet resultSet = statement.executeQuery(sql);
            try {
                consumer.accept(resultSet);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <E> E executeQuery(String sql, ThrowingFunction<ResultSet, E> function)
            throws SQLException, InterruptedException {
        try {
            Statement statement = ConnectionManager.getConnection().createStatement(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE);
            ResultSet resultSet = statement.executeQuery(sql);
            return function.apply(resultSet);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void executePreparedQuery(String sql, List<Object> values, ThrowingConsumer<ResultSet> consumer)
            throws SQLException, InterruptedException {
        try (
                PreparedStatement statement = ConnectionManager.getConnection().prepareStatement(sql, TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE);
        ) {
            for (int i = 1; i <= values.size(); i++) {
                Object value = values.get(i - 1);
                setValueOnPreparedStatement(statement, i, value);
            }

            ResultSet resultSet = statement.executeQuery();
            consumer.accept(resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean execute(String sql) throws SQLException, InterruptedException {
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

    public static int executePreparedUpdate(String sql, List<Object> values)
            throws SQLException, InterruptedException{
        try (
            PreparedStatement statement = ConnectionManager.getConnection().prepareStatement(sql);
        ) {
            for (int i = 1; i <= values.size(); i++) {
                setValueOnPreparedStatement(statement, i, values.get(i - 1));
            }

            return statement.executeUpdate();
        }
    }

    public static void setValueOnPreparedStatement(PreparedStatement statement, int index, Object value) throws SQLException {
        switch (value) {
            case null -> statement.setNull(index, Types.NULL);
            case Long longValue -> statement.setLong(index, longValue);
            case Integer intValue -> statement.setInt(index, intValue);
            case Float floatValue -> statement.setFloat(index, floatValue);
            case Double doubleValue -> statement.setDouble(index, doubleValue);
            case Short shortValue -> statement.setShort(index, shortValue);
            case Byte byteValue -> statement.setByte(index, byteValue);
            case Character charValue -> statement.setString(index, value.toString());
            case String stringValue -> statement.setString(index, stringValue);
            case Boolean booleanValue -> statement.setBoolean(index, booleanValue);
            default -> statement.setObject(index, value);
        }
    }

}
