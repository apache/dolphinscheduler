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
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ProcessService {
    @Transactional
    ProcessInstance handleCommand(Logger logger, String host, Command command);

    void moveToErrorCommand(Command command, String message);

    int createCommand(Command command);

    List<Command> findCommandPage(int pageSize, int pageNumber);

    List<Command> findCommandPageBySlot(int pageSize, int pageNumber, int masterCount, int thisMasterSlot);

    boolean verifyIsNeedCreateCommand(Command command);

    ProcessInstance findProcessInstanceDetailById(int processId);

    List<TaskDefinition> getTaskNodeListByDefinition(long defineCode);

    ProcessInstance findProcessInstanceById(int processId);

    ProcessDefinition findProcessDefineById(int processDefinitionId);

    ProcessDefinition findProcessDefinition(Long processDefinitionCode, int version);

    ProcessDefinition findProcessDefinitionByCode(Long processDefinitionCode);

    int deleteWorkProcessInstanceById(int processInstanceId);

    int deleteAllSubWorkProcessByParentId(int processInstanceId);

    void removeTaskLogFile(Integer processInstanceId);

    void deleteWorkTaskInstanceByProcessInstanceId(int processInstanceId);

    void recurseFindSubProcess(long parentCode, List<Long> ids);

    void createRecoveryWaitingThreadCommand(Command originCommand, ProcessInstance processInstance);

    Tenant getTenantForProcess(int tenantId, int userId);

    Environment findEnvironmentByCode(Long environmentCode);

    void setSubProcessParam(ProcessInstance subProcessInstance);

    TaskInstance submitTaskWithRetry(ProcessInstance processInstance, TaskInstance taskInstance, int commitRetryTimes, int commitInterval);

    @Transactional(rollbackFor = Exception.class)
    TaskInstance submitTask(ProcessInstance processInstance, TaskInstance taskInstance);

    void createSubWorkProcess(ProcessInstance parentProcessInstance, TaskInstance task);

    Map<String, String> getGlobalParamMap(String globalParams);

    Command createSubProcessCommand(ProcessInstance parentProcessInstance,
                                    ProcessInstance childInstance,
                                    ProcessInstanceMap instanceMap,
                                    TaskInstance task);

    TaskInstance submitTaskInstanceToDB(TaskInstance taskInstance, ProcessInstance processInstance);

    ExecutionStatus getSubmitTaskState(TaskInstance taskInstance, ProcessInstance processInstance);

    void saveProcessInstance(ProcessInstance processInstance);

    int saveCommand(Command command);

    boolean saveTaskInstance(TaskInstance taskInstance);

    boolean createTaskInstance(TaskInstance taskInstance);

    boolean updateTaskInstance(TaskInstance taskInstance);

    TaskInstance findTaskInstanceById(Integer taskId);

    List<TaskInstance> findTaskInstanceByIdList(List<Integer> idList);

    void packageTaskInstance(TaskInstance taskInstance, ProcessInstance processInstance);

    void updateTaskDefinitionResources(TaskDefinition taskDefinition);

    List<Integer> findTaskIdByInstanceState(int instanceId, ExecutionStatus state);

    List<TaskInstance> findValidTaskListByProcessId(Integer processInstanceId);

    List<TaskInstance> findPreviousTaskListByWorkProcessId(Integer processInstanceId);

    int updateWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap);

    int createWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap);

    ProcessInstanceMap findWorkProcessMapByParent(Integer parentWorkProcessId, Integer parentTaskId);

    int deleteWorkProcessMapByParentId(int parentWorkProcessId);

    ProcessInstance findSubProcessInstance(Integer parentProcessId, Integer parentTaskId);

    ProcessInstance findParentProcessInstance(Integer subProcessId);

    int updateProcessInstance(ProcessInstance processInstance);

    void changeOutParam(TaskInstance taskInstance);

    List<String> convertIntListToString(List<Integer> intList);

    Schedule querySchedule(int id);

    List<Schedule> queryReleaseSchedulerListByProcessDefinitionCode(long processDefinitionCode);

    Map<Long, String> queryWorkerGroupByProcessDefinitionCodes(List<Long> processDefinitionCodeList);

    List<DependentProcessDefinition> queryDependentProcessDefinitionByProcessDefinitionCode(long processDefinitionCode);

    List<ProcessInstance> queryNeedFailoverProcessInstances(String host);

    List<String> queryNeedFailoverProcessInstanceHost();

    @Transactional(rollbackFor = RuntimeException.class)
    void processNeedFailoverProcessInstances(ProcessInstance processInstance);

    List<TaskInstance> queryNeedFailoverTaskInstances(String host);

    DataSource findDataSourceById(int id);

    int updateProcessInstanceState(Integer processInstanceId, ExecutionStatus executionStatus);

    ProcessInstance findProcessInstanceByTaskId(int taskId);

    List<UdfFunc> queryUdfFunListByIds(Integer[] ids);

    String queryTenantCodeByResName(String resName, ResourceType resourceType);

    List<Schedule> selectAllByProcessDefineCode(long[] codes);

    ProcessInstance findLastSchedulerProcessInterval(Long definitionCode, DateInterval dateInterval);

    ProcessInstance findLastManualProcessInterval(Long definitionCode, DateInterval dateInterval);

    ProcessInstance findLastRunningProcess(Long definitionCode, Date startTime, Date endTime);

    String queryUserQueueByProcessInstance(ProcessInstance processInstance);

    ProjectUser queryProjectWithUserByProcessInstanceId(int processInstanceId);

    String getTaskWorkerGroup(TaskInstance taskInstance);

    List<Project> getProjectListHavePerm(int userId);

    <T> List<T> listUnauthorized(int userId, T[] needChecks, AuthorizationType authorizationType);

    User getUserById(int userId);

    Resource getResourceById(int resourceId);

    List<Resource> listResourceByIds(Integer[] resIds);

    String formatTaskAppId(TaskInstance taskInstance);

    int switchVersion(ProcessDefinition processDefinition, ProcessDefinitionLog processDefinitionLog);

    int switchProcessTaskRelationVersion(ProcessDefinition processDefinition);

    int switchTaskDefinitionVersion(long taskCode, int taskVersion);

    String getResourceIds(TaskDefinition taskDefinition);

    int saveTaskDefine(User operator, long projectCode, List<TaskDefinitionLog> taskDefinitionLogs, Boolean syncDefine);

    int saveProcessDefine(User operator, ProcessDefinition processDefinition, Boolean syncDefine, Boolean isFromProcessDefine);

    int saveTaskRelation(User operator, long projectCode, long processDefinitionCode, int processDefinitionVersion,
                         List<ProcessTaskRelationLog> taskRelationList, List<TaskDefinitionLog> taskDefinitionLogs,
                         Boolean syncDefine);

    boolean isTaskOnline(long taskCode);

    DAG<String, TaskNode, TaskNodeRelation> genDagGraph(ProcessDefinition processDefinition);

    DagData genDagData(ProcessDefinition processDefinition);

    List<TaskDefinitionLog> genTaskDefineList(List<ProcessTaskRelation> processTaskRelations);

    List<TaskDefinitionLog> getTaskDefineLogListByRelation(List<ProcessTaskRelation> processTaskRelations);

    TaskDefinition findTaskDefinition(long taskCode, int taskDefinitionVersion);

    List<ProcessTaskRelation> findRelationByCode(long processDefinitionCode, int processDefinitionVersion);

    List<TaskNode> transformTask(List<ProcessTaskRelation> taskRelationList, List<TaskDefinitionLog> taskDefinitionLogs);

    Map<ProcessInstance, TaskInstance> notifyProcessList(int processId);

    DqExecuteResult getDqExecuteResultByTaskInstanceId(int taskInstanceId);

    int updateDqExecuteResultUserId(int taskInstanceId);

    int updateDqExecuteResultState(DqExecuteResult dqExecuteResult);

    int deleteDqExecuteResultByTaskInstanceId(int taskInstanceId);

    int deleteTaskStatisticsValueByTaskInstanceId(int taskInstanceId);

    DqRule getDqRule(int ruleId);

    List<DqRuleInputEntry> getRuleInputEntry(int ruleId);

    List<DqRuleExecuteSql> getDqExecuteSql(int ruleId);

    DqComparisonType getComparisonTypeById(int id);

    boolean acquireTaskGroup(int taskId,
                             String taskName, int groupId,
                             int processId, int priority);

    boolean robTaskGroupResouce(TaskGroupQueue taskGroupQueue);

    boolean acquireTaskGroupAgain(TaskGroupQueue taskGroupQueue);

    void releaseAllTaskGroup(int processInstanceId);

    TaskInstance releaseTaskGroup(TaskInstance taskInstance);

    void changeTaskGroupQueueStatus(int taskId, TaskGroupQueueStatus status);

    TaskGroupQueue insertIntoTaskGroupQueue(Integer taskId,
                                            String taskName, Integer groupId,
                                            Integer processId, Integer priority, TaskGroupQueueStatus status);

    int updateTaskGroupQueueStatus(Integer taskId, int status);

    int updateTaskGroupQueue(TaskGroupQueue taskGroupQueue);

    TaskGroupQueue loadTaskGroupQueue(int taskId);

    void sendStartTask2Master(ProcessInstance processInstance, int taskId,
                              org.apache.dolphinscheduler.remote.command.CommandType taskType);

    ProcessInstance loadNextProcess4Serial(long code, int state, int id);
}
