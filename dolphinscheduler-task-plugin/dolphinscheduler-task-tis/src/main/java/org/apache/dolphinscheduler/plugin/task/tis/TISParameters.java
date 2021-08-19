package org.apache.dolphinscheduler.plugin.task.tis;

import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.ResourceInfo;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * TIS parameter
 */
public class TISParameters extends AbstractParameters {

    private static final Logger logger = LoggerFactory.getLogger(TISParameters.class);
    /**
     * TIS target job name
     */
    private String targetJobName;

    public String getTargetJobName() {
        return targetJobName;
    }

    public void setTargetJobName(String targetJobName) {
        this.targetJobName = targetJobName;
    }

    @Override
    public boolean checkParameters() {
        if (StringUtils.isBlank(this.targetJobName)) {
            logger.error("checkParameters faild targetJobName can not be null");
            return false;
        }
//        if (getTisHostProp() == null) {
//            logger.error("checkParameters faild getTisHostProp() can not be null");
//            return false;
//        }
        return true;
    }

//    public String getTisHost() {
//        return getTisHostProp().getValue();
//    }

//    private Property getTisHostProp() {
//
//        return this.getVarPoolMap().get(KEY_POOL_VAR_TIS_HOST);
//    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return Collections.emptyList();
    }
}
