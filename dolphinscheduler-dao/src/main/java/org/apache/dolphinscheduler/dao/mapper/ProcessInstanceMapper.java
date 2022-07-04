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

import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

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
     * @param stateArray
     * @return
     */
    List<String> queryNeedFailoverProcessInstanceHost(@Param("states") int[] stateArray);

    /**
     * query process instance by tenantId and stateArray
     *
     * @param tenantId tenantId
     * @param states   states array
     * @return process instance list
     */
    List<ProcessInstance> queryByTenantIdAndStatus(@Param("tenantId") int tenantId,
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
     * @param executorId            executorId
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
                                                          @Param("executorId") Integer executorId,
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
    int updateProcessInstanceByState(@Param("originState") ExecutionStatus originState,
                                     @Param("destState") ExecutionStatus destState);

    /**
     * update process instance by tenantId
     *
     * @param originTenantId originTenantId
     * @param destTenantId   destTenantId
     * @return update result
     */
    int updateProcessInstanceByTenantId(@Param("originTenantId") int originTenantId,
                                        @Param("destTenantId") int destTenantId);

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
     * @return process instance
     */
    ProcessInstance queryLastSchedulerProcess(@Param("processDefinitionCode") Long definitionCode,
                                              @Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime);

    /**
     * query last running process instance
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @param stateArray     stateArray
     * @return process instance
     */
    ProcessInstance queryLastRunningProcess(@Param("processDefinitionCode") Long definitionCode,
                                            @Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime,
                                            @Param("states") int[] stateArray);

    /**
     * query last manual process instance
     *
     * @param definitionCode definitionCode
     * @param startTime      startTime
     * @param endTime        endTime
     * @return process instance
     */
    ProcessInstance queryLastManualProcess(@Param("processDefinitionCode") Long definitionCode,
                                           @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime);

    /**
     * query top n process instance order by running duration
     *
     * @param size size
     * @param startTime start time
     * @param startTime end time
     * @param status process instance status
     * @param projectCode project code
     * @return ProcessInstance list
     */

    List<ProcessInstance> queryTopNProcessInstance(@Param("size") int size,
                                                   @Param("startTime") Date startTime,
                                                   @Param("endTime") Date endTime,
                                                   @Param("status") ExecutionStatus status,
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
                                                                     @Param("states") int[] states, @Param("id") int id);

    int updateGlobalParamsById(@Param("globalParams") String globalParams,
                               @Param("id") int id);

    boolean updateNextProcessIdById(@Param("thisInstanceId") int thisInstanceId, @Param("runningInstanceId") int runningInstanceId);

    ProcessInstance loadNextProcess4Serial(@Param("processDefinitionCode") Long processDefinitionCode, @Param("state") int state, @Param("id") int id);
}
