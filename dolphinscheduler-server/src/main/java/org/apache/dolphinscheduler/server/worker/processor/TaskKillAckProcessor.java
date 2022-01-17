package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskKillAckCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.cache.ResponceCache;
import org.apache.dolphinscheduler.spi.task.TaskExecutionContextCacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class TaskKillAckProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskKillAckProcessor.class);

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_KILL_RESPONSE_ACK == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskKillAckCommand taskKillAckCommand = JSONUtils.parseObject(
                command.getBody(), TaskKillAckCommand.class);

        if (taskKillAckCommand == null) {
            return;
        }

        if (taskKillAckCommand.getStatus() == ExecutionStatus.SUCCESS.getCode()) {
            ResponceCache.get().removeKillResponseCache(taskKillAckCommand.getTaskInstanceId());
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskKillAckCommand.getTaskInstanceId());
            logger.debug("removeKillResponseCache: taskinstance id:{}", taskKillAckCommand.getTaskInstanceId());
            TaskCallbackService.remove(taskKillAckCommand.getTaskInstanceId());
            logger.debug("remove REMOTE_CHANNELS, task instance id:{}", taskKillAckCommand.getTaskInstanceId());
        }
    }
}
