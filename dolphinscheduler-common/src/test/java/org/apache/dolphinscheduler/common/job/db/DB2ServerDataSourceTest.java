package org.apache.dolphinscheduler.common.job.db;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.junit.Assert;
import org.junit.Test;

public class DB2ServerDataSourceTest {
    @Test
    public void getJdbcUrl(){
        BaseDataSource db2Ds = DataSourceFactory.getDatasource(DbType.DB2,
                "{\"user\":\"user\",\"password\":\"xxx\"," +
                        "\"address\":\"db2host\",\"database\":\"db\",\"other\":\"otherPara\"}");
        Assert.assertTrue(db2Ds instanceof DB2ServerDataSource);
        DB2ServerDataSource db2ServerDataSource = (DB2ServerDataSource) db2Ds;
        Assert.assertEquals("db2host/db:otherPara",db2ServerDataSource.getJdbcUrl());
        try {
            db2ServerDataSource.isConnectable();
            Assert.fail("db2 data source can not be connected");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

    }
}
