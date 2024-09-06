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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.Schedule;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * scheduler mapper interface
 */
public interface ScheduleMapper extends BaseMapper<Schedule> {

    int insert(Schedule entity);

    int updateById(@Param("et") Schedule entity);

    /**
     * query schedule list by process definition code
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return schedule list
     */
    List<Schedule> queryReleaseSchedulerListByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);

    /**
     * scheduler page
     *
     * @param page page
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param searchVal searchVal
     * @return scheduler IPage
     */
    IPage<Schedule> queryByWorkflowDefinitionCodePaging(IPage<Schedule> page,
                                                        @Param("workflowDefinitionCode") long workflowDefinitionCode,
                                                        @Param("searchVal") String searchVal);

    /**
     * scheduler page
     *
     * @param page page
     * @param projectCode projectCode
     * @param workflowDefinitionCode workflowDefinitionCode
     * @param searchVal searchVal
     * @return scheduler IPage
     */
    IPage<Schedule> queryByProjectAndWorkflowDefinitionCodePaging(IPage<Schedule> page,
                                                                  @Param("projectCode") long projectCode,
                                                                  @Param("workflowDefinitionCode") long workflowDefinitionCode,
                                                                  @Param("searchVal") String searchVal);

    /**
     * Filter schedule
     *
     * @param page page
     * @param schedule schedule
     * @return schedule IPage
     */
    IPage<Schedule> filterSchedules(IPage<Schedule> page,
                                    @Param("schedule") Schedule schedule);

    /**
     * query schedule list by project name
     *
     * @param projectName projectName
     * @return schedule list
     */
    List<Schedule> querySchedulerListByProjectName(@Param("projectName") String projectName);

    /**
     * query schedule list by process definition codes
     *
     * @param workflowDefinitionCodes workflowDefinitionCodes
     * @return schedule list
     */
    List<Schedule> selectAllByWorkflowDefinitionArray(@Param("workflowDefinitionCodes") long[] workflowDefinitionCodes);

    /**
     * query schedule list by process definition code
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return schedule
     */
    Schedule queryByWorkflowDefinitionCode(@Param("workflowDefinitionCode") long workflowDefinitionCode);

    /**
     * query worker group list by process definition code
     *
     * @param workflowDefinitionCodeList workflowDefinitionCodeList
     * @return schedule
     */
    List<Schedule> querySchedulesByWorkflowDefinitionCodes(@Param("workflowDefinitionCodeList") List<Long> workflowDefinitionCodeList);

    /**
     * query schedule by tenant
     *
     * @param tenantCode tenantCode
     * @return schedule list
     */
    List<Schedule> queryScheduleListByTenant(@Param("tenantCode") String tenantCode);
}
