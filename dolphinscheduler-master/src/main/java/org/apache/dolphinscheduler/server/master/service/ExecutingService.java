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

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.dto.TaskInstanceExecuteDto;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * executing service, to query executing data from memory, such workflow instance
 */
@Component
@Slf4j
public class ExecutingService {

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    public Optional<WorkflowExecuteDto> queryWorkflowExecutingData(Integer processInstanceId) {
        WorkflowExecuteRunnable workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
        if (workflowExecuteRunnable == null) {
            log.info("workflow execute data not found, maybe it has finished, workflow id:{}", processInstanceId);
            return Optional.empty();
        }
        try {
            WorkflowExecuteDto workflowExecuteDto = new WorkflowExecuteDto();
            BeanUtils.copyProperties(workflowExecuteDto,
                    workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowInstance());
            List<TaskInstanceExecuteDto> taskInstanceList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(workflowExecuteRunnable.getAllTaskInstances())) {
                for (TaskInstance taskInstance : workflowExecuteRunnable.getAllTaskInstances()) {
                    TaskInstanceExecuteDto taskInstanceExecuteDto = new TaskInstanceExecuteDto();
                    BeanUtils.copyProperties(taskInstanceExecuteDto, taskInstance);
                    taskInstanceList.add(taskInstanceExecuteDto);
                }
            }
            workflowExecuteDto.setTaskInstances(taskInstanceList);
            return Optional.of(workflowExecuteDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("query workflow execute data fail, workflow id:{}", processInstanceId, e);
        }
        return Optional.empty();
    }
}
