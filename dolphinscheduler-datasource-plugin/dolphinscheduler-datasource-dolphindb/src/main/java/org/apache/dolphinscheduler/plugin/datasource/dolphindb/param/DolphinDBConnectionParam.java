package org.apache.dolphinscheduler.plugin.datasource.dolphindb.param;

import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;

public class DolphinDBConnectionParam extends BaseConnectionParam {

    @Override
    public String toString() {
        return "DolphinDBConnectionParam{"
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
