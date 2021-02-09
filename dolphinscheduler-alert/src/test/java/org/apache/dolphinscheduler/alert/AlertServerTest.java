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

package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.alert.runner.AlertSender;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AlertServer.class, DaoFactory.class})
public class AlertServerTest {

    @Before
    public void before() {

    }

    @Test
    public void testMain() throws Exception {
        AlertDao alertDao = PowerMockito.mock(AlertDao.class);
        PowerMockito.mockStatic(DaoFactory.class);
        PowerMockito.when(DaoFactory.getDaoInstance(AlertDao.class)).thenReturn(alertDao);

        PluginDao pluginDao = PowerMockito.mock(PluginDao.class);
        PowerMockito.when(DaoFactory.getDaoInstance(PluginDao.class)).thenReturn(pluginDao);

        AlertChannel alertChannelMock = PowerMockito.mock(AlertChannel.class);

        AlertPluginManager alertPluginManager = PowerMockito.mock(AlertPluginManager.class);
        PowerMockito.whenNew(AlertPluginManager.class).withNoArguments().thenReturn(alertPluginManager);
        ConcurrentHashMap alertChannelMap = new ConcurrentHashMap<>();
        alertChannelMap.put("pluginName", alertChannelMock);
        PowerMockito.when(alertPluginManager.getPluginNameById(Mockito.anyInt())).thenReturn("pluginName");
        PowerMockito.when(alertPluginManager.getAlertChannelMap()).thenReturn(alertChannelMap);

        DolphinPluginManagerConfig alertPluginManagerConfig = PowerMockito.mock(DolphinPluginManagerConfig.class);
        PowerMockito.whenNew(DolphinPluginManagerConfig.class).withNoArguments().thenReturn(alertPluginManagerConfig);

        NettyRemotingServer nettyRemotingServer = PowerMockito.mock(NettyRemotingServer.class);
        PowerMockito.whenNew(NettyRemotingServer.class).withAnyArguments().thenReturn(nettyRemotingServer);
        AlertSender alertSender = PowerMockito.mock(AlertSender.class);
        PowerMockito.whenNew(AlertSender.class).withAnyArguments().thenReturn(alertSender);

        DolphinPluginLoader dolphinPluginLoader = PowerMockito.mock(DolphinPluginLoader.class);
        PowerMockito.whenNew(DolphinPluginLoader.class).withAnyArguments().thenReturn(dolphinPluginLoader);

        AlertServer alertServer = AlertServer.getInstance();
        Assert.assertNotNull(alertServer);

        new Thread(() -> {
            alertServer.start();
        })
                .start();

        Thread.sleep(5 * Constants.ALERT_SCAN_INTERVAL);

        alertServer.stop();

    }

}
