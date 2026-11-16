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

package org.apache.dolphinscheduler.server.master.engine.workflow.serial;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.metrics.WorkflowInstanceMetrics;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
public abstract class AbstractSerialCommandHandler implements ISerialCommandHandler {

    @Autowired
    protected WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    protected IWorkflowControlClient workflowControlClient;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected MasterConfig masterConfig;

    @Autowired
    protected CommandDao commandDao;

    @Autowired
    protected SerialCommandDao serialCommandDao;

    protected boolean launchSerialCommand(SerialCommandDto serialCommand) {
        final boolean launched = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            // Claim before inserting so two WAITING snapshots cannot create two Commands.
            if (!serialCommandDao.launchSerialCommand(serialCommand.getId(), DateUtils.getCurrentDate())) {
                log.info("Skip launching SerialCommand: {}, it is no longer in WAITING state", serialCommand.getId());
                return false;
            }
            if (commandDao.insert(serialCommand.getCommand()) != 1) {
                throw new IllegalStateException("Failed to insert Command for SerialCommand: " + serialCommand.getId());
            }
            return true;
        }));
        if (launched) {
            // Keep the snapshot unchanged if the transaction rolls back.
            serialCommand.setState(SerialCommandDto.State.LAUNCHED);
        }
        return launched;
    }

    protected boolean discardSerialCommandAndStopWorkflowInstanceInDB(SerialCommandDto serialCommand) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            // A stale WAITING snapshot must not discard a Command another coordinator has launched.
            if (!serialCommandDao.deleteByIdAndState(serialCommand.getId(), SerialCommandDto.State.WAITING)) {
                log.info("Skip discarding SerialCommand: {}, it is no longer in WAITING state", serialCommand.getId());
                return false;
            }

            // todo: call api to stop the workflow instance
            final Integer workflowInstanceId = serialCommand.getWorkflowInstanceId();
            final WorkflowInstance workflowInstance = workflowInstanceDao.queryById(workflowInstanceId);
            workflowInstance.setEndTime(DateUtils.getCurrentDate());
            workflowInstance.setState(WorkflowExecutionStatus.STOP);
            if (!workflowInstanceDao.updateById(workflowInstance)) {
                throw new IllegalStateException("Failed to stop WorkflowInstance: " + workflowInstanceId);
            }
            WorkflowInstanceMetrics.recordWorkflowInstanceFinish(
                    WorkflowExecutionStatus.STOP,
                    workflowInstance.getWorkflowDefinitionCode());
            return true;
        }));
    }

    protected void stopWorkflowInstanceInMaster(SerialCommandDto serialCommand) {

        // todo: directly call master api
        // todo: call api to stop the workflow instance
        // todo: We might need to set a status discarding in serial command. then we can avoid duplicate stop
        // workflow instance
        final Integer workflowInstanceId = serialCommand.getWorkflowInstanceId();
        final WorkflowInstance workflowInstance = workflowInstanceDao.queryById(workflowInstanceId);
        if (!workflowInstance.getState().isCanStop()) {
            return;
        }

        final String workflowInstanceLaunchedHost = workflowInstance.getHost();
        final WorkflowInstanceStopRequest stopRequest = new WorkflowInstanceStopRequest(workflowInstanceId);
        if (masterConfig.getMasterAddress().equals(workflowInstanceLaunchedHost)) {
            workflowControlClient.stopWorkflowInstance(stopRequest);
        } else {
            Clients.withService(IWorkflowControlClient.class)
                    .withHost(workflowInstanceLaunchedHost)
                    .stopWorkflowInstance(stopRequest);
        }
    }
}
