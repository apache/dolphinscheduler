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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NettyServerConfig} and {@link NettyClientConfig} maxFrameSize field.
 */
class NettyConfigTest {

    @Test
    void serverConfig_defaultMaxFrameSize() {
        NettyServerConfig config = NettyServerConfig.builder()
                .serverName("test")
                .listenPort(12345)
                .build();

        assertEquals(64 * 1024 * 1024, config.getMaxFrameSize(),
                "Default maxFrameSize should be 64MB");
    }

    @Test
    void serverConfig_customMaxFrameSize() {
        NettyServerConfig config = NettyServerConfig.builder()
                .serverName("test")
                .listenPort(12345)
                .maxFrameSize(32 * 1024 * 1024)
                .build();

        assertEquals(32 * 1024 * 1024, config.getMaxFrameSize(),
                "Custom maxFrameSize should be 32MB");
    }

    @Test
    void clientConfig_defaultMaxFrameSize() {
        NettyClientConfig config = new NettyClientConfig();

        assertEquals(64 * 1024 * 1024, config.getMaxFrameSize(),
                "Default maxFrameSize should be 64MB");
    }

    @Test
    void clientConfig_customMaxFrameSize() {
        NettyClientConfig config = NettyClientConfig.builder()
                .maxFrameSize(128 * 1024 * 1024)
                .build();

        assertEquals(128 * 1024 * 1024, config.getMaxFrameSize(),
                "Custom maxFrameSize should be 128MB");
    }
}
