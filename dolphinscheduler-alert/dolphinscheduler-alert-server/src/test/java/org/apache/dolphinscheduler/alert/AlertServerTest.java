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
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;


@RunWith(MockitoJUnitRunner.class)
public class AlertServerTest extends TestCase {
    
    @InjectMocks
    private AlertServer alertServer;
    
    @Mock
    private PluginDao pluginDao;
    
    @Mock
    private AlertConfig alertConfig;
    
    @Test
    public void testStart() {
        Mockito.when(pluginDao.checkPluginDefineTableExist()).thenReturn(true);
        
        Mockito.when(alertConfig.getPort()).thenReturn(50053);
        
        alertServer.start(null);
    
        NettyRemotingServer nettyRemotingServer = Whitebox.getInternalState(alertServer, "server");
    
        NettyServerConfig nettyServerConfig = Whitebox.getInternalState(nettyRemotingServer, "serverConfig");
        
        Assert.assertEquals(50053, nettyServerConfig.getListenPort());
    }
}
