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
package cn.escheduler.api.service;


import cn.escheduler.api.dto.ScheduleParam;
import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.common.enums.FailureStrategy;
import cn.escheduler.common.enums.Priority;
import cn.escheduler.common.enums.ReleaseState;
import cn.escheduler.common.enums.WarningType;
import cn.escheduler.common.model.MasterServer;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.mapper.MasterServerMapper;
import cn.escheduler.dao.mapper.ProcessDefinitionMapper;
import cn.escheduler.dao.mapper.ProjectMapper;
import cn.escheduler.dao.mapper.ScheduleMapper;
import cn.escheduler.dao.model.*;
import cn.escheduler.dao.utils.cron.CronUtils;
import cn.escheduler.server.quartz.ProcessScheduleJob;
import cn.escheduler.server.quartz.QuartzExecutors;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * scheduler service
 */
@Service
public class SchedulerService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ProcessDao processDao;

    @Autowired
    private MasterServerMapper masterServerMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    /**
     * save schedule
     *
     * @param loginUser
     * @param projectName
     * @param processDefineId
     * @param schedule
     * @param warningType
     * @param warningGroupId
     * @param failureStrategy
     * @return
     */
    @Transactional(value = "TransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> insertSchedule(User loginUser, String projectName, Integer processDefineId, String schedule, WarningType warningType,
                                              int warningGroupId, FailureStrategy failureStrategy,
                                              String receivers, String receiversCc,Priority processInstancePriority, int workerGroupId) throws IOException {

        Map<String, Object> result = new HashMap<String, Object>(5);

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        Map<String, Object> checkResult = checkAuth(loginUser, projectName, project);
        if (checkResult != null) {
            return checkResult;
        }

        // check work flow define release state
        ProcessDefinition processDefinition = processDao.findProcessDefineById(processDefineId);
        result = executorService.checkProcessDefinitionValid(processDefinition, processDefineId);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        Schedule scheduleObj = new Schedule();
        Date now = new Date();

        scheduleObj.setProjectName(projectName);
        scheduleObj.setProcessDefinitionId(processDefinition.getId());
        scheduleObj.setProcessDefinitionName(processDefinition.getName());

        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);
        if (DateUtils.differSec(scheduleParam.getStartTime(),scheduleParam.getEndTime()) == 0) {
            logger.warn("The start time must not be the same as the end");
            putMsg(result,Status.SCHEDULE_START_TIME_END_TIME_SAME);
            return result;
        }
        scheduleObj.setStartTime(scheduleParam.getStartTime());
        scheduleObj.setEndTime(scheduleParam.getEndTime());
        if (!org.quartz.CronExpression.isValidExpression(scheduleParam.getCrontab())) {
            logger.error(scheduleParam.getCrontab() + " verify failure");

            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleParam.getCrontab());
            return result;
        }
        scheduleObj.setCrontab(scheduleParam.getCrontab());
        scheduleObj.setWarningType(warningType);
        scheduleObj.setWarningGroupId(warningGroupId);
        scheduleObj.setFailureStrategy(failureStrategy);
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setUserName(loginUser.getUserName());
        scheduleObj.setReleaseState(ReleaseState.OFFLINE);
        scheduleObj.setProcessInstancePriority(processInstancePriority);
        scheduleObj.setWorkerGroupId(workerGroupId);
        scheduleMapper.insert(scheduleObj);

        /**
         * updateProcessInstance receivers and cc by process definition id
         */
        processDefinitionMapper.updateReceiversAndCcById(receivers, receiversCc, processDefineId);
        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * updateProcessInstance schedule
     *
     * @param loginUser
     * @param projectName
     * @param id
     * @param scheduleExpression
     * @param warningType
     * @param warningGroupId
     * @param failureStrategy
     * @param scheduleStatus
     * @param workerGroupId
     * @return
     */
    @Transactional(value = "TransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> updateSchedule(User loginUser, String projectName, Integer id, String scheduleExpression, WarningType warningType,
                                              int warningGroupId, FailureStrategy failureStrategy,
                                              String receivers, String receiversCc, ReleaseState scheduleStatus,
                                              Priority processInstancePriority, int workerGroupId) throws IOException {
        Map<String, Object> result = new HashMap<String, Object>(5);

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        Map<String, Object> checkResult = checkAuth(loginUser, projectName, project);
        if (checkResult != null) {
            return checkResult;
        }

        // check schedule exists
        Schedule schedule = scheduleMapper.queryById(id);

        if (schedule == null) {
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, id);
            return result;
        }

        ProcessDefinition processDefinition = processDao.findProcessDefineById(schedule.getProcessDefinitionId());
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, schedule.getProcessDefinitionId());
            return result;
        }

        /**
         * scheduling on-line status forbid modification
         */
        if (checkValid(result, schedule.getReleaseState() == ReleaseState.ONLINE, Status.SCHEDULE_CRON_ONLINE_FORBID_UPDATE)) {
            return result;
        }

        Date now = new Date();

        // updateProcessInstance param
        if (StringUtils.isNotEmpty(scheduleExpression)) {
            ScheduleParam scheduleParam = JSONUtils.parseObject(scheduleExpression, ScheduleParam.class);
            if (DateUtils.differSec(scheduleParam.getStartTime(),scheduleParam.getEndTime()) == 0) {
                logger.warn("The start time must not be the same as the end");
                putMsg(result,Status.SCHEDULE_START_TIME_END_TIME_SAME);
                return result;
            }
            schedule.setStartTime(scheduleParam.getStartTime());
            schedule.setEndTime(scheduleParam.getEndTime());
            if (!org.quartz.CronExpression.isValidExpression(scheduleParam.getCrontab())) {
                putMsg(result, Status.SCHEDULE_CRON_CHECK_FAILED, scheduleParam.getCrontab());
                return result;
            }
            schedule.setCrontab(scheduleParam.getCrontab());
        }

        if (warningType != null) {
            schedule.setWarningType(warningType);
        }

        schedule.setWarningGroupId(warningGroupId);

        if (failureStrategy != null) {
            schedule.setFailureStrategy(failureStrategy);
        }

        if (scheduleStatus != null) {
            schedule.setReleaseState(scheduleStatus);
        }
        schedule.setWorkerGroupId(workerGroupId);
        schedule.setUpdateTime(now);
        schedule.setProcessInstancePriority(processInstancePriority);
        scheduleMapper.update(schedule);

        /**
         * updateProcessInstance recipients and cc by process definition ID
         */
        processDefinitionMapper.updateReceiversAndCcById(receivers, receiversCc, schedule.getProcessDefinitionId());

        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * set schedule online or offline
     *
     * @param loginUser
     * @param projectName
     * @param id
     * @param scheduleStatus
     * @return
     */
    @Transactional(value = "TransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> setScheduleState(User loginUser, String projectName, Integer id, ReleaseState scheduleStatus) {

        Map<String, Object> result = new HashMap<String, Object>(5);

        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = checkAuth(loginUser, projectName, project);
        if (checkResult != null) {
            return checkResult;
        }

        // check schedule exists
        Schedule scheduleObj = scheduleMapper.queryById(id);

        if (scheduleObj == null) {
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, id);
            return result;
        }
        // check schedule release state
        if(scheduleObj.getReleaseState() == scheduleStatus){
            logger.info("schedule release is already {},needn't to change schedule id: {} from {} to {}",
                    scheduleObj.getReleaseState(), scheduleObj.getId(), scheduleObj.getReleaseState(), scheduleStatus);
            putMsg(result, Status.SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE, scheduleStatus);
            return result;
        }
        ProcessDefinition processDefinition = processDao.findProcessDefineById(scheduleObj.getProcessDefinitionId());
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, scheduleObj.getProcessDefinitionId());
            return result;
        }

        if(scheduleStatus == ReleaseState.ONLINE){
            // check process definition release state
            if(processDefinition.getReleaseState() != ReleaseState.ONLINE){
                logger.info("not release process definition id: {} , name : {}",
                        processDefinition.getId(), processDefinition.getName());
                putMsg(result, Status.PROCESS_DEFINE_NOT_RELEASE, scheduleObj.getProcessDefinitionId());
                return result;
            }
            // check sub process definition release state
            List<String> subProcessDefineIds = new ArrayList<>();
            processDao.recurseFindSubProcessId(scheduleObj.getProcessDefinitionId(), subProcessDefineIds);
            if (subProcessDefineIds.size() > 0){
                List<ProcessDefinition> subProcessDefinitionList = processDefinitionMapper.queryDefinitionListByIdList(subProcessDefineIds);
                if (subProcessDefinitionList != null && subProcessDefinitionList.size() > 0){
                    for (ProcessDefinition subProcessDefinition : subProcessDefinitionList){
                        /**
                         * if there is no online process, exit directly
                         */
                        if (subProcessDefinition.getReleaseState() != ReleaseState.ONLINE){
                            logger.info("not release process definition id: {} , name : {}",
                                    subProcessDefinition.getId(), subProcessDefinition.getName());
                            putMsg(result, Status.PROCESS_DEFINE_NOT_RELEASE, subProcessDefinition.getId());
                            return result;
                        }
                    }
                }
            }
        }

        // check master server exists
        List<MasterServer> masterServers = masterServerMapper.queryAllMaster();

        if (masterServers.size() == 0) {
            putMsg(result, Status.MASTER_NOT_EXISTS);
        }

        // set status
        scheduleObj.setReleaseState(scheduleStatus);

        scheduleMapper.update(scheduleObj);

        try {
            switch (scheduleStatus) {
                case ONLINE: {
                    logger.info("Call master client set schedule online, project id: {}, flow id: {},host: {}, port: {}", project.getId(), processDefinition.getId(), masterServers);
                    setSchedule(project.getId(), id);
                    break;
                }
                case OFFLINE: {
                    logger.info("Call master client set schedule offline, project id: {}, flow id: {},host: {}, port: {}", project.getId(), processDefinition.getId(), masterServers);
                    deleteSchedule(project.getId(), id);
                    break;
                }
                default: {
                    putMsg(result, Status.SCHEDULE_STATUS_UNKNOWN, scheduleStatus.toString());
                    return result;
                }
            }
        } catch (Exception e) {
            result.put(Constants.MSG, scheduleStatus == ReleaseState.ONLINE ? "set online failure" : "set offline failure");
            throw new RuntimeException(result.get(Constants.MSG).toString());
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }



    /**
     * query schedule
     *
     * @param loginUser
     * @param projectName
     * @param processDefineId
     * @return
     */
    public Map<String, Object> querySchedule(User loginUser, String projectName, Integer processDefineId, String searchVal, Integer pageNo, Integer pageSize) {

        HashMap<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        Map<String, Object> checkResult = checkAuth(loginUser, projectName, project);
        if (checkResult != null) {
            return checkResult;
        }

        ProcessDefinition processDefinition = processDao.findProcessDefineById(processDefineId);
        if (processDefinition == null) {
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processDefineId);
            return result;
        }

        Integer count = scheduleMapper.countByProcessDefineId(processDefineId, searchVal);

        PageInfo pageInfo = new PageInfo<Schedule>(pageNo, pageSize);

        List<Schedule> scheduleList = scheduleMapper.queryByProcessDefineIdPaging(processDefinition.getId(), searchVal, pageInfo.getStart(), pageSize);

        pageInfo.setTotalCount(count);
        pageInfo.setLists(scheduleList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query schedule list
     *
     * @param loginUser
     * @param projectName
     * @return
     */
    public Map<String, Object> queryScheduleList(User loginUser, String projectName) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        // check project auth
        Map<String, Object> checkResult = checkAuth(loginUser, projectName, project);
        if (checkResult != null) {
            return checkResult;
        }

        List<Schedule> schedules = scheduleMapper.querySchedulerListByProjectName(projectName);

        result.put(Constants.DATA_LIST, schedules);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * set schedule
     *
     * @see
     */
    public void setSchedule(int projectId, int scheduleId) throws RuntimeException{
        logger.info("set schedule, project id: {}, scheduleId: {}", projectId, scheduleId);


        Schedule schedule = processDao.querySchedule(scheduleId);
        if (schedule == null) {
            logger.warn("process schedule info not exists");
        }

        Date startDate = schedule.getStartTime();
        Date endDate = schedule.getEndTime();

        String jobName = QuartzExecutors.buildJobName(scheduleId);
        String jobGroupName = QuartzExecutors.buildJobGroupName(projectId);

        Map<String, Object> dataMap = QuartzExecutors.buildDataMap(projectId, scheduleId, schedule);

        QuartzExecutors.getInstance().addJob(ProcessScheduleJob.class, jobName, jobGroupName, startDate, endDate,
                schedule.getCrontab(), dataMap);

    }

    /**
     * delete schedule
     */
    public static void deleteSchedule(int projectId, int scheduleId) throws RuntimeException{
        logger.info("delete schedules of project id:{}, schedule id:{}", projectId, scheduleId);

        String jobName = QuartzExecutors.buildJobName(scheduleId);
        String jobGroupName = QuartzExecutors.buildJobGroupName(projectId);

        if(!QuartzExecutors.getInstance().deleteJob(jobName, jobGroupName)){
            logger.warn("set offline failure:projectId:{},scheduleId:{}",projectId,scheduleId);
            throw new RuntimeException(String.format("set offline failure"));
        }

    }

    /**
     * check valid
     *
     * @param result
     * @param bool
     * @param status
     * @return
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
     *
     * @param loginUser
     * @param projectName
     * @param project
     * @return
     */
    private Map<String, Object> checkAuth(User loginUser, String projectName, Project project) {
        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        return null;
    }

    /**
     * delete schedule by id
     *
     * @param loginUser
     * @param projectName
     * @param scheduleId
     * @return
     */
    public Map<String, Object> deleteScheduleById(User loginUser, String projectName, Integer scheduleId) {

        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }

        Schedule schedule = scheduleMapper.queryById(scheduleId);

        if (schedule == null) {
            putMsg(result, Status.SCHEDULE_CRON_NOT_EXISTS, scheduleId);
            return result;
        }

        // Determine if the login user is the owner of the schedule
        if (loginUser.getId() != schedule.getUserId()) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // check schedule is already online
        if(schedule.getReleaseState() == ReleaseState.ONLINE){
            putMsg(result, Status.SCHEDULE_CRON_STATE_ONLINE,schedule.getId());
            return result;
        }


        int delete = scheduleMapper.delete(scheduleId);

        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_SCHEDULE_CRON_BY_ID_ERROR);
        }
        return result;
    }

    /**
     * preview schedule
     * @param loginUser
     * @param projectName
     * @param schedule
     * @return
     */
    public Map<String,Object> previewSchedule(User loginUser, String projectName, String schedule) {
        Map<String, Object> result = new HashMap<>(5);
        CronExpression cronExpression;
        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);
        Date now = new Date();

        Date startTime = now.after(scheduleParam.getStartTime()) ? now : scheduleParam.getStartTime();
        Date endTime = scheduleParam.getEndTime();
        try {
            cronExpression = CronUtils.parse2CronExpression(scheduleParam.getCrontab());
        } catch (ParseException e) {
            logger.error(e.getMessage(),e);
            putMsg(result,Status.PARSE_TO_CRON_EXPRESSION_ERROR);
            return result;
        }
        List<Date> selfFireDateList = CronUtils.getSelfFireDateList(startTime, endTime,cronExpression);
        result.put(Constants.DATA_LIST, selfFireDateList.stream().map(t -> DateUtils.dateToString(t)).limit(cn.escheduler.common.Constants.PREVIEW_SCHEDULE_EXECUTE_COUNT));
        putMsg(result, Status.SUCCESS);
        return result;
    }
}