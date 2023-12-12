package org.apache.dolphinscheduler.plugin.datasource.dolphindb;

import org.apache.dolphinscheduler.spi.datasource.AdHocDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.PooledDataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class DolphinDBDataSourceChannel implements DataSourceChannel {

    @Override
    public AdHocDataSourceClient createAdHocDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        return new DolphinDBAdHocDataSourceClient(baseConnectionParam, dbType);
    }

    @Override
    public PooledDataSourceClient createPooledDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        return new DolphinDBPooledDataSourceClient(baseConnectionParam, dbType);
    }
}
