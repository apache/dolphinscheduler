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

package org.apache.dolphinscheduler.plugin.task.api.loop.template.http;

import org.apache.dolphinscheduler.plugin.task.api.loop.LoopTaskDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.method.HttpLoopTaskCancelTaskMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.method.HttpLoopTaskQueryStatusMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.method.HttpLoopTaskSubmitTaskMethodDefinition;

import lombok.NonNull;

public class HttpLoopTaskDefinition
        implements
            LoopTaskDefinition<HttpLoopTaskSubmitTaskMethodDefinition, HttpLoopTaskQueryStatusMethodDefinition, HttpLoopTaskCancelTaskMethodDefinition> {

    private final String taskName;
    private final HttpLoopTaskSubmitTaskMethodDefinition submitTaskMethod;
    private final HttpLoopTaskQueryStatusMethodDefinition queryTaskStateMethod;
    private final HttpLoopTaskCancelTaskMethodDefinition cancelTaskMethod;

    public HttpLoopTaskDefinition(@NonNull String taskName,
                                  @NonNull HttpLoopTaskSubmitTaskMethodDefinition submitTaskMethod,
                                  @NonNull HttpLoopTaskQueryStatusMethodDefinition queryTaskStateMethod,
                                  @NonNull HttpLoopTaskCancelTaskMethodDefinition cancelTaskMethod) {
        this.taskName = taskName;
        this.submitTaskMethod = submitTaskMethod;
        this.queryTaskStateMethod = queryTaskStateMethod;
        this.cancelTaskMethod = cancelTaskMethod;
    }

    @Override
    public @NonNull String getTaskName() {
        return taskName;
    }

    @Override
    public @NonNull HttpLoopTaskSubmitTaskMethodDefinition getSubmitTaskMethod() {
        return submitTaskMethod;
    }

    @Override
    public @NonNull HttpLoopTaskQueryStatusMethodDefinition getQueryTaskStateMethod() {
        return queryTaskStateMethod;
    }

    @Override
    public @NonNull HttpLoopTaskCancelTaskMethodDefinition getCancelTaskMethod() {
        return cancelTaskMethod;
    }
}
