package com.turding;

import com.turding.sponge.core.*;
import com.turding.sponge.database.Database;
import com.turding.sponge.database.SqlQueryStructureParser;
import junit.framework.TestCase;

/**
 * Created by yunfeng.pan on 17-8-18.
 */
public class SqlQueryStructureParserTest extends TestCase{

    public void testParse() {
        QueryStructure queryStructure = QueryStructure.of(TestEntity.class)
                .setQueryFields("id", "name", "sex", "age").addQueryField(QueryField.of(Exps.avg(Exps.raw("id")), "sum"))
                .filterExp(CombinedExpression.of(Exps.eq("id", 100))
                        .and(Exps.contain("name", "panyunfeng"))
                        .and(Exps.startWith("sex", "男人")))
        .groupBy("id", "name").orderBy(OrderBy.of("id").asc());

        System.out.println(SqlQueryStructureParser.of(queryStructure).parse().result().prepareSql());
    }
}
