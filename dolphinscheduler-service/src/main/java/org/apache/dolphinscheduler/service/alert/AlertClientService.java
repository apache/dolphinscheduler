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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequest;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponse;
import org.apache.dolphinscheduler.remote.factory.NettyRemotingClientFactory;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlertClientService implements AutoCloseable {

    private final NettyRemotingClient client;

    private final AtomicBoolean isRunning;

    private String host;

    private int port;

    /**
     * request time out
     */
    private static final long ALERT_REQUEST_TIMEOUT = 10 * 1000L;

    /**
     * alert client
     */
    public AlertClientService() {
        this.client = NettyRemotingClientFactory.buildNettyRemotingClient();
        this.isRunning = new AtomicBoolean(true);
    }

    /**
     * close
     */
    @Override
    public void close() {
        if (isRunning.compareAndSet(true, false)) {
            log.warn("Alert client is already closed");
            return;
        }

        log.info("Alter client closing");
        this.client.close();
        log.info("Alter client closed");
    }

    /**
     * alert sync send data
     * @param groupId
     * @param title
     * @param content
     * @return
     */
    public AlertSendResponse sendAlert(int groupId, String title, String content, int strategy) {
        return this.sendAlert(this.host, this.port, groupId, title, content, strategy);
    }

    /**
     * alert sync send data
     * @param host host
     * @param port port
     * @param groupId groupId
     * @param title title
     * @param content content
     * @return AlertSendResponseCommand
     */
    public AlertSendResponse sendAlert(String host, int port, int groupId, String title, String content,
                                       int strategy) {
        log.info("sync alert send, host : {}, port : {}, groupId : {}, title : {} , strategy : {} ", host, port,
                groupId, title, strategy);
        AlertSendRequest request = new AlertSendRequest(groupId, title, content, strategy);
        final Host address = new Host(host, port);
        try {
            Message message = request.convert2Command();
            Message response = this.client.sendSync(address, message, ALERT_REQUEST_TIMEOUT);
            if (response != null) {
                return JsonSerializer.deserialize(response.getBody(), AlertSendResponse.class);
            }
        } catch (Exception e) {
            log.error("sync alert send error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return null;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
