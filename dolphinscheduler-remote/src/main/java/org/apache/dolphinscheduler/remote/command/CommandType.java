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

public enum CommandType {

    /**
     * remove task log request,
     */
    REMOVE_TAK_LOG_REQUEST,

    /**
     * remove task log response
     */
    REMOVE_TAK_LOG_RESPONSE,

    /**
     *  roll view log request
     */
    ROLL_VIEW_LOG_REQUEST,

    /**
     *  roll view log response
     */
    ROLL_VIEW_LOG_RESPONSE,

    /**
     * view whole log request
     */
    VIEW_WHOLE_LOG_REQUEST,

    /**
     * view whole log response
     */
    VIEW_WHOLE_LOG_RESPONSE,

    /**
     * get log bytes request
     */
    GET_LOG_BYTES_REQUEST,

    /**
     * get log bytes response
     */
    GET_LOG_BYTES_RESPONSE,


    WORKER_REQUEST,
    MASTER_RESPONSE,

    /**
     * execute task request
     */
    TASK_EXECUTE_REQUEST,

    /**
     * execute task ack
     */
    TASK_EXECUTE_ACK,

    /**
     * execute task response
     */
    TASK_EXECUTE_RESPONSE,

    /**
     * db task ack
     */
    DB_TASK_ACK,

    /**
     * db task response
     */
    DB_TASK_RESPONSE,

    /**
     * kill task
     */
    TASK_KILL_REQUEST,

    /**
     * kill task response
     */
    TASK_KILL_RESPONSE,

    /**
     * HEART_BEAT
     */
    HEART_BEAT,

    /**
     * ping
     */
    PING,

    /**
     *  pong
     */
    PONG;
}
