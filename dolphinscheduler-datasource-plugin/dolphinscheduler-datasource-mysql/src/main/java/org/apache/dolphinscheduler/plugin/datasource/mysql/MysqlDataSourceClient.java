package org.apache.dolphinscheduler.plugin.datasource.mysql;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.dao.datasource.CommonDataSourceClient;

public class MysqlDataSourceClient extends CommonDataSourceClient {

    public MysqlDataSourceClient(BaseConnectionParam baseConnectionParam) {
        super(baseConnectionParam);
    }


}
