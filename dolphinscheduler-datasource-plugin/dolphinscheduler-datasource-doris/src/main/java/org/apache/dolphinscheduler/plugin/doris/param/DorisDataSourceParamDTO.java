package org.apache.dolphinscheduler.plugin.doris.param;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.enums.DbType;

/**
 * @author xinxing
 * @description 功能描述
 * @create 2023/6/7 14:49
 */
public class DorisDataSourceParamDTO extends BaseDataSourceParamDTO {
    @Override
    public String toString() {
        return "DorisDataSourceParamDTO{"
                + "name='" + name + '\''
                + ", note='" + note + '\''
                + ", host='" + host + '\''
                + ", port=" + port
                + ", database='" + database + '\''
                + ", userName='" + userName + '\''
                + ", password='" + password + '\''
                + ", other='" + other + '\''
                + '}';
    }
    @Override
    public DbType getType() {
        return DbType.DORIS;
    }
}
