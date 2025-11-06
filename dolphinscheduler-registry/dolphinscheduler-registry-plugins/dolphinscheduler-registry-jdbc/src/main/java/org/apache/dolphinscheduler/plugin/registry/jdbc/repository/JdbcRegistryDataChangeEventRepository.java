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

import org.apache.dolphinscheduler.plugin.registry.jdbc.mapper.JdbcRegistryDataChangeEventMapper;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DO.JdbcRegistryDataChangeEvent;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryDataChangeEventDTO;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class JdbcRegistryDataChangeEventRepository {

    private final JdbcRegistryDataChangeEventMapper jdbcRegistryDataChangeEventMapper;

    public JdbcRegistryDataChangeEventRepository(JdbcRegistryDataChangeEventMapper jdbcRegistryDataChangeEventMapper) {
        this.jdbcRegistryDataChangeEventMapper = jdbcRegistryDataChangeEventMapper;
    }

    public long getMaxJdbcRegistryDataChangeEventId() {
        Long maxId = jdbcRegistryDataChangeEventMapper.getMaxId();
        if (maxId == null) {
            return -1;
        } else {
            return maxId;
        }
    }

    public List<JdbcRegistryDataChangeEventDTO> selectJdbcRegistryDataChangeEventWhereIdAfter(long id) {
        return jdbcRegistryDataChangeEventMapper.selectJdbcRegistryDataChangeEventWhereIdAfter(id)
                .stream()
                .map(JdbcRegistryDataChangeEventDTO::fromJdbcRegistryDataChangeEvent)
                .collect(Collectors.toList());
    }

    public void insert(JdbcRegistryDataChangeEventDTO registryDataChangeEvent) {
        JdbcRegistryDataChangeEvent jdbcRegistryDataChangeEvent =
                JdbcRegistryDataChangeEventDTO.toJdbcRegistryDataChangeEvent(registryDataChangeEvent);
        jdbcRegistryDataChangeEventMapper.insert(jdbcRegistryDataChangeEvent);
        registryDataChangeEvent.setId(jdbcRegistryDataChangeEvent.getId());
    }

    public void deleteJdbcRegistryDataChangeEventBeforeCreateTime(Date createTime) {
        jdbcRegistryDataChangeEventMapper.deleteJdbcRegistryDataChangeEventBeforeCreateTime(createTime);
    }
}
