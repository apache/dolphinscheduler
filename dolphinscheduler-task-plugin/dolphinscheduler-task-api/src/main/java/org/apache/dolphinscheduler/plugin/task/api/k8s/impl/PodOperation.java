/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.api.k8s.impl;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.internal.OperationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.am.KubernetesApplicationManagerContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sOperation;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.YamlContent;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.*;

@Slf4j
public class PodOperation implements AbstractK8sOperation {


    private static final String PENDING = "Pending";
    private static final String FAILED = "Failed";
    private static final String UNKNOWN = "Unknown";


    private KubernetesClient client;

    private static String SUCCEEDED = "Succeeded";

    public PodOperation(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public HasMetadata buildMetadata(YamlContent yamlContent) {
        return client.apps().deployments().load(yamlContent.getYaml()).get();
    }

    @Override
    public void createOrReplaceMetadata(HasMetadata metadata) {
        client.pods().createOrReplace((Pod) metadata);
    }

    @Override
    public int getState(HasMetadata hasMetadata) {
        Pod pod = (Pod) hasMetadata;
        String phase = pod.getStatus().getPhase();

        if (phase.equals(SUCCEEDED)) {
            return EXIT_CODE_SUCCESS;
        } else if (phase.equals(FAILED)) {
            return EXIT_CODE_FAILURE;
        } else {
            return TaskConstants.RUNNING_CODE;
        }
    }

    @Override
    public Watch createBatchWatcher(String jobName, CountDownLatch countDownLatch,
                                    TaskResponse taskResponse, HasMetadata hasMetadata,
                                    TaskExecutionContext taskRequest) {
        Watcher<Pod> watcher = new Watcher<Pod>() {

            @Override
            public void eventReceived(Action action, Pod pod) {
                try {
                    LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());
                    log.info("event received : job:{} action:{}", pod.getMetadata().getName(), action);
                    if (action == Action.DELETED) {
                        log.error("[K8sJobExecutor-{}] fail in k8s", pod.getMetadata().getName());
                        taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
                        countDownLatch.countDown();
                    } else if (action != Action.ADDED) {
                        int jobStatus = getState(pod);
                        log.info("job {} status {}", pod.getMetadata().getName(), jobStatus);
                        if (jobStatus == TaskConstants.RUNNING_CODE) {
                            return;
                        }
                        setTaskStatus(hasMetadata,jobStatus, String.valueOf(taskRequest.getTaskInstanceId()), taskResponse);
                        countDownLatch.countDown();
                    }
                } finally {
                    LogUtils.removeTaskInstanceLogFullPathMDC();
                }
            }

            @Override
            public void onClose(WatcherException e) {
                log.error("[K8sJobExecutor-{}] fail in k8s: {}", hasMetadata.getMetadata().getName(), e.getMessage());
                taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
                countDownLatch.countDown();
            }
        };
        return client.pods().inNamespace(hasMetadata.getMetadata().getNamespace())
                .withName(jobName)
                .watch(watcher);
    }

    @Override
    public LogWatch getLogWatcher(String labelValue, String namespace) {
        boolean metadataIsReady = false;
        Pod pod = null;
        while (!metadataIsReady) {
            FilterWatchListDeletable<Pod, PodList, PodResource> watchList =
                    getListenPod(labelValue, namespace);
            List<Pod> podList = watchList == null ? null : watchList.list().getItems();
            if (CollectionUtils.isEmpty(podList)) {
                return null;
            }
            pod = podList.get(0);
            String phase = pod.getStatus().getPhase();
            if (phase.equals(PENDING) || phase.equals(UNKNOWN)) {
                ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            } else {
                metadataIsReady = true;
            }
        }
        return client.pods().inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName())
                .inContainer(pod.getMetadata().getName())
                .watchLog();
    }

    /**
     * get driver pod
     *
     * @return
     */
    private FilterWatchListDeletable<Pod, PodList, PodResource> getListenPod(String labelValue,String namespace) {
        List<Pod> podList = null;
        FilterWatchListDeletable<Pod, PodList, PodResource> watchList = null;
        int retryTimes = 0;
        while (CollectionUtils.isEmpty(podList) && retryTimes < MAX_RETRY_TIMES) {
            watchList = client.pods()
                    .inNamespace(namespace)
                    .withLabel(UNIQUE_LABEL_NAME, labelValue);
            podList = watchList.list().getItems();
            if (!CollectionUtils.isEmpty(podList)) {
                break;
            }
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            retryTimes += 1;
        }

        return watchList;
    }

}
