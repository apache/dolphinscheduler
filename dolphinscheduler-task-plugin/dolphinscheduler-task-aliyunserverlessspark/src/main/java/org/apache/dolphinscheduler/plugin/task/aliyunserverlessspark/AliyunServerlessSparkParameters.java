package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.List;

@Data
@Slf4j
public class AliyunServerlessSparkParameters extends AbstractParameters {
    // connection configurations
    String regionId;
    String accessKeyId;
    String accessKeySecret;
    // spark job configurations
    String workspaceId;
    String resourceQueueId;
    String codeType;
    String jobName;
    String engineReleaseVersion;
    String entryPoint;
    String entryPointArguments;
    String sparkSubmitParameters;
    boolean isProduction;

    @Override
    public boolean checkParameters() {
        return true;
    }
}
