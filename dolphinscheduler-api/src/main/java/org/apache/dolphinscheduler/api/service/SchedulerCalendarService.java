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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.dolphinscheduler.api.dto.CalendarParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.dolphinscheduler.dao.entity.SchedulerCalendar;
import org.apache.dolphinscheduler.dao.entity.SchedulerCalendarDetails;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.SchedulerCalendarDetailsMapper;
import org.apache.dolphinscheduler.dao.mapper.SchedulerCalendarMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import  com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;
/**
 * Calendar service
 */
@Service
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class SchedulerCalendarService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(SchedulerCalendarService.class);

  @Autowired
  private SchedulerCalendarMapper schedulerCalendarMapper;
  @Autowired
  private SchedulerCalendarDetailsMapper schedulerCalendarDetailsMapper;

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
                         String desc)  {

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

    SchedulerCalendar calendar = new SchedulerCalendar();
    Date now = new Date();

    calendar.setName(name);
    calendar.setStartTime(calendarParam.getStartTime());
    calendar.setEndTime(calendarParam.getEndTime());
    calendar.setReleaseState(ReleaseState.OFFLINE);

    calendar.setUserId(loginUser.getId());
    calendar.setDescription(desc);
    calendar.setCreateTime(now);
    calendar.setUpdateTime(now);




   boolean detailStatus = doDetailsInfo(calendar,calendarParam);

   if( detailStatus ){

     // save
     schedulerCalendarMapper.insert(calendar);

     putMsg(result, Status.SUCCESS);
     result.put(Constants.MSG, Status.SUCCESS.getMsg());
   }else{
     putMsg(result, Status.UPDATE_CALENDAR_DETAILS_ERROR);
     result.put(Constants.MSG, Status.UPDATE_CALENDAR_DETAILS_ERROR.getMsg());
   }

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

    Page<SchedulerCalendar> page = new Page(pageNo, pageSize);
    IPage<SchedulerCalendar> calendarIPage = schedulerCalendarMapper.queryCalendarPaging(page, searchVal);
    PageInfo<SchedulerCalendar> pageInfo = new PageInfo<>(pageNo, pageSize);
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
          String desc)  {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);

    CalendarParam calendarParam = JSONUtils.parseObject(calendarInfo, CalendarParam.class);


    if (DateUtils.differSec(calendarParam.getStartTime(),calendarParam.getEndTime()) == 0) {
      logger.warn("The start time must not be the same as the end");
      putMsg(result,Status.SCHEDULE_START_TIME_END_TIME_SAME);
      return result;
    }



    SchedulerCalendar calendar = schedulerCalendarMapper.selectById(id);

    if (calendar == null){
      putMsg(result, Status.CALENDAR_NOT_EXIST);
      return result;
    }



    // todo deal detail info
    boolean detailStatus = doDetailsInfo(calendar,calendarParam);

    if( detailStatus ){

      // update SchedulerCalendar Instance
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

      calendar.setReleaseState(ReleaseState.OFFLINE);

      calendar.setUserId(loginUser.getId());
      calendar.setDescription(desc);

      calendar.setUpdateTime(now);

      schedulerCalendarMapper.updateById(calendar);


      putMsg(result, Status.SUCCESS);
      result.put(Constants.MSG, Status.SUCCESS.getMsg());
    }else{
      putMsg(result, Status.UPDATE_CALENDAR_DETAILS_ERROR);
      result.put(Constants.MSG, Status.UPDATE_CALENDAR_DETAILS_ERROR.getMsg());
    }

    return result;

  }

  /**
   * delete SchedulerCalendar
   *
   * @param loginUser login user
   * @param id SchedulerCalendar id
   * @return delete result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> deleteCalendarById(User loginUser, int id) throws Exception {
    Map<String, Object> result = new HashMap<>(5);


    SchedulerCalendar calendar = schedulerCalendarMapper.selectById(id);
    if (calendar == null){
      putMsg(result, Status.CALENDAR_NOT_EXIST);
      return result;
    }


    schedulerCalendarMapper.deleteById(id);
    schedulerCalendarDetailsMapper.clearByCalendarId(id);
    //todo 这里需要处理任务&实例的依赖关系


    putMsg(result, Status.SUCCESS);
    return result;
  }


  /**
   * select SchedulerCalendar
   *
   * @param loginUser login user
   * @param id SchedulerCalendar id
   * @return delete result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> selectById(User loginUser, int id) throws Exception {
    Map<String, Object> result = new HashMap<>(5);


    SchedulerCalendar calendar = schedulerCalendarMapper.selectById(id);

    if (calendar == null){
      putMsg(result, Status.CALENDAR_NOT_EXIST);
      return result;
    }

    List<SchedulerCalendarDetails> detailsList =  schedulerCalendarDetailsMapper.queryByCalendarId(id);
    calendar.setDetailsList(detailsList);

    putMsg(result, Status.SUCCESS);
    return result;
  }



  /**
   * query SchedulerCalendar list
   *
   * @param loginUser login user
   * @return SchedulerCalendar list
   */
  public Map<String, Object> querySchedulerCalendarList(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);

    List<SchedulerCalendar> resourceList = schedulerCalendarMapper.selectList(null);
    result.put(Constants.DATA_LIST, resourceList);
    putMsg(result, Status.SUCCESS);
    
    return result;
  }

  /**
   *
   * @param loginUser
   * @return
   */
  public Map<String, Object> queryCalendarList(User loginUser) {
    Map<String, Object> result = new HashMap<>(5);

    List<SchedulerCalendar> resourceList = schedulerCalendarMapper.queryByCalendarName(null);
    if (CollectionUtils.isNotEmpty(resourceList)) {
      result.put(Constants.DATA_LIST, resourceList);
      putMsg(result, Status.SUCCESS);
    } else {
      putMsg(result, Status.CALENDAR_NOT_EXIST);
    }

    return result;
  }

  /**
   * verify SchedulerCalendar name
   *
   * @param    name
   * @return true if SchedulerCalendar name can user, otherwise return false
   */
  public Result verifyCalendarName(String name) {
    Result result=new Result();
    if (checkCalendarExists(name)) {
      logger.error("Calendar {} has exist, can't create again.", name);
      putMsg(result, Status.CALENDAR_NAME_EXIST);
    }else{
      putMsg(result, Status.SUCCESS);
    }
    return result;
  }


  /**
   * check SchedulerCalendar exists
   *
   * @param   name
   * @return ture if the SchedulerCalendar name exists, otherwise return false
   */
  private boolean checkCalendarExists(String name) {
      List<SchedulerCalendar> Calendars = schedulerCalendarMapper.queryByCalendarName(name);
      return CollectionUtils.isNotEmpty(Calendars);
  }

  /**
   * release process definition: online / offline
   *
   * @param loginUser login user
   * @param id process definition id
   * @param releaseState release state
   * @return release result code
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> releaseCalendar(User loginUser,  int id, int releaseState) {
    HashMap<String, Object> result = new HashMap<>();

    ReleaseState state = ReleaseState.getEnum(releaseState);

    // check state
    if (null == state) {
      putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "releaseState");
      return result;
    }

    SchedulerCalendar calendar = schedulerCalendarMapper.selectById(id);
    switch (state) {
      case ONLINE:
        calendar.setReleaseState(state);
        schedulerCalendarMapper.updateById(calendar);
        break;
      case OFFLINE:
        calendar.setReleaseState(state);
        schedulerCalendarMapper.updateById(calendar);
        break;
      default:
        putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "releaseState");
        return result;
    }

    putMsg(result, Status.SUCCESS);
    return result;

  }

  /**
   * deal details info
   * @param calendar
   * @param calendarParam
   * @return
   */
  private boolean doDetailsInfo(SchedulerCalendar calendar, CalendarParam calendarParam) {

    boolean result = false;

    //validate calendarParam
    if(null != calendarParam
            && null != calendarParam.getStartTime()
            && null != calendarParam.getEndTime()){

      Map<Integer,Object> extDateMap = new HashMap<>();

      List<Integer> extList = calendarParam.getExtTime();

      if(null != extList && extList.size()>0){
        for (Integer key : extList){
          extDateMap.put(key,null);
        }
      }

      //clear old data
      schedulerCalendarDetailsMapper.clearByCalendarId(calendar.getId());

      //Gets the list<Integer>  days from the start date to the end date
      List<Integer> days  =  getDays(calendarParam.getStartTime(),calendarParam.getEndTime());

      //insert details
      if(null != days && days.size()>0){
        SchedulerCalendarDetails schedulerCalendarDetails = null ;
        for (Integer day : days){

          schedulerCalendarDetails = new SchedulerCalendarDetails();
          schedulerCalendarDetails.setCalendarId(calendar.getId());
          schedulerCalendarDetails.setCreateTime(calendar.getUpdateTime());
          schedulerCalendarDetails.setUpdateTime(calendar.getUpdateTime());
          schedulerCalendarDetails.setUserId(calendar.getUserId());
          schedulerCalendarDetails.setStamp(day);

          schedulerCalendarDetails.setFlag(Flag.YES);

          schedulerCalendarDetailsMapper.insert(schedulerCalendarDetails);
          schedulerCalendarDetails = null ;
        }
      }

      result = true;
    }

    return  result;

  }


  /**
   * 这个方法待优化
   * 获取两个日期之间的所有日期
   *
   * @param startTime
   *            开始日期
   * @param endTime
   *            结束日期
   * @return
   */
  public static List<Integer> getDays(Date startTime, Date endTime) {

    // 返回的日期集合
    List<Integer> days = new ArrayList<Integer>();

    try {

      Calendar tempStart = Calendar.getInstance();
      tempStart.setTime(startTime);
      tempStart.set(Calendar.HOUR_OF_DAY, 0);
      tempStart.set(Calendar.MINUTE, 0);
      tempStart.set(Calendar.SECOND, 0);
      tempStart.set(Calendar.MILLISECOND, 0);

      Calendar tempEnd = Calendar.getInstance();
      tempEnd.setTime(endTime);
      tempEnd.set(Calendar.HOUR_OF_DAY, 0);
      tempEnd.set(Calendar.MINUTE, 0);
      tempEnd.set(Calendar.SECOND, 0);
      tempEnd.set(Calendar.MILLISECOND, 0);

      // 日期加1(包含结束)
      tempEnd.add(Calendar.DATE, +1);
      while (tempStart.before(tempEnd)) {
        String dateTime =  DateUtils.format(tempStart.getTime(),"yyyyMMdd");
        days.add(Integer.valueOf(dateTime) );
        tempStart.add(Calendar.DAY_OF_YEAR, 1);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return days;
  }


}
