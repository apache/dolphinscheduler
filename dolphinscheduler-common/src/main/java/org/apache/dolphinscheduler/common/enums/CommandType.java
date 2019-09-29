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

/**
 * command types
 */
public enum CommandType {

    /**
     * command types
     * 0 start a new process
     * 1 start a new process from current nodes
     * 2  recover tolerance fault work flow
     * 3 start process from paused task nodes
     * 4 start process from failure task nodes
     * 5 complement data
     * 6 start a new process from scheduler
     * 7 repeat running a work flow
     * 8 pause a process
     * 9 stop a process
     * 10 recover waiting thread
     */
    START_PROCESS, START_CURRENT_TASK_PROCESS, RECOVER_TOLERANCE_FAULT_PROCESS, RECOVER_SUSPENDED_PROCESS,
    START_FAILURE_TASK_PROCESS,COMPLEMENT_DATA,SCHEDULER, REPEAT_RUNNING,PAUSE,STOP,RECOVER_WAITTING_THREAD;
}
