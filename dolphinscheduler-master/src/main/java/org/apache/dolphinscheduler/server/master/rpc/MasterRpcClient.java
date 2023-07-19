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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterRpcClient implements AutoCloseable {

    @Autowired
    private MasterConfig masterConfig;

    private static final long DEFAULT_TIME_OUT_MILLS = 10_000L;
    private NettyRemotingClient client;

    public void start() {
        client = new NettyRemotingClient(masterConfig.getMasterRpcClientConfig());
        log.info("Success initialized MasterRPCClient...");
    }

    public Message sendSyncCommand(@NonNull Host host,
                                   @NonNull Message rpcMessage) throws RemotingException, InterruptedException {
        return client.sendSync(host, rpcMessage, DEFAULT_TIME_OUT_MILLS);
    }

    public void send(@NonNull Host host, @NonNull Message message) throws RemotingException {
        client.send(host, message);
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }

}
