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
     * 1 RESOURCE_FILE_NAME;
     * 2 UDF_FILE;
     * 3 DATASOURCE;
     * 4 UDF;
     * 5 PROJECTS;
     * 6 WORKER_GROUP;
     * 7 ALERT_GROUP;
     * 8 ENVIRONMENT;
     * 9 ACCESS_TOKEN;
     * 10 QUEUE;
     * 11 DATA_ANALYSIS;
     * 12 K8S_NAMESPACE;
     * 13 MONITOR;
     * 14 ALERT_PLUGIN_INSTANCE;
     * 15 TENANT;
     * 16 USER;
     * 17 Data_Quality;
     */
    RESOURCE_FILE_ID(0, "resource file id"),
    RESOURCE_FILE_NAME(1, "resource file name"),
    UDF_FILE(2, "udf file"),
    DATASOURCE(3, "data source"),
    UDF(4, "udf function"),
    PROJECTS(5, "projects"),
    WORKER_GROUP(6, "worker group"),
    ALERT_GROUP(7, "alert group"),
    ENVIRONMENT(8, "environment"),
    ACCESS_TOKEN(9, "access token"),
    QUEUE(10, "queue"),
    DATA_ANALYSIS(11, "data analysis"),
    K8S_NAMESPACE(12, "k8s namespace"),
    MONITOR(13, "monitor"),
    ALERT_PLUGIN_INSTANCE(14, "alert plugin instance"),
    TENANT(15, "tenant"),
    DATA_QUALITY(16, "data quality"),
    TASK_GROUP(17, "task group"),
    ;

    AuthorizationType(int code, String descp) {
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
