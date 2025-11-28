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

package org.apache.dolphinscheduler.dao.model;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.SerialCommand;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.core.type.TypeReference;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SerialCommandDto {

    private Integer id;

    private Integer workflowInstanceId;

    private Long workflowDefinitionCode;

    private Integer workflowDefinitionVersion;

    private Command command;

    private State state;

    private Date createTime;

    private Date updateTime;

    public static SerialCommandDto newSerialCommand(Command command) {
        return SerialCommandDto.builder()
                .workflowInstanceId(command.getWorkflowInstanceId())
                .command(command)
                .workflowDefinitionCode(command.getWorkflowDefinitionCode())
                .workflowDefinitionVersion(command.getWorkflowDefinitionVersion())
                .state(State.WAITING)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
    }

    public static SerialCommandDto fromEntity(SerialCommand serialCommand) {
        return SerialCommandDto.builder()
                .id(serialCommand.getId())
                .workflowInstanceId(serialCommand.getWorkflowInstanceId())
                .workflowDefinitionCode(serialCommand.getWorkflowDefinitionCode())
                .workflowDefinitionVersion(serialCommand.getWorkflowDefinitionVersion())
                .command(JSONUtils.parseObject(serialCommand.getCommand(), new TypeReference<Command>() {
                }))
                .state(State.of(serialCommand.getState()))
                .createTime(serialCommand.getCreateTime())
                .updateTime(serialCommand.getUpdateTime())
                .build();
    }

    public SerialCommand toEntity() {
        return SerialCommand.builder()
                .id(this.id)
                .workflowInstanceId(this.workflowInstanceId)
                .workflowDefinitionCode(this.workflowDefinitionCode)
                .workflowDefinitionVersion(this.workflowDefinitionVersion)
                .command(JSONUtils.toJsonString(this.command))
                .state(this.state.getValue())
                .createTime(this.createTime)
                .updateTime(this.updateTime)
                .build();
    }

    @Getter
    public enum State {

        // If the workflow instance is finished, then we directly delete the item from the queue
        // so there are no finished state here
        WAITING(0),
        LAUNCHED(1),
        ;
        private final int value;

        State(int value) {
            this.value = value;
        }

        public static State of(int value) {
            for (State state : values()) {
                if (state.value == value) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Invalid State value: " + value);
        }
    }

}
