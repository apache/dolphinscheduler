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

package org.apache.dolphinscheduler.plugin.task.api.k8s;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.LogWatch;

/**
 * AbstractK8sOperation defines Operation for User-customized YAML tasks
 */
public interface AbstractK8sOperation {

    int MAX_RETRY_TIMES = 3;

    /**
     * Builds metadata for Kubernetes resource from user-customized YAML content string.
     *
     * @param yamlContentStr user-customized YAML content string.
     * @return The {@link HasMetadata} object representing the metadata of the Kubernetes resource.
     */
    HasMetadata buildMetadata(String yamlContentStr);

    /**
     * Creates or replaces a resource in the Kubernetes cluster.
     *
     * @param metadata The {@link HasMetadata} object representing the metadata of the Kubernetes resource
     * @param taskInstanceId task instance id
     * @throws Exception if error occurred in creating or replacing a resource
     */
    void createOrReplaceMetadata(HasMetadata metadata, int taskInstanceId) throws Exception;

    /**
     * stop a resource in the kubernetes cluster
     *
     * @param metadata {@link HasMetadata} object representing the metadata of the Kubernetes resource
     * @return a list of StatusDetails
     * @throws Exception if error occurred in stopping a resource
     */
    List<StatusDetails> stopMetadata(HasMetadata metadata) throws Exception;

    /**
     * Gets the state of a Kubernetes resource.
     *
     * @param hasMetadata {@link HasMetadata} object representing the metadata.
     * @return An integer representing the state of the Pod.
     */
    int getState(HasMetadata hasMetadata);

    /**
     * Creates a watch to monitor the state of the Kubernetes resource.
     *
     * @param countDownLatch A CountDownLatch that will be counted down when the Pod's state changes or an error occurs.
     * @param taskResponse the response of the task.
     * @param hasMetadata {@link HasMetadata} object representing the Kubernetes resource metadata.
     * @param taskRequest Context information for the task, including task instance ID and process instance ID.
     * @return A {@link Watch} object that monitors the specified Pod and triggers events based on the Pod's status.
     */
    Watch createBatchWatcher(CountDownLatch countDownLatch,
                             TaskResponse taskResponse, HasMetadata hasMetadata,
                             TaskExecutionContext taskRequest);

    /**
     * Creates a log watcher for a Pod.
     *
     * @param labelValue The unique label value to filter and identify the Pod.
     * @param namespace The namespace where Pod locates. If the namespace is not specified a default namespace is used.
     * @return A {@link LogWatch} object that allows watching the logs of the identified Pod.
     *         Returns null if no Pod is found or if the Pod is not in a state where logs can be watched.
     */
    LogWatch getLogWatcher(String labelValue, String namespace);

    /**
     * Sets the status of a task.
     *
     * @param jobStatus The status of the job defined in {@link TaskConstants}.
     * @param taskResponse the response of the task.
     */
    default void setTaskStatus(int jobStatus, TaskResponse taskResponse) {
        if (jobStatus == TaskConstants.EXIT_CODE_SUCCESS || jobStatus == TaskConstants.EXIT_CODE_FAILURE) {
            if (jobStatus == TaskConstants.EXIT_CODE_SUCCESS) {
                taskResponse.setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
            } else {
                taskResponse.setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            }
        }
    }
}
