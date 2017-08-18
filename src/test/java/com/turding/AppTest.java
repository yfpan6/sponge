package com.turding;

import com.turding.sponge.core.Entity;
import com.turding.sponge.core.EntityParser;
import com.turding.sponge.core.Expression;
import com.turding.sponge.core.Exps;
import com.turding.sponge.database.DMLSqlParser;
import com.turding.sponge.database.SqlExpressionParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.sql.Time;
import java.util.Date;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        Entity<TestEntity> entityEntity = EntityParser.of(TestEntity.class).parse().result();
        Expression expression = Exps.sum(Expression.CaseWhen
                .ofCase(Exps.eq("id", 100))
                .when(Exps.val("true")).then(Exps.val(Time.valueOf("18:00:01")))
                .els(Exps.val(new Date()))
                .end());
        SqlExpressionParser.Result result = SqlExpressionParser.of(expression,
                entityEntity)
                .parse()
                .result();
        System.out.println(result.getRawSql());
        System.out.println(DMLSqlParser.of(entityEntity).parseForInsert().result().getPrepareSql());
        System.out.println(DMLSqlParser.of(entityEntity).parseForDelete(Exps.eq("name", 100)).result().getPrepareSql());
    }

}
