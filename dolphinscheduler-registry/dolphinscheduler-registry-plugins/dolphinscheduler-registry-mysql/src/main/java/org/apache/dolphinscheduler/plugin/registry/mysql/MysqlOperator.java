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

import org.apache.dolphinscheduler.plugin.registry.mysql.model.DataType;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryData;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryLock;

import org.apache.commons.lang3.StringUtils;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Used to CRUD from mysql
 */
public class MysqlOperator implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MysqlOperator.class);

    private final HikariDataSource dataSource;
    private final long expireTimeWindow;

    public MysqlOperator(MysqlRegistryProperties registryProperties) {
        this.expireTimeWindow = registryProperties.getTermExpireTimes() * registryProperties.getTermRefreshInterval().toMillis();

        HikariConfig hikariConfig = registryProperties.getHikariConfig();
        hikariConfig.setPoolName("MysqlRegistryDataSourcePool");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public void healthCheck() throws SQLException {
        String sql = "select 1 from t_ds_mysql_registry_data";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery();) {
            // if no exception, the healthCheck success
        }
    }

    public List<MysqlRegistryData> queryAllMysqlRegistryData() throws SQLException {
        String sql =
                "select id, `key`, data, type, last_term, create_time, last_update_time from t_ds_mysql_registry_data";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            List<MysqlRegistryData> result = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                MysqlRegistryData mysqlRegistryData = MysqlRegistryData.builder()
                        .id(resultSet.getLong("id"))
                        .key(resultSet.getString("key"))
                        .data(resultSet.getString("data"))
                        .type(resultSet.getInt("type"))
                        .lastTerm(resultSet.getLong("last_term"))
                        .createTime(resultSet.getTimestamp("create_time"))
                        .lastUpdateTime(resultSet.getTimestamp("last_update_time"))
                        .build();
                result.add(mysqlRegistryData);
            }
            return result;
        }
    }

    public Long insertOrUpdateEphemeralData(String key, String value) throws SQLException {
        Optional<MysqlRegistryData> mysqlRegistryDataOptional = selectByKey(key);
        if (mysqlRegistryDataOptional.isPresent()) {
            long id = mysqlRegistryDataOptional.get().getId();
            if (!updateValueById(id, value)) {
                throw new SQLException(String.format("update registry value failed, key: %s, value: %s", key, value));
            }
            return id;
        }
        MysqlRegistryData mysqlRegistryData = MysqlRegistryData.builder()
                .key(key)
                .data(value)
                .type(DataType.EPHEMERAL.getTypeValue())
                .lastTerm(System.currentTimeMillis())
                .build();
        return insertMysqlRegistryData(mysqlRegistryData);
    }

    private Optional<MysqlRegistryData> selectByKey(@NonNull String key) throws SQLException {
        String sql =
                "select id, `key`, data, type, create_time, last_update_time from t_ds_mysql_registry_data where `key` = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(
                            MysqlRegistryData.builder()
                                    .id(resultSet.getLong("id"))
                                    .key(resultSet.getString("key"))
                                    .data(resultSet.getString("data"))
                                    .type(resultSet.getInt("type"))
                                    .createTime(resultSet.getTimestamp("create_time"))
                                    .lastUpdateTime(resultSet.getTimestamp("last_update_time"))
                                    .build());
                }
                return Optional.empty();
            }
        }
    }

    private boolean updateValueById(long id, String value) throws SQLException {
        String sql = "update t_ds_mysql_registry_data set data = ?, last_term = ? where id = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, value);
            preparedStatement.setLong(2, System.currentTimeMillis());
            preparedStatement.setLong(3, id);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private long insertMysqlRegistryData(@NonNull MysqlRegistryData mysqlRegistryData) throws SQLException {
        String sql =
                "INSERT INTO t_ds_mysql_registry_data (`key`, data, type, last_term) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, mysqlRegistryData.getKey());
            preparedStatement.setString(2, mysqlRegistryData.getData());
            preparedStatement.setInt(3, mysqlRegistryData.getType());
            preparedStatement.setLong(4, mysqlRegistryData.getLastTerm());
            int insertCount = preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (insertCount < 1 || !generatedKeys.next()) {
                throw new SQLException("Insert ephemeral data error, data: " + mysqlRegistryData);
            }
            return generatedKeys.getLong(1);
        }
    }

    public long insertOrUpdatePersistentData(String key, String value) throws SQLException {
        String sql =
                "INSERT INTO t_ds_mysql_registry_data (`key`, data, type, last_term) VALUES (?, ?, ?, ?)"
                        +
                        "ON DUPLICATE KEY UPDATE data=?, last_term=?";
        // put a persistent Data
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            long term = System.currentTimeMillis();
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, value);
            preparedStatement.setInt(3, DataType.PERSISTENT.getTypeValue());
            preparedStatement.setLong(4, term);
            preparedStatement.setString(5, value);
            preparedStatement.setLong(6, term);
            int insertCount = preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (insertCount < 1 || !generatedKeys.next()) {
                throw new SQLException("Insert or update persistent data error");
            }
            return generatedKeys.getLong(1);
        }
    }

    public void deleteEphemeralData(String key) throws SQLException {
        String sql = "DELETE from t_ds_mysql_registry_data where `key` = ? and type = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            preparedStatement.setInt(2, DataType.EPHEMERAL.getTypeValue());
            preparedStatement.execute();
        }
    }

    public void deleteEphemeralData(long ephemeralNodeId) throws SQLException {
        String sql = "DELETE from t_ds_mysql_registry_data where `id` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, ephemeralNodeId);
            preparedStatement.execute();
        }
    }

    public void deletePersistentData(String key) throws SQLException {
        String sql = "DELETE from t_ds_mysql_registry_data where `key` = ? and type = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            preparedStatement.setInt(2, DataType.PERSISTENT.getTypeValue());
            preparedStatement.execute();
        }
    }

    public void clearExpireLock() {
        String sql = "delete from t_ds_mysql_registry_lock where last_term < ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, System.currentTimeMillis() - expireTimeWindow);
            int i = preparedStatement.executeUpdate();
            if (i > 0) {
                logger.info("Clear expire lock, size: {}", i);
            }
        } catch (Exception ex) {
            logger.warn("Clear expire lock from mysql registry error", ex);
        }
    }

    public void clearExpireEphemeralDate() {
        String sql = "delete from t_ds_mysql_registry_data where last_term < ? and type = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, System.currentTimeMillis() - expireTimeWindow);
            preparedStatement.setInt(2, DataType.EPHEMERAL.getTypeValue());
            int i = preparedStatement.executeUpdate();
            if (i > 0) {
                logger.info("clear expire ephemeral data, size:{}", i);
            }
        } catch (Exception ex) {
            logger.warn("Clear expire ephemeral data from mysql registry error", ex);
        }
    }

    public MysqlRegistryData getData(String key) throws SQLException {
        String sql =
                "SELECT id, `key`, data, type, last_term, create_time, last_update_time FROM t_ds_mysql_registry_data WHERE `key` = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return MysqlRegistryData.builder()
                        .id(resultSet.getLong("id"))
                        .key(resultSet.getString("key"))
                        .data(resultSet.getString("data"))
                        .type(resultSet.getInt("type"))
                        .lastTerm(resultSet.getLong("last_term"))
                        .createTime(resultSet.getTimestamp("create_time"))
                        .lastUpdateTime(resultSet.getTimestamp("last_update_time"))
                        .build();
            }
        }
    }

    public List<String> getChildren(String key) throws SQLException {
        String sql = "SELECT `key` from t_ds_mysql_registry_data where `key` like ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key + "%");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> result = new ArrayList<>(resultSet.getFetchSize());
                while (resultSet.next()) {
                    String fullPath = resultSet.getString("key");
                    if (fullPath.length() > key.length()) {
                        result.add(StringUtils.substringBefore(fullPath.substring(key.length() + 1), "/"));
                    }
                }
                return result;
            }
        }
    }

    public boolean existKey(String key) throws SQLException {
        String sql = "SELECT 1 FROM t_ds_mysql_registry_data WHERE `key` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Try to acquire the target Lock, if cannot acquire, return null.
     */
    public MysqlRegistryLock tryToAcquireLock(String key) throws SQLException {
        String sql =
                "INSERT INTO t_ds_mysql_registry_lock (`key`, lock_owner, last_term) VALUES (?, ?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, MysqlRegistryConstant.LOCK_OWNER);
            preparedStatement.setLong(3, System.currentTimeMillis());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    long newLockId = resultSet.getLong(1);
                    return getLockById(newLockId);
                }
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            // duplicate exception
            return null;
        }
    }

    public MysqlRegistryLock getLockById(long lockId) throws SQLException {
        String sql = "SELECT `id`, `key`, lock_owner, last_term, last_update_time, create_time FROM t_ds_mysql_registry_lock WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, lockId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return MysqlRegistryLock.builder()
                            .id(resultSet.getLong("id"))
                            .key(resultSet.getString("key"))
                            .lockOwner(resultSet.getString("lock_owner"))
                            .lastTerm(resultSet.getLong("last_term"))
                            .lastUpdateTime(resultSet.getTimestamp("last_update_time"))
                            .createTime(resultSet.getTimestamp("create_time"))
                            .build();
                }
            }
            return null;
        }
    }

    // release the lock
    public boolean releaseLock(long lockId) throws SQLException {
        String sql = "DELETE FROM t_ds_mysql_registry_lock WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, lockId);
            int i = preparedStatement.executeUpdate();
            return i > 0;
        }
    }

    public boolean updateEphemeralDataTerm(Collection<Long> ephemeralDateIds) throws SQLException {
        StringBuilder sb = new StringBuilder("update t_ds_mysql_registry_data set `last_term` = ? where `id` IN (");
        Iterator<Long> iterator = ephemeralDateIds.iterator();
        for (int i = 0; i < ephemeralDateIds.size(); i++) {
            sb.append(iterator.next());
            if (i != ephemeralDateIds.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
            preparedStatement.setLong(1, System.currentTimeMillis());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateLockTerm(List<Long> lockIds) throws SQLException {
        StringBuilder sb =
                new StringBuilder("update t_ds_mysql_registry_lock set `last_term` = ? where `id` IN (");
        Iterator<Long> iterator = lockIds.iterator();
        for (int i = 0; i < lockIds.size(); i++) {
            sb.append(iterator.next());
            if (i != lockIds.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
            preparedStatement.setLong(1, System.currentTimeMillis());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    @Override
    public void close() throws Exception {
        if (!dataSource.isClosed()) {
            try (HikariDataSource closedDatasource = this.dataSource) {

            }
        }
    }
}
