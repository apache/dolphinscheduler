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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.api.dto.CalendarParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.dolphinscheduler.dao.entity.Calendar;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CalendarMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * Calendar service
 */
@Service
public class CalendarService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

  @Autowired
  private CalendarMapper calendarMapper;

  @Autowired
  private UserMapper userMapper;


  /**
   *
   * @param loginUser
   * @param name
   * @param calendarInfo
   * @param desc
   * @return
   * @throws Exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String,Object> createCalendar(User loginUser,
                         String name,
                         String calendarInfo,
                         String desc) throws Exception {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);




    CalendarParam calendarParam = JSONUtils.parseObject(calendarInfo, CalendarParam.class);


    if (DateUtils.differSec(calendarParam.getStartTime(),calendarParam.getEndTime()) == 0) {
      logger.warn("The start time must not be the same as the end");
      putMsg(result,Status.SCHEDULE_START_TIME_END_TIME_SAME);
      return result;
    }


    if (checkCalendarExists(name)){
      putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, name);
      return result;
    }

    Calendar calendar = new Calendar();
    Date now = new Date();

    calendar.setName(name);
    calendar.setStartTime(calendarParam.getStartTime());
    calendar.setEndTime(calendarParam.getEndTime());
    calendar.setFlag(Flag.NO);

    calendar.setUserId(loginUser.getId());
    calendar.setDescription(desc);
    calendar.setCreateTime(now);
    calendar.setUpdateTime(now);

    // save
    calendarMapper.insert(calendar);


    // todo deal detail info
   doDetailsInfo(calendar,calendarParam);

    putMsg(result, Status.SUCCESS);

    return result;
}



  /**
   * query Calendar list paging
   *
   * @param loginUser login user
   * @param searchVal search value
   * @param pageNo page number
   * @param pageSize page size
   * @return Calendar list page
   */
  public Map<String,Object> queryCalendarList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

    Map<String, Object> result = new HashMap<>(5);
    if (checkAdmin(loginUser, result)) {
      return result;
    }

    Page<Calendar> page = new Page(pageNo, pageSize);
    IPage<Calendar> calendarIPage = calendarMapper.queryCalendarPaging(page, searchVal);
    PageInfo<Calendar> pageInfo = new PageInfo<>(pageNo, pageSize);
    pageInfo.setTotalCount((int)calendarIPage.getTotal());
    pageInfo.setLists(calendarIPage.getRecords());
    result.put(Constants.DATA_LIST, pageInfo);

    putMsg(result, Status.SUCCESS);

    return result;
  }

  /**
   *
   * @param loginUser
   * @param id
   * @param name
   * @param desc
   * @return
   * @throws Exception
   */
  public Map<String, Object>  updateCalendar(User loginUser,
          int id,
          String name,
          String calendarInfo,
          String desc) throws Exception {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);

    CalendarParam calendarParam = JSONUtils.parseObject(calendarInfo, CalendarParam.class);


    if (DateUtils.differSec(calendarParam.getStartTime(),calendarParam.getEndTime()) == 0) {
      logger.warn("The start time must not be the same as the end");
      putMsg(result,Status.SCHEDULE_START_TIME_END_TIME_SAME);
      return result;
    }



    Calendar calendar = calendarMapper.selectById(id);

    if (calendar == null){
      putMsg(result, Status.CALENDAR_NOT_EXIST);
      return result;
    }

    // update Calendar Instance

    Date now = new Date();

    if (StringUtils.isNotEmpty(name)){
      calendar.setName(name);
    }

    if (null != calendarParam.getStartTime() ){
      calendar.setStartTime(calendarParam.getStartTime());
    }

    if (null != calendarParam.getEndTime() ){
      calendar.setEndTime(calendarParam.getEndTime());
    }

    calendar.setFlag(Flag.NO);

    calendar.setUserId(loginUser.getId());
    calendar.setDescription(desc);

    calendar.setUpdateTime(now);


    calendarMapper.updateById(calendar);


    doDetailsInfo(calendar,calendarParam);



    result.put(Constants.STATUS, Status.SUCCESS);
    result.put(Constants.MSG, Status.SUCCESS.getMsg());
    return result;

  }

  /**
   * delete Calendar
   *
   * @param loginUser login user
   * @param id Calendar id
   * @return delete result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> deleteCalendarById(User loginUser, int id) throws Exception {
    Map<String, Object> result = new HashMap<>(5);


    Calendar calendar = calendarMapper.selectById(id);
    if (calendar == null){
      putMsg(result, Status.CALENDAR_NOT_EXIST);
      return result;
    }

    //todo 这里需要处理任务&实例的依赖关系



    calendarMapper.deleteById(id);

    putMsg(result, Status.SUCCESS);
    return result;
  }


  /**
   * query Calendar list
   *
   * @param loginUser login user
   * @return Calendar list
   */
  public Map<String, Object> queryCalendarList(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);

    List<Calendar> resourceList = calendarMapper.selectList(null);
    result.put(Constants.DATA_LIST, resourceList);
    putMsg(result, Status.SUCCESS);
    
    return result;
  }

  /**
   * query Calendar list via Calendar name
   * @param name
   * @return Calendar list
   */
  public Map<String, Object> queryCalendarList(String name) {
    Map<String, Object> result = new HashMap<>(5);

    List<Calendar> resourceList = calendarMapper.queryByCalendarName(name);
    if (CollectionUtils.isNotEmpty(resourceList)) {
      result.put(Constants.DATA_LIST, resourceList);
      putMsg(result, Status.SUCCESS);
    } else {
      putMsg(result, Status.CALENDAR_NOT_EXIST);
    }

    return result;
  }

  /**
   * verify Calendar name
   *
   * @param CalendarCode Calendar name
   * @return true if Calendar name can user, otherwise return false
   */
  public Result verifyCalendarName(String CalendarCode) {
    Result result=new Result();
    if (checkCalendarExists(CalendarCode)) {
      logger.error("Calendar {} has exist, can't create again.", CalendarCode);
      putMsg(result, Status.CALENDAR_NAME_EXIST);
    }else{
      putMsg(result, Status.SUCCESS);
    }
    return result;
  }


  /**
   * check Calendar exists
   *
   * @param   name
   * @return ture if the Calendar name exists, otherwise return false
   */
  private boolean checkCalendarExists(String name) {
      List<Calendar> Calendars = calendarMapper.queryByCalendarName(name);
      return CollectionUtils.isNotEmpty(Calendars);
  }


  private void doDetailsInfo(Calendar calendar, CalendarParam calendarParam) {



  }




}
