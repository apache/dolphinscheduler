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

import static org.awaitility.Awaitility.await;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    void close_shouldOnlyPurgeClientsOnceWhenCalledConcurrently() throws Exception {
        CountDownLatch firstPurgeStarted = new CountDownLatch(1);
        CountDownLatch allowFirstPurgeToFinish = new CountDownLatch(1);
        AtomicInteger purgeInvocations = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            if (purgeInvocations.incrementAndGet() == 1) {
                firstPurgeStarted.countDown();
                allowFirstPurgeToFinish.await(5, TimeUnit.SECONDS);
            }
            return null;
        }).when(jdbcRegistryClientRepository).deleteByIds(Mockito.any());
        ExecutorService closeExecutor = Executors.newFixedThreadPool(2);
        Future<?> firstClose = closeExecutor.submit(jdbcRegistryServer::close);
        Future<?> secondClose = null;

        try {
            Truth.assertThat(firstPurgeStarted.await(5, TimeUnit.SECONDS)).isTrue();
            secondClose = closeExecutor.submit(jdbcRegistryServer::close);
            secondClose.get(5, TimeUnit.SECONDS);

            Truth.assertThat(purgeInvocations.get()).isEqualTo(1);
        } finally {
            allowFirstPurgeToFinish.countDown();
            firstClose.get(5, TimeUnit.SECONDS);
            if (secondClose != null) {
                secondClose.get(5, TimeUnit.SECONDS);
            }
            closeExecutor.shutdownNow();
        }
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refreshClientsHeartbeat_shouldStopNotifyingWhenListenerClosesServer(boolean heartbeatSucceeds) {
        setServerState(JdbcRegistryServerState.SUSPENDED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", 0L);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenReturn(heartbeatSucceeds);
        if (heartbeatSucceeds) {
            Mockito.doAnswer(invocation -> {
                jdbcRegistryServer.close();
                return null;
            }).when(connectionStateListener).onReconnected();
        } else {
            Mockito.doAnswer(invocation -> {
                jdbcRegistryServer.close();
                return null;
            }).when(connectionStateListener).onDisConnected();
        }
        ConnectionStateListener laterListener = Mockito.mock(ConnectionStateListener.class);
        jdbcRegistryServer.subscribeConnectionStateChange(laterListener);

        ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.STOPPED);
        Mockito.verifyNoInteractions(laterListener);
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
    void refreshClientsHeartbeat_shouldNotDisconnectWhenCloseWinsRace() throws Exception {
        setServerState(JdbcRegistryServerState.STARTED);
        CountDownLatch heartbeatUpdateStarted = new CountDownLatch(1);
        CountDownLatch allowHeartbeatUpdateToFinish = new CountDownLatch(1);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenAnswer(invocation -> {
            heartbeatUpdateStarted.countDown();
            allowHeartbeatUpdateToFinish.await(5, TimeUnit.SECONDS);
            return false;
        });
        ExecutorService heartbeatExecutor = Executors.newSingleThreadExecutor();
        Future<?> heartbeatFuture = heartbeatExecutor.submit(() -> {
            ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");
        });

        try {
            Truth.assertThat(heartbeatUpdateStarted.await(5, TimeUnit.SECONDS)).isTrue();
            jdbcRegistryServer.close();
            allowHeartbeatUpdateToFinish.countDown();
            heartbeatFuture.get(5, TimeUnit.SECONDS);
        } finally {
            allowHeartbeatUpdateToFinish.countDown();
            heartbeatExecutor.shutdownNow();
        }

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.STOPPED);
        Mockito.verify(connectionStateListener, Mockito.never()).onDisConnected();
    }

    @Test
    void refreshClientsHeartbeat_shouldNotReconnectWhenCloseWinsSuccessfulHeartbeatRace() throws Exception {
        setServerState(JdbcRegistryServerState.SUSPENDED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", 42L);
        CountDownLatch heartbeatUpdateStarted = new CountDownLatch(1);
        CountDownLatch allowHeartbeatUpdateToFinish = new CountDownLatch(1);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenAnswer(invocation -> {
            heartbeatUpdateStarted.countDown();
            allowHeartbeatUpdateToFinish.await(5, TimeUnit.SECONDS);
            return true;
        });
        ExecutorService heartbeatExecutor = Executors.newSingleThreadExecutor();
        Future<?> heartbeatFuture = heartbeatExecutor.submit(() -> {
            ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");
        });

        try {
            Truth.assertThat(heartbeatUpdateStarted.await(5, TimeUnit.SECONDS)).isTrue();
            jdbcRegistryServer.close();
            allowHeartbeatUpdateToFinish.countDown();
            heartbeatFuture.get(5, TimeUnit.SECONDS);
        } finally {
            allowHeartbeatUpdateToFinish.countDown();
            heartbeatExecutor.shutdownNow();
        }

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.STOPPED);
        Truth.assertThat((long) ReflectionTestUtils.getField(jdbcRegistryServer, "lastSuccessHeartbeat"))
                .isEqualTo(42L);
        Mockito.verify(connectionStateListener, Mockito.never()).onReconnected();
        Mockito.verify(connectionStateListener, Mockito.never()).onDisConnected();
    }

    @Test
    void refreshClientsHeartbeat_shouldNotSuspendWhenCloseWinsFailedHeartbeatRace() throws Exception {
        setServerState(JdbcRegistryServerState.STARTED);
        CountDownLatch heartbeatUpdateStarted = new CountDownLatch(1);
        CountDownLatch allowHeartbeatUpdateToFail = new CountDownLatch(1);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenAnswer(invocation -> {
            heartbeatUpdateStarted.countDown();
            allowHeartbeatUpdateToFail.await(5, TimeUnit.SECONDS);
            throw new RuntimeException("Heartbeat update failed");
        });
        ExecutorService heartbeatExecutor = Executors.newSingleThreadExecutor();
        Future<?> heartbeatFuture = heartbeatExecutor.submit(() -> {
            ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat");
        });

        try {
            Truth.assertThat(heartbeatUpdateStarted.await(5, TimeUnit.SECONDS)).isTrue();
            jdbcRegistryServer.close();
            allowHeartbeatUpdateToFail.countDown();
            heartbeatFuture.get(5, TimeUnit.SECONDS);
        } finally {
            allowHeartbeatUpdateToFail.countDown();
            heartbeatExecutor.shutdownNow();
        }

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.STOPPED);
        Mockito.verify(connectionStateListener, Mockito.never()).onReconnected();
        Mockito.verify(connectionStateListener, Mockito.never()).onDisConnected();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refreshClientsHeartbeat_shouldPreserveCloseWhenFailureTransitionLosesRace(boolean timedOut) throws Exception {
        setServerState(JdbcRegistryServerState.STARTED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat",
                timedOut ? 0L : System.currentTimeMillis());
        JdbcRegistryProperties properties = Mockito.spy((JdbcRegistryProperties) ReflectionTestUtils
                .getField(jdbcRegistryServer, "jdbcRegistryProperties"));
        ReflectionTestUtils.setField(jdbcRegistryServer, "jdbcRegistryProperties", properties);
        CountDownLatch timeoutCheckStarted = new CountDownLatch(1);
        CountDownLatch allowTimeoutCheck = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            timeoutCheckStarted.countDown();
            Truth.assertThat(allowTimeoutCheck.await(5, TimeUnit.SECONDS)).isTrue();
            return timedOut ? Duration.ZERO : Duration.ofDays(1);
        }).when(properties).getSessionTimeout();
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenReturn(false);
        ExecutorService heartbeatExecutor = Executors.newSingleThreadExecutor();
        Future<?> heartbeatFuture = heartbeatExecutor
                .submit(() -> ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat"));

        try {
            Truth.assertThat(timeoutCheckStarted.await(5, TimeUnit.SECONDS)).isTrue();
            jdbcRegistryServer.close();
            allowTimeoutCheck.countDown();
            heartbeatFuture.get(5, TimeUnit.SECONDS);
        } finally {
            allowTimeoutCheck.countDown();
            heartbeatExecutor.shutdownNow();
        }

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.STOPPED);
        Mockito.verifyNoInteractions(connectionStateListener);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refreshClientsHeartbeat_shouldNotNotifyWhenClosedAfterStateTransition(boolean heartbeatSucceeds) throws Exception {
        setServerState(JdbcRegistryServerState.SUSPENDED);
        ReflectionTestUtils.setField(jdbcRegistryServer, "lastSuccessHeartbeat", 42L);
        Mockito.when(jdbcRegistryClientRepository.updateById(Mockito.any())).thenReturn(heartbeatSucceeds);
        ExecutorService heartbeatExecutor = Executors.newSingleThreadExecutor();
        Future<?> heartbeatFuture;
        try {
            synchronized (jdbcRegistryServer) {
                heartbeatFuture = heartbeatExecutor
                        .submit(() -> ReflectionTestUtils.invokeMethod(jdbcRegistryServer, "refreshClientsHeartbeat"));
                // Pause notification after the CAS, then let close() win before the heartbeat commits its effects.
                await().atMost(Duration.ofSeconds(5))
                        .until(() -> jdbcRegistryServer
                                .getServerState() == (heartbeatSucceeds ? JdbcRegistryServerState.STARTED
                                        : JdbcRegistryServerState.DISCONNECTED));
                jdbcRegistryServer.close();
            }
            heartbeatFuture.get(5, TimeUnit.SECONDS);
        } finally {
            heartbeatExecutor.shutdownNow();
        }

        Truth.assertThat(jdbcRegistryServer.getServerState()).isEqualTo(JdbcRegistryServerState.STOPPED);
        Truth.assertThat((long) ReflectionTestUtils.getField(jdbcRegistryServer, "lastSuccessHeartbeat"))
                .isEqualTo(42L);
        Mockito.verifyNoInteractions(connectionStateListener);
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
