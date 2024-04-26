package org.apache.dolphinscheduler.plugin.datasource.flink.param;

import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;

public class FlinkConnectionParam extends BaseConnectionParam {

    @Override
    public String toString() {
        return "FlinkConnectionParam{"
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
