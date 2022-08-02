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

package org.apache.dolphinscheduler.server.master.runner.task;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_STREAM;

import com.google.auto.service.AutoService;

/**
 * stream task processor for DAG, do nothing and skip
 */
@AutoService(ITaskProcessor.class)
public class StreamTaskProcessor extends BaseTaskProcessor {

    @Override
    protected boolean submitTask() {
        return true;
    }

    @Override
    protected boolean resubmitTask() {
        return true;
    }

    @Override
    public boolean runTask() {
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        return true;
    }

    @Override
    protected boolean pauseTask() {
        return true;
    }

    @Override
    public String getType() {
        return TASK_TYPE_STREAM;
    }

    @Override
    public boolean dispatchTask() {
        return true;
    }

    @Override
    public boolean killTask() {
        return true;
    }
}
