package org.apache.dolphinscheduler.plugin.alert.webhook;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.alert.wechat.WeChatAlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class WebHookAlertChannelFactoryTest {
    @Test
    public void testGetParams() {
        WebHookAlertChannelFactory webHookAlertChannelFactory = new WebHookAlertChannelFactory();
        List<PluginParams> params = webHookAlertChannelFactory.params();
        JSONUtils.toJsonString(params);
        Assert.assertEquals(2, params.size());
    }

    @Test
    public void testCreate() {
        WebHookAlertChannelFactory webHookAlertChannelFactory = new WebHookAlertChannelFactory();
        AlertChannel alertChannel = webHookAlertChannelFactory.create();
        Assert.assertNotNull(alertChannel);
    }
}
