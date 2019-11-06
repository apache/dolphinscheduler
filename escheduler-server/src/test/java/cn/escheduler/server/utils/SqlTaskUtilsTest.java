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
package cn.escheduler.server.utils;

import cn.escheduler.common.enums.DataType;
import cn.escheduler.common.enums.Direct;
import cn.escheduler.common.process.Property;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SqlTaskUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(SqlTaskUtilsTest.class);
    private static final String sql = "select REGEXP_EXTRACT(\\\"uid\\\\\\\":\\\\\\\"123\\\",'uid\\\":\\\"?([^,\\\"|}]+)',1) as uid from course where dt='${day}' and hour=${hour}";
    private static final String sql1 = "select REGEXP_EXTRACT(\\\"uid\\\\\\\":\\\\\\\"123\\\",'uid\\\":\\\"?([^,\\\"|}]+)',1) as uid from course where cno>${cno_number}";

    @Test
    public void getFormatSql(){

        String formatSql = SqlTaskUtils.getFormatSql(sql);
        logger.info("formatSql:"+formatSql);

    }

    @Test
    public void printReplacedSql(){
        Map<Integer,Property> sqlParamsMap = new HashMap<Integer,Property>(5);
        sqlParamsMap.put(1,new Property("cno_number", Direct.IN, DataType.INTEGER,"2"));
        SqlTaskUtils.printReplacedSql(sql,SqlTaskUtils.getFormatSql(sql),sqlParamsMap);
    }



}