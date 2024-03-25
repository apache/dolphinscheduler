package org.apache.dolphinscheduler.plugin.datasource.flink;

import org.apache.dolphinscheduler.plugin.datasource.api.client.BaseAdHocDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class FlinkAdHocDataSourceClient extends BaseAdHocDataSourceClient {

    public FlinkAdHocDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }
}
