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

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * This strategy will wait the oldest workflow instance.
 */
@Slf4j
@Component
public class SerialCommandWaitHandler extends AbstractSerialCommandHandler {

    @Override
    public void handle(SerialCommandsGroup serialCommandsGroup) {
        // If the first command is not fired, then fire it.
        final List<SerialCommandDto> serialCommands = serialCommandsGroup.getSerialCommands();
        if (CollectionUtils.isEmpty(serialCommands)) {
            return;
        }
        final SerialCommandDto serialCommand = serialCommands.get(0);
        if (serialCommand.getState() == SerialCommandDto.State.WAITING) {
            launchSerialCommand(serialCommand);
            log.info("Launched SerialCommand: {}", serialCommand);
        }
    }

}
