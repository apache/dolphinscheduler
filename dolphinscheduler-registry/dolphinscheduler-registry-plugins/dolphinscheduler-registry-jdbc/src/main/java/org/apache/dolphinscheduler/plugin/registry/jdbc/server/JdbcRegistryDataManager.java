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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryThreadFactory;
import org.apache.dolphinscheduler.plugin.registry.jdbc.KeyUtils;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataChangeEventDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataChangeEventRepository;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataRepository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

@Slf4j
public class JdbcRegistryDataManager
        implements
            IRegistryRowChangeNotifier<JdbcRegistryDataDTO>,
            IJdbcRegistryDataManager {

    private final Integer keepJdbcRegistryDataChangeEventHours = 2;

    private final JdbcRegistryProperties registryProperties;

    private final JdbcRegistryDataRepository jdbcRegistryDataRepository;

    private final JdbcRegistryDataChangeEventRepository jdbcRegistryDataChangeEventRepository;

    private final TransactionTemplate jdbcRegistryTransactionTemplate;

    private final List<RegistryRowChangeListener<JdbcRegistryDataDTO>> registryRowChangeListeners;

    private long lastDetectedJdbcRegistryDataChangeEventId = -1;

    public JdbcRegistryDataManager(JdbcRegistryProperties registryProperties,
                                   JdbcRegistryDataRepository jdbcRegistryDataRepository,
                                   JdbcRegistryDataChangeEventRepository jdbcRegistryDataChangeEventRepository,
                                   TransactionTemplate jdbcRegistryTransactionTemplate) {
        this.registryProperties = registryProperties;
        this.jdbcRegistryDataChangeEventRepository = jdbcRegistryDataChangeEventRepository;
        this.jdbcRegistryDataRepository = jdbcRegistryDataRepository;
        this.jdbcRegistryTransactionTemplate = jdbcRegistryTransactionTemplate;
        this.registryRowChangeListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void start() {
        this.lastDetectedJdbcRegistryDataChangeEventId =
                jdbcRegistryDataChangeEventRepository.getMaxJdbcRegistryDataChangeEventId();
        JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor().scheduleWithFixedDelay(
                this::detectJdbcRegistryDataChangeEvent,
                registryProperties.getHeartbeatRefreshInterval().toMillis(),
                registryProperties.getHeartbeatRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS);

        JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor().scheduleWithFixedDelay(
                this::purgeHistoryJdbcRegistryDataChangeEvent,
                0,
                Duration.ofHours(keepJdbcRegistryDataChangeEventHours).toHours(),
                TimeUnit.HOURS);
    }

    private void detectJdbcRegistryDataChangeEvent() {
        final List<JdbcRegistryDataChangeEventDTO> jdbcRegistryDataChangeEvents = jdbcRegistryDataChangeEventRepository
                .selectJdbcRegistryDataChangeEventWhereIdAfter(lastDetectedJdbcRegistryDataChangeEventId);
        if (CollectionUtils.isEmpty(jdbcRegistryDataChangeEvents)) {
            return;
        }
        for (JdbcRegistryDataChangeEventDTO jdbcRegistryDataChangeEvent : jdbcRegistryDataChangeEvents) {
            log.debug("Detect JdbcRegistryDataChangeEvent: {}", jdbcRegistryDataChangeEvent);
            switch (jdbcRegistryDataChangeEvent.getEventType()) {
                case ADD:
                    doTriggerJdbcRegistryDataAddedListener(
                            Lists.newArrayList(jdbcRegistryDataChangeEvent.getJdbcRegistryData()));
                    break;
                case UPDATE:
                    doTriggerJdbcRegistryDataUpdatedListener(
                            Lists.newArrayList(jdbcRegistryDataChangeEvent.getJdbcRegistryData()));
                    break;
                case DELETE:
                    doTriggerJdbcRegistryDataRemovedListener(
                            Lists.newArrayList(jdbcRegistryDataChangeEvent.getJdbcRegistryData()));
                    break;
                default:
                    log.error("Unknown event type: {}", jdbcRegistryDataChangeEvent.getEventType());
                    break;
            }
            if (jdbcRegistryDataChangeEvent.getId() > lastDetectedJdbcRegistryDataChangeEventId) {
                lastDetectedJdbcRegistryDataChangeEventId = jdbcRegistryDataChangeEvent.getId();
            }
        }
    }

    private void purgeHistoryJdbcRegistryDataChangeEvent() {
        log.info("Purge JdbcRegistryDataChangeEvent which createTime is before: {} hours",
                keepJdbcRegistryDataChangeEventHours);
        jdbcRegistryDataChangeEventRepository.deleteJdbcRegistryDataChangeEventBeforeCreateTime(
                DateUtils.addHours(new Date(), -keepJdbcRegistryDataChangeEventHours));
    }

    @Override
    public void subscribeRegistryRowChange(RegistryRowChangeListener<JdbcRegistryDataDTO> registryRowChangeListener) {
        registryRowChangeListeners.add(checkNotNull(registryRowChangeListener));
    }

    @Override
    public boolean existKey(String key) {
        checkNotNull(key);
        return jdbcRegistryDataRepository.selectByKey(key).isPresent();
    }

    @Override
    public List<JdbcRegistryDataDTO> getAllJdbcRegistryData() {
        return jdbcRegistryDataRepository.selectAll();
    }

    @Override
    public Optional<JdbcRegistryDataDTO> getRegistryDataByKey(String key) {
        checkNotNull(key);
        return jdbcRegistryDataRepository.selectByKey(key);
    }

    @Override
    public List<JdbcRegistryDataDTO> listJdbcRegistryDataChildren(final String key) {
        checkNotNull(key);
        return jdbcRegistryDataRepository.selectAll()
                .stream()
                .filter(jdbcRegistryDataDTO -> KeyUtils.isParent(key, jdbcRegistryDataDTO.getDataKey()))
                .collect(Collectors.toList());
    }

    @Override
    public void putJdbcRegistryData(Long clientId, String key, String value, DataType dataType) {
        checkNotNull(clientId);
        checkNotNull(key);
        checkNotNull(dataType);

        final Optional<JdbcRegistryDataDTO> jdbcRegistryDataOptional = jdbcRegistryDataRepository.selectByKey(key);

        jdbcRegistryTransactionTemplate.execute(status -> {
            if (jdbcRegistryDataOptional.isPresent()) {
                JdbcRegistryDataDTO jdbcRegistryData = jdbcRegistryDataOptional.get();
                if (!dataType.name().equals(jdbcRegistryData.getDataType())) {
                    throw new UnsupportedOperationException("The data type: " + jdbcRegistryData.getDataType()
                            + " of the key: " + key + " cannot be updated");
                }

                if (DataType.EPHEMERAL.name().equals(jdbcRegistryData.getDataType())) {
                    if (!jdbcRegistryData.getClientId().equals(clientId)) {
                        throw new UnsupportedOperationException(
                                "The EPHEMERAL data: " + key + " can only be updated by its owner: "
                                        + jdbcRegistryData.getClientId() + " but not: " + clientId);
                    }
                }

                jdbcRegistryData.setDataValue(value);
                jdbcRegistryData.setLastUpdateTime(new Date());
                jdbcRegistryDataRepository.updateById(jdbcRegistryData);

                JdbcRegistryDataChangeEventDTO jdbcRegistryDataChangeEvent = JdbcRegistryDataChangeEventDTO.builder()
                        .jdbcRegistryData(jdbcRegistryData)
                        .eventType(JdbcRegistryDataChangeEventDTO.EventType.UPDATE)
                        .createTime(new Date())
                        .build();
                jdbcRegistryDataChangeEventRepository.insert(jdbcRegistryDataChangeEvent);
            } else {
                JdbcRegistryDataDTO jdbcRegistryDataDTO = JdbcRegistryDataDTO.builder()
                        .clientId(clientId)
                        .dataKey(key)
                        .dataValue(value)
                        .dataType(dataType.name())
                        .createTime(new Date())
                        .lastUpdateTime(new Date())
                        .build();
                jdbcRegistryDataRepository.insert(jdbcRegistryDataDTO);
                JdbcRegistryDataChangeEventDTO registryDataChangeEvent = JdbcRegistryDataChangeEventDTO.builder()
                        .jdbcRegistryData(jdbcRegistryDataDTO)
                        .eventType(JdbcRegistryDataChangeEventDTO.EventType.ADD)
                        .createTime(new Date())
                        .build();
                jdbcRegistryDataChangeEventRepository.insert(registryDataChangeEvent);
            }
            return null;
        });

    }

    @Override
    public void deleteJdbcRegistryDataByKey(String key) {
        checkNotNull(key);
        Optional<JdbcRegistryDataDTO> jdbcRegistryDataOptional = jdbcRegistryDataRepository.selectByKey(key);
        if (!jdbcRegistryDataOptional.isPresent()) {
            return;
        }
        jdbcRegistryTransactionTemplate.execute(status -> {
            jdbcRegistryDataRepository.deleteByKey(key);
            final JdbcRegistryDataChangeEventDTO registryDataChangeEvent = JdbcRegistryDataChangeEventDTO.builder()
                    .jdbcRegistryData(jdbcRegistryDataOptional.get())
                    .eventType(JdbcRegistryDataChangeEventDTO.EventType.DELETE)
                    .createTime(new Date())
                    .build();
            jdbcRegistryDataChangeEventRepository.insert(registryDataChangeEvent);
            return null;
        });
    }

    private void doTriggerJdbcRegistryDataAddedListener(List<JdbcRegistryDataDTO> valuesToAdd) {
        if (CollectionUtils.isEmpty(valuesToAdd)) {
            return;
        }
        log.debug("Trigger:onJdbcRegistryDataAdded: {}", valuesToAdd);
        valuesToAdd.forEach(jdbcRegistryData -> {
            try {
                registryRowChangeListeners.forEach(listener -> listener.onRegistryRowAdded(jdbcRegistryData));
            } catch (Exception ex) {
                log.error("Trigger:onRegistryRowAdded: {} failed", jdbcRegistryData, ex);
            }
        });
    }

    private void doTriggerJdbcRegistryDataRemovedListener(List<JdbcRegistryDataDTO> valuesToRemoved) {
        if (CollectionUtils.isEmpty(valuesToRemoved)) {
            return;
        }
        log.debug("Trigger:onJdbcRegistryDataDeleted: {}", valuesToRemoved);
        valuesToRemoved.forEach(jdbcRegistryData -> {
            try {
                registryRowChangeListeners.forEach(listener -> listener.onRegistryRowDeleted(jdbcRegistryData));
            } catch (Exception ex) {
                log.error("Trigger:onRegistryRowAdded: {} failed", jdbcRegistryData, ex);
            }
        });
    }

    private void doTriggerJdbcRegistryDataUpdatedListener(List<JdbcRegistryDataDTO> valuesToUpdated) {
        if (CollectionUtils.isEmpty(valuesToUpdated)) {
            return;
        }
        log.debug("Trigger:onJdbcRegistryDataUpdated: {}", valuesToUpdated);
        valuesToUpdated.forEach(jdbcRegistryData -> {
            try {
                registryRowChangeListeners.forEach(listener -> listener.onRegistryRowUpdated(jdbcRegistryData));
            } catch (Exception ex) {
                log.error("Trigger:onRegistryRowAdded: {} failed", jdbcRegistryData, ex);
            }
        });
    }

}
