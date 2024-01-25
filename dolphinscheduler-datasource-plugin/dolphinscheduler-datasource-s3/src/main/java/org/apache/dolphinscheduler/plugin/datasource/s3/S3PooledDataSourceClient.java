package org.apache.dolphinscheduler.plugin.datasource.s3;

import org.apache.dolphinscheduler.plugin.datasource.api.client.BasePooledDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class S3PooledDataSourceClient extends BasePooledDataSourceClient {

    public S3PooledDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
	  super(baseConnectionParam, dbType);
    }
}
