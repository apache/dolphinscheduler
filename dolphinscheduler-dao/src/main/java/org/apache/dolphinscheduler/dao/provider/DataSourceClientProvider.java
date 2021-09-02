package org.apache.dolphinscheduler.dao.provider;

import static org.apache.dolphinscheduler.common.enums.DbType.HIVE;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.exception.BaseException;
import org.apache.dolphinscheduler.common.utils.ClassLoaderUtils;
import org.apache.dolphinscheduler.common.utils.ReflectionUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.DataSourceClient;
import org.apache.dolphinscheduler.dao.datasource.JdbcDriverManager;

import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.sql.Driver;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * DataSource Plug in dynamic loading
 */
public class DataSourceClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceClientProvider.class);

    private static final JdbcDriverManager jdbcDriverManagerInstance = JdbcDriverManager.getInstance();

    public static DataSourceClient createDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        logger.info("Creating the createDataSourceClient. JdbcUrl: {} ", baseConnectionParam.getJdbcUrl());
        //Check jdbc driver location
        checkDriverLocation(baseConnectionParam, dbType);

        logger.info("Creating the ClassLoader for the jdbc driver and plugin.");
        ClassLoader driverClassLoader = getDriverClassLoader(baseConnectionParam, dbType);

        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(driverClassLoader);
            return createDataSourceClientWithClassLoader(baseConnectionParam, driverClassLoader, dbType);
        } finally {
            Thread.currentThread().setContextClassLoader(threadClassLoader);
        }
    }

    protected static void checkDriverLocation(BaseConnectionParam baseConnectionParam, DbType dbType) {
        final String driverLocation = baseConnectionParam.getDriverLocation();
        if (StringUtils.isBlank(driverLocation)) {
            logger.warn("No jdbc driver provide,will use randomly driver jar for {}.", dbType.getDescp());
            baseConnectionParam.setDriverLocation(jdbcDriverManagerInstance.getDefaultDriverPluginPath(dbType.getDescp()));
        }
    }

    protected static ClassLoader getDriverClassLoader(BaseConnectionParam baseConnectionParam, DbType dbType) {
        FilenameFilter filenameFilter = (dir, name) -> name != null && name.endsWith(".jar");
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader;

        String locationString = baseConnectionParam.getDriverLocation();
        logger.info("Driver location: {}", locationString);
        HashSet<String> paths = Sets.newHashSet(locationString);
        try {
            classLoader = ClassLoaderUtils.getCustomClassLoader(paths, threadClassLoader, filenameFilter);
        } catch (final MalformedURLException e) {
            throw BaseException.getInstance("Invalid jdbc driver location.", e);
        }
        loadJdbcDriver(classLoader, dbType, baseConnectionParam.getJdbcUrl());

        String pluginPath = JdbcDriverManager.getInstance().getPluginPath(dbType);
        logger.info("Plugin location: {}", pluginPath);
        paths.add(pluginPath);

        if (dbType == HIVE || dbType == DbType.SPARK) {
            try {
                Class.forName("org.apache.hadoop.conf.Configuration", true, classLoader);
                Class.forName("org.apache.hadoop.security.UserGroupInformation", true, classLoader);
                Class.forName("org.apache.hadoop.fs.FileSystem", true, classLoader);
            } catch (ClassNotFoundException cnf) {
                paths.add(JdbcDriverManager.getInstance().getHadoopClientPath());
            }
        }

        try {
            classLoader = ClassLoaderUtils.getCustomClassLoader(paths, threadClassLoader, filenameFilter);
        } catch (final MalformedURLException e) {
            throw new RuntimeException("Plugin classpath init error.");
        }
        logger.info("Create InstanceClassLoader Success {}", classLoader.toString());
        return classLoader;
    }

    protected static void loadJdbcDriver(ClassLoader classLoader, DbType dbType, String jdbcUrl) {
        Boolean loaded;
        String drv = dbType.getDefaultDriverClass();
        try {
            final Class<?> clazz = Class.forName(drv, true, classLoader);
            final Driver driver = (Driver) clazz.newInstance();
            if (!driver.acceptsURL(jdbcUrl)) {
                logger.warn("{} : Driver {} cannot accept url.", "Jdbc driver loading error", drv);
                throw BaseException.getInstance("Jdbc driver loading error");
            }
            logger.info("Loader jdbc driver {} success.", drv);
            loaded = Boolean.TRUE;
        } catch (final Exception e) {
            logger.warn("The specified driver not suitable.");
            loaded =  Boolean.FALSE;
        }
        if (!loaded) {
            throw BaseException.getInstance("Jdbc driver loading error");
        }
    }

    protected static DataSourceClient createDataSourceClientWithClassLoader(BaseConnectionParam baseConnectionParam, ClassLoader classLoader, DbType dbType) {
        Class<?> dataSourceClientClass;
        DataSourceClient dataSourceClient;
        try {
            switch (dbType) {
                case MYSQL:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.plugin.datasource.mysql.MysqlDataSourceClient", true, classLoader);
                    break;
                case POSTGRESQL:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.plugin.datasource.postgresql.PostgreSQLDataSourceClient", true, classLoader);
                    break;
                case HIVE:
                case SPARK:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.plugin.datasource.hive.HiveDataSourceClient", true, classLoader);
                    break;
                case CLICKHOUSE:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.plugin.datasource.clickhouse.ClickhouseDataSourceClient", true, classLoader);
                    break;
                case ORACLE:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.plugin.datasource.oracle.OracleDataSourceClient", true, classLoader);
                    break;
                case SQLSERVER:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.plugin.datasource.sqlserver.SqlserverDataSourceClient", true, classLoader);
                    break;
                case DB2:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.plugin.datasource.db2.DB2DataSourceClient", true, classLoader);
                    break;
                default:
                    dataSourceClientClass = Class.forName("org.apache.dolphinscheduler.common.datasource.CommonDataSourceClient", true, classLoader);
            }
            logger.info("Reflection: {}", dataSourceClientClass);
            dataSourceClient = (DataSourceClient) ReflectionUtils.newInstance(dataSourceClientClass, baseConnectionParam);
        } catch (Exception e) {
            throw BaseException.getInstance("Datasource plugin initialize fail", e);
        }
        logger.info("Create DataSourceClient {} for {} success.", baseConnectionParam.getJdbcUrl(), dbType.getDescp());
        return dataSourceClient;
    }

}
