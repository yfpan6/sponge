package com.turding.sponge.database;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by yunfeng.pan on 17-8-18.
 */
public class SqlContext {
    private DataSource dataSource;

    public static SqlContext of(DataSource dataSource) {
        return new SqlContext();
    }

    public TXSession openTxSession() {
        try {
            return new TXSession(dataSource.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
