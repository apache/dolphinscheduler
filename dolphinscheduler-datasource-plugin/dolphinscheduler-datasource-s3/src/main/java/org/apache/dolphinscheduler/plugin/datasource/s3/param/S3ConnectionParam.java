package org.apache.dolphinscheduler.plugin.datasource.s3.param;

import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;

public class S3ConnectionParam extends BaseConnectionParam {

    @Override
    public String toString() {
	  return "OssConnectionParam{" + "Accesskey='" + user + '\'' + ", SecretKey='" + password + '\'' + ", Location='" + database + '\'' + ", Endpoint='" + jdbcUrl + '\'' + '}';
    }
}
