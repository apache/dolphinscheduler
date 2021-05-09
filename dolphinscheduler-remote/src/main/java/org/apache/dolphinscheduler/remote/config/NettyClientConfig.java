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

package org.apache.dolphinscheduler.remote.config;

import org.apache.dolphinscheduler.remote.utils.Constants;

/**
 * netty client config
 */
public class NettyClientConfig {

    /**
     * worker threads，default get machine cpus
     */
    private int workerThreads = Constants.CPUS;

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
     * connect timeout millis
     */
    private int connectTimeoutMillis = 3000;

    /**
     * see {@link io.netty.handler.timeout.IdleStateHandler}
     */
    private long readerIdleTime = 1000 * 6;

    /**
     * see {@link io.netty.handler.timeout.IdleStateHandler}
     */
    private long writerIdleTime = 0;

    /**
     * see {@link io.netty.handler.timeout.IdleStateHandler}
     */
    private long allIdleTime = 0;

    public int getWorkerThreads() {
        return workerThreads;
    }

    public NettyClientConfig setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public NettyClientConfig setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    public boolean isSoKeepalive() {
        return soKeepalive;
    }

    public NettyClientConfig setSoKeepalive(boolean soKeepalive) {
        this.soKeepalive = soKeepalive;
        return this;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public NettyClientConfig setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
        return this;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public NettyClientConfig setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public NettyClientConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public NettyClientConfig setReaderIdleTime(long readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
        return this;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public NettyClientConfig setWriterIdleTime(long writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
        return this;
    }

    public long getAllIdleTime() {
        return allIdleTime;
    }

    public NettyClientConfig setAllIdleTime(long allIdleTime) {
        this.allIdleTime = allIdleTime;
        return this;
    }
}
