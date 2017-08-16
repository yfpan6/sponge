package com.turding.sponge.database;

import java.sql.ResultSet;
import java.util.List;

/**
 * Created by yunfeng.pan on 17-6-17.
 */
public interface ResultSetCallback<T> {

    public List<T> handle(ResultSet resultSet) throws Exception;

}
