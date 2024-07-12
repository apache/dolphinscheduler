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

package org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DO.JdbcRegistryData;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DO.JdbcRegistryDataChanceEvent;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdbcRegistryDataChanceEventDTO {

    private Long id;

    private EventType eventType;

    private JdbcRegistryDataDTO jdbcRegistryData;

    private Date createTime;

    public enum EventType {
        ADD,
        UPDATE,
        DELETE;

    }

    public static JdbcRegistryDataChanceEventDTO fromJdbcRegistryDataChanceEvent(JdbcRegistryDataChanceEvent jdbcRegistryDataChanceEvent) {
        JdbcRegistryData jdbcRegistryData =
                JSONUtils.parseObject(jdbcRegistryDataChanceEvent.getJdbcRegistryData(), JdbcRegistryData.class);
        if (jdbcRegistryData == null) {
            throw new IllegalArgumentException(
                    "jdbcRegistryData: " + jdbcRegistryDataChanceEvent.getJdbcRegistryData() + " is invalidated");
        }
        return JdbcRegistryDataChanceEventDTO.builder()
                .id(jdbcRegistryDataChanceEvent.getId())
                .jdbcRegistryData(JdbcRegistryDataDTO.fromJdbcRegistryData(jdbcRegistryData))
                .eventType(EventType.valueOf(jdbcRegistryDataChanceEvent.getEventType()))
                .createTime(jdbcRegistryDataChanceEvent.getCreateTime())
                .build();
    }

    public static JdbcRegistryDataChanceEvent toJdbcRegistryDataChanceEvent(JdbcRegistryDataChanceEventDTO jdbcRegistryDataChanceEvent) {
        return JdbcRegistryDataChanceEvent.builder()
                .id(jdbcRegistryDataChanceEvent.getId())
                .jdbcRegistryData(JSONUtils.toJsonString(jdbcRegistryDataChanceEvent.getJdbcRegistryData()))
                .eventType(jdbcRegistryDataChanceEvent.getEventType().name())
                .createTime(jdbcRegistryDataChanceEvent.getCreateTime())
                .build();
    }

}
