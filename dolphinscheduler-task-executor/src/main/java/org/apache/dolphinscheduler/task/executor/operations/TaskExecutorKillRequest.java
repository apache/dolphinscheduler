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

package org.apache.dolphinscheduler.task.executor.operations;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutorKillRequest implements Serializable {

    /**
     * The timeout of the kill RPC, in milliseconds.
     *
     * <p>The caller of the kill waits until the task is stopped, which is not a single call: the task
     * is cancelled through its client and, when the client is an orchestrator such as the flink SQL
     * client, every job the task submitted is cancelled with its own command. A job is cancelled with
     * a command which has a 30s timeout of its own, so the default RPC timeout of 10s would make the
     * caller give up while the cancellation still runs and hide its result.
     */
    public static final long KILL_TIMEOUT_MILLIS = 5 * 60 * 1000L;

    private Integer taskInstanceId;

    public static TaskExecutorKillRequest of(Integer taskInstanceId) {
        return new TaskExecutorKillRequest(taskInstanceId);
    }

}
