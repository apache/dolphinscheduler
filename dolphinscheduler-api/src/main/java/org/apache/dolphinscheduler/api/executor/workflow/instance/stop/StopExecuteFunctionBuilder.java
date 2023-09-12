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

package org.apache.dolphinscheduler.api.executor.workflow.instance.stop;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.executor.ExecuteContext;
import org.apache.dolphinscheduler.api.executor.ExecuteFunction;
import org.apache.dolphinscheduler.api.executor.ExecuteFunctionBuilder;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopExecuteFunctionBuilder implements ExecuteFunctionBuilder<StopRequest, StopResult> {

    public static final ExecuteType EXECUTE_TYPE = ExecuteType.STOP;

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Override
    public CompletableFuture<ExecuteFunction<StopRequest, StopResult>> createWorkflowInstanceExecuteFunction(ExecuteContext executeContext) {
        return CompletableFuture.completedFuture(new StopExecuteFunction(processInstanceDao));
    }

    @Override
    public CompletableFuture<StopRequest> createWorkflowInstanceExecuteRequest(ExecuteContext executeContext) {
        return CompletableFuture.completedFuture(new StopRequest(executeContext.getWorkflowInstance()));
    }

    @Override
    public ExecuteType getExecuteType() {
        return EXECUTE_TYPE;
    }
}
