package com.turding.sponge.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    private Connection conn;
    private PreparedStatement preparedStatement;
    private boolean closed;

    public TXSession(Connection connection) {
        this.conn = connection;
        open();
    }

    private TXSession open() {
        return open(Level.NONE);
    }

    private TXSession open(Level level) {
        try {
            conn.setAutoCommit(false);
            if (level != null && level != Level.NONE) {
                conn.setTransactionIsolation(level.level);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public TXSession execute(String prepareSql, Object... values) {
        try {
            preparedStatement = conn.prepareStatement(prepareSql);
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
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            end();
        }
    }

    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            end();
        }
    }

    private void end() {
        closed = true;
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Database.close(conn, preparedStatement);
    }
}
