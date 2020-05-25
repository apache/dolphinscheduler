package org.apache.dolphinscheduler.dao.datasource;

import org.junit.Assert;
import org.junit.Test;

import static org.apache.dolphinscheduler.common.Constants.ORACLE_DB_CONNECT_TYPE;
import static org.junit.Assert.*;

public class OracleDataSourceTest {

    @Test
    public void getJdbcUrl() {
        //Oracle JDBC Thin ServiceName:Method One
        OracleDataSource oracleDataSource = new OracleDataSource();
        oracleDataSource.setAddress("jdbc:oracle:thin:@//127.0.0.1:1521");
        oracleDataSource.setDatabase("test");
        oracleDataSource.setPassword("123456");
        oracleDataSource.setUser("test");
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());
        //set fake principal
        oracleDataSource.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());

        //Oracle JDBC Thin ServiceName:Method Two
        oracleDataSource.setAddress("jdbc:oracle:thin:@127.0.0.1:1521");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());
        //set fake principal
        oracleDataSource.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521/test",
                oracleDataSource.getJdbcUrl());


        //Oracle JDBC Thin using SID
        OracleDataSource oracleDataSource2 = new OracleDataSource();
        oracleDataSource2.setAddress("jdbc:oracle:thin:@127.0.0.1:1521");
        oracleDataSource2.setDatabase("test");
        oracleDataSource2.setPassword("123456");
        oracleDataSource2.setUser("test");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521/test",
                oracleDataSource2.getJdbcUrl());
        //set fake principal
        oracleDataSource2.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521/test",
                oracleDataSource2.getJdbcUrl());
    }

    @Test
    public void oracleDBConnectType() {
        String oracleConnectType = "type";
        Assert.assertEquals(oracleConnectType, ORACLE_DB_CONNECT_TYPE );
    }

}