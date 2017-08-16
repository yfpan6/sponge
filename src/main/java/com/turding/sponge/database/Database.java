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
        return executeInsert(dataSource, insertSql, null);
    }

    public static List<Long> insert(DataSource dataSource,
                                    String prepareInsertSql,
                                    List<Object> prepareValueList) {
       return executeInsert(dataSource, prepareInsertSql, prepareValueList);
    }
    public static int update(DataSource dataSource,
                             String updateSql) {
        return executeUpdate(dataSource, updateSql, null);
    }

    public static int update(DataSource dataSource,
                             String prepareUpdateSql,
                             List<Object> prepareValueList) {
        return executeUpdate(dataSource, prepareUpdateSql, prepareValueList);
    }

    public static int delete(DataSource dataSource,
                             String deleteSql) {
        return executeUpdate(dataSource, deleteSql, null);

    }

    public static int delete(DataSource dataSource,
                             String prepareDeleteSql,
                             List<Object> prepareValueList) {
        return executeUpdate(dataSource, prepareDeleteSql, prepareValueList);
    }

    public static <T> List<T> select(DataSource dataSource,
                                     String selectSql,
                                     ResultSetCallback<T> callback) {
        return select(dataSource, selectSql, null, callback);
    }
    public static <T> List<T> select(DataSource dataSource,
                              String prepareSelectSql,
                              List<Object> prepareValueList,
                              ResultSetCallback<T> callback) {
        debugSql(prepareSelectSql, prepareValueList);

        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stm = conn.prepareStatement(prepareSelectSql);
            if (prepareValueList != null) {
                int size = prepareValueList.size();
                for (int i = 0; i < size; i++) {
                    stm.setObject((i + 1), prepareValueList.get(i));
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

    private static int executeUpdate(DataSource dataSource,
                                     String prepareUpdateSql,
                                     List<Object> prepareValueList) {
        debugSql(prepareUpdateSql, prepareValueList);
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = dataSource.getConnection();
            stm = conn.prepareStatement(prepareUpdateSql);
            if (prepareValueList != null) {
                int len = prepareValueList.size();
                for (int i = 0; i < len; i++) {
                    stm.setObject((i + 1), prepareValueList.get(i));
                }
            }
            return stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn, stm);
        }
    }

    public static List<Long> executeInsert(DataSource dataSource,
                                           String prepareInsertSql,
                                           List<Object> prepareValueList) {
        debugSql(prepareInsertSql, prepareValueList);
        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stm = conn.prepareStatement(prepareInsertSql, Statement.RETURN_GENERATED_KEYS);
            if (prepareValueList != null) {
                int len = prepareValueList.size();
                for (int i = 0; i < len; i++) {
                    stm.setObject((i + 1), prepareValueList.get(i));
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
            throw new RuntimeException(e);
        } finally {
            close(conn, stm, rs);
        }
    }

    private static void debugSql(String prepareSelectSql, List<Object> prepareValueList) {
        if (log.isDebugEnabled()) {
            log.debug("Sql: " + prepareSelectSql);
            log.debug("Parameters: " + prepareValueList);
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
