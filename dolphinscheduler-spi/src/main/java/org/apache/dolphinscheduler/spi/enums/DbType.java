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

package org.apache.dolphinscheduler.spi.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.google.common.base.Functions;

public enum DbType {
    MYSQL(0, "mysql"),
    POSTGRESQL(1, "postgresql"),
    HIVE(2, "hive"),
    SPARK(3, "spark"),
    CLICKHOUSE(4, "clickhouse"),
    ORACLE(5, "oracle"),
    SQLSERVER(6, "sqlserver"),
    DB2(7, "db2"),
    PRESTO(8, "presto"),
    H2(9, "h2"),
    REDSHIFT(10,"redshift"),
    ;

    @EnumValue
    private final int code;
    private final String descp;

    DbType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    private static final Map<Integer, DbType> DB_TYPE_MAP =
            Arrays.stream(DbType.values()).collect(toMap(DbType::getCode, Functions.identity()));

    public static DbType of(int type) {
        if (DB_TYPE_MAP.containsKey(type)) {
            return DB_TYPE_MAP.get(type);
        }
        return null;
    }

    public boolean isHive() {
        return this == DbType.HIVE;
    }
}
