package org.apache.dolphinscheduler.plugin.datasource.flink;

import org.apache.dolphinscheduler.spi.datasource.AdHocDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.PooledDataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class FlinkDataSourceChannel implements DataSourceChannel {

    @Override
    public AdHocDataSourceClient createAdHocDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        return new FlinkAdHocDataSourceClient(baseConnectionParam, dbType);
    }

    @Override
    public PooledDataSourceClient createPooledDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        return new FlinkPooledDataSourceClient(baseConnectionParam, dbType);
    }
}