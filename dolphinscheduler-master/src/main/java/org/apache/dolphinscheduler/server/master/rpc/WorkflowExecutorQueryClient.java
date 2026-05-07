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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.extract.master.IWorkflowExecutorQueryClient;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowExecutorQueryRequest;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowExecutorQueryResponse;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEngine;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowExecutorQueryClient implements IWorkflowExecutorQueryClient {

    private final WorkflowEngine workflowEngine;

    public WorkflowExecutorQueryClient(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @Override
    public WorkflowExecutorQueryResponse queryWorkflowExecutors(WorkflowExecutorQueryRequest request) {
        try {
            return WorkflowExecutorQueryResponse.success(workflowEngine.queryWorkflowExecutors());
        } catch (Exception ex) {
            log.error("Query running workflow instances failed", ex);
            return WorkflowExecutorQueryResponse.fail(
                    "Query running workflow instances failed: " + ExceptionUtils.getMessage(ex));
        }
    }

}
