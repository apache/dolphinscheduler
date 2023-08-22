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

package org.apache.dolphinscheduler.api.rpc;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.listener.ListenerResponse;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;

import org.springframework.stereotype.Component;

@Component
public class ApiRpcClient {

    private final NettyRemotingClient nettyRemotingClient;

    private static final long DEFAULT_TIME_OUT_MILLS = 60_000L;

    public ApiRpcClient() {
        this.nettyRemotingClient = new NettyRemotingClient(new NettyClientConfig());
    }

    public void send(Host host, Message message) throws RemotingException {
        nettyRemotingClient.send(host, message);
    }

    public ListenerResponse sendListenerMessageSync(Host host,
                                                    Message message) throws RemotingException, InterruptedException {
        Message resMessage = nettyRemotingClient.sendSync(host, message, DEFAULT_TIME_OUT_MILLS);
        return JSONUtils.parseObject(resMessage.getBody(), ListenerResponse.class);
    }

}
