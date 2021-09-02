package org.apache.dolphinscheduler.plugin.datasource.sqlserver;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.dao.datasource.CommonDataSourceClient;

public class SqlserverDataSourceClient extends CommonDataSourceClient {

    public SqlserverDataSourceClient(BaseConnectionParam baseConnectionParam) {
        super(baseConnectionParam);
    }

}
