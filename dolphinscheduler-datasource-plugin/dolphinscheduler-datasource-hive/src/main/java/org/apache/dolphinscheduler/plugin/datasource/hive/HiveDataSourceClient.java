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

package org.apache.dolphinscheduler.plugin.datasource.hive;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.exception.DataSourceException;
import org.apache.dolphinscheduler.plugin.datasource.api.provider.JDBCDataSourceProvider;
import org.apache.dolphinscheduler.plugin.datasource.hive.utils.CommonUtil;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import sun.security.krb5.Config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.spi.utils.Constants.*;

public class HiveDataSourceClient extends CommonDataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(HiveDataSourceClient.class);

    private static final String DEFAULT_DATABASE_NAME = "default";

    private ScheduledExecutorService kerberosRenewalService;

    private Configuration hadoopConf;
    protected HikariDataSource oneSessionDataSource;
    private UserGroupInformation ugi;
    private JdbcTemplate oneSessionJdbcTemplate;
    private boolean retryGetConnection = true;

    /**
     * Stores metastore clients for direct accesses to HMS.
     */
    private MetaStoreClientPool metaStoreClientPool_;

    public HiveDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    protected void preInit() {
        logger.info("PreInit in {}", getClass().getName());
        this.kerberosRenewalService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("Hive-Kerberos-Renewal-Thread-").setDaemon(true).build());
    }

    @Override
    protected void initClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        logger.info("Create Configuration for hive configuration.");
        this.hadoopConf = createHadoopConf();
        logger.info("Create Configuration success.");

        logger.info("Create UserGroupInformation.");
        this.ugi = createUserGroupInformation(baseConnectionParam.getUser());
        logger.info("Create ugi success.");

        if (baseConnectionParam.getConnMetaStore()) {
            this.metaStoreClientPool_ = new MetaStoreClientPool(5, 2, getHadoopConf());
            logger.info("Create HiveMetaStore success.");
        }

        super.initClient(baseConnectionParam, dbType);
        this.oneSessionDataSource = JDBCDataSourceProvider.createOneSessionJdbcDataSource(baseConnectionParam, dbType);
        this.oneSessionJdbcTemplate = new JdbcTemplate(oneSessionDataSource);
        logger.info("Init {} success.", getClass().getName());
    }

    @Override
    protected void checkEnv(BaseConnectionParam baseConnectionParam) {
        super.checkEnv(baseConnectionParam);
        checkKerberosEnv();
    }

    private void checkKerberosEnv() {
        String krb5File = PropertyUtils.getString(JAVA_SECURITY_KRB5_CONF_PATH);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        if (kerberosStartupState && StringUtils.isNotBlank(krb5File)) {
            System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5File);
            try {
                Config.refresh();
                Class<?> kerberosName = Class.forName("org.apache.hadoop.security.authentication.util.KerberosName");
                Field field = kerberosName.getDeclaredField("defaultRealm");
                field.setAccessible(true);
                field.set(null, Config.getInstance().getDefaultRealm());
            } catch (Exception e) {
                throw DataSourceException.getInstance("Update Kerberos environment failed.", e);
            }
        }
    }

    private void checkHiveMetaStoreClient() {
        if (!super.baseConnectionParam.getConnMetaStore()) {
            throw DataSourceException.getInstance("Hive metastore not connect");
        }
        try {
            this.metaStoreClientPool_.getClient().getHiveClient().getAllDatabases();
        } catch (Exception e) {
            this.ugi.doAs((PrivilegedAction<Void>) () -> {
                try {
                    this.metaStoreClientPool_.getClient().getHiveClient().reconnect();
                    return null;
                } catch (Exception e2) {
                    throw DataSourceException.getInstance("Hive metastore connect failed." + " : " + hadoopConf.get("hive.metastore.uris"), e2);
                }
            });
        }
    }

    private UserGroupInformation createUserGroupInformation(String username) {
        String krb5File = PropertyUtils.getString(JAVA_SECURITY_KRB5_CONF_PATH);
        String keytab = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH);
        String principal = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME);

        try {
            UserGroupInformation ugi = CommonUtil.createUGI(getHadoopConf(), principal, keytab, krb5File, username);
            try {
                Field isKeytabField = ugi.getClass().getDeclaredField("isKeytab");
                isKeytabField.setAccessible(true);
                isKeytabField.set(ugi, true);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.warn(e.getMessage());
            }

            kerberosRenewalService.scheduleWithFixedDelay(() -> {
                try {
                    ugi.checkTGTAndReloginFromKeytab();
                } catch (IOException e) {
                    logger.error("Check TGT and Renewal from Keytab error", e);
                }
            }, 5, 5, TimeUnit.MINUTES);
            return ugi;
        } catch (IOException e) {
            throw DataSourceException.getInstance("createUserGroupInformation fail. ", e);
        }
    }

    protected Configuration createHadoopConf() {
        Configuration hadoopConf = new Configuration();
        hadoopConf.setBoolean("ipc.client.fallback-to-simple-auth-allowed", true);
        return hadoopConf;
    }

    protected Configuration getHadoopConf() {
        return this.hadoopConf;
    }

    @Override
    public Connection getConnection() {
        try {
            return oneSessionDataSource.getConnection();
        } catch (SQLException e) {
            boolean kerberosStartupState = PropertyUtils.getBoolean(HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
            if (retryGetConnection && kerberosStartupState) {
                retryGetConnection = false;
                createUserGroupInformation(baseConnectionParam.getUser());
                Connection connection = getConnection();
                retryGetConnection = true;
                return connection;
            }
            logger.error("get oneSessionDataSource Connection fail SQLException: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<String> getDatabaseList(String databasePattern) {
        try {
            checkHiveMetaStoreClient();
            if (StringUtils.isBlank(databasePattern)) {
                return this.metaStoreClientPool_.getClient().getHiveClient().getAllDatabases();
            }
            return this.metaStoreClientPool_.getClient().getHiveClient().getDatabases(databasePattern.trim());
        } catch (Exception e) {
            throw DataSourceException.getInstance("META_EXCEPTION", e);
        }
    }

    @Override
    public List<String> getTableList(String dbName, String schemaName, String tablePattern) {
        if (StringUtils.isBlank(dbName)) {
            dbName = DEFAULT_DATABASE_NAME;
        }
        try {
            checkHiveMetaStoreClient();
            if (StringUtils.isBlank(tablePattern)) {
                return this.metaStoreClientPool_.getClient().getHiveClient().getAllTables(dbName.trim());
            }
            return this.metaStoreClientPool_.getClient().getHiveClient().getTables(dbName.trim(), "*" + tablePattern.trim() + "*");
        } catch (Exception e) {
            throw DataSourceException.getInstance("META_EXCEPTION", e);
        }
    }

    @Override
    public List<Map<String, Object>> getTableStruct(String dbName, String schemaName, String tableName) {
        List<Map<String, Object>> struct = super.executeSql(dbName, schemaName, Boolean.FALSE, String.format("desc %s", tableName)).getMiddle();
        List<Map<String, Object>> filterStruct = new LinkedList<>();
        for (Map<String, Object> stringObjectMap : struct) {
            if (StringUtils.isBlank(stringObjectMap.get("col_name").toString())) {
                break;
            }
            filterStruct.add(stringObjectMap);
        }
        return filterStruct;
    }

    @Override
    protected JdbcTemplate getJdbcTemplate(Boolean oneSession) {
        if (oneSession) {
            return this.oneSessionJdbcTemplate;
        }
        return this.jdbcTemplate;
    }

    @Override
    public void close() {
        super.close();

        logger.info("close {}.", this.getClass().getSimpleName());
        kerberosRenewalService.shutdown();
        this.ugi = null;

        this.oneSessionDataSource.close();
        this.oneSessionDataSource = null;
    }
}
