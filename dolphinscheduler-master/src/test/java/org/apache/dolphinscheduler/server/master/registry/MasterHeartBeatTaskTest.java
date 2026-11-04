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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.model.MasterHeartBeat;
import org.apache.dolphinscheduler.meter.metrics.MetricsProvider;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.config.MasterServerLoadProtection;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;

import org.junit.jupiter.api.Test;

class MasterHeartBeatTaskTest {

    @Test
    void samplesLocallyAndChecksFailoverWithoutWritingRuntimeMetadata() {
        MasterConfig config = new MasterConfig();
        RegistryClient registry = mock(RegistryClient.class);
        MetricsProvider metrics = mock(MetricsProvider.class);
        when(metrics.getSystemMetrics()).thenReturn(new SystemMetrics());
        MasterHeartBeatTask task = new MasterHeartBeatTask(config, mock(MasterServerLoadProtection.class), metrics,
                registry, mock(MasterCoordinator.class));
        MasterHeartBeat snapshot = task.getLatestHeartBeat();
        assertSame(snapshot, task.getLatestHeartBeat());
        task.writeHeartBeat(snapshot);
        verify(registry).exists(anyString());
        verify(registry, never()).persistEphemeral(anyString(), anyString());
        IStoppable stoppable = mock(IStoppable.class);
        when(registry.exists(anyString())).thenReturn(true);
        when(registry.getStoppable()).thenReturn(stoppable);
        task.writeHeartBeat(snapshot);
        verify(stoppable).stop(anyString());
    }
}
