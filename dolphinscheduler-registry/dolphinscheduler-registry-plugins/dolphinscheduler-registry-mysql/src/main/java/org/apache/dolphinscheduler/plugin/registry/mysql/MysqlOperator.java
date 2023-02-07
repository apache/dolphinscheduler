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

package org.apache.dolphinscheduler.plugin.registry.mysql;

import org.apache.dolphinscheduler.plugin.registry.mysql.mapper.MysqlRegistryDataMapper;
import org.apache.dolphinscheduler.plugin.registry.mysql.mapper.MysqlRegistryLockMapper;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.DataType;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryData;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryLock;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "mysql")
public class MysqlOperator {

    @Autowired
    private MysqlRegistryDataMapper mysqlRegistryDataMapper;
    @Autowired
    private MysqlRegistryLockMapper mysqlRegistryLockMapper;

    private final long expireTimeWindow;

    public MysqlOperator(MysqlRegistryProperties registryProperties) {
        this.expireTimeWindow =
                registryProperties.getTermExpireTimes() * registryProperties.getTermRefreshInterval().toMillis();
    }

    public void healthCheck() {
        mysqlRegistryLockMapper.countAll();
    }

    public List<MysqlRegistryData> queryAllMysqlRegistryData() {
        return mysqlRegistryDataMapper.selectAll();
    }

    public Long insertOrUpdateEphemeralData(String key, String value) throws SQLException {
        MysqlRegistryData mysqlRegistryData = mysqlRegistryDataMapper.selectByKey(key);
        if (mysqlRegistryData != null) {
            long id = mysqlRegistryData.getId();
            if (mysqlRegistryDataMapper.updateDataAndTermById(id, value, System.currentTimeMillis()) <= 0) {
                throw new SQLException(String.format("update registry value failed, key: %s, value: %s", key, value));
            }
            return id;
        }
        mysqlRegistryData = MysqlRegistryData.builder()
                .key(key)
                .data(value)
                .type(DataType.EPHEMERAL.getTypeValue())
                .lastTerm(System.currentTimeMillis())
                .build();
        mysqlRegistryDataMapper.insert(mysqlRegistryData);
        return mysqlRegistryData.getId();
    }

    public long insertOrUpdatePersistentData(String key, String value) throws SQLException {
        MysqlRegistryData mysqlRegistryData = mysqlRegistryDataMapper.selectByKey(key);
        if (mysqlRegistryData != null) {
            long id = mysqlRegistryData.getId();
            if (mysqlRegistryDataMapper.updateDataAndTermById(id, value, System.currentTimeMillis()) <= 0) {
                throw new SQLException(String.format("update registry value failed, key: %s, value: %s", key, value));
            }
            return id;
        }
        mysqlRegistryData = MysqlRegistryData.builder()
                .key(key)
                .data(value)
                .type(DataType.PERSISTENT.getTypeValue())
                .lastTerm(System.currentTimeMillis())
                .build();
        mysqlRegistryDataMapper.insert(mysqlRegistryData);
        return mysqlRegistryData.getId();
    }

    public void deleteDataByKey(String key) {
        mysqlRegistryDataMapper.deleteByKey(key);
    }

    public void deleteDataById(long id) {
        mysqlRegistryDataMapper.deleteById(id);
    }

    public void clearExpireLock() {
        mysqlRegistryLockMapper.clearExpireLock(System.currentTimeMillis() - expireTimeWindow);
    }

    public void clearExpireEphemeralDate() {
        mysqlRegistryDataMapper.clearExpireEphemeralDate(System.currentTimeMillis() - expireTimeWindow,
                DataType.EPHEMERAL.getTypeValue());
    }

    public MysqlRegistryData getData(String key) throws SQLException {
        return mysqlRegistryDataMapper.selectByKey(key);
    }

    public List<String> getChildren(String key) throws SQLException {
        return mysqlRegistryDataMapper.fuzzyQueryByKey(key)
                .stream()
                .map(MysqlRegistryData::getKey)
                .filter(fullPath -> fullPath.length() > key.length())
                .map(fullPath -> StringUtils.substringBefore(fullPath.substring(key.length() + 1), "/"))
                .collect(Collectors.toList());
    }

    public boolean existKey(String key) throws SQLException {
        MysqlRegistryData mysqlRegistryData = mysqlRegistryDataMapper.selectByKey(key);
        return mysqlRegistryData != null;
    }

    /**
     * Try to acquire the target Lock, if cannot acquire, return null.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public MysqlRegistryLock tryToAcquireLock(String key) throws SQLException {
        MysqlRegistryLock mysqlRegistryLock = MysqlRegistryLock.builder()
                .key(key)
                .lockOwner(MysqlRegistryConstant.LOCK_OWNER)
                .lastTerm(System.currentTimeMillis())
                .build();
        try {
            mysqlRegistryLockMapper.insert(mysqlRegistryLock);
            return mysqlRegistryLock;
        } catch (Exception e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                return null;
            }
            throw e;
        }
    }

    public MysqlRegistryLock getLockById(long lockId) throws SQLException {
        return mysqlRegistryLockMapper.selectById(lockId);
    }

    public boolean releaseLock(long lockId) throws SQLException {
        return mysqlRegistryLockMapper.deleteById(lockId) > 0;
    }

    public boolean updateEphemeralDataTerm(Collection<Long> ephemeralDateIds) throws SQLException {
        if (CollectionUtils.isEmpty(ephemeralDateIds)) {
            return true;
        }
        return mysqlRegistryDataMapper.updateTermByIds(ephemeralDateIds, System.currentTimeMillis()) > 0;
    }

    public boolean updateLockTerm(List<Long> lockIds) {
        if (CollectionUtils.isEmpty(lockIds)) {
            return true;
        }
        return mysqlRegistryLockMapper.updateTermByIds(lockIds, System.currentTimeMillis()) > 0;
    }

}
