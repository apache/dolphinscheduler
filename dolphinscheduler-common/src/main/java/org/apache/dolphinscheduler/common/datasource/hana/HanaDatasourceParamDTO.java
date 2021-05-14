package org.apache.dolphinscheduler.common.datasource.hana;

import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.enums.DbType;


public class HanaDatasourceParamDTO extends BaseDataSourceParamDTO {
    @Override
    public String toString() {
        return "HanaDatasourceParamDTO{"
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
        return DbType.HANA;
    }
}
