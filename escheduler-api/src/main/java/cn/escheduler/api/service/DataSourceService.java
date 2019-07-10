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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.DbType;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.common.job.db.*;
import cn.escheduler.common.utils.CommonUtils;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.dao.mapper.DataSourceMapper;
import cn.escheduler.dao.mapper.DatasourceUserMapper;
import cn.escheduler.dao.mapper.ProjectMapper;
import cn.escheduler.dao.model.DataSource;
import cn.escheduler.dao.model.Resource;
import cn.escheduler.dao.model.User;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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

import static cn.escheduler.common.utils.PropertyUtils.getString;

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
    public static final String PASSWORD = cn.escheduler.common.Constants.PASSWORD;
    public static final String OTHER = "other";


    @Autowired
    private DataSourceMapper dataSourceMapper;


    @Autowired
    private DatasourceUserMapper datasourceUserMapper;

    /**
     * create data source
     *
     * @param loginUser
     * @param name
     * @param desc
     * @param type
     * @param parameter
     * @return
     */
    public Map<String, Object> createDataSource(User loginUser, String name, String desc, DbType type, String parameter) {

        Map<String, Object> result = new HashMap<>(5);
        // check name can use or not
        if (checkName(name, result)) {
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
     * @param loginUser
     * @param name
     * @param desc
     * @param type
     * @param parameter
     * @return
     */
    public Map<String, Object> updateDataSource(int id, User loginUser, String name, String desc, DbType type, String parameter) {

        Map<String, Object> result = new HashMap<>();
        // determine whether the data source exists
        DataSource dataSource = dataSourceMapper.queryById(id);
        if (dataSource == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        //check name can use or not
        if(!name.trim().equals(dataSource.getName()) && checkName(name, result)){
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
        dataSourceMapper.update(dataSource);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private boolean checkName(String name, Map<String, Object> result) {
        List<DataSource> queryDataSource = dataSourceMapper.queryDataSourceByName(name.trim());
        if (queryDataSource != null && queryDataSource.size() > 0) {
            putMsg(result, Status.DATASOURCE_EXIST);
            return true;
        }
        return false;
    }


    /**
     * updateProcessInstance datasource
     */
    public Map<String, Object> queryDataSource(int id) {

        Map<String, Object> result = new HashMap<String, Object>(5);
        DataSource dataSource = dataSourceMapper.queryById(id);
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
        String database = datasourceForm.getDatabase();
        // jdbc connection params
        String other = datasourceForm.getOther();
        String address = datasourceForm.getAddress();

        String[] hostsPorts = getHostsAndPort(address);
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
     * @param loginUser
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Integer count = getTotalCount(loginUser);

        PageInfo pageInfo = new PageInfo<Resource>(pageNo, pageSize);
        pageInfo.setTotalCount(count);
        List<DataSource> datasourceList = getDataSources(loginUser, searchVal, pageSize, pageInfo);

        pageInfo.setLists(datasourceList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * get list paging
     *
     * @param loginUser
     * @param searchVal
     * @param pageSize
     * @param pageInfo
     * @return
     */
    private List<DataSource> getDataSources(User loginUser, String searchVal, Integer pageSize, PageInfo pageInfo) {
        List<DataSource> dataSourceList = null;
        if (isAdmin(loginUser)) {
            dataSourceList = dataSourceMapper.queryAllDataSourcePaging(searchVal, pageInfo.getStart(), pageSize);
        }else{
            dataSourceList = dataSourceMapper.queryDataSourcePaging(loginUser.getId(), searchVal,
                    pageInfo.getStart(), pageSize);
        }

        handlePasswd(dataSourceList);

        return dataSourceList;
    }


    /**
     * handle datasource connection password for safety
     * @param dataSourceList
     */
    private void handlePasswd(List<DataSource> dataSourceList) {

        for (DataSource dataSource : dataSourceList) {

            String connectionParams  = dataSource.getConnectionParams();
            JSONObject  object = JSONObject.parseObject(connectionParams);
            object.put(cn.escheduler.common.Constants.PASSWORD, cn.escheduler.common.Constants.XXXXXX);
            dataSource.setConnectionParams(JSONUtils.toJson(object));

        }
    }


    /**
     * get datasource total num
     *
     * @param loginUser
     * @return
     */
    private Integer getTotalCount(User loginUser) {
        if (isAdmin(loginUser)) {
            return dataSourceMapper.countAllDatasource();
        }
        return dataSourceMapper.countUserDatasource(loginUser.getId());
    }

    /**
     * query data resource list
     *
     * @param loginUser
     * @param type
     * @return
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
     * @param loginUser
     * @param name
     * @return
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
     * @param dbType
     * @param parameter
     * @return
     */
    private Connection getConnection(DbType dbType, String parameter) {
        Connection connection = null;
        BaseDataSource datasource = null;
        try {
            switch (dbType) {
                case POSTGRESQL:
                    datasource = JSONObject.parseObject(parameter, PostgreDataSource.class);
                    Class.forName(Constants.ORG_POSTGRESQL_DRIVER);
                    break;
                case MYSQL:
                    datasource = JSONObject.parseObject(parameter, MySQLDataSource.class);
                    Class.forName(Constants.COM_MYSQL_JDBC_DRIVER);
                    break;
                case HIVE:
                case SPARK:
                    if (CommonUtils.getKerberosStartupState())  {
                            System.setProperty(cn.escheduler.common.Constants.JAVA_SECURITY_KRB5_CONF,
                                    getString(cn.escheduler.common.Constants.JAVA_SECURITY_KRB5_CONF_PATH));
                            Configuration configuration = new Configuration();
                            configuration.set(cn.escheduler.common.Constants.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
                            UserGroupInformation.setConfiguration(configuration);
                            UserGroupInformation.loginUserFromKeytab(getString(cn.escheduler.common.Constants.LOGIN_USER_KEY_TAB_USERNAME),
                                    getString(cn.escheduler.common.Constants.LOGIN_USER_KEY_TAB_PATH));
                    }
                    if (dbType == DbType.HIVE){
                        datasource = JSONObject.parseObject(parameter, HiveDataSource.class);
                    }else if (dbType == DbType.SPARK){
                        datasource = JSONObject.parseObject(parameter, SparkDataSource.class);
                    }
                    Class.forName(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER);
                    break;
                case CLICKHOUSE:
                    datasource = JSONObject.parseObject(parameter, ClickHouseDataSource.class);
                    Class.forName(Constants.COM_CLICKHOUSE_JDBC_DRIVER);
                    break;
                case ORACLE:
                    datasource = JSONObject.parseObject(parameter, OracleDataSource.class);
                    Class.forName(Constants.COM_ORACLE_JDBC_DRIVER);
                    break;
                case SQLSERVER:
                    datasource = JSONObject.parseObject(parameter, SQLServerDataSource.class);
                    Class.forName(Constants.COM_SQLSERVER_JDBC_DRIVER);
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
     * @param type
     * @param parameter
     * @return
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
     * @param loginUser
     * @param id
     * @return
     */
    public boolean connectionTest(User loginUser, int id) {
        DataSource dataSource = dataSourceMapper.queryById(id);
        return checkConnection(dataSource.getType(), dataSource.getConnectionParams());
    }

    /**
     * build paramters
     *
     * @param name
     * @param desc
     * @param type
     * @param host
     * @param port
     * @param database
     * @param userName
     * @param password
     * @param other
     * @return
     */
    public String buildParameter(String name, String desc, DbType type, String host,
                                 String port, String database,String principal,String userName,
                                 String password, String other) {

        String address = buildAddress(type, host, port);

        String jdbcUrl = address + "/" + database;
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
                || Constants.SQLSERVER.equals(type.name())) {
            separator = ";";
        }

        Map<String, Object> parameterMap = new LinkedHashMap<String, Object>(6);
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
            Map map = JSONObject.parseObject(other, new TypeReference<LinkedHashMap<String, String>>() {
            });
            if (map.size() > 0) {
                Set<String> keys = map.keySet();
                StringBuilder otherSb = new StringBuilder();
                for (String key : keys) {
                    otherSb.append(String.format("%s=%s%s", key, map.get(key), separator));

                }
                otherSb.deleteCharAt(otherSb.length() - 1);
                parameterMap.put(Constants.OTHER, otherSb);
            }

        }

        if(logger.isDebugEnabled()){
            logger.info("parameters map-----" + JSONObject.toJSONString(parameterMap));
        }
        return JSONObject.toJSONString(parameterMap);


    }

    private String buildAddress(DbType type, String host, String port) {
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
            sb.append(Constants.JDBC_ORACLE);
            sb.append(host).append(":").append(port);
        } else if (Constants.SQLSERVER.equals(type.name())) {
            sb.append(Constants.JDBC_SQLSERVER);
            sb.append(host).append(":").append(port);
        }

        return sb.toString();
    }

    /**
     * delete datasource
     *
     * @param loginUser
     * @param datasourceId
     * @return
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public Result delete(User loginUser, int datasourceId) {
        Result result = new Result();
        try {
            //query datasource by id
            DataSource dataSource = dataSourceMapper.queryById(datasourceId);
            if(dataSource == null){
                logger.error("resource id {} not exist", datasourceId);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            if(loginUser.getId() != dataSource.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER){
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
            dataSourceMapper.deleteDataSourceById(datasourceId);
            datasourceUserMapper.deleteByDatasourceId(datasourceId);
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            logger.error("delete datasource fail",e);
            throw new RuntimeException("delete datasource fail");
        }
        return result;
    }

    /**
     * unauthorized datasource
     *
     * @param loginUser
     * @param userId
     * @return
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

            List<DataSource> authedDataSourceList = dataSourceMapper.authedDatasource(userId);

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
     * @param loginUser
     * @param userId
     * @return
     */
    public Map<String, Object> authedDatasource(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>(5);

        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        List<DataSource> authedDatasourceList = dataSourceMapper.authedDatasource(userId);
        result.put(Constants.DATA_LIST, authedDatasourceList);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * get host and port by address
     *
     * @param address
     * @return
     */
    private String[] getHostsAndPort(String address) {
        String[] result = new String[2];
        String[] tmpArray = address.split(cn.escheduler.common.Constants.DOUBLE_SLASH);
        String hostsAndPorts = tmpArray[tmpArray.length - 1];
        StringBuilder hosts = new StringBuilder();
        String[] hostPortArray = hostsAndPorts.split(cn.escheduler.common.Constants.COMMA);
        String port = hostPortArray[0].split(cn.escheduler.common.Constants.COLON)[1];
        for (String hostPort : hostPortArray) {
            hosts.append(hostPort.split(cn.escheduler.common.Constants.COLON)[0]).append(cn.escheduler.common.Constants.COMMA);
        }
        hosts.deleteCharAt(hosts.length() - 1);
        result[0] = hosts.toString();
        result[1] = port;
        return result;
    }
}
