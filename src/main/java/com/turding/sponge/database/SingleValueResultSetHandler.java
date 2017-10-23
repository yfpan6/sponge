package com.turding.sponge.database;

import com.turding.sponge.util.StringUtil;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yunfeng.pan on 17-6-17.
 */
public class SingleValueResultSetHandler<T> implements ResultSetCallback<T> {

    private String columnName;
    private Class<T> clazz;

    public SingleValueResultSetHandler() {
    }

    public SingleValueResultSetHandler(String columnName, Class<T> clazz) {
        this.columnName = columnName;
        this.clazz = clazz;
    }

    @Override
    public List<T> handle(ResultSet resultSet) throws Exception {
        List<T> resultList = new ArrayList<>();
        if (StringUtil.isBlank(columnName)) {
            columnName = resultSet.getMetaData().getColumnLabel(1);
        }
        while (resultSet.next()) {
            resultList.add((T) SqlUtil.getValue(resultSet, clazz, columnName));
        }
        return resultList;
    }

}
