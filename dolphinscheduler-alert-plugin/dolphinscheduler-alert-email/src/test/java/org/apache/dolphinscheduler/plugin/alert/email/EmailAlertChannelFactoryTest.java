package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EmailAlertChannelFactory Tester.
 *
 * @version 1.0
 * @since <pre>Aug 20, 2020</pre>
 */
public class EmailAlertChannelFactoryTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getName()
     */
    @Test
    public void testGetName() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getParams()
     */
    @Test
    public void testGetParams() throws Exception {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        List<PluginParams> params = emailAlertChannelFactory.getParams();
        JSONUtils.toJsonString(params);
        Assert.assertEquals(12, params.size());
    }

    /**
     * Method: create()
     */
    @Test
    public void testCreate() throws Exception {
        EmailAlertChannelFactory emailAlertChannelFactory = new EmailAlertChannelFactory();
        AlertChannel alertChannel = emailAlertChannelFactory.create();
        Assert.assertNotNull(alertChannel);
    }


} 
