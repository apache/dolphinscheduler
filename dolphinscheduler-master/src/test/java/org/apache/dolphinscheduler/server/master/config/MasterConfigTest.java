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

package org.apache.dolphinscheduler.server.master.config;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureMockMvc
@SpringBootTest(classes = MasterConfig.class)
public class MasterConfigTest {

    @Autowired
    private MasterConfig masterConfig;

    @Test
    public void getMasterDispatchTaskNumber() {
        int masterDispatchTaskNumber = masterConfig.getDispatchTaskNumber();
        assertEquals(30, masterDispatchTaskNumber);
    }

    @Test
    public void getServerLoadProtection() {
        MasterServerLoadProtection serverLoadProtection = masterConfig.getServerLoadProtection();
        assertTrue(serverLoadProtection.isEnabled());
        assertEquals(0.77, serverLoadProtection.getMaxSystemCpuUsagePercentageThresholds());
        assertEquals(0.77, serverLoadProtection.getMaxJvmCpuUsagePercentageThresholds());
        assertEquals(0.77, serverLoadProtection.getMaxJvmCpuUsagePercentageThresholds());
        assertEquals(0.77, serverLoadProtection.getMaxSystemMemoryUsagePercentageThresholds());
        assertEquals(0.77, serverLoadProtection.getMaxDiskUsagePercentageThresholds());
    }

    @Test
    public void getCommandFetchStrategy() {
        CommandFetchStrategy commandFetchStrategy = masterConfig.getCommandFetchStrategy();
        assertThat(commandFetchStrategy.getType())
                .isEqualTo(CommandFetchStrategy.CommandFetchStrategyType.ID_SLOT_BASED);

        CommandFetchStrategy.IdSlotBasedFetchConfig idSlotBasedFetchConfig =
                (CommandFetchStrategy.IdSlotBasedFetchConfig) commandFetchStrategy.getConfig();
        assertThat(idSlotBasedFetchConfig.getIdStep()).isEqualTo(3);
        assertThat(idSlotBasedFetchConfig.getFetchSize()).isEqualTo(11);
    }
}
