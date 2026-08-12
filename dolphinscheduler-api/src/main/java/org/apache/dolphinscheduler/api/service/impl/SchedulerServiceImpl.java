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
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.validator.TenantExistValidator;
import org.apache.dolphinscheduler.api.vo.ScheduleVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ScheduleDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    private ScheduleDao scheduleDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private SchedulerApi schedulerApi;

    @Autowired
    private TenantExistValidator tenantExistValidator;

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
     */
    @Override
    @Transactional
    public Schedule insertSchedule(User loginUser,
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

        Project project = projectDao.queryByCode(projectCode);

        // check project auth
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        // check workflow define release state
        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(workflowDefinitionCode).orElse(null);
        executorService.checkWorkflowDefinitionValid(projectCode, workflowDefinition, workflowDefinitionCode,
                workflowDefinition.getVersion());

        Schedule scheduleExists =
                scheduleDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        if (scheduleExists != null) {
            log.error("Schedule already exist, scheduleId:{}, workflowDefinitionCode:{}", scheduleExists.getId(),
                    workflowDefinitionCode);
            throw new ServiceException(Status.SCHEDULE_ALREADY_EXISTS, workflowDefinitionCode,
                    scheduleExists.getId());
        }

        Schedule scheduleObj = new Schedule();
        Date now = new Date();

        tenantExistValidator.validate(tenantCode);

        scheduleObj.setTenantCode(tenantCode);
        scheduleObj.setProjectName(project.getName());
        scheduleObj.setWorkflowDefinitionCode(workflowDefinitionCode);
        scheduleObj.setWorkflowDefinitionName(workflowDefinition.getName());

        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);
        if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
            log.warn("The start time must not be the same as the end or time can not be null.");
            throw new ServiceException(Status.SCHEDULE_START_TIME_END_TIME_SAME);
        }
        if (scheduleParam.getStartTime().getTime() > scheduleParam.getEndTime().getTime()) {
            log.warn("The start time must smaller than end time");
            throw new ServiceException(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR);
        }

        scheduleObj.setStartTime(scheduleParam.getStartTime());
        scheduleObj.setEndTime(scheduleParam.getEndTime());
        if (!CronUtils.isValidExpression(scheduleParam.getCrontab())) {
            log.error("Schedule crontab verify failure, crontab:{}.", scheduleParam.getCrontab());
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleParam.getCrontab());
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
        scheduleDao.insert(scheduleObj);

        /**
         * updateWorkflowInstance receivers and cc by workflow definition id
         */
        workflowDefinition.setWarningGroupId(warningGroupId);
        workflowDefinitionDao.updateById(workflowDefinition);

        log.info("Schedule create complete, projectCode:{}, workflowDefinitionCode:{}, scheduleId:{}.",
                projectCode, workflowDefinitionCode, scheduleObj.getId());
        return scheduleDao.queryById(scheduleObj.getId());
    }

    protected void projectPermCheckByWorkflowCode(User loginUser, long workflowDefinitionCode) {
        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(workflowDefinitionCode).orElse(null);
        if (workflowDefinition == null) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowDefinitionCode);
        }
        Project project = projectDao.queryByCode(workflowDefinition.getProjectCode());
        // check project auth
        this.projectService.checkProjectAndAuthThrowException(loginUser, project, null);
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
     */
    @Override
    @Transactional
    public Schedule updateSchedule(User loginUser,
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

        Project project = projectDao.queryByCode(projectCode);

        // check project auth
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        // check schedule exists
        Schedule schedule = scheduleDao.queryById(id);

        if (schedule == null) {
            log.error("Schedule does not exist, scheduleId:{}.", id);
            throw new ServiceException(Status.SCHEDULE_NOT_EXISTS, id);
        }

        WorkflowDefinition workflowDefinition =
                workflowDefinitionDao.queryByCode(schedule.getWorkflowDefinitionCode()).orElse(null);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.",
                    schedule.getWorkflowDefinitionCode());
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST,
                    String.valueOf(schedule.getWorkflowDefinitionCode()));
        }

        return updateSchedule(schedule, workflowDefinition, scheduleExpression, warningType, warningGroupId,
                failureStrategy, workflowInstancePriority, workerGroup, tenantCode, environmentCode);
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

        Project project = projectDao.queryByCode(projectCode);

        // check project auth
        projectService.checkProjectAndAuthThrowException(loginUser, project, PROJECT);

        if (workflowDefinitionCode != 0) {
            WorkflowDefinition workflowDefinition =
                    workflowDefinitionDao.queryByCode(workflowDefinitionCode).orElse(null);
            if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
                log.error("workflow definition does not exist, workflowDefinitionCode:{}.", workflowDefinitionCode);
                putMsg(result, Status.WORKFLOW_DEFINITION_NOT_EXIST, String.valueOf(workflowDefinitionCode));
                return result;
            }
        }

        Page<Schedule> page = new Page<>(pageNo, pageSize);

        IPage<Schedule> schedulePage =
                scheduleDao.queryByProjectAndWorkflowDefinitionCodePaging(page, projectCode, workflowDefinitionCode,
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
        return scheduleDao.querySchedulesByWorkflowDefinitionCodes(workflowDefinitionCodes);
    }

    /**
     * query schedule list
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return schedule list
     */
    @Override
    public List<ScheduleVO> queryScheduleList(User loginUser, long projectCode) {
        Project project = projectDao.queryByCode(projectCode);

        // check project auth
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        List<Schedule> schedules = scheduleDao.querySchedulerListByProjectName(project.getName());
        List<ScheduleVO> scheduleList = new ArrayList<>();
        for (Schedule schedule : schedules) {
            scheduleList.add(new ScheduleVO(schedule));
        }
        return scheduleList;
    }

    /**
     * delete schedule by id
     *
     * @param loginUser   login user
     * @param scheduleId  schedule id
     */
    @Override
    public void deleteSchedulesById(User loginUser, Integer scheduleId) {
        Schedule schedule = scheduleDao.queryById(scheduleId);
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
        boolean delete = scheduleDao.deleteById(scheduleId);
        if (!delete) {
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
    public List<String> previewSchedule(User loginUser, String schedule) {
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
            throw new ServiceException(Status.PARSE_TO_CRON_EXPRESSION_ERROR);
        }
        List<ZonedDateTime> selfFireDateList =
                CronUtils.getSelfFireDateList(startTime, endTime, cron, Constants.PREVIEW_SCHEDULE_EXECUTE_COUNT);
        return selfFireDateList.stream()
                .map(t -> DateUtils.dateToString(t, zoneId))
                .collect(Collectors.toList());
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
     */
    @Override
    public Schedule updateScheduleByWorkflowDefinitionCode(User loginUser,
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
        Project project = projectDao.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, null);

        // check schedule exists
        Schedule schedule = scheduleDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        if (schedule == null) {
            log.error("Schedule of workflow definition does not exist, workflowDefinitionCode:{}.",
                    workflowDefinitionCode);
            throw new ServiceException(Status.SCHEDULE_CRON_NOT_EXISTS, workflowDefinitionCode);
        }

        WorkflowDefinition workflowDefinition = workflowDefinitionDao.queryByCode(workflowDefinitionCode).orElse(null);
        if (workflowDefinition == null || projectCode != workflowDefinition.getProjectCode()) {
            log.error("workflow definition does not exist, workflowDefinitionCode:{}.", workflowDefinitionCode);
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST,
                    String.valueOf(workflowDefinitionCode));
        }

        return updateSchedule(schedule, workflowDefinition, scheduleExpression, warningType, warningGroupId,
                failureStrategy, workflowInstancePriority, workerGroup, tenantCode, environmentCode);
    }

    @Transactional
    @Override
    public void onlineScheduler(User loginUser, Long projectCode, Integer schedulerId) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_ONLINE_OFFLINE);
        Schedule schedule = scheduleDao.queryById(schedulerId);
        doOnlineScheduler(schedule);
    }

    @Transactional
    @Override
    public void onlineSchedulerByWorkflowCode(Long workflowDefinitionCode) {
        Schedule schedule = scheduleDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
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
                workflowDefinitionDao.queryByCode(schedule.getWorkflowDefinitionCode()).orElse(null);
        if (!ReleaseState.ONLINE.equals(workflowDefinition.getReleaseState())) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_RELEASE, workflowDefinition.getName());
        }

        schedule.setReleaseState(ReleaseState.ONLINE);
        schedule.setUpdateTime(new Date());
        scheduleDao.updateById(schedule);

        Project project = projectDao.queryByCode(workflowDefinition.getProjectCode());
        schedulerApi.insertOrUpdateScheduleTask(project.getId(), schedule);
    }

    @Transactional
    @Override
    public void offlineScheduler(User loginUser, Long projectCode, Integer schedulerId) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, WORKFLOW_ONLINE_OFFLINE);
        Schedule schedule = scheduleDao.queryById(schedulerId);
        doOfflineScheduler(schedule);
    }

    @Transactional
    @Override
    public void offlineSchedulerByWorkflowCode(Long workflowDefinitionCode) {
        Schedule schedule = scheduleDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
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
        schedule.setUpdateTime(new Date());
        schedule.setReleaseState(ReleaseState.OFFLINE);
        scheduleDao.updateById(schedule);
        WorkflowDefinition workflowDefinition =
                workflowDefinitionDao.queryByCode(schedule.getWorkflowDefinitionCode()).orElse(null);
        Project project = projectDao.queryByCode(workflowDefinition.getProjectCode());
        schedulerApi.deleteScheduleTask(project.getId(), schedule.getId());
    }

    private Schedule updateSchedule(Schedule schedule, WorkflowDefinition workflowDefinition,
                                    String scheduleExpression, WarningType warningType, int warningGroupId,
                                    FailureStrategy failureStrategy, Priority workflowInstancePriority,
                                    String workerGroup, String tenantCode, long environmentCode) {
        if (schedule.getReleaseState() == ReleaseState.ONLINE) {
            log.warn("Schedule can not be updated due to schedule is {}, scheduleId:{}.",
                    ReleaseState.ONLINE.getDescp(), schedule.getId());
            throw new ServiceException(Status.SCHEDULE_CRON_ONLINE_FORBID_UPDATE);
        }

        Date now = new Date();

        tenantExistValidator.validate(tenantCode);
        schedule.setTenantCode(tenantCode);

        // updateWorkflowInstance param
        if (!StringUtils.isEmpty(scheduleExpression)) {
            ScheduleParam scheduleParam = JSONUtils.parseObject(scheduleExpression, ScheduleParam.class);
            if (scheduleParam == null) {
                log.warn("Parameter scheduleExpression is invalid, so parse cron error.");
                throw new ServiceException(Status.PARSE_TO_CRON_EXPRESSION_ERROR);
            }
            if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
                log.warn("The start time must not be the same as the end or time can not be null.");
                throw new ServiceException(Status.SCHEDULE_START_TIME_END_TIME_SAME);
            }
            if (scheduleParam.getStartTime().getTime() > scheduleParam.getEndTime().getTime()) {
                log.warn("The start time must smaller than end time");
                throw new ServiceException(Status.START_TIME_BIGGER_THAN_END_TIME_ERROR);
            }

            schedule.setStartTime(scheduleParam.getStartTime());
            schedule.setEndTime(scheduleParam.getEndTime());
            if (!CronUtils.isValidExpression(scheduleParam.getCrontab())) {
                log.error("Schedule crontab verify failure, crontab:{}.", scheduleParam.getCrontab());
                throw new ServiceException(Status.SCHEDULE_CRON_CHECK_FAILED, scheduleParam.getCrontab());
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
        scheduleDao.updateById(schedule);

        workflowDefinition.setWarningGroupId(warningGroupId);

        workflowDefinitionDao.updateById(workflowDefinition);

        log.info("Schedule update complete, projectCode:{}, workflowDefinitionCode:{}, scheduleId:{}.",
                workflowDefinition.getProjectCode(), workflowDefinition.getCode(), schedule.getId());
        return schedule;
    }

}
