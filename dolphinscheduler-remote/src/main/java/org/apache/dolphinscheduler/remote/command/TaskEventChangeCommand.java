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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.Serializable;

/**
 * db task final result response command
 */
@Data
@NoArgsConstructor
public class TaskEventChangeCommand implements Serializable {

    private String key;

    private int processInstanceId;

    private int taskInstanceId;

    public TaskEventChangeCommand(
                                  int processInstanceId,
                                  int taskInstanceId) {
        this.key = String.format("%d-%d",
                processInstanceId,
                taskInstanceId);

        this.processInstanceId = processInstanceId;
        this.taskInstanceId = taskInstanceId;
    }

    /**
     * package response command
     *
     * @return command
     */
    public Command convert2Command(CommandType commandType) {
        Command command = new Command();
        command.setType(commandType);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

}
