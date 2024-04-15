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

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import java.util.List;

public interface ProcessInstanceDao extends IDao<ProcessInstance> {

    /**
     * insert or update work process instance to database
     *
     * @param processInstance processInstance
     */
    void upsertProcessInstance(ProcessInstance processInstance);

    /**
     * performs an "upsert" operation (update or insert) on a ProcessInstance object within a new transaction
     *
     * @param processInstance processInstance
     */
    void performTransactionalUpsert(ProcessInstance processInstance);

    /**
     * find last scheduler process instance in the date interval
     *
     * @param processDefinitionCode definitionCode
     * @param taskDefinitionCode definitionCode
     * @param dateInterval   dateInterval
     * @return process instance
     */
    ProcessInstance queryLastSchedulerProcessInterval(Long processDefinitionCode, Long taskDefinitionCode,
                                                      DateInterval dateInterval, int testFlag);

    /**
     * find last manual process instance interval
     *
     * @param definitionCode process definition code
     * @param taskCode taskCode
     * @param dateInterval   dateInterval
     * @return process instance
     */
    ProcessInstance queryLastManualProcessInterval(Long definitionCode, Long taskCode, DateInterval dateInterval,
                                                   int testFlag);

    /**
     * query first schedule process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    ProcessInstance queryFirstScheduleProcessInstance(Long definitionCode);

    /**
     * query first manual process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    ProcessInstance queryFirstStartProcessInstance(Long definitionCode);

    ProcessInstance querySubProcessInstanceByParentId(Integer processInstanceId, Integer taskInstanceId);

    List<ProcessInstance> queryByWorkflowCodeVersionStatus(Long workflowDefinitionCode,
                                                           int workflowDefinitionVersion,
                                                           int[] states);
}
