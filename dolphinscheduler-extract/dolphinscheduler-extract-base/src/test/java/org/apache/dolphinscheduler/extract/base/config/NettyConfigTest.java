package org.apache.dolphinscheduler.extract.base.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
