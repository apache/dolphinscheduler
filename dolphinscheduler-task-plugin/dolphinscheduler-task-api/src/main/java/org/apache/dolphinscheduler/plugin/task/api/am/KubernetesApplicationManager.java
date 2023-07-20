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

package org.apache.dolphinscheduler.plugin.task.api.am;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SLEEP_TIME_MILLIS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.UNIQUE_LABEL_NAME;

import org.apache.dolphinscheduler.common.enums.ResourceManagerType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;

@Slf4j
@AutoService(ApplicationManager.class)
public class KubernetesApplicationManager implements ApplicationManager {

    private static final String PENDING = "Pending";
    private static final String RUNNING = "Running";
    private static final String FINISH = "Succeeded";
    private static final String FAILED = "Failed";
    private static final String UNKNOWN = "Unknown";

    private static final int MAX_RETRY_TIMES = 10;

    /**
     * cache k8s client for same task
     */
    private final Map<String, KubernetesClient> cacheClientMap = new ConcurrentHashMap<>();

    @Override
    public boolean killApplication(ApplicationManagerContext applicationManagerContext) throws TaskException {
        KubernetesApplicationManagerContext kubernetesApplicationManagerContext =
                (KubernetesApplicationManagerContext) applicationManagerContext;

        boolean isKill;
        String labelValue = kubernetesApplicationManagerContext.getLabelValue();
        FilterWatchListDeletable<Pod, PodList, PodResource> watchList =
                getListenPod(kubernetesApplicationManagerContext);
        try {
            if (getApplicationStatus(kubernetesApplicationManagerContext, watchList).isFailure()) {
                log.error("Driver pod is in FAILED or UNKNOWN status.");
                isKill = false;
            } else {
                watchList.delete();
                isKill = true;
            }
        } catch (Exception e) {
            throw new TaskException("Failed to kill Kubernetes application with label " + labelValue, e);
        } finally {
            // remove client cache after killing application
            removeCache(labelValue);
        }

        return isKill;
    }

    @Override
    public ResourceManagerType getResourceManagerType() {
        return ResourceManagerType.KUBERNETES;
    }

    /**
     * get driver pod
     *
     * @param kubernetesApplicationManagerContext
     * @return
     */
    private FilterWatchListDeletable<Pod, PodList, PodResource> getListenPod(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) {
        KubernetesClient client = getClient(kubernetesApplicationManagerContext);
        String labelValue = kubernetesApplicationManagerContext.getLabelValue();
        List<Pod> podList = null;
        FilterWatchListDeletable<Pod, PodList, PodResource> watchList = null;
        int retryTimes = 0;
        while (CollectionUtils.isEmpty(podList) && retryTimes < MAX_RETRY_TIMES) {
            watchList = client.pods()
                    .inNamespace(kubernetesApplicationManagerContext.getK8sTaskExecutionContext().getNamespace())
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

    /**
     * create client or get from cache map
     *
     * @param kubernetesApplicationManagerContext
     * @return
     */
    private KubernetesClient getClient(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) {
        K8sTaskExecutionContext k8sTaskExecutionContext =
                kubernetesApplicationManagerContext.getK8sTaskExecutionContext();
        return cacheClientMap.computeIfAbsent(kubernetesApplicationManagerContext.getLabelValue(),
                key -> new KubernetesClientBuilder()
                        .withConfig(Config.fromKubeconfig(k8sTaskExecutionContext.getConfigYaml())).build());
    }

    public void removeCache(String cacheKey) {
        try (KubernetesClient ignored = cacheClientMap.remove(cacheKey)) {
        }
    }

    /**
     * get application execution status
     *
     * @param kubernetesApplicationManagerContext
     * @return TaskExecutionStatus  SUCCESS / FAILURE
     * @throws TaskException
     */
    public TaskExecutionStatus getApplicationStatus(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) throws TaskException {
        return getApplicationStatus(kubernetesApplicationManagerContext, null);
    }

    /**
     * get application (driver pod) status
     *
     * @param kubernetesApplicationManagerContext
     * @param watchList
     * @return
     * @throws TaskException
     */
    private TaskExecutionStatus getApplicationStatus(KubernetesApplicationManagerContext kubernetesApplicationManagerContext,
                                                     FilterWatchListDeletable<Pod, PodList, PodResource> watchList) throws TaskException {
        String phase;
        try {
            if (Objects.isNull(watchList)) {
                watchList = getListenPod(kubernetesApplicationManagerContext);
            }
            List<Pod> driverPod = watchList.list().getItems();
            if (!driverPod.isEmpty()) {
                // cluster mode
                Pod driver = driverPod.get(0);
                phase = driver.getStatus().getPhase();
            } else {
                // client mode
                phase = FINISH;
            }
        } catch (Exception e) {
            throw new TaskException("Failed to get Kubernetes application status", e);
        }

        return phase.equals(FAILED) || phase.equals(UNKNOWN) ? TaskExecutionStatus.FAILURE
                : TaskExecutionStatus.SUCCESS;
    }

    /**
     * get pod's log watcher
     *
     * @param kubernetesApplicationManagerContext
     * @return
     */
    public LogWatch getPodLogWatcher(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) {
        KubernetesClient client = getClient(kubernetesApplicationManagerContext);
        boolean podIsReady = false;
        Pod pod = null;
        while (!podIsReady) {
            FilterWatchListDeletable<Pod, PodList, PodResource> watchList =
                    getListenPod(kubernetesApplicationManagerContext);
            List<Pod> podList = watchList == null ? null : watchList.list().getItems();
            if (CollectionUtils.isEmpty(podList)) {
                return null;
            }
            pod = podList.get(0);
            String phase = pod.getStatus().getPhase();
            if (phase.equals(PENDING) || phase.equals(UNKNOWN)) {
                ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            } else {
                podIsReady = true;
            }
        }

        return client.pods().inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName())
                .inContainer(kubernetesApplicationManagerContext.getContainerName())
                .watchLog();
    }

}
