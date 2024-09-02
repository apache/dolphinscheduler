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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_ONLINE_OFFLINE;

import org.apache.dolphinscheduler.api.dto.ScheduleParam;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleCreateRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleFilterRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.ScheduleVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cronutils.model.Cron;

@Service
@Slf4j
public class SchedulerServiceImpl extends BaseServiceImpl implements SchedulerService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private SchedulerApi schedulerApi;

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Autowired
    private TenantMapper tenantMapper;

    /**
     * save schedule
     *
     * @param loginUser               login user
     * @param projectCode             project name
     * @param workflowDefinitionCode       workflow definition code
     * @param schedule                scheduler
     * @param warningType             warning type
     * @param warningGroupId          warning group id
     * @param failureStrategy         failure strategy
     * @param workflowInstancePriority workflow instance priority
     * @param workerGroup             worker group
     * @param tenantCode              tenant code
     * @param environmentCode         environment code
     * @return create result code
     */
    @Override
    @Transactional
    public Map<String, Object> insertSchedule(User loginUser,
                                              long projectCode,
                                              long workflowDefinitionCode,
                                              String schedule,
                                              WarningType warningType,
                                              int warningGroupId,
                                              FailureStrategy failureStrategy,
                                              Priority workflowInstancePriority,
                                              String workerGroup,
                                              String tenantCode,
                                              Long environmentCode) {

        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        // check workflow define release state
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
        executorService.checkWorkflowDefinitionValid(projectCode, workflowDefinition, workflowDefinitionCode,
                workflowDefinition.getVersion());

        Schedule scheduleExists =
                scheduleMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        if (scheduleExists != null) {
            log.error("Schedule already exist, scheduleId:{}, workflowDefinitionCode:{}", scheduleExists.getId(),
                    workflowDefinitionCode);
            putMsg(result, Status.SCHEDULE_ALREADY_EXISTS, workflowDefinitionCode, scheduleExists.getId());
            return result;
        }

        Schedule scheduleObj = new Schedule();
        Date now = new Date();

        checkValidTenant(tenantCode);

        scheduleObj.setTenantCode(tenantCode);
        scheduleObj.setProjectName(project.getName());
        scheduleObj.setWorkflowDefinitionCode(workflowDefinitionCode);
        scheduleObj.setWorkflowDefinitionName(workflowDefinition.getName());

        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);
        if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
            log.warn("The start time must not be the same as the end or time can not be null.");
            putMsg(result, Status.SCHEDULE_START_TIME_END_TIME_SAME);
            return result;
        }
        if (scheduleParam.getStartTime().getTime() > scheduleParam.getEndTime().getTime()) {
            log.warn("The start time must smaller than end time");
            putMsg(result, Status.START_TIME_BIGGER_THAN_END_TIME_ERROR);
            return result;
        }

        scheduleObj.setStartTime(scheduleParam.getStartTime());
        scheduleObj.setEndTime(scheduleParam.getEndTime());
        if (!CronUtils.isValidExpression(scheduleParam.getCrontab())) {
            log.error("Schedule crontab verify failure, crontab:{}.", scheduleParam.getCrontab());
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleParam.getCrontab());
            return result;
        }
        scheduleObj.setCrontab(scheduleParam.getCrontab());
        scheduleObj.setTimezoneId(scheduleParam.getTimezoneId());
        scheduleObj.setWarningType(warningType);
        scheduleObj.setWarningGroupId(warningGroupId);
        scheduleObj.setFailureStrategy(failureStrategy);
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setUserName(loginUser.getUserName());
        scheduleObj.setReleaseState(ReleaseState.OFFLINE);
        scheduleObj.setWorkflowInstancePriority(workflowInstancePriority);
        scheduleObj.setWorkerGroup(workerGroup);
        scheduleObj.setEnvironmentCode(environmentCode);
        scheduleMapper.insert(scheduleObj);

        /**
         * updateWorkflowInstance receivers and cc by workflow definition id
         */
        workflowDefinition.setWarningGroupId(warningGroupId);
        workflowDefinitionMapper.updateById(workflowDefinition);

        // return scheduler object with ID
        result.put(Constants.DATA_LIST, scheduleMapper.selectById(scheduleObj.getId()));
        putMsg(result, Status.SUCCESS);
        log.info("Schedule create complete, projectCode:{}, workflowDefinitionCode:{}, scheduleId:{}.",
                projectCode, workflowDefinitionCode, scheduleObj.getId());
        result.put("scheduleId", scheduleObj.getId());
        return result;
    }

    protected void projectPermCheckByWorkflowCode(User loginUser, long workflowDefinitionCode) {
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
        if (workflowDefinition == null) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowDefinitionCode);
        }
        Project project = projectMapper.queryByCode(workflowDefinition.getProjectCode());
        // check project auth
        this.projectService.checkProjectAndAuthThrowException(loginUser, project, null);
    }

    private void scheduleParamCheck(String scheduleParamStr) {
        ScheduleParam scheduleParam = JSONUtils.parseObject(scheduleParamStr, ScheduleParam.class);
        if (scheduleParam == null) {
            throw new ServiceException(Status.PARSE_SCHEDULE_PARAM_ERROR, scheduleParamStr);
        }
        if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
            throw new ServiceException(Status.SCHEDULE_START_TIME_END_TIME_SAME);
        }
        if (scheduleParam.getStartTime().getTime() > scheduleParam.getEndTime().getTime()) {
            throw new ServiceException(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR);
        }
        if (!CronUtils.isValidExpression(scheduleParam.getCrontab())) {
            throw new ServiceException(Status.SCHEDULE_CRON_CHECK_FAILED, scheduleParam.getCrontab());
        }
    }

    /**
     * save schedule V2, will also change workflow definition's warningGroupId if schedule's warningGroupId be set
     *
     * @param loginUser               login user
     * @param scheduleCreateRequest   schedule create object
     * @return Schedule object just be created
     */
    @Override
    @Transactional
    public Schedule createSchedulesV2(User loginUser,
                                      ScheduleCreateRequest scheduleCreateRequest) {
        this.projectPermCheckByWorkflowCode(loginUser, scheduleCreateRequest.getWorkflowDefinitionCode());

        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(scheduleCreateRequest.getWorkflowDefinitionCode());

        // check workflow define release state
        executorService.checkWorkflowDefinitionValid(workflowDefinition.getProjectCode(), workflowDefinition,
                workflowDefinition.getCode(), workflowDefinition.getVersion());

        Schedule scheduleExists =
                scheduleMapper.queryByWorkflowDefinitionCode(scheduleCreateRequest.getWorkflowDefinitionCode());
        if (scheduleExists != null) {
            throw new ServiceException(Status.SCHEDULE_ALREADY_EXISTS,
                    scheduleCreateRequest.getWorkflowDefinitionCode(),
                    scheduleExists.getId());
        }

        checkValidTenant(scheduleCreateRequest.getTenantCode());

        Schedule schedule = scheduleCreateRequest.convert2Schedule();
        Environment environment = environmentMapper.queryByEnvironmentCode(schedule.getEnvironmentCode());
        if (environment == null) {
            throw new ServiceException(Status.QUERY_ENVIRONMENT_BY_CODE_ERROR, schedule.getEnvironmentCode());
        }

        schedule.setUserId(loginUser.getId());
        // give more detail when return schedule object
        schedule.setUserName(loginUser.getUserName());
        schedule.setWorkflowDefinitionName(workflowDefinition.getName());

        this.scheduleParamCheck(scheduleCreateRequest.getScheduleParam());
        int create = scheduleMapper.insert(schedule);
        if (create <= 0) {
            throw new ServiceException(Status.CREATE_SCHEDULE_ERROR);
        }
        // updateWorkflowInstance receivers and cc by workflow definition id
        workflowDefinition.setWarningGroupId(schedule.getWarningGroupId());
        workflowDefinitionMapper.updateById(workflowDefinition);
        return schedule;
    }

    /**
     * updateWorkflowInstance schedule
     *
     * @param loginUser               login user
     * @param projectCode             project code
     * @param id                      scheduler id
     * @param scheduleExpression      scheduler
     * @param warningType             warning type
     * @param warningGroupId          warning group id
     * @param failureStrategy         failure strategy
     * @param workerGroup             worker group
     * @param tenantCode              tenant code
     * @param environmentCode         environment code
     * @param workflowInstancePriority workflow instance priority
     * @return update result code
     */
    @Override
    @Transactional
    public Map<String, Object> updateSchedule(User loginUser,
                                              long projectCode,
                                              Integer id,
                                              String scheduleExpression,
                                              WarningType warningType,
                                              int warningGroupId,
                                              FailureStrategy failureStrategy,
                                              Priority workflowInstancePriority,
                                              String workerGroup,
                                              String tenantCode,
                                              Long environmentCode) {
        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        // check schedule exists
        Schedule schedule = scheduleMapper.selectById(id);

        if (schedule == null) {
            log.error("Schedule does not exist, scheduleId:{}.", id);
            putMsg(result, Status.SCHEDULE_NOT_EXISTS, id);
            return result;
        }

        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(schedule.getWorkflowDefinitionCode());
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.",
                    schedule.getWorkflowDefinitionCode());
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(schedule.getWorkflowDefinitionCode()));
            return result;
        }

        updateSchedule(result, schedule, workflowDefinition, scheduleExpression, warningType, warningGroupId,
                failureStrategy, workflowInstancePriority, workerGroup, tenantCode, environmentCode);
        return result;
    }

    /**
     * update schedule object V2
     *
     * @param loginUser login user
     * @param scheduleId scheduler id
     * @param scheduleUpdateRequest the schedule object will be updated
     * @return Schedule object
     */
    @Override
    @Transactional
    public Schedule updateSchedulesV2(User loginUser,
                                      Integer scheduleId,
                                      ScheduleUpdateRequest scheduleUpdateRequest) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new ServiceException(Status.SCHEDULE_NOT_EXISTS, scheduleId);
        }

        Schedule scheduleUpdate;
        try {
            scheduleUpdate = scheduleUpdateRequest.mergeIntoSchedule(schedule);
            // check update params
            this.scheduleParamCheck(scheduleUpdateRequest.updateScheduleParam(scheduleUpdate));
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException
                | NoSuchMethodException e) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleUpdateRequest.toString());
        }
        // check update params
        this.projectPermCheckByWorkflowCode(loginUser, scheduleUpdate.getWorkflowDefinitionCode());

        if (scheduleUpdate.getEnvironmentCode() != null) {
            Environment environment = environmentMapper.queryByEnvironmentCode(scheduleUpdate.getEnvironmentCode());
            if (environment == null) {
                throw new ServiceException(Status.QUERY_ENVIRONMENT_BY_CODE_ERROR, scheduleUpdate.getEnvironmentCode());
            }
        }

        int update = scheduleMapper.updateById(scheduleUpdate);
        if (update <= 0) {
            throw new ServiceException(Status.UPDATE_SCHEDULE_ERROR);
        }
        return scheduleUpdate;
    }

    /**
     * get schedule object
     *
     * @param loginUser login user
     * @param scheduleId scheduler id
     * @return Schedule object
     */
    @Override
    @Transactional
    public Schedule getSchedule(User loginUser,
                                Integer scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new ServiceException(Status.SCHEDULE_NOT_EXISTS, scheduleId);
        }
        this.projectPermCheckByWorkflowCode(loginUser, schedule.getWorkflowDefinitionCode());
        return schedule;
    }

    /**
     * query schedule
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param workflowDefinitionCode workflow definition code
     * @param pageNo            page number
     * @param pageSize          page size
     * @param searchVal         search value
     * @return schedule list page
     */
    @Override
    public Result querySchedule(User loginUser, long projectCode, long workflowDefinitionCode, String searchVal,
                                Integer pageNo, Integer pageSize) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }

        if (workflowDefinitionCode != 0) {
            WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
            if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
                log.error("workflow definition does not exist, workflowDefinitionCode:{}.", workflowDefinitionCode);
                putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(workflowDefinitionCode));
                return result;
            }
        }

        Page<Schedule> page = new Page<>(pageNo, pageSize);

        IPage<Schedule> schedulePage =
                scheduleMapper.queryByProjectAndWorkflowDefinitionCodePaging(page, projectCode, workflowDefinitionCode,
                        searchVal);

        List<ScheduleVO> scheduleList = new ArrayList<>();
        for (Schedule schedule : schedulePage.getRecords()) {
            scheduleList.add(new ScheduleVO(schedule));
        }

        PageInfo<ScheduleVO> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) schedulePage.getTotal());
        pageInfo.setTotalList(scheduleList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    public List<Schedule> queryScheduleByWorkflowDefinitionCodes(@NonNull List<Long> workflowDefinitionCodes) {
        if (CollectionUtils.isEmpty(workflowDefinitionCodes)) {
            return Collections.emptyList();
        }
        return scheduleMapper.querySchedulesByWorkflowDefinitionCodes(workflowDefinitionCodes);
    }

    /**
     * query schedule
     *
     * @param loginUser login user
     * @param scheduleFilterRequest schedule filter request
     * @return schedule list page
     */
    @Override
    @Transactional
    public PageInfo<Schedule> filterSchedules(User loginUser,
                                              ScheduleFilterRequest scheduleFilterRequest) {
        if (scheduleFilterRequest.getProjectName() != null) {
            Project project = projectMapper.queryByName(scheduleFilterRequest.getProjectName());
            // check project auth
            projectService.checkProjectAndAuthThrowException(loginUser, project, null);
        }
        Page<Schedule> page = new Page<>(scheduleFilterRequest.getPageNo(), scheduleFilterRequest.getPageSize());
        IPage<Schedule> scheduleIPage = scheduleMapper.filterSchedules(page, scheduleFilterRequest.convert2Schedule());

        PageInfo<Schedule> pageInfo =
                new PageInfo<>(scheduleFilterRequest.getPageNo(), scheduleFilterRequest.getPageSize());
        pageInfo.setTotal((int) scheduleIPage.getTotal());
        pageInfo.setTotalList(scheduleIPage.getRecords());

        return pageInfo;
    }

    /**
     * query schedule list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return schedule list
     */
    @Override
    public Map<String, Object> queryScheduleList(User loginUser, long projectCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);

        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        List<Schedule> schedules = scheduleMapper.querySchedulerListByProjectName(project.getName());
        List<ScheduleVO> scheduleList = new ArrayList<>();
        for (Schedule schedule : schedules) {
            scheduleList.add(new ScheduleVO(schedule));
        }

        result.put(Constants.DATA_LIST, scheduleList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * check valid
     *
     * @param result result
     * @param bool   bool
     * @param status status
     * @return check result code
     */
    private boolean checkValid(Map<String, Object> result, boolean bool, Status status) {
        // timeout is valid
        if (bool) {
            putMsg(result, status);
            return true;
        }
        return false;
    }

    /**
     * delete schedule by id
     *
     * @param loginUser   login user
     * @param scheduleId  schedule id
     */
    @Override
    public void deleteSchedulesById(User loginUser, Integer scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new ServiceException(Status.SCHEDULE_NOT_EXISTS, scheduleId);
        }
        // check schedule is already online
        if (schedule.getReleaseState() == ReleaseState.ONLINE) {
            throw new ServiceException(Status.SCHEDULE_STATE_ONLINE, scheduleId);
        }
        // Determine if the login user is the owner of the schedule
        if (loginUser.getId() != schedule.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        this.projectPermCheckByWorkflowCode(loginUser, schedule.getWorkflowDefinitionCode());
        int delete = scheduleMapper.deleteById(scheduleId);
        if (delete <= 0) {
            throw new ServiceException(Status.DELETE_SCHEDULE_BY_ID_ERROR);
        }
    }

    /**
     * preview schedule
     *
     * @param loginUser login user
     * @param schedule  schedule expression
     * @return the next five fire time
     */
    @Override
    public Map<String, Object> previewSchedule(User loginUser, String schedule) {
        Map<String, Object> result = new HashMap<>();
        Cron cron;
        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);

        assert scheduleParam != null;
        ZoneId zoneId = TimeZone.getTimeZone(scheduleParam.getTimezoneId()).toZoneId();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime startTime = ZonedDateTime.ofInstant(scheduleParam.getStartTime().toInstant(), zoneId);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(scheduleParam.getEndTime().toInstant(), zoneId);
        startTime = now.isAfter(startTime) ? now : startTime;

        try {
            cron = CronUtils.parse2Cron(scheduleParam.getCrontab());
        } catch (CronParseException e) {
            log.error("Parse cron to cron expression error, crontab:{}.", scheduleParam.getCrontab(), e);
            putMsg(result, Status.PARSE_TO_CRON_EXPRESSION_ERROR);
            return result;
        }
        List<ZonedDateTime> selfFireDateList =
                CronUtils.getSelfFireDateList(startTime, endTime, cron, Constants.PREVIEW_SCHEDULE_EXECUTE_COUNT);
        List<String> previewDateList =
                selfFireDateList.stream().map(t -> DateUtils.dateToString(t, zoneId)).collect(Collectors.toList());
        result.put(Constants.DATA_LIST, previewDateList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update workflow definition schedule
     *
     * @param loginUser               login user
     * @param projectCode             project code
     * @param workflowDefinitionCode   workflow definition code
     * @param scheduleExpression      scheduleExpression
     * @param warningType             warning type
     * @param warningGroupId          warning group id
     * @param failureStrategy         failure strategy
     * @param workerGroup             worker group
     * @param tenantCode              tenant code
     * @param workflowInstancePriority workflow instance priority
     * @return update result code
     */
    @Override
    public Map<String, Object> updateScheduleByWorkflowDefinitionCode(User loginUser,
                                                                      long projectCode,
                                                                      long workflowDefinitionCode,
                                                                      String scheduleExpression,
                                                                      WarningType warningType,
                                                                      int warningGroupId,
                                                                      FailureStrategy failureStrategy,
                                                                      Priority workflowInstancePriority,
                                                                      String workerGroup,
                                                                      String tenantCode,
                                                                      long environmentCode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode, null);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // check schedule exists
        Schedule schedule = scheduleMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        if (schedule == null) {
            log.error("Schedule of workflow definition does not exist, workflowDefinitionCode:{}.",
                    workflowDefinitionCode);
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, workflowDefinitionCode);
            return result;
        }

        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", workflowDefinitionCode);
            putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(workflowDefinitionCode));
            return result;
        }

        updateSchedule(result, schedule, workflowDefinition, scheduleExpression, warningType, warningGroupId,
                failureStrategy, workflowInstancePriority, workerGroup, tenantCode, environmentCode);
        return result;
    }

    @Transactional
    @Override
    public void onlineScheduler(User loginUser, Long projectCode, Integer schedulerId) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_ONLINE_OFFLINE);
        Schedule schedule = scheduleMapper.selectById(schedulerId);
        doOnlineScheduler(schedule);
    }

    @Transactional
    @Override
    public void onlineSchedulerByWorkflowCode(Long workflowDefinitionCode) {
        Schedule schedule = scheduleMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        doOnlineScheduler(schedule);
    }

    private void doOnlineScheduler(Schedule schedule) {
        if (schedule == null) {
            return;
        }
        if (ReleaseState.ONLINE.equals(schedule.getReleaseState())) {
            log.debug("The schedule is already online, scheduleId:{}.", schedule.getId());
            return;
        }
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(schedule.getWorkflowDefinitionCode());
        if (!ReleaseState.ONLINE.equals(workflowDefinition.getReleaseState())) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_RELEASE, workflowDefinition.getName());
        }

        schedule.setReleaseState(ReleaseState.ONLINE);
        scheduleMapper.updateById(schedule);

        Project project = projectMapper.queryByCode(workflowDefinition.getProjectCode());
        schedulerApi.insertOrUpdateScheduleTask(project.getId(), schedule);
    }

    @Transactional
    @Override
    public void offlineScheduler(User loginUser, Long projectCode, Integer schedulerId) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_ONLINE_OFFLINE);
        Schedule schedule = scheduleMapper.selectById(schedulerId);
        doOfflineScheduler(schedule);
    }

    @Transactional
    @Override
    public void offlineSchedulerByWorkflowCode(Long workflowDefinitionCode) {
        Schedule schedule = scheduleMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        doOfflineScheduler(schedule);
    }

    private void doOfflineScheduler(Schedule schedule) {
        if (schedule == null) {
            return;
        }
        if (ReleaseState.OFFLINE.equals(schedule.getReleaseState())) {
            log.debug("The schedule is already offline, scheduleId:{}.", schedule.getId());
            return;
        }
        schedule.setReleaseState(ReleaseState.OFFLINE);
        scheduleMapper.updateById(schedule);
        WorkflowDefinition workflowDefinition =
                workflowDefinitionMapper.queryByCode(schedule.getWorkflowDefinitionCode());
        Project project = projectMapper.queryByCode(workflowDefinition.getProjectCode());
        schedulerApi.deleteScheduleTask(project.getId(), schedule.getId());
    }

    private void updateSchedule(Map<String, Object> result, Schedule schedule, WorkflowDefinition workflowDefinition,
                                String scheduleExpression, WarningType warningType, int warningGroupId,
                                FailureStrategy failureStrategy, Priority workflowInstancePriority, String workerGroup,
                                String tenantCode,
                                long environmentCode) {
        if (checkValid(result, schedule.getReleaseState() == ReleaseState.ONLINE,
                Status.SCHEDULE_CRON_ONLINE_FORBID_UPDATE)) {
            log.warn("Schedule can not be updated due to schedule is {}, scheduleId:{}.",
                    ReleaseState.ONLINE.getDescp(), schedule.getId());
            return;
        }

        Date now = new Date();

        checkValidTenant(tenantCode);
        schedule.setTenantCode(tenantCode);

        // updateWorkflowInstance param
        if (!StringUtils.isEmpty(scheduleExpression)) {
            ScheduleParam scheduleParam = JSONUtils.parseObject(scheduleExpression, ScheduleParam.class);
            if (scheduleParam == null) {
                log.warn("Parameter scheduleExpression is invalid, so parse cron error.");
                putMsg(result, Status.PARSE_TO_CRON_EXPRESSION_ERROR);
                return;
            }
            if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
                log.warn("The start time must not be the same as the end or time can not be null.");
                putMsg(result, Status.SCHEDULE_START_TIME_END_TIME_SAME);
                return;
            }
            if (scheduleParam.getStartTime().getTime() > scheduleParam.getEndTime().getTime()) {
                log.warn("The start time must smaller than end time");
                putMsg(result, Status.START_TIME_BIGGER_THAN_END_TIME_ERROR);
                return;
            }

            schedule.setStartTime(scheduleParam.getStartTime());
            schedule.setEndTime(scheduleParam.getEndTime());
            if (!CronUtils.isValidExpression(scheduleParam.getCrontab())) {
                log.error("Schedule crontab verify failure, crontab:{}.", scheduleParam.getCrontab());
                putMsg(result, Status.SCHEDULE_CRON_CHECK_FAILED, scheduleParam.getCrontab());
                return;
            }
            schedule.setCrontab(scheduleParam.getCrontab());
            schedule.setTimezoneId(scheduleParam.getTimezoneId());
        }

        if (warningType != null) {
            schedule.setWarningType(warningType);
        }

        schedule.setWarningGroupId(warningGroupId);

        if (failureStrategy != null) {
            schedule.setFailureStrategy(failureStrategy);
        }

        schedule.setWorkerGroup(workerGroup);
        schedule.setEnvironmentCode(environmentCode);
        schedule.setUpdateTime(now);
        schedule.setWorkflowInstancePriority(workflowInstancePriority);
        scheduleMapper.updateById(schedule);

        workflowDefinition.setWarningGroupId(warningGroupId);

        workflowDefinitionMapper.updateById(workflowDefinition);

        log.info("Schedule update complete, projectCode:{}, workflowDefinitionCode:{}, scheduleId:{}.",
                workflowDefinition.getProjectCode(), workflowDefinition.getCode(), schedule.getId());
        result.put(Constants.DATA_LIST, schedule);
        putMsg(result, Status.SUCCESS);
    }

    /**
     * check valid tenant
     *
     * @param tenantCode
     */
    private void checkValidTenant(String tenantCode) {
        if (!Constants.DEFAULT.equals(tenantCode)) {
            Tenant tenant = tenantMapper.queryByTenantCode(tenantCode);
            if (tenant == null) {
                throw new ServiceException(Status.TENANT_NOT_EXIST, tenantCode);
            }
        }
    }
}
