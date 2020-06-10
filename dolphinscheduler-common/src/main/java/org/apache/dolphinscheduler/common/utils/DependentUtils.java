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
package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.model.DateInterval;
import org.apache.dolphinscheduler.common.utils.dependent.DependentDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DependentUtils {

    private static final Logger logger = LoggerFactory.getLogger(DependentUtils.class);

    public static DependResult getDependResultForRelation(DependentRelation relation,
                                                          List<DependResult> dependResultList){

        DependResult dependResult = DependResult.SUCCESS;

        switch (relation){
            case AND:
                if(dependResultList.contains(DependResult.FAILED)){
                    dependResult = DependResult.FAILED;
                }else if(dependResultList.contains(DependResult.WAITING)){
                    dependResult = DependResult.WAITING;
                }else{
                    dependResult = DependResult.SUCCESS;
                }
                break;
            case OR:
                if(dependResultList.contains(DependResult.SUCCESS)){
                    dependResult = DependResult.SUCCESS;
                }else if(dependResultList.contains(DependResult.WAITING)){
                    dependResult = DependResult.WAITING;
                }else{
                    dependResult = DependResult.FAILED;
                }
                break;
            default:
               break;
        }
        return dependResult;
    }


    /**
     * get date interval list by business date and date value.
     * @param businessDate business date
     * @param dateValue date value
     * @return date interval list by business date and date value.
     */
    public static List<DateInterval> getDateIntervalList(Date businessDate, String dateValue){
        List<DateInterval> result = new ArrayList<>();
        switch (dateValue){
            case "currentHour":
                result = DependentDateUtils.getLastHoursInterval(businessDate, 0);
                break;
            case "last1Hour":
                result =  DependentDateUtils.getLastHoursInterval(businessDate, 1);
                break;
            case "last2Hours":
                result =  DependentDateUtils.getLastHoursInterval(businessDate, 2);
                break;
            case "last3Hours":
                result =  DependentDateUtils.getLastHoursInterval(businessDate, 3);
                break;
            case "last24Hours":
                result = DependentDateUtils.getSpecialLastDayInterval(businessDate);
                break;
            case "today":
                result =  DependentDateUtils.getTodayInterval(businessDate);
                break;
            case "last1Days":
                result =  DependentDateUtils.getLastDayInterval(businessDate, 1);
                break;
            case "last2Days":
                result =  DependentDateUtils.getLastDayInterval(businessDate, 2);
                break;
            case "last3Days":
                result = DependentDateUtils.getLastDayInterval(businessDate, 3);
                break;
            case "last7Days":
                result = DependentDateUtils.getLastDayInterval(businessDate, 7);
                break;
            case "thisWeek":
                result = DependentDateUtils.getThisWeekInterval(businessDate);
                break;
            case "lastWeek":
                result = DependentDateUtils.getLastWeekInterval(businessDate);
                break;
            case "lastMonday":
                result = DependentDateUtils.getLastWeekOneDayInterval(businessDate, 1);
                break;
            case "lastTuesday":
                result = DependentDateUtils.getLastWeekOneDayInterval(businessDate, 2);
                break;
            case "lastWednesday":
                result = DependentDateUtils.getLastWeekOneDayInterval(businessDate, 3);
                break;
            case "lastThursday":
                result = DependentDateUtils.getLastWeekOneDayInterval(businessDate, 4);
                break;
            case "lastFriday":
                result = DependentDateUtils.getLastWeekOneDayInterval(businessDate, 5);
                break;
            case "lastSaturday":
                result = DependentDateUtils.getLastWeekOneDayInterval(businessDate, 6);
                break;
            case "lastSunday":
                result = DependentDateUtils.getLastWeekOneDayInterval(businessDate, 7);
                break;
            case "thisMonth":
                result = DependentDateUtils.getThisMonthInterval(businessDate);
                break;
            case "lastMonth":
                result = DependentDateUtils.getLastMonthInterval(businessDate);
                break;
            case "lastMonthBegin":
                result = DependentDateUtils.getLastMonthBeginInterval(businessDate, true);
                break;
            case "lastMonthEnd":
                result = DependentDateUtils.getLastMonthBeginInterval(businessDate, false);
                break;
            default:
                break;
        }
        return result;
    }


}
