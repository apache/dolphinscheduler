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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * process instance mapper interface
 */
public interface ProcessInstanceMapper extends BaseMapper<ProcessInstance> {

    /**
     * query process instance detail info by id
     *
     * @param processId processId
     * @return process instance
     */
    ProcessInstance queryDetailById(@Param("processId") int processId);

    /**
     * query process instance by host and stateArray
     *
     * @param host       host
     * @param stateArray stateArray
     * @return process instance list
     */
    List<ProcessInstance> queryByHostAndStatus(@Param("host") String host,
                                               @Param("states") int[] stateArray);

    /**
     * query process instance host by stateArray
     *
     * @param stateArray
     * @return
     */
    List<String> queryNeedFailoverProcessInstanceHost(@Param("states") int[] stateArray);

    /**
     * query process instance by tenantCode and stateArray
     *
     * @param tenantCode tenantCode
     * @param states   states array
     * @return process instance list
     */
    List<ProcessInstance> queryByTenantCodeAndStatus(@Param("tenantCode") String tenantCode,
                                                     @Param("states") int[] states);

    /**
     * @param workerGroupName workerGroupName
     * @param states          states array
     * @return process instance list
     */
    List<ProcessInstance> queryByWorkerGroupNameAndStatus(@Param("workerGroupName") String workerGroupName,
                                                          @Param("states") int[] states);

    /**
     * process instance page
     * @param page page
     * @param projectId projectId
     * @param processDefinitionId processDefinitionId
     * @param searchVal searchVal
     * @param executorId executorId
     * @param statusArray statusArray
     * @param host host
     * @param startTime startTime
     * @param endTime endTime
     * @return process instance IPage
     */

