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

<<<<<<< HEAD
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.junit.jupiter.api.Assertions;
=======
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.factory.NettyRemotingServerFactory;

>>>>>>> refs/remotes/origin/3.1.1-release
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
<<<<<<< HEAD
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;
=======
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
>>>>>>> refs/remotes/origin/3.1.1-release

@ExtendWith(MockitoExtension.class)
public class AlertServerTest {

    @Mock
    private PluginDao pluginDao;

    @Mock
    private AlertConfig alertConfig;

    @Mock
    private AlertSenderService alertSenderService;

<<<<<<< HEAD
    @InjectMocks
=======
    @Mock
    private NettyRemotingServer nettyRemotingServer;

    @InjectMocks
    @Spy
>>>>>>> refs/remotes/origin/3.1.1-release
    private AlertServer alertServer;

    @BeforeEach
    void init() {
        Mockito.lenient().when(pluginDao.checkPluginDefineTableExist()).thenReturn(true);

        Mockito.lenient().when(alertConfig.getPort()).thenReturn(50052);
<<<<<<< HEAD

        Mockito.doNothing().when(alertSenderService).start();
=======
    }

    @Test
    public void alertServerRunSuccessfully() {
        doNothing().when(alertServer).checkTable();
        doNothing().when(alertServer).startServer();
>>>>>>> refs/remotes/origin/3.1.1-release

    }
    @Test
    public void alertServerStartSuccessfully() {

        alertServer.run(null);
<<<<<<< HEAD

        NettyRemotingServer nettyRemotingServer = Whitebox.getInternalState(alertServer, "nettyRemotingServer");

        NettyServerConfig nettyServerConfig = Whitebox.getInternalState(nettyRemotingServer, "serverConfig");

        Assertions.assertEquals(50052, nettyServerConfig.getListenPort());
=======
>>>>>>> refs/remotes/origin/3.1.1-release

        Mockito.verify(alertServer, times(1)).checkTable();
        Mockito.verify(alertServer, times(1)).startServer();
        Mockito.verify(alertSenderService, times(1)).start();
    }

    @Test
    public void alertServerServerStartWithExpectedListeningPort() {
        try (
                MockedStatic<NettyRemotingServerFactory> mockedNettyRemotingServerFactory =
                        mockStatic(NettyRemotingServerFactory.class)) {
            mockedNettyRemotingServerFactory.when(() -> NettyRemotingServerFactory.buildNettyRemotingServer(anyInt()))
                    .thenReturn(nettyRemotingServer);
            alertServer.startServer();
            mockedNettyRemotingServerFactory.verify(() -> NettyRemotingServerFactory.buildNettyRemotingServer(50052));
            verify(nettyRemotingServer, times(1)).registerProcessor(any(), any());
            verify(nettyRemotingServer, times(1)).start();
        }
    }
}
