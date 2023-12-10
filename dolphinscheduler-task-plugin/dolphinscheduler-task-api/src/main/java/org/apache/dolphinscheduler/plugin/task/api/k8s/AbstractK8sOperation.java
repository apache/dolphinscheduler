package org.apache.dolphinscheduler.plugin.task.api.k8s;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.YamlContent;

import java.util.concurrent.CountDownLatch;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.*;

public interface AbstractK8sOperation{

    int MAX_RETRY_TIMES =3;

    HasMetadata buildMetadata(YamlContent yamlContent);

    void createOrReplaceMetadata(HasMetadata metadata);

    int getState(HasMetadata hasMetadata);

    Watch createBatchWatcher(String jobName, CountDownLatch countDownLatch,
                             TaskResponse taskResponse, HasMetadata hasMetadata,
                             TaskExecutionContext taskRequest);

    LogWatch getLogWatcher(String labelValue, String namespace);

    default void setTaskStatus(HasMetadata metadata,int jobStatus, String taskInstanceId, TaskResponse taskResponse) {
        if (jobStatus == EXIT_CODE_SUCCESS || jobStatus == EXIT_CODE_FAILURE) {
            if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(Integer.valueOf(taskInstanceId))) {
                log.info("[K8sYamlJobExecutor-{}] killed", metadata.getMetadata().getName());
                taskResponse.setExitStatusCode(EXIT_CODE_KILL);
            } else if (jobStatus == EXIT_CODE_SUCCESS) {
                log.info("[K8sYamlJobExecutor-{}] succeed in k8s", metadata.getMetadata().getName());
                taskResponse.setExitStatusCode(EXIT_CODE_SUCCESS);
            } else {
                log.error("[K8sYamlJobExecutor-{}] fail in k8s", metadata.getMetadata().getName());
                taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
            }
        }
    }
}
