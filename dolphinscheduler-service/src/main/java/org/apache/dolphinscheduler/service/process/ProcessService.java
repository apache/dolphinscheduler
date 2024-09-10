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

package org.apache.dolphinscheduler.service.process;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.model.TaskNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProcessService {

    WorkflowInstance constructWorkflowInstance(Command command,
                                               String host) throws CronParseException, CodeGenerateUtils.CodeGenerateException;

    Optional<WorkflowInstance> findWorkflowInstanceDetailById(int workflowInstanceId);

    WorkflowInstance findWorkflowInstanceById(int workflowInstanceId);

    WorkflowDefinition findWorkflowDefinition(Long workflowDefinitionCode, int workflowDefinitionVersion);

    WorkflowDefinition findWorkflowDefinitionByCode(Long workflowDefinitionCode);

    int deleteWorkflowInstanceById(int workflowInstanceId);

    int deleteAllSubWorkflowByParentId(int workflowInstanceId);

    void removeTaskLogFile(Integer workflowInstanceId);

    List<Long> findAllSubWorkflowDefinitionCode(long workflowDefinitionCode);

    String getTenantForWorkflow(String tenantCode, int userId);

    int deleteWorkflowMapByParentId(int parentWorkflowId);

    WorkflowInstance findSubWorkflowInstance(Integer parentWorkflowInstanceId, Integer parentTaskId);

    WorkflowInstance findParentWorkflowInstance(Integer subWorkflowInstanceId);

    void changeOutParam(TaskInstance taskInstance);

    Schedule querySchedule(int id);

    List<Schedule> queryReleaseSchedulerListByWorkflowDefinitionCode(long workflowDefinitionCode);

    DataSource findDataSourceById(int id);

    <T> List<T> listUnauthorized(int userId, T[] needChecks, AuthorizationType authorizationType);

    User getUserById(int userId);

    String formatTaskAppId(TaskInstance taskInstance);

    int switchVersion(WorkflowDefinition workflowDefinition, WorkflowDefinitionLog workflowDefinitionLog);

    int switchWorkflowTaskRelationVersion(WorkflowDefinition workflowDefinition);

    int switchTaskDefinitionVersion(long taskCode, int taskVersion);

    String getResourceIds(TaskDefinition taskDefinition);

    int saveTaskDefine(User operator, long projectCode, List<TaskDefinitionLog> taskDefinitionLogs, Boolean syncDefine);

    int saveWorkflowDefine(User operator, WorkflowDefinition workflowDefinition, Boolean syncDefine,
                           Boolean isFromWorkflowDefinition);

    int saveTaskRelation(User operator, long projectCode, long workflowDefinitionCode, int workflowDefinitionVersion,
                         List<WorkflowTaskRelationLog> taskRelationList, List<TaskDefinitionLog> taskDefinitionLogs,
                         Boolean syncDefine);

    boolean isTaskOnline(long taskCode);

    DAG<Long, TaskNode, TaskNodeRelation> genDagGraph(WorkflowDefinition workflowDefinition);

    DagData genDagData(WorkflowDefinition workflowDefinition);

    List<WorkflowTaskRelation> findRelationByCode(long workflowDefinitionCode, int workflowDefinitionVersion);

    List<TaskNode> transformTask(List<WorkflowTaskRelation> taskRelationList,
                                 List<TaskDefinitionLog> taskDefinitionLogs);

    DqRule getDqRule(int ruleId);

    List<DqRuleInputEntry> getRuleInputEntry(int ruleId);

    List<DqRuleExecuteSql> getDqExecuteSql(int ruleId);

    DqComparisonType getComparisonTypeById(int id);

    TaskGroupQueue insertIntoTaskGroupQueue(Integer taskId,
                                            String taskName,
                                            Integer groupId,
                                            Integer workflowInstanceId,
                                            Integer priority,
                                            TaskGroupQueueStatus status);

    String findConfigYamlByName(String clusterName);

    void forceWorkflowInstanceSuccessByTaskInstanceId(TaskInstance taskInstance);

    void setGlobalParamIfCommanded(WorkflowDefinition workflowDefinition, Map<String, String> cmdParam);
}
