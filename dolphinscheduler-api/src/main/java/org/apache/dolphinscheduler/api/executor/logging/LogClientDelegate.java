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
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;
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

    public String getTaskLogString(TaskInstance taskInstance, int skipLineNum, int limit) {
        return getPartLogString(taskInstance, skipLineNum, limit, TaskLogType.LOG);
    }

    public String getTaskOutputString(TaskInstance taskInstance, int skipLineNum, int limit) {
        return getPartLogString(taskInstance, skipLineNum, limit, TaskLogType.OUTPUT);
    }

    private String getPartLogString(TaskInstance taskInstance, int skipLineNum, int limit, TaskLogType taskLogType) {
        checkArgs(taskInstance);
        if (checkNodeExists(taskInstance)) {
            TaskInstanceLogPageQueryResponse response =
                    taskLogType == TaskLogType.LOG
                            ? localLogClient.getTaskLog(taskInstance, skipLineNum, limit)
                            : localLogClient.getTaskOutput(taskInstance, skipLineNum, limit);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogContent();
            } else {
                log.warn("get part log string is not success for task instance {}; reason :{}",
                        taskInstance.getId(), response.getMessage());
                return taskLogType == TaskLogType.LOG
                        ? remoteLogClient.getTaskLogString(taskInstance, skipLineNum, limit)
                        : remoteLogClient.getTaskOutputString(taskInstance, skipLineNum, limit);
            }
        } else {
            return taskLogType == TaskLogType.LOG
                    ? remoteLogClient.getTaskLogString(taskInstance, skipLineNum, limit)
                    : remoteLogClient.getTaskOutputString(taskInstance, skipLineNum, limit);
        }
    }

    /**
     * Retrieves the complete log content for a given task instance as a byte array.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param taskInstance The task instance object, containing information needed for log retrieval.
     * @return A byte array containing the complete log content.
     */
    public byte[] getTaskLogBytes(TaskInstance taskInstance) {
        return getWholeLogBytes(taskInstance, TaskLogType.LOG);
    }

    public byte[] getTaskOutputBytes(TaskInstance taskInstance) {
        return getWholeLogBytes(taskInstance, TaskLogType.OUTPUT);
    }

    private byte[] getWholeLogBytes(TaskInstance taskInstance, TaskLogType taskLogType) {
        checkArgs(taskInstance);
        if (checkNodeExists(taskInstance)) {
            TaskInstanceLogFileDownloadResponse response =
                    taskLogType == TaskLogType.LOG
                            ? localLogClient.getTaskLog(taskInstance)
                            : localLogClient.getTaskOutput(taskInstance);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogBytes();
            } else {
                log.warn("get whole log bytes is not success for task instance {}; reason :{}", taskInstance.getId(),
                        response.getMessage());
                return taskLogType == TaskLogType.LOG
                        ? remoteLogClient.getTaskLogBytes(taskInstance)
                        : remoteLogClient.getTaskOutputBytes(taskInstance);
            }
        } else {
            return taskLogType == TaskLogType.LOG
                    ? remoteLogClient.getTaskLogBytes(taskInstance)
                    : remoteLogClient.getTaskOutputBytes(taskInstance);
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

}
