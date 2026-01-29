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

package org.apache.dolphinscheduler.plugin.registry.jdbc.client;

import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.ConnectionStateListener;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.JdbcRegistryDataChangeListener;

import java.util.List;
import java.util.Optional;

public interface IJdbcRegistryClient extends AutoCloseable {

    /**
     * Start the jdbc registry client, once started, the client will connect to the jdbc registry server, and then it can be used.
     */
    void start();

    /**
     * Get identify of the client.
     */
    JdbcRegistryClientIdentify getJdbcRegistryClientIdentify();

    /**
     * Check the connectivity of the client.
     */
    boolean isConnectivity();

    /**
     * Subscribe the jdbc registry connection state change event.
     */
    void subscribeConnectionStateChange(ConnectionStateListener connectionStateListener);

    /**
     * Subscribe the {@link JdbcRegistryDataDTO} change event.
     */
    void subscribeJdbcRegistryDataChange(JdbcRegistryDataChangeListener jdbcRegistryDataChangeListener);

    /**
     * Get the {@link JdbcRegistryDataDTO} by key.
     */
    Optional<JdbcRegistryDataDTO> getJdbcRegistryDataByKey(String key);

    /**
     * Put the {@link JdbcRegistryDataDTO} to the jdbc registry server.
     * <p>
     * If the key is already exist, then update the {@link JdbcRegistryDataDTO}. If the key is not exist, then insert a new {@link JdbcRegistryDataDTO}.
     */
    void putJdbcRegistryData(String key, String value, DataType dataType);

    /**
     * Delete the {@link JdbcRegistryDataDTO} by key.
     */
    void deleteJdbcRegistryDataByKey(String key);

    /**
     * List all the {@link JdbcRegistryDataDTO} children by key.
     * <p>
     * e.g. key = "/dolphinscheduler/master", and data exist in db is "/dolphinscheduler/master/master1", "/dolphinscheduler/master/master2"
     * <p>
     * then the return value will be ["master1", "master2"]
     */
    List<JdbcRegistryDataDTO> listJdbcRegistryDataChildren(String key);

    /**
     * Check the key exist in the jdbc registry server.
     */
    boolean existJdbcRegistryDataKey(String key);

    /**
     * Acquire the jdbc registry lock by key. this is a blocking method. if you want to stop the blocking, you can use interrupt the thread.
     */
    void acquireJdbcRegistryLock(String lockKey) throws IllegalArgumentException;

    /**
     * Acquire the jdbc registry lock by key until timeout.
     */
    boolean acquireJdbcRegistryLock(String lockKey, long timeout);

    /**
     * Release the jdbc registry lock by key, if the lockKey is not exist will do nothing.
     */
    void releaseJdbcRegistryLock(String lockKey);

    /**
     * Close the jdbc registry client, once the client been closed, it cannot work anymore.
     */
    @Override
    void close();
}
