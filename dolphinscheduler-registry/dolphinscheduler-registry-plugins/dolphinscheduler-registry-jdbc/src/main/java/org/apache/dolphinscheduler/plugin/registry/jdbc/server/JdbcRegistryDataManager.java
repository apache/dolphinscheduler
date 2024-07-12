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
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataChanceEventDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryDataChanceEventRepository;
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

import com.google.common.collect.Lists;

@Slf4j
public class JdbcRegistryDataManager
        implements
            IRegistryRowChangeNotifier<JdbcRegistryDataDTO>,
            IJdbcRegistryDataManager {

    private final Integer keepJdbcRegistryDataChanceEventHours = 2;

    private final JdbcRegistryProperties registryProperties;

    private final JdbcRegistryDataRepository jdbcRegistryDataRepository;

    private final JdbcRegistryDataChanceEventRepository jdbcRegistryDataChanceEventRepository;

    private final List<RegistryRowChangeListener<JdbcRegistryDataDTO>> registryRowChangeListeners;

    private long lastDetectedJdbcRegistryDataChangeEventId = -1;

    public JdbcRegistryDataManager(JdbcRegistryProperties registryProperties,
                                   JdbcRegistryDataRepository jdbcRegistryDataRepository,
                                   JdbcRegistryDataChanceEventRepository jdbcRegistryDataChanceEventRepository) {
        this.registryProperties = registryProperties;
        this.jdbcRegistryDataChanceEventRepository = jdbcRegistryDataChanceEventRepository;
        this.jdbcRegistryDataRepository = jdbcRegistryDataRepository;
        this.registryRowChangeListeners = new CopyOnWriteArrayList<>();
        this.lastDetectedJdbcRegistryDataChangeEventId =
                jdbcRegistryDataChanceEventRepository.getMaxJdbcRegistryDataChanceEventId();
    }

    @Override
    public void start() {
        JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor().scheduleWithFixedDelay(
                this::detectJdbcRegistryDataChangeEvent,
                registryProperties.getHeartbeatRefreshInterval().toMillis(),
                registryProperties.getHeartbeatRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS);

        JdbcRegistryThreadFactory.getDefaultSchedulerThreadExecutor().scheduleWithFixedDelay(
                this::purgeHistoryJdbcRegistryDataChangeEvent,
                0,
                Duration.ofHours(keepJdbcRegistryDataChanceEventHours).toHours(),
                TimeUnit.HOURS);
    }

    private void detectJdbcRegistryDataChangeEvent() {
        final List<JdbcRegistryDataChanceEventDTO> jdbcRegistryDataChanceEvents = jdbcRegistryDataChanceEventRepository
                .selectJdbcRegistryDataChangeEventWhereIdAfter(lastDetectedJdbcRegistryDataChangeEventId);
        if (CollectionUtils.isEmpty(jdbcRegistryDataChanceEvents)) {
            return;
        }
        for (JdbcRegistryDataChanceEventDTO jdbcRegistryDataChanceEvent : jdbcRegistryDataChanceEvents) {
            log.debug("Detect JdbcRegistryDataChangeEvent: {}", jdbcRegistryDataChanceEvent);
            switch (jdbcRegistryDataChanceEvent.getEventType()) {
                case ADD:
                    doTriggerJdbcRegistryDataAddedListener(
                            Lists.newArrayList(jdbcRegistryDataChanceEvent.getJdbcRegistryData()));
                    break;
                case UPDATE:
                    doTriggerJdbcRegistryDataUpdatedListener(
                            Lists.newArrayList(jdbcRegistryDataChanceEvent.getJdbcRegistryData()));
                    break;
                case DELETE:
                    doTriggerJdbcRegistryDataRemovedListener(
                            Lists.newArrayList(jdbcRegistryDataChanceEvent.getJdbcRegistryData()));
                    break;
                default:
                    log.error("Unknown event type: {}", jdbcRegistryDataChanceEvent.getEventType());
                    break;
            }
            if (jdbcRegistryDataChanceEvent.getId() > lastDetectedJdbcRegistryDataChangeEventId) {
                lastDetectedJdbcRegistryDataChangeEventId = jdbcRegistryDataChanceEvent.getId();
            }
        }
    }

    private void purgeHistoryJdbcRegistryDataChangeEvent() {
        log.info("Purge JdbcRegistryDataChanceEvent which createTime is before: {} hours",
                keepJdbcRegistryDataChanceEventHours);
        jdbcRegistryDataChanceEventRepository.deleteJdbcRegistryDataChangeEventBeforeCreateTime(
                DateUtils.addHours(new Date(), -keepJdbcRegistryDataChanceEventHours));
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
    public Optional<JdbcRegistryDataDTO> getRegistryDataByKey(String key) {
        checkNotNull(key);
        return jdbcRegistryDataRepository.selectByKey(key);
    }

    @Override
    public List<JdbcRegistryDataDTO> listJdbcRegistryDataChildren(String key) {
        checkNotNull(key);
        return jdbcRegistryDataRepository.selectAll()
                .stream()
                .filter(jdbcRegistryDataDTO -> jdbcRegistryDataDTO.getDataKey().startsWith(key)
                        && !jdbcRegistryDataDTO.getDataKey().equals(key))
                .collect(Collectors.toList());
    }

    @Override
    public void putJdbcRegistryData(Long clientId, String key, String value, DataType dataType) {
        checkNotNull(clientId);
        checkNotNull(key);
        checkNotNull(dataType);

        Optional<JdbcRegistryDataDTO> jdbcRegistryDataOptional = jdbcRegistryDataRepository.selectByKey(key);
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

            JdbcRegistryDataChanceEventDTO jdbcRegistryDataChanceEvent = JdbcRegistryDataChanceEventDTO.builder()
                    .jdbcRegistryData(jdbcRegistryData)
                    .eventType(JdbcRegistryDataChanceEventDTO.EventType.UPDATE)
                    .createTime(new Date())
                    .build();
            jdbcRegistryDataChanceEventRepository.insert(jdbcRegistryDataChanceEvent);
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
            JdbcRegistryDataChanceEventDTO registryDataChanceEvent = JdbcRegistryDataChanceEventDTO.builder()
                    .jdbcRegistryData(jdbcRegistryDataDTO)
                    .eventType(JdbcRegistryDataChanceEventDTO.EventType.ADD)
                    .createTime(new Date())
                    .build();
            jdbcRegistryDataChanceEventRepository.insert(registryDataChanceEvent);
        }

    }

    @Override
    public void deleteJdbcRegistryDataByKey(String key) {
        checkNotNull(key);
        // todo: this is not atomic, need to be improved
        Optional<JdbcRegistryDataDTO> jdbcRegistryDataOptional = jdbcRegistryDataRepository.selectByKey(key);
        if (!jdbcRegistryDataOptional.isPresent()) {
            return;
        }
        jdbcRegistryDataRepository.deleteByKey(key);
        JdbcRegistryDataChanceEventDTO registryDataChanceEvent = JdbcRegistryDataChanceEventDTO.builder()
                .jdbcRegistryData(jdbcRegistryDataOptional.get())
                .eventType(JdbcRegistryDataChanceEventDTO.EventType.DELETE)
                .createTime(new Date())
                .build();
        jdbcRegistryDataChanceEventRepository.insert(registryDataChanceEvent);
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
