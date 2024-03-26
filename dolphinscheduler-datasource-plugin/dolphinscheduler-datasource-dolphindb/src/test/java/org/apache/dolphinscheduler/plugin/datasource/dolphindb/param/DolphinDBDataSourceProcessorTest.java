package org.apache.dolphinscheduler.plugin.datasource.dolphindb.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({Class.class, DriverManager.class, DataSourceUtils.class, CommonUtils.class, DataSourceClientProvider.class, PropertyUtils.class})
public class DolphinDBDataSourceProcessorTest {

    private final DolphinDBDataSourceProcessor dolphinDBDataSourceProcessor = new DolphinDBDataSourceProcessor();

    Connection conn;
    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        DolphinDBDataSourceParamDTO dolphinDBDataSourceParamDTO = new DolphinDBDataSourceParamDTO();
        dolphinDBDataSourceParamDTO.setUserName("admin");
        dolphinDBDataSourceParamDTO.setPassword("123456");
        dolphinDBDataSourceParamDTO.setHost("xxx.xxx.xxx.xxx");
        dolphinDBDataSourceParamDTO.setPort(8848);
        dolphinDBDataSourceParamDTO.setDatabase("test");
        dolphinDBDataSourceParamDTO.setOther(props);

        // PowerMockito.mockStatic(PasswordUtils.class);
        // PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");

        DolphinDBConnectionParam dolphinDBConnectionParam = (DolphinDBConnectionParam) dolphinDBDataSourceProcessor
                .createConnectionParams(dolphinDBDataSourceParamDTO);
        Assert.assertEquals("jdbc:dolphindb://xxx.xxx.xxx.xxx:8848", dolphinDBConnectionParam.getAddress());
        Assert.assertEquals("jdbc:dolphindb://xxx.xxx.xxx.xxx:8848/test", dolphinDBConnectionParam.getJdbcUrl());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionJson =
                "{\"user\":\"admin\",\"password\":\"123456\",\"address\":\"jdbc:dolphindb://xxx.xxx.xxx.xxx:8848\""
                        + ",\"database\":\"test\",\"jdbcUrl\":\"jdbc:dolphindb://xxx.xxx.xxx.xxx:8848/test\"}";
        DolphinDBConnectionParam connectionParams = (DolphinDBConnectionParam) dolphinDBDataSourceProcessor
                .createConnectionParams(connectionJson);
        Assert.assertNotNull(connectionJson);
        System.out.println(connectionParams);
        Assert.assertEquals("admin", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.COM_DOLPHINDB_JDBC_DRIVER,
                dolphinDBDataSourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        DolphinDBConnectionParam dolphinDBConnectionParam = new DolphinDBConnectionParam();
        dolphinDBConnectionParam.setJdbcUrl("jdbc:dolphindb://xxx.xxx.xxx.xxx:8848/test");
        Assert.assertEquals("jdbc:dolphindb://xxx.xxx.xxx.xxx:8848/test",
                dolphinDBDataSourceProcessor.getJdbcUrl(dolphinDBConnectionParam));
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.DOLPHINDB, dolphinDBDataSourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.DOLPHINDB_VALIDATION_QUERY,
                dolphinDBDataSourceProcessor.getValidationQuery());
    }

    @Test
    public void testGetDatasourceUniqueId() {
        DolphinDBConnectionParam dolphinDBConnectionParam = new DolphinDBConnectionParam();
        dolphinDBConnectionParam.setJdbcUrl("jdbc:dolphindb://xxx.xxx.xxx.xxx:8848/test");
        dolphinDBConnectionParam.setUser("admin");
        dolphinDBConnectionParam.setPassword("123456");
        // PowerMockito.mockStatic(PasswordUtils.class);
        // PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
        Assert.assertEquals("dolphindb@admin@123456@jdbc:dolphindb://xxx.xxx.xxx.xxx:8848/test",
                dolphinDBDataSourceProcessor.getDatasourceUniqueId(dolphinDBConnectionParam, DbType.DOLPHINDB));
    }

    @Test
    public void testConnent() throws SQLException, IOException, ClassNotFoundException {
        DolphinDBConnectionParam connectionParams = new DolphinDBConnectionParam();
        connectionParams.setJdbcUrl("jdbc:dolphindb://xxx.xxx.xxx.xxx:8848");
        // connectionParams.setOther("user=admin&password=123456");
        System.out.println(dolphinDBDataSourceProcessor.getJdbcUrl(connectionParams));
        Class.forName(DataSourceConstants.COM_DOLPHINDB_JDBC_DRIVER);
        Connection connection = DriverManager.getConnection(dolphinDBDataSourceProcessor.getJdbcUrl(connectionParams));
        PreparedStatement statement = connection.prepareStatement("select 1");
        statement.execute();
    }
}
