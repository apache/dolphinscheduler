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

package org.apache.dolphinscheduler.scheduler.quartz;

import lombok.Getter;

import org.quartz.JobDataMap;

@Getter
public class QuartzJobData {

    private static final String PROJECT_ID = "projectId";
    private static final String SCHEDULE_ID = "scheduleId";

    private final Integer projectId;

    private final Integer scheduleId;

    private QuartzJobData(Integer projectId, Integer scheduleId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId cannot be null");
        }
        if (scheduleId == null) {
            throw new IllegalArgumentException("schedule cannot be null");
        }
        this.projectId = projectId;
        this.scheduleId = scheduleId;
    }

    public static QuartzJobData of(Integer projectId, Integer scheduleId) {
        return new QuartzJobData(projectId, scheduleId);
    }

    public static QuartzJobData of(JobDataMap jobDataMap) {
        Integer projectId = jobDataMap.getInt(PROJECT_ID);
        Integer scheduleId = jobDataMap.getInt(SCHEDULE_ID);
        return of(projectId, scheduleId);
    }

    public JobDataMap toJobDataMap() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(PROJECT_ID, projectId);
        jobDataMap.put(SCHEDULE_ID, scheduleId);
        return jobDataMap;
    }

}
