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
package org.apache.dolphinscheduler.api.controller;


import java.util.Map;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SchedulerCalendarService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;


/**
 * calendar controller
 */
@Api(tags = "CALENDAR_TAG", position = 1)
@RestController
@RequestMapping("/calendar")
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class SchedulerCalendarController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(SchedulerCalendarController.class);


    @Autowired
    private SchedulerCalendarService schedulerCalendarService;

    @ApiOperation(value = "createCalendar", notes= "CREATE_CALENDAR_NOTES")
    @ApiImplicitParams({

            @ApiImplicitParam(name = "name", value = "CALENDAR_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "calendarInfo", value = "CALENDARINFO", required = true, dataType ="String",example =
                    "{'startTime' : '2019-06-10 00:00:00','endTime' : '2019-06-13 00:00:00','extTime' : ['20190102','20190103','20190103']}"),
            @ApiImplicitParam(name = "description", value = "CALENDAR_DESC", dataType ="String")

    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createCalendar(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestParam(value = "name") String name,
                                                       @RequestParam(value = "calendarInfo") String calendarInfo,
                                                       @RequestParam(value = "description",required = false) String desc) {
        try {

            Map<String, Object> result = schedulerCalendarService.createCalendar(loginUser,name,calendarInfo,desc);
            return returnDataList(result);

        }catch (Exception e){
            logger.error(Status.CREATE_CALENDAR_ERROR.getMsg(),e);
            return error(Status.CREATE_CALENDAR_ERROR.getCode(), Status.CREATE_CALENDAR_ERROR.getMsg());
        }
    }


    /**
     * query calendar list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return calendar list page
     */
    @ApiOperation(value = "queryCalendarlistPaging", notes= "QUERY_CALENDAR_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType ="Int",example = "20")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryCalendarlistPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam("pageNo") Integer pageNo,
                                                     @RequestParam(value = "searchVal", required = false) String searchVal,
                                                     @RequestParam("pageSize") Integer pageSize){
        logger.info("login user {}, list paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(),pageNo,searchVal,pageSize);
        try{
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = schedulerCalendarService.queryCalendarList(loginUser, searchVal, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(Status.QUERY_CALENDAR_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_CALENDAR_LIST_PAGING_ERROR.getCode(), Status.QUERY_CALENDAR_LIST_PAGING_ERROR.getMsg());
        }
    }


    /**
     * calendar list
     *
     * @param loginUser login user
     * @return calendar list
     */
    @ApiOperation(value = "queryCalendarlist", notes= "QUERY_CALENDAR_LIST_NOTES")
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryCalendarlist(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser){
        logger.info("login user {}, query calendar list", loginUser.getUserName());
        try{
            Map<String, Object> result = schedulerCalendarService.queryCalendarList(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.QUERY_CALENDAR_LIST_ERROR.getMsg(),e);
            return error(Status.QUERY_CALENDAR_LIST_ERROR.getCode(), Status.QUERY_CALENDAR_LIST_ERROR.getMsg());
        }
    }



    /**
     * udpate calendar
     *
     * @param loginUser login user
     * @param id
     * @param name
     * @param calendarInfo
     * @param description description
     * @return update result code
     */
    @ApiOperation(value = "updateCalendar", notes= "UPDATE_CALENDAR_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "CALENDAR_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "name", value = "CALENDAR_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "calendarInfo", value = "CALENDARINFO", required = true, dataType ="String",example =
                    "{'startTime' : '2019-06-10 00:00:00','endTime' : '2019-06-13 00:00:00','extTime' : ['20190102','20190103','20190103']}"),
            @ApiImplicitParam(name = "description", value = "CALENDAR_DESC", dataType ="String")

    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateCalendar(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestParam(value = "id") int id,
                                                       @RequestParam(value = "name") String name,
                                                       @RequestParam(value = "calendarInfo") String calendarInfo,
                                                       @RequestParam(value = "description",required = false) String description) {

        try {
            Map<String, Object> result = schedulerCalendarService.updateCalendar(loginUser,id,name, calendarInfo, description);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.UPDATE_CALENDAR_ERROR.getMsg(),e);
            return error(Status.UPDATE_CALENDAR_ERROR.getCode(), Status.UPDATE_CALENDAR_ERROR.getMsg());
        }
    }

    /**
     * select calendar by id
     *
     * @param loginUser login user
     * @param id calendar id
     * @return delete result code
     */
    @ApiOperation(value = "selectCalendarById", notes= "GET_CALENDAR_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "CALENDAR_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/selectCalendarById")
    @ResponseStatus(HttpStatus.OK)
    public Result selectCalendarById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete calendar, calendarId: {},", loginUser.getUserName(), id);
        try {
            Map<String, Object> result = schedulerCalendarService.selectById(loginUser,id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.DELETE_CALENDAR_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_CALENDAR_BY_ID_ERROR.getCode(), Status.DELETE_CALENDAR_BY_ID_ERROR.getMsg());
        }
    }




    /**
     * delete calendar by id
     *
     * @param loginUser login user
     * @param id calendar id
     * @return delete result code
     */
    @ApiOperation(value = "deleteCalendarById", notes= "DELETE_CALENDAR_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "CALENDAR_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteCalendarById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete calendar, calendarId: {},", loginUser.getUserName(), id);
        try {
            Map<String, Object> result = schedulerCalendarService.deleteCalendarById(loginUser,id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.DELETE_CALENDAR_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_CALENDAR_BY_ID_ERROR.getCode(), Status.DELETE_CALENDAR_BY_ID_ERROR.getMsg());
        }
    }


    /**
     * verify calendar code
     *
     * @param loginUser login user
     * @param calendarCode calendar code
     * @return true if calendar code can user, otherwise return false
     */
    @ApiOperation(value = "verifyCalendarCode", notes= "VERIFY_CALENDAR_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "calendarCode", value = "CALENDAR_CODE", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-calendar-code")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyCalendarCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value ="calendarCode") String calendarCode
    ) {

        try{
            logger.info("login user {}, verfiy calendar code: {}",
                    loginUser.getUserName(),calendarCode);
            return schedulerCalendarService.verifyCalendarName(calendarCode);
        }catch (Exception e){
            logger.error(Status.VERIFY_CALENDAR_NAME_ERROR.getMsg(),e);
            return error(Status.VERIFY_CALENDAR_NAME_ERROR.getCode(), Status.VERIFY_CALENDAR_NAME_ERROR.getMsg());
        }
    }


    /**
     * release calendar
     *
     * @param loginUser login user
     * @param id calendar id
     * @param releaseState release state
     * @return release result code
     */
    @ApiOperation(value = "releaseCalendar", notes= "RELEASE_CALENDAR_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "releaseState", value = "CALENDAR_CONNECTS", required = true, dataType = "Int", example = "100"),
    })
    @PostMapping(value = "/release")
    @ResponseStatus(HttpStatus.OK)
    public Result releaseCalendar(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "id", required = true) int id,
            @RequestParam(value = "releaseState", required = true) int releaseState) {

        try {

            Map<String, Object> result = schedulerCalendarService.releaseCalendar(loginUser, id, releaseState);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.RELEASE_CALENDAR_ERROR.getMsg(),e);
            return error(Status.RELEASE_CALENDAR_ERROR.getCode(), Status.RELEASE_CALENDAR_ERROR.getMsg());
        }
    }

}
