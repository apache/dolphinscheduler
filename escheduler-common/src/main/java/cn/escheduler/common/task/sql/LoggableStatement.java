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
package cn.escheduler.common.task.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * added logging capability.
 * provides a method {@link #getQueryString} which can be used to get the query sql
 */
public class LoggableStatement implements PreparedStatement {
    /**
     * used for storing parameter values needed for producing log
     */
    private ArrayList parameterValues;

    /**
     * the query string with question marks as parameter placeholders
     */
    private String sqlTemplate;

    /**
     * a statement created from a real database connection
     */
    private PreparedStatement wrappedStatement;

    public LoggableStatement(Connection connection, String sql)
            throws SQLException {
        // use connection to make a prepared statement
        wrappedStatement = connection.prepareStatement(sql);
        sqlTemplate = sql;
        parameterValues = new ArrayList();
    }

    private void saveQueryParamValue(int position, Object obj) {
        String strValue;
        if (obj instanceof String || obj instanceof Date) {
            // if we have a String, include '' in the saved value
            strValue = "'" + obj + "'";
        } else {
            if (obj == null) {
                // convert null to the string null
                strValue = "null";
            } else {
                // unknown object (includes all Numbers), just call toString
                strValue = obj.toString();
            }
        }
        // if we are setting a position larger than current size of
        // parameterValues, first make it larger
        while (position >= parameterValues.size()) {

            parameterValues.add(null);
        }
        // save the parameter
        parameterValues.set(position, strValue);
    }

    // print the complete SQL statement
    public String getQueryString() {
        int len = sqlTemplate.length();
        StringBuffer t = new StringBuffer(len * 2);

        if (parameterValues != null) {
            int i = 1, limit = 0, base = 0;

            while ((limit = sqlTemplate.indexOf('?', limit)) != -1) {
                t.append(sqlTemplate.substring(base, limit));
                t.append(parameterValues.get(i));
                i++;
                limit++;
                base = limit;
            }
            if (base < len) {
                t.append(sqlTemplate.substring(base));
            }
        }
        return t.toString();
    }

    public void addBatch() throws SQLException {
        wrappedStatement.addBatch();
    }

    public void clearParameters() throws SQLException {
        wrappedStatement.clearParameters();
    }

    public boolean execute() throws SQLException {
        return wrappedStatement.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        return wrappedStatement.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return wrappedStatement.executeUpdate();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return wrappedStatement.getMetaData();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return wrappedStatement.getParameterMetaData();
    }

    public void setArray(int i, Array x) throws SQLException {
        wrappedStatement.setArray(i, x);
        saveQueryParamValue(i, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        wrappedStatement.setAsciiStream(parameterIndex, x, length);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws SQLException {
        wrappedStatement.setBigDecimal(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        wrappedStatement.setBinaryStream(parameterIndex, x, length);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        wrappedStatement.setBlob(i, x);
        saveQueryParamValue(i, x);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        wrappedStatement.setBoolean(parameterIndex, x);
        saveQueryParamValue(parameterIndex, new Boolean(x));
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        wrappedStatement.setByte(parameterIndex, x);
        saveQueryParamValue(parameterIndex, new Byte(x));
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        wrappedStatement.setBytes(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        wrappedStatement.setCharacterStream(parameterIndex, reader, length);
        saveQueryParamValue(parameterIndex, reader);
    }

    public void setClob(int i, Clob x) throws SQLException {
        wrappedStatement.setClob(i, x);
        saveQueryParamValue(i, x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        wrappedStatement.setDate(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
        wrappedStatement.setDate(parameterIndex, x, cal);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        wrappedStatement.setDouble(parameterIndex, x);
        saveQueryParamValue(parameterIndex, new Double(x));
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        wrappedStatement.setFloat(parameterIndex, x);
        saveQueryParamValue(parameterIndex, new Float(x));
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        wrappedStatement.setInt(parameterIndex, x);
        saveQueryParamValue(parameterIndex, new Integer(x));
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        wrappedStatement.setLong(parameterIndex, x);
        saveQueryParamValue(parameterIndex, new Long(x));
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        wrappedStatement.setNull(parameterIndex, sqlType);
        saveQueryParamValue(parameterIndex, new Integer(sqlType));
    }

    public void setNull(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        wrappedStatement.setNull(paramIndex, sqlType, typeName);
        saveQueryParamValue(paramIndex, new Integer(sqlType));
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        wrappedStatement.setObject(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        wrappedStatement.setObject(parameterIndex, x, targetSqlType);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType,
                          int scale) throws SQLException {
        wrappedStatement.setObject(parameterIndex, x, targetSqlType, scale);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setRef(int i, Ref x) throws SQLException {
        wrappedStatement.setRef(i, x);
        saveQueryParamValue(i, x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        wrappedStatement.setShort(parameterIndex, x);
        saveQueryParamValue(parameterIndex, new Short(x));
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        wrappedStatement.setString(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        wrappedStatement.setTime(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        wrappedStatement.setTime(parameterIndex, x, cal);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        wrappedStatement.setTimestamp(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        wrappedStatement.setTimestamp(parameterIndex, x, cal);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        wrappedStatement.setURL(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        wrappedStatement.setUnicodeStream(parameterIndex, x, length);
        saveQueryParamValue(parameterIndex, x);
    }

    public void addBatch(String sql) throws SQLException {
        wrappedStatement.addBatch(sql);
    }

    public void cancel() throws SQLException {
        wrappedStatement.cancel();
    }

    public void clearBatch() throws SQLException {
        wrappedStatement.clearBatch();
    }

    public void clearWarnings() throws SQLException {
        wrappedStatement.clearWarnings();
    }

    public void close() throws SQLException {
        wrappedStatement.close();
    }

    public boolean execute(String sql) throws SQLException {
        return wrappedStatement.execute(sql);
    }

    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException {
        return wrappedStatement.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return wrappedStatement.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames)
            throws SQLException {
        return wrappedStatement.execute(sql, columnNames);
    }

    public int[] executeBatch() throws SQLException {
        return wrappedStatement.executeBatch();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return wrappedStatement.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        return wrappedStatement.executeUpdate(sql);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException {
        return wrappedStatement.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes)
            throws SQLException {
        return wrappedStatement.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames)
            throws SQLException {
        return wrappedStatement.executeUpdate(sql, columnNames);
    }

    public Connection getConnection() throws SQLException {
        return wrappedStatement.getConnection();
    }

    public int getFetchDirection() throws SQLException {
        return wrappedStatement.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return wrappedStatement.getFetchSize();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return wrappedStatement.getGeneratedKeys();
    }

    public int getMaxFieldSize() throws SQLException {
        return wrappedStatement.getMaxFieldSize();
    }

    public int getMaxRows() throws SQLException {
        return wrappedStatement.getMaxRows();
    }

    public boolean getMoreResults() throws SQLException {
        return wrappedStatement.getMoreResults();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return wrappedStatement.getMoreResults(current);
    }

    public int getQueryTimeout() throws SQLException {
        return wrappedStatement.getQueryTimeout();
    }

    public ResultSet getResultSet() throws SQLException {
        return wrappedStatement.getResultSet();
    }

    public int getResultSetConcurrency() throws SQLException {
        return wrappedStatement.getResultSetConcurrency();
    }

    public int getResultSetHoldability() throws SQLException {
        return wrappedStatement.getResultSetHoldability();
    }

    public int getResultSetType() throws SQLException {
        return wrappedStatement.getResultSetType();
    }

    public int getUpdateCount() throws SQLException {
        return wrappedStatement.getUpdateCount();
    }

    public SQLWarning getWarnings() throws SQLException {
        return wrappedStatement.getWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        wrappedStatement.setCursorName(name);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        wrappedStatement.setEscapeProcessing(enable);
    }

    public void setFetchDirection(int direction) throws SQLException {
        wrappedStatement.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        wrappedStatement.setFetchSize(rows);
    }

    public void setMaxFieldSize(int max) throws SQLException {
        wrappedStatement.setMaxFieldSize(max);
    }

    public void setMaxRows(int max) throws SQLException {
        wrappedStatement.setMaxFieldSize(max);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        wrappedStatement.setQueryTimeout(seconds);
    }

    public void setAsciiStream(int parameterIndex, InputStream x)
            throws SQLException {

    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length)
            throws SQLException {

    }

    public void setBinaryStream(int parameterIndex, InputStream x)
            throws SQLException {

    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length)
            throws SQLException {

    }

    public void setBlob(int parameterIndex, InputStream inputStream)
            throws SQLException {

    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {

    }

    public void setCharacterStream(int parameterIndex, Reader reader)
            throws SQLException {

    }

    public void setCharacterStream(int parameterIndex, Reader reader,
                                   long length) throws SQLException {

    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {

    }

    public void setClob(int parameterIndex, Reader reader, long length)
            throws SQLException {

    }

    public void setNCharacterStream(int parameterIndex, Reader value)
            throws SQLException {

    }

    public void setNCharacterStream(int parameterIndex, Reader value,
                                    long length) throws SQLException {

    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {

    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {

    }

    public void setNClob(int parameterIndex, Reader reader, long length)
            throws SQLException {

    }

    public void setNString(int parameterIndex, String value)
            throws SQLException {

    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {

    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject)
            throws SQLException {

    }

    public boolean isClosed() throws SQLException {
        return false;
    }

    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    public void setPoolable(boolean poolable) throws SQLException {

    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }
}
