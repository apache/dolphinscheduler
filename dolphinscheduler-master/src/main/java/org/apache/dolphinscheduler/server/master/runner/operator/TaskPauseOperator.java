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

package org.apache.dolphinscheduler.server.master.runner.operator;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.task.TaskPauseRequest;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.rpc.MasterRpcClient;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnable;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskPauseOperator implements TaskOperator {

    @Autowired
    private MasterRpcClient masterRpcClient;

    @Override
    public void handle(DefaultTaskExecuteRunnable taskExecuteRunnable) {
        try {
            pauseRemoteTaskInstanceInThreadPool(taskExecuteRunnable.getTaskInstance());
        } catch (Exception e) {
            log.error("Pause MasterTaskExecuteRunnable failed", e);
        }
    }

    private void pauseRemoteTaskInstanceInThreadPool(TaskInstance taskInstance) throws RemotingException {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            log.info("The task instance: {}'s host is null", taskInstance.getName());
            return;
        }
        masterRpcClient.send(Host.of(taskInstance.getHost()),
                new TaskPauseRequest(taskInstance.getId()).convert2Command());
    }
}
