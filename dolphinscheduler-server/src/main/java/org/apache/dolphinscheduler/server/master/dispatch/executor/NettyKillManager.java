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

package org.apache.dolphinscheduler.server.master.dispatch.executor;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.KillTaskRequestCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.processor.TaskKillResponseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *  netty executor manager
 */
@Service
public class NettyKillManager extends AbstractExecutorManager<Boolean>{

    private final Logger logger = LoggerFactory.getLogger(NettyKillManager.class);
    /**
     * netty remote client
     */
    private final NettyRemotingClient nettyRemotingClient;

    public NettyKillManager(){
        final NettyClientConfig clientConfig = new NettyClientConfig();
        this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
        /**
         * register KILL_TASK_RESPONSE command type TaskKillResponseProcessor
         */
        this.nettyRemotingClient.registerProcessor(CommandType.KILL_TASK_RESPONSE, new TaskKillResponseProcessor());
    }

    /**
     * execute logic
     *
     * @param context context
     * @return result
     * @throws ExecuteException
     */
    @Override
    public Boolean execute(ExecutionContext context) throws ExecuteException {
        Host host = context.getHost();
        Command command = buildCommand(context);
        try {
            doExecute(host, command);
            return true;
        }catch (ExecuteException ex) {
            logger.error(String.format("execute context : %s error", context.getContext()), ex);
            return false;
        }
    }


    private Command buildCommand(ExecutionContext context) {
        KillTaskRequestCommand requestCommand = new KillTaskRequestCommand();
        TaskExecutionContext taskExecutionContext = context.getContext();

        requestCommand.setTaskExecutionContext(FastJsonSerializer.serializeToString(taskExecutionContext));
        return requestCommand.convert2Command();
    }

    /**
     *  execute logic
     * @param host host
     * @param command command
     * @throws ExecuteException
     */
    private void doExecute(final Host host, final Command command) throws ExecuteException {
        /**
         * retry count，default retry 3
         */
        int retryCount = 3;
        boolean success = false;
        do {
            try {
                nettyRemotingClient.send(host, command);
                success = true;
            } catch (Exception ex) {
                logger.error(String.format("send command : %s to %s error", command, host), ex);
                retryCount--;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {}
            }
        } while (retryCount >= 0 && !success);

        if (!success) {
            throw new ExecuteException(String.format("send command : %s to %s error", command, host));
        }
    }
}
