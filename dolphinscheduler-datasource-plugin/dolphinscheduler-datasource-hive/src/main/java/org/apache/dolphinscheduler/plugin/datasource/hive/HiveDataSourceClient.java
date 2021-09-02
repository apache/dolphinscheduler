package org.apache.dolphinscheduler.plugin.datasource.hive;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.exception.BaseException;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.CommonDataSourceClient;
import org.apache.dolphinscheduler.dao.provider.JdbcDataSourceProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.druid.pool.DruidDataSource;

public class HiveDataSourceClient extends CommonDataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(HiveDataSourceClient.class);

    protected DruidDataSource oneSessionDataSource;
    private JdbcTemplate oneSessionJdbcTemplate;
    private Configuration configuration;

    public HiveDataSourceClient(BaseConnectionParam baseConnectionParam) {
        super(baseConnectionParam);
    }

    @Override
    protected void initClient(BaseConnectionParam baseConnectionParam) {
        logger.info("Create Configuration for hive configuration.");
        loginKerberos();

        super.initClient(baseConnectionParam);
        this.oneSessionDataSource = JdbcDataSourceProvider.createOneSessionJdbcDataSource(baseConnectionParam);
        this.oneSessionJdbcTemplate = new JdbcTemplate(oneSessionDataSource);
        logger.info("Init {} success.", getClass().getName());
    }

    private void loginKerberos() {
        try {
            if (getKerberosStartupState()) {
                System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF, StringUtils.defaultIfBlank(null, PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH)));
                configuration = new Configuration();
                configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION, Constants.KERBEROS);
                UserGroupInformation.setConfiguration(configuration);
                UserGroupInformation.loginUserFromKeytab(StringUtils.defaultIfBlank(null, PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME)),
                        StringUtils.defaultIfBlank(null, PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH)));
            }
        } catch (IOException e) {
            throw BaseException.getInstance("load Kerberos environment failed", e);
        }
    }

    private boolean getKerberosStartupState() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        return resUploadType == ResUploadType.HDFS && kerberosStartupState;
    }

    @Override
    public Connection getConnection() {
        try {
            return oneSessionDataSource.getConnection();
        } catch (SQLException e) {
            logger.error("get oneSessionDataSource Connection fail SQLException: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        super.close();
        logger.info("close HiveDataSourceClient.");

        this.oneSessionDataSource.close();
        this.oneSessionDataSource = null;
        this.oneSessionJdbcTemplate = null;
    }
}
