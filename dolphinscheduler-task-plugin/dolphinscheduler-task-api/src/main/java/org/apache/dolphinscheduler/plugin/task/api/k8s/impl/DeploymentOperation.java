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
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.am.KubernetesApplicationManagerContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sOperation;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.YamlContent;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SLEEP_TIME_MILLIS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.UNIQUE_LABEL_NAME;

public class DeploymentOperation implements AbstractK8sOperation {

    private KubernetesClient client;

    public DeploymentOperation(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public HasMetadata buildMetadata(YamlContent yamlContent) {
        return client.apps().deployments().load(yamlContent.getYaml()).get();
    }

    @Override
    public void createOrReplaceMetadata(HasMetadata metadata) {
        client.apps().deployments().createOrReplace((Deployment) metadata);
    }

    @Override
    public int getState(HasMetadata hasMetadata) {
        return 0;
    }

    @Override
    public Watch createBatchWatcher(CountDownLatch countDownLatch, TaskResponse taskResponse, HasMetadata hasMetadata, TaskExecutionContext taskRequest) {
        return null;
    }


    @Override
    public LogWatch getLogWatcher(String labelValue, String namespace) {
        boolean metadataIsReady = false;
        Deployment deployment = null;
        while (!metadataIsReady) {

            FilterWatchListDeletable<Deployment, DeploymentList, RollableScalableResource<Deployment>> listenDeployment = getListenDeployment(labelValue, namespace);
            List<Deployment> deploymentList = listenDeployment == null ? null : listenDeployment.list().getItems();
            if (CollectionUtils.isEmpty(deploymentList)) {
                return null;
            }
            deployment = deploymentList.get(0);
            DeploymentStatus deploymentStatus = deployment.getStatus();
            if (deploymentStatus != null &&
                    deploymentStatus.getReplicas() != null &&
                    deploymentStatus.getReplicas() > 0 &&
                    deploymentStatus.getAvailableReplicas() != null &&
                    deploymentStatus.getAvailableReplicas() > 0) {
                metadataIsReady = true;
            } else {
                ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            }
        }
        return client.apps().deployments().inNamespace(deployment.getMetadata().getNamespace())
                .withName(deployment.getMetadata().getName())
                .inContainer(deployment.getMetadata().getName())
                .watchLog();
    }

    private FilterWatchListDeletable<Deployment, DeploymentList, RollableScalableResource<Deployment>> getListenDeployment(String labelValue, String namespace) {
        List<Deployment> deploymentList = null;
        FilterWatchListDeletable<Deployment, DeploymentList, RollableScalableResource<Deployment>> watchList = null;
        int retryTimes = 0;
        while (CollectionUtils.isEmpty(deploymentList) && retryTimes < MAX_RETRY_TIMES) {
            watchList = client.apps().deployments()
                    .inNamespace(namespace)
                    .withLabel(UNIQUE_LABEL_NAME, labelValue);
            deploymentList = watchList.list().getItems();
            if (!CollectionUtils.isEmpty(deploymentList)) {
                break;
            }
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            retryTimes += 1;
        }

        return watchList;
    }
}
