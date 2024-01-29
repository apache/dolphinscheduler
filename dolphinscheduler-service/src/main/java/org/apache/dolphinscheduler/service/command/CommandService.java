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

package org.apache.dolphinscheduler.service.command;

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.List;

/**
 * Command Service
 */
public interface CommandService {

    /**
     * Save error command, and delete original command. If the given command has already been moved into error command,
     * will throw {@link java.sql.SQLIntegrityConstraintViolationException ).
     * @param command command
     * @param message message
     */
    void moveToErrorCommand(Command command, String message);

    /**
     * Create new command
     * @param command command
     * @return result
     */
    int createCommand(Command command);

    /**
     * Get command page
     * @param pageSize page size
     * @param masterCount master count
     * @param thisMasterSlot master slot
     * @return command page
     */
    List<Command> findCommandPageBySlot(int pageSize, int masterCount, int thisMasterSlot);

    /**
     * check the input command exists in queue list
     *
     * @param command command
     * @return create command result
     */
    boolean verifyIsNeedCreateCommand(Command command);

    /**
     * create recovery waiting thread command when thread pool is not enough for the process instance.
     * sub work process instance need not create recovery command.
     * create recovery waiting thread  command and delete origin command at the same time.
     * if the recovery command is exists, only update the field update_time
     *
     * @param originCommand   originCommand
     * @param processInstance processInstance
     */
    void createRecoveryWaitingThreadCommand(Command originCommand, ProcessInstance processInstance);

    /**
     * create sub work process command
     * @param parentProcessInstance parent process instance
     * @param childInstance child process instance
     * @param instanceMap process instance map
     * @param task task instance
     * @return command
     */
    Command createSubProcessCommand(ProcessInstance parentProcessInstance,
                                    ProcessInstance childInstance,
                                    ProcessInstanceMap instanceMap,
                                    TaskInstance task);
}
