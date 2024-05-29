package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.util.List;

@Data
@Slf4j
public class AliyunServerlessSparkParameters extends AbstractParameters {
    // connection configurations
//    private String regionId;
//    private String accessKeyId;
//    private String accessKeySecret;
    // spark job configurations
    private String workspaceId;
    private String resourceQueueId;
    private String codeType;
    private String jobName;
    private String engineReleaseVersion;
    private String entryPoint;
    private String entryPointArguments;
    private String sparkSubmitParameters;
    boolean isProduction;
    private int datasource;
    private String type;

    @Override
    public boolean checkParameters() {
        return true;
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();
        resources.put(ResourceType.DATASOURCE, datasource);
        return resources;
    }
}
