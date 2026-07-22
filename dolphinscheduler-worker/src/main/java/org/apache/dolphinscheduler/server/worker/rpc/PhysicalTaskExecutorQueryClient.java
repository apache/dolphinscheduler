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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.extract.worker.ITaskExecutorQueryClient;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutorQueryRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskExecutorQueryResponse;
import org.apache.dolphinscheduler.server.worker.executor.PhysicalTaskEngineDelegator;
import org.apache.dolphinscheduler.task.executor.dto.TaskExecutorDTO;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PhysicalTaskExecutorQueryClient implements ITaskExecutorQueryClient {

    private final PhysicalTaskEngineDelegator physicalTaskEngineDelegator;

    public PhysicalTaskExecutorQueryClient(PhysicalTaskEngineDelegator physicalTaskEngineDelegator) {
        this.physicalTaskEngineDelegator = physicalTaskEngineDelegator;
    }

    @Override
    public TaskExecutorQueryResponse queryTaskInstances(TaskExecutorQueryRequest request) {
        try {
            List<TaskExecutorDTO> tasks = physicalTaskEngineDelegator.queryTaskExecutors();
            return TaskExecutorQueryResponse.success(tasks);
        } catch (Exception ex) {
            log.error("Query running task instances failed", ex);
            return TaskExecutorQueryResponse.fail(
                    "Query running task instances failed: " + ExceptionUtils.getMessage(ex));
        }
    }

}
