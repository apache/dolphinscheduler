package org.apache.dolphinscheduler.spi.task.request;

import org.apache.dolphinscheduler.spi.task.UdfFuncBean;
import org.apache.dolphinscheduler.spi.task.UdfFuncBean.UdfFuncDeserializer;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 *  SQL Task ExecutionContext
 *  to master/worker task transport
 */
public class SQLTaskRequest extends TaskRequest {

    /**
     * warningGroupId
     */
    private int warningGroupId;

    /**
     * connectionParams
     */
    private String connectionParams;
    /**
     * udf function tenant code map
     */
    @JsonDeserialize(keyUsing = UdfFuncDeserializer.class)
    private Map<UdfFuncBean,String> udfFuncTenantCodeMap;


    public int getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(int warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public Map<UdfFuncBean, String> getUdfFuncTenantCodeMap() {
        return udfFuncTenantCodeMap;
    }

    public void setUdfFuncTenantCodeMap(Map<UdfFuncBean, String> udfFuncTenantCodeMap) {
        this.udfFuncTenantCodeMap = udfFuncTenantCodeMap;
    }

    public String getConnectionParams() {
        return connectionParams;
    }

    public void setConnectionParams(String connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public String toString() {
        return "SQLTaskExecutionContext{"
                + "warningGroupId=" + warningGroupId
                + ", connectionParams='" + connectionParams + '\''
                + ", udfFuncTenantCodeMap=" + udfFuncTenantCodeMap
                + '}';
    }
}
