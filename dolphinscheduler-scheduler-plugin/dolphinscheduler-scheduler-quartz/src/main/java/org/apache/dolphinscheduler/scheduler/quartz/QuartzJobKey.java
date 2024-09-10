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

import org.quartz.JobKey;

@Getter
public class QuartzJobKey {

    private final int schedulerId;
    private final int projectId;

    private static final String QUARTZ_JOB_PREFIX = "job";
    private static final String QUARTZ_JOB_GROUP_PREFIX = "jobgroup";
    private static final String UNDERLINE = "_";

    private QuartzJobKey(int projectId, int schedulerId) {
        this.schedulerId = schedulerId;
        this.projectId = projectId;
    }

    public static QuartzJobKey of(int projectId, int schedulerId) {
        return new QuartzJobKey(projectId, schedulerId);
    }

    public JobKey toJobKey() {
        // todo: We don't need to add prefix to job name and job group?
        String jobName = QUARTZ_JOB_PREFIX + UNDERLINE + schedulerId;
        String jobGroup = QUARTZ_JOB_GROUP_PREFIX + UNDERLINE + projectId;
        return new JobKey(jobName, jobGroup);
    }
}
