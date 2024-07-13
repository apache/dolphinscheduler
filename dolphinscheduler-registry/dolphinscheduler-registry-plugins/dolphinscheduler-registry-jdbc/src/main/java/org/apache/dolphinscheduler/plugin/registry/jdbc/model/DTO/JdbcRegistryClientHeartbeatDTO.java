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
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DO.JdbcRegistryClientHeartbeat;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdbcRegistryClientHeartbeatDTO {

    private Long id;

    // clientName
    private String clientName;

    private Long lastHeartbeatTime;

    private ClientConfig clientConfig;

    private Date createTime;

    public static JdbcRegistryClientHeartbeatDTO fromJdbcRegistryClientHeartbeat(JdbcRegistryClientHeartbeat jdbcRegistryClientHeartbeat) {
        return JdbcRegistryClientHeartbeatDTO.builder()
                .id(jdbcRegistryClientHeartbeat.getId())
                .clientName(jdbcRegistryClientHeartbeat.getClientName())
                .lastHeartbeatTime(jdbcRegistryClientHeartbeat.getLastHeartbeatTime())
                .clientConfig(
                        JSONUtils.parseObject(jdbcRegistryClientHeartbeat.getConnectionConfig(), ClientConfig.class))
                .createTime(jdbcRegistryClientHeartbeat.getCreateTime())
                .build();
    }

    public static JdbcRegistryClientHeartbeat toJdbcRegistryClientHeartbeat(JdbcRegistryClientHeartbeatDTO jdbcRegistryClientHeartbeatDTO) {
        return JdbcRegistryClientHeartbeat.builder()
                .id(jdbcRegistryClientHeartbeatDTO.getId())
                .clientName(jdbcRegistryClientHeartbeatDTO.getClientName())
                .lastHeartbeatTime(jdbcRegistryClientHeartbeatDTO.getLastHeartbeatTime())
                .connectionConfig(JSONUtils.toJsonString(jdbcRegistryClientHeartbeatDTO.getClientConfig()))
                .createTime(jdbcRegistryClientHeartbeatDTO.getCreateTime())
                .build();
    }

    public boolean isDead() {
        // check if the client connection is expired.
        return System.currentTimeMillis() - lastHeartbeatTime > clientConfig.getSessionTimeout();
    }

    @SneakyThrows
    @Override
    public JdbcRegistryClientHeartbeatDTO clone() {
        return JdbcRegistryClientHeartbeatDTO.builder()
                .id(id)
                .clientName(clientName)
                .lastHeartbeatTime(lastHeartbeatTime)
                .clientConfig(clientConfig)
                .createTime(createTime)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientConfig {

        @Builder.Default
        private long sessionTimeout = 60 * 1000L;

    }

}
