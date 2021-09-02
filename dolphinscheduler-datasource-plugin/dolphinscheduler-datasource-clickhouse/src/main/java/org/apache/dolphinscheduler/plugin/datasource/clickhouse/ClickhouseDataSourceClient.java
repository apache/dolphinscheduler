package org.apache.dolphinscheduler.plugin.datasource.clickhouse;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.dao.datasource.CommonDataSourceClient;

public class ClickhouseDataSourceClient extends CommonDataSourceClient {

    public ClickhouseDataSourceClient(BaseConnectionParam baseConnectionParam) {
        super(baseConnectionParam);
    }


}
