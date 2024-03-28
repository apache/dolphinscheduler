package org.apache.dolphinscheduler.plugin.datasource.flink;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.datasource.api.client.BasePooledDataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class FlinkPooledDataSourceClient extends BasePooledDataSourceClient {

    public FlinkPooledDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        super.close();
        log.info("Closed Flink datasource client.");
    }
}