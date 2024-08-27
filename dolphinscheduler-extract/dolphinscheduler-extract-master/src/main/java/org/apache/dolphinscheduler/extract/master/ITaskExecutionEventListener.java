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

package org.apache.dolphinscheduler.extract.master;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionDispatchEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionFailedEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionKilledEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionPausedEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionRunningEvent;
import org.apache.dolphinscheduler.extract.master.transportor.TaskExecutionSuccessEvent;

@RpcService
public interface ITaskExecutionEventListener {

    @RpcMethod
    void onTaskInstanceDispatched(final TaskExecutionDispatchEvent taskExecutionDispatchEvent);

    @RpcMethod
    void onTaskInstanceExecutionRunning(final TaskExecutionRunningEvent taskInstanceExecutionRunningEvent);

    @RpcMethod
    void onTaskInstanceExecutionSuccess(final TaskExecutionSuccessEvent taskInstanceExecutionSuccessEvent);

    @RpcMethod
    void onTaskInstanceExecutionFailed(final TaskExecutionFailedEvent taskInstanceExecutionFailedEvent);

    @RpcMethod
    void onTaskInstanceExecutionKilled(final TaskExecutionKilledEvent taskInstanceExecutionKilledEvent);

    @RpcMethod
    void onTaskInstanceExecutionPaused(final TaskExecutionPausedEvent taskInstanceExecutionPausedEvent);

}
