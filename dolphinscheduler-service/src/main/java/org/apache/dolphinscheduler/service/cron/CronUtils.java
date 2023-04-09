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

package org.apache.dolphinscheduler.service.cron;

import static com.cronutils.model.CronType.QUARTZ;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.day;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.hour;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.min;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.month;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.week;
import static org.apache.dolphinscheduler.service.cron.CycleFactory.year;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

/**
 * // todo: this utils is heavy, it rely on quartz and corn-utils.
 * cron utils
 */
@Slf4j
public class CronUtils {

    private CronUtils() {
        throw new IllegalStateException("CronUtils class");
    }

    private static final CronParser QUARTZ_CRON_PARSER =
            new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));

    /**
     * parse to cron
     *
     * @param cronExpression cron expression, never null
     * @return Cron instance, corresponding to cron expression received
     */
    public static Cron parse2Cron(String cronExpression) throws CronParseException {
        try {
            return QUARTZ_CRON_PARSER.parse(cronExpression);
        } catch (IllegalArgumentException ex) {
            throw new CronParseException(String.format("Parse corn expression: [%s] error", cronExpression), ex);
        }
    }

    /**
     * Indicates whether the specified cron expression can be parsed into a
     * valid cron expression
     *
     * @param cronExpression the expression to evaluate
     * @return a boolean indicating whether the given expression is a valid cron
     *         expression
     */
    public static boolean isValidExpression(String cronExpression) {
        try {
            parse2Cron(cronExpression);
        } catch (CronParseException e) {
            return false;
        }

        return true;
    }

    /**
     * get max cycle
     *
     * @param cron cron
     * @return CycleEnum
     */
    public static CycleEnum getMaxCycle(Cron cron) {
        return min(cron).addCycle(hour(cron)).addCycle(day(cron)).addCycle(week(cron)).addCycle(month(cron))
                .addCycle(year(cron)).getCycle();
    }

    /**
     * get min cycle
     *
     * @param cron cron
     * @return CycleEnum
     */
    public static CycleEnum getMiniCycle(Cron cron) {
        return min(cron).addCycle(hour(cron))
                .addCycle(day(cron))
                .addCycle(week(cron))
                .addCycle(month(cron))
                .addCycle(year(cron))
                .getMiniCycle();
    }

    /**
     * get max cycle
     *
     * @param crontab crontab
     * @return CycleEnum
     */
    public static CycleEnum getMaxCycle(String crontab) {
        try {
            return getMaxCycle(parse2Cron(crontab));
        } catch (CronParseException ex) {
            throw new RuntimeException("Get max cycle error", ex);
        }
    }

    public static List<ZonedDateTime> getFireDateList(@NonNull ZonedDateTime startTime,
                                                      @NonNull ZonedDateTime endTime,
                                                      @NonNull String cron) throws CronParseException {
        return getFireDateList(startTime, endTime, parse2Cron(cron));
    }

    /**
     * gets all scheduled times for a period of time based on not self dependency
     *
     * @param startTime startTime
     * @param endTime   endTime
     * @param cron      cron
     * @return date list
     */
    public static List<ZonedDateTime> getFireDateList(@NonNull ZonedDateTime startTime,
                                                      @NonNull ZonedDateTime endTime,
                                                      @NonNull Cron cron) {
        List<ZonedDateTime> dateList = new ArrayList<>();
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        while (!ServerLifeCycleManager.isStopped()) {
            Optional<ZonedDateTime> nextExecutionTimeOptional = executionTime.nextExecution(startTime);
            if (!nextExecutionTimeOptional.isPresent()) {
                break;
            }
            startTime = nextExecutionTimeOptional.get();
            if (startTime.isAfter(endTime)) {
                break;
            }
            dateList.add(startTime);
        }

        return dateList;
    }

    /**
     * Gets expect scheduled times for a period of time based on self dependency
     *
     * @param startTime startTime
     * @param endTime   endTime
     * @param cron      cron
     * @param fireTimes fireTimes
     * @return nextTime execution list
     */
    public static List<ZonedDateTime> getSelfFireDateList(@NonNull ZonedDateTime startTime,
                                                          @NonNull ZonedDateTime endTime, @NonNull Cron cron,
                                                          int fireTimes) {
        List<ZonedDateTime> executeTimes = new ArrayList<>();
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        while (fireTimes > 0) {
            Optional<ZonedDateTime> nextTime = executionTime.nextExecution(startTime);
            if (!nextTime.isPresent()) {
                break;
            }
            startTime = nextTime.get();
            if (startTime.isAfter(endTime)) {
                break;
            }
            executeTimes.add(startTime);
            fireTimes--;
        }
        return executeTimes;
    }

    public static List<Date> getSelfFireDateList(@NonNull final Date startTime,
                                                 @NonNull final Date endTime,
                                                 @NonNull final List<Schedule> schedules) throws CronParseException {
        ZonedDateTime zonedDateTimeStart = ZonedDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault());
        ZonedDateTime zonedDateTimeEnd = ZonedDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault());

        return getSelfFireDateList(zonedDateTimeStart, zonedDateTimeEnd, schedules).stream()
                .map(zonedDateTime -> new Date(zonedDateTime.toInstant().toEpochMilli()))
                .collect(Collectors.toList());
    }

    /**
     * gets all scheduled times for a period of time based on self dependency
     * if schedulers is empty then default scheduler = 1 day
     */
    public static List<ZonedDateTime> getSelfFireDateList(@NonNull final ZonedDateTime startTime,
                                                          @NonNull final ZonedDateTime endTime,
                                                          @NonNull final List<Schedule> schedules) throws CronParseException {
        List<ZonedDateTime> result = new ArrayList<>();
        if (startTime.equals(endTime)) {
            result.add(startTime);
            return result;
        }

        // support left closed and right closed time interval (startDate <= N <= endDate)
        ZonedDateTime from = startTime.minusSeconds(1L);
        ZonedDateTime to = endTime.plusSeconds(1L);

        List<Schedule> listSchedule = new ArrayList<>();
        listSchedule.addAll(schedules);
        if (CollectionUtils.isEmpty(listSchedule)) {
            Schedule schedule = new Schedule();
            schedule.setCrontab(Constants.DEFAULT_CRON_STRING);
            listSchedule.add(schedule);
        }
        for (Schedule schedule : listSchedule) {
            result.addAll(CronUtils.getFireDateList(from, to, schedule.getCrontab()));
        }
        return result;
    }

    /**
     * get expiration time
     *
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
                    log.error("Dependent process definition's  cycleEnum is {},not support!!", cycleEnum);
                    break;
            }
            maxExpirationTime = calendar.getTime();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return DateUtils.compare(startTimeMax, maxExpirationTime) ? maxExpirationTime : startTimeMax;
    }

    /**
     * get the end time of the day by value of date
     *
     * @return date
     */
    private static Date getEndTime(Date date) {
        Calendar end = new GregorianCalendar();
        end.setTime(date);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTime();
    }

    /**
     * get Schedule Date
     *
     * @param param
     * @return date list
     */
    public static List<Date> getSelfScheduleDateList(Map<String, String> param) {
        List<Date> result = new ArrayList<>();
        String scheduleDates = param.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);
        if (StringUtils.isNotEmpty(scheduleDates)) {
            for (String stringDate : scheduleDates.split(COMMA)) {
                result.add(DateUtils.stringToDate(stringDate.trim()));
            }
            return result;
        }
        return null;
    }

}
