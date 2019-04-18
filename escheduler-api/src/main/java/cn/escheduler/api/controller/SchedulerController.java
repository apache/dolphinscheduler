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
package cn.escheduler.api.controller;


import cn.escheduler.api.enums.Status;
import cn.escheduler.api.service.SchedulerService;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.FailureStrategy;
import cn.escheduler.common.enums.Priority;
import cn.escheduler.common.enums.ReleaseState;
import cn.escheduler.common.enums.WarningType;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.*;
import static cn.escheduler.api.utils.Constants.SESSION_USER;

/**
 * schedule controller
 */
@RestController
@RequestMapping("/projects/{projectName}/schedule")
public class SchedulerController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);
    public static final String DEFAULT_WARNING_TYPE = "NONE";
    public static final String DEFAULT_NOTIFY_GROUP_ID = "1";
    public static final String DEFAULT_FAILURE_POLICY = "CONTINUE";


    @Autowired
    private SchedulerService schedulerService;


    /**
     * create schedule
     *
     * @param loginUser
     * @param projectName
     * @param processDefinitionId
     * @param schedule
     * @param warningType
     * @param warningGroupId
     * @param failureStrategy
     * @return
     */
  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  public Result createSchedule(@RequestAttribute(value = SESSION_USER) User loginUser,
                                                       @PathVariable String projectName,
                                                       @RequestParam(value = "processDefinitionId") Integer processDefinitionId,
                                                       @RequestParam(value = "schedule") String schedule,
                                                       @RequestParam(value = "warningType", required = false,defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                                       @RequestParam(value = "warningGroupId", required = false,defaultValue = DEFAULT_NOTIFY_GROUP_ID) int warningGroupId,
                                                       @RequestParam(value = "failureStrategy", required = false, defaultValue = DEFAULT_FAILURE_POLICY) FailureStrategy failureStrategy,
                                                       @RequestParam(value = "receivers", required = false) String receivers,
                                                       @RequestParam(value = "receiversCc", required = false) String receiversCc,
                                                       @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority) {
    logger.info("login user {}, project name: {}, process name: {}, create schedule: {}, warning type: {}, warning group id: {}," +
                    "failure policy: {},receivers : {},receiversCc : {},processInstancePriority : {}",
            loginUser.getUserName(), projectName, processDefinitionId, schedule, warningType, warningGroupId, failureStrategy,receivers,receiversCc,processInstancePriority);
      try {
          Map<String, Object> result = schedulerService.insertSchedule(loginUser, projectName, processDefinitionId, schedule,
                  warningType, warningGroupId, failureStrategy, receivers,receiversCc,processInstancePriority);

          return returnDataList(result);
      }catch (Exception e){
          logger.error(CREATE_SCHEDULE_ERROR.getMsg(),e);
          return error(CREATE_SCHEDULE_ERROR.getCode(), CREATE_SCHEDULE_ERROR.getMsg());
      }
  }

    /**
     * updateProcessInstance schedule
     *
     * @param loginUser
     * @param projectName
     * @param id
     * @param schedule
     * @param warningType
     * @param warningGroupId
     * @param failureStrategy
     * @return
     */
  @PostMapping("/update")
  public Result updateSchedule(@RequestAttribute(value = SESSION_USER) User loginUser,
                                                       @PathVariable String projectName,
                                                       @RequestParam(value = "id") Integer id,
                                                       @RequestParam(value = "schedule") String schedule,
                                                       @RequestParam(value = "warningType", required = false, defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                                       @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
                                                       @RequestParam(value = "failureStrategy", required = false, defaultValue = "END") FailureStrategy failureStrategy,
                                                       @RequestParam(value = "receivers", required = false) String receivers,
                                                       @RequestParam(value = "receiversCc", required = false) String receiversCc,
                                                       @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority) {
    logger.info("login user {}, project name: {},id: {}, updateProcessInstance schedule: {}, notify type: {}, notify mails: {}, " +
                    "failure policy: {},receivers : {},receiversCc : {},processInstancePriority : {}",
            loginUser.getUserName(), projectName, id, schedule, warningType, warningGroupId, failureStrategy,receivers,receiversCc,processInstancePriority);

      try {
          Map<String, Object> result = schedulerService.updateSchedule(loginUser, projectName, id, schedule,
                  warningType, warningGroupId, failureStrategy, receivers,receiversCc,null,processInstancePriority);
          return returnDataList(result);

      }catch (Exception e){
          logger.error(UPDATE_SCHEDULE_ERROR.getMsg(),e);
          return error(Status.UPDATE_SCHEDULE_ERROR.getCode(), Status.UPDATE_SCHEDULE_ERROR.getMsg());
      }
  }

    /**
     * publish schedule setScheduleState
     *
     * @param loginUser
     * @param projectName
     * @param id
     * @return
     * @throws Exception
     */
  @PostMapping("/online")
  public Result online(@RequestAttribute(value = SESSION_USER) User loginUser,
                                               @PathVariable("projectName") String projectName,
                                               @RequestParam("id") Integer id)  {
    logger.info("login user {}, schedule setScheduleState, project name: {}, id: {}",
            loginUser.getUserName(), projectName, id);
      try {
          Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectName, id, ReleaseState.ONLINE);
          return returnDataList(result);

      }catch (Exception e){
          logger.error(PUBLISH_SCHEDULE_ONLINE_ERROR.getMsg(),e);
          return error(Status.PUBLISH_SCHEDULE_ONLINE_ERROR.getCode(), Status.PUBLISH_SCHEDULE_ONLINE_ERROR.getMsg());
      }
  }

    /**
     * offline schedule
     *
     * @param loginUser
     * @param projectName
     * @param id
     * @return
     */
  @PostMapping("/offline")
  public Result offline(@RequestAttribute(value = SESSION_USER) User loginUser,
                                                @PathVariable("projectName") String projectName,
                                                @RequestParam("id") Integer id)  {
    logger.info("login user {}, schedule offline, project name: {}, process definition id: {}",
            loginUser.getUserName(), projectName, id);

      try {
          Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectName, id, ReleaseState.OFFLINE);
          return returnDataList(result);

      }catch (Exception e){
          logger.error(OFFLINE_SCHEDULE_ERROR.getMsg(),e);
          return error(Status.OFFLINE_SCHEDULE_ERROR.getCode(), Status.OFFLINE_SCHEDULE_ERROR.getMsg());
      }
  }

    /**
     * query schedule list paging
     *
     * @param loginUser
     * @param projectName
     * @param processDefinitionId
     * @return
     */
  @GetMapping("/list-paging")
  public Result querySchedule(@RequestAttribute(value = SESSION_USER) User loginUser,
                                                      @PathVariable String projectName,
                                                      @RequestParam Integer processDefinitionId,
                                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                                      @RequestParam("pageNo") Integer pageNo,
                                                      @RequestParam("pageSize") Integer pageSize) {
    logger.info("login user {}, query schedule, project name: {}, process definition id: {}",
            loginUser.getUserName(), projectName, processDefinitionId);
      try {
          Map<String, Object> result = schedulerService.querySchedule(loginUser, projectName, processDefinitionId, searchVal, pageNo, pageSize);
          return returnDataListPaging(result);
       }catch (Exception e){
          logger.error(QUERY_SCHEDULE_LIST_PAGING_ERROR.getMsg(),e);
          return error(Status.QUERY_SCHEDULE_LIST_PAGING_ERROR.getCode(), Status.QUERY_SCHEDULE_LIST_PAGING_ERROR.getMsg());
      }

  }

  /**
   * query schedule list
   *
   * @param loginUser
   * @param projectName
   * @return
   */
  @PostMapping("/list")
  public Result queryScheduleList(@RequestAttribute(value = SESSION_USER) User loginUser,
                                  @PathVariable String projectName) {
      try{
          logger.info("login user {}, query schedule list, project name: {}",
                  loginUser.getUserName(), projectName);
          Map<String, Object> result = schedulerService.queryScheduleList(loginUser, projectName);
          return returnDataList(result);
      }catch (Exception e){
          logger.error(QUERY_SCHEDULE_LIST_ERROR.getMsg(),e);
          return error(Status.QUERY_SCHEDULE_LIST_ERROR.getCode(), Status.QUERY_SCHEDULE_LIST_ERROR.getMsg());
      }
  }
}
