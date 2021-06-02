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

<<<<<<< HEAD
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
=======
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * data source service
 */
public interface DataSourceService {

    /**
     * create data source
     *
     * @param loginUser login user
     * @param datasourceParam datasource parameter
     * @return create result code
     */
    Result<Object> createDataSource(User loginUser, BaseDataSourceParamDTO datasourceParam);

    /**
     * updateProcessInstance datasource
     *
     * @param loginUser login user
     * @param id data source id
     * @param dataSourceParam data source params
     * @return update result code
     */
    Result<Object> updateDataSource(int id, User loginUser, BaseDataSourceParamDTO dataSourceParam);

    /**
     * updateProcessInstance datasource
     *
     * @param id datasource id
     * @return data source detail
     */
<<<<<<< HEAD
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
        // ip host
        String host = null;
        // port
        String port = null;
        if (DbType.REMOTESERVER.equals(dataSource.getType())) {
            host = ((RemoteServerSource)datasourceForm).getHost();
            port = ((RemoteServerSource)datasourceForm).getPort();
        } else {
            String[] hostsPorts = getHostsAndPort(address,hostSeperator);
            host = hostsPorts[0];
            port = hostsPorts[1];
        }

        String separator;
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
        map.put(OTHER, otherMap);
        result.put(Constants.DATA_LIST, map);
        putMsg(result, Status.SUCCESS);
        return result;
    }

=======
    Map<String, Object> queryDataSource(int id);
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07

    /**
     * query datasource list by keyword
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return data source list page
     */
    Map<String, Object> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query data resource list
     *
     * @param loginUser login user
     * @param type      data source type
     * @return data source list page
     */
    Map<String, Object> queryDataSourceList(User loginUser, Integer type);

    /**
     * verify datasource exists
     *
     * @param name      datasource name
     * @return true if data datasource not exists, otherwise return false
     */
<<<<<<< HEAD
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
                    datasource = JSONUtils.parseObject(parameter, PostgreDataSource.class);
                    Class.forName(Constants.ORG_POSTGRESQL_DRIVER);
                    break;
                case MYSQL:
                    datasource = JSONUtils.parseObject(parameter, MySQLDataSource.class);
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
                        datasource = JSONUtils.parseObject(parameter, HiveDataSource.class);
                    }else if (dbType == DbType.SPARK){
                        datasource = JSONUtils.parseObject(parameter, SparkDataSource.class);
                    }
                    Class.forName(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER);
                    break;
                case CLICKHOUSE:
                    datasource = JSONUtils.parseObject(parameter, ClickHouseDataSource.class);
                    Class.forName(Constants.COM_CLICKHOUSE_JDBC_DRIVER);
                    break;
                case ORACLE:
                    datasource = JSONUtils.parseObject(parameter, OracleDataSource.class);
                    Class.forName(Constants.COM_ORACLE_JDBC_DRIVER);
                    break;
                case SQLSERVER:
                    datasource = JSONUtils.parseObject(parameter, SQLServerDataSource.class);
                    Class.forName(Constants.COM_SQLSERVER_JDBC_DRIVER);
                    break;
                case DB2:
                    datasource = JSONUtils.parseObject(parameter, DB2ServerDataSource.class);
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
     * get remote sever session
     *
     * @param parameter parameter
     * @return session for remote server
     */
    private Session getRemoteSession(String parameter) {
        Session session = null;
        try {
            RemoteServerSource serverSource = JSONUtils.parseObject(parameter, RemoteServerSource.class);
            JSch jsch = new JSch();
            session = jsch.getSession(serverSource.getUser(), serverSource.getHost(), Integer.parseInt(serverSource.getPort()));
            session.setPassword(serverSource.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            // making a connection with timeout.
            session.connect(Constants.REMOTESERVER_TIME_OUT);
        } catch (Exception e) {
            session = null;
            logger.error(e.getMessage(),e);
        }
        return session;
    }
=======
    Result<Object> verifyDataSourceName(String name);
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07

    /**
     * check connection
     *
     * @param type      data source type
     * @param parameter data source parameters
     * @return true if connect successfully, otherwise false
     */
<<<<<<< HEAD
    public boolean checkConnection(DbType type, String parameter) {
        Boolean isConnection = false;

        if (Constants.REMOTESERVER.equals(type.name())) {
            Session session = getRemoteSession(parameter);
            if (session != null) {
                isConnection = true;
                try {
                    session.disconnect();
                } catch (Exception e) {
                    logger.error("close connection fail at DataSourceService::checkConnection()", e);
                }
            }
        }

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

=======
    Result<Object> checkConnection(DbType type, ConnectionParam parameter);
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07

    /**
     * test connection
     *
     * @param id datasource id
     * @return connect result code
     */
<<<<<<< HEAD
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

        parameterMap.put(TYPE, connectType);
        parameterMap.put(Constants.ADDRESS, address);
        parameterMap.put(Constants.DATABASE, database);
        parameterMap.put(Constants.JDBC_URL, jdbcUrl);
        parameterMap.put(Constants.USER, userName);
        parameterMap.put(Constants.PASSWORD, password);
        if (Constants.REMOTESERVER.equals(type.name())) {
            parameterMap.put(Constants.HOST, host);
            parameterMap.put(Constants.PORT, port);
        }
        if (CommonUtils.getKerberosStartupState() &&
                (type == DbType.HIVE || type == DbType.SPARK)){
            parameterMap.put(Constants.PRINCIPAL,principal);
        }
        if (other != null && !"".equals(other)) {
            Map<String, String> map = JSONUtils.toMap(other);
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
            logger.info("parameters map:{}", JSONUtils.toJsonString(parameterMap));
        }
        return JSONUtils.toJsonString(parameterMap);


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
=======
    Result<Object> connectionTest(int id);
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07

    /**
     * delete datasource
     *
     * @param loginUser    login user
     * @param datasourceId data source id
     * @return delete result code
     */
    Result<Object> delete(User loginUser, int datasourceId);

    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthed data source result code
     */
    Map<String, Object> unauthDatasource(User loginUser, Integer userId);

    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result code
     */
    Map<String, Object> authedDatasource(User loginUser, Integer userId);
}
