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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ProcessInstanceDaoImpl implements ProcessInstanceDao {

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Override
    public int insertProcessInstance(ProcessInstance processInstance) {
        return processInstanceMapper.insert(processInstance);
    }

    @Override
    public int updateProcessInstance(ProcessInstance processInstance) {
        return processInstanceMapper.updateById(processInstance);
    }

    @Override
    public int upsertProcessInstance(@NonNull ProcessInstance processInstance) {
        if (processInstance.getId() != null) {
            return updateProcessInstance(processInstance);
        } else {
            return insertProcessInstance(processInstance);
        }
    }

    @Override
    public void deleteByIds(List<Integer> needToDeleteWorkflowInstanceIds) {
        if (CollectionUtils.isEmpty(needToDeleteWorkflowInstanceIds)) {
            return;
        }
        processInstanceMapper.deleteBatchIds(needToDeleteWorkflowInstanceIds);
    }

    @Override
    public void deleteById(Integer workflowInstanceId) {
        processInstanceMapper.deleteById(workflowInstanceId);
    }

    @Override
    public ProcessInstance queryByWorkflowInstanceId(Integer workflowInstanceId) {
        return processInstanceMapper.selectById(workflowInstanceId);
    }

    /**
     * find last scheduler process instance in the date interval
     *
     * @param definitionCode definitionCode
     * @param dateInterval   dateInterval
     * @return process instance
     */
    @Override
    public ProcessInstance findLastSchedulerProcessInterval(Long definitionCode, DateInterval dateInterval,
                                                            int testFlag) {
        return processInstanceMapper.queryLastSchedulerProcess(definitionCode,
                dateInterval.getStartTime(),
                dateInterval.getEndTime(),
                testFlag);
    }

    /**
     * find last manual process instance interval
     *
     * @param definitionCode process definition code
     * @param dateInterval   dateInterval
     * @return process instance
     */
    @Override
    public ProcessInstance findLastManualProcessInterval(Long definitionCode, DateInterval dateInterval, int testFlag) {
        return processInstanceMapper.queryLastManualProcess(definitionCode,
                dateInterval.getStartTime(),
                dateInterval.getEndTime(),
                testFlag);
    }

    /**
     * find last running process instance
     *
     * @param definitionCode process definition code
     * @param startTime      start time
     * @param endTime        end time
     * @return process instance
     */
    @Override
    public ProcessInstance findLastRunningProcess(Long definitionCode, Date startTime, Date endTime, int testFlag) {
        return processInstanceMapper.queryLastRunningProcess(definitionCode,
                startTime,
                endTime,
                testFlag,
                WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState());
    }

    /**
     * query first schedule process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    @Override
    public ProcessInstance queryFirstScheduleProcessInstance(Long definitionCode) {
        return processInstanceMapper.queryFirstScheduleProcessInstance(definitionCode);
    }

    /**
     * query first manual process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    @Override
    public ProcessInstance queryFirstStartProcessInstance(Long definitionCode) {
        return processInstanceMapper.queryFirstStartProcessInstance(definitionCode);
    }
}
