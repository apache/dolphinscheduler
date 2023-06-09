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

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ProcessInstanceDaoImpl extends BaseDao<ProcessInstance, ProcessInstanceMapper>
        implements
            ProcessInstanceDao {

    @Autowired
    private ProcessInstanceMapMapper processInstanceMapMapper;

    public ProcessInstanceDaoImpl(@NonNull ProcessInstanceMapper processInstanceMapper) {
        super(processInstanceMapper);
    }

    @Override
    public void upsertProcessInstance(@NonNull ProcessInstance processInstance) {
        if (processInstance.getId() != null) {
            updateById(processInstance);
        } else {
            insert(processInstance);
        }
    }

    /**
     * find last scheduler process instance in the date interval
     *
     * @param definitionCode definitionCode
     * @param dateInterval   dateInterval
     * @return process instance
     */
    @Override
    public ProcessInstance queryLastSchedulerProcessInterval(Long definitionCode, DateInterval dateInterval,
                                                             int testFlag) {
        return mybatisMapper.queryLastSchedulerProcess(definitionCode,
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
    public ProcessInstance queryLastManualProcessInterval(Long definitionCode, DateInterval dateInterval,
                                                          int testFlag) {
        return mybatisMapper.queryLastManualProcess(definitionCode,
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
    public ProcessInstance queryFirstScheduleProcessInstance(Long definitionCode) {
        return mybatisMapper.queryFirstScheduleProcessInstance(definitionCode);
    }

    /**
     * query first manual process instance
     *
     * @param definitionCode definitionCode
     * @return process instance
     */
    @Override
    public ProcessInstance queryFirstStartProcessInstance(Long definitionCode) {
        return mybatisMapper.queryFirstStartProcessInstance(definitionCode);
    }

    @Override
    public ProcessInstance querySubProcessInstanceByParentId(Integer processInstanceId, Integer taskInstanceId) {
        ProcessInstance processInstance = null;
        ProcessInstanceMap processInstanceMap =
                processInstanceMapMapper.queryByParentId(processInstanceId, taskInstanceId);
        if (processInstanceMap == null || processInstanceMap.getProcessInstanceId() == 0) {
            return processInstance;
        }
        processInstance = queryById(processInstanceMap.getProcessInstanceId());
        return processInstance;
    }
}
