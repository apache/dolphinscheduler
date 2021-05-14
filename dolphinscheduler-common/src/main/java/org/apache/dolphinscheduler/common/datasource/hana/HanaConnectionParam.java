package org.apache.dolphinscheduler.common.datasource.hana;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;


public class HanaConnectionParam extends BaseConnectionParam {
    @Override
    public String toString() {
        return "HanaConnectionParam{"
                + "user='" + user + '\''
                + ", password='" + password + '\''
                + ", address='" + address + '\''
                + ", database='" + database + '\''
                + ", jdbcUrl='" + jdbcUrl + '\''
                + ", other='" + other + '\''
                + '}';
    }
}
