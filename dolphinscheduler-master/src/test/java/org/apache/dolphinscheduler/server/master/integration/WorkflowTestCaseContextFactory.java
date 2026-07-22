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

package org.apache.dolphinscheduler.server.master.integration;

import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskRelationLogDao;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowTestCaseContextFactory {

    private static final String DEFAULT_MASTER_ADDRESS = "127.0.0.1:5678";

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Autowired
    private WorkflowTaskRelationLogDao workflowTaskRelationLogDao;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskGroupDao taskGroupDao;

    @Autowired
    private IEnvironmentDao environmentDao;

    @Autowired
    private MasterConfig masterConfig;

    public WorkflowTestCaseContext initializeContextFromYaml(final String yamlPath) {
        final WorkflowTestCaseContext workflowTestCaseContext = YamlFactory.load(yamlPath);
        normalizeDefaultMasterAddress(workflowTestCaseContext);
        initializeProjectToDB(workflowTestCaseContext.getProject());
        initializeWorkflowDefinitionToDB(workflowTestCaseContext.getWorkflows());
        initializeTaskDefinitionsToDB(workflowTestCaseContext.getTasks());
        initializeTaskRelationsToDB(workflowTestCaseContext.getTaskRelations());

        initializeWorkflowInstancesToDB(workflowTestCaseContext.getWorkflowInstances());
        initializeTaskInstancesToDB(workflowTestCaseContext.getTaskInstances());
        initializeTaskGroupsToDB(workflowTestCaseContext.getTaskGroups());
        initializeEnvironmentToDB(workflowTestCaseContext.getEnvironments());
        return workflowTestCaseContext;
    }

    private void normalizeDefaultMasterAddress(final WorkflowTestCaseContext workflowTestCaseContext) {
        normalizeWorkflowInstanceHost(workflowTestCaseContext.getWorkflowInstances());
        normalizeTaskInstanceHost(workflowTestCaseContext.getTaskInstances());
    }

    private void normalizeWorkflowInstanceHost(final List<WorkflowInstance> workflowInstances) {
        if (CollectionUtils.isEmpty(workflowInstances)) {
            return;
        }
        for (final WorkflowInstance workflowInstance : workflowInstances) {
            if (DEFAULT_MASTER_ADDRESS.equals(workflowInstance.getHost())) {
                workflowInstance.setHost(masterConfig.getMasterAddress());
            }
        }
    }

    private void normalizeTaskInstanceHost(final List<TaskInstance> taskInstances) {
        if (CollectionUtils.isEmpty(taskInstances)) {
            return;
        }
        for (final TaskInstance taskInstance : taskInstances) {
            if (DEFAULT_MASTER_ADDRESS.equals(taskInstance.getHost())) {
                taskInstance.setHost(masterConfig.getMasterAddress());
            }
        }
    }

    private void initializeTaskInstancesToDB(List<TaskInstance> taskInstances) {
        if (CollectionUtils.isEmpty(taskInstances)) {
            return;
        }
        for (TaskInstance taskInstance : taskInstances) {
            taskInstanceDao.insert(taskInstance);
        }
    }

    private void initializeWorkflowInstancesToDB(List<WorkflowInstance> workflowInstances) {
        if (CollectionUtils.isEmpty(workflowInstances)) {
            return;
        }
        for (WorkflowInstance workflowInstance : workflowInstances) {
            workflowInstanceDao.insert(workflowInstance);
        }
    }

    private void initializeWorkflowDefinitionToDB(final List<WorkflowDefinition> workflowDefinitions) {
        if (CollectionUtils.isEmpty(workflowDefinitions)) {
            return;
        }
        for (final WorkflowDefinition workflowDefinition : workflowDefinitions) {
            workflowDefinitionDao.insert(workflowDefinition);
            final WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
            workflowDefinitionLog.setOperator(workflowDefinition.getUserId());
            workflowDefinitionLog.setOperateTime(new Date());
            workflowDefinitionLogDao.insert(workflowDefinitionLog);
        }
    }

    private void initializeTaskDefinitionsToDB(final List<TaskDefinition> taskDefinitions) {
        if (CollectionUtils.isEmpty(taskDefinitions)) {
            return;
        }
        for (final TaskDefinition taskDefinition : taskDefinitions) {
            taskDefinitionDao.insert(taskDefinition);

            final TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog(taskDefinition);
            taskDefinitionLog.setOperator(taskDefinition.getUserId());
            taskDefinitionLog.setOperateTime(new Date());
            taskDefinitionLogDao.insert(taskDefinitionLog);
        }
    }

    private void initializeTaskRelationsToDB(final List<WorkflowTaskRelation> taskRelations) {
        if (CollectionUtils.isEmpty(taskRelations)) {
            return;
        }
        for (final WorkflowTaskRelation taskRelation : taskRelations) {
            workflowTaskRelationMapper.insert(taskRelation);

            final WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog(taskRelation);
            processTaskRelationLog.setOperateTime(new Date());
            workflowTaskRelationLogDao.insert(processTaskRelationLog);
        }
    }

    private void initializeProjectToDB(final Project project) {
        projectDao.insert(project);
    }

    private void initializeTaskGroupsToDB(final List<TaskGroup> taskGroups) {
        if (CollectionUtils.isEmpty(taskGroups)) {
            return;
        }
        for (final TaskGroup taskGroup : taskGroups) {
            taskGroupDao.insert(taskGroup);
        }
    }

    private void initializeEnvironmentToDB(final List<Environment> environments) {
        if (CollectionUtils.isEmpty(environments)) {
            return;
        }
        for (final Environment environment : environments) {
            environmentDao.insert(environment);
        }
    }

}
