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

package org.apache.dolphinscheduler.server.master.registry;

import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * server node manager test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ RegistryClient.class })
@PowerMockIgnore({"javax.management.*"})
public class ServerNodeManagerTest {

    private ServerNodeManager serverNodeManager;

    @Mock
    private WorkerGroupMapper workerGroupMapper;

    @Mock
    private AlertDao alertDao;

    @Before
    public void before() {
        PowerMockito.suppress(PowerMockito.constructor(RegistryClient.class));
        serverNodeManager = PowerMockito.mock(ServerNodeManager.class);
    }

    @Test
    public void test(){
        //serverNodeManager.getWorkerGroupNodes()
    }

}
