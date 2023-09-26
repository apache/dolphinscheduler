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

package org.apache.dolphinscheduler.common.sql;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClasspathSqlScriptParserTest {

    @Test
    void testMysqlDmlSql() throws IOException {
        ClasspathSqlScriptParser classpathSqlScriptParser = new ClasspathSqlScriptParser("sql/mysql_dml.sql");
        List<String> allSql = classpathSqlScriptParser.getAllSql();
        Assertions.assertEquals("drop PROCEDURE if EXISTS dolphin_t_ds_tenant_insert_default;", allSql.get(0));
        Assertions.assertEquals("CREATE PROCEDURE dolphin_t_ds_tenant_insert_default()\n" +
                "BEGIN\n" +
                "    IF\n" +
                "NOT EXISTS(SELECT 1\n" +
                "                   FROM t_ds_tenant\n" +
                "                   WHERE id = -1)\n" +
                "    THEN\n" +
                "        INSERT INTO `t_ds_tenant` VALUES ('-1', 'default', 'default tenant', '1', current_timestamp, current_timestamp);\n"
                +
                "END IF;\n" +
                "END;", String.join("", allSql.get(1)));
    }

    @Test
    void testMysqlDdlSql() throws IOException {
        ClasspathSqlScriptParser classpathSqlScriptParser = new ClasspathSqlScriptParser("sql/mysql_ddl.sql");
        List<String> allSql = classpathSqlScriptParser.getAllSql();
        Assertions.assertEquals("ALTER TABLE t_ds_process_definition DROP tenant_id;", allSql.get(0));
    }
}
