package org.apache.dolphinscheduler.plugin.datasource.oracle;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.dao.datasource.CommonDataSourceClient;

public class OracleDataSourceClient extends CommonDataSourceClient {

    public OracleDataSourceClient(BaseConnectionParam baseConnectionParam) {
        super(baseConnectionParam);
    }

}
