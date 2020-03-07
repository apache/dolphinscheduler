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
package org.apache.dolphinscheduler.common.utils.placeholder;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.dolphinscheduler.common.Constants.PARAMETER_FORMAT_DATE;
import static org.apache.dolphinscheduler.common.Constants.PARAMETER_FORMAT_TIME;
import static org.apache.dolphinscheduler.common.utils.DateUtils.format;
import static org.apache.commons.lang.time.DateUtils.addDays;


/**
 * business time utils
 */
public class BusinessTimeUtils {
  private BusinessTimeUtils() {
    throw new IllegalStateException("BusinessTimeUtils class");
  }
  /**
   * get business time in parameters by different command types
   *
   * @param commandType command type
   * @param runTime run time or schedule time
   * @return business time
   */
  public static Map<String, String> getBusinessTime(CommandType commandType, Date runTime) {
    Date businessDate = runTime;
    switch (commandType) {
      case COMPLEMENT_DATA:
          break;
      case START_PROCESS:
      case START_CURRENT_TASK_PROCESS:
      case RECOVER_TOLERANCE_FAULT_PROCESS:
      case RECOVER_SUSPENDED_PROCESS:
      case START_FAILURE_TASK_PROCESS:
      case REPEAT_RUNNING:
      case SCHEDULER:
      default:
          businessDate = addDays(new Date(), -1);
          if (runTime != null){
            /**
             * If there is a scheduled time, take the scheduling time. Recovery from failed nodes, suspension of recovery, re-run for scheduling
             */
            businessDate = addDays(runTime, -1);
          }
          break;
    }
    Date businessCurrentDate = addDays(businessDate, 1);
    Map<String, String> result = new HashMap<>();
    result.put(Constants.PARAMETER_CURRENT_DATE, format(businessCurrentDate, PARAMETER_FORMAT_DATE));
    result.put(Constants.PARAMETER_BUSINESS_DATE, format(businessDate, PARAMETER_FORMAT_DATE));
    result.put(Constants.PARAMETER_DATETIME, format(businessCurrentDate, PARAMETER_FORMAT_TIME));
    return result;
  }
}
