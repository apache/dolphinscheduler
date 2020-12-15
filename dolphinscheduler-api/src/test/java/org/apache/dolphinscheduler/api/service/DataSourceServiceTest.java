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
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.datasource.MySQLDataSource;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"sun.security.*", "javax.net.*"})
public class DataSourceServiceTest {

    @InjectMocks
    private DataSourceService dataSourceService;
    @Mock
    private DataSourceMapper dataSourceMapper;
    @Mock
    private DataSourceUserMapper datasourceUserMapper;

    public void createDataSourceTest() {
        User loginUser = getAdminUser();

        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";
        DbType dataSourceType = DbType.POSTGRESQL;
        String parameter = dataSourceService.buildParameter(dataSourceType, "172.16.133.200", "5432", "dolphinscheduler", null, "postgres", "", null, null);

        // data source exits
        List<DataSource> dataSourceList = new ArrayList<>();
        DataSource dataSource = new DataSource();
        dataSource.setName(dataSourceName);
        dataSourceList.add(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(dataSourceList);
        Map<String, Object> dataSourceExitsResult = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_EXIST, dataSourceExitsResult.get(Constants.STATUS));

        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(false);
        Map<String, Object> connectFailedResult = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED, connectFailedResult.get(Constants.STATUS));

        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(true);
        PowerMockito.when(DataSourceFactory.getDatasource(dataSourceType, parameter)).thenReturn(null);
        Map<String, Object> notValidError = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, notValidError.get(Constants.STATUS));

        // success
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(true);
        PowerMockito.when(DataSourceFactory.getDatasource(dataSourceType, parameter)).thenReturn(JSONUtils.parseObject(parameter, MySQLDataSource.class));
        Map<String, Object> success = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.SUCCESS, success.get(Constants.STATUS));
    }

    public void updateDataSourceTest() {
        User loginUser = getAdminUser();

        int dataSourceId = 12;
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";
        DbType dataSourceType = DbType.POSTGRESQL;
        String parameter = dataSourceService.buildParameter(dataSourceType, "172.16.133.200", "5432", "dolphinscheduler", null, "postgres", "", null, null);

        // data source not exits
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Map<String, Object> resourceNotExits = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST, resourceNotExits.get(Constants.STATUS));
        // user no operation perm
        DataSource dataSource = new DataSource();
        dataSource.setUserId(0);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Map<String, Object> userNoOperationPerm = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, userNoOperationPerm.get(Constants.STATUS));

        // data source name exits
        dataSource.setUserId(-1);
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(dataSource);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(dataSourceList);
        Map<String, Object> dataSourceNameExist = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_EXIST, dataSourceNameExist.get(Constants.STATUS));

        // data source connect failed
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(true);
        Map<String, Object> connectFailed = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED, connectFailed.get(Constants.STATUS));

        //success
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(false);
        Map<String, Object> success = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.SUCCESS, connectFailed.get(Constants.STATUS));

    }

    @Test
    public void queryDataSourceListPagingTest() {
        User loginUser = getAdminUser();
        String searchVal = "";
        int pageNo = 1;
        int pageSize = 10;
        Map<String, Object> success = dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
        Assert.assertEquals(Status.SUCCESS, success.get(Constants.STATUS));
    }

    @Test
    public void connectionTest() {
        int dataSourceId = -1;
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Assert.assertFalse(dataSourceService.connectionTest(dataSourceId));
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
        dataSource.setConnectionParams("{\"connectType\":\"ORACLE_SID\",\"address\":\"jdbc:oracle:thin:@192.168.xx.xx:49161\",\"database\":\"XE\",\"jdbcUrl\":\"jdbc:oracle:thin:@192.168.xx.xx:49161/XE\",\"user\":\"system\",\"password\":\"oracle\"}");

        return dataSource;
    }

    @Test
    public void buildParameter() {
        String param = dataSourceService.buildParameter(DbType.ORACLE, "192.168.9.1", "1521", "im"
                , "", "test", "test", DbConnectType.ORACLE_SERVICE_NAME, "");
        String expected = "{\"connectType\":\"ORACLE_SERVICE_NAME\",\"type\":\"ORACLE_SERVICE_NAME\",\"address\":\"jdbc:oracle:thin:@//192.168.9.1:1521\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:oracle:thin:@//192.168.9.1:1521/im\",\"user\":\"test\",\"password\":\"test\"}";
        Assert.assertEquals(expected, param);
    }

    @Test
    public void buildParameterWithDecodePassword() {
        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "true");
        String param = dataSourceService.buildParameter(DbType.MYSQL, "192.168.9.1", "1521", "im"
                , "", "test", "123456", null, "");
        String expected = "{\"type\":null,\"address\":\"jdbc:mysql://192.168.9.1:1521\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/im\",\"user\":\"test\",\"password\":\"IUAjJCVeJipNVEl6TkRVMg==\"}";
        Assert.assertEquals(expected, param);


        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "false");
        param = dataSourceService.buildParameter(DbType.MYSQL, "192.168.9.1", "1521", "im"
                , "", "test", "123456", null, "");
        expected = "{\"type\":null,\"address\":\"jdbc:mysql://192.168.9.1:1521\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/im\",\"user\":\"test\",\"password\":\"123456\"}";
        Assert.assertEquals(expected, param);
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

}