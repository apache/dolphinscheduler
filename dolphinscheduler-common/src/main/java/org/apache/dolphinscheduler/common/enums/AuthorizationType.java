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
 * Authorization type
 */
public enum AuthorizationType {
    /**
     * 0 RESOURCE_FILE_ID;
     * 0 RESOURCE_FILE_NAME;
     * 1 UDF_FILE;
     * 1 DATASOURCE;
     * 2 UDF;
     */
    RESOURCE_FILE_ID(0, "resource file id"),
    RESOURCE_FILE_NAME(1, "resource file name"),
    UDF_FILE(2, "udf file"),
    DATASOURCE(3, "data source"),
    UDF(4, "udf function");

    AuthorizationType(int code, String descp){
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
}
