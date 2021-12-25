package org.apache.dolphinscheduler.plugin.datasource.api.provider;

import org.apache.dolphinscheduler.plugin.datasource.api.exception.DataSourceException;
import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Driver;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceFactoryBasicImpl implements DataSourceFactory<BasicDataSource> {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactoryBasicImpl.class);

    @Override
    public BasicDataSource createDataSource(JdbcConnectionParam connectionParam) {
        BasicDataSource dataSource;
        DbType dbType = connectionParam.getDbType();
        if (dbType.equals(DbType.HIVE) || dbType.equals(DbType.SPARK)) {
            dataSource = createOneSessionJdbcDataSource(connectionParam);
        } else {
            dataSource = createJdbcDataSource(connectionParam);
        }
        return dataSource;
    }

    /**
     * @return Multi Session Jdbc DataSource
     */
    public static BasicDataSource createJdbcDataSource(JdbcConnectionParam connectionParam) {
        logger.info("Creating BasicDataSource pool for JdbcUrl:{}", connectionParam.getJdbcUrl());
        BasicDataSource dataSource = new BasicDataSource();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Driver driver = getDataSourceDriver(connectionParam, classLoader);

        dataSource.setDriverClassLoader(classLoader);
        if (driver != null) {
            dataSource.setDriver(driver);
        }
        dataSource.setDriverClassName(connectionParam.getDriverClassName());
        dataSource.setUrl(connectionParam.getJdbcUrl());
        dataSource.setUsername(connectionParam.getUser());
        dataSource.setPassword(connectionParam.getPassword());

        if (MapUtils.isNotEmpty(connectionParam.getProps())) {
            connectionParam.getProps().forEach(dataSource::addConnectionProperty);
        }

        logger.info("Creating BasicDataSource pool success.");
        return dataSource;
    }

    /**
     * @return One Session Jdbc DataSource
     */
    public static BasicDataSource createOneSessionJdbcDataSource(JdbcConnectionParam connectionParam) {
        logger.info("Creating OneSession BasicDataSource pool for JdbcUrl:{}", connectionParam.getJdbcUrl());
        BasicDataSource dataSource = new BasicDataSource();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Driver driver = getDataSourceDriver(connectionParam, classLoader);

        dataSource.setDriverClassLoader(classLoader);
        if (driver != null) {
            dataSource.setDriver(driver);
        }
        dataSource.setDriverClassName(connectionParam.getDriverClassName());
        dataSource.setUrl(connectionParam.getJdbcUrl());
        dataSource.setUsername(connectionParam.getUser());
        dataSource.setPassword(connectionParam.getPassword());

        dataSource.setMaxTotal(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxIdle(1);

        if (MapUtils.isNotEmpty(connectionParam.getProps())) {
            connectionParam.getProps().forEach(dataSource::addConnectionProperty);
        }
        logger.info("Creating OneSession BasicDataSource pool success.");
        return dataSource;
    }

    private static Driver getDataSourceDriver(JdbcConnectionParam connectionParam, ClassLoader classLoader) {
        Driver driver;
        try {
            final Class<?> clazz = Class.forName(connectionParam.getDriverClassName(), true, classLoader);
            driver = (Driver) clazz.newInstance();
        } catch (Exception e) {
            throw DataSourceException.getInstance("Jdbc driver init error.", e);
        }
        return driver;
    }

    @Override
    public void destroy(DataSource dataSource) {
        try {
            ((BasicDataSource) dataSource).close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
