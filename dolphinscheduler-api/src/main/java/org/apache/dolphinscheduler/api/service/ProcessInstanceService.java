
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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.DynamicSubWorkflowDto;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryRequest;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * process instance service
 */

public interface ProcessInstanceService {

    /**
     * return top n SUCCESS process instance order by running time which started between startTime and endTime
     */
    Map<String, Object> queryTopNLongestRunningProcessInstance(User loginUser,
                                                               long projectCode,
                                                               int size,
                                                               String startTime,
                                                               String endTime);

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processId process instance id
     * @return process instance detail
     */
    Map<String, Object> queryProcessInstanceById(User loginUser,
                                                 long projectCode,
                                                 Integer processId);

    ProcessInstance queryByWorkflowInstanceIdThrowExceptionIfNotFound(Integer processId);

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param processId process instance id
     * @return process instance detail
     */
    Map<String, Object> queryProcessInstanceById(User loginUser,
                                                 Integer processId);

    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefineCode process definition code
     * @param searchVal search value
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @param otherParamsJson otherParamsJson handle other params
     * @return process instance list
     */
    Result<PageInfo<ProcessInstance>> queryProcessInstanceList(User loginUser,
                                                               long projectCode,
                                                               long processDefineCode,
                                                               String startDate,
                                                               String endDate,
                                                               String searchVal,
                                                               String executorName,
                                                               WorkflowExecutionStatus stateType,
                                                               String host,
                                                               String otherParamsJson,
                                                               Integer pageNo,
                                                               Integer pageSize);

    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser login user
     * @param workflowInstanceQueryRequest workflowInstanceQueryRequest
     * @return process instance list
     */
    Result queryProcessInstanceList(User loginUser,
                                    WorkflowInstanceQueryRequest workflowInstanceQueryRequest);

    /**
     * query task list by process instance id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processId process instance id
     * @return task list for the process instance
     * @throws IOException io exception
     */
    Map<String, Object> queryTaskListByProcessId(User loginUser,
                                                 long projectCode,
                                                 Integer processId) throws IOException;

    Map<String, DependResult> parseLogForDependentResult(String log) throws IOException;

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskId task id
     * @return sub process instance detail
     */
    Map<String, Object> querySubProcessInstanceByTaskId(User loginUser,
                                                        long projectCode,
                                                        Integer taskId);

    List<DynamicSubWorkflowDto> queryDynamicSubWorkflowInstances(User loginUser,
                                                                 Integer taskId);

    /**
     * update process instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskRelationJson process task relation json
     * @param taskDefinitionJson taskDefinitionJson
     * @param processInstanceId process instance id
     * @param scheduleTime schedule time
     * @param syncDefine sync define
     * @param globalParams global params
     * @param locations locations for nodes
     * @param timeout timeout
     * @return update result code
     */
    Map<String, Object> updateProcessInstance(User loginUser,
                                              long projectCode,
                                              Integer processInstanceId,
                                              String taskRelationJson,
                                              String taskDefinitionJson,
                                              String scheduleTime,
                                              Boolean syncDefine,
                                              String globalParams,
                                              String locations,
                                              int timeout);

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param subId sub process id
     * @return parent instance detail
     */
    Map<String, Object> queryParentInstanceBySubId(User loginUser,
                                                   long projectCode,
                                                   Integer subId);

    /**
     * delete process instance by id, at the same time，delete task instance and their mapping relation data
     *
     * @param loginUser         login user
     * @param processInstanceId process instance id
     * @return delete result code
     */
    void deleteProcessInstanceById(User loginUser,
                                   Integer processInstanceId);

    /**
     * view process instance variables
     *
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @return variables data
     */
    Map<String, Object> viewVariables(long projectCode, Integer processInstanceId);

    /**
     * encapsulation gantt structure
     *
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @return gantt tree data
     * @throws Exception exception when json parse
     */
    Map<String, Object> viewGantt(long projectCode, Integer processInstanceId) throws Exception;

    /**
     * query process instance by processDefinitionCode and stateArray
     *
     * @param processDefinitionCode processDefinitionCode
     * @param states states array
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineCodeAndStatus(Long processDefinitionCode,
                                                            int[] states);

    /**
     * query process instance by processDefinitionCode
     *
     * @param processDefinitionCode processDefinitionCode
     * @param size size
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineCode(Long processDefinitionCode,
                                                   int size);

    /**
     * query process instance list bt trigger code
     *
     * @param loginUser
     * @param projectCode
     * @param triggerCode
     * @return
     */
    Map<String, Object> queryByTriggerCode(User loginUser, long projectCode, Long triggerCode);

    void deleteProcessInstanceByWorkflowDefinitionCode(long workflowDefinitionCode);

    void deleteProcessInstanceById(int workflowInstanceId);

}
