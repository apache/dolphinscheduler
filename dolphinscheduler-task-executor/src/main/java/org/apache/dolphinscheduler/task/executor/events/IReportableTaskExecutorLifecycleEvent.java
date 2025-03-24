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

package org.apache.dolphinscheduler.task.executor.events;

import org.apache.dolphinscheduler.task.executor.ITaskExecutor;

/**
 * The lifecycle event of {@link ITaskExecutor} which should be report to master.
 */
public interface IReportableTaskExecutorLifecycleEvent extends ITaskExecutorLifecycleEvent {

    /**
     * The id of the workflow instance which the event should report to.
     */
    int getWorkflowInstanceId();

    /**
     * The host of the workflow instance which the event should report to.
     */
    String getWorkflowInstanceHost();

    /**
     * Set the host of the workflow instance which the event should report to.
     */
    void setWorkflowInstanceHost(String workflowInstanceHost);

    /**
     * Get the latest report time of the event, if the event is never reported, return null.
     */
    Long getLatestReportTime();

    /**
     * Set the latest report time of the event.
     */
    void setLatestReportTime(Long latestReportTime);

}
