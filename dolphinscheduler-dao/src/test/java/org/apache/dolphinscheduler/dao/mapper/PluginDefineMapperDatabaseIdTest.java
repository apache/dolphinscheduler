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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PluginDefineMapperDatabaseIdTest {

    private static final String MAPPER_RESOURCE =
            "org/apache/dolphinscheduler/dao/mapper/PluginDefineMapper.xml";

    private static final String CHECK_TABLE_EXIST_STATEMENT =
            "org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper.checkTableExist";

    @Test
    void testCheckTableExistUsesMysqlSqlWhenDatabaseIdIsMysql() throws Exception {
        String sql = getCheckTableExistSql("mysql");

        Assertions.assertTrue(sql.contains("information_schema.TABLES"));
        Assertions.assertTrue(sql.contains("table_schema = database()"));
    }

    @Test
    void testCheckTableExistUsesPostgresqlSqlWhenDatabaseIdIsPostgresql() throws Exception {
        String sql = getCheckTableExistSql("postgresql");

        Assertions.assertTrue(sql.contains("table_schema = current_schema()"));
        Assertions.assertTrue(sql.contains("information_schema.tables"));
    }

    @Test
    void testCheckTableExistUsesDefaultSqlWhenDatabaseIdIsNull() throws Exception {
        String sql = getCheckTableExistSql(null);

        Assertions.assertFalse(sql.contains("table_schema = database()"));
        Assertions.assertFalse(sql.contains("table_schema = current_schema()"));
        Assertions.assertTrue(sql.contains("information_schema.TABLES"));
    }

    private String getCheckTableExistSql(String databaseId) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setDatabaseId(databaseId);

        try (InputStream inputStream = Resources.getResourceAsStream(MAPPER_RESOURCE)) {
            XMLMapperBuilder mapperBuilder =
                    new XMLMapperBuilder(inputStream, configuration, MAPPER_RESOURCE, configuration.getSqlFragments());
            mapperBuilder.parse();
        }

        MappedStatement mappedStatement = configuration.getMappedStatement(CHECK_TABLE_EXIST_STATEMENT);
        return mappedStatement.getBoundSql(null).getSql();
    }
}
