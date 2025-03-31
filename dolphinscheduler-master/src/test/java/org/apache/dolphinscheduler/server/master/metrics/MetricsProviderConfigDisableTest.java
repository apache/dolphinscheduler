package org.apache.dolphinscheduler.server.master.metrics;

import org.apache.dolphinscheduler.meter.metrics.EmptyMetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.server.master.MasterServer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {MasterServer.class})
@TestPropertySource(properties = "metrics.enabled=false")
public class MetricsProviderConfigDisableTest {

    @Autowired
    private MetricsProvider metricsProvider;

    @Test
    public void testDisableMetrics() {
        Assertions.assertInstanceOf(EmptyMetricsProvider.class, metricsProvider);
    }
}
