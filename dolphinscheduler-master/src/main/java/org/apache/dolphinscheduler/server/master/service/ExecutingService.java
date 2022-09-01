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

import com.google.common.collect.Lists;
import lombok.NonNull;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.dto.MasterTaskInstanceDispatchingDto;
import org.apache.dolphinscheduler.remote.dto.TaskInstanceExecuteDetailDto;
import org.apache.dolphinscheduler.remote.dto.WorkflowInstanceExecuteDetailDto;
import org.apache.dolphinscheduler.remote.dto.MasterWorkflowInstanceExecutingListingDto;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.controller.WorkflowExecutionController;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.service.queue.TaskDispatchFailedQueue;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * executing service, to query executing data from memory, such workflow instance
 */
@Component
public class ExecutingService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutionController.class);

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Resource(name = Constants.TASK_PRIORITY_QUEUE)
    private TaskPriorityQueueImpl taskPriorityQueue;

    @Resource(name = Constants.TASK_DISPATCH_FAILED_QUEUE)
    private TaskDispatchFailedQueue taskDispatchFailedQueue;

    public Optional<WorkflowInstanceExecuteDetailDto> queryWorkflowExecutingData(Integer processInstanceId) {
        WorkflowExecuteRunnable workflowExecuteRunnable = processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId);
        if (workflowExecuteRunnable == null) {
            logger.info("workflow execute data not found, maybe it has finished, workflow id:{}", processInstanceId);
            return Optional.empty();
        }
        try {
            WorkflowInstanceExecuteDetailDto workflowInstanceExecuteDetailDto = new WorkflowInstanceExecuteDetailDto();
            BeanUtils.copyProperties(workflowInstanceExecuteDetailDto, workflowExecuteRunnable.getProcessInstance());
            List<TaskInstanceExecuteDetailDto> taskInstanceList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(workflowExecuteRunnable.getAllTaskInstances())) {
                for (TaskInstance taskInstance : workflowExecuteRunnable.getAllTaskInstances()) {
                    TaskInstanceExecuteDetailDto taskInstanceExecuteDetailDto = new TaskInstanceExecuteDetailDto();
                    BeanUtils.copyProperties(taskInstanceExecuteDetailDto, taskInstance);
                    taskInstanceList.add(taskInstanceExecuteDetailDto);
                }
            }
            workflowInstanceExecuteDetailDto.setTaskInstances(taskInstanceList);
            return Optional.of(workflowInstanceExecuteDetailDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("query workflow execute data fail, workflow id:{}", processInstanceId, e);
        }
        return Optional.empty();
    }

    public List<MasterWorkflowInstanceExecutingListingDto> listingExecutingWorkflows() {
        return processInstanceExecCacheManager.getAll()
                .stream()
                .map(workflowExecuteRunnable -> {
                    ProcessInstance processInstance = workflowExecuteRunnable.getProcessInstance();
                    Date startTime;
                    if (processInstance.getRestartTime() != null) {
                        startTime = processInstance.getRestartTime();
                    } else {
                        startTime = processInstance.getStartTime();
                    }
                    return MasterWorkflowInstanceExecutingListingDto.builder()
                            .workflowInstanceId(processInstance.getId())
                            .workflowInstanceName(processInstance.getName())
                            .startTime(processInstance.getStartTime())
                            .costTime(System.currentTimeMillis() - startTime.getTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<MasterTaskInstanceDispatchingDto> listingDispatchingTaskInstance() {
        List<TaskPriority> taskDispatchingList = taskPriorityQueue.getAll();
        List<TaskPriority> taskDispatchFailedList = taskDispatchFailedQueue.getAll();
        List<MasterTaskInstanceDispatchingDto> result = new ArrayList<>();
        result.addAll(transformTaskPriorityToTaskInstanceDispatchingDto(taskDispatchingList));
        result.addAll(transformTaskPriorityToTaskInstanceDispatchingDto(taskDispatchFailedList));
        return result;
    }

    private List<MasterTaskInstanceDispatchingDto> transformTaskPriorityToTaskInstanceDispatchingDto(@NonNull List<TaskPriority> taskInstances) {
        return taskInstances.stream()
                .map(taskPriority -> {
                    TaskExecutionContext taskExecutionContext = taskPriority.getTaskExecutionContext();
                    return MasterTaskInstanceDispatchingDto.builder()
                            .taskInstanceId(taskPriority.getTaskInstanceId())
                            .taskGroupName(taskPriority.getGroupName())
                            .submitTime(taskExecutionContext.getFirstSubmitTime())
                            .dispatchTimes(taskPriority.getDispatchFailedRetryTimes())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
