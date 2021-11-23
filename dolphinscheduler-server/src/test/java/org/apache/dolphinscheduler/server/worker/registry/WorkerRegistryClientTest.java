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

package org.apache.dolphinscheduler.server.worker.registry;

import static org.mockito.BDDMockito.given;

import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * worker registry test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class WorkerRegistryClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerRegistryClientTest.class);

    private static final String TEST_WORKER_GROUP = "test";

    @InjectMocks
    private WorkerRegistryClient workerRegistryClient;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private WorkerConfig workerConfig;

    @Mock
    private Set<String> workerGroups = Sets.newHashSet("127.0.0.1");

    @Mock
    private ScheduledExecutorService heartBeatExecutor;

    //private static final Set<String> workerGroups;

    static {
        // workerGroups = Sets.newHashSet(DEFAULT_WORKER_GROUP, TEST_WORKER_GROUP);
    }

    @Before
    public void before() {
        given(workerConfig.getGroups()).willReturn(Sets.newHashSet("127.0.0.1"));
        //given(heartBeatExecutor.getWorkerGroups()).willReturn(Sets.newHashSet("127.0.0.1"));
        //scheduleAtFixedRate
        given(heartBeatExecutor.scheduleAtFixedRate(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class))).willReturn(null);

    }

    @Test
    public void testRegistry() {
        //workerRegistryClient.initWorkRegistry();
        //Set<String> workerGroups = Sets.newHashSet("127.0.0.1");
        //workerRegistryClient.registry();
       // workerRegistryClient.handleDeadServer();

    }

    @Test
    public void testUnRegistry() {

    }

    @Test
    public void testGetWorkerZkPaths() {

    }
}
