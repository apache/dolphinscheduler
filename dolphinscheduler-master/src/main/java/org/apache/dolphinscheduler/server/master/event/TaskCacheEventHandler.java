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

package org.apache.dolphinscheduler.server.master.event;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.TaskInstanceUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskCacheEventHandler implements TaskEventHandler {

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private DataQualityResultOperator dataQualityResultOperator;

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    /**
     * handle CACHE task event
     * copy a new task instance from the cache task has been successfully run
     * @param taskEvent task event
     */
    @Override
    public void handleTaskEvent(TaskEvent taskEvent) {
        int taskInstanceId = taskEvent.getTaskInstanceId();
        int processInstanceId = taskEvent.getProcessInstanceId();

        WorkflowExecuteRunnable workflowExecuteRunnable = processInstanceExecCacheManager.getByProcessInstanceId(
                processInstanceId);
        Optional<TaskInstance> taskInstanceOptional = workflowExecuteRunnable.getTaskInstance(taskInstanceId);
        if (!taskInstanceOptional.isPresent()) {
            return;
        }
        TaskInstance taskInstance = taskInstanceOptional.get();
        dataQualityResultOperator.operateDqExecuteResult(taskEvent, taskInstance);

        TaskInstance cacheTaskInstance = taskInstanceDao.queryById(taskEvent.getCacheTaskInstanceId());

        // keep the task instance fields
        cacheTaskInstance.setId(taskInstance.getId());
        cacheTaskInstance.setProcessInstanceId(processInstanceId);
        cacheTaskInstance.setProcessInstanceName(taskInstance.getProcessInstanceName());
        cacheTaskInstance.setProcessInstance(taskInstance.getProcessInstance());
        cacheTaskInstance.setProcessDefine(taskInstance.getProcessDefine());
        cacheTaskInstance.setStartTime(taskInstance.getSubmitTime());
        cacheTaskInstance.setSubmitTime(taskInstance.getSubmitTime());
        cacheTaskInstance.setEndTime(new Date());
        cacheTaskInstance.setFlag(Flag.YES);

        TaskInstanceUtils.copyTaskInstance(cacheTaskInstance, taskInstance);

        processService.changeOutParam(taskInstance);

        taskInstanceDao.updateById(taskInstance);
        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(taskEvent.getProcessInstanceId())
                .taskInstanceId(taskEvent.getTaskInstanceId())
                .status(taskEvent.getState())
                .type(StateEventType.TASK_STATE_CHANGE)
                .build();

        workflowExecuteThreadPool.submitStateEvent(stateEvent);

    }

    @Override
    public TaskEventType getHandleEventType() {
        return TaskEventType.CACHE;
    }
}
