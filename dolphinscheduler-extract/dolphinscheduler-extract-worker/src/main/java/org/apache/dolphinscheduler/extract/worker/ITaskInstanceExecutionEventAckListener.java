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

package org.apache.dolphinscheduler.extract.worker;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionFailedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionKilledEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionPausedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutionSuccessEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionDispatchedEventAck;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceExecutionRunningEventAck;

@RpcService
public interface ITaskInstanceExecutionEventAckListener {

    @RpcMethod
    void handleTaskInstanceDispatchedEventAck(final TaskInstanceExecutionDispatchedEventAck taskInstanceExecutionDispatchedEventAck);

    // todo: If we use sync, then we don't need ack here
    @RpcMethod
    void handleTaskInstanceExecutionRunningEventAck(TaskInstanceExecutionRunningEventAck taskInstanceExecutionRunningEventAck);

    @RpcMethod
    void handleTaskExecutionSuccessEventAck(TaskExecutionSuccessEventAck taskExecutionSuccessEventAck);

    @RpcMethod
    void handleTaskExecutionPausedEventAck(TaskExecutionPausedEventAck taskExecutionPausedEventAck);

    @RpcMethod
    void handleTaskExecutionFailedEventAck(TaskExecutionFailedEventAck taskExecutionFailedEventAck);

    @RpcMethod
    void handleTaskExecutionKilledEventAck(TaskExecutionKilledEventAck taskExecutionKilledEventAck);

}
