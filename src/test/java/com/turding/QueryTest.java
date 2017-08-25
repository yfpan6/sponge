package com.turding;

import com.turding.sponge.core.*;
import com.turding.sponge.database.SqlQueryStructureParser;
import junit.framework.TestCase;

/**
 * Created by yunfeng.pan on 17-8-18.
 */
public class QueryTest extends TestCase {

    public void testQuery() {
        QueryStructure queryStructure = QueryStructure.of(TestEntity.class)
                .setQueryFields("id", "name", "sex", "age").addQueryField(QueryField.of(Exps.sum(Exps.raw("id")), "sum"))
                .filterExp(CombinedExpression.of(Exps.eq("id", 100))
                        .and(Exps.contain("name", "panyunfeng"))
                        .and(Exps.startWith("sex", "男人")))
                .groupBy("id", "name").orderBy(OrderBy.of("id").asc());
        Entity<TestEntity> entity = EntityParser.of(TestEntity.class).parse().result();
        SqlQueryStructureParser.Result result = SqlQueryStructureParser.of(QueryStructure.of(TestEntity.class)
                .filterExp(Exps.eq("id", 100))
                .limit(10)).parse().result();

        System.out.println(result.rawSql());
//        DataSource dataSource = null;
//        List<TestEntity> list = Database.select(dataSource, result.prepareSql(), Arrays.asList(result.prepareValues()),
//                new ResultSetHandler(queryStructure.entityType(), result.entityFields()));
    }
}
