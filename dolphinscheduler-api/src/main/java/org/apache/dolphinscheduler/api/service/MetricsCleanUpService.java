package org.apache.dolphinscheduler.api.service;

public interface MetricsCleanUpService {

    void cleanUpWorkflowMetricsByDefinitionCode(String workflowDefinitionCode);

}
