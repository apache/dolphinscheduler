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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "mysql")
@ConfigurationProperties(prefix = "registry")
public class MysqlRegistryProperties {

    /**
     * Used to schedule refresh the ephemeral data/ lock.
     */
    private long termRefreshInterval = MysqlRegistryConstant.TERM_REFRESH_INTERVAL;
    /**
     * Used to calculate the expire time,
     * e.g. if you set 2, and latest two refresh error, then the ephemeral data/lock will be expire.
     */
    private int termExpireTimes = MysqlRegistryConstant.TERM_EXPIRE_TIMES;
    private MysqlDatasourceProperties mysqlDatasource;

    @Data
    public static final class MysqlDatasourceProperties {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private int maximumPoolSize;
        private long connectionTimeout;
        private long idleTimeout;
    }

}
