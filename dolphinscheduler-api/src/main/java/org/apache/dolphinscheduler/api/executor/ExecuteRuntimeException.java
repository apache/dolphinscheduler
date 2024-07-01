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

// todo: implement from DolphinSchedulerRuntimeException
public class ExecuteRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String EXECUTE_WORKFLOW_INSTANCE_ERROR =
            "Execute workflow instance %s failed, execute type is %s";

    public ExecuteRuntimeException(String message) {
        super(message);
    }

    public ExecuteRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ExecuteRuntimeException executeWorkflowInstanceError(ExecuteContext executeContext) {
        return executeWorkflowInstanceError(executeContext, null);
    }

    public static ExecuteRuntimeException executeWorkflowInstanceError(ExecuteContext executeContext, Throwable cause) {
        return new ExecuteRuntimeException(
                String.format(EXECUTE_WORKFLOW_INSTANCE_ERROR, executeContext.getWorkflowInstance().getName(),
                        executeContext.getExecuteType()),
                cause);
    }
}
