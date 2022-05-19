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

import junit.framework.TestCase;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class AlertServerTest extends TestCase {
    
    @InjectMocks
    private AlertServer alertServer;
    
    @Mock
    private PluginDao pluginDao;
    
    @Mock
    private AlertConfig alertConfig;

    @Mock
    private AlertSenderService alertSenderService;
    
    @Test
    public void testStart() {

        Mockito.when(pluginDao.checkPluginDefineTableExist()).thenReturn(true);

        Mockito.when(alertConfig.getPort()).thenReturn(50052);

        Mockito.doNothing().when(alertSenderService).start();

        alertServer.run(null);
    
        NettyRemotingServer nettyRemotingServer = Whitebox.getInternalState(alertServer, "nettyRemotingServer");
    
        NettyServerConfig nettyServerConfig = Whitebox.getInternalState(nettyRemotingServer, "serverConfig");
        
        Assert.assertEquals(50052, nettyServerConfig.getListenPort());

    }
}
