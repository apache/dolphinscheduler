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

package org.apache.dolphinscheduler.api.executor;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.api.enums.ExecuteType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * This is the main class for executing workflow/workflowInstance/tasks.
 * <pre>
 *     ExecuteContext executeContext = ExecuteContext.builder()
 *         .processInstance(processInstance)
 *         .executeType(...)
 *         .build();
 *     executeClient.execute(executeContext);
 * </pre>
 */
@Component
@SuppressWarnings("unchecked")
public class ExecuteClient {

    private final Map<ExecuteType, ExecuteFunctionBuilder> executorFunctionBuilderMap;

    public ExecuteClient(List<ExecuteFunctionBuilder> executeFunctionBuilders) {
        executorFunctionBuilderMap = executeFunctionBuilders.stream()
                .collect(Collectors.toMap(ExecuteFunctionBuilder::getExecuteType, Function.identity()));
    }

    public ExecuteResult executeWorkflowInstance(ExecuteContext executeContext) throws ExecuteRuntimeException {
        ExecuteFunctionBuilder<ExecuteRequest, ExecuteResult> executeFunctionBuilder = checkNotNull(
                executorFunctionBuilderMap.get(executeContext.getExecuteType()),
                String.format("The executeType: %s is not supported", executeContext.getExecuteType()));

        return executeFunctionBuilder.createWorkflowInstanceExecuteFunction(executeContext)
                .thenCombine(executeFunctionBuilder.createWorkflowInstanceExecuteRequest(executeContext),
                        ExecuteFunction::execute)
                .join();
    }
}
