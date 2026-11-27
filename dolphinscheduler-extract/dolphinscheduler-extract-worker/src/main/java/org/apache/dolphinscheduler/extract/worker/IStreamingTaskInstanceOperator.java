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
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceTriggerSavepointRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceTriggerSavepointResponse;

@RpcService
public interface IStreamingTaskInstanceOperator {

    /**
     * The timeout of the savepoint RPC, in milliseconds.
     *
     * <p>The savepoint runs on the worker while the caller waits: every job of the task is
     * checkpointed with a command which waits up to 10 minutes for a large stateful job, so the
     * default RPC timeout of 10s would make the caller give up while the savepoints still run. The
     * request returns as soon as the last savepoint is finished, the value is only an upper bound.
     */
    long TRIGGER_SAVEPOINT_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    /**
     * Trigger the savepoint of the task and wait for its result.
     */
    @RpcMethod(timeout = TRIGGER_SAVEPOINT_TIMEOUT_MILLIS)
    TaskInstanceTriggerSavepointResponse triggerSavepoint(TaskInstanceTriggerSavepointRequest taskInstanceTriggerSavepointRequest);

}
