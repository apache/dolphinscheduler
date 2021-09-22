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
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.datasource.hive.HiveDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.mysql.MysqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.oracle.OracleDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.postgresql.PostgreSqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;

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
@PrepareForTest({DatasourceUtil.class, CommonUtils.class})
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

        PostgreSqlDatasourceParamDTO postgreSqlDatasourceParam = new PostgreSqlDatasourceParamDTO();
        postgreSqlDatasourceParam.setDatabase(dataSourceName);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setHost("172.16.133.200");
        postgreSqlDatasourceParam.setPort(5432);
        postgreSqlDatasourceParam.setDatabase("dolphinscheduler");
        postgreSqlDatasourceParam.setUserName("postgres");
        postgreSqlDatasourceParam.setPassword("");

        // data source exits
        List<DataSource> dataSourceList = new ArrayList<>();
        DataSource dataSource = new DataSource();
        dataSource.setName(dataSourceName);
        dataSourceList.add(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(dataSourceList);
        Result dataSourceExitsResult = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.DATASOURCE_EXIST.getCode(), dataSourceExitsResult.getCode().intValue());

        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(postgreSqlDatasourceParam);
        DbType dataSourceType = postgreSqlDatasourceParam.getType();
        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        Result connectionResult = new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());
        //PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(connectionResult);
        PowerMockito.doReturn(connectionResult).when(dataSourceService).checkConnection(dataSourceType, connectionParam);
        Result connectFailedResult = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailedResult.getCode().intValue());

        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
        Result notValidError = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), notValidError.getCode().intValue());

        // success
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
        Result success = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.SUCCESS.getCode(), success.getCode().intValue());
    }

    public void updateDataSourceTest() {
        User loginUser = getAdminUser();

        int dataSourceId = 12;
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";

        PostgreSqlDatasourceParamDTO postgreSqlDatasourceParam = new PostgreSqlDatasourceParamDTO();
        postgreSqlDatasourceParam.setDatabase(dataSourceName);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setHost("172.16.133.200");
        postgreSqlDatasourceParam.setPort(5432);
        postgreSqlDatasourceParam.setDatabase("dolphinscheduler");
        postgreSqlDatasourceParam.setUserName("postgres");
        postgreSqlDatasourceParam.setPassword("");

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
        DbType dataSourceType = postgreSqlDatasourceParam.getType();
        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(postgreSqlDatasourceParam);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        Result connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
        Result connectFailed = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailed.getCode().intValue());

        //success
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        connectionResult = new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
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
        OracleDatasourceParamDTO oracleDatasourceParamDTO = new OracleDatasourceParamDTO();
        oracleDatasourceParamDTO.setHost("192.168.9.1");
        oracleDatasourceParamDTO.setPort(1521);
        oracleDatasourceParamDTO.setDatabase("im");
        oracleDatasourceParamDTO.setUserName("test");
        oracleDatasourceParamDTO.setPassword("test");
        oracleDatasourceParamDTO.setConnectType(DbConnectType.ORACLE_SERVICE_NAME);

        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(oracleDatasourceParamDTO);
        String expected = "{\"user\":\"test\",\"password\":\"test\",\"address\":\"jdbc:oracle:thin:@//192.168.9.1:1521\","
                + "\"database\":\"im\",\"jdbcUrl\":\"jdbc:oracle:thin:@//192.168.9.1:1521/im\",\"connectType\":\"ORACLE_SERVICE_NAME\"}";
        Assert.assertEquals(expected, JSONUtils.toJsonString(connectionParam));

        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.getKerberosStartupState()).thenReturn(true);
        PowerMockito.when(CommonUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        HiveDataSourceParamDTO hiveDataSourceParamDTO = new HiveDataSourceParamDTO();
        hiveDataSourceParamDTO.setHost("192.168.9.1");
        hiveDataSourceParamDTO.setPort(10000);
        hiveDataSourceParamDTO.setDatabase("im");
        hiveDataSourceParamDTO.setPrincipal("hive/hdfs-mycluster@ESZ.COM");
        hiveDataSourceParamDTO.setUserName("test");
        hiveDataSourceParamDTO.setPassword("test");
        hiveDataSourceParamDTO.setJavaSecurityKrb5Conf("/opt/krb5.conf");
        hiveDataSourceParamDTO.setLoginUserKeytabPath("/opt/hdfs.headless.keytab");
        hiveDataSourceParamDTO.setLoginUserKeytabUsername("test2/hdfs-mycluster@ESZ.COM");
        connectionParam = DatasourceUtil.buildConnectionParams(hiveDataSourceParamDTO);
        expected = "{\"user\":\"test\",\"password\":\"test\",\"address\":\"jdbc:hive2://192.168.9.1:10000\","
                + "\"database\":\"im\",\"jdbcUrl\":\"jdbc:hive2://192.168.9.1:10000/im;principal=hive/hdfs-mycluster@ESZ.COM\",\"principal\":\"hive/hdfs-mycluster@ESZ.COM\","
                + "\"javaSecurityKrb5Conf\":\"/opt/krb5.conf\",\"loginUserKeytabUsername\":\"test2/hdfs-mycluster@ESZ.COM\",\"loginUserKeytabPath\":\"/opt/hdfs.headless.keytab\"}";
        Assert.assertEquals(expected, JSONUtils.toJsonString(connectionParam));

    }

    @Test
    public void buildParameterWithDecodePassword() {
        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "true");
        Map<String, String> other = new HashMap<>();
        other.put("autoDeserialize", "yes");
        other.put("allowUrlInLocalInfile", "true");
        MysqlDatasourceParamDTO mysqlDatasourceParamDTO = new MysqlDatasourceParamDTO();
        mysqlDatasourceParamDTO.setHost("192.168.9.1");
        mysqlDatasourceParamDTO.setPort(1521);
        mysqlDatasourceParamDTO.setDatabase("im");
        mysqlDatasourceParamDTO.setUserName("test");
        mysqlDatasourceParamDTO.setPassword("123456");
        mysqlDatasourceParamDTO.setOther(other);
        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(mysqlDatasourceParamDTO);
        String expected = "{\"user\":\"test\",\"password\":\"IUAjJCVeJipNVEl6TkRVMg==\",\"address\":\"jdbc:mysql://192.168.9.1:1521\","
                + "\"database\":\"im\",\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/im\"}";
        Assert.assertEquals(expected, JSONUtils.toJsonString(connectionParam));

        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "false");
        mysqlDatasourceParamDTO = new MysqlDatasourceParamDTO();
        mysqlDatasourceParamDTO.setHost("192.168.9.1");
        mysqlDatasourceParamDTO.setPort(1521);
        mysqlDatasourceParamDTO.setDatabase("im");
        mysqlDatasourceParamDTO.setUserName("test");
        mysqlDatasourceParamDTO.setPassword("123456");
        connectionParam = DatasourceUtil.buildConnectionParams(mysqlDatasourceParamDTO);
        expected = "{\"user\":\"test\",\"password\":\"123456\",\"address\":\"jdbc:mysql://192.168.9.1:1521\","
                + "\"database\":\"im\",\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/im\"}";
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

        PostgreSqlDatasourceParamDTO postgreSqlDatasourceParam = new PostgreSqlDatasourceParamDTO();
        postgreSqlDatasourceParam.setDatabase(dataSourceName);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setHost("172.16.133.200");
        postgreSqlDatasourceParam.setPort(5432);
        postgreSqlDatasourceParam.setDatabase("dolphinscheduler");
        postgreSqlDatasourceParam.setUserName("postgres");
        postgreSqlDatasourceParam.setPassword("");
        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(postgreSqlDatasourceParam);

        PowerMockito.mockStatic(DatasourceUtil.class);

        Result result = dataSourceService.checkConnection(dataSourceType, connectionParam);
        Assert.assertEquals(Status.CONNECTION_TEST_FAILURE.getCode(), result.getCode().intValue());

        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.when(DatasourceUtil.getConnection(Mockito.any(), Mockito.any())).thenReturn(connection);
        result = dataSourceService.checkConnection(dataSourceType, connectionParam);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

    }

}