    /**
     * process instance page
     *
     * @param page                  page
     * @param projectCode           projectCode
     * @param processDefinitionCode processDefinitionCode
     * @param searchVal             searchVal
     * @param executorName            executorName
     * @param statusArray           statusArray
     * @param host                  host
     * @param startTime             startTime
     * @param endTime               endTime
     * @return process instance page
     */
    IPage<ProcessInstance> queryProcessInstanceListPaging(Page<ProcessInstance> page,
                                                          @Param("projectCode") Long projectCode,
                                                          @Param("processDefinitionCode") Long processDefinitionCode,
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
     * update process instance by state
     *
     * @param originState originState
     * @param destState   destState
     * @return update result
     */
    int updateProcessInstanceByState(@Param("originState") WorkflowExecutionStatus originState,
                                     @Param("destState") WorkflowExecutionStatus destState);

    /**
     * update process instance by tenantCode
     *
     * @param originTenantCode originTenantCode
     * @param destTenantCode   destTenantCode
     * @return update result
     */
    int updateProcessInstanceByTenantCode(@Param("originTenantCode") String originTenantCode,
                                          @Param("destTenantCode") String destTenantCode);

    /**
     * update process instance by worker groupId
     *
     * @param originWorkerGroupName originWorkerGroupName
     * @param destWorkerGroupName   destWorkerGroupName
     * @return update result
     */
    int updateProcessInstanceByWorkerGroupName(@Param("originWorkerGroupName") String originWorkerGroupName,
                                               @Param("destWorkerGroupName") String destWorkerGroupName);

    /**
     * Statistics process instance state by given project codes list
     * <p>
     * We only need project codes to determine whether the process instance belongs to the user or not.
     *
     * @param startTime    startTime
     * @param endTime      endTime
     * @param projectCodes projectCodes
     * @return ExecuteStatusCount list
     */
    List<ExecuteStatusCount> countInstanceStateByProjectCodes(
                                                              @Param("startTime") Date startTime,
                                                              @Param("endTime") Date endTime,
                                                              @Param("projectCodes") Long[] projectCodes);

    /**
     * query process instance by processDefinitionCode
     *
     * @param processDefinitionCode processDefinitionCode
     * @param size                  size
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineCode(@Param("processDefinitionCode") Long processDefinitionCode,
                                                   @Param("size") int size);

    /**
     * query last scheduler process instance
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @param testFlag       testFlag
     * @return process instance
     */
    ProcessInstance queryLastSchedulerProcess(@Param("processDefinitionCode") Long definitionCode,
                                              @Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime,
                                              @Param("testFlag") int testFlag);

    /**
     * query last running process instance
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @param testFlag       testFlag
     * @param stateArray     stateArray
     * @return process instance
     */
    ProcessInstance queryLastRunningProcess(@Param("processDefinitionCode") Long definitionCode,
                                            @Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime,
                                            @Param("testFlag") int testFlag,
                                            @Param("states") int[] stateArray);

    /**
     * query last manual process instance
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @param testFlag       testFlag
     * @return process instance
     */
    ProcessInstance queryLastManualProcess(@Param("processDefinitionCode") Long definitionCode,
                                           @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime,
                                           @Param("testFlag") int testFlag);

    /**
     * query first schedule process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    ProcessInstance queryFirstScheduleProcessInstance(@Param("processDefinitionCode") Long definitionCode);

    /**
     * query first manual process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    ProcessInstance queryFirstStartProcessInstance(@Param("processDefinitionCode") Long definitionCode);

    /**
     * query top n process instance order by running duration
     *
     * @param size        size
     * @param startTime   start time
     * @param startTime   end time
     * @param status      process instance status
     * @param projectCode project code
     * @return ProcessInstance list
     */

    List<ProcessInstance> queryTopNProcessInstance(@Param("size") int size,
                                                   @Param("startTime") Date startTime,
                                                   @Param("endTime") Date endTime,
                                                   @Param("status") WorkflowExecutionStatus status,
                                                   @Param("projectCode") long projectCode);

    /**
     * query process instance by processDefinitionCode and stateArray
     *
     * @param processDefinitionCode processDefinitionCode
     * @param states                states array
     * @return process instance list
     */

    List<ProcessInstance> queryByProcessDefineCodeAndStatus(@Param("processDefinitionCode") Long processDefinitionCode,
                                                            @Param("states") int[] states);

    List<ProcessInstance> queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(@Param("processDefinitionCode") Long processDefinitionCode,
                                                                                                @Param("processDefinitionVersion") int processDefinitionVersion,
                                                                                                @Param("states") int[] states,
                                                                                                @Param("id") Integer id);

    int updateGlobalParamsById(@Param("globalParams") String globalParams,
                               @Param("id") int id);

    boolean updateNextProcessIdById(@Param("thisInstanceId") int thisInstanceId,
                                    @Param("runningInstanceId") int runningInstanceId);

    ProcessInstance loadNextProcess4Serial(@Param("processDefinitionCode") Long processDefinitionCode,
                                           @Param("state") int state, @Param("id") int id);

    /**
     * Filter process instance
     *
     * @param page                  page
     * @param processDefinitionCode processDefinitionCode
     * @param name                  name
     * @param host                  host
     * @param startTime             startTime
     * @param endTime               endTime
     * @return process instance IPage
     */
    IPage<ProcessInstance> queryProcessInstanceListV2Paging(Page<ProcessInstance> page,
                                                            @Param("projectCode") Long projectCode,
                                                            @Param("processDefinitionCode") Long processDefinitionCode,
                                                            @Param("name") String name,
                                                            @Param("startTime") String startTime,
                                                            @Param("endTime") String endTime,
                                                            @Param("state") Integer state,
                                                            @Param("host") String host);

    /**
     * Statistics process instance state v2
     * <p>
     * We only need project codes to determine whether the process instance belongs to the user or not.
     *
     * @param startTime    startTime
     * @param endTime      endTime
     * @param projectCode  projectCode
     * @param workflowCode workflowCode
     * @param model model
     * @param projectIds projectIds
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
    List<ProcessInstance> queryByTriggerCode(@Param("triggerCode") Long triggerCode);
}
