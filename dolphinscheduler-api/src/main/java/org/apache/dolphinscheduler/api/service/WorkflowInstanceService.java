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
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface WorkflowInstanceService {

    /**
     * return top n SUCCESS workflow instance order by running time which started between startTime and endTime
     */
    Map<String, Object> queryTopNLongestRunningWorkflowInstance(User loginUser,
                                                                long projectCode,
                                                                int size,
                                                                String startTime,
                                                                String endTime);

    /**
     * query workflow instance by id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param workflowInstanceId   workflow instance id
     * @return workflow instance detail
     */
    Map<String, Object> queryWorkflowInstanceById(User loginUser,
                                                  long projectCode,
                                                  Integer workflowInstanceId);

    WorkflowInstance queryByWorkflowInstanceIdThrowExceptionIfNotFound(Integer workflowInstanceId);

    /**
     * query workflow instance by id
     *
     * @param loginUser login user
     * @param workflowInstanceId workflow instance id
     * @return workflow instance detail
     */
    Map<String, Object> queryWorkflowInstanceById(User loginUser,
                                                  Integer workflowInstanceId);

    /**
     * paging query workflow instance list, filtering according to project, workflow definition, time range, keyword, workflow status
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param pageNo            page number
     * @param pageSize          page size
     * @param workflowDefinitionCode workflow definition code
     * @param searchVal         search value
     * @param stateType         state type
     * @param host              host
     * @param startDate         start time
     * @param endDate           end time
     * @param otherParamsJson   otherParamsJson handle other params
     * @return workflow instance list
     */
    Result<PageInfo<WorkflowInstance>> queryWorkflowInstanceList(User loginUser,
                                                                 long projectCode,
                                                                 long workflowDefinitionCode,
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
     * paging query workflow instance list, filtering according to project, workflow definition, time range, keyword, workflow status
     *
     * @param loginUser                    login user
     * @param workflowInstanceQueryRequest workflowInstanceQueryRequest
     * @return workflow instance list
     */
    Result queryWorkflowInstanceList(User loginUser,
                                     WorkflowInstanceQueryRequest workflowInstanceQueryRequest);

    /**
     * query task list by workflow instance id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param workflowInstanceId   workflow instance id
     * @return task list for the workflow instance
     * @throws IOException io exception
     */
    Map<String, Object> queryTaskListByWorkflowInstanceId(User loginUser,
                                                          long projectCode,
                                                          Integer workflowInstanceId) throws IOException;

    /**
     * query sub workflow instance detail info by task id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskId      task id
     * @return sub workflow instance detail
     */
    Map<String, Object> querySubWorkflowInstanceByTaskId(User loginUser,
                                                         long projectCode,
                                                         Integer taskId);

    List<DynamicSubWorkflowDto> queryDynamicSubWorkflowInstances(User loginUser,
                                                                 Integer taskId);

    /**
     * update workflow instance
     *
     * @param loginUser          login user
     * @param projectCode        project code
     * @param taskRelationJson   workflow task relation json
     * @param taskDefinitionJson taskDefinitionJson
     * @param workflowInstanceId  workflow instance id
     * @param scheduleTime       schedule time
     * @param syncDefine         sync define
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @return update result code
     */
    Map<String, Object> updateWorkflowInstance(User loginUser,
                                               long projectCode,
                                               Integer workflowInstanceId,
                                               String taskRelationJson,
                                               String taskDefinitionJson,
                                               String scheduleTime,
                                               Boolean syncDefine,
                                               String globalParams,
                                               String locations,
                                               int timeout);

    /**
     * query parent workflow instance detail info by sub workflow instance id
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param subId       sub workflow id
     * @return parent instance detail
     */
    Map<String, Object> queryParentInstanceBySubId(User loginUser,
                                                   long projectCode,
                                                   Integer subId);

    /**
     * delete workflow instance by id, at the same time，delete task instance and their mapping relation data
     *
     * @param loginUser         login user
     * @param workflowInstanceId workflow instance id
     * @return delete result code
     */
    void deleteWorkflowInstanceById(User loginUser,
                                    Integer workflowInstanceId);

    /**
     * view workflow instance variables
     *
     * @param projectCode       project code
     * @param workflowInstanceId workflow instance id
     * @return variables data
     */
    Map<String, Object> viewVariables(long projectCode, Integer workflowInstanceId);

    /**
     * encapsulation gantt structure
     *
     * @param projectCode       project code
     * @param workflowInstanceId workflow instance id
     * @return gantt tree data
     * @throws Exception exception when json parse
     */
    Map<String, Object> viewGantt(long projectCode, Integer workflowInstanceId) throws Exception;

    /**
     * query workflow instance by workflowDefinitionCode and stateArray
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param states                states array
     * @return workflow instance list
     */
    List<WorkflowInstance> queryByWorkflowDefinitionCodeAndStatus(Long workflowDefinitionCode,
                                                                  int[] states);

    /**
     * query workflow instance by workflowDefinitionCode and stateArray
     *
     * @param workflowDefinitionCode    workflowDefinitionCode
     * @param workflowDefinitionVersion workflowDefinitionVersion
     * @param states                    states array
     * @return workflow instance list
     */
    List<WorkflowInstance> queryByWorkflowCodeVersionStatus(Long workflowDefinitionCode,
                                                            int workflowDefinitionVersion,
                                                            int[] states);

    /**
     * query workflow instance by workflowDefinitionCode
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param size                  size
     * @return workflow instance list
     */
    List<WorkflowInstance> queryByWorkflowDefinitionCode(Long workflowDefinitionCode,
                                                         int size);

    /**
     * query workflow instance list bt trigger code
     *
     * @param loginUser
     * @param projectCode
     * @param triggerCode
     * @return
     */
    Map<String, Object> queryByTriggerCode(User loginUser, long projectCode, Long triggerCode);

    void deleteWorkflowInstanceByWorkflowDefinitionCode(long workflowDefinitionCode);

    void deleteWorkflowInstanceById(int workflowInstanceId);

}
