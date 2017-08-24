package com.turding.sponge.database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TXSession
 *
 * Created by yunfeng.pan on 17-8-18.
 */
public class TXSession {

    public enum Level {
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
        NONE(Connection.TRANSACTION_NONE);

        private int level;
        Level(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    private Connection connection;
    private PreparedStatement preparedStatement;
    private boolean closed;

    TXSession(Connection connection, Level level) {
        this.connection = connection;
        open(level);
    }

    private TXSession open(Level level) {
        try {
            connection.setAutoCommit(false);
            if (level != null && level != Level.NONE) {
                connection.setTransactionIsolation(level.level);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public static TXSession of(DataSource dataSource) {
        return of(dataSource, null);
    }

    public static TXSession of(DataSource dataSource, Level level) {
        try {
            return new TXSession(dataSource.getConnection(), level);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static TXSession of(Connection connection) {
        return of(connection, null);
    }

    public static TXSession of(Connection connection, Level level) {
        return new TXSession(connection, level);
    }

    public TXSession execute(List<String> sqls) {
        for (String sql : sqls) {
            execute(sql);
        }
        return this;
    }

    public TXSession execute(String sql) {
        return execute(sql, new Object[0]);
    }

    public TXSession execute(Map<String, List<Object>> prepareSqlValues) {
        prepareSqlValues.forEach((prepareSql, values) ->
            execute(prepareSql, values)
        );
        return this;
    }

    public TXSession execute(String prepareSql, List<Object> values) {
        return execute(prepareSql, values == null ? null : values.toArray());
    }

    public TXSession execute(String prepareSql, Object... values) {
        if (closed) {
            throw new IllegalStateException("tx session has closed.");
        }
        try {
            preparedStatement = connection.prepareStatement(prepareSql);
            if (values != null) {
                for (int i = 0, index = 1; i < values.length; ) {
                    preparedStatement.setObject(index, values[i]);
                    index = i++;
                }
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void close() {
        closed = true;
        try {
            connection.setAutoCommit(true);
            Database.close(connection, preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
