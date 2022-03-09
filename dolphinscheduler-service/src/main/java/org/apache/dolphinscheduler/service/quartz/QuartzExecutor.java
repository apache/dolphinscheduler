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

package org.apache.dolphinscheduler.service.quartz;

import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.util.Map;

import org.quartz.Job;

public interface QuartzExecutor {

    /**
     * build job name
     */
    String buildJobName(int scheduleId);

    /**
     * build job group name
     */
    String buildJobGroupName(int projectId);

    /**
     * build data map of job detail
     */
    Map<String, Object> buildDataMap(int projectId, Schedule schedule);

    /**
     * add job to quartz
     */
    void addJob(Class<? extends Job> clazz, int projectId, final Schedule schedule);
}
