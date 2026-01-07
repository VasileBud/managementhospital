package server.repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Repository {
    // Supabase pooler (PgBouncer transaction pooling) does not work reliably with server-side prepared statements.
    // Disable them to avoid errors like: "prepared statement 'S_5' already exists".
    private static final String URL = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:6543/postgres"
            + "?prepareThreshold=0&preferQueryMode=simple";
    private static final String USER = "postgres.kpvooltkaebzdkinypzh";
    private static final String PASSWORD = "RazvanSiVasi";

    private static final int MAX_POOL_SIZE = readIntProperty("db.pool.max", 10);
    private static final int MIN_IDLE = readIntProperty("db.pool.min", 2);
    private static final BlockingQueue<Connection> POOL = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
    private static final AtomicInteger TOTAL_CONNECTIONS = new AtomicInteger(0);
    private static final Object CREATE_LOCK = new Object();

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL driver not found.");
        }
        warmPool();
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = borrowConnection();
        return wrap(conn);
    }

    private static void warmPool() {
        int target = Math.min(MIN_IDLE, MAX_POOL_SIZE);
        for (int i = 0; i < target; i++) {
            try {
                Connection conn = createConnection();
                if (POOL.offer(conn)) {
                    TOTAL_CONNECTIONS.incrementAndGet();
                } else {
                    closeSilently(conn);
                }
            } catch (SQLException ignored) {
                break;
            }
        }
    }

    private static Connection borrowConnection() throws SQLException {
        while (true) {
            Connection conn = POOL.poll();
            if (conn != null) {
                if (isValid(conn)) {
                    return conn;
                }
                closeAndDecrement(conn);
            }

            Connection created = tryCreateConnection();
            if (created != null) {
                return created;
            }

            try {
                conn = POOL.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for DB connection.", e);
            }

            if (isValid(conn)) {
                return conn;
            }
            closeAndDecrement(conn);
        }
    }

    private static Connection tryCreateConnection() throws SQLException {
        synchronized (CREATE_LOCK) {
            if (TOTAL_CONNECTIONS.get() >= MAX_POOL_SIZE) {
                return null;
            }
            TOTAL_CONNECTIONS.incrementAndGet();
        }

        try {
            return createConnection();
        } catch (SQLException e) {
            TOTAL_CONNECTIONS.decrementAndGet();
            throw e;
        }
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static Connection wrap(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                Repository.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandler(connection)
        );
    }

    private static void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
        }

        if (!isValid(connection)) {
            closeAndDecrement(connection);
            return;
        }

        if (!POOL.offer(connection)) {
            closeAndDecrement(connection);
        }
    }

    private static boolean isValid(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void closeAndDecrement(Connection connection) {
        closeSilently(connection);
        TOTAL_CONNECTIONS.decrementAndGet();
    }

    private static void closeSilently(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private static int readIntProperty(String key, int fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static final class PooledConnectionHandler implements InvocationHandler {
        private final Connection connection;
        private boolean closed;

        private PooledConnectionHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                if (!closed) {
                    closed = true;
                    releaseConnection(connection);
                }
                return null;
            }

            if (closed) {
                throw new SQLException("Connection is closed.");
            }

            try {
                return method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
