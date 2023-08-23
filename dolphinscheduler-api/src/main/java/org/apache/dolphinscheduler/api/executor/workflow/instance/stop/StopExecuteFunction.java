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
import org.apache.dolphinscheduler.api.executor.ExecuteFunction;
import org.apache.dolphinscheduler.api.executor.ExecuteRuntimeException;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStateChangeEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopExecuteFunction implements ExecuteFunction<StopRequest, StopResult> {

    private final ProcessInstanceDao processInstanceDao;

    public StopExecuteFunction(ProcessInstanceDao processInstanceDao) {
        this.processInstanceDao = processInstanceDao;
    }

    @Override
    public StopResult execute(StopRequest request) throws ExecuteRuntimeException {
        ProcessInstance workflowInstance = request.getWorkflowInstance();

        if (!workflowInstance.getState().canStop()
                || workflowInstance.getState() == WorkflowExecutionStatus.READY_STOP) {
            throw new ExecuteRuntimeException(
                    String.format("The workflow instance: %s status is %s, can not be stopped",
                            workflowInstance.getName(), workflowInstance.getState()));
        }
        // update the workflow instance's status to stop
        workflowInstance.setCommandType(CommandType.STOP);
        workflowInstance.addHistoryCmd(CommandType.STOP);
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.READY_STOP, CommandType.STOP.getDescp() + " by user");
        if (processInstanceDao.updateById(workflowInstance)) {
            log.info("Workflow instance {} ready to stop success, will call master to stop the workflow instance",
                    workflowInstance.getName());
            try {
                // todo: direct call the workflow instance stop method
                ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                        SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                                .getProxyClient(workflowInstance.getHost(), ITaskInstanceExecutionEventListener.class);
                iTaskInstanceExecutionEventListener.onWorkflowInstanceInstanceStateChange(
                        new WorkflowInstanceStateChangeEvent(
                                workflowInstance.getId(), 0, workflowInstance.getState(), workflowInstance.getId(), 0));
            } catch (Exception e) {
                throw new ExecuteRuntimeException(
                        String.format("WorkflowInstance: %s stop failed", workflowInstance.getName()), e);
            }
            // todo: use async and inject the completeFuture in the result.
            return new StopResult(workflowInstance);
        }
        throw new ExecuteRuntimeException(
                "Workflow instance stop failed, due to update the workflow instance status failed");
    }

    @Override
    public ExecuteType getExecuteType() {
        return StopExecuteFunctionBuilder.EXECUTE_TYPE;
    }

}
