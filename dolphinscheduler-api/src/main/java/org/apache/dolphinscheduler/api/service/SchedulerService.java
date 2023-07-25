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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.schedule.ScheduleCreateRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleFilterRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleUpdateRequest;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * scheduler service
 */
public interface SchedulerService {

    /**
     * save schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefineCode process definition code
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @param tenantCode tenant code
     * @param environmentCode environment code
     * @return create result code
     */
    Map<String, Object> insertSchedule(User loginUser,
                                       long projectCode,
                                       long processDefineCode,
                                       String schedule,
                                       WarningType warningType,
                                       int warningGroupId,
                                       FailureStrategy failureStrategy,
                                       Priority processInstancePriority,
                                       String workerGroup,
                                       String tenantCode,
                                       Long environmentCode);

    /**
     * save schedule V2
     *
     * @param loginUser             login user
     * @param scheduleCreateRequest the new schedule object will be created
     * @return Schedule object
     */
    Schedule createSchedulesV2(User loginUser,
                               ScheduleCreateRequest scheduleCreateRequest);

    /**
     * updateProcessInstance schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id scheduler id
     * @param scheduleExpression scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param workerGroup worker group
     * @param tenantCode tenant code
     * @param environmentCode environment code
     * @param processInstancePriority process instance priority
     * @return update result code
     */
    Map<String, Object> updateSchedule(User loginUser,
                                       long projectCode,
                                       Integer id,
                                       String scheduleExpression,
                                       WarningType warningType,
                                       int warningGroupId,
                                       FailureStrategy failureStrategy,
                                       Priority processInstancePriority,
                                       String workerGroup,
                                       String tenantCode,
                                       Long environmentCode);

    /**
     * update schedule object V2
     *
     * @param loginUser login user
     * @param scheduleId scheduler id
     * @param scheduleUpdateRequest the schedule object will be updated
     * @return Schedule object
     */
    Schedule updateSchedulesV2(User loginUser,
                               Integer scheduleId,
                               ScheduleUpdateRequest scheduleUpdateRequest);

    /**
     * get schedule object
     *
     * @param loginUser login user
     * @param scheduleId scheduler id
     * @return Schedule object
     */
    Schedule getSchedule(User loginUser,
                         Integer scheduleId);

    /**
     * set schedule online or offline
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param id scheduler id
     * @param scheduleStatus schedule status
     */
    void setScheduleState(User loginUser,
                          long projectCode,
                          Integer id,
                          ReleaseState scheduleStatus);

    /**
     * query schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefineCode process definition code
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @return schedule list page
     */
    Result querySchedule(User loginUser, long projectCode, long processDefineCode, String searchVal,
                         Integer pageNo, Integer pageSize);

    List<Schedule> queryScheduleByProcessDefinitionCodes(List<Long> processDefinitionCodes);

    /**
     * query schedule V2
     *
     * @param loginUser login user
     * @param scheduleFilterRequest schedule filter request
     * @return schedule list page
     */
    PageInfo<Schedule> filterSchedules(User loginUser,
                                       ScheduleFilterRequest scheduleFilterRequest);

    /**
     * query schedule list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return schedule list
     */
    Map<String, Object> queryScheduleList(User loginUser, long projectCode);

    /**
     * delete schedule
     *
     * @param projectId project id
     * @param scheduleId schedule id
     * @throws RuntimeException runtime exception
     */
    void deleteSchedule(int projectId, int scheduleId);

    /**
     * delete schedule by id
     *
     * @param loginUser login user
     * @param scheduleId schedule id
     */
    void deleteSchedulesById(User loginUser, Integer scheduleId);

    /**
     * preview schedule
     *
     * @param loginUser login user
     * @param schedule schedule expression
     * @return the next five fire time
     */
    Map<String, Object> previewSchedule(User loginUser, String schedule);

    /**
     * update process definition schedule
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param scheduleExpression scheduleExpression
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param workerGroup worker group
     * @param tenantCode tenant code
     * @param processInstancePriority process instance priority
     * @return update result code
     */
    Map<String, Object> updateScheduleByProcessDefinitionCode(User loginUser,
                                                              long projectCode,
                                                              long processDefinitionCode,
                                                              String scheduleExpression,
                                                              WarningType warningType,
                                                              int warningGroupId,
                                                              FailureStrategy failureStrategy,
                                                              Priority processInstancePriority,
                                                              String workerGroup,
                                                              String tenantCode,
                                                              long environmentCode);
}
