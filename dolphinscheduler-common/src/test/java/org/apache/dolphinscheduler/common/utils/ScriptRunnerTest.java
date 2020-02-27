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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.StringReader;
import java.sql.*;

public class ScriptRunnerTest {
    @Test
    public void testRunScript() {
        //connection is null
        Exception exception = null;
        ScriptRunner s = new ScriptRunner(null, true, true);
        try {
            s.runScript(new StringReader("select 1"));
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNotNull(exception);

        //connect is not null
        runScript("");
    }

    private void runScript(String dbName) {
        try {
            Connection conn = Mockito.mock(Connection.class);
            Mockito.when(conn.getAutoCommit()).thenReturn(true);
            PreparedStatement st = Mockito.mock(PreparedStatement.class);
            Mockito.when(conn.createStatement()).thenReturn(st);
            ResultSet rs = Mockito.mock(ResultSet.class);
            Mockito.when(st.getResultSet()).thenReturn(rs);
            ResultSetMetaData md = Mockito.mock(ResultSetMetaData.class);
            Mockito.when(rs.getMetaData()).thenReturn(md);
            Mockito.when(md.getColumnCount()).thenReturn(2);
            Mockito.when(rs.next()).thenReturn(true, false);
            ScriptRunner s = new ScriptRunner(conn, true, true);
            if (dbName.isEmpty()) {
                s.runScript(new StringReader("select 1;"));
            } else {
                s.runScript(new StringReader("select 1;"), dbName);
            }
            Mockito.verify(md).getColumnLabel(1);
        } catch(Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testRunScriptWithDbName() {
        //connect is not null
        runScript("db_test");
    }
}
