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

package org.apache.dolphinscheduler.server.master.engine.command;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.server.master.engine.command.handler.ReRunWorkflowCommandHandler;
import org.apache.dolphinscheduler.server.master.engine.command.handler.RecoverFailureTaskCommandHandler;
import org.apache.dolphinscheduler.server.master.engine.command.handler.RunWorkflowCommandHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnable;

/**
 * The interface represent the handler used to handle the {@link Command}.
 * <p> Each handler should handle a specific type of command.
 *
 * @see RunWorkflowCommandHandler
 * @see ReRunWorkflowCommandHandler
 * @see RecoverFailureTaskCommandHandler
 */
public interface ICommandHandler {

    /**
     * Handle the command and return the WorkflowExecutionRunnable.
     */
    WorkflowExecutionRunnable handleCommand(final Command command);

    /**
     * The type of the command which should be handled by this handler.
     */
    CommandType commandType();

}
