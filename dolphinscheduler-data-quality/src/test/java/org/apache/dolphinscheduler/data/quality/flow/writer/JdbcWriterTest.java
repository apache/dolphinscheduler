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

package org.apache.dolphinscheduler.data.quality.flow.writer;

import org.apache.dolphinscheduler.data.quality.configuration.WriterParameter;
import org.apache.dolphinscheduler.data.quality.flow.FlowTestBase;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;

import java.util.Map;

import static org.apache.dolphinscheduler.data.quality.Constants.*;

/**
 * JdbcWriterTest
 */
public class JdbcWriterTest extends FlowTestBase {

    @Before
    public void before() {
        super.before();
        createWriterTable();
    }

    @Test
    public void testJdbcWriterExecute() {
        JdbcWriter jdbcWriter = new JdbcWriter(sparkSession,buildWriterParameter());
        jdbcWriter.execute();
    }

    private WriterParameter buildWriterParameter() {
        WriterParameter writerParameter = new WriterParameter();
        writerParameter.setType("JDBC");
        Map<String,Object> config = new HashMap<>();
        config.put(DATABASE,"test");
        config.put(TABLE,"test2");
        config.put(URL,url);
        config.put(USER,"test");
        config.put(PASSWORD,"123456");
        config.put(DRIVER,driver);
        config.put("sql","SELECT '1' as company,'1' as date,'2' as c1,'2' as c2,'2' as c3, 2 as c4");
        writerParameter.setConfig(config);
        return writerParameter;
    }

    private void createWriterTable(){
        try{
            Connection connection = getConnection();
            connection.prepareStatement("create schema if not exists test").executeUpdate();

            connection.prepareStatement("drop table if exists test.test2").executeUpdate();
            connection
                    .prepareStatement(
                            "CREATE TABLE test.test2 (\n" +
                                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                                    "  `company` varchar(255) DEFAULT NULL,\n" +
                                    "  `date` varchar(255) DEFAULT NULL,\n" +
                                    "  `c1` varchar(255) DEFAULT NULL,\n" +
                                    "  `c2` varchar(255) DEFAULT NULL,\n" +
                                    "  `c3` varchar(255) DEFAULT NULL,\n" +
                                    "  `c4` int(11) DEFAULT NULL,\n" +
                                    "  PRIMARY KEY (`id`)\n" +
                                    ")")
                    .executeUpdate();
            connection.prepareStatement("set schema test").executeUpdate();
            connection.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
