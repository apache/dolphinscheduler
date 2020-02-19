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
package org.apache.dolphinscheduler.service.quartz.cron;


import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

import static com.cronutils.model.CronType.QUARTZ;
import static org.apache.dolphinscheduler.service.quartz.cron.CycleFactory.*;


/**
 * cron utils
 */
public class CronUtils {
  private CronUtils() {
    throw new IllegalStateException("CronUtils class");
  }
  private static final Logger logger = LoggerFactory.getLogger(CronUtils.class);


  private static final CronParser QUARTZ_CRON_PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));

  /**
   * parse to cron
   * @param cronExpression cron expression, never null
   * @return Cron instance, corresponding to cron expression received
   */
  public static Cron parse2Cron(String cronExpression) {
    return QUARTZ_CRON_PARSER.parse(cronExpression);
  }


  /**
   * build a new CronExpression based on the string cronExpression
   * @param cronExpression String representation of the cron expression the new object should represent
   * @return CronExpression
   * @throws ParseException if the string expression cannot be parsed into a valid
   */
  public static CronExpression parse2CronExpression(String cronExpression) throws ParseException {
    return new CronExpression(cronExpression);
  }

  /**
   * get max cycle
   * @param cron cron
   * @return CycleEnum
   */
  public static CycleEnum getMaxCycle(Cron cron) {
    return min(cron).addCycle(hour(cron)).addCycle(day(cron)).addCycle(week(cron)).addCycle(month(cron)).getCycle();
  }

  /**
   * get min cycle
   * @param cron cron
   * @return CycleEnum
   */
  public static CycleEnum getMiniCycle(Cron cron) {
    return min(cron).addCycle(hour(cron)).addCycle(day(cron)).addCycle(week(cron)).addCycle(month(cron)).getMiniCycle();
  }

  /**
   * get max cycle
   * @param crontab crontab
   * @return CycleEnum
   */
  public static CycleEnum getMaxCycle(String crontab) {
    return getMaxCycle(parse2Cron(crontab));
  }

  /**
   * gets all scheduled times for a period of time based on not self dependency
   * @param startTime startTime
   * @param endTime endTime
   * @param cronExpression cronExpression
   * @return date list
   */
  public static List<Date> getFireDateList(Date startTime, Date endTime, CronExpression cronExpression) {
    List<Date> dateList = new ArrayList<>();

    while (Stopper.isRunning()) {
      startTime = cronExpression.getNextValidTimeAfter(startTime);
      if (startTime.after(endTime)) {
        break;
      }
      dateList.add(startTime);
    }

    return dateList;
  }

  /**
   * gets expect scheduled times for a period of time based on self dependency
   * @param startTime startTime
   * @param endTime endTime
   * @param cronExpression cronExpression
   * @param fireTimes fireTimes
   * @return date list
   */
  public static List<Date> getSelfFireDateList(Date startTime, Date endTime, CronExpression cronExpression,int fireTimes) {
    List<Date> dateList = new ArrayList<>();
    while (fireTimes > 0) {
      startTime = cronExpression.getNextValidTimeAfter(startTime);
      if (startTime.after(endTime) || startTime.equals(endTime)) {
        break;
      }
      dateList.add(startTime);
      fireTimes--;
    }

    return dateList;
  }


  /**
   * gets all scheduled times for a period of time based on self dependency
   * @param startTime startTime
   * @param endTime endTime
   * @param cronExpression cronExpression
   * @return date list
   */
  public static List<Date> getSelfFireDateList(Date startTime, Date endTime, CronExpression cronExpression) {
    List<Date> dateList = new ArrayList<>();

    while (Stopper.isRunning()) {
      startTime = cronExpression.getNextValidTimeAfter(startTime);
      if (startTime.after(endTime) || startTime.equals(endTime)) {
        break;
      }
      dateList.add(startTime);
    }

    return dateList;
  }

  /**
   * gets all scheduled times for a period of time based on self dependency
   * @param startTime startTime
   * @param endTime endTime
   * @param cron cron
   * @return date list
   */
  public static List<Date> getSelfFireDateList(Date startTime, Date endTime, String cron) {
    CronExpression cronExpression = null;
    try {
      cronExpression = parse2CronExpression(cron);
    }catch (ParseException e){
      logger.error(e.getMessage(), e);
      return Collections.emptyList();
    }
    return getSelfFireDateList(startTime, endTime, cronExpression);
  }

  /**
   * get expiration time
   * @param startTime startTime
   * @param cycleEnum cycleEnum
   * @return date
   */
  public static Date getExpirationTime(Date startTime, CycleEnum cycleEnum) {
    Date maxExpirationTime = null;
    Date startTimeMax = null;
    try {
      startTimeMax = getEndTime(startTime);

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(startTime);
      switch (cycleEnum) {
        case HOUR:
          calendar.add(Calendar.HOUR, 1);
          break;
        case DAY:
          calendar.add(Calendar.DATE, 1);
          break;
        case WEEK:
          calendar.add(Calendar.DATE, 1);
          break;
        case MONTH:
          calendar.add(Calendar.DATE, 1);
          break;
        default:
          logger.error("Dependent process definition's  cycleEnum is {},not support!!", cycleEnum);
          break;
      }
      maxExpirationTime = calendar.getTime();
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
    }
    return DateUtils.compare(startTimeMax,maxExpirationTime)?maxExpirationTime:startTimeMax;
  }

  /**
   * get the end time of the day by value of date
   * @param date
   * @return date
   */
  private static Date getEndTime(Date date) {
    Calendar end = new GregorianCalendar();
    end.setTime(date);
    end.set(Calendar.HOUR_OF_DAY,23);
    end.set(Calendar.MINUTE,59);
    end.set(Calendar.SECOND,59);
    end.set(Calendar.MILLISECOND,999);
    return end.getTime();
  }

}
