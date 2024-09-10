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

import org.quartz.JobBuilder;
import org.quartz.JobDetail;

public class QuartzJobDetailBuilder {

    private Integer projectId;

    private Integer scheduleId;

    public static QuartzJobDetailBuilder newBuilder() {
        return new QuartzJobDetailBuilder();
    }

    public QuartzJobDetailBuilder withProjectId(Integer projectId) {
        this.projectId = projectId;
        return this;
    }

    public QuartzJobDetailBuilder withSchedule(Integer scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    public JobDetail build() {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId cannot be null");
        }
        if (scheduleId == null) {
            throw new IllegalArgumentException("scheduleId cannot be null");
        }
        QuartzJobData quartzJobData = QuartzJobData.of(projectId, scheduleId);

        return JobBuilder.newJob(ProcessScheduleTask.class)
                .withIdentity(QuartzJobKey.of(projectId, scheduleId).toJobKey())
                .setJobData(quartzJobData.toJobDataMap())
                .build();
    }

}
