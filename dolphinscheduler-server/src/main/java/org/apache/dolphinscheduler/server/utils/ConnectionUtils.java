package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author simfo
 * @date 2020/5/27 12:24
 */
public class ConnectionUtils {

    public static Connection getConnection(Properties paramProp,BaseDataSource baseDataSource) throws SQLException {
        return DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                paramProp);
    }

    public static Connection getConnection(BaseDataSource baseDataSource) throws SQLException{
        return DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                baseDataSource.getUser(),
                baseDataSource.getPassword());
    }
}
