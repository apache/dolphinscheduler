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
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;

import java.io.Serializable;

/**
 * db task final result response command
 */
public class StateEventChangeCommand implements Serializable {

    private String key;

    private ExecutionStatus sourceStatus;

    private int sourceProcessInstanceId;

    private int sourceTaskInstanceId;

    private int destProcessInstanceId;

    private int destTaskInstanceId;

    public StateEventChangeCommand() {
        super();
    }

    public StateEventChangeCommand(int sourceProcessInstanceId, int sourceTaskInstanceId,
                                   ExecutionStatus sourceStatus,
                                   int destProcessInstanceId,
                                   int destTaskInstanceId
    ) {
        this.key = String.format("%d-%d-%d-%d",
                sourceProcessInstanceId,
                sourceTaskInstanceId,
                destProcessInstanceId,
                destTaskInstanceId);

        this.sourceStatus = sourceStatus;
        this.sourceProcessInstanceId = sourceProcessInstanceId;
        this.sourceTaskInstanceId = sourceTaskInstanceId;
        this.destProcessInstanceId = destProcessInstanceId;
        this.destTaskInstanceId = destTaskInstanceId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * package response command
     *
     * @return command
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.STATE_EVENT_REQUEST);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

    @Override
    public String toString() {
        return "StateEventResponseCommand{"
                + "key=" + key
                + '}';
    }

    public ExecutionStatus getSourceStatus() {
        return sourceStatus;
    }

    public void setSourceStatus(ExecutionStatus sourceStatus) {
        this.sourceStatus = sourceStatus;
    }

    public int getSourceProcessInstanceId() {
        return sourceProcessInstanceId;
    }

    public void setSourceProcessInstanceId(int sourceProcessInstanceId) {
        this.sourceProcessInstanceId = sourceProcessInstanceId;
    }

    public int getSourceTaskInstanceId() {
        return sourceTaskInstanceId;
    }

    public void setSourceTaskInstanceId(int sourceTaskInstanceId) {
        this.sourceTaskInstanceId = sourceTaskInstanceId;
    }

    public int getDestProcessInstanceId() {
        return destProcessInstanceId;
    }

    public void setDestProcessInstanceId(int destProcessInstanceId) {
        this.destProcessInstanceId = destProcessInstanceId;
    }

    public int getDestTaskInstanceId() {
        return destTaskInstanceId;
    }

    public void setDestTaskInstanceId(int destTaskInstanceId) {
        this.destTaskInstanceId = destTaskInstanceId;
    }
}
