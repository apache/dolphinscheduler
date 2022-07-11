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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.processor.NettyRemoteChannel;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * task callback service
 */
@Service
public class TaskCallbackService {

    private final Logger logger = LoggerFactory.getLogger(TaskCallbackService.class);
    private static final int[] RETRY_BACKOFF = {1, 2, 3, 5, 10, 20, 40, 100, 100, 100, 100, 200, 200, 200};

    @Autowired
    private TaskExecuteRunningAckProcessor taskExecuteRunningProcessor;

    @Autowired
    private TaskExecuteResultAckProcessor taskExecuteResultAckProcessor;

    /**
     * remote channels
     */
    private static final ConcurrentHashMap<Integer, NettyRemoteChannel> REMOTE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * netty remoting client
     */
    private final NettyRemotingClient nettyRemotingClient;

    public TaskCallbackService() {
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_EXECUTE_RUNNING_ACK, taskExecuteRunningProcessor);
        this.nettyRemotingClient.registerProcessor(CommandType.TASK_EXECUTE_RESULT_ACK, taskExecuteResultAckProcessor);
    }

    //    /**
    //     * add callback channel
    //     *
    //     * @param taskInstanceId taskInstanceId
    //     * @param channel channel
    //     */
    //    public void addRemoteChannel(int taskInstanceId, NettyRemoteChannel channel) {
    //        REMOTE_CHANNELS.put(taskInstanceId, channel);
    //    }
    //
    //    /**
    //     * change remote channel
    //     */
    //    public void changeRemoteChannel(int taskInstanceId, NettyRemoteChannel channel) {
    //        REMOTE_CHANNELS.put(taskInstanceId, channel);
    //    }
    //
    //    /**
    //     * get callback channel
    //     *
    //     * @param taskInstanceId taskInstanceId
    //     * @return callback channel
    //     */
    //    private Optional<NettyRemoteChannel> getRemoteChannel(int taskInstanceId) {
    //        Channel newChannel;
    //        NettyRemoteChannel nettyRemoteChannel = REMOTE_CHANNELS.get(taskInstanceId);
    //        if (nettyRemoteChannel != null) {
    //            if (nettyRemoteChannel.isActive()) {
    //                return Optional.of(nettyRemoteChannel);
    //            }
    //            newChannel = nettyRemotingClient.getChannel(nettyRemoteChannel.getHost());
    //            if (newChannel != null) {
    //                return Optional.of(getRemoteChannel(newChannel, nettyRemoteChannel.getOpaque(), taskInstanceId));
    //            }
    //        }
    //        return Optional.empty();
    //    }
    //
    //    public long pause(int ntries) {
    //        return SLEEP_TIME_MILLIS * RETRY_BACKOFF[ntries % RETRY_BACKOFF.length];
    //    }
    //
    //    private NettyRemoteChannel getRemoteChannel(Channel newChannel, long opaque, int taskInstanceId) {
    //        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel, opaque);
    //        addRemoteChannel(taskInstanceId, remoteChannel);
    //        return remoteChannel;
    //    }
    //
    //    private NettyRemoteChannel getRemoteChannel(Channel newChannel, int taskInstanceId) {
    //        NettyRemoteChannel remoteChannel = new NettyRemoteChannel(newChannel);
    //        addRemoteChannel(taskInstanceId, remoteChannel);
    //        return remoteChannel;
    //    }
    //
    //    /**
    //     * remove callback channels
    //     *
    //     * @param taskInstanceId taskInstanceId
    //     */
    //    public static void remove(int taskInstanceId) {
    //        REMOTE_CHANNELS.remove(taskInstanceId);
    //    }
    //
    //    /**
    //     * send result
    //     *
    //     * @param taskInstanceId taskInstanceId
    //     * @param command command
    //     */
    //    public void send(int taskInstanceId, Command command) {
    //        Optional<NettyRemoteChannel> nettyRemoteChannel = getRemoteChannel(taskInstanceId);
    //        if (nettyRemoteChannel.isPresent()) {
    //            nettyRemoteChannel.get().writeAndFlush(command).addListener(new ChannelFutureListener() {
    //                @Override
    //                public void operationComplete(ChannelFuture future) {
    //                    if (!future.isSuccess()) {
    //                        logger.error("Send callback command error, taskInstanceId: {}, command: {}", taskInstanceId, command);
    //                    }
    //                }
    //            });
    //        } else {
    //            logger.warn("Remote channel of taskInstanceId is null: {}, cannot send command: {}", taskInstanceId, command);
    //        }
    //    }
    //
    //    /**
    //     * build task execute running command
    //     *
    //     * @param taskExecutionContext taskExecutionContext
    //     * @return TaskExecuteAckCommand
    //     */
    //    private TaskExecuteRunningMessage buildTaskExecuteRunningCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskExecuteRunningMessage command = new TaskExecuteRunningMessage();
    //        command.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
    //        command.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
    //        command.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
    //        command.setLogPath(taskExecutionContext.getLogPath());
    //        command.setHost(taskExecutionContext.getHost());
    //        command.setStartTime(taskExecutionContext.getStartTime());
    //        command.setExecutePath(taskExecutionContext.getExecutePath());
    //        return command;
    //    }
    //
    //    /**
    //     * build task execute response command
    //     *
    //     * @param taskExecutionContext taskExecutionContext
    //     * @return TaskExecuteResponseCommand
    //     */
    //    private TaskExecuteResultMessage buildTaskExecuteResponseCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskExecuteResultMessage command = new TaskExecuteResultMessage();
    //        command.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
    //        command.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
    //        command.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
    //        command.setLogPath(taskExecutionContext.getLogPath());
    //        command.setExecutePath(taskExecutionContext.getExecutePath());
    //        command.setAppIds(taskExecutionContext.getAppIds());
    //        command.setProcessId(taskExecutionContext.getProcessId());
    //        command.setHost(taskExecutionContext.getHost());
    //        command.setStartTime(taskExecutionContext.getStartTime());
    //        command.setEndTime(taskExecutionContext.getEndTime());
    //        command.setVarPool(taskExecutionContext.getVarPool());
    //        command.setExecutePath(taskExecutionContext.getExecutePath());
    //        return command;
    //    }
    //
    //    /**
    //     * build TaskKillResponseCommand
    //     *
    //     * @param taskExecutionContext taskExecutionContext
    //     * @return build TaskKillResponseCommand
    //     */
    //    private TaskKillResponseCommand buildKillTaskResponseCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskKillResponseCommand taskKillResponseCommand = new TaskKillResponseCommand();
    //        taskKillResponseCommand.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
    //        taskKillResponseCommand.setAppIds(Arrays.asList(taskExecutionContext.getAppIds().split(TaskConstants.COMMA)));
    //        taskKillResponseCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
    //        taskKillResponseCommand.setHost(taskExecutionContext.getHost());
    //        taskKillResponseCommand.setProcessId(taskExecutionContext.getProcessId());
    //        return taskKillResponseCommand;
    //    }
    //
    //    private TaskRejectMessage buildRecallCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskRejectMessage taskRejectMessage = new TaskRejectMessage();
    //        taskRejectMessage.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
    //        taskRejectMessage.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
    //        taskRejectMessage.setHost(taskExecutionContext.getHost());
    //        return taskRejectMessage;
    //    }
    //
    //    /**
    //     * send task execute running command
    //     * todo unified callback command
    //     */
    //    public void sendTaskExecuteRunningCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskExecuteRunningMessage command = buildTaskExecuteRunningCommand(taskExecutionContext);
    //        // add response cache
    //        ResponseCache.get().cache(taskExecutionContext.getTaskInstanceId(), command.convert2Command(), TaskEventType.RUNNING);
    //        send(taskExecutionContext.getTaskInstanceId(), command.convert2Command());
    //    }
    //
    //    /**
    //     * send task execute delay command
    //     * todo unified callback command
    //     */
    //    public void sendTaskExecuteDelayCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskExecuteRunningMessage command = buildTaskExecuteRunningCommand(taskExecutionContext);
    //        send(taskExecutionContext.getTaskInstanceId(), command.convert2Command());
    //    }
    //
    //    /**
    //     * send task execute response command
    //     * todo unified callback command
    //     */
    //    public void sendTaskExecuteResponseCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskExecuteResultMessage command = buildTaskExecuteResponseCommand(taskExecutionContext);
    //        // add response cache
    //        ResponseCache.get().cache(taskExecutionContext.getTaskInstanceId(), command.convert2Command(), TaskEventType.RESULT);
    //        send(taskExecutionContext.getTaskInstanceId(), command.convert2Command());
    //    }
    //
    //    public void sendTaskKillResponseCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskKillResponseCommand taskKillResponseCommand = buildKillTaskResponseCommand(taskExecutionContext);
    //        send(taskExecutionContext.getTaskInstanceId(), taskKillResponseCommand.convert2Command());
    //    }
    //
    //    /**
    //     * send task execute response command
    //     */
    //    public void sendRecallCommand(TaskExecutionContext taskExecutionContext) {
    //        TaskRejectMessage taskRejectMessage = buildRecallCommand(taskExecutionContext);
    //        ResponseCache.get().cache(taskExecutionContext.getTaskInstanceId(), taskRejectMessage.convert2Command(), TaskEventType.WORKER_REJECT);
    //        send(taskExecutionContext.getTaskInstanceId(), taskRejectMessage.convert2Command());
    //    }
}
