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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.WorkflowInstanceStatusCountDto;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface WorkflowInstanceDao extends IDao<WorkflowInstance> {

    /**
     * insert or update work workflow instance to database
     *
     * @param workflowInstance workflowInstance
     */
    void upsertWorkflowInstance(WorkflowInstance workflowInstance);

    /**
     * Update workflow instance from originState to targetState
     */
    void updateWorkflowInstanceState(Integer workflowInstanceId,
                                     WorkflowExecutionStatus originState,
                                     WorkflowExecutionStatus targetState);

    void forceUpdateWorkflowInstanceState(Integer id, WorkflowExecutionStatus status);

    /**
     * find last scheduler workflow instance in the date interval
     *
     * @param workflowDefinitionCode definitionCode
     * @param taskDefinitionCode    definitionCode
     * @param dateInterval          dateInterval
     * @return workflow instance
     */
    WorkflowInstance queryLastSchedulerWorkflowInterval(Long workflowDefinitionCode, Long taskDefinitionCode,
                                                        DateInterval dateInterval);

    /**
     * find last manual workflow instance interval
     *
     * @param definitionCode workflow definition code
     * @param taskCode       taskCode
     * @param dateInterval   dateInterval
     * @return workflow instance
     */
    WorkflowInstance queryLastManualWorkflowInterval(Long definitionCode, Long taskCode, DateInterval dateInterval);

    WorkflowInstance queryLastRunningWorkflowInterval(Long definitionCode, DateInterval dateInterval);

    /**
     * query first schedule workflow instance
     *
     * @param definitionCode definitionCode
     * @return workflow instance
     */
    WorkflowInstance queryFirstScheduleWorkflowInstance(Long definitionCode);

    /**
     * query first manual workflow instance
     *
     * @param definitionCode definitionCode
     * @return workflow instance
     */
    WorkflowInstance queryFirstStartWorkflowInstance(Long definitionCode);

    WorkflowInstance querySubWorkflowInstanceByParentId(Integer workflowInstanceId, Integer taskInstanceId);

    List<WorkflowInstance> queryByWorkflowCodeVersionStatus(Long workflowDefinitionCode,
                                                            int workflowDefinitionVersion,
                                                            int[] states);

    List<String> queryNeedFailoverMasters();

    /**
     * Query the workflow instances under the master that need to be failover.
     */
    List<WorkflowInstance> queryNeedFailoverWorkflowInstances(String masterAddress);

    WorkflowInstance queryDetailById(int id);

    List<WorkflowInstanceStatusCountDto> countWorkflowInstanceStateByProjectCodes(Date startTime,
                                                                                  Date endTime,
                                                                                  Collection<Long> projectCodes);

    int updateWorkflowInstanceByTenantCode(String originTenantCode, String destTenantCode);

    int updateWorkflowInstanceByWorkerGroupName(String originWorkerGroupName, String destWorkerGroupName);

    List<WorkflowInstance> queryByTenantCodeAndStatus(String tenantCode, int[] states);

    List<WorkflowInstance> queryByWorkerGroupNameAndStatus(String workerGroupName, int[] states);

    List<WorkflowInstance> queryTopNWorkflowInstance(int size,
                                                     Date startTime,
                                                     Date endTime,
                                                     WorkflowExecutionStatus status,
                                                     long projectCode);

    IPage<WorkflowInstance> queryWorkflowInstanceListPaging(Page<WorkflowInstance> page,
                                                            Long projectCode,
                                                            Long workflowDefinitionCode,
                                                            String searchVal,
                                                            String executorName,
                                                            int[] statusArray,
                                                            String host,
                                                            Date startTime,
                                                            Date endTime);

    List<WorkflowInstance> queryByWorkflowDefinitionCodeAndStatus(Long workflowDefinitionCode, int[] states);

    List<WorkflowInstance> queryByWorkflowDefinitionCode(Long workflowDefinitionCode, int size);

    List<WorkflowInstance> queryByTriggerCode(Long triggerCode);
}
