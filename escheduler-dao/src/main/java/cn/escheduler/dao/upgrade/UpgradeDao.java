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

import cn.escheduler.common.utils.MysqlUtils;
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

public class UpgradeDao extends AbstractBaseDao {

    public static final Logger logger = LoggerFactory.getLogger(UpgradeDao.class);
    private static final String T_VERSION_NAME = "t_escheduler_version";
    private static final String rootDir = System.getProperty("user.dir");

    @Override
    protected void init() {

    }

    private static class UpgradeDaoHolder {
        private static final UpgradeDao INSTANCE = new UpgradeDao();
    }

    private UpgradeDao() {
    }

    public static final UpgradeDao getInstance() {
        return UpgradeDaoHolder.INSTANCE;
    }



    public void initEschedulerSchema() {

        // Execute the escheduler DDL, it cannot be rolled back
        runInitEschedulerDDL();

        // Execute the escheduler DML, it can be rolled back
        runInitEschedulerDML();

    }

    private void runInitEschedulerDML() {
        Connection conn = null;
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String mysqlSQLFilePath = rootDir + "/sql/create/release-1.0.0_schema/mysql/escheduler_dml.sql";
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
            MysqlUtils.releaseResource(null, null, conn);

        }

    }

    private void runInitEschedulerDDL() {
        Connection conn = null;
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String mysqlSQLFilePath = rootDir + "/sql/create/release-1.0.0_schema/mysql/escheduler_ddl.sql";
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
            MysqlUtils.releaseResource(null, null, conn);

        }

    }

    /**
     * Determines whether a table exists
     * @param tableName
     * @return
     */
    public boolean isExistsTable(String tableName) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            MysqlUtils.releaseResource(null, null, conn);

        }

    }

    /**
     * Determines whether a field exists in the specified table
     * @param tableName
     * @param columnName
     * @return
     */
    public boolean isExistsColumn(String tableName,String columnName) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            ResultSet rs = conn.getMetaData().getColumns(null,null,tableName,columnName);
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            MysqlUtils.releaseResource(null, null, conn);

        }

    }


    public String getCurrentVersion() {
        String sql = String.format("select version from %s",T_VERSION_NAME);
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
            MysqlUtils.releaseResource(rs, pstmt, conn);

        }
    }


    public void upgradeEscheduler(String schemaDir) {

        upgradeEschedulerDDL(schemaDir);

        upgradeEschedulerDML(schemaDir);

    }

    private void upgradeEschedulerDML(String schemaDir) {
        String schemaVersion = schemaDir.split("_")[0];
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String mysqlSQLFilePath = rootDir + "/sql/upgrade/" + schemaDir + "/mysql/escheduler_dml.sql";
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
            MysqlUtils.releaseResource(null, pstmt, conn);
        }

    }

    private void upgradeEschedulerDDL(String schemaDir) {
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String mysqlSQLFilePath = rootDir + "/sql/upgrade/" + schemaDir + "/mysql/escheduler_ddl.sql";
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
            MysqlUtils.releaseResource(null, pstmt, conn);
        }

    }



    public void updateVersion(String version) {
        // Change version in the version table to the new version
        String upgradeSQL = String.format("update %s set version = ?",T_VERSION_NAME);
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
            MysqlUtils.releaseResource(null, pstmt, conn);
        }

    }

}
