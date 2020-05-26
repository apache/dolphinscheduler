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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.datasource.*;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.apache.dolphinscheduler.common.utils.PropertyUtils.getString;

/**
 * datasource service
 */
@Service
public class DataSourceService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(DataSourceService.class);

    public static final String NAME = "name";
    public static final String NOTE = "note";
    public static final String TYPE = "type";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String PRINCIPAL = "principal";
    public static final String DATABASE = "database";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = Constants.PASSWORD;
    public static final String OTHER = "other";


    @Autowired
    private DataSourceMapper dataSourceMapper;


    @Autowired
    private DataSourceUserMapper datasourceUserMapper;

    /**
     * create data source
     *
     * @param loginUser login user
     * @param name data source name
     * @param desc data source description
     * @param type data source type
     * @param parameter datasource parameters
     * @return create result code
     */
    public Map<String, Object> createDataSource(User loginUser, String name, String desc, DbType type, String parameter) {

        Map<String, Object> result = new HashMap<>(5);
        // check name can use or not
        if (checkName(name)) {
            putMsg(result, Status.DATASOURCE_EXIST);
            return result;
        }
        Boolean isConnection = checkConnection(type, parameter);
        if (!isConnection) {
            logger.info("connect failed, type:{}, parameter:{}", type, parameter);
            putMsg(result, Status.DATASOURCE_CONNECT_FAILED);
            return result;
        }

        BaseDataSource datasource = DataSourceFactory.getDatasource(type, parameter);
        if (datasource == null) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, parameter);
            return result;
        }

        // build datasource
        DataSource dataSource = new DataSource();
        Date now = new Date();

        dataSource.setName(name.trim());
        dataSource.setNote(desc);
        dataSource.setUserId(loginUser.getId());
        dataSource.setUserName(loginUser.getUserName());
        dataSource.setType(type);
        dataSource.setConnectionParams(parameter);
        dataSource.setCreateTime(now);
        dataSource.setUpdateTime(now);
        dataSourceMapper.insert(dataSource);

        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * updateProcessInstance datasource
     *
     * @param loginUser login user
     * @param name data source name
     * @param desc data source description
     * @param type data source type
     * @param parameter datasource parameters
     * @param id data source id
     * @return update result code
     */
    public Map<String, Object> updateDataSource(int id, User loginUser, String name, String desc, DbType type, String parameter) {

        Map<String, Object> result = new HashMap<>();
        // determine whether the data source exists
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        if(!hasPerm(loginUser, dataSource.getUserId())){
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        //check name can use or not
        if(!name.trim().equals(dataSource.getName()) && checkName(name)){
            putMsg(result, Status.DATASOURCE_EXIST);
            return result;
        }

        Boolean isConnection = checkConnection(type, parameter);
        if (!isConnection) {
            logger.info("connect failed, type:{}, parameter:{}", type, parameter);
            putMsg(result, Status.DATASOURCE_CONNECT_FAILED);
            return result;
        }
        Date now = new Date();

        dataSource.setName(name.trim());
        dataSource.setNote(desc);
        dataSource.setUserName(loginUser.getUserName());
        dataSource.setType(type);
        dataSource.setConnectionParams(parameter);
        dataSource.setUpdateTime(now);
        dataSourceMapper.updateById(dataSource);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private boolean checkName(String name) {
        List<DataSource> queryDataSource = dataSourceMapper.queryDataSourceByName(name.trim());
        if (queryDataSource != null && queryDataSource.size() > 0) {
            return true;
        }
        return false;
    }


    /**
     * updateProcessInstance datasource
     * @param id datasource id
     * @return data source detail
     */
    public Map<String, Object> queryDataSource(int id) {

        Map<String, Object> result = new HashMap<String, Object>(5);
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        // type
        String dataSourceType = dataSource.getType().toString();
        // name
        String dataSourceName = dataSource.getName();
        // desc
        String desc = dataSource.getNote();
        // parameter
        String parameter = dataSource.getConnectionParams();

        BaseDataSource datasourceForm = DataSourceFactory.getDatasource(dataSource.getType(), parameter);
        DbConnectType  connectType = null;
        String hostSeperator = Constants.DOUBLE_SLASH;
        if(DbType.ORACLE.equals(dataSource.getType())){
            connectType = ((OracleDataSource) datasourceForm).getConnectType();
            if(DbConnectType.ORACLE_SID.equals(connectType)){
                hostSeperator = Constants.AT_SIGN;
            }
        }
        String database = datasourceForm.getDatabase();
        // jdbc connection params
        String other = datasourceForm.getOther();
        String address = datasourceForm.getAddress();

        String[] hostsPorts = getHostsAndPort(address,hostSeperator);
        // ip host
        String host = hostsPorts[0];
        // prot
        String port = hostsPorts[1];
        String separator = "";

        switch (dataSource.getType()) {
            case HIVE:
            case SQLSERVER:
                separator = ";";
                break;
            case MYSQL:
            case POSTGRESQL:
            case CLICKHOUSE:
            case ORACLE:
                separator = "&";
                break;
            default:
                separator = "&";
                break;
        }

        Map<String, String> otherMap = new LinkedHashMap<String, String>();
        if (other != null) {
            String[] configs = other.split(separator);
            for (String config : configs) {
                otherMap.put(config.split("=")[0], config.split("=")[1]);
            }

        }

        Map<String, Object> map = new HashMap<>(10);
        map.put(NAME, dataSourceName);
        map.put(NOTE, desc);
        map.put(TYPE, dataSourceType);
        if (connectType != null) {
            map.put(Constants.ORACLE_DB_CONNECT_TYPE, connectType);
        }

        map.put(HOST, host);
        map.put(PORT, port);
        map.put(PRINCIPAL, datasourceForm.getPrincipal());
        map.put(DATABASE, database);
        map.put(USER_NAME, datasourceForm.getUser());
        map.put(PASSWORD, datasourceForm.getPassword());
        map.put(OTHER, otherMap);
        result.put(Constants.DATA_LIST, map);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * query datasource list by keyword
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return data source list page
     */
    public Map<String, Object> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        IPage<DataSource> dataSourceList = null;
        Page<DataSource> dataSourcePage = new Page(pageNo, pageSize);

        if (isAdmin(loginUser)) {
            dataSourceList = dataSourceMapper.selectPaging(dataSourcePage, 0, searchVal);
        }else{
            dataSourceList = dataSourceMapper.selectPaging(dataSourcePage, loginUser.getId(), searchVal);
        }

        List<DataSource> dataSources = dataSourceList.getRecords();
        handlePasswd(dataSources);
        PageInfo pageInfo = new PageInfo<Resource>(pageNo, pageSize);
        pageInfo.setTotalCount((int)(dataSourceList.getTotal()));
        pageInfo.setLists(dataSources);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * handle datasource connection password for safety
     * @param dataSourceList
     */
    private void handlePasswd(List<DataSource> dataSourceList) {

        for (DataSource dataSource : dataSourceList) {

            String connectionParams  = dataSource.getConnectionParams();
            JSONObject  object = JSON.parseObject(connectionParams);
            object.put(Constants.PASSWORD, Constants.XXXXXX);
            dataSource.setConnectionParams(JSONUtils.toJson(object));

        }
    }

    /**
     * query data resource list
     *
     * @param loginUser login user
     * @param type data source type
     * @return data source list page
     */
    public Map<String, Object> queryDataSourceList(User loginUser, Integer type) {
        Map<String, Object> result = new HashMap<>(5);

        List<DataSource> datasourceList;

        if (isAdmin(loginUser)) {
            datasourceList = dataSourceMapper.listAllDataSourceByType(type);
        }else{
            datasourceList = dataSourceMapper.queryDataSourceByType(loginUser.getId(), type);
        }

        result.put(Constants.DATA_LIST, datasourceList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * verify datasource exists
     *
     * @param loginUser login user
     * @param name datasource name
     * @return true if data datasource not exists, otherwise return false
     */
    public Result verifyDataSourceName(User loginUser, String name) {
        Result result = new Result();
        List<DataSource> dataSourceList = dataSourceMapper.queryDataSourceByName(name);
        if (dataSourceList != null && dataSourceList.size() > 0) {
            logger.error("datasource name:{} has exist, can't create again.", name);
            putMsg(result, Status.DATASOURCE_EXIST);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * get connection
     *
     * @param dbType datasource type
     * @param parameter parameter
     * @return connection for datasource
     */
    private Connection getConnection(DbType dbType, String parameter) {
        Connection connection = null;
        BaseDataSource datasource = null;
        try {
            switch (dbType) {
                case POSTGRESQL:
                    datasource = JSON.parseObject(parameter, PostgreDataSource.class);
                    Class.forName(Constants.ORG_POSTGRESQL_DRIVER);
                    break;
                case MYSQL:
                    datasource = JSON.parseObject(parameter, MySQLDataSource.class);
                    Class.forName(Constants.COM_MYSQL_JDBC_DRIVER);
                    break;
                case HIVE:
                case SPARK:
                    if (CommonUtils.getKerberosStartupState())  {
                            System.setProperty(org.apache.dolphinscheduler.common.Constants.JAVA_SECURITY_KRB5_CONF,
                                    getString(org.apache.dolphinscheduler.common.Constants.JAVA_SECURITY_KRB5_CONF_PATH));
                            Configuration configuration = new Configuration();
                            configuration.set(org.apache.dolphinscheduler.common.Constants.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
                            UserGroupInformation.setConfiguration(configuration);
                            UserGroupInformation.loginUserFromKeytab(getString(org.apache.dolphinscheduler.common.Constants.LOGIN_USER_KEY_TAB_USERNAME),
                                    getString(org.apache.dolphinscheduler.common.Constants.LOGIN_USER_KEY_TAB_PATH));
                    }
                    if (dbType == DbType.HIVE){
                        datasource = JSON.parseObject(parameter, HiveDataSource.class);
                    }else if (dbType == DbType.SPARK){
                        datasource = JSON.parseObject(parameter, SparkDataSource.class);
                    }
                    Class.forName(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER);
                    break;
                case CLICKHOUSE:
                    datasource = JSON.parseObject(parameter, ClickHouseDataSource.class);
                    Class.forName(Constants.COM_CLICKHOUSE_JDBC_DRIVER);
                    break;
                case ORACLE:
                    datasource = JSON.parseObject(parameter, OracleDataSource.class);
                    Class.forName(Constants.COM_ORACLE_JDBC_DRIVER);
                    break;
                case SQLSERVER:
                    datasource = JSON.parseObject(parameter, SQLServerDataSource.class);
                    Class.forName(Constants.COM_SQLSERVER_JDBC_DRIVER);
                    break;
                case DB2:
                    datasource = JSON.parseObject(parameter, DB2ServerDataSource.class);
                    Class.forName(Constants.COM_DB2_JDBC_DRIVER);
                    break;
                default:
                    break;
            }

            if(datasource != null){
                connection = DriverManager.getConnection(datasource.getJdbcUrl(), datasource.getUser(), datasource.getPassword());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return connection;
    }


    /**
     * check connection
     *
     * @param type data source type
     * @param parameter data source parameters
     * @return true if connect successfully, otherwise false
     */
    public boolean checkConnection(DbType type, String parameter) {
        Boolean isConnection = false;
        Connection con = getConnection(type, parameter);
        if (con != null) {
            isConnection = true;
            try {
                con.close();
            } catch (SQLException e) {
                logger.error("close connection fail at DataSourceService::checkConnection()", e);
            }
        }
        return isConnection;
    }


    /**
     * test connection
     *
     * @param loginUser login user
     * @param id datasource id
     * @return connect result code
     */
    public boolean connectionTest(User loginUser, int id) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        return checkConnection(dataSource.getType(), dataSource.getConnectionParams());
    }

    /**
     * build paramters
     *
     * @param name data source name
     * @param desc data source description
     * @param type data source  type
     * @param host data source  host
     * @param port data source port
     * @param database data source database name
     * @param userName user name
     * @param password password
     * @param other other parameters
     * @param principal principal
     * @return datasource parameter
     */
    public String buildParameter(String name, String desc, DbType type, String host,
                                 String port, String database, String principal, String userName,
                                 String password, DbConnectType connectType, String other) {

        String address = buildAddress(type, host, port, connectType);
        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>(6);
        String jdbcUrl = address + "/" + database;
        if (Constants.ORACLE.equals(type.name())) {
            parameterMap.put(Constants.ORACLE_DB_CONNECT_TYPE, connectType);
        }

        if (CommonUtils.getKerberosStartupState() &&
                (type == DbType.HIVE || type == DbType.SPARK)){
            jdbcUrl += ";principal=" + principal;
        }

        String separator = "";
        if (Constants.MYSQL.equals(type.name())
                || Constants.POSTGRESQL.equals(type.name())
                || Constants.CLICKHOUSE.equals(type.name())
                || Constants.ORACLE.equals(type.name())) {
            separator = "&";
        } else if (Constants.HIVE.equals(type.name())
                || Constants.SPARK.equals(type.name())
                || Constants.DB2.equals(type.name())
                || Constants.SQLSERVER.equals(type.name())) {
            separator = ";";
        }

        parameterMap.put(Constants.ADDRESS, address);
        parameterMap.put(Constants.DATABASE, database);
        parameterMap.put(Constants.JDBC_URL, jdbcUrl);
        parameterMap.put(Constants.USER, userName);
        parameterMap.put(Constants.PASSWORD, password);
        if (CommonUtils.getKerberosStartupState() &&
                (type == DbType.HIVE || type == DbType.SPARK)){
            parameterMap.put(Constants.PRINCIPAL,principal);
        }
        if (other != null && !"".equals(other)) {
            LinkedHashMap<String, String> map = JSON.parseObject(other, new TypeReference<LinkedHashMap<String, String>>() {
            });
            if (map.size() > 0) {
                StringBuilder otherSb = new StringBuilder();
                for (Map.Entry<String, String> entry: map.entrySet()) {
                    otherSb.append(String.format("%s=%s%s", entry.getKey(), entry.getValue(), separator));
                }
                if (!Constants.DB2.equals(type.name())) {
                    otherSb.deleteCharAt(otherSb.length() - 1);
                }
                parameterMap.put(Constants.OTHER, otherSb);
            }

        }

        if(logger.isDebugEnabled()){
            logger.info("parameters map-----" + JSON.toJSONString(parameterMap));
        }
        return JSON.toJSONString(parameterMap);


    }

    private String buildAddress(DbType type, String host, String port, DbConnectType connectType) {
        StringBuilder sb = new StringBuilder();
        if (Constants.MYSQL.equals(type.name())) {
            sb.append(Constants.JDBC_MYSQL);
            sb.append(host).append(":").append(port);
        } else if (Constants.POSTGRESQL.equals(type.name())) {
            sb.append(Constants.JDBC_POSTGRESQL);
            sb.append(host).append(":").append(port);
        } else if (Constants.HIVE.equals(type.name()) || Constants.SPARK.equals(type.name())) {
            sb.append(Constants.JDBC_HIVE_2);
            String[] hostArray = host.split(",");
            if (hostArray.length > 0) {
                for (String zkHost : hostArray) {
                    sb.append(String.format("%s:%s,", zkHost, port));
                }
                sb.deleteCharAt(sb.length() - 1);
            }
        } else if (Constants.CLICKHOUSE.equals(type.name())) {
            sb.append(Constants.JDBC_CLICKHOUSE);
            sb.append(host).append(":").append(port);
        } else if (Constants.ORACLE.equals(type.name())) {
            if (connectType == DbConnectType.ORACLE_SID) {
                sb.append(Constants.JDBC_ORACLE_SID);
            } else {
                sb.append(Constants.JDBC_ORACLE_SERVICE_NAME);
            }
            sb.append(host).append(":").append(port);
        } else if (Constants.SQLSERVER.equals(type.name())) {
            sb.append(Constants.JDBC_SQLSERVER);
            sb.append(host).append(":").append(port);
        }else if (Constants.DB2.equals(type.name())) {
            sb.append(Constants.JDBC_DB2);
            sb.append(host).append(":").append(port);
        }

        return sb.toString();
    }

    /**
     * delete datasource
     *
     * @param loginUser login user
     * @param datasourceId data source id
     * @return delete result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Result delete(User loginUser, int datasourceId) {
        Result result = new Result();
        try {
            //query datasource by id
            DataSource dataSource = dataSourceMapper.selectById(datasourceId);
            if(dataSource == null){
                logger.error("resource id {} not exist", datasourceId);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            if(!hasPerm(loginUser, dataSource.getUserId())){
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
            dataSourceMapper.deleteById(datasourceId);
            datasourceUserMapper.deleteByDatasourceId(datasourceId);
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            logger.error("delete datasource error",e);
            throw new RuntimeException("delete datasource error");
        }
        return result;
    }

    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthed data source result code
     */
    public Map<String, Object> unauthDatasource(User loginUser, Integer userId) {

        Map<String, Object> result = new HashMap<>();
        //only admin operate
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        /**
         * query all data sources except userId
         */
        List<DataSource> resultList = new ArrayList<>();
        List<DataSource> datasourceList = dataSourceMapper.queryDatasourceExceptUserId(userId);
        Set<DataSource> datasourceSet = null;
        if (datasourceList != null && datasourceList.size() > 0) {
            datasourceSet = new HashSet<>(datasourceList);

            List<DataSource> authedDataSourceList = dataSourceMapper.queryAuthedDatasource(userId);

            Set<DataSource> authedDataSourceSet = null;
            if (authedDataSourceList != null && authedDataSourceList.size() > 0) {
                authedDataSourceSet = new HashSet<>(authedDataSourceList);
                datasourceSet.removeAll(authedDataSourceSet);

            }
            resultList = new ArrayList<>(datasourceSet);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    public Map<String, Object> authedDatasource(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>(5);

        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        List<DataSource> authedDatasourceList = dataSourceMapper.queryAuthedDatasource(userId);
        result.put(Constants.DATA_LIST, authedDatasourceList);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * get host and port by address
     *
     * @param address   address
     * @return sting array: [host,port]
     */
    private String[] getHostsAndPort(String address) {
        return getHostsAndPort(address,Constants.DOUBLE_SLASH);
    }

    /**
     * get host and port by address
     *
     * @param address   address
     * @param separator separator
     * @return sting array: [host,port]
     */
    private String[] getHostsAndPort(String address,String separator) {
        String[] result = new String[2];
        String[] tmpArray = address.split(separator);
        String hostsAndPorts = tmpArray[tmpArray.length - 1];
        StringBuilder hosts = new StringBuilder();
        String[] hostPortArray = hostsAndPorts.split(Constants.COMMA);
        String port = hostPortArray[0].split(Constants.COLON)[1];
        for (String hostPort : hostPortArray) {
            hosts.append(hostPort.split(Constants.COLON)[0]).append(Constants.COMMA);
        }
        hosts.deleteCharAt(hosts.length() - 1);
        result[0] = hosts.toString();
        result[1] = port;
        return result;
    }
}
