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

package org.apache.dolphinscheduler.plugin.registry.jdbc.repository;

import org.apache.dolphinscheduler.plugin.registry.jdbc.mapper.JdbcRegistryDataMapper;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DO.JdbcRegistryData;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.DataType;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataDTO;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRegistryDataRepository {

    @Autowired
    private JdbcRegistryDataMapper jdbcRegistryDataMapper;

    public List<JdbcRegistryDataDTO> selectAll() {
        return jdbcRegistryDataMapper
                .selectAll()
                .stream()
                .map(JdbcRegistryDataDTO::fromJdbcRegistryData)
                .collect(Collectors.toList());
    }

    public Optional<JdbcRegistryDataDTO> selectByKey(String key) {
        return Optional.ofNullable(jdbcRegistryDataMapper.selectByKey(key))
                .map(JdbcRegistryDataDTO::fromJdbcRegistryData);
    }

    public void deleteEphemeralDateByClientIds(List<Long> clientIds) {
        if (CollectionUtils.isEmpty(clientIds)) {
            return;
        }
        jdbcRegistryDataMapper.deleteByClientIds(clientIds, DataType.EPHEMERAL.name());
    }

    public void deleteByKey(String key) {
        jdbcRegistryDataMapper.deleteByKey(key);
    }

    public void insert(JdbcRegistryDataDTO jdbcRegistryData) {
        JdbcRegistryData jdbcRegistryDataDO = JdbcRegistryDataDTO.toJdbcRegistryData(jdbcRegistryData);
        jdbcRegistryDataMapper.insert(jdbcRegistryDataDO);
        jdbcRegistryData.setId(jdbcRegistryDataDO.getId());
    }

    public void updateById(JdbcRegistryDataDTO jdbcRegistryDataDTO) {
        jdbcRegistryDataMapper.updateById(JdbcRegistryDataDTO.toJdbcRegistryData(jdbcRegistryDataDTO));
    }
}
