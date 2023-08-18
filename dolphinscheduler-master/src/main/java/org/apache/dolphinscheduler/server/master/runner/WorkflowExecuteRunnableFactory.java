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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.exception.WorkflowCreateException;
import org.apache.dolphinscheduler.server.master.rpc.MasterRpcClient;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnableFactory;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowExecuteRunnableFactory {

    @Autowired
    private CommandService commandService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private MasterRpcClient masterRpcClient;

    @Autowired
    private ProcessAlertManager processAlertManager;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    @Autowired
    private CuringParamsService curingGlobalParamsService;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private DefaultTaskExecuteRunnableFactory defaultTaskExecuteRunnableFactory;

    @Autowired
    private WorkflowExecuteContextFactory workflowExecuteContextFactory;

    public Optional<WorkflowExecuteRunnable> createWorkflowExecuteRunnable(Command command) throws WorkflowCreateException {
        try {
            Optional<IWorkflowExecuteContext> workflowExecuteRunnableContextOptional =
                    workflowExecuteContextFactory.createWorkflowExecuteRunnableContext(command);
            return workflowExecuteRunnableContextOptional.map(iWorkflowExecuteContext -> new WorkflowExecuteRunnable(
                    iWorkflowExecuteContext,
                    commandService,
                    processService,
                    processInstanceDao,
                    masterRpcClient,
                    processAlertManager,
                    masterConfig,
                    stateWheelExecuteThread,
                    curingGlobalParamsService,
                    taskInstanceDao,
                    defaultTaskExecuteRunnableFactory));
        } catch (Exception ex) {
            throw new WorkflowCreateException("Create workflow execute runnable failed", ex);
        }
    }

}
