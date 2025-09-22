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

import org.springframework.stereotype.Component;

/**
 * This strategy will discard the new workflow instance if there is a running workflow instance.
 */
@Component
public class SerialCommandDiscardHandler extends AbstractSerialCommandHandler {

    @Override
    public void handle(SerialCommandsGroup serialCommandsGroup) {
        // If the first item in the queue is not running, then notify it to run.
        // Discard all other items in the queue.
        List<SerialCommandDto> serialCommands = serialCommandsGroup.getSerialCommands();
        for (int i = 0; i < serialCommands.size(); i++) {
            SerialCommandDto serialCommandDto = serialCommands.get(i);
            if (i == 0 && serialCommandDto.getState() == SerialCommandDto.State.WAITING) {
                launchSerialCommand(serialCommandDto);
                continue;
            }
            // Discard all other items in the queue.
            if (serialCommandDto.getState() != SerialCommandDto.State.WAITING) {
                throw new IllegalStateException("The post commands must be in waiting state");
            }
            discardSerialCommandAndStopWorkflowInstanceInDB(serialCommandDto);
        }
    }

}
