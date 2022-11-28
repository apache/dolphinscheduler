package org.apache.dolphinscheduler.server.worker.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.spark.SparkConstants;
import org.apache.dolphinscheduler.plugin.task.spark.SparkParameters;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @author yangkai1@corp.netease.com
 */
public class FailOverUtils {

    public static boolean supportExitAfterSubmitTask(String taskType, String taskParams) {
        if (taskType.equalsIgnoreCase("SPARK")) {
            SparkParameters sparkParameters = JSONUtils.parseObject(taskParams, SparkParameters.class);
            if (Objects.nonNull(sparkParameters)) {
                return StringUtils.isEmpty(sparkParameters.getDeployMode())
                        || sparkParameters.getDeployMode().equals(SparkConstants.DEPLOY_MODE_CLUSTER);
            }
        }
        return false;
    }
}
