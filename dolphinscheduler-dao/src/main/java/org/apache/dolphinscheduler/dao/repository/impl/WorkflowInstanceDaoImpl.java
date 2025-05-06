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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceRelationMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    public void updateWorkflowInstanceState(Integer workflowInstanceId,
                                            WorkflowExecutionStatus originalStatus,
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

    /**
     * find last scheduler process instance in the date interval
     *
     * @param workflowDefinitionCode definitionCode
     * @param taskDefinitionCode     definitionCode
     * @param dateInterval           dateInterval
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
        return queryOneByCondition(queryWrapper -> queryWrapper
                .eq(WorkflowInstance::getWorkflowDefinitionCode, definitionCode)
                .isNotNull(WorkflowInstance::getScheduleTime)
                .orderByDesc(WorkflowInstance::getScheduleTime)).orElse(null);
    }

    /**
     * query first manual process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    @Override
    public WorkflowInstance queryFirstStartWorkflowInstance(Long definitionCode) {
        return queryOneByCondition(queryWrapper -> queryWrapper
                .eq(WorkflowInstance::getWorkflowDefinitionCode, definitionCode)
                .isNotNull(WorkflowInstance::getStartTime)
                .orderByDesc(WorkflowInstance::getStartTime)).orElse(null);
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
    public List<String> listHostsNeedingFailover() {
        return queryByCondition(queryWrapper -> queryWrapper
                .in(WorkflowInstance::getState,
                        Arrays.stream(WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState()).boxed()
                                .collect(Collectors.toList())))
                                        .stream()
                                        .map(WorkflowInstance::getHost)
                                        .distinct()
                                        .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstance> queryNeedFailoverWorkflowInstances(String masterAddress) {
        return queryByCondition(queryWrapper -> queryWrapper
                .eq(WorkflowInstance::getHost, masterAddress)
                .eq(WorkflowInstance::getIsSubWorkflow, Flag.NO.getCode())
                .in(WorkflowInstance::getState,
                        Arrays.stream(WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState()).boxed()
                                .collect(Collectors.toList())));
    }
}
