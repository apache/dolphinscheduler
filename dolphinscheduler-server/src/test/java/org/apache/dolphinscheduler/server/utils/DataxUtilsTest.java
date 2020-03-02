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
package org.apache.dolphinscheduler.server.utils;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * DataxUtils Tester.
 */
public class DataxUtilsTest {

    /**
     *
     * Method: getReaderPluginName(DbType dbType)
     *
     */
    @Test
    public void testGetReaderPluginName() {
        assertEquals(DataxUtils.DATAX_READER_PLUGIN_MYSQL, DataxUtils.getReaderPluginName(DbType.MYSQL));
        assertEquals(DataxUtils.DATAX_READER_PLUGIN_POSTGRESQL, DataxUtils.getReaderPluginName(DbType.POSTGRESQL));
        assertEquals(DataxUtils.DATAX_READER_PLUGIN_SQLSERVER, DataxUtils.getReaderPluginName(DbType.SQLSERVER));
        assertEquals(DataxUtils.DATAX_READER_PLUGIN_ORACLE, DataxUtils.getReaderPluginName(DbType.ORACLE));
        assertTrue(DataxUtils.getReaderPluginName(DbType.DB2) == null);
    }

    /**
     *
     * Method: getWriterPluginName(DbType dbType)
     *
     */
    @Test
    public void testGetWriterPluginName() {
        assertEquals(DataxUtils.DATAX_WRITER_PLUGIN_MYSQL, DataxUtils.getWriterPluginName(DbType.MYSQL));
        assertEquals(DataxUtils.DATAX_WRITER_PLUGIN_POSTGRESQL, DataxUtils.getWriterPluginName(DbType.POSTGRESQL));
        assertEquals(DataxUtils.DATAX_WRITER_PLUGIN_SQLSERVER, DataxUtils.getWriterPluginName(DbType.SQLSERVER));
        assertEquals(DataxUtils.DATAX_WRITER_PLUGIN_ORACLE, DataxUtils.getWriterPluginName(DbType.ORACLE));
        assertTrue(DataxUtils.getWriterPluginName(DbType.DB2) == null);
    }

    /**
     *
     * Method: getSqlStatementParser(DbType dbType, String sql)
     *
     */
    @Test
    public void testGetSqlStatementParser() throws Exception {
        assertTrue(DataxUtils.getSqlStatementParser(DbType.MYSQL, "select 1") instanceof MySqlStatementParser);
        assertTrue(DataxUtils.getSqlStatementParser(DbType.POSTGRESQL, "select 1") instanceof PGSQLStatementParser);
        assertTrue(DataxUtils.getSqlStatementParser(DbType.ORACLE, "select 1") instanceof OracleStatementParser);
        assertTrue(DataxUtils.getSqlStatementParser(DbType.SQLSERVER, "select 1") instanceof SQLServerStatementParser);
        assertTrue(DataxUtils.getSqlStatementParser(DbType.DB2, "select 1") == null);
    }

    /**
     *
     * Method: convertKeywordsColumns(DbType dbType, String[] columns)
     *
     */
    @Test
    public void testConvertKeywordsColumns() throws Exception {
        String[] fromColumns = new String[]{"`select`", "from", "\"where\"", " table "};
        String[] targetColumns = new String[]{"`select`", "`from`", "`where`", "`table`"};

        String[] toColumns = DataxUtils.convertKeywordsColumns(DbType.MYSQL, fromColumns);

        assertTrue(fromColumns.length == toColumns.length);

        for (int i = 0; i < toColumns.length; i++) {
            assertEquals(targetColumns[i], toColumns[i]);
        }
    }

    /**
     *
     * Method: doConvertKeywordsColumn(DbType dbType, String column)
     *
     */
    @Test
    public void testDoConvertKeywordsColumn() throws Exception {
        assertEquals("`select`", DataxUtils.doConvertKeywordsColumn(DbType.MYSQL, " \"`select`\" "));
        assertEquals("\"select\"", DataxUtils.doConvertKeywordsColumn(DbType.POSTGRESQL, " \"`select`\" "));
        assertEquals("`select`", DataxUtils.doConvertKeywordsColumn(DbType.SQLSERVER, " \"`select`\" "));
        assertEquals("\"select\"", DataxUtils.doConvertKeywordsColumn(DbType.ORACLE, " \"`select`\" "));
        assertEquals("select", DataxUtils.doConvertKeywordsColumn(DbType.DB2, " \"`select`\" "));
    }
}
