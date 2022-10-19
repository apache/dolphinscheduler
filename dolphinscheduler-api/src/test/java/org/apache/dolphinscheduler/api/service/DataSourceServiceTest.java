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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DATASOURCE_DELETE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DataSourceServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.hive.param.HiveDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.mysql.param.MySQLDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.oracle.param.OracleDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.postgresql.param.PostgreSQLDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbConnectType;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections.CollectionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * data source service test
 */
@ExtendWith(MockitoExtension.class)
public class DataSourceServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger logger = LoggerFactory.getLogger(DataSourceServiceTest.class);
    private static final Logger dataSourceServiceLogger = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    @InjectMocks
    private DataSourceServiceImpl dataSourceService;

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Mock
    private DataSourceUserMapper datasourceUserMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    public void createDataSourceTest() {
        User loginUser = getAdminUser();
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";

        PostgreSQLDataSourceParamDTO postgreSqlDatasourceParam = new PostgreSQLDataSourceParamDTO();
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
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(dataSourceList);
        Result dataSourceExitsResult = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.DATASOURCE_EXIST.getCode(), dataSourceExitsResult.getCode().intValue());

        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(postgreSqlDatasourceParam);
        DbType dataSourceType = postgreSqlDatasourceParam.getType();
        // data source exits
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        Result connectionResult =
                new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());

        Mockito.doReturn(connectionResult).when(dataSourceService).checkConnection(dataSourceType, connectionParam);
        Result connectFailedResult = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailedResult.getCode().intValue());

        // data source exits
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        Mockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
        Result notValidError = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), notValidError.getCode().intValue());

        // success
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        Mockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
        Result success = dataSourceService.createDataSource(loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.SUCCESS.getCode(), success.getCode().intValue());
    }

    public void updateDataSourceTest() {
        User loginUser = getAdminUser();

        int dataSourceId = 12;
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";

        PostgreSQLDataSourceParamDTO postgreSqlDatasourceParam = new PostgreSQLDataSourceParamDTO();
        postgreSqlDatasourceParam.setDatabase(dataSourceName);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setHost("172.16.133.200");
        postgreSqlDatasourceParam.setPort(5432);
        postgreSqlDatasourceParam.setDatabase("dolphinscheduler");
        postgreSqlDatasourceParam.setUserName("postgres");
        postgreSqlDatasourceParam.setPassword("");

        // data source not exits
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Result resourceNotExits =
                dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST.getCode(), resourceNotExits.getCode().intValue());
        // user no operation perm
        DataSource dataSource = new DataSource();
        dataSource.setUserId(0);
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Result userNoOperationPerm =
                dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), userNoOperationPerm.getCode().intValue());

        // data source name exits
        dataSource.setUserId(-1);
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(dataSource);
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(dataSourceList);
        Result dataSourceNameExist =
                dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.DATASOURCE_EXIST.getCode(), dataSourceNameExist.getCode().intValue());

        // data source connect failed
        DbType dataSourceType = postgreSqlDatasourceParam.getType();
        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(postgreSqlDatasourceParam);
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        Result connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        Mockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
        Result connectFailed = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailed.getCode().intValue());

        // success
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        connectionResult =
                new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());
        Mockito.when(dataSourceService.checkConnection(dataSourceType, connectionParam)).thenReturn(connectionResult);
        Result success = dataSourceService.updateDataSource(dataSourceId, loginUser, postgreSqlDatasourceParam);
        Assertions.assertEquals(Status.SUCCESS.getCode(), success.getCode().intValue());

    }

    @Test
    public void queryDataSourceListPagingTest() {
        User loginUser = getAdminUser();
        String searchVal = "";
        int pageNo = 1;
        int pageSize = 10;

        Result result = dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void connectionTest() {
        int dataSourceId = -1;
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Result result = dataSourceService.connectionTest(dataSourceId);
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST.getCode(), result.getCode().intValue());
    }

    @Test
    public void deleteTest() {
        User loginUser = getAdminUser();
        int dataSourceId = 1;
        Result result = new Result();
        // resource not exist
        dataSourceService.putMsg(result, Status.RESOURCE_NOT_EXIST);
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Assertions.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

        // user no operation perm
        dataSourceService.putMsg(result, Status.USER_NO_OPERATION_PERM);
        DataSource dataSource = new DataSource();
        dataSource.setUserId(0);
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Assertions.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

        // success
        dataSourceService.putMsg(result, Status.SUCCESS);
        dataSource.setUserId(-1);
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(1);
        dataSource.setId(22);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.DATASOURCE, null,
                loginUser.getId(), DATASOURCE_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.DATASOURCE,
                new Object[]{dataSource.getId()}, 0, baseServiceLogger)).thenReturn(true);
        Mockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Assertions.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

    }

    @Test
    public void unauthDatasourceTest() {
        User loginUser = getAdminUser();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        int userId = 3;
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.DATASOURCE, null,
                loginUser.getId(), null, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.DATASOURCE, null, 0,
                baseServiceLogger)).thenReturn(true);
        // test admin user
        Mockito.when(dataSourceMapper.queryAuthedDatasource(userId)).thenReturn(getSingleDataSourceList());
        Mockito.when(dataSourceMapper.queryDatasourceExceptUserId(userId)).thenReturn(getDataSourceList());
        Map<String, Object> result = dataSourceService.unauthDatasource(loginUser, userId);
        logger.info(result.toString());
        List<DataSource> dataSources = (List<DataSource>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(dataSources));

        // test non-admin user
        loginUser.setId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(dataSourceMapper.selectByMap(Collections.singletonMap("user_id", loginUser.getId())))
                .thenReturn(getDataSourceList());
        result = dataSourceService.unauthDatasource(loginUser, userId);
        logger.info(result.toString());
        dataSources = (List<DataSource>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(dataSources));
    }

    @Test
    public void authedDatasourceTest() {
        User loginUser = getAdminUser();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        int userId = 3;

        // test admin user
        Mockito.when(dataSourceMapper.queryAuthedDatasource(userId)).thenReturn(getSingleDataSourceList());
        Map<String, Object> result = dataSourceService.authedDatasource(loginUser, userId);
        logger.info(result.toString());
        List<DataSource> dataSources = (List<DataSource>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(dataSources));

        // test non-admin user
        loginUser.setId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> success = dataSourceService.authedDatasource(loginUser, userId);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, success.get(Constants.STATUS));
    }

    @Test
    public void queryDataSourceListTest() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        Set<Integer> dataSourceIds = new HashSet<>();
        dataSourceIds.add(1);
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.DATASOURCE,
                loginUser.getId(), dataSourceServiceLogger)).thenReturn(dataSourceIds);

        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        Mockito.when(dataSourceMapper.selectBatchIds(dataSourceIds)).thenReturn(Collections.singletonList(dataSource));
        Map<String, Object> map =
                dataSourceService.queryDataSourceList(loginUser, DbType.MYSQL.ordinal(), Constants.TEST_FLAG_NO);
        Assertions.assertEquals(Status.SUCCESS, map.get(Constants.STATUS));
    }

    @Test
    public void verifyDataSourceNameTest() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        String dataSourceName = "dataSource1";
        Mockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(getDataSourceList());
        Result result = dataSourceService.verifyDataSourceName(dataSourceName);
        Assertions.assertEquals(Status.DATASOURCE_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void queryDataSourceTest() {
        Mockito.when(dataSourceMapper.selectById(Mockito.anyInt())).thenReturn(null);
        Map<String, Object> result = dataSourceService.queryDataSource(Mockito.anyInt());
        Assertions.assertEquals(((Status) result.get(Constants.STATUS)).getCode(), Status.RESOURCE_NOT_EXIST.getCode());

        Mockito.when(dataSourceMapper.selectById(Mockito.anyInt())).thenReturn(getOracleDataSource());
        result = dataSourceService.queryDataSource(Mockito.anyInt());
        Assertions.assertEquals(((Status) result.get(Constants.STATUS)).getCode(), Status.SUCCESS.getCode());
    }

    private List<DataSource> getDataSourceList() {

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(getOracleDataSource(1));
        dataSources.add(getOracleDataSource(2));
        dataSources.add(getOracleDataSource(3));
        return dataSources;
    }

    private List<DataSource> getSingleDataSourceList() {
        return Collections.singletonList(getOracleDataSource(3));
    }

    private DataSource getOracleDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setName("test");
        dataSource.setNote("Note");
        dataSource.setType(DbType.ORACLE);
        dataSource.setConnectionParams(
                "{\"connectType\":\"ORACLE_SID\",\"address\":\"jdbc:oracle:thin:@192.168.xx.xx:49161\",\"database\":\"XE\","
                        + "\"jdbcUrl\":\"jdbc:oracle:thin:@192.168.xx.xx:49161/XE\",\"user\":\"system\",\"password\":\"oracle\"}");

        return dataSource;
    }

    private DataSource getOracleDataSource(int dataSourceId) {
        DataSource dataSource = new DataSource();
        dataSource.setId(dataSourceId);
        dataSource.setName("test");
        dataSource.setNote("Note");
        dataSource.setType(DbType.ORACLE);
        dataSource.setConnectionParams(
                "{\"connectType\":\"ORACLE_SID\",\"address\":\"jdbc:oracle:thin:@192.168.xx.xx:49161\",\"database\":\"XE\","
                        + "\"jdbcUrl\":\"jdbc:oracle:thin:@192.168.xx.xx:49161/XE\",\"user\":\"system\",\"password\":\"oracle\"}");

        return dataSource;
    }

    @Test
    public void buildParameter() {
        OracleDataSourceParamDTO oracleDatasourceParamDTO = new OracleDataSourceParamDTO();
        oracleDatasourceParamDTO.setHost("192.168.9.1");
        oracleDatasourceParamDTO.setPort(1521);
        oracleDatasourceParamDTO.setDatabase("im");
        oracleDatasourceParamDTO.setUserName("test");
        oracleDatasourceParamDTO.setPassword("test");
        oracleDatasourceParamDTO.setConnectType(DbConnectType.ORACLE_SERVICE_NAME);

        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(oracleDatasourceParamDTO);
        String expected =
                "{\"user\":\"test\",\"password\":\"test\",\"address\":\"jdbc:oracle:thin:@//192.168.9.1:1521\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:oracle:thin:@//192.168.9.1:1521/im\","
                        + "\"driverClassName\":\"oracle.jdbc.OracleDriver\",\"validationQuery\":\"select 1 from dual\",\"connectType\":\"ORACLE_SERVICE_NAME\"}";
        Assertions.assertEquals(expected, JSONUtils.toJsonString(connectionParam));

        try (MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticCommonUtils.when(CommonUtils::getKerberosStartupState).thenReturn(true);
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
            connectionParam = DataSourceUtils.buildConnectionParams(hiveDataSourceParamDTO);

            expected =
                    "{\"user\":\"test\",\"password\":\"test\",\"address\":\"jdbc:hive2://192.168.9.1:10000\",\"database\":\"im\","
                            + "\"jdbcUrl\":\"jdbc:hive2://192.168.9.1:10000/im\",\"driverClassName\":\"org.apache.hive.jdbc.HiveDriver\",\"validationQuery\":\"select 1\","
                            + "\"principal\":\"hive/hdfs-mycluster@ESZ.COM\",\"javaSecurityKrb5Conf\":\"/opt/krb5.conf\",\"loginUserKeytabUsername\":\"test2/hdfs-mycluster@ESZ.COM\","
                            + "\"loginUserKeytabPath\":\"/opt/hdfs.headless.keytab\"}";
            Assertions.assertEquals(expected, JSONUtils.toJsonString(connectionParam));
        }
    }

    @Test
    public void buildParameterWithDecodePassword() {
        try (MockedStatic<PropertyUtils> mockedStaticPropertyUtils = Mockito.mockStatic(PropertyUtils.class)) {
            mockedStaticPropertyUtils
                    .when(() -> PropertyUtils.getBoolean(Constants.DATASOURCE_ENCRYPTION_ENABLE, false))
                    .thenReturn(true);
            Map<String, String> other = new HashMap<>();
            other.put("autoDeserialize", "yes");
            other.put("allowUrlInLocalInfile", "true");
            MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();
            mysqlDatasourceParamDTO.setHost("192.168.9.1");
            mysqlDatasourceParamDTO.setPort(1521);
            mysqlDatasourceParamDTO.setDatabase("im");
            mysqlDatasourceParamDTO.setUserName("test");
            mysqlDatasourceParamDTO.setPassword("123456");
            mysqlDatasourceParamDTO.setOther(other);
            ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(mysqlDatasourceParamDTO);
            String expected =
                    "{\"user\":\"test\",\"password\":\"bnVsbE1USXpORFUy\",\"address\":\"jdbc:mysql://192.168.9.1:1521\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/"
                            + "im\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\",\"props\":{\"autoDeserialize\":\"yes\",\"allowUrlInLocalInfile\":\"true\"}}";
            Assertions.assertEquals(expected, JSONUtils.toJsonString(connectionParam));
        }

        MySQLDataSourceParamDTO mysqlDatasourceParamDTO = new MySQLDataSourceParamDTO();
        mysqlDatasourceParamDTO.setHost("192.168.9.1");
        mysqlDatasourceParamDTO.setPort(1521);
        mysqlDatasourceParamDTO.setDatabase("im");
        mysqlDatasourceParamDTO.setUserName("test");
        mysqlDatasourceParamDTO.setPassword("123456");
        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(mysqlDatasourceParamDTO);
        String expected =
                "{\"user\":\"test\",\"password\":\"123456\",\"address\":\"jdbc:mysql://192.168.9.1:1521\",\"database\":\"im\","
                        + "\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/im\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\"}";
        Assertions.assertEquals(expected, JSONUtils.toJsonString(connectionParam));
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

        PostgreSQLDataSourceParamDTO postgreSqlDatasourceParam = new PostgreSQLDataSourceParamDTO();
        postgreSqlDatasourceParam.setDatabase(dataSourceName);
        postgreSqlDatasourceParam.setNote(dataSourceDesc);
        postgreSqlDatasourceParam.setHost("172.16.133.200");
        postgreSqlDatasourceParam.setPort(5432);
        postgreSqlDatasourceParam.setDatabase("dolphinscheduler");
        postgreSqlDatasourceParam.setUserName("postgres");
        postgreSqlDatasourceParam.setPassword("");
        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(postgreSqlDatasourceParam);

        try (
                MockedStatic<DataSourceClientProvider> mockedStaticDataSourceClientProvider =
                        Mockito.mockStatic(DataSourceClientProvider.class)) {
            DataSourceClientProvider clientProvider = Mockito.mock(DataSourceClientProvider.class);
            Mockito.when(DataSourceClientProvider.getInstance()).thenReturn(clientProvider);
            mockedStaticDataSourceClientProvider.when(DataSourceClientProvider::getInstance).thenReturn(clientProvider);

            Result result = dataSourceService.checkConnection(dataSourceType, connectionParam);
            Assertions.assertEquals(Status.CONNECTION_TEST_FAILURE.getCode(), result.getCode().intValue());

            Connection connection = Mockito.mock(Connection.class);
            Mockito.when(clientProvider.getConnection(Mockito.any(), Mockito.any())).thenReturn(connection);
            result = dataSourceService.checkConnection(dataSourceType, connectionParam);
            Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        }
    }

}
