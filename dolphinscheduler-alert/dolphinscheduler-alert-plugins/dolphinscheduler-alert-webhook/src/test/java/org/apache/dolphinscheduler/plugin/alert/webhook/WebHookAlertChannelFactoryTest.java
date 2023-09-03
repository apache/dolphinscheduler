package org.apache.dolphinscheduler.plugin.alert.webhook;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebHookAlertChannelFactoryTest {

    @Test
    public void testGetParams() {
        WebHookAlertChannelFactory webHookAlertChannelFactory = new WebHookAlertChannelFactory();
        List<PluginParams> params = webHookAlertChannelFactory.params();
        JSONUtils.toJsonString(params);
        Assertions.assertEquals(2, params.size());
    }

    @Test
    public void testCreate() {
        WebHookAlertChannelFactory webHookAlertChannelFactory = new WebHookAlertChannelFactory();
        AlertChannel alertChannel = webHookAlertChannelFactory.create();
        Assertions.assertNotNull(alertChannel);
    }
}
