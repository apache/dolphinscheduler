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
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskDispatchRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskDispatchResponse;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskKillRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskKillResponse;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskPauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskPauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceForceStartRequest;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceForceStartResponse;
import org.apache.dolphinscheduler.extract.master.transportor.TaskInstanceWakeupRequest;

@RpcService
public interface ILogicTaskInstanceOperator {

    @RpcMethod
    LogicTaskDispatchResponse dispatchLogicTask(LogicTaskDispatchRequest taskDispatchRequest);

    @RpcMethod
    LogicTaskKillResponse killLogicTask(LogicTaskKillRequest taskKillRequest);

    @RpcMethod
    LogicTaskPauseResponse pauseLogicTask(LogicTaskPauseRequest taskPauseRequest);

    @RpcMethod
    TaskInstanceForceStartResponse forceStartTaskInstance(TaskInstanceForceStartRequest taskForceStartRequest);

    @RpcMethod
    void wakeupTaskInstance(TaskInstanceWakeupRequest taskWakeupRequest);

}
