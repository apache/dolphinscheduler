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

package org.apache.dolphinscheduler.server.master.runner.task.dependent;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskInitializeException;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.server.master.runner.task.BaseAsyncLogicTask;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DependentLogicTask extends BaseAsyncLogicTask<DependentParameters> {

    public static final String TASK_TYPE = "DEPENDENT";

    private final ProjectDao projectDao;
    private final ProcessDefinitionDao processDefinitionDao;
    private final TaskDefinitionDao taskDefinitionDao;
    private final TaskInstanceDao taskInstanceDao;
    private final ProcessInstanceDao processInstanceDao;

    private final ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    public DependentLogicTask(TaskExecutionContext taskExecutionContext,
                              ProjectDao projectDao,
                              ProcessDefinitionDao processDefinitionDao,
                              TaskDefinitionDao taskDefinitionDao,
                              TaskInstanceDao taskInstanceDao,
                              ProcessInstanceDao processInstanceDao,
                              ProcessInstanceExecCacheManager processInstanceExecCacheManager) throws LogicTaskInitializeException {
        super(taskExecutionContext,
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId())
                        .getTaskInstance(taskExecutionContext.getTaskInstanceId())
                        .orElseThrow(() -> new LogicTaskInitializeException(
                                "Cannot find the task instance in workflow execute runnable"))
                        .getDependency());
        this.projectDao = projectDao;
        this.processDefinitionDao = processDefinitionDao;
        this.taskDefinitionDao = taskDefinitionDao;
        this.taskInstanceDao = taskInstanceDao;
        this.processInstanceDao = processInstanceDao;
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;

    }

    @Override
    public AsyncTaskExecuteFunction getAsyncTaskExecuteFunction() {
        return new DependentAsyncTaskExecuteFunction(taskExecutionContext,
                taskParameters,
                projectDao,
                processDefinitionDao,
                taskDefinitionDao,
                taskInstanceDao,
                processInstanceDao);
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        WorkflowExecuteRunnable workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        if (workflowExecuteRunnable == null) {
            log.error("Cannot find the WorkflowExecuteRunnable");
            return;
        }
        TaskInstance taskInstance =
                workflowExecuteRunnable.getTaskInstance(taskExecutionContext.getTaskInstanceId()).orElse(null);
        if (taskInstance == null) {
            log.error("Cannot find the TaskInstance in workflowExecuteRunnable");
            return;
        }
        taskInstance.setState(TaskExecutionStatus.PAUSE);
        taskInstance.setEndTime(new Date());
        taskInstanceDao.upsertTaskInstance(taskInstance);
        super.pause();
    }

}
