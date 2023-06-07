package org.apache.dolphinscheduler.plugin.doris;

import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;

/**
 * @author xinxing
 * @description 功能描述
 * @create 2023/6/7 14:43
 */
public class DorisDataSourceChannel implements DataSourceChannel {
    @Override
    public DataSourceClient createDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        return null;
    }
}
