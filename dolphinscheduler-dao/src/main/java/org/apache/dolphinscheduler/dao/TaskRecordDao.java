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
package org.apache.dolphinscheduler.dao;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskRecordStatus;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.ConnectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.TaskRecord;
import org.apache.dolphinscheduler.dao.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * task record dao
 */
public class TaskRecordDao {


    private static Logger logger = LoggerFactory.getLogger(TaskRecordDao.class.getName());

    /**
     *  get task record flag
     * @return whether startup taskrecord
     */
    public static boolean getTaskRecordFlag(){
       return PropertyUtils.getBoolean(Constants.TASK_RECORD_FLAG,false);
    }

    /**
     * create connection
     *
     * @return connection
     */
    private static Connection getConn() {
        if (!getTaskRecordFlag()) {
            return null;
        }
        String driver = "com.mysql.jdbc.Driver";
        String url = PropertyUtils.getString(Constants.TASK_RECORD_URL);
        String username = PropertyUtils.getString(Constants.TASK_RECORD_USER);
        String password = PropertyUtils.getString(Constants.TASK_RECORD_PWD);
        Connection conn = null;
        try {
            //classLoader，load driver
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found Exception ", e);
        } catch (SQLException e) {
            logger.error("SQL Exception ", e);
        }
        return conn;
    }

    /**
     * generate where sql string
     *
     * @param filterMap filterMap
     * @return sql string
     */
    private static String getWhereString(Map<String, String> filterMap) {
        if (filterMap.size() == 0) {
            return "";
        }

        String result = " where 1=1 ";

        Object taskName = filterMap.get("taskName");
        if (taskName != null && StringUtils.isNotEmpty(taskName.toString())) {
            result += " and PROC_NAME like concat('%', '" + taskName.toString() + "', '%') ";
        }

        Object taskDate = filterMap.get("taskDate");
        if (taskDate != null && StringUtils.isNotEmpty(taskDate.toString())) {
            result += " and PROC_DATE='" + taskDate.toString() + "'";
        }

        Object state = filterMap.get("state");
        if (state != null && StringUtils.isNotEmpty(state.toString())) {
            result += " and NOTE='" + state.toString() + "'";
        }

        Object sourceTable = filterMap.get("sourceTable");
        if (sourceTable != null && StringUtils.isNotEmpty(sourceTable.toString())) {
            result += " and SOURCE_TAB like concat('%', '" + sourceTable.toString() + "', '%')";
        }

        Object targetTable = filterMap.get("targetTable");
        if (sourceTable != null && StringUtils.isNotEmpty(targetTable.toString())) {
            result += " and TARGET_TAB like concat('%', '" + targetTable.toString() + "', '%') ";
        }

        Object start = filterMap.get("startTime");
        if (start != null && StringUtils.isNotEmpty(start.toString())) {
            result += " and STARTDATE>='" + start.toString() + "'";
        }

        Object end = filterMap.get("endTime");
        if (end != null && StringUtils.isNotEmpty(end.toString())) {
            result += " and ENDDATE>='" + end.toString() + "'";
        }
        return result;
    }

    /**
     * count task record
     *
     * @param filterMap filterMap
     * @param table     table
     * @return task record count
     */
    public static int countTaskRecord(Map<String, String> filterMap, String table) {

        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConn();
            if (conn == null) {
                return count;
            }
            String sql = String.format("select count(1) as count from %s", table);
            sql += getWhereString(filterMap);
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()){
                count = rs.getInt("count");
                break;
            }
        } catch (SQLException e) {
            logger.error("Exception ", e);
        }finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }
        return count;
    }

    /**
     * query task record by filter map paging
     *
     * @param filterMap filterMap
     * @param table     table
     * @return task record list
     */
    public static List<TaskRecord> queryAllTaskRecord(Map<String, String> filterMap, String table) {

        String sql = String.format("select * from  %s", table);
        sql += getWhereString(filterMap);

        int offset = Integer.parseInt(filterMap.get("offset"));
        int pageSize = Integer.parseInt(filterMap.get("pageSize"));
        sql += String.format(" order by STARTDATE desc limit %d,%d", offset, pageSize);

        List<TaskRecord> recordList = new ArrayList<>();
        try {
            recordList = getQueryResult(sql);
        } catch (Exception e) {
            logger.error("Exception ", e);
        }
        return recordList;
    }

    /**
     * convert result set to task record
     *
     * @param resultSet resultSet
     * @return task record
     * @throws SQLException if error throws SQLException
     */
    private static TaskRecord convertToTaskRecord(ResultSet resultSet) throws SQLException {

        TaskRecord taskRecord = new TaskRecord();
        taskRecord.setId(resultSet.getInt("ID"));
        taskRecord.setProcId(resultSet.getInt("PROC_ID"));
        taskRecord.setProcName(resultSet.getString("PROC_NAME"));
        taskRecord.setProcDate(resultSet.getString("PROC_DATE"));
        taskRecord.setStartTime(DateUtils.stringToDate(resultSet.getString("STARTDATE")));
        taskRecord.setEndTime(DateUtils.stringToDate(resultSet.getString("ENDDATE")));
        taskRecord.setResult(resultSet.getString("RESULT"));
        taskRecord.setDuration(resultSet.getInt("DURATION"));
        taskRecord.setNote(resultSet.getString("NOTE"));
        taskRecord.setSchema(resultSet.getString("SCHEMA"));
        taskRecord.setJobId(resultSet.getString("JOB_ID"));
        taskRecord.setSourceTab(resultSet.getString("SOURCE_TAB"));
        taskRecord.setSourceRowCount(resultSet.getLong("SOURCE_ROW_COUNT"));
        taskRecord.setTargetTab(resultSet.getString("TARGET_TAB"));
        taskRecord.setTargetRowCount(resultSet.getLong("TARGET_ROW_COUNT"));
        taskRecord.setErrorCode(resultSet.getString("ERROR_CODE"));
        return taskRecord;
    }

    /**
     * query task list by select sql
     *
     * @param selectSql select sql
     * @return task record list
     */
    private static List<TaskRecord> getQueryResult(String selectSql) {
        List<TaskRecord> recordList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConn();
            if (conn == null) {
                return recordList;
            }
            pstmt = conn.prepareStatement(selectSql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TaskRecord taskRecord = convertToTaskRecord(rs);
                recordList.add(taskRecord);
            }
        } catch (SQLException e) {
            logger.error("Exception ", e);
        }finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }
        return recordList;
    }

    /**
     * according to procname and procdate query task record
     *
     * @param procName procName
     * @param procDate procDate
     * @return task record status
     */
    public static TaskRecordStatus getTaskRecordState(String procName, String procDate) {
        String sql = String.format("SELECT * FROM eamp_hive_log_hd WHERE PROC_NAME='%s' and PROC_DATE like '%s'"
                , procName, procDate + "%");
        List<TaskRecord> taskRecordList = getQueryResult(sql);

        // contains no record and sql exception
        if (CollectionUtils.isEmpty(taskRecordList)) {
            // exception
            return TaskRecordStatus.EXCEPTION;
        } else if (taskRecordList.size() > 1) {
            return TaskRecordStatus.EXCEPTION;
        } else {
            TaskRecord taskRecord = taskRecordList.get(0);
            if (taskRecord == null) {
                return TaskRecordStatus.EXCEPTION;
            }
            Long targetRowCount = taskRecord.getTargetRowCount();
            if (targetRowCount <= 0) {
                return TaskRecordStatus.FAILURE;
            } else {
                return TaskRecordStatus.SUCCESS;
            }

        }
    }
}
