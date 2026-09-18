package net.servboot.orm.database;

import net.servboot.client.ClientRequestTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class ConnectionManager {
    public static final short MAX_CONNECTIONS = 10;
    private static final Stack<Connection> pool = new Stack<>();
    private static final Map<String, Connection> connections = new LinkedHashMap<>();

    public static void init() throws SQLException {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            pool.add(createConnection());
        }
    }

    private static Connection createConnection() throws SQLException {
        Connection connection = DataBase.getPostgreSqlConnection();
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
        Connection connection;

        if (connections.containsKey(Thread.currentThread().getName())) {
            connection = connections.get(Thread.currentThread().getName());
        } else {
            synchronized (pool) {
                while (pool.isEmpty()) {
                    pool.wait();
                }

                connection = pool.pop();
                String x = Thread.currentThread().getName();
                connections.put(x, connection);
            }

            if (Thread.currentThread() instanceof ClientRequestTask) {
                ((ClientRequestTask) Thread.currentThread()).setOnFinalize(t -> {
                    addConnection(connections.remove(t.getName()));
                });
            }
        }

        return connection;
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

    public static void begin() {
        try {
            begin(getConnection(Thread.currentThread().getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public static void commit() {
        try {
            commit(getConnection(Thread.currentThread().getName()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void commit(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("\n commit;");
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
