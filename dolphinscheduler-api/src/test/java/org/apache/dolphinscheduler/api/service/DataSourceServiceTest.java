/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.DataSourceServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourcePluginManager;
import org.apache.dolphinscheduler.plugin.datasource.api.provider.JdbcDataSourceProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.DataSourceParam;
import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * data source service test
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"sun.security.*", "javax.net.*"})
@PrepareForTest({JdbcDataSourceProvider.class, CommonUtils.class, DataSourcePluginManager.class, PasswordUtils.class})
public class DataSourceServiceTest {

    @InjectMocks
    private DataSourceServiceImpl dataSourceService;

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Mock
    private DataSourceUserMapper datasourceUserMapper;

    public void createDataSourceTest() {
        User loginUser = getAdminUser();
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";

        DataSourceParam postgreSqlDatasourceParam = new DataSourceParam();
        postgreSqlDatasourceParam.setDbType(DbType.POSTGRESQL);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setName(dataSourceName);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:postgresql://172.16.133.200:5432/dolphinscheduler");
        props.put("user", "postgres");
        props.put("password", "");
        postgreSqlDatasourceParam.setProps(props);

        // data source exits
        List<DataSource> dataSourceList = new ArrayList<>();
        DataSource dataSource = new DataSource();
        dataSource.setName(dataSourceName);
        dataSourceList.add(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(dataSourceList);
        Result dataSourceExitsResult = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.DATASOURCE_EXIST.getCode(), dataSourceExitsResult.getCode().intValue());

        JdbcConnectionParam jdbcConnectionParam = JdbcDataSourceProvider.buildConnectionParams(postgreSqlDatasourceParam);
        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        Result connectionResult = new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());
        //PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(connectionResult);
        PowerMockito.doReturn(connectionResult).when(dataSourceService).checkConnection(jdbcConnectionParam);
        Result connectFailedResult = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailedResult.getCode().intValue());

        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(jdbcConnectionParam)).thenReturn(connectionResult);
        Result notValidError = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), notValidError.getCode().intValue());

        // success
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(jdbcConnectionParam)).thenReturn(connectionResult);
        Result success = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.SUCCESS.getCode(), success.getCode().intValue());
    }

    public void updateDataSourceTest() {
        User loginUser = getAdminUser();

        int dataSourceId = 12;
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";

        DataSourceParam postgreSqlDatasourceParam = new DataSourceParam();
        postgreSqlDatasourceParam.setDbType(DbType.POSTGRESQL);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setName(dataSourceName);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:postgresql://172.16.133.200:5432/dolphinscheduler");
        props.put("user", "postgres");
        props.put("password", "");
        postgreSqlDatasourceParam.setProps(props);

        // data source not exits
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Result resourceNotExits = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getCode(), resourceNotExits.getCode().intValue());
        // user no operation perm
        DataSource dataSource = new DataSource();
        dataSource.setUserId(0);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Result userNoOperationPerm = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), userNoOperationPerm.getCode().intValue());

        // data source name exits
        dataSource.setUserId(-1);
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(dataSource);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(dataSourceList);
        Result dataSourceNameExist = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.DATASOURCE_EXIST.getCode(), dataSourceNameExist.getCode().intValue());

        // data source connect failed
        JdbcConnectionParam connectionParam = JdbcDataSourceProvider.buildConnectionParams(postgreSqlDatasourceParam);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        Result connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(connectionParam)).thenReturn(connectionResult);
        Result connectFailed = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailed.getCode().intValue());

        //success
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        connectionResult = new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(connectionParam)).thenReturn(connectionResult);
        Result success = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.SUCCESS.getCode(), success.getCode().intValue());

    }

    @Test
    public void queryDataSourceListPagingTest() {
        User loginUser = getAdminUser();
        String searchVal = "";
        int pageNo = 1;
        int pageSize = 10;
        Result result = dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
        Assert.assertEquals(Status.SUCCESS.getCode(),(int)result.getCode());
    }

    @Test
    public void connectionTest() {
        int dataSourceId = -1;
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Result result = dataSourceService.connectionTest(dataSourceId);
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getCode(), result.getCode().intValue());
    }

    @Test
    public void deleteTest() {
        User loginUser = getAdminUser();
        int dataSourceId = 1;
        Result result = new Result();

        //resource not exist
        dataSourceService.putMsg(result, Status.RESOURCE_NOT_EXIST);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Assert.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

        // user no operation perm
        dataSourceService.putMsg(result, Status.USER_NO_OPERATION_PERM);
        DataSource dataSource = new DataSource();
        dataSource.setUserId(0);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Assert.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

        // success
        dataSourceService.putMsg(result, Status.SUCCESS);
        dataSource.setUserId(-1);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Assert.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

    }

    @Test
    public void unauthDatasourceTest() {
        User loginUser = getAdminUser();
        int userId = -1;

        //user no operation perm
        Map<String, Object> noOperationPerm = dataSourceService.unauthDatasource(loginUser, userId);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, noOperationPerm.get(Constants.STATUS));

        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> success = dataSourceService.unauthDatasource(loginUser, userId);
        Assert.assertEquals(Status.SUCCESS, success.get(Constants.STATUS));
    }

    @Test
    public void authedDatasourceTest() {
        User loginUser = getAdminUser();
        int userId = -1;

        //user no operation perm
        Map<String, Object> noOperationPerm = dataSourceService.authedDatasource(loginUser, userId);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, noOperationPerm.get(Constants.STATUS));

        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> success = dataSourceService.authedDatasource(loginUser, userId);
        Assert.assertEquals(Status.SUCCESS, success.get(Constants.STATUS));
    }

    @Test
    public void queryDataSourceListTest() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> map = dataSourceService.queryDataSourceList(loginUser, DbType.MYSQL.ordinal());
        Assert.assertEquals(Status.SUCCESS, map.get(Constants.STATUS));
    }

    @Test
    public void verifyDataSourceNameTest() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        String dataSourceName = "dataSource1";
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(getDataSourceList());
        Result result = dataSourceService.verifyDataSourceName(dataSourceName);
        Assert.assertEquals(Status.DATASOURCE_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void queryDataSourceTest() {
        PowerMockito.when(dataSourceMapper.selectById(Mockito.anyInt())).thenReturn(null);
        Map<String, Object> result = dataSourceService.queryDataSource(Mockito.anyInt());
        Assert.assertEquals(((Status) result.get(Constants.STATUS)).getCode(), Status.RESOURCE_NOT_EXIST.getCode());

        PowerMockito.when(dataSourceMapper.selectById(Mockito.anyInt())).thenReturn(getOracleDataSource());
        result = dataSourceService.queryDataSource(Mockito.anyInt());
        Assert.assertEquals(((Status) result.get(Constants.STATUS)).getCode(), Status.SUCCESS.getCode());
    }

    private List<DataSource> getDataSourceList() {

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(getOracleDataSource());
        return dataSources;
    }

    private DataSource getOracleDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setName("test");
        dataSource.setNote("Note");
        dataSource.setType(DbType.ORACLE);
        dataSource.setConnectionParams("{\"connectType\":\"ORACLE_SID\",\"address\":\"jdbc:oracle:thin:@192.168.xx.xx:49161\",\"database\":\"XE\","
                + "\"jdbcUrl\":\"jdbc:oracle:thin:@192.168.xx.xx:49161/XE\",\"user\":\"system\",\"password\":\"oracle\"}");

        return dataSource;
    }

    @Test
    public void buildParameter() {
        DataSourceParam datasourceParam = new DataSourceParam();
        datasourceParam.setDbType(DbType.ORACLE);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:postgresql://172.16.133.200:5432/dolphinscheduler");
        props.put("user", "test");
        props.put("password", "test");
        props.put("jdbcUrl", "jdbc:oracle:thin:@//192.168.9.1:1521/im");
        datasourceParam.setProps(props);

        JdbcConnectionParam connectionParam = JdbcDataSourceProvider.buildConnectionParams(datasourceParam);
        String expected = "{\"dbType\":\"ORACLE\",\"jdbcUrl\":\"jdbc:oracle:thin:@//192.168.9.1:1521/im\",\"user\":\"test\",\"password\":\"test\",\"driverClassName\":\"oracle.jdbc.OracleDriver\"}";
        Assert.assertEquals(expected, JSONUtils.toJsonString(connectionParam));

        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(CommonUtils.getKerberosStartupState()).thenReturn(true);
        PowerMockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        DataSourceParam dataSourceParam = new DataSourceParam();
        dataSourceParam.setDbType(DbType.HIVE);
        props.put("jdbcUrl", "jdbc:hive2://192.168.9.1:10000/im");
        props.put("user", "test");
        props.put("password", "test");

        Map<String, Object> otherProps = new HashMap<>();
        otherProps.put("kerberosPrincipal", "hive/hdfs-mycluster@ESZ.COM");
        otherProps.put("kerberosKeytab", "/opt/hdfs.headless.keytab");
        otherProps.put("kerberosKrb5Conf", "/opt/krb5.conf");
        props.put("props", otherProps);
        dataSourceParam.setProps(props);

        connectionParam = JdbcDataSourceProvider.buildConnectionParams(dataSourceParam);

        expected = "{\"dbType\":\"HIVE\",\"jdbcUrl\":\"jdbc:hive2://192.168.9.1:10000/im\",\"user\":\"test\",\"password\":\"test\",\"driverClassName\":\"org.apache.hive.jdbc.HiveDriver\",\"props\":{\"kerberosKrb5Conf\":\"/opt/krb5.conf\",\"kerberosPrincipal\":\"hive/hdfs-mycluster@ESZ.COM\",\"kerberosKeytab\":\"/opt/hdfs.headless.keytab\"}}";
        Assert.assertEquals(expected, JSONUtils.toJsonString(connectionParam));

    }

    @Test
    public void buildParameterWithDecodePassword() {
        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "true");
        Map<String, Object> other = new HashMap<>();
        other.put("autoDeserialize", "yes");
        other.put("allowUrlInLocalInfile", "true");

        DataSourceParam dataSourceParam = new DataSourceParam();
        dataSourceParam.setDbType(DbType.MYSQL);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:mysql://172.16.133.200:3306/dolphinscheduler");
        props.put("user", "test");
        props.put("password", "123456");
        props.put("props", other);
        dataSourceParam.setProps(props);
        JdbcConnectionParam connectionParam = JdbcDataSourceProvider.buildConnectionParams(dataSourceParam);
        String expected = "{\"dbType\":\"MYSQL\",\"jdbcUrl\":\"jdbc:mysql://172.16.133.200:3306/dolphinscheduler\",\"user\":\"test\",\"password\":\"IUAjJCVeJipNVEl6TkRVMg==\",\"driverClassName\":\"com.mysql.jdbc.Driver\",\"props\":{\"autoDeserialize\":\"yes\",\"allowUrlInLocalInfile\":\"true\"}}";
        Assert.assertEquals(expected, JSONUtils.toJsonString(connectionParam));

        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "false");
        dataSourceParam = new DataSourceParam();
        dataSourceParam.setDbType(DbType.MYSQL);
        props.put("jdbcUrl", "jdbc:mysql://172.16.133.200:3306/dolphinscheduler");
        props.put("user", "test");
        props.put("password", "123456");
        dataSourceParam.setProps(props);
        connectionParam = JdbcDataSourceProvider.buildConnectionParams(dataSourceParam);
        expected = "{\"dbType\":\"MYSQL\",\"jdbcUrl\":\"jdbc:mysql://172.16.133.200:3306/dolphinscheduler\",\"user\":\"test\",\"password\":\"123456\",\"driverClassName\":\"com.mysql.jdbc.Driver\",\"props\":{\"autoDeserialize\":\"yes\",\"allowUrlInLocalInfile\":\"true\"}}";
        Assert.assertEquals(expected, JSONUtils.toJsonString(connectionParam));
    }

    /**
     * get Mock Admin User
     *
     * @return admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
    }

    /**
     * test check connection
     */
    @Test
    public void testCheckConnection() throws Exception {
        DbType dataSourceType = DbType.POSTGRESQL;
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";

        DataSourceParam postgreSqlDatasourceParam = new DataSourceParam();
        postgreSqlDatasourceParam.setDbType(DbType.POSTGRESQL);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setName(dataSourceName);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:postgresql://172.16.133.200:5432/dolphinscheduler");
        props.put("user", "postgres");
        props.put("password", "");
        postgreSqlDatasourceParam.setProps(props);
        JdbcConnectionParam connectionParam = JdbcDataSourceProvider.buildConnectionParams(postgreSqlDatasourceParam);

        PowerMockito.mockStatic(JdbcDataSourceProvider.class);
        PowerMockito.mockStatic(DataSourcePluginManager.class);
        DataSourcePluginManager dataSourcePluginManager = PowerMockito.mock(DataSourcePluginManager.class);

        Result result = dataSourceService.checkConnection(connectionParam);
        Assert.assertEquals(Status.CONNECTION_TEST_FAILURE.getCode(), result.getCode().intValue());

        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.when(dataSourcePluginManager.getConnection(Mockito.any())).thenReturn(connection);
        result = dataSourceService.checkConnection(connectionParam);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

    }

}
