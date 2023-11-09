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

package org.apache.dolphinscheduler.server.worker.runner.operator;

import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskInstancePauseOperationFunction
        implements
            ITaskInstanceOperationFunction<TaskInstancePauseRequest, TaskInstancePauseResponse> {

    @Override
    public TaskInstancePauseResponse operate(TaskInstancePauseRequest taskInstancePauseRequest) {
        try {
            LogUtils.setTaskInstanceIdMDC(taskInstancePauseRequest.getTaskInstanceId());
            log.info("Receive TaskInstancePauseRequest: {}", taskInstancePauseRequest);
            log.warn("TaskInstancePauseOperationFunction is not support for worker task yet!");
            return TaskInstancePauseResponse.success();
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }
}
