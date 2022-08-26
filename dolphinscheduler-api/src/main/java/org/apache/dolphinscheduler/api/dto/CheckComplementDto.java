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

package org.apache.dolphinscheduler.api.dto;

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.COMMA;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckComplementDto {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ProcessDefinition processDefinition;

    private String scheduleTimeParam;

    private Map<String, String> cronMap;

    private boolean checkResult;

    private TimeSelectionTypeEnum timeSelectionType;

    private String writingTimes;

    private List<String> writingTimeList = Lists.newArrayList();

    private String selectStartDate;

    private String selectEndDate;

    private Date startDate;

    private Date endDate;

    private List<String> filterTimes = Lists.newArrayList();

    public CheckComplementDto(String scheduleTimeParam, ProcessDefinition processDefinition, boolean checkResult) {
        this.scheduleTimeParam = scheduleTimeParam;
        this.processDefinition = processDefinition;
        this.checkResult = checkResult;
    }

    public void setHandleTypes(boolean manual) {
        this.timeSelectionType = manual ? TimeSelectionTypeEnum.MANUAL : TimeSelectionTypeEnum.SELECTED;
    }

    public void setWritingTimes(String writingTimes) {
        this.writingTimes = writingTimes;
        this.writingTimeList = Arrays.stream(writingTimes.split(COMMA)).distinct().collect(Collectors.toList());
    }

    public void setSelectTimes(String startTime, String endTime) {
        this.selectStartDate = startTime;
        this.selectEndDate = endTime;
        this.startDate = DateUtils.stringToDate(selectStartDate);
        this.endDate = DateUtils.stringToDate(selectEndDate);
    }

    public boolean isManual() {
        return timeSelectionType.equals(TimeSelectionTypeEnum.MANUAL);
    }

    public boolean checkCronTimeParamEmpty() {
        if (StringUtils.isEmpty(scheduleTimeParam)) {
            return true;
        }
        Map<String, String> scheduleTimeParamMap = JSONUtils.toMap(scheduleTimeParam);
        boolean manual = scheduleTimeParamMap.containsKey(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);
        boolean selected = scheduleTimeParamMap.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE);
        boolean withoutAny = !manual && !selected;
        if (CollectionUtils.isEmpty(cronMap) || withoutAny) {
            return true;
        }
        this.setHandleTypes(manual);
        this.setCronMap(scheduleTimeParamMap);

        if (isManual()) {
            String writingTimesValue = cronMap.get(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);
            if (StringUtils.isEmpty(writingTimesValue)) {
                return true;
            }
            this.setWritingTimes(writingTimesValue);
        } else {
            String startTime = cronMap.get(CMDPARAM_COMPLEMENT_DATA_START_DATE);
            String endTime = cronMap.get(CMDPARAM_COMPLEMENT_DATA_END_DATE);
            if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
                return true;
            }
            this.setSelectTimes(startTime, endTime);
        }
        return false;
    }

    public boolean checkWritingNumOfTimes() {
        return isManual() && !writingTimeList.isEmpty() && writingTimeList.size() > Constants.SCHEDULE_TIME_MAX_LENGTH;
    }

    public boolean checkStartEndOrder() {
        return !isManual() && startDate.after(endDate);
    }

    public boolean checkSelectNumOfTimes() {
        if (StringUtils.isEmpty(selectStartDate) || StringUtils.isEmpty(selectEndDate)) {
            return false;
        }
        return ChronoUnit.DAYS.between(LocalDate.parse(selectStartDate, FORMATTER), LocalDate.parse(selectEndDate, FORMATTER)) + 1 > Constants.SCHEDULE_TIME_MAX_LENGTH;
    }

    public boolean checkFilter() {
        if (filterTimes.isEmpty()) {
            return false;
        }
        List<String> targetList = Lists.newArrayList();
        targetList.removeAll(filterTimes);
        return targetList.isEmpty();
    }

    /**
     * complement time selection type
     */
    enum TimeSelectionTypeEnum {
        MANUAL, SELECTED
    }
}
