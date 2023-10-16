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

import org.apache.dolphinscheduler.extract.worker.transportor.UpdateWorkflowHostRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.UpdateWorkflowHostResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UpdateWorkflowHostOperationFunction
        implements
            ITaskInstanceOperationFunction<UpdateWorkflowHostRequest, UpdateWorkflowHostResponse> {

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public UpdateWorkflowHostResponse operate(UpdateWorkflowHostRequest updateWorkflowHostRequest) {
        try {
            final int taskInstanceId = updateWorkflowHostRequest.getTaskInstanceId();
            final String workflowHost = updateWorkflowHostRequest.getWorkflowHost();

            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            log.info("Received UpdateWorkflowHostRequest: {}", updateWorkflowHostRequest);

            TaskExecutionContext taskExecutionContext =
                    TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
            if (taskExecutionContext == null) {
                log.error("Cannot find the taskExecutionContext for taskInstance : {}", taskInstanceId);
                return UpdateWorkflowHostResponse.failed("Cannot find the taskExecutionContext");
            }

            LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());
            taskExecutionContext.setWorkflowInstanceHost(workflowHost);
            messageRetryRunner.updateMessageHost(taskInstanceId, workflowHost);
            log.info("Success update workflow host: {} for taskInstance: {}", workflowHost, taskInstanceId);
            return UpdateWorkflowHostResponse.success();
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
            LogUtils.removeTaskInstanceLogFullPathMDC();
        }
    }
}
