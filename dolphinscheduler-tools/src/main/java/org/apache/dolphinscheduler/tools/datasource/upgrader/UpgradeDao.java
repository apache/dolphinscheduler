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

package org.apache.dolphinscheduler.tools.datasource.upgrader;

import org.apache.dolphinscheduler.common.sql.SqlScriptRunner;
import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;
import org.apache.dolphinscheduler.tools.datasource.dao.ResourceDao;
import org.apache.dolphinscheduler.tools.datasource.utils.SchemaUtils;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.annotation.DbType;

@Slf4j
@Service
public class UpgradeDao {

    private static final String T_VERSION_NAME = "t_escheduler_version";
    private static final String T_NEW_VERSION_NAME = "t_ds_version";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DbType dbType;

    @Autowired
    private DatabaseDialect databaseDialect;

    /**
     * run init sql to init db schema
     */
    public void initSchema() {
        // Execute the dolphinscheduler full sql
        String sqlFilePath = String.format("sql/dolphinscheduler_%s.sql", dbType.getDb());
        SqlScriptRunner sqlScriptRunner = new SqlScriptRunner(dataSource, sqlFilePath);
        try {
            sqlScriptRunner.execute();
            log.info("Success execute the sql initialize file: {}", sqlFilePath);
        } catch (Exception ex) {
            throw new RuntimeException("Execute initialize sql file: " + sqlFilePath + " error", ex);
        }
    }

    public String getCurrentVersion(String versionName) {
        String sql = String.format("select version from %s", versionName);
        String version = null;
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                version = rs.getString(1);
            }
            return version;
        } catch (SQLException e) {
            log.error("Get current version from database error, sql: {}", sql, e);
            throw new RuntimeException("Get current version from database error, sql: " + sql, e);
        }
    }

    public void upgradeDolphinScheduler(String schemaDir) {
        upgradeDolphinSchedulerDDL(schemaDir, "dolphinscheduler_ddl.sql");
        upgradeDolphinSchedulerDML(schemaDir, "dolphinscheduler_dml.sql");
    }

    /**
     * upgrade DolphinScheduler to 2.0.6
     */
    public void upgradeDolphinSchedulerResourceFileSize() {
        ResourceDao resourceDao = new ResourceDao();
        try (Connection conn = dataSource.getConnection()) {
            // update the size of the folder that is the type of file.
            resourceDao.updateResourceFolderSizeByFileType(conn, 0);
            // update the size of the folder that is the type of udf.
            resourceDao.updateResourceFolderSizeByFileType(conn, 1);
        } catch (Exception ex) {
            log.error("Failed to upgrade because of failing to update the folder's size of resource files.");
        }
    }

    private void upgradeDolphinSchedulerDML(String schemaDir, String scriptFile) {
        String schemaVersion = schemaDir.split("_")[0];
        String sqlFilePath =
                String.format("sql/upgrade/%s/%s/%s", schemaDir, dbType.getDb(), scriptFile);
        try {
            // Execute the upgraded dolphinscheduler dml
            SqlScriptRunner sqlScriptRunner = new SqlScriptRunner(dataSource, sqlFilePath);
            sqlScriptRunner.execute();
            try (Connection connection = dataSource.getConnection()) {
                String upgradeSQL;
                if (databaseDialect.tableExists(T_VERSION_NAME)) {
                    // Change version in the version table to the new version
                    upgradeSQL = String.format("update %s set version = ?", T_VERSION_NAME);
                } else if (databaseDialect.tableExists(T_NEW_VERSION_NAME)) {
                    // Change version in the version table to the new version
                    upgradeSQL = String.format("update %s set version = ?", T_NEW_VERSION_NAME);
                } else {
                    throw new RuntimeException("The version table does not exist");
                }
                try (PreparedStatement pstmt = connection.prepareStatement(upgradeSQL)) {
                    pstmt.setString(1, schemaVersion);
                    pstmt.executeUpdate();
                }
            }
            log.info("Success execute the dml file, schemaDir:  {}, ddlScript: {}", schemaDir, scriptFile);
        } catch (FileNotFoundException e) {
            log.error("Cannot find the DDL file, schemaDir:  {}, ddlScript: {}", schemaDir, scriptFile, e);
            throw new RuntimeException("sql file not found ", e);
        } catch (Exception e) {
            log.error("Execute ddl file failed, meet an unknown exception, schemaDir:  {}, ddlScript: {}", schemaDir,
                    scriptFile, e);
            throw new RuntimeException("Execute ddl file failed, meet an unknown exception", e);
        }
    }

    /**
     * upgradeDolphinScheduler DDL
     *
     * @param schemaDir schemaDir
     */
    public void upgradeDolphinSchedulerDDL(String schemaDir, String scriptFile) {
        String sqlFilePath =
                String.format("sql/upgrade/%s/%s/%s", schemaDir, dbType.getDb(), scriptFile);
        SqlScriptRunner sqlScriptRunner = new SqlScriptRunner(dataSource, sqlFilePath);
        try {
            // Execute the dolphinscheduler ddl.sql for the upgrade
            sqlScriptRunner.execute();
            log.info("Success execute the ddl file, schemaDir:  {}, ddlScript: {}", schemaDir, scriptFile);
        } catch (FileNotFoundException e) {
            log.error("Cannot find the DDL file, schemaDir:  {}, ddlScript: {}", schemaDir, scriptFile, e);
            throw new RuntimeException("sql file not found ", e);
        } catch (Exception e) {
            log.error("Execute ddl file failed, meet an unknown exception, schemaDir:  {}, ddlScript: {}", schemaDir,
                    scriptFile, e);
            throw new RuntimeException("Execute ddl file failed, meet an unknown exception", e);
        }
    }

    /**
     * update version
     *
     * @param version version
     */
    public void updateVersion(String version) {
        // Change version in the version table to the new version
        String versionName = T_VERSION_NAME;
        if (!SchemaUtils.isAGreatVersion("1.2.0", version)) {
            versionName = "t_ds_version";
        }
        String upgradeSQL = String.format("update %s set version = ?", versionName);
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(upgradeSQL)) {
            pstmt.setString(1, version);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("Update version error, sql: {}", upgradeSQL, e);
            throw new RuntimeException("Upgrade version error, sql: " + upgradeSQL, e);
        }
    }
}
