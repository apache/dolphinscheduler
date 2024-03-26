package org.apache.dolphinscheduler.plugin.datasource.dolphindb.param;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class DolphinDBDataSourceParamDTO extends BaseDataSourceParamDTO {

    @Override
    public DbType getType() {
        return DbType.DOLPHINDB;
    }
}
