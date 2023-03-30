package org.apache.dolphinscheduler.plugin.datasource.kyuubi.provider;

import org.apache.dolphinscheduler.plugin.datasource.api.provider.JDBCDataSourceProvider;
import org.apache.dolphinscheduler.plugin.datasource.kyuubi.param.KyuubiConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.zaxxer.hikari.HikariDataSource;

public class KyuubiJDBCDataSourceProviderTest {

    @Test
    public void testCreateJdbcDataSource() {
        try (
                MockedStatic<JDBCDataSourceProvider> mockedJDBCDataSourceProvider =
                        Mockito.mockStatic(JDBCDataSourceProvider.class)) {
            HikariDataSource dataSource = Mockito.mock(HikariDataSource.class);
            mockedJDBCDataSourceProvider
                    .when(() -> JDBCDataSourceProvider.createJdbcDataSource(Mockito.any(), Mockito.any()))
                    .thenReturn(dataSource);
            Assertions.assertNotNull(
                    JDBCDataSourceProvider.createJdbcDataSource(new KyuubiConnectionParam(), DbType.KYUUBI));
        }
    }

    @Test
    public void testCreateOneSessionJdbcDataSource() {
        try (
                MockedStatic<JDBCDataSourceProvider> mockedJDBCDataSourceProvider =
                        Mockito.mockStatic(JDBCDataSourceProvider.class)) {
            HikariDataSource dataSource = Mockito.mock(HikariDataSource.class);
            mockedJDBCDataSourceProvider
                    .when(() -> JDBCDataSourceProvider.createOneSessionJdbcDataSource(Mockito.any(), Mockito.any()))
                    .thenReturn(dataSource);
            Assertions.assertNotNull(
                    JDBCDataSourceProvider.createOneSessionJdbcDataSource(new KyuubiConnectionParam(), DbType.KYUUBI));
        }
    }
}
