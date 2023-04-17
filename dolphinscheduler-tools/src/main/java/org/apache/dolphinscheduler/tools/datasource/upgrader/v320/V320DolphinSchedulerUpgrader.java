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

package org.apache.dolphinscheduler.tools.datasource.upgrader.v320;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.tools.datasource.dao.UpgradeDao;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerUpgrader;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerVersion;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Slf4j
@Component
public class V320DolphinSchedulerUpgrader implements DolphinSchedulerUpgrader {

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Lazy()
    @Autowired
    private UpgradeDao upgradeDao;

    @Override
    public void doUpgrade() {
        upgradeWorkflowInstance();
        upgradeTaskInstance();
        upgradeDao.upgradeDolphinSchedulerDDL(getCurrentVersion().getVersionName() + "_schema",
                "dolphinscheduler_ddl_post.sql");
    }

    private void upgradeWorkflowInstance() {
        Map<Integer, String> userMap = userMapper.selectList(new QueryWrapper<>())
                .stream()
                .collect(Collectors.toMap(User::getId, User::getUserName));

        while (true) {
            LambdaQueryWrapper<ProcessInstance> wrapper = new QueryWrapper<ProcessInstance>()
                    .lambda()
                    .eq(ProcessInstance::getProjectCode, null)
                    .last("limit 1000");
            List<ProcessInstance> needUpdateWorkflowInstance = processInstanceMapper.selectList(wrapper);
            if (CollectionUtils.isEmpty(needUpdateWorkflowInstance)) {
                return;
            }
            needUpdateWorkflowInstance.parallelStream()
                    .forEach(processInstance -> {
                        ProcessDefinitionLog processDefinitionLog =
                                processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                                        processInstance.getProcessDefinitionCode(),
                                        processInstance.getProcessDefinitionVersion());
                        Schedule schedule =
                                scheduleMapper.queryByProcessDefinitionCode(processInstance.getProcessDefinitionCode());
                        if (processDefinitionLog != null) {
                            processInstance.setProjectCode(processDefinitionLog.getProjectCode());
                            processInstance.setTenantCode(
                                    StringUtils.defaultIfEmpty(schedule.getTenantCode(), Constants.DEFAULT));
                            processInstance.setExecutorName(userMap.get(processInstance.getExecutorId()));
                        } else {
                            processInstance.setProjectCode(-1L);
                        }
                        processInstanceMapper.updateById(processInstance);
                    });
            log.info("Success upgrade workflow instance, current batch size: {}", needUpdateWorkflowInstance.size());
        }
    }

    private void upgradeTaskInstance() {
        while (true) {
            LambdaQueryWrapper<TaskInstance> wrapper = new QueryWrapper<TaskInstance>()
                    .lambda()
                    .eq(TaskInstance::getProjectCode, null)
                    .last("limit 1000");
            List<TaskInstance> taskInstances = taskInstanceMapper.selectList(wrapper);
            if (CollectionUtils.isEmpty(taskInstances)) {
                return;
            }
            taskInstances.parallelStream()
                    .forEach(taskInstance -> {
                        ProcessInstance processInstance =
                                processInstanceMapper.selectById(taskInstance.getProcessInstanceId());
                        if (processInstance == null) {
                            taskInstance.setProjectCode(-1L);
                        } else {
                            taskInstance.setProjectCode(processInstance.getProjectCode());
                            taskInstance.setProcessInstanceName(processInstance.getName());
                            taskInstance.setExecutorName(processInstance.getExecutorName());
                        }
                        taskInstanceMapper.updateById(taskInstance);
                    });
            log.info("Success upgrade task instance, current batch size: {}", taskInstances.size());
        }
    }

    @Override
    public DolphinSchedulerVersion getCurrentVersion() {
        return DolphinSchedulerVersion.V3_2_0;
    }
}
