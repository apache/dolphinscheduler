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

import java.time.Duration;

import lombok.Data;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;

@Data
@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "mysql")
@ConfigurationProperties(prefix = "registry")
public class MysqlRegistryProperties {

    /**
     * Used to schedule refresh the ephemeral data/ lock.
     */
    private Duration termRefreshInterval = Duration.ofSeconds(2);
    /**
     * Used to calculate the expire time,
     * e.g. if you set 2, and latest two refresh error, then the ephemeral data/lock will be expire.
     */
    private int termExpireTimes = 3;
    private HikariConfig hikariConfig;

}
