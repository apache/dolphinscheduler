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

package org.apache.dolphinscheduler.server.master.failover;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.RegistryLock;
import org.apache.dolphinscheduler.registry.api.utils.RegistryUtils;
import org.apache.dolphinscheduler.server.master.cluster.ClusterManager;
import org.apache.dolphinscheduler.server.master.cluster.MasterClusters;
import org.apache.dolphinscheduler.server.master.cluster.MasterServerMetadata;
import org.apache.dolphinscheduler.server.master.cluster.WorkerClusters;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.system.event.GlobalMasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.MasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.WorkerFailoverEvent;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FailoverCoordinatorTest {

    @InjectMocks
    private FailoverCoordinator failoverCoordinator;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private ClusterManager clusterManager;

    @Mock
    private IWorkflowRepository workflowRepository;

    @Mock
    private TaskFailover taskFailover;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private WorkflowFailover workflowFailover;

    @Mock
    private MasterClusters masterClusters;

    @Mock
    private RegistryLock registryLock;

    /**
     * Regression test for issue #18197: doMasterFailover used to acquire the lock at
     * {@code /lock/master-failover/<masterAddress>} but release {@code /lock/master-failover},
     * leaking the per-master lock. The fix uses a single lock-path local + try-with-resources.
     */
    @Test
    void failoverMaster_releasesTheSameLockItAcquired() {
        final String masterAddress = "127.0.0.1:5679";
        final MasterServerMetadata metadata = MasterServerMetadata.builder()
                .processId(1234)
                .serverStartupTime(System.currentTimeMillis())
                .address(masterAddress)
                .build();
        final MasterFailoverEvent event = MasterFailoverEvent.of(metadata.getServerIdentity(), new Date(), 0L);

        when(clusterManager.getMasterClusters()).thenReturn(masterClusters);
        when(masterClusters.getServerIdentity(masterAddress)).thenReturn(Optional.empty());
        when(registryClient.getLock(anyString())).thenReturn(registryLock);
        when(registryClient.exists(anyString())).thenReturn(false);
        when(workflowInstanceDao.queryNeedFailoverWorkflowInstances(masterAddress))
                .thenReturn(Collections.emptyList());

        failoverCoordinator.failoverMaster(event);

        final ArgumentCaptor<String> lockPathCaptor = ArgumentCaptor.forClass(String.class);
        verify(registryClient).getLock(lockPathCaptor.capture());
        assertThat(lockPathCaptor.getValue())
                .isEqualTo(RegistryUtils.getMasterFailoverLockPath(masterAddress));
        verify(registryLock, times(1)).close();
    }
    @Test
    void registeredMasterWithoutHeartbeatSkipsFailover() {
        try (MasterClusters clusters = new MasterClusters()) {
            ServerIdentity identity = MasterServerMetadata.builder().address("localhost:5678")
                    .processId(10).serverStartupTime(1000).build().getServerIdentity();
            clusters.onServerAdded(identity);
            assertThat(clusters.getServer(identity.getAddress())).isEmpty();
            when(clusterManager.getMasterClusters()).thenReturn(clusters);

            failoverCoordinator.failoverMaster(MasterFailoverEvent.of(identity, new Date(), 0));

            verifyNoInteractions(registryClient, workflowInstanceDao, workflowFailover);
        }
    }

    @Test
    void registeredWorkerWithoutHeartbeatSkipsFailover() {
        try (WorkerClusters clusters = new WorkerClusters()) {
            ServerIdentity identity = MasterServerMetadata.builder().address("localhost:1234")
                    .processId(10).serverStartupTime(1000).build().getServerIdentity();
            clusters.onServerAdded(identity);
            assertThat(clusters.getServer(identity.getAddress())).isEmpty();
            when(clusterManager.getWorkerClusters()).thenReturn(clusters);

            failoverCoordinator.failoverWorker(WorkerFailoverEvent.of(identity, new Date(), 0));

            verifyNoInteractions(registryClient, taskFailover);
        }
    }

    @Test
    void globalFailoverUsesRegisteredStartupTimeWithoutHeartbeat() {
        try (MasterClusters clusters = new MasterClusters()) {
            ServerIdentity identity = MasterServerMetadata.builder().address("localhost:5678")
                    .processId(10).serverStartupTime(1000).build().getServerIdentity();
            clusters.onServerAdded(identity);
            when(clusterManager.getMasterClusters()).thenReturn(clusters);
            when(workflowInstanceDao.queryNeedFailoverMasters())
                    .thenReturn(Collections.singletonList(identity.getAddress()));
            when(workflowInstanceDao.queryNeedFailoverWorkflowInstances(identity.getAddress()))
                    .thenReturn(Collections.emptyList());
            when(registryClient.getLock(anyString())).thenReturn(registryLock);

            failoverCoordinator.globalMasterFailover(GlobalMasterFailoverEvent.of(new Date(2000)));

            verify(registryClient).persist(RegistryUtils.getGlobalMasterFailoverNodePath(identity.getAddress()),
                    "1000");
        }
    }

}
