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

import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TASK_INSTANCE_ID;

import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.mapper.DqExecuteResultMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.DqExecuteResultDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Component
public class DqExecuteResultDaoImpl implements DqExecuteResultDao {

    @Autowired
    DqExecuteResultMapper dqExecuteResultMapper;

    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public DqExecuteResult getDqExecuteResultByTaskInstanceId(int taskInstanceId) {
        return dqExecuteResultMapper.getExecuteResultById(taskInstanceId);
    }

    @Override
    public int updateById(DqExecuteResult dqExecuteResult) {
        return dqExecuteResultMapper.updateById(dqExecuteResult);
    }

    @Override
    public int deleteDqExecuteResultByTaskInstanceId(int taskInstanceId) {
        return dqExecuteResultMapper.delete(
                new QueryWrapper<DqExecuteResult>()
                        .eq(TASK_INSTANCE_ID, taskInstanceId));
    }

    @Override
    public int updateDqExecuteResultUserId(int taskInstanceId) {
        DqExecuteResult dqExecuteResult =
                dqExecuteResultMapper
                        .selectOne(new QueryWrapper<DqExecuteResult>().eq(TASK_INSTANCE_ID, taskInstanceId));
        if (dqExecuteResult == null) {
            return -1;
        }

        ProcessInstance processInstance = processInstanceMapper.selectById(dqExecuteResult.getProcessInstanceId());
        if (processInstance == null) {
            return -1;
        }

        ProcessDefinition processDefinition =
                processDefinitionMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition == null) {
            return -1;
        }

        dqExecuteResult.setProcessDefinitionId(processDefinition.getId());
        dqExecuteResult.setUserId(processDefinition.getUserId());
        dqExecuteResult.setState(DqTaskState.DEFAULT.getCode());
        return dqExecuteResultMapper.updateById(dqExecuteResult);
    }
}
