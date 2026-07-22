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

import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ScheduleDao;

import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class ScheduleDaoImpl extends BaseDao<Schedule, ScheduleMapper> implements ScheduleDao {

    public ScheduleDaoImpl(@NonNull ScheduleMapper scheduleMapper) {
        super(scheduleMapper);
    }

    @Override
    public Schedule queryByWorkflowDefinitionCode(long workflowDefinitionCode) {
        return mybatisMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
    }

    @Override
    public List<Schedule> querySchedulerListByProjectName(String projectName) {
        return mybatisMapper.querySchedulerListByProjectName(projectName);
    }

    @Override
    public IPage<Schedule> queryByProjectAndWorkflowDefinitionCodePaging(IPage<Schedule> page,
                                                                         long projectCode,
                                                                         long workflowDefinitionCode,
                                                                         String searchVal) {
        return mybatisMapper.queryByProjectAndWorkflowDefinitionCodePaging(page, projectCode, workflowDefinitionCode,
                searchVal);
    }

    @Override
    public List<Schedule> querySchedulesByWorkflowDefinitionCodes(List<Long> workflowDefinitionCodes) {
        return mybatisMapper.querySchedulesByWorkflowDefinitionCodes(workflowDefinitionCodes);
    }

    @Override
    public List<Schedule> queryScheduleListByTenant(String tenantCode) {
        return mybatisMapper.queryScheduleListByTenant(tenantCode);
    }

    @Override
    public List<Schedule> queryScheduleByWorkerGroup(String workerGroupName) {
        return mybatisMapper.selectList(
                new QueryWrapper<Schedule>().lambda().eq(Schedule::getWorkerGroup, workerGroupName));
    }
}
