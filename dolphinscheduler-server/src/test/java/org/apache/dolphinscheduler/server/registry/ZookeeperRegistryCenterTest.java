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

package org.apache.dolphinscheduler.server.registry;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.service.zk.RegisterOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * zookeeper registry center test
 */
@RunWith(MockitoJUnitRunner.class)
public class ZookeeperRegistryCenterTest {

    @InjectMocks
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Mock
    protected RegisterOperator registerOperator;

    @Mock
    private ZookeeperConfig zookeeperConfig;

    private static final String DS_ROOT = "/dolphinscheduler";

    @Test
    public void testGetDeadZNodeParentPath() {
        ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
        zookeeperConfig.setDsRoot(DS_ROOT);
        Mockito.when(registerOperator.getZookeeperConfig()).thenReturn(zookeeperConfig);

        String deadZNodeParentPath = zookeeperRegistryCenter.getDeadZNodeParentPath();

        Assert.assertEquals(deadZNodeParentPath, DS_ROOT + Constants.ZOOKEEPER_DOLPHINSCHEDULER_DEAD_SERVERS);

    }

}