package org.apache.dolphinscheduler.plugin.task.api.k8s.impl;


import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.API_VERSION;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CPU;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.IMAGE_PULL_POLICY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LAYER_LABEL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LAYER_LABEL_VALUE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MEMORY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MI;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAME_LABEL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RESTART_POLICY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_INSTANCE_ID;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.k8s.K8sTaskMainParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;

/**
 * K8sTaskExecutor used to submit k8s task to K8S
 */
public class K8sTaskExecutor extends AbstractK8sTaskExecutor {

    public K8sTaskExecutor(Logger logger, TaskExecutionContext taskRequest) {
        super(logger, taskRequest);
    }

    @Override
    public Job buildK8sJob(TaskExecutionContext taskRequest, K8sTaskMainParameters k8sTaskParameters) {
        String taskInstanceId = String.valueOf(taskRequest.getTaskInstanceId());
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        String image = k8sTaskParameters.getImage();
        Map<String, String> otherParams = k8sTaskParameters.getParamsMap();
        Double podMem = k8sTaskParameters.getMinMemorySpace();
        Double podCpu = k8sTaskParameters.getMinCpuCores();
        Double limitPodMem = podMem * 2;
        Double limitPodCpu = podCpu * 2;
        int retryNum = 0;
        String k8sJobName = String.format("%s-%s", taskName, taskInstanceId);
        Map<String, Quantity> reqRes = new HashMap<>();
        reqRes.put(MEMORY, new Quantity(String.format("%s%s", podMem, MI)));
        reqRes.put(CPU, new Quantity(String.valueOf(podCpu)));
        Map<String, Quantity> limitRes = new HashMap<>();
        limitRes.put(MEMORY, new Quantity(String.format("%s%s", limitPodMem, MI)));
        limitRes.put(CPU, new Quantity(String.valueOf(limitPodCpu)));
        Map<String, String> labelMap = new HashMap<>();
        labelMap.put(LAYER_LABEL, LAYER_LABEL_VALUE);
        labelMap.put(NAME_LABEL, k8sJobName);
        EnvVar taskInstanceIdVar = new EnvVar(TASK_INSTANCE_ID, taskInstanceId, null);
        List<EnvVar> envVars = new ArrayList<>();
        envVars.add(taskInstanceIdVar);
        if (MapUtils.isNotEmpty(otherParams)) {
            for (String param : otherParams.keySet()) {
                EnvVar envVar = new EnvVar(param, otherParams.get(param), null);
                envVars.add(envVar);
            }
        }
        Job job = new JobBuilder()
            .withApiVersion(API_VERSION)
            .withNewMetadata()
            .withName(k8sJobName)
            .withLabels(labelMap)
            .endMetadata()
            .withNewSpec()
            .withTtlSecondsAfterFinished(0)
            .withNewTemplate()
            .withNewSpec()
            .addNewContainer()
            .withName(k8sJobName)
            .withImage(image)
            .withImagePullPolicy(IMAGE_PULL_POLICY)
            .withResources(new ResourceRequirements(limitRes, reqRes))
            .withEnv(envVars)
            .endContainer()
            .withRestartPolicy(RESTART_POLICY)
            .endSpec()
            .endTemplate()
            .withBackoffLimit(retryNum)
            .endSpec()
            .build();
        return job;
    }

}
