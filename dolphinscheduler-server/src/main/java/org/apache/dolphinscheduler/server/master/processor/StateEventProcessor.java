package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.processor.queue.StateEventResponseService;
import org.apache.dolphinscheduler.server.master.runner.StateEvent;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * 处理master/api发送过来的stateEvent事件
 * 1. 处理完需要返回成功消息
 * 2. 缓存处理
 */
public class StateEventProcessor implements NettyRequestProcessor {


    private final Logger logger = LoggerFactory.getLogger(StateEventProcessor.class);

    private StateEventResponseService stateEventResponseService;

    public StateEventProcessor(){
        stateEventResponseService = SpringApplicationContext.getBean(StateEventResponseService.class);
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.STATE_EVENT_REQUEST == command.getType(), String.format("invalid command type: %s", command.getType()));


        StateEventChangeCommand stateEventChangeCommand = JSONUtils.parseObject(command.getBody(), StateEventChangeCommand.class);
        StateEvent  stateEvent = new StateEvent();
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        stateEvent.setKey(stateEventChangeCommand.getKey());
        stateEvent.setProcessInstanceId(stateEventChangeCommand.getDestProcessInstanceId());
        stateEvent.setTaskInstanceId(stateEventChangeCommand.getDestTaskInstanceId());
        String type = stateEvent.getTaskInstanceId() ==0 ? "process":"task";
        stateEvent.setType(type);

        logger.info("received command : {}", stateEvent.toString());
        stateEventResponseService.addResponse(stateEvent);
    }
}
