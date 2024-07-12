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

import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DO.JdbcRegistryData;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdbcRegistryDataDTO {

    private Long id;
    private String dataKey;
    private String dataValue;
    private String dataType;
    private Long clientId;
    private Date createTime;
    private Date lastUpdateTime;

    public static JdbcRegistryDataDTO fromJdbcRegistryData(JdbcRegistryData jdbcRegistryData) {
        return JdbcRegistryDataDTO.builder()
                .id(jdbcRegistryData.getId())
                .dataKey(jdbcRegistryData.getDataKey())
                .dataValue(jdbcRegistryData.getDataValue())
                .dataType(jdbcRegistryData.getDataType())
                .clientId(jdbcRegistryData.getClientId())
                .createTime(jdbcRegistryData.getCreateTime())
                .lastUpdateTime(jdbcRegistryData.getLastUpdateTime())
                .build();
    }

    public static JdbcRegistryData toJdbcRegistryData(JdbcRegistryDataDTO jdbcRegistryData) {
        return JdbcRegistryData.builder()
                .id(jdbcRegistryData.getId())
                .dataKey(jdbcRegistryData.getDataKey())
                .dataValue(jdbcRegistryData.getDataValue())
                .dataType(jdbcRegistryData.getDataType())
                .clientId(jdbcRegistryData.getClientId())
                .createTime(jdbcRegistryData.getCreateTime())
                .lastUpdateTime(jdbcRegistryData.getLastUpdateTime())
                .build();
    }

}
