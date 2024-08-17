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

import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.ConnectionStateListener;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.IJdbcRegistryServer;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.JdbcRegistryDataChangeListener;
import org.apache.dolphinscheduler.plugin.registry.jdbc.server.JdbcRegistryServerState;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * The client of jdbc registry, used to interact with the {@link org.apache.dolphinscheduler.plugin.registry.jdbc.server.JdbcRegistryServer}.
 */
@Slf4j
public class JdbcRegistryClient implements IJdbcRegistryClient {

    private static final String DEFAULT_CLIENT_NAME = NetUtils.getHost() + "_" + OSUtils.getProcessID();

    private final JdbcRegistryProperties jdbcRegistryProperties;

    private final JdbcRegistryClientIdentify jdbcRegistryClientIdentify;

    private final IJdbcRegistryServer jdbcRegistryServer;

    public JdbcRegistryClient(JdbcRegistryProperties jdbcRegistryProperties, IJdbcRegistryServer jdbcRegistryServer) {
        this.jdbcRegistryProperties = jdbcRegistryProperties;
        this.jdbcRegistryServer = jdbcRegistryServer;
        this.jdbcRegistryClientIdentify =
                new JdbcRegistryClientIdentify(CodeGenerateUtils.genCode(), DEFAULT_CLIENT_NAME);
    }

    @Override
    public void start() {
        jdbcRegistryServer.registerClient(this);
    }

    @Override
    public JdbcRegistryClientIdentify getJdbcRegistryClientIdentify() {
        return jdbcRegistryClientIdentify;
    }

    @Override
    public void subscribeConnectionStateChange(ConnectionStateListener connectionStateListener) {
        jdbcRegistryServer.subscribeConnectionStateChange(connectionStateListener);
    }

    @Override
    public void subscribeJdbcRegistryDataChange(JdbcRegistryDataChangeListener jdbcRegistryDataChangeListener) {
        jdbcRegistryServer.subscribeJdbcRegistryDataChange(jdbcRegistryDataChangeListener);
    }

    @Override
    public Optional<JdbcRegistryDataDTO> getJdbcRegistryDataByKey(String key) {
        return jdbcRegistryServer.getJdbcRegistryDataByKey(key);
    }

    @Override
    public void putJdbcRegistryData(String key, String value, DataType dataType) {
        jdbcRegistryServer.putJdbcRegistryData(jdbcRegistryClientIdentify.getClientId(), key, value, dataType);
    }

    @Override
    public void deleteJdbcRegistryDataByKey(String key) {
        jdbcRegistryServer.deleteJdbcRegistryDataByKey(key);
    }

    @Override
    public List<JdbcRegistryDataDTO> listJdbcRegistryDataChildren(String key) {
        return jdbcRegistryServer.listJdbcRegistryDataChildren(key);
    }

    @Override
    public boolean existJdbcRegistryDataKey(String key) {
        return jdbcRegistryServer.existJdbcRegistryDataKey(key);
    }

    @Override
    public void acquireJdbcRegistryLock(String key) {
        jdbcRegistryServer.acquireJdbcRegistryLock(jdbcRegistryClientIdentify.getClientId(), key);
    }

    @Override
    public boolean acquireJdbcRegistryLock(String key, long timeout) {
        return jdbcRegistryServer.acquireJdbcRegistryLock(jdbcRegistryClientIdentify.getClientId(), key, timeout);
    }

    @Override
    public void releaseJdbcRegistryLock(String key) {
        jdbcRegistryServer.releaseJdbcRegistryLock(jdbcRegistryClientIdentify.getClientId(), key);
    }

    @Override
    public void close() {
        jdbcRegistryServer.deregisterClient(this);
        log.info("Closed JdbcRegistryClient: {}", jdbcRegistryClientIdentify);
    }

    @Override
    public boolean isConnectivity() {
        return jdbcRegistryServer.getServerState() == JdbcRegistryServerState.STARTED;
    }

}
