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

import cn.escheduler.common.utils.ConnectionUtils;
import cn.escheduler.dao.datasource.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresqlUpgradeDao extends UpgradeDao {

    public static final Logger logger = LoggerFactory.getLogger(UpgradeDao.class);
    private static final String schema = getSchema();

    @Override
    protected void init() {

    }

    private static class PostgresqlUpgradeDaoHolder {
        private static final PostgresqlUpgradeDao INSTANCE = new PostgresqlUpgradeDao();
    }

    private PostgresqlUpgradeDao() {
    }

    public static final PostgresqlUpgradeDao getInstance() {
        return PostgresqlUpgradeDaoHolder.INSTANCE;
    }


    @Override
    public void initSchema(String initSqlPath) {
        super.initSchema(initSqlPath);
    }

    public static String getSchema(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            pstmt = conn.prepareStatement("select current_schema()");
            resultSet = pstmt.executeQuery();
            while (resultSet.next()){
                if(resultSet.isFirst()){
                    return resultSet.getString(1);
                }
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(resultSet, pstmt, conn);
        }
        return "";
    }

    /**
     * Determines whether a table exists
     * @param tableName
     * @return
     */
    public boolean isExistsTable(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();

            rs = conn.getMetaData().getTables(null, schema, tableName, null);
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(rs, null, conn);
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
        ResultSet rs = null;
        try {
            conn = ConnectionFactory.getDataSource().getConnection();
            rs = conn.getMetaData().getColumns(null,schema,tableName,columnName);
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(rs, null, conn);

        }

    }

}
