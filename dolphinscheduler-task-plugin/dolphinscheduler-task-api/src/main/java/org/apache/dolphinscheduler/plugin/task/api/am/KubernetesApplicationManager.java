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

import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceManagerType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;

@Slf4j
@AutoService(ApplicationManager.class)
public class KubernetesApplicationManager implements ApplicationManager<KubernetesApplicationManagerContext> {

    private static final String PENDING = "Pending";
    private static final String RUNNING = "Running";
    private static final String FINISH = "Succeeded";
    private static final String FAILED = "Failed";
    private static final String UNKNOWN = "Unknown";

    private static final int MAX_RETRY_TIMES = 10;

    /**
     * Get Kubernetes client connection pool instance
     */
    public final KubernetesClientPool clientPool = KubernetesClientPool.getInstance();

    @Override
    public boolean killApplication(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) throws TaskException {

        boolean isKill;
        String labelValue = kubernetesApplicationManagerContext.getLabelValue();

        FilterWatchListDeletable<Pod, PodList, PodResource> watchList =
                getListenPod(kubernetesApplicationManagerContext);
        try {
            if (getApplicationStatus(kubernetesApplicationManagerContext, watchList).isFailure()) {
                log.error("Driver pod is in FAILED or UNKNOWN status.");
                isKill = false;
            } else {
                String clusterId = getClusterId(kubernetesApplicationManagerContext.getK8sTaskExecutionContext());
                KubernetesClient client = null;
                try {
                    client = getClient(kubernetesApplicationManagerContext);
                    // Retrieve watchList again, as the previous instance of tes client connection pool may have expired
                    FilterWatchListDeletable<Pod, PodList, PodResource> newWatchList =
                            client.pods()
                                    .inNamespace(kubernetesApplicationManagerContext.getK8sTaskExecutionContext()
                                            .getNamespace())
                                    .withLabel(UNIQUE_LABEL_NAME, labelValue);
                    newWatchList.delete();
                    isKill = true;
                } finally {
                    if (client != null) {
                        returnClient(clusterId, client);
                    }
                }
            }
        } catch (Exception e) {
            throw new TaskException("Failed to kill Kubernetes application with label " + labelValue, e);
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
     * @param kubernetesApplicationManagerContext Context
     * @return pods
     */
    @SneakyThrows
    public FilterWatchListDeletable<Pod, PodList, PodResource> getListenPod(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) {
        String clusterId = getClusterId(kubernetesApplicationManagerContext.getK8sTaskExecutionContext());
        KubernetesClient client = null;
        String labelValue = kubernetesApplicationManagerContext.getLabelValue();
        List<Pod> podList = null;
        FilterWatchListDeletable<Pod, PodList, PodResource> watchList = null;
        int retryTimes = 0;
        try {
            client = getClient(kubernetesApplicationManagerContext);
            while (CollectionUtils.isEmpty(podList) && retryTimes < MAX_RETRY_TIMES) {
                watchList = client.pods()
                        .inNamespace(kubernetesApplicationManagerContext.getK8sTaskExecutionContext().getNamespace())
                        .withLabel(UNIQUE_LABEL_NAME, labelValue);
                podList = watchList.list().getItems();
                if (!CollectionUtils.isEmpty(podList)) {
                    break;
                }
                Thread.sleep(SLEEP_TIME_MILLIS);
                retryTimes += 1;
            }
            return watchList;
        } finally {
            if (client != null) {
                returnClient(clusterId, client);
            }
        }
    }

    /**
     * Retrieve Kubernetes clients from the connection pool
     *
     * @param kubernetesApplicationManagerContext Context parameters
     * @return Kubernetes Client
     */
    public KubernetesClient getClient(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) {
        K8sTaskExecutionContext k8sTaskExecutionContext =
                kubernetesApplicationManagerContext.getK8sTaskExecutionContext();

        // Using k8s configuration as cluster identifier
        String clusterId = getClusterId(k8sTaskExecutionContext);
        String kubeConfig = k8sTaskExecutionContext.getConfigYaml();

        try {
            return clientPool.getClient(clusterId, kubeConfig);
        } catch (Exception e) {
            log.error("Failed to get Kubernetes client from pool", e);
            throw new RuntimeException("Failed to get Kubernetes client", e);
        }
    }

    /**
     * Get Cluster Identifier
     */
    public String getClusterId(K8sTaskExecutionContext k8sTaskExecutionContext) {
        // The hash value of kubeconfig is used as the cluster identifier
        String kubeConfig = k8sTaskExecutionContext.getConfigYaml();
        return "k8s-cluster-" + Math.abs((long) kubeConfig.hashCode());
    }

    /**
     * Return the client to the connection pool
     *
     * @param clusterId Cluster Identifier
     * @param client Client to be returned
     */
    public void returnClient(String clusterId, KubernetesClient client) {
        try {
            clientPool.returnClient(clusterId, client);
        } catch (Exception e) {
            log.error("Failed to return Kubernetes client to pool", e);
        }
    }

    /**
     * get application execution status
     *
     * @param kubernetesApplicationManagerContext Context
     * @return TaskExecutionStatus  SUCCESS / FAILURE
     * @throws TaskException throws Exception
     */
    public TaskExecutionStatus getApplicationStatus(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) throws TaskException {
        return getApplicationStatus(kubernetesApplicationManagerContext, null);
    }

    /**
     * get application (driver pod) status
     *
     * @param kubernetesApplicationManagerContext Context
     * @param watchList watchers
     * @return status
     * @throws TaskException throws Exception
     */
    public TaskExecutionStatus getApplicationStatus(KubernetesApplicationManagerContext kubernetesApplicationManagerContext,
                                                    FilterWatchListDeletable<Pod, PodList, PodResource> watchList) throws TaskException {
        String phase;
        try {
            if (Objects.isNull(watchList)) {
                watchList = getListenPod(kubernetesApplicationManagerContext);
            }

            // To avoid the possibility of watchList expiration, retrieve the client again to perform the list operation
            String clusterId = getClusterId(kubernetesApplicationManagerContext.getK8sTaskExecutionContext());
            KubernetesClient client = null;
            try {
                client = getClient(kubernetesApplicationManagerContext);
                String labelValue = kubernetesApplicationManagerContext.getLabelValue();

                // Build the latest watchList again
                FilterWatchListDeletable<Pod, PodList, PodResource> newWatchList =
                        client.pods()
                                .inNamespace(
                                        kubernetesApplicationManagerContext.getK8sTaskExecutionContext().getNamespace())
                                .withLabel(UNIQUE_LABEL_NAME, labelValue);

                List<Pod> driverPod = newWatchList.list().getItems();
                if (!driverPod.isEmpty()) {
                    // cluster mode
                    Pod driver = driverPod.get(0);
                    phase = driver.getStatus().getPhase();
                } else {
                    // client mode
                    phase = FINISH;
                }
            } finally {
                if (client != null) {
                    returnClient(clusterId, client);
                }
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
     * @param kubernetesApplicationManagerContext Context
     * @return Watcher
     */
    @SneakyThrows
    public LogWatch getPodLogWatcher(KubernetesApplicationManagerContext kubernetesApplicationManagerContext) {
        KubernetesClient client = null;
        boolean podIsReady = false;
        Pod pod = null;
        try {
            client = getClient(kubernetesApplicationManagerContext);
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
                    Thread.sleep(SLEEP_TIME_MILLIS);
                } else {
                    podIsReady = true;
                }
            }

            return client.pods().inNamespace(pod.getMetadata().getNamespace())
                    .withName(pod.getMetadata().getName())
                    .inContainer(kubernetesApplicationManagerContext.getContainerName())
                    .watchLog();
        } finally {
            log.debug("Log watch client is not returned immediately, will be managed by caller after watch completes");
        }
    }

}
