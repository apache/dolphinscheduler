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

package org.apache.dolphinscheduler.server.master.engine.task.listener;

import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;

/**
 * Todo: this interface is used to listen to the lifecycle of the task execution runnable.
 * We should use this to listen the state and trigger alert
 */
public interface ITaskExecutionRunnableLifecycleListener {

    void onDispatched(TaskDispatchedLifecycleEvent taskDispatchedEvent);

    void onRunning(TaskRunningLifecycleEvent event);

    void onPaused(TaskPausedLifecycleEvent event);

    void onFailed(TaskFailedLifecycleEvent event);

    void onKilled(TaskKilledLifecycleEvent event);

    void onSuccess(TaskSuccessLifecycleEvent event);

}
