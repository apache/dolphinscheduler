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

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * command types
 */
@Getter
public enum CommandType {

    /**
     * command types
     * 0 start a new process
     * 1 start a new process from current nodes
     * 2 recover tolerance fault work flow
     * 3 start process from paused task nodes
     * 4 start process from failure task nodes
     * 5 complement data
     * 6 start a new process from scheduler
     * 7 repeat running a work flow
     * 8 pause a process
     * 9 stop a process
     * 10 recover waiting thread
     */
    START_PROCESS(0, "start a new process"),
    START_CURRENT_TASK_PROCESS(1, "start a new process from current nodes"),
    RECOVER_TOLERANCE_FAULT_PROCESS(2, "recover tolerance fault work flow"),
    RECOVER_SUSPENDED_PROCESS(3, "start process from paused task nodes"),
    START_FAILURE_TASK_PROCESS(4, "start a new process"),
    COMPLEMENT_DATA(5, "complement data"),
    SCHEDULER(6, "start a new process from scheduler"),
    REPEAT_RUNNING(7, "start a new process"),
    PAUSE(8, "start a new process"),
    STOP(9, "start a new process"),
    RECOVER_WAITTING_THREAD(10, "start a new process");

    CommandType(int code, String descp){
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;
}
