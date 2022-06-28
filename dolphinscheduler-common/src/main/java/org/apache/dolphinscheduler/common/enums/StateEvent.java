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

import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * state event
 */
@Data
public class StateEvent {

    /**
     * origin_pid-origin_task_id-process_instance_id-task_instance_id
     */
    private String key;

    private StateEventType type;

    private ExecutionStatus executionStatus;

    private int taskInstanceId;

    private long taskCode;

    private int processInstanceId;

    private String context;

    private Channel channel;

}
