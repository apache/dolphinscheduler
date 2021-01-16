package org.apache.dolphinscheduler.remote.rpc.client;

import org.apache.dolphinscheduler.remote.rpc.common.AbstractRpcCallBack;
import org.apache.dolphinscheduler.remote.rpc.common.ConsumerConfigConstants;

/**
 * ConsumerConfig
 */
public class ConsumerConfig {

    private Class<? extends AbstractRpcCallBack> callBackClass;

    private String serviceName;

    private Boolean async = ConsumerConfigConstants.DEFAULT_SYNC;

    private Boolean isOneway = ConsumerConfigConstants.DEFAULT_IS_ONEWAY;

    private Integer retries = ConsumerConfigConstants.DEFAULT_RETRIES;


    public Class<? extends AbstractRpcCallBack> getCallBackClass() {
        return callBackClass;
    }

    public void setCallBackClass(Class<? extends AbstractRpcCallBack> callBackClass) {
        this.callBackClass = callBackClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Boolean getOneway() {
        return isOneway;
    }

    public void setOneway(Boolean oneway) {
        isOneway = oneway;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }
}
