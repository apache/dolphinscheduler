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

package org.apache.dolphinscheduler.service.task;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskRemoteHost;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskRemoteHostDao;
import org.apache.dolphinscheduler.plugin.task.api.BashTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.SSHSessionHost;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

@Component
public class TaskInstanceHandler {

    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceHandler.class);

    private static final String REMOTE_HOST_CODE = "remoteHostCode";

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskRemoteHostDao taskRemoteHostDao;

    /**
     * Determine whether the task needs to be run in the bash environment
     * @param taskInstance task instance {@link TaskInstance}
     * @return result
     */
    public boolean needRunningOnBash(TaskInstance taskInstance) {
        // TODO The first step is only for SHELL and Python tasks
        return "SHELL".equals(taskInstance.getTaskType()) || "PYTHON".equals(taskInstance.getTaskType());
    }

    /**
     * Generate bash execution context {@link BashTaskExecutionContext}
     * @param taskInstance task instance {@link TaskInstance}
     * @return BashTaskExecutionContext
     */
    public BashTaskExecutionContext createBashTaskExecutionContext(TaskInstance taskInstance) {
        if (!needRunningOnBash(taskInstance)) {
            return null;
        }

        BashTaskExecutionContext bashTaskExecutionContext = new BashTaskExecutionContext();
        bashTaskExecutionContext.setSessionHost(createSessionHost(taskInstance));

        return bashTaskExecutionContext;
    }

    /**
     * Generate task ssh session host
     * @param taskInstance task instance {@link TaskInstance}
     * @return remote session host {@link SSHSessionHost}
     */
    public SSHSessionHost createSessionHost(TaskInstance taskInstance) {
        Map<String, Object> taskParameters = getTaskParameters(taskInstance);

        if (taskParameters == null || !taskParameters.containsKey(REMOTE_HOST_CODE)
                || taskParameters.get(REMOTE_HOST_CODE) == null) {
            logger.warn("cannot find task parameters or do not contain remote_host_code, task instance code: {}",
                    taskInstance.getTaskCode());
            return null;
        }

        long remoteHostCode = (long) taskParameters.get(REMOTE_HOST_CODE);
        TaskRemoteHost taskRemoteHost = taskRemoteHostDao.getTaskRemoteHostByCode(remoteHostCode);
        SSHSessionHost sessionHost = new SSHSessionHost();
        BeanUtils.copyProperties(taskRemoteHost, sessionHost);

        return sessionHost;
    }

    public Map<String, Object> getTaskParameters(TaskInstance taskInstance) {
        TaskDefinition taskDefinition = taskDefinitionDao.findTaskDefinition(
                taskInstance.getTaskCode(),
                taskInstance.getTaskDefinitionVersion());

        return JSONUtils.parseObject(
                taskDefinition.getTaskParams(),
                new TypeReference<Map<String, Object>>() {
                });
    }

}
