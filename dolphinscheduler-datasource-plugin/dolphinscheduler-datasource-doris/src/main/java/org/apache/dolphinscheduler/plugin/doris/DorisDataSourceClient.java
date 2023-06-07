package org.apache.dolphinscheduler.plugin.doris;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

/**
 * @author xinxing
 * @description 功能描述
 * @create 2023/6/7 14:40
 */
public class DorisDataSourceClient extends CommonDataSourceClient {
    public DorisDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }
}
