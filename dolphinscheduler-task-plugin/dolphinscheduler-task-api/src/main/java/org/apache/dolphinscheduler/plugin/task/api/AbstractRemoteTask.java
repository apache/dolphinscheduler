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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;

public abstract class AbstractRemoteTask extends AbstractTask {

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected AbstractRemoteTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void cancel() throws TaskException {
        this.cancelApplication();
    }

    public abstract List<String> getApplicationIds() throws TaskException;

    public abstract void cancelApplication() throws TaskException;

    /**
     * If appIds is empty, submit a new remote application; otherwise, just track application status.
     *
     * @param taskCallBack
     * @throws TaskException
     */
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        // if appIds is not empty, just track application status, avoid resubmitting remote task
        if (StringUtils.isNotEmpty(taskRequest.getAppIds())) {
            setAppIds(taskRequest.getAppIds());
            trackApplicationStatus();
            return;
        }

        // submit a remote application
        submitApplication();

        if (StringUtils.isNotEmpty(getAppIds())) {
            taskRequest.setAppIds(getAppIds());
            // callback to update remote application info
            taskCallBack.updateRemoteApplicationInfo(taskRequest.getTaskInstanceId(), new ApplicationInfo(getAppIds()));
        }

        // keep tracking application status
        trackApplicationStatus();
    }

    /**
     * submit a new remote application and get application info
     *
     * @return
     * @throws TaskException
     */
    public abstract void submitApplication() throws TaskException;

    /**
     * keep checking application status
     * @throws TaskException
     */
    public abstract void trackApplicationStatus() throws TaskException;
}
