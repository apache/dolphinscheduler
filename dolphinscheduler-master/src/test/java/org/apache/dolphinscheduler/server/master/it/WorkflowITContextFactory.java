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

package org.apache.dolphinscheduler.server.master.it;

import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.ProcessTaskRelationLogDao;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowITContextFactory {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProcessDefinitionDao processDefinitionDao;

    @Autowired
    private ProcessDefinitionLogDao processDefinitionLogDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessTaskRelationLogDao processTaskRelationLogDao;

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    public WorkflowITContext initializeContextFromYaml(final String yamlPath) {
        final WorkflowITContext workflowITContext = YamlFactory.load(yamlPath);
        initializeProjectToDB(workflowITContext.getProject());
        initializeWorkflowDefinitionToDB(workflowITContext.getWorkflow());
        initializeTaskDefinitionsToDB(workflowITContext.getTasks());
        initializeTaskRelationsToDB(workflowITContext.getTaskRelations());
        if (workflowITContext.getWorkflowInstance() != null) {
            initializeWorkflowInstanceToDB(workflowITContext.getWorkflowInstance());
        }
        if (CollectionUtils.isNotEmpty(workflowITContext.getTaskInstances())) {
            initializeTaskInstancesToDB(workflowITContext.getTaskInstances());
        }
        return workflowITContext;
    }

    private void initializeTaskInstancesToDB(List<TaskInstance> taskInstances) {
        for (TaskInstance taskInstance : taskInstances) {
            taskInstanceDao.insert(taskInstance);
        }
    }

    private void initializeWorkflowInstanceToDB(WorkflowInstance workflowInstance) {
        processInstanceDao.insert(workflowInstance);
    }

    private void initializeWorkflowDefinitionToDB(final WorkflowDefinition workflowDefinition) {
        processDefinitionDao.insert(workflowDefinition);
        final WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
        workflowDefinitionLog.setOperator(workflowDefinition.getUserId());
        workflowDefinitionLog.setOperateTime(new Date());
        processDefinitionLogDao.insert(workflowDefinitionLog);
    }

    private void initializeTaskDefinitionsToDB(final List<TaskDefinition> taskDefinitions) {
        for (final TaskDefinition taskDefinition : taskDefinitions) {
            taskDefinitionDao.insert(taskDefinition);

            final TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
            taskDefinitionLog.setOperator(taskDefinition.getUserId());
            taskDefinitionLog.setOperateTime(new Date());
            taskDefinitionLogDao.insert(taskDefinitionLog);
        }
    }

    private void initializeTaskRelationsToDB(final List<WorkflowTaskRelation> taskRelations) {
        for (final WorkflowTaskRelation taskRelation : taskRelations) {
            processTaskRelationMapper.insert(taskRelation);

            final WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog(taskRelation);
            processTaskRelationLog.setOperateTime(new Date());
            processTaskRelationLogDao.insert(processTaskRelationLog);
        }
    }

    private void initializeProjectToDB(final Project project) {
        projectDao.insert(project);
    }

}
