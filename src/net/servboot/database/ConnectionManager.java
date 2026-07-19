package net.servboot.database;

import net.servboot.client.ClientRequestTask;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

public class ConnectionManager {
    public static final short MAX_CONNECTIONS = 100;
    private static final Stack<Connection> pool = new Stack<>();
    private static final Map<String, Connection> connections = new LinkedHashMap<>();

    public static void init() throws SQLException {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            pool.add(createConnection());
        }
    }

    private static Connection createConnection() throws SQLException {
        Connection connection = DataBase.getConnection();
        connection.setAutoCommit(false);

        return connection;
    }

    public static Connection getConnection(String threadName) throws InterruptedException {
        if (connections.containsKey(threadName)) {
            return connections.get(threadName);
        }

        return getConnection();
    }

    public static Connection getConnection() throws InterruptedException {
        synchronized (pool) {
            while (pool.isEmpty()) {
                pool.wait();
            }

            Connection c = pool.pop();
            String x = Thread.currentThread().getName();
            connections.put(x, c);

            if (Thread.currentThread() instanceof ClientRequestTask thread) {
                thread.setOnFinalize(t -> addConnection(c));
            }

            return c;
        }
    }

    public static void addConnection(Connection conn) {
        try {
            if (conn.isClosed()) {
                return;
            }

            rollback(conn);
        } catch (SQLException e) {
            // Se houver algum erro ao dar rollback na conexão, cria uma nova conexão e adiciona ao pool
            try {
                conn.close();
                pool.push(createConnection());
                return;
            } catch (Exception ignored) { }
        }

        synchronized (pool) {
            pool.push(conn);
            pool.notify();
        }
    }

    public static void begin() throws SQLException, InterruptedException {
        begin(getConnection(Thread.currentThread().getName()));
    }

    public static void begin(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("\n begin;");
        }
    }

    public static void addSavepoint(String savePointName) throws SQLException, InterruptedException {
        addSavepoint(getConnection(Thread.currentThread().getName()), savePointName);
    }

    public static void addSavepoint(Connection connection, String savePointName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("\n savepoint " + savePointName);
        }
    }

    public static void commit() throws SQLException, InterruptedException {
        commit(getConnection(Thread.currentThread().getName()));
    }

    public static void commit(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("\n commit;");
        }
    }

    public static void rollback() throws SQLException, InterruptedException {
        rollback(getConnection(Thread.currentThread().getName()));
    }

    public static void rollback(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("\n rollback;");
        }
    }

    public static void rollbackToSavepoint( String savePointName) throws SQLException, InterruptedException {
        rollbackToSavepoint(getConnection(Thread.currentThread().getName()), savePointName);
    }

    public static void rollbackToSavepoint(Connection connection, String savePointName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("\n rollback to " + savePointName);
        }
    }
}
