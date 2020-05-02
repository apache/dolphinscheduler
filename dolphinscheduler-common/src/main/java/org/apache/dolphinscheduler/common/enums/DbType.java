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
package org.apache.dolphinscheduler.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * data base types
 */
public enum DbType {
    /**
     * 0 mysql
     * 1 postgresql
     * 2 hive
     * 3 spark
     * 4 clickhouse
     * 5 oracle
     * 6 sqlserver
     * 7 db2
     */
    MYSQL(0, "mysql"),
    POSTGRESQL(1, "postgresql"),
    HIVE(2, "hive"),
    SPARK(3, "spark"),
    CLICKHOUSE(4, "clickhouse"),
    ORACLE(5, "oracle"),
    SQLSERVER(6, "sqlserver"),
    DB2(7, "db2");

    DbType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }


    public static DbType of(int type){
        for(DbType ty : values()){
            if(ty.getCode() == type){
                return ty;
            }
        }
        throw new IllegalArgumentException("invalid type : " + type);
    }
}
