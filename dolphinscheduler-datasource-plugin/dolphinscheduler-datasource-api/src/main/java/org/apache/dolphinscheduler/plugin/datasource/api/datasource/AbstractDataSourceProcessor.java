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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DynamicDriverLoader;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;

@Slf4j
public abstract class AbstractDataSourceProcessor implements DataSourceProcessor {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.\\,]+$");

    private static final Pattern IPV6_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.\\:\\[\\]\\,]+$");

    private static final Pattern DATABASE_PATTER = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern PARAMS_PATTER = Pattern.compile("^[a-zA-Z0-9\\-\\_\\/\\@\\.\\:]+$");

    private static final Set<String> POSSIBLE_MALICIOUS_KEYS = Sets.newHashSet("allowLoadLocalInfile");

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";

    private static final String AUTO_DESERIALIZE = "autoDeserialize";

    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";

    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        if (!baseDataSourceParamDTO.getType().equals(DbType.REDSHIFT)) {
            // due to redshift use not regular hosts
            checkHost(baseDataSourceParamDTO.getHost());
        }
        checkDatabasePatter(baseDataSourceParamDTO.getDatabase());
        checkOther(baseDataSourceParamDTO.getOther());
    }

    /**
     * Check the host is valid
     *
     * @param host datasource host
     */
    protected void checkHost(String host) {
        if (com.google.common.net.InetAddresses.isInetAddress(host)) {
        } else if (!IPV4_PATTERN.matcher(host).matches() || !IPV6_PATTERN.matcher(host).matches()) {
            throw new IllegalArgumentException("datasource host illegal");
        }
    }

    /**
     * check database name is valid
     *
     * @param database database name
     */
    protected void checkDatabasePatter(String database) {
        if (!DATABASE_PATTER.matcher(database).matches()) {
            throw new IllegalArgumentException("database name illegal");
        }
    }

    /**
     * check other is valid
     *
     * @param other other
     */
    protected void checkOther(Map<String, String> other) {
        if (MapUtils.isEmpty(other)) {
            return;
        }

        if (!Sets.intersection(other.keySet(), POSSIBLE_MALICIOUS_KEYS).isEmpty()) {
            throw new IllegalArgumentException("Other params include possible malicious keys.");
        }

        for (Map.Entry<String, String> entry : other.entrySet()) {
            if (!PARAMS_PATTER.matcher(entry.getKey()).matches()) {
                throw new IllegalArgumentException("datasource other params: " + entry.getKey() + " illegal");
            }
        }
    }

    protected Map<String, String> transformOtherParamToMap(String other) {
        if (StringUtils.isBlank(other)) {
            return Collections.emptyMap();
        }
        return JSONUtils.parseObject(other, new TypeReference<Map<String, String>>() {
        });
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}@{3}", dbType.getName(), baseConnectionParam.getUser(),
                PasswordUtils.encodePassword(baseConnectionParam.getPassword()), baseConnectionParam.getJdbcUrl());
    }

    @Override
    public boolean checkDataSourceConnectivity(ConnectionParam connectionParam) {
        try (Connection connection = getConnection(connectionParam)) {
            return true;
        } catch (Exception e) {
            log.error("Check datasource connectivity for: {} error", getDbType().name(), e);
            return false;
        }
    }

    @Override
    public List<String> splitAndRemoveComment(String sql) {
        String cleanSQL = SQLParserUtils.removeComment(sql, com.alibaba.druid.DbType.other);
        return SQLParserUtils.split(cleanSQL, com.alibaba.druid.DbType.other);
    }

    /**
     * Unified method to get connection with dynamic driver loading support
     * This method provides centralized driver loading logic for all data source plugins
     *
     * @param connectionParam Connection parameters
     * @param defaultDriverClassName Default driver class name if not specified in connection param
     * @return Database connection
     * @throws SQLException If connection fails
     */
    protected Connection getConnectionWithDriver(ConnectionParam connectionParam,
                                                 String defaultDriverClassName) throws SQLException {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;

        // Use custom driver class name if specified, otherwise use default
        String driverClassName = baseConnectionParam.getDriverClassName();
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            driverClassName = defaultDriverClassName;
        }

        // Check if custom driver JAR is specified, use dynamic driver loading if available
        String driverJarName = baseConnectionParam.getDriverJarName();
        if (driverJarName != null && !driverJarName.trim().isEmpty()) {
            try {
                // Build driver JAR file path
                String driverJarPath = DynamicDriverLoader.getDriverJarPath(driverJarName, getDbType().name());

                // Dynamically load driver
                Driver driver = DynamicDriverLoader.loadDriver(driverJarPath, driverClassName);

                log.info("Using custom driver JAR: className={}, jarName={}", driverClassName, driverJarName);

                // Filter sensitive parameters
                String user = filterSensitiveParams(baseConnectionParam.getUser());
                String password =
                        PasswordUtils.decodePassword(filterSensitiveParams(baseConnectionParam.getPassword()));

                Properties connectionProperties = getConnectionProperties(baseConnectionParam);
                connectionProperties.setProperty("user", user);
                connectionProperties.setProperty("password", password);

                // Create connection using dynamically loaded driver
                return driver.connect(getJdbcUrl(connectionParam), connectionProperties);

            } catch (Exception e) {
                log.warn("Failed to load custom driver JAR {}, falling back to default driver loading", driverJarName,
                        e);
                // Fallback to default driver loading method
            }
        }

        // Use default driver loading method
        String user = filterSensitiveParams(baseConnectionParam.getUser());
        String password = PasswordUtils.decodePassword(filterSensitiveParams(baseConnectionParam.getPassword()));

        return JdbcDriverConnectionProvider.builder()
                .jdbcDriverClassName(defaultDriverClassName)
                .jdbcUrl(getJdbcUrl(baseConnectionParam))
                .username(user)
                .password(password)
                .properties(getConnectionProperties(baseConnectionParam))
                .build()
                .getConnection();
    }

    /**
     * Filter sensitive parameters from user input
     * @param value Input value
     * @return Filtered value
     */
    private String filterSensitiveParams(String value) {
        if (value == null) {
            return null;
        }

        final String AUTO_DESERIALIZE = "autoDeserialize";
        if (value.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in field is filtered", AUTO_DESERIALIZE);
            return value.replace(AUTO_DESERIALIZE, "");
        }
        return value;
    }

    private static boolean checkKeyIsLegitimate(String key) {
        return !key.contains(ALLOW_LOAD_LOCAL_IN_FILE_NAME)
                && !key.contains(AUTO_DESERIALIZE)
                && !key.contains(ALLOW_LOCAL_IN_FILE_NAME)
                && !key.contains(ALLOW_URL_IN_LOCAL_IN_FILE_NAME);
    }

    /**
     * Get connection properties from connection parameters
     * @param baseConnectionParam Connection parameters
     * @return Properties object
     */
    protected Properties getConnectionProperties(BaseConnectionParam baseConnectionParam) {
        Properties connectionProperties = new Properties();
        Map<String, String> paramMap = baseConnectionParam.getOther();
        if (MapUtils.isNotEmpty(paramMap)) {
            paramMap.forEach((k, v) -> {
                if (!checkKeyIsLegitimate(k)) {
                    log.info("Key `{}` is not legitimate for security reason", k);
                    return;
                }
                connectionProperties.put(k, v);
            });
        }
        connectionProperties.put(AUTO_DESERIALIZE, "false");
        connectionProperties.put(ALLOW_LOAD_LOCAL_IN_FILE_NAME, "false");
        connectionProperties.put(ALLOW_LOCAL_IN_FILE_NAME, "false");
        connectionProperties.put(ALLOW_URL_IN_LOCAL_IN_FILE_NAME, "false");
        return connectionProperties;
    }
}
