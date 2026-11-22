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

package org.apache.dolphinscheduler.plugin.task.datavines;

public class DatavinesConstants {

    private DatavinesConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String TASK_TYPE = "DATAVINES";

    public static final String API_EXECUTE = "/api/v1/openapi/job/execute/";
    public static final String API_STATUS = "/api/v1/openapi/job/execution/status/";
    public static final String API_RESULT = "/api/v1/openapi/job/execution/result/";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_QUEUED = "QUEUED";
    public static final String STATUS_CANCELED = "CANCELED";
    public static final String STATUS_KILLED = "KILLED";

    public static final int DEFAULT_INTERVAL_SECONDS = 2;
    public static final int DEFAULT_TIMEOUT_SECONDS = 3600;
    public static final int DEFAULT_RETRY_TIMES = 3;
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10_000;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 30_000;

    public static final String OUT_EXECUTION_ID = "executionId";
    public static final String OUT_RESULT = "result";
}
