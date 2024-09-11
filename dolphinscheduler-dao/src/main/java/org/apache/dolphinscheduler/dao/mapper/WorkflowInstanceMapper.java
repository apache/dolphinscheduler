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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.WorkflowInstanceStatusCountDto;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * workflow instance mapper interface
 */
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {

    /**
     * query workflow instance detail info by id
     *
     * @param id id
     * @return workflow instance
     */
    WorkflowInstance queryDetailById(@Param("id") int id);

    /**
     * query workflow instance by host and stateArray
     *
     * @param host       host
     * @param stateArray stateArray
     * @return workflow instance list
     */
    List<WorkflowInstance> queryByHostAndStatus(@Param("host") String host,
                                                @Param("states") int[] stateArray);

    /**
     * query workflow instance host by stateArray
     *
     * @param stateArray
     * @return
     */
    List<String> queryNeedFailoverWorkflowInstanceHost(@Param("states") int[] stateArray);

    /**
     * query workflow instance by tenantCode and stateArray
     *
     * @param tenantCode tenantCode
     * @param states     states array
     * @return workflow instance list
     */
    List<WorkflowInstance> queryByTenantCodeAndStatus(@Param("tenantCode") String tenantCode,
                                                      @Param("states") int[] states);

    /**
     * @param workerGroupName workerGroupName
     * @param states          states array
     * @return workflow instance list
     */
    List<WorkflowInstance> queryByWorkerGroupNameAndStatus(@Param("workerGroupName") String workerGroupName,
                                                           @Param("states") int[] states);

    /**
     * workflow instance page
     * @param page page
     * @param projectId projectId
     * @param processDefinitionId processDefinitionId
     * @param searchVal searchVal
     * @param executorId executorId
     * @param statusArray statusArray
     * @param host host
     * @param startTime startTime
     * @param endTime endTime
     * @return workflow instance IPage
     */

    /**
     * workflow instance page
     *
     * @param page                  page
     * @param projectCode           projectCode
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param searchVal             searchVal
     * @param executorName          executorName
     * @param statusArray           statusArray
     * @param host                  host
     * @param startTime             startTime
     * @param endTime               endTime
     * @return workflow instance page
     */
    IPage<WorkflowInstance> queryWorkflowInstanceListPaging(Page<WorkflowInstance> page,
                                                            @Param("projectCode") Long projectCode,
                                                            @Param("workflowDefinitionCode") Long workflowDefinitionCode,
                                                            @Param("searchVal") String searchVal,
                                                            @Param("executorName") String executorName,
                                                            @Param("states") int[] statusArray,
                                                            @Param("host") String host,
                                                            @Param("startTime") Date startTime,
                                                            @Param("endTime") Date endTime);

    /**
     * set failover by host and state array
     *
     * @param host       host
     * @param stateArray stateArray
     * @return set result
     */
    int setFailoverByHostAndStateArray(@Param("host") String host,
                                       @Param("states") int[] stateArray);

    /**
     * Update the workflow instance state from originState to destState
     */
    int updateWorkflowInstanceState(
                                    @Param("workflowInstanceId") Integer workflowInstanceId,
                                    @Param("originState") WorkflowExecutionStatus originState,
                                    @Param("targetState") WorkflowExecutionStatus targetState);

    /**
     * update workflow instance by tenantCode
     *
     * @param originTenantCode originTenantCode
     * @param destTenantCode   destTenantCode
     * @return update result
     */
    int updateWorkflowInstanceByTenantCode(@Param("originTenantCode") String originTenantCode,
                                           @Param("destTenantCode") String destTenantCode);

    /**
     * update workflow instance by worker groupId
     *
     * @param originWorkerGroupName originWorkerGroupName
     * @param destWorkerGroupName   destWorkerGroupName
     * @return update result
     */
    int updateWorkflowInstanceByWorkerGroupName(@Param("originWorkerGroupName") String originWorkerGroupName,
                                                @Param("destWorkerGroupName") String destWorkerGroupName);

    /**
     * Statistics workflow instance state by given project codes list
     * <p>
     * We only need project codes to determine whether the workflow instance belongs to the user or not.
     *
     * @param startTime    startTime
     * @param endTime      endTime
     * @param projectCodes projectCodes
     * @return ExecuteStatusCount list
     */
    List<WorkflowInstanceStatusCountDto> countWorkflowInstanceStateByProjectCodes(
                                                                                  @Param("startTime") Date startTime,
                                                                                  @Param("endTime") Date endTime,
                                                                                  @Param("projectCodes") Collection<Long> projectCodes);

    /**
     * query workflow instance by workflowDefinitionCode
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param size                  size
     * @return workflow instance list
     */
    List<WorkflowInstance> queryByWorkflowDefinitionCode(@Param("workflowDefinitionCode") Long workflowDefinitionCode,
                                                         @Param("size") int size);

    /**
     * query last scheduler workflow instance
     *
     * @param workflowDefinitionCode definitionCode
     * @param taskDefinitionCode    definitionCode
     * @param startTime             startTime
     * @param endTime               endTime
     * @param testFlag              testFlag
     * @return workflow instance
     */
    WorkflowInstance queryLastSchedulerWorkflow(@Param("workflowDefinitionCode") Long workflowDefinitionCode,
                                                @Param("taskDefinitionCode") Long taskDefinitionCode,
                                                @Param("startTime") Date startTime,
                                                @Param("endTime") Date endTime,
                                                @Param("testFlag") int testFlag);

    /**
     * query last manual workflow instance
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param taskCode       taskCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @param testFlag       testFlag
     * @return workflow instance
     */
    WorkflowInstance queryLastManualWorkflow(@Param("workflowDefinitionCode") Long workflowDefinitionCode,
                                             @Param("taskCode") Long taskCode,
                                             @Param("startTime") Date startTime,
                                             @Param("endTime") Date endTime,
                                             @Param("testFlag") int testFlag);

    /**
     * query first schedule workflow instance
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return workflow instance
     */
    WorkflowInstance queryFirstScheduleWorkflowInstance(@Param("workflowDefinitionCode") Long workflowDefinitionCode);

    /**
     * query first manual workflow instance
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return workflow instance
     */
    WorkflowInstance queryFirstStartWorkflowInstance(@Param("workflowDefinitionCode") Long workflowDefinitionCode);

    /**
     * query top n workflow instance order by running duration
     *
     * @param size        size
     * @param startTime   start time
     * @param startTime   end time
     * @param status      workflow instance status
     * @param projectCode project code
     * @return ProcessInstance list
     */

    List<WorkflowInstance> queryTopNWorkflowInstance(@Param("size") int size,
                                                     @Param("startTime") Date startTime,
                                                     @Param("endTime") Date endTime,
                                                     @Param("status") WorkflowExecutionStatus status,
                                                     @Param("projectCode") long projectCode);

    /**
     * query workflow instance by workflowDefinitionCode and stateArray
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param states                states array
     * @return workflow instance list
     */

    List<WorkflowInstance> queryByWorkflowDefinitionCodeAndStatus(@Param("workflowDefinitionCode") Long workflowDefinitionCode,
                                                                  @Param("states") int[] states);

    List<WorkflowInstance> queryByWorkflowCodeVersionStatus(@Param("workflowDefinitionCode") long workflowDefinitionCode,
                                                            @Param("workflowDefinitionVersion") int workflowDefinitionVersion,
                                                            @Param("states") int[] states);

    /**
     * Filter workflow instance
     *
     * @param page                  page
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param name                  name
     * @param host                  host
     * @param startTime             startTime
     * @param endTime               endTime
     * @return workflow instance IPage
     */
    IPage<WorkflowInstance> queryWorkflowInstanceListV2Paging(Page<WorkflowInstance> page,
                                                              @Param("projectCode") Long projectCode,
                                                              @Param("workflowDefinitionCode") Long workflowDefinitionCode,
                                                              @Param("name") String name,
                                                              @Param("startTime") String startTime,
                                                              @Param("endTime") String endTime,
                                                              @Param("state") Integer state,
                                                              @Param("host") String host);

    /**
     * Statistics workflow instance state v2
     * <p>
     * We only need project codes to determine whether the workflow instance belongs to the user or not.
     *
     * @param startTime    startTime
     * @param endTime      endTime
     * @param projectCode  projectCode
     * @param workflowCode workflowCode
     * @param model        model
     * @param projectIds   projectIds
     * @return ExecuteStatusCount list
     */
    List<ExecuteStatusCount> countInstanceStateV2(
                                                  @Param("startTime") Date startTime,
                                                  @Param("endTime") Date endTime,
                                                  @Param("projectCode") Long projectCode,
                                                  @Param("workflowCode") Long workflowCode,
                                                  @Param("model") Integer model,
                                                  @Param("projectIds") Set<Integer> projectIds);

    /**
     * query process list by triggerCode
     *
     * @param triggerCode
     * @return
     */
    List<WorkflowInstance> queryByTriggerCode(@Param("triggerCode") Long triggerCode);
}
