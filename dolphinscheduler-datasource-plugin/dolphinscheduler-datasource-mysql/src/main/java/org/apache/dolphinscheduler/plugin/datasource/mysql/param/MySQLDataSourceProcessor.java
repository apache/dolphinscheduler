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

package org.apache.dolphinscheduler.plugin.datasource.mysql.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.google.auto.service.AutoService;

@AutoService(DataSourceProcessor.class)
@Slf4j
public class MySQLDataSourceProcessor extends AbstractDataSourceProcessor {

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";

    private static final String AUTO_DESERIALIZE = "autoDeserialize";

    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";

    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, MySQLDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        MySQLConnectionParam connectionParams = (MySQLConnectionParam) createConnectionParams(connectionJson);
        MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();

        mysqlDatasourceParamDTO.setUserName(connectionParams.getUser());
        mysqlDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        mysqlDatasourceParamDTO.setDriverJarName(connectionParams.getDriverJarName());
        mysqlDatasourceParamDTO.setOther(connectionParams.getOther());

        String address = connectionParams.getAddress();
        String[] hostSeperator = address.split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);
        mysqlDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        mysqlDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return mysqlDatasourceParamDTO;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        MySQLDataSourceParamDTO mysqlDatasourceParam = (MySQLDataSourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_MYSQL, mysqlDatasourceParam.getHost(),
                mysqlDatasourceParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, mysqlDatasourceParam.getDatabase());

        MySQLConnectionParam mysqlConnectionParam = new MySQLConnectionParam();
        mysqlConnectionParam.setJdbcUrl(jdbcUrl);
        mysqlConnectionParam.setDatabase(mysqlDatasourceParam.getDatabase());
        mysqlConnectionParam.setAddress(address);
        mysqlConnectionParam.setUser(mysqlDatasourceParam.getUserName());
        mysqlConnectionParam.setPassword(PasswordUtils.encodePassword(mysqlDatasourceParam.getPassword()));
        mysqlConnectionParam.setDriverClassName(getDatasourceDriver());
        mysqlConnectionParam.setDriverJarName(mysqlDatasourceParam.getDriverJarName());
        mysqlConnectionParam.setValidationQuery(getValidationQuery());
        mysqlConnectionParam.setOther(mysqlDatasourceParam.getOther());

        return mysqlConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, MySQLConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_MYSQL_CJ_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.MYSQL_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        MySQLConnectionParam mysqlConnectionParam = (MySQLConnectionParam) connectionParam;
        if (MapUtils.isNotEmpty(mysqlConnectionParam.getOther())) {
            return String.format("%s?%s", mysqlConnectionParam.getJdbcUrl(),
                    transformOther(mysqlConnectionParam.getOther()));
        }
        return mysqlConnectionParam.getJdbcUrl();
    }
    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws SQLException {
        MySQLConnectionParam mysqlConnectionParam = (MySQLConnectionParam) connectionParam;

        // 使用默认的驱动加载方式
        String driverClassName = mysqlConnectionParam.getDriverClassName();
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            driverClassName = getDatasourceDriver();
        }

        // 检查是否有指定的驱动JAR名称，如果有则使用动态驱动加载
        String driverJarName = mysqlConnectionParam.getDriverJarName();
        if (driverJarName != null && !driverJarName.trim().isEmpty()) {
            try {
                // 构建驱动JAR文件路径
                String driverJarPath = getDriverJarPath(driverJarName, DbType.MYSQL.name());

                // 动态加载驱动
                Driver driver = DynamicDriverLoader.loadDriver(driverJarPath, driverClassName);

                log.info("Using custom driver JAR: className={}, jarName={}", driverClassName, driverJarName);

                String user = mysqlConnectionParam.getUser();
                if (user.contains(AUTO_DESERIALIZE)) {
                    log.warn("sensitive param : {} in username field is filtered", AUTO_DESERIALIZE);
                    user = user.replace(AUTO_DESERIALIZE, "");
                }
                String password = PasswordUtils.decodePassword(mysqlConnectionParam.getPassword());
                if (password.contains(AUTO_DESERIALIZE)) {
                    log.warn("sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
                    password = password.replace(AUTO_DESERIALIZE, "");
                }

                Properties connectionProperties = getConnectionProperties(mysqlConnectionParam, user, password);

                // 使用动态加载的驱动创建连接
                return driver.connect(getJdbcUrl(connectionParam), connectionProperties);

            } catch (Exception e) {
                log.warn("Failed to load custom driver JAR {}, falling back to default driver loading", driverJarName,
                        e);
                // 降级到默认驱动加载方式
            }
        }

        String user = mysqlConnectionParam.getUser();
        if (user.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in username field is filtered", AUTO_DESERIALIZE);
            user = user.replace(AUTO_DESERIALIZE, "");
        }
        String password = PasswordUtils.decodePassword(mysqlConnectionParam.getPassword());
        if (password.contains(AUTO_DESERIALIZE)) {
            log.warn("sensitive param : {} in password field is filtered", AUTO_DESERIALIZE);
            password = password.replace(AUTO_DESERIALIZE, "");
        }

        Properties connectionProperties = getConnectionProperties(mysqlConnectionParam, user, password);

        return DriverManager.getConnection(getJdbcUrl(connectionParam), connectionProperties);
    }

    private Properties getConnectionProperties(MySQLConnectionParam mysqlConnectionParam, String user,
                                               String password) {
        Properties connectionProperties = new Properties();
        connectionProperties.put("user", user);
        connectionProperties.put("password", password);
        Map<String, String> paramMap = mysqlConnectionParam.getOther();
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

    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    @Override
    public DataSourceProcessor create() {
        return new MySQLDataSourceProcessor();
    }

    @Override
    public List<String> splitAndRemoveComment(String sql) {
        String cleanSQL = SQLParserUtils.removeComment(sql, com.alibaba.druid.DbType.mysql);
        return SQLParserUtils.split(cleanSQL, com.alibaba.druid.DbType.mysql);
    }

    private static boolean checkKeyIsLegitimate(String key) {
        return !key.contains(ALLOW_LOAD_LOCAL_IN_FILE_NAME)
                && !key.contains(AUTO_DESERIALIZE)
                && !key.contains(ALLOW_LOCAL_IN_FILE_NAME)
                && !key.contains(ALLOW_URL_IN_LOCAL_IN_FILE_NAME);
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isNotEmpty(otherMap)) {
            List<String> list = new ArrayList<>(otherMap.size());
            otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
            return String.join("&", list);
        }
        return null;
    }

    /**
     * 获取驱动JAR文件路径
     * @param jarFileName JAR文件名
     * @param dataSourceType 数据源类型
     * @return 完整的JAR文件路径
     */
    private String getDriverJarPath(String jarFileName, String dataSourceType) {
        // 根据数据源类型构建驱动目录路径
        String driverBasePath = "plugins/datasource-plugins/driver/" + dataSourceType.toLowerCase();

        // 优先检查driver/{datasource_type}目录（构建时打包的驱动）
        File libDriverDir = new File(driverBasePath);
        if (libDriverDir.exists() && libDriverDir.isDirectory()) {
            // 直接查找指定的JAR文件
            File jarFile = new File(libDriverDir, jarFileName);
            if (jarFile.exists()) {
                log.info("Found driver JAR in {}: {}", driverBasePath, jarFileName);
                return jarFile.getAbsolutePath();
            }

            // 如果精确匹配失败，尝试在目录中查找包含指定文件名的JAR
            File[] jarFiles = libDriverDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (jarFiles != null) {
                // 首先尝试精确匹配（不区分大小写）
                for (File file : jarFiles) {
                    if (file.getName().equalsIgnoreCase(jarFileName)) {
                        log.info("Found driver JAR (case-insensitive) in {}: {}", driverBasePath, file.getName());
                        return file.getAbsolutePath();
                    }
                }

                // 然后尝试包含匹配
                for (File file : jarFiles) {
                    if (file.getName().toLowerCase().contains(jarFileName.toLowerCase().replace(".jar", ""))) {
                        log.info("Found approximate match for {} in {}: {}", jarFileName, driverBasePath,
                                file.getName());
                        return file.getAbsolutePath();
                    }
                }
            }
        }

        throw new RuntimeException("Driver JAR file not found: " + jarFileName
                + ". Please ensure the driver JAR is placed in one of the following locations: " +
                "driver/mysql/ directory");
    }
}
