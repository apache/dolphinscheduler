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
package cn.escheduler.dao.upgrade;

import cn.escheduler.common.enums.DbType;
import cn.escheduler.common.utils.ConnectionUtils;
import cn.escheduler.common.utils.SchemaUtils;
import cn.escheduler.common.utils.ScriptRunner;
import cn.escheduler.dao.AbstractBaseDao;
import cn.escheduler.dao.datasource.ConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

public abstract class UpgradeDao extends AbstractBaseDao {

    public static final Logger logger = LoggerFactory.getLogger(UpgradeDao.class);
    private static final String T_VERSION_NAME = "t_escheduler_version";
    private static final String T_NEW_VERSION_NAME = "t_dolphinscheduler_version";
    private static final String rootDir = System.getProperty("user.dir");
    private static final DbType dbType = getCurrentDbType();

    @Override
    protected void init() {

    }

    /**
     * get db type
     * @return
     */
    public static DbType getDbType(){
        return dbType;
    }

    /**
     * get db type
     * @return
     */
    private static DbType getCurrentDbType(){
        Connection conn = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            String name = conn.getMetaData().getDatabaseProductName().toUpperCase();
            return DbType.valueOf(name);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }finally {
            ConnectionUtils.releaseResource(null, null, conn);
        }
    }

    public void initSchema(){
        DbType dbType = getDbType();
        String initSqlPath = "";
        if (dbType != null) {
            switch (dbType) {
                case MYSQL:
                    initSqlPath = "/sql/create/release-1.0.0_schema/mysql/";
                    initSchema(initSqlPath);
                    break;
                case POSTGRESQL:
                    initSqlPath = "/sql/create/release-1.2.0_schema/postgresql/";
                    initSchema(initSqlPath);
                    break;
                default:
                    logger.error("not support sql type: {},can't upgrade", dbType);
                    throw new IllegalArgumentException("not support sql type,can't upgrade");
            }
        }
    }


    public void initSchema(String initSqlPath) {

        // Execute the escheduler DDL, it cannot be rolled back
        runInitDDL(initSqlPath);

        // Execute the escheduler DML, it can be rolled back
        runInitDML(initSqlPath);

    }

    private void runInitDML(String initSqlPath) {
        Connection conn = null;
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        //String mysqlSQLFilePath = rootDir + "/sql/create/release-1.0.0_schema/mysql/escheduler_dml.sql";
        String mysqlSQLFilePath = rootDir + initSqlPath + "dolphinscheduler_dml.sql";
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            conn.setAutoCommit(false);
            // 执行escheduler_dml.sql脚本，导入escheduler相关的数据
            // Execute the ark_manager_dml.sql script to import the data related to escheduler

            ScriptRunner initScriptRunner = new ScriptRunner(conn, false, true);
            Reader initSqlReader = new FileReader(new File(mysqlSQLFilePath));
            initScriptRunner.runScript(initSqlReader);

            conn.commit();
        } catch (IOException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(),e1);
            }
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(),e1);
            }
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(null, null, conn);

        }

    }

    private void runInitDDL(String initSqlPath) {
        Connection conn = null;
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        //String mysqlSQLFilePath = rootDir + "/sql/create/release-1.0.0_schema/mysql/dolphinscheduler_ddl.sql";
        String mysqlSQLFilePath = rootDir + initSqlPath + "dolphinscheduler_ddl.sql";
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            // Execute the escheduler_ddl.sql script to create the table structure of escheduler
            ScriptRunner initScriptRunner = new ScriptRunner(conn, true, true);
            Reader initSqlReader = new FileReader(new File(mysqlSQLFilePath));
            initScriptRunner.runScript(initSqlReader);

        } catch (IOException e) {

            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } catch (Exception e) {

            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(null, null, conn);

        }

    }

    /**
     * Determines whether a table exists
     * @param tableName
     * @return
     */
    public abstract boolean isExistsTable(String tableName);

    /**
     * Determines whether a field exists in the specified table
     * @param tableName
     * @param columnName
     * @return
     */
    public abstract boolean isExistsColumn(String tableName,String columnName);


    public String getCurrentVersion(String versionName) {
        String sql = String.format("select version from %s",versionName);
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String version = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                version = rs.getString(1);
            }

            return version;

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }
    }


    public void upgradeDolphinScheduler(String schemaDir) {

        upgradeDolphinSchedulerDDL(schemaDir);

        upgradeDolphinSchedulerDML(schemaDir);

    }

    private void upgradeDolphinSchedulerDML(String schemaDir) {
        String schemaVersion = schemaDir.split("_")[0];
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String mysqlSQLFilePath = MessageFormat.format("{0}/sql/upgrade/{1}/{2}/dolphinscheduler_dml.sql",rootDir,schemaDir,getDbType().name().toLowerCase());
        logger.info("mysqlSQLFilePath"+mysqlSQLFilePath);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            conn.setAutoCommit(false);
            // Execute the upgraded escheduler dml
            ScriptRunner scriptRunner = new ScriptRunner(conn, false, true);
            Reader sqlReader = new FileReader(new File(mysqlSQLFilePath));
            scriptRunner.runScript(sqlReader);
            if (isExistsTable(T_VERSION_NAME)) {
                // Change version in the version table to the new version
                String upgradeSQL = String.format("update %s set version = ?",T_VERSION_NAME);
                pstmt = conn.prepareStatement(upgradeSQL);
                pstmt.setString(1, schemaVersion);
                pstmt.executeUpdate();
            }else if (isExistsTable(T_NEW_VERSION_NAME)) {
                // Change version in the version table to the new version
                String upgradeSQL = String.format("update %s set version = ?",T_NEW_VERSION_NAME);
                pstmt = conn.prepareStatement(upgradeSQL);
                pstmt.setString(1, schemaVersion);
                pstmt.executeUpdate();
            }
            conn.commit();
        } catch (FileNotFoundException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(),e1);
            }
            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql file not found ", e);
        } catch (IOException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(),e1);
            }
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(),e1);
            }
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(),e1);
            }
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(null, pstmt, conn);
        }

    }

    private void upgradeDolphinSchedulerDDL(String schemaDir) {
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String mysqlSQLFilePath = MessageFormat.format("{0}/sql/upgrade/{1}/{2}/dolphinscheduler_ddl.sql",rootDir,schemaDir,getDbType().name().toLowerCase());
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            String dbName = conn.getCatalog();
            logger.info(dbName);
            conn.setAutoCommit(true);
            // Execute the escheduler ddl.sql for the upgrade
            ScriptRunner scriptRunner = new ScriptRunner(conn, true, true);
            Reader sqlReader = new FileReader(new File(mysqlSQLFilePath));
            scriptRunner.runScript(sqlReader);

        } catch (FileNotFoundException e) {

            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql file not found ", e);
        } catch (IOException e) {

            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } catch (SQLException e) {

            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } catch (Exception e) {

            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(null, pstmt, conn);
        }

    }



    public void updateVersion(String version) {
        // Change version in the version table to the new version
        String versionName = T_VERSION_NAME;
        if(!SchemaUtils.isAGreatVersion("1.2.0" , version)){
            versionName = "t_dolphinscheduler_version";
        }
        String upgradeSQL = String.format("update %s set version = ?",versionName);
        PreparedStatement pstmt = null;
        Connection conn = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            pstmt = conn.prepareStatement(upgradeSQL);
            pstmt.setString(1, version);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException("sql: " + upgradeSQL, e);
        } finally {
            ConnectionUtils.releaseResource(null, pstmt, conn);
        }

    }

}
