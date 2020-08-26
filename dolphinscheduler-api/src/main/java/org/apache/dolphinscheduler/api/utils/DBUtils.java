package org.apache.dolphinscheduler.api.utils;

import static org.apache.dolphinscheduler.common.utils.PropertyUtils.getString;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.ClickHouseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DB2ServerDataSource;
import org.apache.dolphinscheduler.dao.datasource.HiveDataSource;
import org.apache.dolphinscheduler.dao.datasource.MySQLDataSource;
import org.apache.dolphinscheduler.dao.datasource.OracleDataSource;
import org.apache.dolphinscheduler.dao.datasource.PostgreDataSource;
import org.apache.dolphinscheduler.dao.datasource.PrestoDataSource;
import org.apache.dolphinscheduler.dao.datasource.SQLServerDataSource;
import org.apache.dolphinscheduler.dao.datasource.SparkDataSource;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * check connection
     *
     * @param type data source type
     * @param parameter data source parameters
     * @return true if connect successfully, otherwise false
     */
    public static boolean checkConnection(DbType type, String parameter) {
        Boolean isConnection = false;
        Connection con = getConnection(type, parameter);
        if (con != null) {
            isConnection = true;
            try {
                con.close();
            } catch (SQLException e) {
                logger.error("close connection fail at DataSourceService::checkConnection()", e);
            }
        }
        return isConnection;
    }

    /**
     * get connection
     *
     * @param dbType datasource type
     * @param parameter parameter
     * @return connection for datasource
     */
    public static Connection getConnection(DbType dbType, String parameter) {
        Connection connection = null;
        BaseDataSource datasource = null;
        try {
            switch (dbType) {
                case POSTGRESQL:
                    datasource = JSONUtils.parseObject(parameter, PostgreDataSource.class);
                    Class.forName(Constants.ORG_POSTGRESQL_DRIVER);
                    break;
                case MYSQL:
                    datasource = JSONUtils.parseObject(parameter, MySQLDataSource.class);
                    Class.forName(Constants.COM_MYSQL_JDBC_DRIVER);
                    break;
                case HIVE:
                case SPARK:
                    if (CommonUtils.getKerberosStartupState()) {
                        System.setProperty(org.apache.dolphinscheduler.common.Constants.JAVA_SECURITY_KRB5_CONF,
                                getString(org.apache.dolphinscheduler.common.Constants.JAVA_SECURITY_KRB5_CONF_PATH));
                        Configuration configuration = new Configuration();
                        configuration.set(org.apache.dolphinscheduler.common.Constants.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
                        UserGroupInformation.setConfiguration(configuration);
                        UserGroupInformation.loginUserFromKeytab(getString(org.apache.dolphinscheduler.common.Constants.LOGIN_USER_KEY_TAB_USERNAME),
                                getString(org.apache.dolphinscheduler.common.Constants.LOGIN_USER_KEY_TAB_PATH));
                    }
                    if (dbType == DbType.HIVE) {
                        datasource = JSONUtils.parseObject(parameter, HiveDataSource.class);
                    } else if (dbType == DbType.SPARK) {
                        datasource = JSONUtils.parseObject(parameter, SparkDataSource.class);
                    }
                    Class.forName(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER);
                    break;
                case CLICKHOUSE:
                    datasource = JSONUtils.parseObject(parameter, ClickHouseDataSource.class);
                    Class.forName(Constants.COM_CLICKHOUSE_JDBC_DRIVER);
                    break;
                case ORACLE:
                    datasource = JSONUtils.parseObject(parameter, OracleDataSource.class);
                    Class.forName(Constants.COM_ORACLE_JDBC_DRIVER);
                    break;
                case SQLSERVER:
                    datasource = JSONUtils.parseObject(parameter, SQLServerDataSource.class);
                    Class.forName(Constants.COM_SQLSERVER_JDBC_DRIVER);
                    break;
                case DB2:
                    datasource = JSONUtils.parseObject(parameter, DB2ServerDataSource.class);
                    Class.forName(Constants.COM_DB2_JDBC_DRIVER);
                    break;
                case PRESTO:
                    datasource = JSONUtils.parseObject(parameter, PrestoDataSource.class);
                    Class.forName(Constants.COM_PRESTO_JDBC_DRIVER);
                    break;
                default:
                    break;
            }

            if (datasource != null) {
                connection = DriverManager.getConnection(datasource.getJdbcUrl(), datasource.getUser(), datasource.getPassword());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return connection;
    }
}
