package org.apache.dolphinscheduler.plugin.doris.param;

import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;

/**
 * @author xinxing
 * @description 功能描述
 * @create 2023/6/7 14:47
 */
public class DorisConnectionParam extends BaseConnectionParam {
    @Override
    public String toString() {
        return "DorisConnectionParam{"
                + "user='" + user + '\''
                + ", password='" + password + '\''
                + ", address='" + address + '\''
                + ", database='" + database + '\''
                + ", jdbcUrl='" + jdbcUrl + '\''
                + ", driverLocation='" + driverLocation + '\''
                + ", driverClassName='" + driverClassName + '\''
                + ", validationQuery='" + validationQuery + '\''
                + ", other='" + other + '\''
                + '}';
    }
}
