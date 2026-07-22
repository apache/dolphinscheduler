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

import org.apache.dolphinscheduler.dao.model.SerialCommandDto;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * This strategy will stop the previous workflow instance and notify the newly workflow instance.
 */
@Slf4j
@Component
public class SerialCommandPriorityHandler extends AbstractSerialCommandHandler {

    @Override
    public void handle(SerialCommandsGroup serialCommandsGroup) {
        final List<SerialCommandDto> serialCommands = serialCommandsGroup.getSerialCommands();
        // Stop the 1 ~ n-1 item
        for (int i = 0; i < serialCommands.size(); i++) {
            final SerialCommandDto serialCommand = serialCommands.get(i);
            if (i == serialCommands.size() - 1) {
                if (serialCommand.getState() == SerialCommandDto.State.WAITING) {
                    launchSerialCommand(serialCommand);
                    log.info("Launched SerialCommand: {}", serialCommand);
                }
                continue;
            }

            if (serialCommand.getState() == SerialCommandDto.State.WAITING) {
                discardSerialCommandAndStopWorkflowInstanceInDB(serialCommand);
                log.info("Discard SerialCommand: {}", serialCommand);
            } else {
                stopWorkflowInstanceInMaster(serialCommand);
                log.info("Stop the pre WorkflowInstance: {} due to the workflow using SERIAL_PRIORITY strategy",
                        serialCommand.getWorkflowInstanceId());
            }
        }
    }
}
