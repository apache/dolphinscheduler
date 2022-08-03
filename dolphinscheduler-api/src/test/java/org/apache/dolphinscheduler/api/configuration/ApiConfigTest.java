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

package org.apache.dolphinscheduler.api.configuration;

import java.time.Duration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("api")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiConfig.class)
public class ApiConfigTest {

    @Autowired
    private ApiConfig apiConfig;

    @Test
    public void apiConfigParamTest() {
        Assert.assertFalse(apiConfig.isRegistryEnabled());
        Assert.assertEquals(7890, apiConfig.getListenPort());
        Assert.assertEquals(Duration.ofSeconds(10), apiConfig.getHeartbeatInterval());
        Assert.assertEquals(5, apiConfig.getHeartbeatErrorThreshold());
        Assert.assertEquals(-1, apiConfig.getMaxCpuLoadAvg());
        Assert.assertEquals(10, apiConfig.getExecThreads());
        Assert.assertTrue(0.3 == apiConfig.getReservedMemory());
    }
}
