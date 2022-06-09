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

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.DataType;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryData;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryLock;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to CRUD from mysql
 */
public class MysqlOperator {

    private static final Logger logger = LoggerFactory.getLogger(MysqlOperator.class);

    private final DataSource dataSource;

    public MysqlOperator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void healthCheck() throws SQLException {
        String sql = "select 1 from t_ds_mysql_registry_data";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // if no exception, the healthCheck success
            preparedStatement.executeQuery();
        }
    }

    public List<MysqlRegistryData> queryAllMysqlRegistryData() throws SQLException {
        String sql = "select id, `key`, data, type, createTime, lastUpdateTime from t_ds_mysql_registry_data";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<MysqlRegistryData> result = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                MysqlRegistryData mysqlRegistryData = MysqlRegistryData.builder()
                        .id(resultSet.getLong(1))
                        .key(resultSet.getString(2))
                        .data(resultSet.getString(3))
                        .type(resultSet.getInt(4))
                        .createTime(resultSet.getTimestamp(5))
                        .lastUpdateTime(resultSet.getTimestamp(6))
                        .build();
                result.add(mysqlRegistryData);
            }
            return result;
        }
    }

    public long insertOrUpdateEphemeralData(String key, String value) throws SQLException {
        String sql = "INSERT INTO t_ds_mysql_registry_data (`key`, data, type, createTime, lastUpdateTime) VALUES (?, ?, ?, current_timestamp, current_timestamp)" +
                "ON DUPLICATE KEY UPDATE data=?, lastUpdateTime=current_timestamp";
        // put a ephemeralData
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, value);
            preparedStatement.setInt(3, DataType.EPHEMERAL.getTypeValue());
            preparedStatement.setString(4, value);
            int insertCount = preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (insertCount < 1 || !generatedKeys.next()) {
                throw new SQLException("Insert ephemeral data error");
            }
            return generatedKeys.getLong(1);
        }
    }

    public void insertOrUpdatePersistentData(String key, String value) throws SQLException {
        String sql = "INSERT INTO t_ds_mysql_registry_data (`key`, data, type, createTime, lastUpdateTime) VALUES (?, ?, ?, current_timestamp, current_timestamp)" +
                "ON DUPLICATE KEY UPDATE data=?, lastUpdateTime=current_timestamp";
        // put a persistent Data
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, value);
            preparedStatement.setInt(3, DataType.PERSISTENT.getTypeValue());
            preparedStatement.setString(4, value);
            preparedStatement.executeUpdate();
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
        String sql = "delete from t_ds_mysql_registry_lock where current_timestamp - lastTerm > ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, MysqlRegistryConstant.TERM_EXPIRE_TIME / 1000);
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            logger.warn("Clear expire lock from mysql registry error", ex);
        }
    }

    public void clearExpireEphemeralDate() {
        String sql = "delete from t_ds_mysql_registry_data where current_timestamp - lastUpdateTime > ? and type = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, MysqlRegistryConstant.TERM_EXPIRE_TIME / 1000);
            preparedStatement.setInt(2, DataType.EPHEMERAL.getTypeValue());
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            logger.warn("Clear expire ephemeral data from mysql registry error", ex);
        }
    }

    public MysqlRegistryData getData(String key) throws SQLException {
        String sql = "SELECT id, `key`, data, type, createTime, lastUpdateTime FROM t_ds_mysql_registry_data WHERE `key` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet == null) {
                return null;
            }
            return MysqlRegistryData.builder()
                    .id(resultSet.getLong(1))
                    .key(resultSet.getString(2))
                    .data(resultSet.getString(3))
                    .type(resultSet.getInt(4))
                    .createTime(resultSet.getTimestamp(5))
                    .lastUpdateTime(resultSet.getTimestamp(6))
                    .build();
        }
    }

    public List<String> getChildren(String key) throws SQLException {
        String sql = "SELECT `key` from t_ds_mysql_registry_data where `key` like ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> result = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                String fullPath = resultSet.getString(1);
                if (fullPath.length() > key.length()) {
                    result.add(StringUtils.substringBefore(fullPath.substring(key.length() + 1), "/"));
                }
            }
            return result;
        }
    }

    public boolean existKey(String key) throws SQLException {
        String sql = "SELECT 1 FROM t_ds_mysql_registry_data WHERE `key` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to acquire the target Lock, if cannot acquire, return null.
     */
    public MysqlRegistryLock tryToAcquireLock(String key) throws SQLException {
        String sql = "INSERT INTO t_ds_mysql_registry_lock (`key`, host, lastTerm, lastUpdateTime, createTime) VALUES (?, ?, current_timestamp, current_timestamp, current_timestamp)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            preparedStatement.setString(1, key);
            // todo: if we start multiple master in one instance with the same ip,
            //  then only one master can get the lock at the same time.
            preparedStatement.setString(2, NetUtils.getHost());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                long newLockId = resultSet.getLong(1);
                return getLockById(newLockId);
            }
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            // duplicate exception
            return null;
        }
    }

    public MysqlRegistryLock getLockById(long lockId) throws SQLException {
        String sql = "SELECT `id`, `key`, host, lastTerm, lastUpdateTime, createTime FROM t_ds_mysql_registry_lock WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, lockId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return MysqlRegistryLock.builder()
                        .id(resultSet.getLong(1))
                        .key(resultSet.getString(2))
                        .host(resultSet.getString(3))
                        .lastTerm(resultSet.getTimestamp(4))
                        .lastUpdateTime(resultSet.getTimestamp(5))
                        .createTime(resultSet.getTimestamp(6))
                        .build();
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

    public boolean updateEphemeralDateTerm(Long ephemeralDateId) throws SQLException {
        String sql = "update t_ds_mysql_registry_data set `lastUpdateTime` = current_timestamp where `id` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, ephemeralDateId);
            return preparedStatement.executeUpdate() > 1;
        }
    }

    public boolean updateLockTerm(MysqlRegistryLock mysqlRegistryLock) throws SQLException {
        String sql = "update t_ds_mysql_registry_lock set `lastTerm` = current_timestamp and `lastUpdateTime` = current_timestamp where `id` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, mysqlRegistryLock.getId());
            return preparedStatement.executeUpdate() > 1;
        }
    }

}
