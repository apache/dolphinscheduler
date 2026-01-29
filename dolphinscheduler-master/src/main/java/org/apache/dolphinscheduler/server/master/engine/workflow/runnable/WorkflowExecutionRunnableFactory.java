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

package org.apache.dolphinscheduler.server.master.engine.workflow.runnable;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.server.master.engine.command.ICommandHandler;
import org.apache.dolphinscheduler.server.master.engine.exceptions.CommandDuplicateHandleException;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class WorkflowExecutionRunnableFactory {

    @Autowired
    private List<ICommandHandler> commandHandlers;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private CommandDao commandDao;

    /**
     * Generate WorkflowExecutionRunnable from command.
     * <p> We use transaction here to make sure that the command will be handled only once. Since in some case if the
     * master cluster is reblancing, the master slot might different in different master.
     */
    @Transactional
    public IWorkflowExecutionRunnable createWorkflowExecuteRunnable(Command command) {
        deleteCommandOrThrow(command);
        return doCreateWorkflowExecutionRunnable(command);
    }

    /**
     * Create WorkflowExecutionRunnable from command.
     * <p> Each WorkflowExecutionRunnable represent a workflow instance, so this method might create workflow instance in db, dependents on the command type.
     */
    private IWorkflowExecutionRunnable doCreateWorkflowExecutionRunnable(Command command) {
        final CommandType commandType = command.getCommandType();
        final ICommandHandler commandHandler = commandHandlers
                .stream()
                .filter(c -> c.commandType() == commandType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find ICommandHandler for commandType: " + commandType));
        return commandHandler.handleCommand(command);
    }

    /**
     * Delete the command in db, if the command is not exist in db will throw CommandDuplicateHandleException
     */
    private void deleteCommandOrThrow(Command command) {
        boolean deleteResult = commandDao.deleteById(command.getId());
        if (!deleteResult) {
            throw new CommandDuplicateHandleException(command);
        }
    }
}
