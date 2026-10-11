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

package org.apache.dolphinscheduler.plugin.registry.jdbc.server;

import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.jdbc.client.IJdbcRegistryClient;
import org.apache.dolphinscheduler.plugin.registry.jdbc.client.JdbcRegistryClientIdentify;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryClientHeartbeatDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryLockDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryClientRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataChangeEventRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryLockRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.truth.Truth;

@ExtendWith(MockitoExtension.class)
class JdbcRegistryServerTest {

    private static final JdbcRegistryClientIdentify CLIENT_IDENTIFY =
            new JdbcRegistryClientIdentify(1L, "test-client");

    @Mock
    private JdbcRegistryDataRepository jdbcRegistryDataRepository;

    @Mock
    private JdbcRegistryLockRepository jdbcRegistryLockRepository;

    @Mock
    private JdbcRegistryClientRepository jdbcRegistryClientRepository;

    @Mock
    private JdbcRegistryDataChangeEventRepository jdbcRegistryDataChangeEventRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private IJdbcRegistryClient jdbcRegistryClient;

    @Mock
    private ConnectionStateListener connectionStateListener;

    private JdbcRegistryServer jdbcRegistryServer;

    @BeforeEach
    void setUp() {
        JdbcRegistryProperties jdbcRegistryProperties = new JdbcRegistryProperties();
        jdbcRegistryProperties.setSessionTimeout(Duration.ofSeconds(1));
        jdbcRegistryServer = new JdbcRegistryServer(
                jdbcRegistryDataRepository,
                jdbcRegistryLockRepository,
                jdbcRegistryClientRepository,
                jdbcRegistryDataChangeEventRepository,
                jdbcRegistryProperties,
                transactionTemplate);
        Mockito.when(jdbcRegistryClient.getJdbcRegistryClientIdentify()).thenReturn(CLIENT_IDENTIFY);
        jdbcRegistryServer.registerClient(jdbcRegistryClient);
        jdbcRegistryServer.subscribeConnectionStateChange(connectionStateListener);
    }

    @AfterEach
    void tearDown() {
        jdbcRegistryServer.close();
    }

    @Test
    void refreshClientsHeartbeat_shouldDisconnectWhenHeartbeatRecordWasPurged() {
        setServerState(JdbcRegistryServerState.SUSPENDED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", 0L);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenReturn(false);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.DISCONNECTED);
        Mockito.verify(connectionStateListener).onDisConnected();
    }

    @Test
    void refreshClientsHeartbeat_shouldDisconnectImmediatelyWhenStartedHeartbeatRecordWasPurgedAfterTimeout() {
        setServerState(JdbcRegistryServerState.STARTED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", 0L);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenReturn(false);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.DISCONNECTED);
        Mockito.verify(jdbcRegistryClientRepository).updateById(Mockito.any());
        Mockito.verify(connectionStateListener).onDisConnected();
    }

    @Test
    void refreshClientsHeartbeat_shouldSuspendUntilMissingHeartbeatTimesOut() {
        setServerState(JdbcRegistryServerState.STARTED);
        long lastSuccessHeartbeat = System.currentTimeMillis();
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", lastSuccessHeartbeat);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenReturn(false);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.SUSPENDED);
        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");
        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.SUSPENDED);
        Truth.assertThat((long) ReflectionTestUtils.getField(jdbcRegistryServer, "lastSuccessHeartbeat"))
                .isEqualTo(lastSuccessHeartbeat);
        Mockito.verify(connectionStateListener, Mockito.never()).onDisConnected();

        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", 0L);
        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");
        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.DISCONNECTED);
        Mockito.verify(connectionStateListener).onDisConnected();
        Mockito.verify(connectionStateListener, Mockito.never()).onReconnected();
        Mockito.verify(jdbcRegistryClientRepository, Mockito.times(3)).updateById(Mockito.any());
    }

