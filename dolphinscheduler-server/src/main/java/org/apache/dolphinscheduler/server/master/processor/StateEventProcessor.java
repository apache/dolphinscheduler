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

package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * handle state event received from master/api
 */
public class StateEventProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(StateEventProcessor.class);

    private StateEventResponseService stateEventResponseService;

    public StateEventProcessor() {
        stateEventResponseService = SpringApplicationContext.getBean(StateEventResponseService.class);
    }

    public void init(ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceExecMaps) {
        this.stateEventResponseService.init(processInstanceExecMaps);
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.STATE_EVENT_REQUEST == command.getType(), String.format("invalid command type: %s", command.getType()));

        StateEventChangeCommand stateEventChangeCommand = JSONUtils.parseObject(command.getBody(), StateEventChangeCommand.class);
        StateEvent stateEvent = new StateEvent();
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        stateEvent.setKey(stateEventChangeCommand.getKey());
        stateEvent.setProcessInstanceId(stateEventChangeCommand.getDestProcessInstanceId());
        stateEvent.setTaskInstanceId(stateEventChangeCommand.getDestTaskInstanceId());
        StateEventType type = stateEvent.getTaskInstanceId() == 0 ? StateEventType.PROCESS_STATE_CHANGE : StateEventType.TASK_STATE_CHANGE;
        stateEvent.setType(type);

        logger.info("received command : {}", stateEvent.toString());
        stateEventResponseService.addResponse(stateEvent);
    }

}
