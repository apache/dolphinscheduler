package org.apache.dolphinscheduler.plugin.datasource.s3.param;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class S3DataSourceParamDTO extends BaseDataSourceParamDTO {

    @Override
    public String toString() {
	  return "OssConnectionParam{" + "Accesskey='" + userName + '\'' + ", SecretKey='" + password + '\'' + ", Location='" + host + '\'' + ", Endpoint='" + database + '\'' + '}';
    }

    @Override
    public DbType getType() {
	  return DbType.S3;
    }
}
