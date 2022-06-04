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

package org.apache.dolphinscheduler.scheduler.quartz.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.util.HashMap;
import java.util.Map;

import org.quartz.JobKey;

public final class QuartzTaskUtils {

    public static final String QUARTZ_JOB_PREFIX = "job";
    public static final String QUARTZ_JOB_GROUP_PREFIX = "jobgroup";
    public static final String UNDERLINE = "_";
    public static final String PROJECT_ID = "projectId";
    public static final String SCHEDULE_ID = "scheduleId";
    public static final String SCHEDULE = "schedule";

    /**
     * @param schedulerId scheduler id
     * @return quartz job name
     */
    public static JobKey getJobKey(int schedulerId, int projectId) {
        String jobName = QUARTZ_JOB_PREFIX + UNDERLINE + schedulerId;
        String jobGroup = QUARTZ_JOB_GROUP_PREFIX + UNDERLINE + projectId;
        return new JobKey(jobName, jobGroup);
    }

    /**
     * create quartz job data, include projectId and scheduleId, schedule.
     */
    public static Map<String, Object> buildDataMap(int projectId, Schedule schedule) {
        Map<String, Object> dataMap = new HashMap<>(8);
        dataMap.put(PROJECT_ID, projectId);
        dataMap.put(SCHEDULE_ID, schedule.getId());
        dataMap.put(SCHEDULE, JSONUtils.toJsonString(schedule));

        return dataMap;
    }

}
