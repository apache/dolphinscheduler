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

package org.apache.dolphinscheduler.extract.base.config;

import lombok.Data;

@Data
public class NettyServerConfig {

    /**
     * init the server connectable queue
     */
    private int soBacklog = 1024;

    /**
     * whether tpc delay
     */
    private boolean tcpNoDelay = true;

    /**
     * whether keep alive
     */
    private boolean soKeepalive = true;

    /**
     * send buffer size
     */
    private int sendBufferSize = 65535;

    /**
     * receive buffer size
     */
    private int receiveBufferSize = 65535;

    /**
     * worker threads，default get machine cpus
     */
    private int workerThread = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * listen port
     */
    private int listenPort = 12346;

    public NettyServerConfig(int listenPort) {
        this.listenPort = listenPort;
    }
}
