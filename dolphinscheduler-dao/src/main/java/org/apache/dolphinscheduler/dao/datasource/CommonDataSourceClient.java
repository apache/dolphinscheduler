package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.exception.BaseException;
import org.apache.dolphinscheduler.dao.provider.JdbcDataSourceProvider;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Stopwatch;

public class CommonDataSourceClient implements DataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(CommonDataSourceClient.class);

    public static final String COMMON_USER = "root";
    public static final String COMMON_PASSWORD = "123456";
    public static final String COMMON_VALIDATION_QUERY = "select 1";

    protected final BaseConnectionParam baseConnectionParam;
    protected DruidDataSource druidDataSource;
    protected JdbcTemplate jdbcTemplate;

    public CommonDataSourceClient(BaseConnectionParam baseConnectionParam) {
        this.baseConnectionParam = baseConnectionParam;
        preInit();
        checkEnv(baseConnectionParam);
        initClient(baseConnectionParam);
        checkClient();
    }

    protected void preInit() {
        logger.info("preInit in CommonDataSourceClient");
    }

    protected void checkEnv(BaseConnectionParam baseConnectionParam) {
        checkValidationQuery(baseConnectionParam);
        checkUser(baseConnectionParam);
    }

    protected void initClient(BaseConnectionParam baseConnectionParam) {
        this.druidDataSource = JdbcDataSourceProvider.createJdbcDataSource(baseConnectionParam);
        this.jdbcTemplate = new JdbcTemplate(druidDataSource);
    }

    protected void checkUser(BaseConnectionParam baseConnectionParam) {
        if (StringUtils.isBlank(baseConnectionParam.getUser())) {
            setDefaultUsername(baseConnectionParam);
        }
        if (StringUtils.isBlank(baseConnectionParam.getPassword())) {
            setDefaultPassword(baseConnectionParam);
        }
    }

    protected void setDefaultUsername(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setUser(COMMON_USER);
    }

    protected void setDefaultPassword(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setPassword(COMMON_PASSWORD);
    }

    protected void checkValidationQuery(BaseConnectionParam baseConnectionParam) {
        if (StringUtils.isBlank(baseConnectionParam.getValidationQuery())) {
            setDefaultValidationQuery(baseConnectionParam);
        }
    }

    protected void setDefaultValidationQuery(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setValidationQuery(COMMON_VALIDATION_QUERY);
    }

    @Override
    public void checkClient() {
        //Checking data source client
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            this.jdbcTemplate.execute(baseConnectionParam.getValidationQuery());
        } catch (Exception e) {
            throw BaseException.getInstance("JDBC connect failed", e);
        } finally {
            logger.info("Time to execute check jdbc client with sql {} for {} ms ", baseConnectionParam.getValidationQuery(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return druidDataSource.getConnection();
        } catch (SQLException e) {
            logger.error("get druidDataSource Connection fail SQLException: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        logger.info("do close dataSource.");
        this.druidDataSource.close();
        this.druidDataSource = null;
        this.jdbcTemplate = null;
    }

}
