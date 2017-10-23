package com.turding.sponge.database;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yunfeng.pan on 17-6-15.
 */
@Slf4j
public class Database {

    public static List<Long> insert(DataSource dataSource, String insertSql) {
        return insert(dataSource, insertSql, null);
    }

    public static List<Long> insert(Connection conn, String insertSql) {
        return executeInsert(conn, insertSql, null);
    }

    public static List<Long> insert(DataSource dataSource,
                                    String prepareInsertSql,
                                    List<Object> prepareValues) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return executeInsert(conn, prepareInsertSql, prepareValues);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }

    public static int update(DataSource dataSource,
                             String updateSql) {
        return executeUpdate(dataSource, updateSql, null);
    }

    public static int update(DataSource dataSource,
                             String prepareUpdateSql,
                             List<Object> prepareValues) {
        return executeUpdate(dataSource, prepareUpdateSql, prepareValues);
    }

    public static int delete(DataSource dataSource,
                             String deleteSql) {
        return executeUpdate(dataSource, deleteSql, null);

    }

    public static int delete(DataSource dataSource,
                             String prepareDeleteSql,
                             List<Object> prepareValues) {
        return executeUpdate(dataSource, prepareDeleteSql, prepareValues);
    }

    public static <T> List<T> select(DataSource dataSource,
                                     String selectSql,
                                     ResultSetCallback<T> callback) {
        return select(dataSource, selectSql, null, callback);
    }

    public static <T> List<T> select(DataSource dataSource,
                              String prepareSelectSql,
                              List<Object> prepareValues,
                              ResultSetCallback<T> callback) {
        debugSql(prepareSelectSql, prepareValues);

        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stm = conn.prepareStatement(prepareSelectSql);
            if (prepareValues != null) {
                int size = prepareValues.size();
                for (int i = 0; i < size; i++) {
                    stm.setObject((i + 1), prepareValues.get(i));
                }
            }
            rs = stm.executeQuery();
            if (callback != null) {
                return callback.handle(rs);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(conn, stm, rs);
        }
    }

    public static int executeUpdate(DataSource dataSource,
                                     String prepareUpdateSql,
                                     List<Object> prepareValues) {
        try {
            return executeUpdate(dataSource.getConnection(), prepareUpdateSql, prepareValues);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static int executeUpdate(Connection connection,
                                    String prepareUpdateSql,
                                    List<Object> prepareValues) {
        debugSql(prepareUpdateSql, prepareValues);
        PreparedStatement stm = null;
        try {
            stm = connection.prepareStatement(prepareUpdateSql);
            if (prepareValues != null) {
                int len = prepareValues.size();
                for (int i = 0; i < len; i++) {
                    stm.setObject((i + 1), prepareValues.get(i));
                }
            }
            return stm.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            close(connection, stm);
        }
    }

    public static List<Long> executeInsert(DataSource dataSource,
                                           String prepareInsertSql,
                                           List<Object> prepareValues) {
        try {
            return executeInsert(dataSource.getConnection(), prepareInsertSql, prepareValues);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Long> executeInsert(Connection connection,
                                           String prepareInsertSql,
                                           List<Object> prepareValues) {
        debugSql(prepareInsertSql, prepareValues);
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = connection.prepareStatement(prepareInsertSql, Statement.RETURN_GENERATED_KEYS);
            if (prepareValues != null) {
                int len = prepareValues.size();
                for (int i = 0; i < len; i++) {
                    stm.setObject((i + 1), prepareValues.get(i));
                }
            }
            stm.executeUpdate();
            rs = stm.getGeneratedKeys();
            List<Long> generatedKeyList = new ArrayList<>();
            while (rs.next()) {
                generatedKeyList.add(rs.getLong(1));
            }
            return generatedKeyList;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            close(connection, stm, rs);
        }
    }

    public static boolean execute(DataSource dataSource, String sql) {
        try {
            return execute(dataSource.getConnection(), sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean execute(Connection connection, String sql) {
        debugSql(sql, null);
        Statement stm = null;
        try {
            stm = connection.createStatement();
            return stm.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            close(connection, stm);
        }
    }

    private static void debugSql(String prepareSelectSql, List<Object> prepareValues) {
        if (log.isDebugEnabled()) {
            log.debug("Sql: " + prepareSelectSql);
            log.debug("Parameters: " + prepareValues);
        }
    }

    public static void close(Connection conn, Statement stm) {
        close(stm);
        close(conn);
    }

    public static void close(Connection conn, Statement stm, ResultSet rs) {
        close(rs);
        close(stm);
        close(conn);
    }

    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
