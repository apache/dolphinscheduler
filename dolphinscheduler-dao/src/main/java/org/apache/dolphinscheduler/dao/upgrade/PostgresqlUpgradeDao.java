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
package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.common.utils.ConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * postgresql upgrade dao
 */
public class PostgresqlUpgradeDao extends UpgradeDao {

    public static final Logger logger = LoggerFactory.getLogger(PostgresqlUpgradeDao.class);
    private static final String SCHEMA = getSchema();

    /**
     * postgresql upgrade dao holder
     */
    private static class PostgresqlUpgradeDaoHolder {
        private static final PostgresqlUpgradeDao INSTANCE = new PostgresqlUpgradeDao();
    }

    /**
     * PostgresqlUpgradeDao Constructor
     */
    private PostgresqlUpgradeDao() {
    }

    public static final PostgresqlUpgradeDao getInstance() {
        return PostgresqlUpgradeDaoHolder.INSTANCE;
    }

    /**
     * getSchema
     * @return schema
     */
    public static String getSchema(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            conn = dataSource.getConnection();
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
     * determines whether a table exists
     * @param tableName tableName
     * @return if table exist return true，else return false
     */
    @Override
    public boolean isExistsTable(String tableName) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            rs = conn.getMetaData().getTables(null, SCHEMA, tableName, null);

            return rs.next();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(rs, conn);
        }

    }

    /**
     * determines whether a field exists in the specified table
     * @param tableName tableName
     * @param columnName columnName
     * @return  if column name exist return true，else return false
     */
    @Override
    public boolean isExistsColumn(String tableName,String columnName) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            rs = conn.getMetaData().getColumns(null, SCHEMA,tableName,columnName);
            return rs.next();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            ConnectionUtils.releaseResource(rs, conn);

        }

    }

}
