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

package org.apache.dolphinscheduler.plugin.registry.jdbc;

import org.apache.dolphinscheduler.plugin.registry.jdbc.mapper.JdbcRegistryDataMapper;
import org.apache.dolphinscheduler.plugin.registry.jdbc.mapper.JdbcRegistryLockMapper;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.JdbcRegistryData;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.JdbcRegistryLock;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;

public final class JdbcOperator {

    private final JdbcRegistryDataMapper jdbcRegistryDataMapper;
    private final JdbcRegistryLockMapper jdbcRegistryLockMapper;
    private final long expireTimeWindow;

    JdbcOperator(JdbcRegistryProperties registryProperties,
                 JdbcRegistryDataMapper jdbcRegistryDataMapper,
                 JdbcRegistryLockMapper jdbcRegistryLockMapper) {
        this.expireTimeWindow =
                registryProperties.getTermExpireTimes() * registryProperties.getTermRefreshInterval().toMillis();
        this.jdbcRegistryDataMapper = jdbcRegistryDataMapper;
        this.jdbcRegistryLockMapper = jdbcRegistryLockMapper;
    }

    public void healthCheck() {
        jdbcRegistryLockMapper.countAll();
    }

    public List<JdbcRegistryData> queryAllJdbcRegistryData() {
        return jdbcRegistryDataMapper.selectAll();
    }

    public Long insertOrUpdateEphemeralData(String key, String value) throws SQLException {
        JdbcRegistryData jdbcRegistryData = jdbcRegistryDataMapper.selectByKey(key);
        if (jdbcRegistryData != null) {
            jdbcRegistryData.setDataValue(value);
            jdbcRegistryData.setLastUpdateTime(new Date());
            jdbcRegistryData.setLastTerm(System.currentTimeMillis());
            if (jdbcRegistryDataMapper.updateById(jdbcRegistryData) <= 0) {
                throw new SQLException(String.format("update registry value failed, key: %s, value: %s", key, value));
            }
            return jdbcRegistryData.getId();
        }
        jdbcRegistryData = JdbcRegistryData.builder()
                .dataKey(key)
                .dataValue(value)
                .dataType(DataType.EPHEMERAL.getTypeValue())
                .lastTerm(System.currentTimeMillis())
                .lastUpdateTime(new Date())
                .createTime(new Date())
                .build();
        jdbcRegistryDataMapper.insert(jdbcRegistryData);
        return jdbcRegistryData.getId();
    }

    public long insertOrUpdatePersistentData(String key, String value) throws SQLException {
        JdbcRegistryData jdbcRegistryData = jdbcRegistryDataMapper.selectByKey(key);
        if (jdbcRegistryData != null) {
            jdbcRegistryData.setDataValue(value);
            jdbcRegistryData.setLastUpdateTime(new Date());
            jdbcRegistryData.setLastTerm(System.currentTimeMillis());
            if (jdbcRegistryDataMapper.updateById(jdbcRegistryData) <= 0) {
                throw new SQLException(String.format("update registry value failed, key: %s, value: %s", key, value));
            }
            return jdbcRegistryData.getId();
        }
        jdbcRegistryData = JdbcRegistryData.builder()
                .dataKey(key)
                .dataValue(value)
                .dataType(DataType.PERSISTENT.getTypeValue())
                .lastTerm(System.currentTimeMillis())
                .lastUpdateTime(new Date())
                .createTime(new Date())
                .build();
        jdbcRegistryDataMapper.insert(jdbcRegistryData);
        return jdbcRegistryData.getId();
    }

    public void deleteDataByKey(String key) {
        jdbcRegistryDataMapper.deleteByKey(key);
    }

    public void deleteDataById(long id) {
        jdbcRegistryDataMapper.deleteById(id);
    }

    public void clearExpireLock() {
        jdbcRegistryLockMapper.clearExpireLock(System.currentTimeMillis() - expireTimeWindow);
    }

    public void clearExpireEphemeralDate() {
        jdbcRegistryDataMapper.clearExpireEphemeralDate(System.currentTimeMillis() - expireTimeWindow,
                DataType.EPHEMERAL.getTypeValue());
    }

    public JdbcRegistryData getData(String key) throws SQLException {
        return jdbcRegistryDataMapper.selectByKey(key);
    }

    public List<String> getChildren(String key) throws SQLException {
        return jdbcRegistryDataMapper.fuzzyQueryByKey(key)
                .stream()
                .map(JdbcRegistryData::getDataKey)
                .filter(fullPath -> fullPath.length() > key.length())
                .map(fullPath -> StringUtils.substringBefore(fullPath.substring(key.length() + 1), "/"))
                .collect(Collectors.toList());
    }

    public boolean existKey(String key) {
        JdbcRegistryData jdbcRegistryData = jdbcRegistryDataMapper.selectByKey(key);
        return jdbcRegistryData != null;
    }

    /**
     * Try to acquire the target Lock, if cannot acquire, return null.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public JdbcRegistryLock tryToAcquireLock(String key) {
        JdbcRegistryLock jdbcRegistryLock = JdbcRegistryLock.builder()
                .lockKey(key)
                .lockOwner(LockUtils.getLockOwner())
                .lastTerm(System.currentTimeMillis())
                .lastUpdateTime(new Date())
                .build();
        try {
            jdbcRegistryLockMapper.insert(jdbcRegistryLock);
            return jdbcRegistryLock;
        } catch (Exception e) {
            if (e instanceof SQLIntegrityConstraintViolationException || e instanceof DuplicateKeyException) {
                return null;
            }
            throw e;
        }
    }

    public JdbcRegistryLock getLockById(long lockId) {
        return jdbcRegistryLockMapper.selectById(lockId);
    }

    public boolean releaseLock(long lockId) throws SQLException {
        return jdbcRegistryLockMapper.deleteById(lockId) > 0;
    }

    public boolean updateEphemeralDataTerm(Collection<Long> ephemeralDateIds) {
        if (CollectionUtils.isEmpty(ephemeralDateIds)) {
            return true;
        }
        return jdbcRegistryDataMapper.updateTermByIds(ephemeralDateIds, System.currentTimeMillis()) > 0;
    }

    public boolean updateLockTerm(List<Long> lockIds) {
        if (CollectionUtils.isEmpty(lockIds)) {
            return true;
        }
        return jdbcRegistryLockMapper.updateTermByIds(lockIds, System.currentTimeMillis()) > 0;
    }

}
