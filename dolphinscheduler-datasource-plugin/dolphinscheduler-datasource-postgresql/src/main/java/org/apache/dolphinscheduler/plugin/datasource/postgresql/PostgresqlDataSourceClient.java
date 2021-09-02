package org.apache.dolphinscheduler.plugin.datasource.postgresql;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.dao.datasource.CommonDataSourceClient;

public class PostgresqlDataSourceClient extends CommonDataSourceClient {

    public PostgresqlDataSourceClient(BaseConnectionParam baseConnectionParam) {
        super(baseConnectionParam);
    }

}
