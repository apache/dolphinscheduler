package org.apache.dolphinscheduler.dao.datasource;

import java.sql.Connection;

public interface DataSourceClient {

    void checkClient();

    void close();

    Connection getConnection();
}
