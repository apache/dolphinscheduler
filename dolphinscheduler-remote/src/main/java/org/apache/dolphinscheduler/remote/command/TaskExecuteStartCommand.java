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

package org.apache.dolphinscheduler.remote.command;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Task execute start message, from api to master.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskExecuteStartCommand extends BaseCommand {

    private int executorId;

    private String executorName;

    private long projectCode;

    private long taskDefinitionCode;

    private int taskDefinitionVersion;

    private int warningGroupId;

    private String workerGroup;

    private Long environmentCode;

    private Map<String, String> startParams;

    private int tenantId;

    private int dryRun;

    public TaskExecuteStartCommand(String messageSenderAddress, String messageReceiverAddress, long messageSendTime) {
        super(messageSenderAddress, messageReceiverAddress, messageSendTime);
    }

    /**
     * package request command
     *
     * @return command
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.TASK_EXECUTE_START);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

}
