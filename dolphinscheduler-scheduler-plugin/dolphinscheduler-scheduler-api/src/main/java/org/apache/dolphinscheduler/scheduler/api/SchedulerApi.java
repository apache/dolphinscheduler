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

package org.apache.dolphinscheduler.scheduler.api;

import org.apache.dolphinscheduler.dao.entity.Schedule;

/**
 * This is the interface for scheduler, contains methods to operate schedule task.
 */
public interface SchedulerApi extends AutoCloseable {

    /**
     * Start the scheduler, if not start, the scheduler will not execute task.
     *
     * @throws SchedulerException if start failed.
     */
    void start() throws SchedulerException;

    /**
     * @param projectId project id, the schedule task belongs to.
     * @param schedule  schedule metadata.
     * @throws SchedulerException if insert/update failed.
     */
    void insertOrUpdateScheduleTask(int projectId, Schedule schedule) throws SchedulerException;

    /**
     * Delete a schedule task.
     *
     * @param projectId  project id, the schedule task belongs to.
     * @param scheduleId schedule id.
     * @throws SchedulerException if delete failed.
     */
    void deleteScheduleTask(int projectId, int scheduleId) throws SchedulerException;

    /**
     * Close the scheduler and release the resource.
     *
     * @throws SchedulerException if close failed.
     */
    void close() throws Exception;
}