    @Test
    void refreshClientsHeartbeat_shouldReconnectOnceAfterTransientFailure() {
        setServerState(JdbcRegistryServerState.STARTED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", System.currentTimeMillis());
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any()))
                .thenThrow(new IllegalStateException("Database unavailable"))
                .thenReturn(true);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");
        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.SUSPENDED);
        Mockito.verifyNoInteractions(connectionStateListener);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");
        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.STARTED);
        Mockito.verify(connectionStateListener).onReconnected();
        Mockito.verify(connectionStateListener, Mockito.never()).onDisConnected();
        Mockito.verify(jdbcRegistryClientRepository, Mockito.times(3)).updateById(Mockito.any());
    }

    @Test
    void refreshClientsHeartbeat_shouldKeepPartialHeartbeatUpdateWhenLaterClientFails() {
        IJdbcRegistryClient secondClient = Mockito.mock(IJdbcRegistryClient.class);
        JdbcRegistryClientIdentify secondClientIdentify = new JdbcRegistryClientIdentify(2L, "second-client");
        Mockito.when(secondClient.getJdbcRegistryClientIdentify()).thenReturn(secondClientIdentify);
        jdbcRegistryServer.registerClient(secondClient);
        setServerState(JdbcRegistryServerState.STARTED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", System.currentTimeMillis());
        AtomicInteger updateInvocations = new AtomicInteger();
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any()))
                .thenAnswer(invocation -> updateInvocations.incrementAndGet() == 1);

        @SuppressWarnings("unchecked")
        Map<JdbcRegistryClientIdentify, JdbcRegistryClientHeartbeatDTO> heartbeatMap =
                (Map<JdbcRegistryClientIdentify, JdbcRegistryClientHeartbeatDTO>) ReflectionTestUtils
                        .getField(jdbcRegistryServer, "jdbcRegistryClientDTOMap");
        heartbeatMap.get(CLIENT_IDENTIFY).setLastHeartbeatTime(0L);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(updateInvocations.get()).isEqualTo(2);
        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.SUSPENDED);
        Truth.assertThat(heartbeatMap.get(CLIENT_IDENTIFY).getLastHeartbeatTime()).isGreaterThan(0L);
        Mockito.verify(connectionStateListener, Mockito.never()).onReconnected();
        Mockito.verify(connectionStateListener, Mockito.never()).onDisConnected();
    }

    @Test
    void refreshClientsHeartbeat_shouldPersistCurrentHeartbeatTimestamp() {
        ArgumentCaptor<JdbcRegistryClientHeartbeatDTO> registeredHeartbeat =
                ArgumentCaptor.forClass(JdbcRegistryClientHeartbeatDTO.class);
        Mockito.verify(jdbcRegistryClientRepository).insert(registeredHeartbeat.capture());
        registeredHeartbeat.getValue().setLastHeartbeatTime(0L);
        AtomicLong persistedHeartbeatTimestamp = new AtomicLong(-1L);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenAnswer(invocation -> {
            JdbcRegistryClientHeartbeatDTO heartbeat = invocation.getArgument(0);
            persistedHeartbeatTimestamp.set(heartbeat.getLastHeartbeatTime());
            return true;
        });

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(persistedHeartbeatTimestamp.get()).isGreaterThan(0L);
    }

    @Test
    void refreshClientsHeartbeat_shouldNotRefreshAfterDisconnected() {
        setServerState(JdbcRegistryServerState.DISCONNECTED);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Mockito.verify(jdbcRegistryClientRepository, Mockito.never()).updateById(Mockito.any());
    }

    @Test
    void purgeInvalidJdbcRegistryMetadata_shouldKeepMetadataWhenHeartbeatWasUpdatedAfterSnapshot() {
        JdbcRegistryClientHeartbeatDTO staleHeartbeat = JdbcRegistryClientHeartbeatDTO.builder()
                .id(CLIENT_IDENTIFY.getClientId())
                .clientName(CLIENT_IDENTIFY.getClientName())
                .lastHeartbeatTime(System.currentTimeMillis() - Duration.ofSeconds(2).toMillis())
                .clientConfig(new JdbcRegistryClientHeartbeatDTO.ClientConfig(Duration.ofSeconds(1).toMillis()))
                .build();
        JdbcRegistryLockDTO clientLock = JdbcRegistryLockDTO.builder()
                .id(1L)
                .clientId(CLIENT_IDENTIFY.getClientId())
                .build();
        Mockito.when(jdbcRegistryClientRepository.queryAll()).thenReturn(Collections.singletonList(staleHeartbeat));
        Mockito.when(jdbcRegistryClientRepository.deleteByIdAndLastHeartbeatTime(
                staleHeartbeat.getId(), staleHeartbeat.getLastHeartbeatTime())).thenReturn(false);
        Mockito.when(jdbcRegistryDataRepository.selectAll()).thenReturn(Collections.emptyList());
        Mockito.when(jdbcRegistryLockRepository.queryAll()).thenReturn(Collections.singletonList(clientLock));

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "purgeInvalidJdbcRegistryMetadata");

        Mockito.verify(jdbcRegistryClientRepository).deleteByIdAndLastHeartbeatTime(
                staleHeartbeat.getId(), staleHeartbeat.getLastHeartbeatTime());
        Mockito.verify(jdbcRegistryLockRepository, Mockito.never()).deleteById(clientLock.getId());
    }

    @SuppressWarnings("unchecked")
    private void setServerState(JdbcRegistryServerState state) {
        ((AtomicReference<JdbcRegistryServerState>) ReflectionTestUtils.getField(jdbcRegistryServer, "serverState"))
                .set(state);
    }
}
