package org.apache.dolphinscheduler.plugin.datasource.flink.param;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class FlinkDataSourceParamDTO extends BaseDataSourceParamDTO {

    @Override
    public String toString() {
        return "FlinkDataSourceParamDTO{"
                + "host='" + host + '\''
                + ", port=" + port
                + ", database='" + database + '\''
                + ", userName='" + userName + '\''
                + ", password='" + password + '\''
                + ", other='" + other + '\''
                + '}';
    }

    @Override
    public DbType getType() {
        return DbType.FLINK;
    }
}
