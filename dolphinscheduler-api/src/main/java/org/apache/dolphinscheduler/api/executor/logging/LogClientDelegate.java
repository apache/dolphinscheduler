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

package org.apache.dolphinscheduler.api.executor.logging;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.extract.common.transportor.WorkflowInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.WorkflowInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogClientDelegate {

    @Autowired
    private LocalLogClient localLogClient;
    @Autowired
    private RemoteLogClient remoteLogClient;
    @Autowired
    private RegistryClient registryClient;

    /**
     * Retrieves a portion of the log string for a given task instance.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param taskInstance The task instance object, containing information needed for log retrieval.
     * @param skipLineNum The number of log lines to skip from the beginning.
     * @param limit The maximum number of log lines to retrieve.
     * @return A string containing the specified portion of the log.
     */
    public String getPartLogString(TaskInstance taskInstance, int skipLineNum, int limit) {
        checkArgs(taskInstance);
        if (checkNodeExists(taskInstance)) {
            TaskInstanceLogPageQueryResponse response = localLogClient.getPartLog(taskInstance, skipLineNum, limit);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogContent();
            } else {
                log.warn("get part log string is not success for task instance {}; reason :{}",
                        taskInstance.getId(), response.getMessage());
                return remoteLogClient.getPartLog(taskInstance, skipLineNum, limit);
            }
        } else {
            return remoteLogClient.getPartLog(taskInstance, skipLineNum, limit);
        }
    }

    /**
     * Retrieves the complete log content for a given task instance as a byte array.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param taskInstance The task instance object, containing information needed for log retrieval.
     * @return A byte array containing the complete log content.
     */
    public byte[] getWholeLogBytes(TaskInstance taskInstance) {
        checkArgs(taskInstance);
        if (checkNodeExists(taskInstance)) {
            TaskInstanceLogFileDownloadResponse response = localLogClient.getWholeLog(taskInstance);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogBytes();
            } else {
                log.warn("get whole log bytes is not success for task instance {}; reason :{}", taskInstance.getId(),
                        response.getMessage());
                return remoteLogClient.getWholeLog(taskInstance);
            }
        } else {
            return remoteLogClient.getWholeLog(taskInstance);
        }
    }

    private static void checkArgs(TaskInstance taskInstance) {
        if (taskInstance == null) {
            throw new IllegalArgumentException("canFetchLog task instance is null");
        }
    }

    private boolean checkNodeExists(TaskInstance taskInstance) {
        RegistryNodeType nodeType;
        if (TaskTypeUtils.isLogicTask(taskInstance.getTaskType())) {
            nodeType = RegistryNodeType.MASTER;
        } else {
            nodeType = RegistryNodeType.WORKER;
        }
        boolean exists = registryClient.checkNodeExists(taskInstance.getHost(), nodeType);
        if (!exists) {
            log.warn("Node {} does not exist for task instance {}", taskInstance.getHost(), taskInstance.getId());
        }
        return exists;
    }

    /**
     * Retrieves a portion of the log string for a given workflow instance.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param workflowInstance The workflow instance object, containing information needed for log retrieval.
     * @param skipLineNum The number of log lines to skip from the beginning.
     * @param limit The maximum number of log lines to retrieve.
     * @return A string containing the specified portion of the log.
     */
    public String getWorkflowPartLogString(WorkflowInstance workflowInstance, int skipLineNum, int limit) {
        checkWorkflowArgs(workflowInstance);
        if (checkWorkflowNodeExists(workflowInstance)) {
            WorkflowInstanceLogPageQueryResponse response =
                    localLogClient.getWorkflowPartLog(workflowInstance, skipLineNum, limit);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogContent();
            } else {
                log.warn("get part log string is not success for workflow instance {}; reason :{}",
                        workflowInstance.getId(), response.getMessage());
                return remoteLogClient.getWorkflowPartLog(workflowInstance, skipLineNum, limit);
            }
        } else {
            return remoteLogClient.getWorkflowPartLog(workflowInstance, skipLineNum, limit);
        }
    }

    /**
     * Retrieves the complete log content for a given workflow instance as a byte array.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param workflowInstance The workflow instance object, containing information needed for log retrieval.
     * @return A byte array containing the complete log content.
     */
    public byte[] getWorkflowWholeLogBytes(WorkflowInstance workflowInstance) {
        checkWorkflowArgs(workflowInstance);
        if (checkWorkflowNodeExists(workflowInstance)) {
            WorkflowInstanceLogFileDownloadResponse response = localLogClient.getWorkflowWholeLog(workflowInstance);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogBytes();
            } else {
                log.warn("get whole log bytes is not success for workflow instance {}; reason :{}",
                        workflowInstance.getId(), response.getMessage());
                return remoteLogClient.getWorkflowWholeLog(workflowInstance);
            }
        } else {
            return remoteLogClient.getWorkflowWholeLog(workflowInstance);
        }
    }

    private static void checkWorkflowArgs(WorkflowInstance workflowInstance) {
        if (workflowInstance == null) {
            throw new IllegalArgumentException("workflow instance is null");
        }
    }

    private boolean checkWorkflowNodeExists(WorkflowInstance workflowInstance) {
        String host = workflowInstance.getHost();
        if (host == null || host.isEmpty()) {
            log.warn("Host is null or empty for workflow instance {}", workflowInstance.getId());
            return false;
        }
        boolean exists = registryClient.checkNodeExists(host, RegistryNodeType.MASTER);
        if (!exists) {
            log.warn("Node {} does not exist for workflow instance {}", host, workflowInstance.getId());
        }
        return exists;
    }

}
