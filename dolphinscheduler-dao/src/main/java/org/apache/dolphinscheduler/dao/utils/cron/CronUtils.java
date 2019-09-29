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
package org.apache.dolphinscheduler.dao.utils.cron;


import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

import static com.cronutils.model.CronType.QUARTZ;
import static org.apache.dolphinscheduler.dao.utils.cron.CycleFactory.*;


/**
 * cron utils
 */
public class CronUtils {

  private static final Logger logger = LoggerFactory.getLogger(CronUtils.class);


  private static final CronParser QUARTZ_CRON_PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));

  /**
   * Parse string with cron expression to Cron
   *
   * @param cronExpression
   *            - cron expression, never null
   * @return Cron instance, corresponding to cron expression received
   * @throws java.lang.IllegalArgumentException
   *             if expression does not match cron definition
   */
  public static Cron parse2Cron(String cronExpression) {
    return QUARTZ_CRON_PARSER.parse(cronExpression);
  }

  /**
   * build a new <CODE>CronExpression</CODE> based on the string cronExpression.
   *
   * @param cronExpression String representation of the cron expression the
   *                       new object should represent
   * @throws java.text.ParseException
   *         if the string expression cannot be parsed into a valid
   *         <CODE>CronExpression</CODE>
   */
  public static CronExpression parse2CronExpression(String cronExpression) throws ParseException {
    return new CronExpression(cronExpression);
  }

  /**
   * get cycle enum
   * @param cron
   * @return
   */
  public static CycleEnum getMaxCycle(Cron cron) {
    return min(cron).addCycle(hour(cron)).addCycle(day(cron)).addCycle(week(cron)).addCycle(month(cron)).getCycle();
  }

  /**
   * get cycle enum
   * @param cron
   * @return
   */
  public static CycleEnum getMiniCycle(Cron cron) {
    return min(cron).addCycle(hour(cron)).addCycle(day(cron)).addCycle(week(cron)).addCycle(month(cron)).getMiniCycle();
  }

  /**
   * get mini level of cycle enum
   *
   * @param crontab
   * @return
   */
  public static CycleEnum getMiniCycle(String crontab) {
    return getMiniCycle(parse2Cron(crontab));
  }

  /**
   * get cycle enum
   *
   * @param crontab
   * @return
   */
  public static CycleEnum getMaxCycle(String crontab) {
    return getMaxCycle(parse2Cron(crontab));
  }

  /**
   * gets all scheduled times for a period of time based on not self dependency
   * @param startTime
   * @param endTime
   * @param cronExpression
   * @return
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
   * gets all scheduled times for a period of time based on self dependency
   * @param startTime
   * @param endTime
   * @param cronExpression
   * @return
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
   * get expiration time
   * @param startTime
   * @param cycleEnum
   * @return
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
          logger.error("Dependent process definition's  cycleEnum is {},not support!!", cycleEnum.name());
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
   * @return
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
