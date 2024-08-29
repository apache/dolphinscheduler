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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceRelationMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import java.util.List;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class WorkflowInstanceDaoImpl extends BaseDao<WorkflowInstance, WorkflowInstanceMapper>
        implements
            WorkflowInstanceDao {

    @Autowired
    private WorkflowInstanceRelationMapper workflowInstanceRelationMapper;

    public WorkflowInstanceDaoImpl(@NonNull WorkflowInstanceMapper workflowInstanceMapper) {
        super(workflowInstanceMapper);
    }

    @Override
    public void upsertWorkflowInstance(@NonNull WorkflowInstance workflowInstance) {
        if (workflowInstance.getId() != null) {
            updateById(workflowInstance);
        } else {
            insert(workflowInstance);
        }
    }

    @Override
    public void updateWorkflowInstanceState(Integer workflowInstanceId, WorkflowExecutionStatus originalStatus,
                                            WorkflowExecutionStatus targetStatus) {
        int update = mybatisMapper.updateWorkflowInstanceState(workflowInstanceId, originalStatus, targetStatus);
        if (update != 1) {
            WorkflowInstance workflowInstance = mybatisMapper.selectById(workflowInstanceId);
            if (workflowInstance == null) {
                throw new UnsupportedOperationException("updateWorkflowInstance " + workflowInstanceId
                        + " state failed, the workflow instance is not exist in db");
            }
            throw new UnsupportedOperationException(
                    "updateWorkflowInstance " + workflowInstanceId + " state failed, expect original state is "
                            + originalStatus.name() + " actual state is : {} " + workflowInstance.getState().name());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void performTransactionalUpsert(WorkflowInstance workflowInstance) {
        this.upsertWorkflowInstance(workflowInstance);
    }

    /**
     * find last scheduler process instance in the date interval
     *
     * @param workflowDefinitionCode definitionCode
     * @param taskDefinitionCode    definitionCode
     * @param dateInterval          dateInterval
     * @return process instance
     */
    @Override
    public WorkflowInstance queryLastSchedulerWorkflowInterval(Long workflowDefinitionCode, Long taskDefinitionCode,
                                                               DateInterval dateInterval,
                                                               int testFlag) {
        return mybatisMapper.queryLastSchedulerWorkflow(
                workflowDefinitionCode,
                taskDefinitionCode,
                dateInterval.getStartTime(),
                dateInterval.getEndTime(),
                testFlag);
    }

    /**
     * find last manual process instance interval
     *
     * @param definitionCode process definition code
     * @param taskCode       taskCode
     * @param dateInterval   dateInterval
     * @return process instance
     */
    @Override
    public WorkflowInstance queryLastManualWorkflowInterval(Long definitionCode, Long taskCode,
                                                            DateInterval dateInterval,
                                                            int testFlag) {
        return mybatisMapper.queryLastManualWorkflow(definitionCode,
                taskCode,
                dateInterval.getStartTime(),
                dateInterval.getEndTime(),
                testFlag);
    }

    /**
     * query first schedule process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    @Override
    public WorkflowInstance queryFirstScheduleWorkflowInstance(Long definitionCode) {
        return mybatisMapper.queryFirstScheduleWorkflowInstance(definitionCode);
    }

    /**
     * query first manual process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    @Override
    public WorkflowInstance queryFirstStartWorkflowInstance(Long definitionCode) {
        return mybatisMapper.queryFirstStartWorkflowInstance(definitionCode);
    }

    @Override
    public WorkflowInstance querySubWorkflowInstanceByParentId(Integer workflowInstanceId, Integer taskInstanceId) {
        WorkflowInstance workflowInstance = null;
        WorkflowInstanceRelation workflowInstanceRelation =
                workflowInstanceRelationMapper.queryByParentId(workflowInstanceId, taskInstanceId);
        if (workflowInstanceRelation == null || workflowInstanceRelation.getWorkflowInstanceId() == 0) {
            return workflowInstance;
        }
        workflowInstance = queryById(workflowInstanceRelation.getWorkflowInstanceId());
        return workflowInstance;
    }

    @Override
    public List<WorkflowInstance> queryByWorkflowCodeVersionStatus(Long workflowDefinitionCode,
                                                                   int workflowDefinitionVersion,
                                                                   int[] states) {
        return mybatisMapper.queryByWorkflowCodeVersionStatus(workflowDefinitionCode, workflowDefinitionVersion,
                states);
    }

    @Override
    public List<String> queryNeedFailoverMasters() {
        return mybatisMapper
                .queryNeedFailoverWorkflowInstanceHost(WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState());
    }

    @Override
    public List<WorkflowInstance> queryNeedFailoverWorkflowInstances(String masterAddress) {
        return mybatisMapper.queryByHostAndStatus(masterAddress,
                WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState());
    }
}
