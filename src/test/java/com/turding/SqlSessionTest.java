package com.turding;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.turding.sponge.database.SqlSession;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by myron.pan on 18-1-11.
 */
public class SqlSessionTest {

    public static void main(String[] args) {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File("/home/myron/java/codes/turding/sponge/src/test/resource/db.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            DataSource druidDataSource = DruidDataSourceFactory.createDataSource(properties);
            SqlSession sqlSession = SqlSession.of(druidDataSource);
            System.out.println(sqlSession.select(AgentAgentRolesTest.class));
            System.out.println(sqlSession.selectSingleColumnValue("select id from agent_agent_roles",
                    "id", Integer.class));

            AgentAgentRolesTest rolesTest = new AgentAgentRolesTest();
            //rolesTest.setId(110);
            rolesTest.setAgentRoleId(1);
            System.out.println(sqlSession.select(rolesTest));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
