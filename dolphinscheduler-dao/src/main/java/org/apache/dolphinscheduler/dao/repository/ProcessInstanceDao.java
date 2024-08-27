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
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import java.util.List;

public interface ProcessInstanceDao extends IDao<WorkflowInstance> {

    /**
     * insert or update work process instance to database
     *
     * @param workflowInstance processInstance
     */
    void upsertProcessInstance(WorkflowInstance workflowInstance);

    /**
     * Update workflow instance from originState to targetState
     */
    void updateWorkflowInstanceState(Integer workflowInstanceId,
                                     WorkflowExecutionStatus originState,
                                     WorkflowExecutionStatus targetState);

    /**
     * performs an "upsert" operation (update or insert) on a ProcessInstance object within a new transaction
     *
     * @param workflowInstance processInstance
     */
    void performTransactionalUpsert(WorkflowInstance workflowInstance);

    /**
     * find last scheduler process instance in the date interval
     *
     * @param processDefinitionCode definitionCode
     * @param taskDefinitionCode    definitionCode
     * @param dateInterval          dateInterval
     * @return process instance
     */
    WorkflowInstance queryLastSchedulerProcessInterval(Long processDefinitionCode, Long taskDefinitionCode,
                                                       DateInterval dateInterval, int testFlag);

    /**
     * find last manual process instance interval
     *
     * @param definitionCode process definition code
     * @param taskCode       taskCode
     * @param dateInterval   dateInterval
     * @return process instance
     */
    WorkflowInstance queryLastManualProcessInterval(Long definitionCode, Long taskCode, DateInterval dateInterval,
                                                    int testFlag);

    /**
     * query first schedule process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    WorkflowInstance queryFirstScheduleProcessInstance(Long definitionCode);

    /**
     * query first manual process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    WorkflowInstance queryFirstStartProcessInstance(Long definitionCode);

    WorkflowInstance querySubProcessInstanceByParentId(Integer processInstanceId, Integer taskInstanceId);

    List<WorkflowInstance> queryByWorkflowCodeVersionStatus(Long workflowDefinitionCode,
                                                            int workflowDefinitionVersion,
                                                            int[] states);

    List<String> queryNeedFailoverMasters();

    /**
     * Query the workflow instances under the master that need to be failover.
     */
    List<WorkflowInstance> queryNeedFailoverWorkflowInstances(String masterAddress);
}
