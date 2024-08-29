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

package org.apache.dolphinscheduler.common.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import com.baomidou.mybatisplus.annotation.EnumValue;

// todo: rename to WorkflowTriggerType
@Getter
public enum CommandType {

    /**
     * command types
     * 0 start a new workflow
     * 1 start a new workflow from current nodes
     * 2 recover tolerance fault workflow
     * 3 recover suspended workflow
     * 4 start workflow from failure task nodes
     * 5 complement data
     * 6 start a new workflow from scheduler
     * 7 repeat running a workflow
     * 8 pause a workflow
     * 9 stop a workflow
     * 10 recover waiting thread
     * 11 recover serial wait
     * 12 start a task node in a workflow instance
     */
    /**
     * Start the workflow definition, will generate a new workflow instance and start from the StartNodeList, if StartNodeList is empty will start from the beginning tasks.
     */
    START_PROCESS(0, "start a new workflow"),
    /**
     * todo: remove this command, this command doesn't used?
     */
    START_CURRENT_TASK_PROCESS(1, "start a new workflow from current nodes"),
    /**
     * Recover the workflow instance from tolerance fault, these may happened when the master is crashed.
     * Will recover the workflow instance from the last running task node.
     */
    RECOVER_TOLERANCE_FAULT_PROCESS(2, "recover fault tolerance workflow instance"),
    /**
     * Recover the workflow instance from pause status, will start from the paused and unTriggered task instance.
     */
    RECOVER_SUSPENDED_PROCESS(3, "Recover suspended workflow instance"),
    /**
     * Recover the workflow instance from failure task nodes, will start from the failed task nodes.
     * In fact this command has the same logic with RECOVER_SUSPENDED_WORKFLOW.
     */
    START_FAILURE_TASK_PROCESS(4, "Recover workflow instance from failure tasks"),
    /**
     * Backfill the workflow, will use complementScheduleDateList to generate the workflow instance.
     */
    COMPLEMENT_DATA(5, "complement data"),
    /**
     * Start workflow from scheduler, will generate a new workflow instance and start from the beginning tasks.
     * This command is same with START_PROCESS but with different trigger source.
     */
    SCHEDULER(6, "start a new workflow from scheduler"),
    /**
     * Repeat running a workflow instance, will mark the history task instances' flag to no and start from the beginning tasks.
     */
    REPEAT_RUNNING(7, "repeat running a workflow"),
    /**
     * Pause a workflow instance, will pause the running tasks, but not all tasks will be paused.
     */
    PAUSE(8, "pause a workflow"),
    /**
     * Stop a workflow instance, will kill the running tasks.
     */
    STOP(9, "stop a workflow"),
    /**
     * Recover from the serial-wait state.
     * todo: We may need to remove these command, and use the workflow instance origin command type when notify from serial wait.
     */
    RECOVER_SERIAL_WAIT(11, "recover serial wait"),
    /**
     * Trigger the workflow instance from the given StartNodeList, will mark the task instance which is behind the given StartNodeList flag to no
     * and retrigger the task instances.
     */
    EXECUTE_TASK(12, "start a task node in a workflow instance"),
    /**
     * Used in dynamic logic task instance.
     */
    DYNAMIC_GENERATION(13, "dynamic generation"),
    ;

    CommandType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

    private static final Map<Integer, CommandType> COMMAND_TYPE_MAP = new HashMap<>();

    static {
        for (CommandType commandType : CommandType.values()) {
            COMMAND_TYPE_MAP.put(commandType.code, commandType);
        }
    }

    public static CommandType of(Integer status) {
        if (COMMAND_TYPE_MAP.containsKey(status)) {
            return COMMAND_TYPE_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid status : " + status);
    }
}
