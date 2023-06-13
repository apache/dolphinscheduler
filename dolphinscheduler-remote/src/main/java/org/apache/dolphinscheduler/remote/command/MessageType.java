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

public enum MessageType {

    RESPONSE,

    GET_APP_ID_REQUEST,

    REMOVE_TAK_LOG_REQUEST,

    ROLL_VIEW_LOG_REQUEST,

    VIEW_WHOLE_LOG_REQUEST,

    GET_LOG_BYTES_REQUEST,

    /**
     * task execute start, from api to master
     */
    TASK_EXECUTE_START,

    TASK_DISPATCH_REQUEST,
    TASK_DISPATCH_RESPONSE,

    /**
     * task execute running, from worker to master
     */
    TASK_EXECUTE_RUNNING_MESSAGE,

    /**
     * task execute running ack, from master to worker
     */
    TASK_EXECUTE_RUNNING_MESSAGE_ACK,

    /**
     * task execute response, from worker to master
     */
    TASK_EXECUTE_RESULT_MESSAGE,

    /**
     * task execute response ack, from master to worker
     */
    TASK_EXECUTE_RESULT_MESSAGE_ACK,

    TASK_KILL_REQUEST,

    /**
     * task savepoint, for stream task
     */
    TASK_SAVEPOINT_REQUEST,

    HEART_BEAT,

    PING,

    PONG,

    ALERT_SEND_REQUEST,

    WORKFLOW_HOST_CHANGE_REQUEST,

    /**
     * state event request
     */
    STATE_EVENT_REQUEST,
    /**
     * cache expire
     */
    CACHE_EXPIRE,
    /**
     * task state event request
     */
    TASK_FORCE_STATE_EVENT_REQUEST,
    /**
     * task state event request
     */
    TASK_WAKEUP_EVENT_REQUEST,

    /**
     * workflow executing data request, from api to master
     */
    WORKFLOW_EXECUTING_DATA_REQUEST,

    /**
     * update taskInstance's PID request
     */
    TASK_UPDATE_RUNTIME_MESSAGE,

    /**
     * update taskInstance's PID response ack, from master to worker
     */
    TASK_UPDATE_RUNTIME_MESSAGE_ACK,

    WORKFLOW_METRICS_CLEANUP,

    PAUSE_TASK_INSTANCE,
    ;

}
