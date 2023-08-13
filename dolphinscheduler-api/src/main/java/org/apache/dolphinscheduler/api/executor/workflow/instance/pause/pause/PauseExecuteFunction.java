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

package org.apache.dolphinscheduler.api.executor.workflow.instance.pause.pause;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.executor.ExecuteFunction;
import org.apache.dolphinscheduler.api.executor.ExecuteRuntimeException;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStateChangeEvent;

public class PauseExecuteFunction implements ExecuteFunction<PauseExecuteRequest, PauseExecuteResult> {

    private final ProcessInstanceDao processInstanceDao;

    public PauseExecuteFunction(ProcessInstanceDao processInstanceDao) {
        this.processInstanceDao = processInstanceDao;
    }

    @Override
    public PauseExecuteResult execute(PauseExecuteRequest request) throws ExecuteRuntimeException {
        ProcessInstance workflowInstance = request.getWorkflowInstance();
        if (!workflowInstance.getState().isRunning()) {
            throw new ExecuteRuntimeException(
                    String.format("The workflow instance: %s status is %s, can not pause", workflowInstance.getName(),
                            workflowInstance.getState()));
        }
        workflowInstance.setCommandType(CommandType.PAUSE);
        workflowInstance.addHistoryCmd(CommandType.PAUSE);
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.READY_PAUSE,
                CommandType.PAUSE.getDescp() + " by " + request.getExecuteUser().getUserName());

        if (!processInstanceDao.updateById(workflowInstance)) {
            throw new ExecuteRuntimeException(
                    String.format(
                            "The workflow instance: %s pause failed, due to update the workflow instance status in DB failed",
                            workflowInstance.getName()));
        }
        try {
            // todo: direct call the workflow instance pause method
            ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                    SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                            .getProxyClient(workflowInstance.getHost(), ITaskInstanceExecutionEventListener.class);
            iTaskInstanceExecutionEventListener.onWorkflowInstanceInstanceStateChange(
                    new WorkflowInstanceStateChangeEvent(
                            workflowInstance.getId(), 0, workflowInstance.getState(), workflowInstance.getId(), 0));
        } catch (Exception e) {
            throw new ExecuteRuntimeException(
                    String.format(
                            "WorkflowInstance: %s pause failed", workflowInstance.getName()),
                    e);
        }
        return new PauseExecuteResult(workflowInstance);
    }

    @Override
    public ExecuteType getExecuteType() {
        return PauseExecuteFunctionBuilder.EXECUTE_TYPE;
    }
}
