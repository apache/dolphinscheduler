package org.apache.dolphinscheduler.plugin.datasource.api.provider;

import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;

import javax.sql.DataSource;

public interface DataSourceFactory<T extends DataSource> {

    T createDataSource(JdbcConnectionParam connectionParam);

    void destroy(DataSource dataSource);

}
