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
import java.util.NoSuchElementException;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.google.common.base.Functions;

public enum DbType {

    MYSQL(0, "mysql", "mysql"),
    POSTGRESQL(1, "postgresql", "postgresql"),
    HIVE(2, "hive", "hive"),
    SPARK(3, "spark", "spark"),
    CLICKHOUSE(4, "clickhouse", "clickhouse"),
    ORACLE(5, "oracle", "oracle"),
    SQLSERVER(6, "sqlserver", "sqlserver"),
    DB2(7, "db2", "db2"),
    PRESTO(8, "presto", "presto"),
    H2(9, "h2", "h2"),
    REDSHIFT(10, "redshift", "redshift"),
    ATHENA(11, "athena", "athena"),
    TRINO(12, "trino", "trino"),
    STARROCKS(13, "starrocks", "starrocks"),
    AZURESQL(14, "azuresql", "azuresql"),
    DAMENG(15, "dameng", "dameng"),
    OCEANBASE(16, "oceanbase", "oceanbase"),
    SSH(17, "ssh", "ssh"),
    KYUUBI(18, "kyuubi", "kyuubi"),
    DATABEND(19, "databend", "databend"),
    SNOWFLAKE(20, "snowflake", "snowflake"),
    VERTICA(21, "vertica", "vertica"),
    HANA(22, "hana", "hana"),
    DORIS(23, "doris", "doris"),
    ZEPPELIN(24, "zeppelin", "zeppelin"),
    SAGEMAKER(25, "sagemaker", "sagemaker"),

    K8S(26, "k8s", "k8s"),
    DOLPHINDB(27, "dolphindb", "dolphindb");
    private static final Map<Integer, DbType> DB_TYPE_MAP =
            Arrays.stream(DbType.values()).collect(toMap(DbType::getCode, Functions.identity()));
    @EnumValue
    private final int code;
    private final String name;
    private final String descp;

    DbType(int code, String name, String descp) {
        this.code = code;
        this.name = name;
        this.descp = descp;
    }

    public static DbType of(int type) {
        if (DB_TYPE_MAP.containsKey(type)) {
            return DB_TYPE_MAP.get(type);
        }
        return null;
    }

    public static DbType ofName(String name) {
        return Arrays.stream(DbType.values()).filter(e -> e.name().equals(name)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("no such db type"));
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescp() {
        return descp;
    }

    public boolean isHive() {
        return this == DbType.HIVE;
    }

    /**
     * support execute multiple segmented statements at a time
     *
     * @return
     */
    public boolean isSupportMultipleStatement() {
        return isHive() || this == DbType.SPARK;
    }
}
