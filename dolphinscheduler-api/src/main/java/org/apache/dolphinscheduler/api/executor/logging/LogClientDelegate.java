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
    public String getPartLogString(TaskInstance taskInstance, int skipLineNum, int limit) {
        checkArgs(taskInstance);
        checkNodeExists(taskInstance);
        TaskInstanceLogPageQueryResponse response = localLogClient.getPartLog(taskInstance, skipLineNum, limit);
        if (response.getCode() == 0) {
            return response.getLogContent();
        } else {
            log.warn("trying fetch remote part log: {}", taskInstance.getLogPath());
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
        checkNodeExists(taskInstance);
        TaskInstanceLogFileDownloadResponse response = localLogClient.getWholeLog(taskInstance);
        if (response.getCode() == 0) {
            return response.getLogBytes();
        } else {
            log.warn("trying fetch remote whole log: {}", taskInstance.getLogPath());
            return remoteLogClient.getWholeLog(taskInstance);
        }
    }

    private static void checkArgs(TaskInstance taskInstance) {
        if (taskInstance == null) {
            throw new IllegalArgumentException("canFetchLog task instance is null");
        }
    }

    private void checkNodeExists(TaskInstance taskInstance) {
        RegistryNodeType nodeType;
        if (TaskTypeUtils.isLogicTask(taskInstance.getTaskType())) {
            nodeType = RegistryNodeType.MASTER;
        } else {
            nodeType = RegistryNodeType.WORKER;
        }
        if (!registryClient.checkNodeExists(taskInstance.getHost(), nodeType)) {
            throw new IllegalArgumentException("canFetchLog node " + taskInstance.getHost() + " is not exists");
        }
    }
}
