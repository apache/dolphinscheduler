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
package org.apache.dolphinscheduler.common.utils;


import org.apache.dolphinscheduler.common.enums.DbType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * SqlUtils Tester.
 * 
 * @since
 * 
 *        <pre>
 * 五月 29, 2020
 *        </pre>
 * 
 * @version 1.0
 */
public class SqlUtilsTest {

    @Before
    public void before()
        throws Exception {}

    @After
    public void after()
        throws Exception {}

    /**
     * Method: resolveSqlSelectTables(DbType dbType, String sql)
     */
    @Test
    public void testResolveSqlSelectTables()
        throws Exception {
        List<String> tableList = SqlUtils.resolveSqlSelectTables(DbType.MYSQL, "select a from t1");
        Assert.assertNotNull(tableList);
        Assert.assertTrue(tableList.size() == 1);
        Assert.assertEquals("t1", tableList.get(0));
    }

    /**
     * Method: resolveSqlInsertTables(DbType dbType, String sql)
     */
    @Test
    public void testResolveSqlInsertTables()
        throws Exception {
        List<String> tableList = SqlUtils.resolveSqlInsertTables(DbType.MYSQL, "insert into t1(a) values (1)");
        Assert.assertNotNull(tableList);
        Assert.assertTrue(tableList.size() == 1);
        Assert.assertEquals("t1", tableList.get(0));
    }

    /**
     * Method: resolveSqlUpdateTables(DbType dbType, String sql)
     */
    @Test
    public void testResolveSqlUpdateTables()
        throws Exception {
        List<String> tableList = SqlUtils.resolveSqlUpdateTables(DbType.MYSQL, "update t1 set a = 1");
        Assert.assertNotNull(tableList);
        Assert.assertTrue(tableList.size() == 1);
        Assert.assertEquals("t1", tableList.get(0));
    }

    /**
     * Method: resolveSqlDeleteTables(DbType dbType, String sql)
     */
    @Test
    public void testResolveSqlDeleteTables()
        throws Exception {
        List<String> tableList = SqlUtils.resolveSqlDeleteTables(DbType.MYSQL, "delete from t1 where a = 1");
        Assert.assertNotNull(tableList);
        Assert.assertTrue(tableList.size() == 1);
        Assert.assertEquals("t1", tableList.get(0));
    }

    /**
     * Method: convertKeywordsColumns(DbType dbType, String[] columns)
     */
    @Test
    public void testConvertKeywordsColumns()
        throws Exception {
        String[] fromColumns = new String[]{"`select`", "from", "\"where\"", " table "};
        String[] targetColumns = new String[]{"`select`", "`from`", "`where`", "`table`"};

        String[] toColumns = SqlUtils.convertKeywordsColumns(DbType.MYSQL, fromColumns);

        assertTrue(fromColumns.length == toColumns.length);

        for (int i = 0; i < toColumns.length; i++) {
            assertEquals(targetColumns[i], toColumns[i]);
        }
    }

    /**
     * Method: doConvertKeywordsColumn(DbType dbType, String column)
     */
    @Test
    public void testDoConvertKeywordsColumn()
        throws Exception {
        assertEquals("`select`", SqlUtils.doConvertKeywordsColumn(DbType.MYSQL, " \"`select`\" "));
        assertEquals("\"select\"", SqlUtils.doConvertKeywordsColumn(DbType.POSTGRESQL, " \"`select`\" "));
        assertEquals("`select`", SqlUtils.doConvertKeywordsColumn(DbType.SQLSERVER, " \"`select`\" "));
        assertEquals("\"select\"", SqlUtils.doConvertKeywordsColumn(DbType.ORACLE, " \"`select`\" "));
        assertEquals("select", SqlUtils.doConvertKeywordsColumn(DbType.DB2, " \"`select`\" "));
    }

}
